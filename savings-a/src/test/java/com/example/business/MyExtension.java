package com.example.business;

import com.example.business.multithreaded.FinancialEnvironment;
import com.example.business.multithreaded.FinancialEnvironment.Account;
import static com.example.util.LambdaUtil.apply;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.time.StopWatch;
import org.junit.jupiter.api.extension.AfterAllCallback;
import org.junit.jupiter.api.extension.ExtensionContext;
import org.junit.jupiter.api.extension.InvocationInterceptor;
import org.junit.jupiter.api.extension.ParameterContext;
import org.junit.jupiter.api.extension.ParameterResolutionException;
import org.junit.jupiter.api.extension.ParameterResolver;
import org.junit.jupiter.api.extension.ReflectiveInvocationContext;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;
import java.lang.reflect.Constructor;
import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.concurrent.CyclicBarrier;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;
import java.util.function.Supplier;

@Slf4j
public class MyExtension implements InvocationInterceptor, ParameterResolver, AfterAllCallback {


    @Target(ElementType.METHOD)
    @Retention(RetentionPolicy.RUNTIME)
    public @interface MyExtensionOptions {
        int numberOfThreads();

        String threadPoolDescription() default "";

        int numberOfAccounts();

        int moneyInEachAccount();

        int transferencesMadeByEachThread();

        long amountTransferred();

        Class<? extends AccountFactory<?>> accountFactory();
    }

    public interface AccountFactory<T extends Account> {
        T build(int idx, int size, int initialBalance);
    }

    public MyExtension() {
        log.debug("MyExtension");
    }

    @SuppressWarnings("rawtypes")
    @Override
    public void interceptTestMethod(
            Invocation<Void> invocation,
            ReflectiveInvocationContext<Method> invocationContext,
            ExtensionContext extensionContext) {
        log.debug("interceptTestMethod");

        Arrays.stream(invocationContext.getExecutable().getDeclaredAnnotations())
                .filter(annotation -> MyExtensionOptions.class.isAssignableFrom(annotation.annotationType()))
                .findAny()
                .map(annotation -> (MyExtensionOptions) annotation)
                .ifPresentOrElse(a -> {

                    // PS: unfortunately, junit 5 extensions `Invocation` blocks you from calling it repeatedly in this
                    // intercept method.
                    // There probably is a better way to do this, but this is the way I've done it.
                    // DON'T DO THIS IN A PROFESSIONAL ENVIRONMENT!
                    // I grab the instance of instance, which is of type ValidatingInvocation, and take the delegate
                    // field. This will be actual MethodInvocation that we need to call multiple times, and it does not
                    // validate if it is being called more than once.
                    // Then I call this MethodInvocation instead of the ValidatingInvocation that was wrapping it.

                    final Class<? extends Invocation> validatingInvocationClass = invocation.getClass();
                    final Field methodInvocationField = apply(() -> validatingInvocationClass.getDeclaredField(
                            "delegate"));
                    assert methodInvocationField != null;
                    methodInvocationField.setAccessible(true);
                    final Invocation actualMethodInvocation = (Invocation) apply(() -> methodInvocationField.get(
                            invocation));
                    assert actualMethodInvocation != null;

                    final var executorService = Executors.newFixedThreadPool(
                            a.numberOfThreads(),
                            new DescribableThreadFactory(a.threadPoolDescription()));

                    final CyclicBarrier barrier = new CyclicBarrier(a.numberOfThreads() + 1);
                    List<Future<?>> futures = new ArrayList<>(a.numberOfThreads());
                    for (int i = 0; i < a.numberOfThreads(); i++) {
                        futures.add(
                                executorService.submit(() -> {
                                    // wait for other threads and main thread to be ready
                                    apply(barrier::await);

                                    // call the test method
                                    apply(actualMethodInvocation::proceed);
                                })
                        );
                    }

                    // Then we need to set on the true ValidatingInvocation that we executed the delegated invocation
                    // "once"...
                    markInvoked(invocation, validatingInvocationClass);

                    StopWatch watch = new StopWatch();

                    // wait until threads are ready
                    apply(barrier::await);

                    watch.start();
                    futures.forEach(future -> apply(future::get));
                    watch.stop();

                    // print some statistics and information on stdout
                    printTestExecutionInformation(invocationContext, extensionContext, a, watch);

                    executorService.shutdown();

                }, () -> {
                    try {
                        InvocationInterceptor.super.interceptTestMethod(invocation,
                                invocationContext,
                                extensionContext);
                    } catch (Throwable e) {
                        e.printStackTrace();
                    }
                });
    }

    private void printTestExecutionInformation(ReflectiveInvocationContext<Method> invocationContext, ExtensionContext extensionContext,
            MyExtensionOptions a, StopWatch watch) {
        System.out.printf("%s %n", invocationContext.getExecutable().getName());
        System.out.printf("Took %d ms %n", watch.getTime(TimeUnit.MILLISECONDS));

        final var store = extensionContext.getStore(ExtensionContext.Namespace.GLOBAL);
        final FinancialEnvironment financialEnvironment = (FinancialEnvironment) store.get(
                FinancialEnvironment.class.getSimpleName());

        int initialTotalMoneyOnEnvironment = a.numberOfAccounts() * a.moneyInEachAccount();
        System.out.printf("World money before = %d and after = %d%n",
                initialTotalMoneyOnEnvironment,
                financialEnvironment.getTotalSumThreadSafe());
        System.out.printf("Account balances:%n%s%n",
                Arrays.toString(financialEnvironment.accountBalances()));
    }

    private void markInvoked(Invocation<Void> invocation, Class<? extends Invocation> validatingInvocationClass) {
        final Method markInvokedOrSkipped = apply(() -> validatingInvocationClass.getDeclaredMethod(
                "markInvokedOrSkipped"));
        assert markInvokedOrSkipped != null;
        markInvokedOrSkipped.setAccessible(true);
        apply(() -> markInvokedOrSkipped.invoke(invocation));
    }

    @Override
    public boolean supportsParameter(ParameterContext parameterContext,
            ExtensionContext extensionContext) throws ParameterResolutionException {
        return MyExtensionOptions.class.isAssignableFrom(parameterContext.getParameter().getType())
                || FinancialEnvironment.class.isAssignableFrom(parameterContext.getParameter().getType());
    }

    public <T> T getCachedOrInitializeAndGet(ExtensionContext.Store store, Class<T> clazz, Supplier<T> supplier) {
        T cast = clazz.cast(store.get(clazz.getSimpleName()));
        if (cast == null) {
            cast = supplier.get();
            store.put(clazz.getSimpleName(), cast);
        }
        return cast;
    }

    @Override
    public Object resolveParameter(ParameterContext parameterContext,
            ExtensionContext extensionContext) throws ParameterResolutionException {

        final ExtensionContext.Store store = extensionContext.getStore(ExtensionContext.Namespace.GLOBAL);

        final MyExtensionOptions myExtensionOptions1 = getCachedOrInitializeAndGet(store,
                MyExtensionOptions.class,
                () -> parameterContext.getDeclaringExecutable()
                        .getAnnotation(MyExtensionOptions.class)
        );

        final var accountFactory1 = (AccountFactory<? extends Account>) getCachedOrInitializeAndGet(store,
                AccountFactory.class,
                () -> {
                    final Class<? extends AccountFactory<? extends Account>> factoryClass = myExtensionOptions1.accountFactory();
                    final Constructor<? extends AccountFactory<? extends Account>> constructor = apply(factoryClass::getConstructor);
                    assert constructor != null;
                    return apply(constructor::newInstance);
                }
        );

        final FinancialEnvironment financialEnvironment = getCachedOrInitializeAndGet(store,
                FinancialEnvironment.class,
                () -> apply(() -> new FinancialEnvironment(
                        myExtensionOptions1.numberOfAccounts(),
                        (idx, size) -> apply(() -> accountFactory1
                                .build(idx, size,
                                        myExtensionOptions1.moneyInEachAccount()))))
        );

        if (MyExtensionOptions.class.isAssignableFrom(parameterContext.getParameter().getType())) {
            return myExtensionOptions1;
        } else if (FinancialEnvironment.class.isAssignableFrom(parameterContext.getParameter().getType())) {
            return financialEnvironment;
        }
        return null;
    }

    @Override
    public void afterAll(ExtensionContext context) throws Exception {
        Thread.sleep(5000);
    }
}

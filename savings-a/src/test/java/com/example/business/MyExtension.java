package com.example.business;

import com.example.business.FinancialEnvironment.Account;
import static com.example.util.LambdaUtil.apply;
import org.apache.commons.lang3.time.StopWatch;
import org.junit.jupiter.api.extension.DynamicTestInvocationContext;
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

public class MyExtension implements InvocationInterceptor, ParameterResolver {

    @Target(ElementType.METHOD)
    @Retention(RetentionPolicy.RUNTIME)
    public @interface MyExtensionOptions {
        int numberOfThreads();

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
//        System.out.println("MyExtension");
    }

    @Override
    public void interceptTestMethod(
            Invocation<Void> invocation,
            ReflectiveInvocationContext<Method> invocationContext,
            ExtensionContext extensionContext) throws Throwable {
//        System.out.println("interceptTestMethod");

        Arrays.stream(invocationContext.getExecutable().getDeclaredAnnotations())
                .filter(annotation -> MyExtensionOptions.class.isAssignableFrom(annotation.annotationType()))
                .findAny()
                .map(annotation -> (MyExtensionOptions) annotation)
                .ifPresentOrElse(a -> {

                    final Class<? extends Invocation> aClass = invocation.getClass();
                    final Field delegate = apply(() -> aClass.getDeclaredField("delegate"));
                    delegate.setAccessible(true);
                    final Invocation o = (Invocation)apply(()->delegate.get(invocation));

                    int initialTotalMoneyOnEnvironment = a.numberOfAccounts() * a.moneyInEachAccount();

                    final var executorService = Executors.newFixedThreadPool(
                            a.numberOfThreads(),
                            new MyThreadFactory(MtTransferenceStrategy01.class.getSimpleName()));

                    final CyclicBarrier barrier = new CyclicBarrier(a.numberOfThreads() + 1);
                    List<Future<?>> futures = new ArrayList<>(a.numberOfThreads());
                    for (int i = 0; i < a.numberOfThreads(); i++) {
                        futures.add(
                                executorService.submit(() -> {

                                    apply(barrier::await);
                                    apply(o::proceed);
                                })
                        );
                    }

                    final Method markInvokedOrSkipped = apply(() -> aClass.getDeclaredMethod("markInvokedOrSkipped"));
                    markInvokedOrSkipped.setAccessible(true);
                    apply(()->markInvokedOrSkipped.invoke(invocation));

                    StopWatch watch = new StopWatch();
                    apply(barrier::await);

                    watch.start();
                    futures.forEach(future -> {
                        apply(future::get);
                    });
                    watch.stop();
                    System.out.printf("%s %n", invocationContext.getExecutable().getName());
                    System.out.printf("Took %d ms %n", watch.getTime(TimeUnit.MILLISECONDS));
                    final var store = extensionContext.getStore(ExtensionContext.Namespace.GLOBAL);
                    final FinancialEnvironment financialEnvironment = (FinancialEnvironment)store.get(FinancialEnvironment.class.getSimpleName());
                    System.out.printf("World money before = %d and after = %d%n",
                            initialTotalMoneyOnEnvironment,
                            financialEnvironment.getTotalSumThreadSafe());
                    System.out.printf("Account balances:%n%s%n",
                            Arrays.toString(financialEnvironment.accountBalances()));

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

    @Override
    public boolean supportsParameter(ParameterContext parameterContext,
            ExtensionContext extensionContext) throws ParameterResolutionException {
//        System.out.printf("supportsParameter %s%n",
//                MyExtensionOptions.class.isAssignableFrom(parameterContext.getParameter().getType())
//                        || FinancialEnvironment.class.isAssignableFrom(parameterContext.getParameter().getType()));
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
        final AccountFactory accountFactory1 = getCachedOrInitializeAndGet(store,
                AccountFactory.class,
                () -> {
                    final Class<? extends AccountFactory<?>> factoryClass = myExtensionOptions1.accountFactory();
                    final Constructor<? extends AccountFactory<?>> constructor = apply(factoryClass::getConstructor);
                    assert constructor != null;
                    final AccountFactory<?> accountFactory = apply(constructor::newInstance);
                    return accountFactory;
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

        // FinancialEnvironment financialEnvironment = new FinancialEnvironment(
        //                                a.numberOfAccounts(), (idx, size) -> accountFactory.build(idx, size,
        //                                a.moneyInEachAccount()));

//        System.out.printf("resolveParameter %s %n", parameterContext.getParameter().getType());
        if (MyExtensionOptions.class.isAssignableFrom(parameterContext.getParameter().getType())) {
            final var a = myExtensionOptions1;
//            System.out.printf("resolved to value %s %n", a);
            return a;
        } else if (FinancialEnvironment.class.isAssignableFrom(parameterContext.getParameter().getType())) {
            final var o = financialEnvironment;
//            System.out.printf("resolved to value %s %n", o);
            return o;
        }
        return null;
    }

    @Override
    public <T> T interceptTestClassConstructor(
            Invocation<T> invocation,
            ReflectiveInvocationContext<Constructor<T>> invocationContext,
            ExtensionContext extensionContext) throws Throwable {
//        System.out.println("interceptTestClassConstructor");
        return InvocationInterceptor.super.interceptTestClassConstructor(invocation,
                invocationContext,
                extensionContext);
    }

    @Override
    public void interceptBeforeAllMethod(
            Invocation<Void> invocation,
            ReflectiveInvocationContext<Method> invocationContext,
            ExtensionContext extensionContext) throws Throwable {
//        System.out.println("interceptBeforeAllMethod");
        InvocationInterceptor.super.interceptBeforeAllMethod(invocation, invocationContext, extensionContext);
    }

    @Override
    public void interceptBeforeEachMethod(
            Invocation<Void> invocation,
            ReflectiveInvocationContext<Method> invocationContext,
            ExtensionContext extensionContext) throws Throwable {
//        System.out.println("interceptBeforeEachMethod");
        InvocationInterceptor.super.interceptBeforeEachMethod(invocation, invocationContext, extensionContext);
    }

    @Override
    public <T> T interceptTestFactoryMethod(Invocation<T> invocation,
            ReflectiveInvocationContext<Method> invocationContext,
            ExtensionContext extensionContext) throws Throwable {
//        System.out.println("interceptTestFactoryMethod");
        return InvocationInterceptor.super.interceptTestFactoryMethod(invocation, invocationContext, extensionContext);
    }

    @Override
    public void interceptTestTemplateMethod(
            Invocation<Void> invocation,
            ReflectiveInvocationContext<Method> invocationContext,
            ExtensionContext extensionContext) throws Throwable {
//        System.out.println("interceptTestTemplateMethod");
        InvocationInterceptor.super.interceptTestTemplateMethod(invocation, invocationContext, extensionContext);
    }

    @Override
    public void interceptDynamicTest(
            Invocation<Void> invocation, DynamicTestInvocationContext invocationContext,
            ExtensionContext extensionContext) throws Throwable {
//        System.out.println("interceptDynamicTest");
        InvocationInterceptor.super.interceptDynamicTest(invocation, invocationContext, extensionContext);
    }

    @Override
    public void interceptAfterEachMethod(
            Invocation<Void> invocation,
            ReflectiveInvocationContext<Method> invocationContext,
            ExtensionContext extensionContext) throws Throwable {
//        System.out.println("interceptAfterEachMethod");
        InvocationInterceptor.super.interceptAfterEachMethod(invocation, invocationContext, extensionContext);
    }

    @Override
    public void interceptAfterAllMethod(
            Invocation<Void> invocation,
            ReflectiveInvocationContext<Method> invocationContext,
            ExtensionContext extensionContext) throws Throwable {
//        System.out.println("interceptAfterAllMethod");
        InvocationInterceptor.super.interceptAfterAllMethod(invocation, invocationContext, extensionContext);
    }
}

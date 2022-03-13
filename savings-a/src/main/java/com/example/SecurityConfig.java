package com.example;

import org.springframework.context.annotation.Bean;
import org.springframework.security.config.annotation.authentication.builders.AuthenticationManagerBuilder;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configuration.WebSecurityConfigurerAdapter;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;

@EnableWebSecurity
public class SecurityConfig extends WebSecurityConfigurerAdapter {
    //    @Override
    //    protected void configure(HttpSecurity http) throws Exception {
    //        http
    //                .authorizeRequests()
    //                .anyRequest()
    //                .anonymous();
    ////                .and()
    ////                .csrf().csrfTokenRepository(CookieCsrfTokenRepository.withHttpOnlyFalse());
    //    }

    @Override
    protected void configure(HttpSecurity http) throws Exception {
        http.authorizeRequests()
                .antMatchers("/swagger-ui*/**", "/v3/api-docs/**")
                .permitAll()
                .mvcMatchers("/balance/admin/**")
                .hasAnyAuthority("ADMIN")
                .mvcMatchers("/balance/**")
                .hasAnyAuthority("USER")
                .anyRequest()
                .authenticated()
                .and()
                .csrf()
                .disable()
                .httpBasic();
    }

    @Override
    protected void configure(AuthenticationManagerBuilder auth) throws Exception {
        auth.inMemoryAuthentication()
                .withUser("swagger")
                .password(passwordEncoder().encode("swagger"))
                .authorities("USER")
                .and()
                .withUser("transference-svc")
                .password(passwordEncoder().encode("transference-svc"))
                .authorities("USER")
                .and()
                .withUser("admin")
                .password(passwordEncoder().encode("admin"))
                .authorities("USER", "ADMIN");
    }

    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }
}

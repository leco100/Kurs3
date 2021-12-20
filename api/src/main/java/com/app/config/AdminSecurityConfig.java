package com.app.config;


import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpStatus;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.WebSecurityConfigurerAdapter;
import org.springframework.security.web.authentication.HttpStatusEntryPoint;

/**
 * конфигурация безопасности
 */
@Configuration
public class AdminSecurityConfig extends WebSecurityConfigurerAdapter
{
    @Override
    protected void configure(HttpSecurity http) throws Exception
    {

        http.authorizeRequests().antMatchers( "/login**", "/logout**","/register**","/registerSuccessful","/bootstrap/**","/jquery/**","/popper/**",
                "/font-awesome/**","/css/**","/js/**","/h2-console/**").permitAll();
        http.authorizeRequests().antMatchers("/admin/**").access("hasAnyRole('ROLE_ADMIN')");
        http.authorizeRequests().antMatchers("/**").access("hasAnyRole('ROLE_ADMIN','ROLE_USER')");
        http.authorizeRequests().antMatchers("/api/**").permitAll();
        http.authorizeRequests().and().exceptionHandling().authenticationEntryPoint(new HttpStatusEntryPoint(HttpStatus.UNAUTHORIZED));


        http.csrf().disable()
                .authorizeRequests().anyRequest().authenticated()
                .and()
                .httpBasic();
        http.headers().frameOptions().disable();
        // Config for Login Form
        http.authorizeRequests().and().formLogin()//
                // Submit URL of login page.
                .loginProcessingUrl("/j_spring_security_check") // Submit URL
                .loginPage("/login")
                //.defaultSuccessUrl("/")//
                .failureUrl("/login?error=true")//
                .usernameParameter("username")//
                .passwordParameter("password")
                // Config for Logout Page
                .and().logout().logoutUrl("/logout").logoutSuccessUrl("/logoutSuccessful");
    }




}
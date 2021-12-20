package com.app.config;

import java.util.Locale;

import org.springframework.context.MessageSource;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.support.ReloadableResourceBundleMessageSource;
import org.springframework.web.servlet.LocaleResolver;
import org.springframework.web.servlet.config.annotation.InterceptorRegistry;
import org.springframework.web.servlet.config.annotation.ResourceHandlerRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;
import org.springframework.web.servlet.i18n.CookieLocaleResolver;
import org.springframework.web.servlet.i18n.LocaleChangeInterceptor;
import org.thymeleaf.spring5.SpringTemplateEngine;
import org.thymeleaf.templateresolver.ClassLoaderTemplateResolver;

/*
  * подключение jquery и bootstrap
 */
@Configuration
public class WebMvcConfig implements WebMvcConfigurer {

    @Override
    public void addResourceHandlers(ResourceHandlerRegistry registry) {

        registry.addResourceHandler("/jquery/**") //
                .addResourceLocations("classpath:/META-INF/resources/webjars/jquery/3.3.1-1/");
        registry.addResourceHandler("/select2/**") //
                .addResourceLocations("classpath:/META-INF/resources/webjars/select2/4.0.10/");

        registry.addResourceHandler("/popper/**") //
                .addResourceLocations("classpath:/META-INF/resources/webjars/popper.js/1.14.1/umd/");


        registry.addResourceHandler("/bootstrap/**") //
                .addResourceLocations("classpath:/META-INF/resources/webjars/bootstrap/4.1.1/");

        registry.addResourceHandler("/font-awesome/**")
                .addResourceLocations("classpath:/META-INF/resources/webjars/font-awesome/5.10.1/");
        //.addResourceLocations("classpath:/META-INF/resources/webjars/font-awesome/5.0.8/");
        registry.addResourceHandler("/img/**").addResourceLocations("classpath:/static/img/");

        registry.addResourceHandler("/js/**").addResourceLocations("classpath:/static/js/");




    }


    @Bean
    public MessageSource messageSource() {
        ReloadableResourceBundleMessageSource messageSource = new ReloadableResourceBundleMessageSource();
        // Load file: validation.properties
        messageSource.setBasename("classpath:validation");
        // підключимо файл локалізації
        messageSource.addBasenames("classpath:locale/messages");
        messageSource.setDefaultEncoding("UTF-8");
        return messageSource;
    }



    @Bean
    public LocaleResolver localeResolver() {
        CookieLocaleResolver localeResolver = new CookieLocaleResolver();
        localeResolver.setDefaultLocale(Locale.ENGLISH);
        return localeResolver;
    }

    @Override
    public void addInterceptors(InterceptorRegistry registry) {

        LocaleChangeInterceptor localeChangeInterceptor = new LocaleChangeInterceptor();
        localeChangeInterceptor.setParamName("lang");
        registry.addInterceptor(localeChangeInterceptor);
    }


    @Bean
    public ClassLoaderTemplateResolver templateResolver() {

        ClassLoaderTemplateResolver templateResolver = new ClassLoaderTemplateResolver();

        templateResolver.setPrefix("templates/");
        templateResolver.setCacheable(false);
        templateResolver.setSuffix(".html");
        templateResolver.setTemplateMode("HTML5");
        templateResolver.setCharacterEncoding("UTF-8");

        return templateResolver;
    }

    @Bean
    public SpringTemplateEngine templateEngine() {

        SpringTemplateEngine templateEngine = new SpringTemplateEngine();
        templateEngine.setTemplateResolver(templateResolver());
        //templateEngine.addDialect(new Java8TimeDialect());

        return templateEngine;
    }








}


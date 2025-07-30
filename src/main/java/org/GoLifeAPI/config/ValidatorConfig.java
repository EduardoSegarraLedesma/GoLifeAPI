package org.GoLifeAPI.config;

import org.hibernate.validator.HibernateValidator;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.validation.beanvalidation.LocalValidatorFactoryBean;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

@Configuration
public class ValidatorConfig implements WebMvcConfigurer {

    @Bean
    public LocalValidatorFactoryBean failFastValidatorFactory() {
        LocalValidatorFactoryBean factory = new LocalValidatorFactoryBean();
        factory.getValidationPropertyMap()
                .put("hibernate.validator.fail_fast", "true");
        factory.setProviderClass(HibernateValidator.class);
        return factory;
    }

    @Override
    public org.springframework.validation.Validator getValidator() {
        return failFastValidatorFactory();
    }
}
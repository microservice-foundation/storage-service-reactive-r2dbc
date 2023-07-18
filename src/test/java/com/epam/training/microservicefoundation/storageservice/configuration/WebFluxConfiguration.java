package com.epam.training.microservicefoundation.storageservice.configuration;

import com.epam.training.microservicefoundation.storageservice.handler.StorageExceptionHandler;
import com.epam.training.microservicefoundation.storageservice.handler.StorageHandler;
import com.epam.training.microservicefoundation.storageservice.model.dto.SaveStorageDTO;
import com.epam.training.microservicefoundation.storageservice.router.StorageRouter;
import com.epam.training.microservicefoundation.storageservice.service.implementation.StorageServiceImpl;
import com.epam.training.microservicefoundation.storageservice.validator.IdQueryParamValidator;
import com.epam.training.microservicefoundation.storageservice.validator.RequestBodyValidator;
import com.epam.training.microservicefoundation.storageservice.validator.RequestQueryParamValidator;
import org.springframework.boot.autoconfigure.web.WebProperties;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.boot.web.reactive.error.DefaultErrorAttributes;
import org.springframework.context.ApplicationContext;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Import;
import org.springframework.core.Ordered;
import org.springframework.core.annotation.Order;
import org.springframework.http.codec.ServerCodecConfigurer;
import org.springframework.http.codec.json.Jackson2JsonDecoder;
import org.springframework.validation.Validator;
import org.springframework.validation.beanvalidation.LocalValidatorFactoryBean;
import org.springframework.web.reactive.config.EnableWebFlux;
import org.springframework.web.reactive.config.WebFluxConfigurer;

@TestConfiguration
@EnableWebFlux
@Import(value = {StorageRouter.class, StorageHandler.class, IdQueryParamValidator.class, StorageServiceImpl.class})
@EnableConfigurationProperties(WebProperties.class)
public class WebFluxConfiguration implements WebFluxConfigurer {

  // @Order(Ordered.HIGHEST_PRECEDENCE) on ExceptionHandler class in Spring is used to define the order in which multiple exception handler classes get executed.
  // When multiple exception handler classes are present, the one with the highest precedence will be executed first.
  // The Ordered.HIGHEST_PRECEDENCE constant is used to set the order of the bean to the highest possible value. This ensures that the exception handler gets executed before any other error handling method, even the default Spring error handler.
  // This can be important if there are multiple exception handlers present and you want to ensure that a specific handler gets executed before any other.
  @Order(Ordered.HIGHEST_PRECEDENCE)
  @Bean
  public StorageExceptionHandler storageExceptionHandler(WebProperties webProperties, ApplicationContext applicationContext,
      ServerCodecConfigurer configurer) {
    StorageExceptionHandler exceptionHandler =
        new StorageExceptionHandler(new DefaultErrorAttributes(), webProperties.getResources(), applicationContext);

    exceptionHandler.setMessageReaders(configurer.getReaders());
    exceptionHandler.setMessageWriters(configurer.getWriters());
    return exceptionHandler;
  }

  @Override
  public void configureHttpMessageCodecs(ServerCodecConfigurer configurer) {
    configurer.defaultCodecs().jackson2JsonDecoder(new Jackson2JsonDecoder());
  }

  @Bean
  public Validator springValidator() {
    return new LocalValidatorFactoryBean();
  }

  @Bean
  public RequestBodyValidator<SaveStorageDTO> requestBodyValidator(Validator validator) {
    return new RequestBodyValidator<>(validator, SaveStorageDTO.class);
  }

  @Bean
  public RequestQueryParamValidator requestQueryParamValidator(IdQueryParamValidator idQueryParamValidator) {
    return new RequestQueryParamValidator(idQueryParamValidator);
  }
}

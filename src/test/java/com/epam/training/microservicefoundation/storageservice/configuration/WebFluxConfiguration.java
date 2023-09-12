package com.epam.training.microservicefoundation.storageservice.configuration;

import com.epam.training.microservicefoundation.storageservice.domain.dto.SaveStorageDTO;
import com.epam.training.microservicefoundation.storageservice.service.implementation.StorageServiceImpl;
import com.epam.training.microservicefoundation.storageservice.web.handler.StorageExceptionHandler;
import com.epam.training.microservicefoundation.storageservice.web.handler.StorageHandler;
import com.epam.training.microservicefoundation.storageservice.web.router.StorageRouter;
import com.epam.training.microservicefoundation.storageservice.web.validator.IdQueryParamValidator;
import com.epam.training.microservicefoundation.storageservice.web.validator.RequestBodyValidator;
import com.epam.training.microservicefoundation.storageservice.web.validator.RequestQueryParamValidator;
import java.util.HashSet;
import java.util.Set;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.web.ErrorProperties;
import org.springframework.boot.autoconfigure.web.WebProperties;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.boot.web.error.ErrorAttributeOptions;
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

  @Order(Ordered.HIGHEST_PRECEDENCE)
  @Bean
  public StorageExceptionHandler storageExceptionHandler(WebProperties webProperties, ApplicationContext applicationContext,
      ServerCodecConfigurer configurer, ErrorAttributeOptions errorAttributeOptions) {
    StorageExceptionHandler exceptionHandler =
        new StorageExceptionHandler(new DefaultErrorAttributes(), webProperties.getResources(), applicationContext, errorAttributeOptions);

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

  @Bean
  public ErrorAttributeOptions errorAttributeOptions(
      @Value("${server.error.include-message}") ErrorProperties.IncludeAttribute includeMessage,
      @Value("${server.error.include-stacktrace}") ErrorProperties.IncludeStacktrace includeStackTrace) {
    Set<ErrorAttributeOptions.Include>
        includes = new HashSet<>(Set.of(ErrorAttributeOptions.Include.EXCEPTION, ErrorAttributeOptions.Include.BINDING_ERRORS));
    if (includeMessage.equals(ErrorProperties.IncludeAttribute.ALWAYS)) {
      includes.add(ErrorAttributeOptions.Include.MESSAGE);
    }
    if (includeStackTrace.equals(ErrorProperties.IncludeStacktrace.ALWAYS)) {
      includes.add(ErrorAttributeOptions.Include.STACK_TRACE);
    }
    return ErrorAttributeOptions.of(includes);
  }
}

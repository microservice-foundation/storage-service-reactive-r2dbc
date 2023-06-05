package com.epam.training.microservices.storageservice.repository;

import static org.junit.jupiter.api.Assertions.assertTrue;

import com.epam.training.microservices.storageservice.common.CloudStorageExtension;
import com.epam.training.microservices.storageservice.configuration.AwsS3Configuration;
import com.epam.training.microservices.storageservice.configuration.S3ClientConfigurationProperties;
import java.time.Duration;
import java.util.Random;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.TestPropertySource;
import org.springframework.test.context.junit.jupiter.SpringExtension;
import reactor.test.StepVerifier;
import software.amazon.awssdk.core.exception.SdkClientException;
import software.amazon.awssdk.services.s3.model.NoSuchBucketException;

@ExtendWith(value = {SpringExtension.class, CloudStorageExtension.class})
@EnableConfigurationProperties(S3ClientConfigurationProperties.class)
@ContextConfiguration(classes = {AwsS3Configuration.class})
@TestPropertySource(locations = "classpath:application.properties")
class CloudStorageRepositoryTest {
  @Autowired
  private CloudStorageRepository repository;

  @Test
  void shouldCreateBucket() {
    String bucketName = bucketName();
    StepVerifier.create(repository.createBucket(bucketName))
        .assertNext(result -> {
          assertTrue( result.sdkHttpResponse().isSuccessful());
        })
        .verifyComplete();
  }

  @Test
  void shouldThrowExceptionWhenCreateBucketWithNullValue() {
    StepVerifier.create(repository.createBucket(null))
        .expectError(IllegalArgumentException.class)
        .verify();
  }

  @Test
  void shouldDeleteBucket() {
    String bucketName = bucketName();
    StepVerifier.create(repository.createBucket(bucketName))
        .assertNext(result -> {
          assertTrue( result.sdkHttpResponse().isSuccessful());
        })
        .verifyComplete();

    StepVerifier.withVirtualTime(() -> repository.deleteBucket(bucketName))
        .expectSubscription()
        .thenAwait(Duration.ofSeconds(1))
        .expectNextCount(0L)
        .verifyComplete();
  }

  @Test
  void shouldReturnOkWhenDeleteUnExistentBucket() {
    String bucketName = bucketName();
    StepVerifier.create(repository.deleteBucket(bucketName))
        .expectError(NoSuchBucketException.class)
        .verify();
  }

  @Test
  void shouldThrowExceptionWhenDeleteBucketWithNullValue() {
    StepVerifier.create(repository.deleteBucket(null))
        .expectError(SdkClientException.class)
        .verify();
  }

  @Test
  void shouldReturnOkWhenCheckIfExists() {
    String bucketName = bucketName();
    StepVerifier.create(repository.createBucket(bucketName))
        .assertNext(result -> {
          assertTrue( result.sdkHttpResponse().isSuccessful());
        })
        .verifyComplete();

    StepVerifier.create(repository.checkIfExists(bucketName))
        .expectSubscription()
        .expectNextCount(0)
        .expectComplete()
        .verify();
  }

  @Test
  void shouldReturnNoSuchBucketExceptionWhenCheckIfExists() {
    String bucketName = bucketName();

    StepVerifier.create(repository.checkIfExists(bucketName))
        .expectError(NoSuchBucketException.class)
        .verify();
  }

  private static String bucketName() {
    Random random = new Random();
    return "test-bucket-" + random.nextInt(1000);
  }
}

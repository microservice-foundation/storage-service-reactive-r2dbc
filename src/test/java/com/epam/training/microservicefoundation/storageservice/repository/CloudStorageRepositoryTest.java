package com.epam.training.microservicefoundation.storageservice.repository;

import com.epam.training.microservicefoundation.storageservice.common.CloudStorageExtension;
import com.epam.training.microservicefoundation.storageservice.configuration.AwsS3Configuration;
import com.epam.training.microservicefoundation.storageservice.configuration.S3ClientConfigurationProperties;
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
        .expectSubscription()
        .expectNextCount(0)
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
        .expectSubscription()
        .expectNextCount(0)
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
        .expectSubscription()
        .expectNextCount(0)
        .verifyComplete();

    StepVerifier.create(repository.sendHeadRequest(bucketName))
        .expectSubscription()
        .expectNextCount(0)
        .expectComplete()
        .verify();
  }

  @Test
  void shouldReturnNoSuchBucketExceptionWhenCheckIfExists() {
    String bucketName = bucketName();

    StepVerifier.create(repository.sendHeadRequest(bucketName))
        .expectError(NoSuchBucketException.class)
        .verify();
  }

  private static String bucketName() {
    Random random = new Random();
    return "test-bucket-" + random.nextInt(1000);
  }
}

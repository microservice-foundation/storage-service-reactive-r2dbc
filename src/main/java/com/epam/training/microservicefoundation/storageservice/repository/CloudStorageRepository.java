package com.epam.training.microservicefoundation.storageservice.repository;

import com.epam.training.microservicefoundation.storageservice.domain.exception.ExceptionSupplier;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import reactor.core.publisher.Mono;
import software.amazon.awssdk.core.SdkResponse;
import software.amazon.awssdk.http.SdkHttpResponse;
import software.amazon.awssdk.services.s3.S3AsyncClient;
import software.amazon.awssdk.services.s3.model.CreateBucketRequest;
import software.amazon.awssdk.services.s3.model.DeleteBucketRequest;
import software.amazon.awssdk.services.s3.model.HeadBucketRequest;

public class CloudStorageRepository {
  private static final Logger log = LoggerFactory.getLogger(CloudStorageRepository.class);
  private final S3AsyncClient s3Client;

  public CloudStorageRepository(S3AsyncClient s3Client) {
    this.s3Client = s3Client;
  }

  public Mono<Void> createBucket(String bucket) {
    log.info("Creating a bucket with name {}", bucket);
    CreateBucketRequest createBucketRequest = CreateBucketRequest.builder().bucket(bucket).build();
    return Mono.fromFuture(s3Client.createBucket(createBucketRequest))
        .flatMap(response -> {
          log.debug("Creating a bucket result: {}", response);
          return checkResult(response);
        });
  }

  public Mono<Void> deleteBucket(String bucket) {
    log.info("Deleting a bucket with name {}", bucket);
    DeleteBucketRequest deleteBucketRequest = DeleteBucketRequest.builder().bucket(bucket).build();
    return Mono.fromFuture(s3Client.deleteBucket(deleteBucketRequest))
        .flatMap(response -> {
          log.debug("Deleting a bucket result: {}", response);
          return checkResult(response);
        });
  }

  public Mono<Void> sendHeadRequest(String bucket) {
    log.info("Sending a HEAD request to check presence of a bucket with name {}", bucket);
    HeadBucketRequest headBucketRequest = HeadBucketRequest.builder().bucket(bucket).build();
    return Mono.fromFuture(s3Client.headBucket(headBucketRequest))
        .flatMap(response -> {
          log.debug("HEAD bucket request result: {}", response);
          return checkResult(response);
        });
  }
  private Mono<Void> checkResult(SdkResponse response) {
    SdkHttpResponse sdkResponse = response.sdkHttpResponse();
    if (sdkResponse != null && sdkResponse.isSuccessful()) {
      return Mono.empty();
    }
    return Mono.error(ExceptionSupplier.cloudStorageProcessFailed(response).get());
  }
}

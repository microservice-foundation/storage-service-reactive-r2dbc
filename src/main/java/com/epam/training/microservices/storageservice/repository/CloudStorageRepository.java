package com.epam.training.microservices.storageservice.repository;

import com.epam.training.microservices.storageservice.model.exceptions.CreateBucketFailedException;
import com.epam.training.microservices.storageservice.model.exceptions.DeleteBucketFailedException;
import com.epam.training.microservices.storageservice.model.exceptions.HeadBucketFailedException;
import java.util.HashMap;
import java.util.Map;
import java.util.function.Function;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import reactor.core.publisher.Mono;
import software.amazon.awssdk.core.SdkResponse;
import software.amazon.awssdk.http.SdkHttpResponse;
import software.amazon.awssdk.services.s3.S3AsyncClient;
import software.amazon.awssdk.services.s3.model.CreateBucketRequest;
import software.amazon.awssdk.services.s3.model.CreateBucketResponse;
import software.amazon.awssdk.services.s3.model.DeleteBucketRequest;
import software.amazon.awssdk.services.s3.model.DeleteBucketResponse;
import software.amazon.awssdk.services.s3.model.HeadBucketRequest;
import software.amazon.awssdk.services.s3.model.HeadBucketResponse;

public class CloudStorageRepository {
  private static final Logger log = LoggerFactory.getLogger(CloudStorageRepository.class);
  private final S3AsyncClient s3Client;
  private final Map<Class<? extends SdkResponse>, Function<SdkResponse, Mono<Void>>> checkExceptions;

  public CloudStorageRepository(S3AsyncClient s3Client) {
    this.s3Client = s3Client;
    checkExceptions = new HashMap<>();
    checkExceptions.put(CreateBucketResponse.class, response -> Mono.error(new CreateBucketFailedException(response)));
    checkExceptions.put(DeleteBucketResponse.class, response -> Mono.error(new DeleteBucketFailedException(response)));
    checkExceptions.put(HeadBucketResponse.class, response -> Mono.error(new HeadBucketFailedException(response)));
  }

  public Mono<CreateBucketResponse> createBucket(String bucket) {
    log.info("Creating a bucket with name {}", bucket);
    CreateBucketRequest createBucketRequest = CreateBucketRequest.builder().bucket(bucket).build();
    return Mono.fromFuture(s3Client.createBucket(createBucketRequest))
        .map(response -> {
          log.debug("Creating a bucket result: {}", response);
          checkResult(response);
          return response;
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

  public Mono<Void> checkIfExists(String bucketName) {
    log.info("Sending a HEAD request to check presence of a bucket with name {}", bucketName);
    HeadBucketRequest headBucketRequest = HeadBucketRequest.builder().bucket(bucketName).build();
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
    return checkExceptions.get(response.getClass()).apply(response);
  }
}

package com.epam.training.microservices.storageservice.model.exceptions;

import software.amazon.awssdk.core.SdkResponse;

public final class DeleteBucketFailedException extends BaseBucketException {
  public DeleteBucketFailedException(SdkResponse response) {
    super(response);
  }
}
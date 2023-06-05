package com.epam.training.microservices.storageservice.model.exceptions;

import software.amazon.awssdk.core.SdkResponse;

public class CreateBucketFailedException extends BaseBucketException {
  public CreateBucketFailedException(SdkResponse response) {
    super(response);
  }
}

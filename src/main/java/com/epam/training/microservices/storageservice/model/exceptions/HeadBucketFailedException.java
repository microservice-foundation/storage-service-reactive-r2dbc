package com.epam.training.microservices.storageservice.model.exceptions;

import software.amazon.awssdk.core.SdkResponse;

public class HeadBucketFailedException extends BaseBucketException {
  public HeadBucketFailedException(SdkResponse response) {
    super(response);
  }
}
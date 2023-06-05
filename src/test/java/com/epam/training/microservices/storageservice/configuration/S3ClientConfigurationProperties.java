package com.epam.training.microservices.storageservice.configuration;

import java.net.URI;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.boot.context.properties.ConstructorBinding;
import software.amazon.awssdk.regions.Region;

@ConfigurationProperties(prefix = "aws.s3")
@ConstructorBinding
public class S3ClientConfigurationProperties {
  private Region region;
  private URI endpoint;
  private String accessKey;
  private String secretKey;

  public S3ClientConfigurationProperties(Region region, URI endpoint, String accessKey, String secretKey, int maxRetry) {
    this.region = region;
    this.endpoint = endpoint;
    this.accessKey = accessKey;
    this.secretKey = secretKey;
    this.maxRetry = maxRetry;
  }

  private int maxRetry;

  // AWS S3 requires that file parts must have at least 5MB, except
  // for the last part. This may change for other S3-compatible services, so let't
  // define a configuration property for that
  private final int multipartMinPartSize = 5 * 1024 * 1024;

  public Region getRegion() {
    return region;
  }

  public void setRegion(Region region) {
    this.region = region;
  }

  public URI getEndpoint() {
    return endpoint;
  }

  public void setEndpoint(URI endpoint) {
    this.endpoint = endpoint;
  }

  public int getMaxRetry() {
    return maxRetry;
  }

  public void setMaxRetry(int maxRetry) {
    this.maxRetry = maxRetry;
  }

  public int getMultipartMinPartSize() {
    return multipartMinPartSize;
  }

  public String getAccessKey() {
    return accessKey;
  }

  public void setAccessKey(String accessKey) {
    this.accessKey = accessKey;
  }

  public String getSecretKey() {
    return secretKey;
  }

  public void setSecretKey(String secretKey) {
    this.secretKey = secretKey;
  }
}

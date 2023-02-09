/*
 * Copyright (C) 2019 OnGres, Inc.
 * SPDX-License-Identifier: AGPL-3.0-or-later
 */

package io.stackgres.common.prometheus;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.google.common.base.MoreObjects;
import io.quarkus.runtime.annotations.RegisterForReflection;
import io.sundr.builder.annotations.Buildable;

@RegisterForReflection
@JsonInclude(JsonInclude.Include.NON_DEFAULT)
@JsonIgnoreProperties(ignoreUnknown = true)
@Buildable(editableEnabled = false, validationEnabled = false, lazyCollectionInitEnabled = false)
public class Endpoint {

  private boolean honorLabels;
  private boolean honorTimestamps;
  private String port;
  private String path;
  private String scheme;
  private String interval;
  private String scrapeTimeout;

  public boolean isHonorLabels() {
    return honorLabels;
  }

  public void setHonorLabels(boolean honorLabels) {
    this.honorLabels = honorLabels;
  }

  public boolean isHonorTimestamps() {
    return honorTimestamps;
  }

  public void setHonorTimestamps(boolean honorTimestamps) {
    this.honorTimestamps = honorTimestamps;
  }

  public String getPort() {
    return port;
  }

  public void setPort(String port) {
    this.port = port;
  }

  public String getPath() {
    return path;
  }

  public void setPath(String path) {
    this.path = path;
  }

  public String getScheme() {
    return scheme;
  }

  public void setScheme(String scheme) {
    this.scheme = scheme;
  }

  public String getInterval() {
    return interval;
  }

  public void setInterval(String interval) {
    this.interval = interval;
  }

  public String getScrapeTimeout() {
    return scrapeTimeout;
  }

  public void setScrapeTimeout(String scrapeTimeout) {
    this.scrapeTimeout = scrapeTimeout;
  }

  @Override
  public String toString() {
    return MoreObjects.toStringHelper(this)
        .omitNullValues()
        .add("port", port)
        .add("path", path)
        .toString();
  }
}

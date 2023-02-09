/*
 * Copyright (C) 2019 OnGres, Inc.
 * SPDX-License-Identifier: AGPL-3.0-or-later
 */

package io.stackgres.common.prometheus;

import java.util.Map;
import java.util.Objects;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonInclude;
import io.fabric8.kubernetes.api.model.ObjectMeta;
import io.quarkus.runtime.annotations.RegisterForReflection;
import io.sundr.builder.annotations.Buildable;

@RegisterForReflection
@JsonInclude(JsonInclude.Include.NON_DEFAULT)
@JsonIgnoreProperties(ignoreUnknown = true)
@Buildable(editableEnabled = false, validationEnabled = false, lazyCollectionInitEnabled = false)
public class PrometheusInstallation {

  private String namespace;

  private String name;

  private Integer port;

  private ObjectMeta metadata;

  private PodMonitorSpec spec;

  private Map<String, String> matchLabels;

  @JsonIgnore
  public boolean match(PrometheusInstallation other) {
    return Objects.equals(namespace, other.namespace)
        && Objects.equals(name, other.name);
  }

  public String getNamespace() {
    return namespace;
  }

  public void setNamespace(String namespace) {
    this.namespace = namespace;
  }

  public String getName() {
    return name;
  }

  public void setName(String name) {
    this.name = name;
  }

  public Integer getPort() {
    return port;
  }

  public void setPort(Integer port) {
    this.port = port;
  }

  public ObjectMeta getMetadata() {
    return metadata;
  }

  public void setMetadata(ObjectMeta metadata) {
    this.metadata = metadata;
  }

  public PodMonitorSpec getSpec() {
    return spec;
  }

  public void setSpec(PodMonitorSpec spec) {
    this.spec = spec;
  }

  public Map<String, String> getMatchLabels() {
    return matchLabels;
  }

  public void setMatchLabels(Map<String, String> matchLabels) {
    this.matchLabels = matchLabels;
  }
}


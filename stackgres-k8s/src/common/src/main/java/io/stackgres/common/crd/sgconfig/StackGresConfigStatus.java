/*
 * Copyright (C) 2019 OnGres, Inc.
 * SPDX-License-Identifier: AGPL-3.0-or-later
 */

package io.stackgres.common.crd.sgconfig;

import java.util.Map;
import java.util.Optional;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonInclude;
import io.quarkus.runtime.annotations.RegisterForReflection;
import io.stackgres.common.StackGresUtil;
import io.stackgres.common.crd.JsonObject;

@RegisterForReflection
@JsonInclude(JsonInclude.Include.NON_DEFAULT)
@JsonIgnoreProperties(ignoreUnknown = true)
public class StackGresConfigStatus extends JsonObject {

  private static final long serialVersionUID = 1L;

  public StackGresConfigStatus() {
    super();
  }

  public StackGresConfigStatus(int initialCapacity, float loadFactor) {
    super(initialCapacity, loadFactor);
  }

  public StackGresConfigStatus(int initialCapacity) {
    super(initialCapacity);
  }

  public StackGresConfigStatus(Map<? extends String, ? extends Object> m) {
    super(m);
  }

  @JsonIgnore
  public String getVersion() {
    return Optional.ofNullable(get("version"))
        .map(String.class::cast)
        .orElse(null);
  }

  @JsonIgnore
  public void setVersion(String version) {
    put("version", version);
  }

  @Override
  public String toString() {
    return StackGresUtil.toPrettyYaml(this);
  }
}

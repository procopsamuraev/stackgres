/*
 * Copyright (C) 2019 OnGres, Inc.
 * SPDX-License-Identifier: AGPL-3.0-or-later
 */

package io.stackgres.common.crd.sgcluster;

import java.util.HashMap;
import java.util.Map;
import java.util.Objects;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonInclude;
import io.fabric8.kubernetes.model.annotation.Group;
import io.fabric8.kubernetes.model.annotation.Kind;
import io.fabric8.kubernetes.model.annotation.Version;
import io.quarkus.runtime.annotations.RegisterForReflection;
import io.stackgres.common.StackGresUtil;
import io.sundr.builder.annotations.Buildable;

@RegisterForReflection
@JsonInclude(JsonInclude.Include.NON_DEFAULT)
@JsonIgnoreProperties(ignoreUnknown = true)
@Buildable(editableEnabled = false, validationEnabled = false, generateBuilderPackage = false,
    lazyCollectionInitEnabled = false, lazyMapInitEnabled = false,
    builderPackage = "io.fabric8.kubernetes.api.builder")
@Group(StackGresServiceBinding.GROUP)
@Version(StackGresServiceBinding.VERSION)
@Kind(StackGresServiceBinding.KIND)
public class StackGresServiceBinding {
  public static final String GROUP = "servicebinding.io";

  public static final String VERSION = "v1";

  public static final String KIND = "Secret";

  private String type;

  private Map<String, String> stringData = new HashMap<>();

  public String getType() {
    return type;
  }

  public void setType(String type) {
    this.type = String.format(GROUP + "/%s", type);
  }

  public Map<String, String> getStringData() {
    return stringData;
  }

  public void setStringData(Map<String, String> stringData) {
    this.stringData = stringData;
  }

  @Override
  public int hashCode() {
    return Objects.hash(stringData);
  }

  @Override
  public boolean equals(Object obj) {
    if (this == obj) {
      return true;
    }
    if (!(obj instanceof StackGresServiceBinding)) {
      return false;
    }
    StackGresServiceBinding other = (StackGresServiceBinding) obj;
    return Objects.equals(stringData, other.stringData);
  }

  @Override
  public String toString() {
    return StackGresUtil.toPrettyYaml(this);
  }
}

/*
 * Copyright (C) 2019 OnGres, Inc.
 * SPDX-License-Identifier: AGPL-3.0-or-later
 */

package io.stackgres.apiweb.dto.cluster;

import java.util.Objects;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import io.quarkus.runtime.annotations.RegisterForReflection;
import io.stackgres.common.StackGresUtil;

@JsonDeserialize
@JsonInclude(JsonInclude.Include.NON_DEFAULT)
@RegisterForReflection
public class ClusterReplicateFromInstance {

  @JsonProperty("external")
  private ClusterReplicateFromExternal external;

  public ClusterReplicateFromExternal getExternal() {
    return external;
  }

  public void setExternal(ClusterReplicateFromExternal external) {
    this.external = external;
  }

  @Override
  public int hashCode() {
    return Objects.hash(external);
  }

  @Override
  public boolean equals(Object obj) {
    if (this == obj) {
      return true;
    }
    if (!(obj instanceof ClusterReplicateFromInstance)) {
      return false;
    }
    ClusterReplicateFromInstance other = (ClusterReplicateFromInstance) obj;
    return Objects.equals(external, other.external);
  }

  @Override
  public String toString() {
    return StackGresUtil.toPrettyYaml(this);
  }
}

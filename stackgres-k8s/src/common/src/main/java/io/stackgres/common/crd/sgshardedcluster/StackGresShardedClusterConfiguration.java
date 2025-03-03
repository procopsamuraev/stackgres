/*
 * Copyright (C) 2019 OnGres, Inc.
 * SPDX-License-Identifier: AGPL-3.0-or-later
 */

package io.stackgres.common.crd.sgshardedcluster;

import java.util.List;
import java.util.Objects;

import javax.validation.Valid;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import io.quarkus.runtime.annotations.RegisterForReflection;
import io.stackgres.common.StackGresUtil;
import io.stackgres.common.crd.sgcluster.StackGresClusterCredentials;
import io.sundr.builder.annotations.Buildable;

@RegisterForReflection
@JsonInclude(JsonInclude.Include.NON_DEFAULT)
@JsonIgnoreProperties(ignoreUnknown = true)
@Buildable(editableEnabled = false, validationEnabled = false, generateBuilderPackage = false,
    lazyCollectionInitEnabled = false, lazyMapInitEnabled = false,
    builderPackage = "io.fabric8.kubernetes.api.builder")
public class StackGresShardedClusterConfiguration {

  @JsonProperty("backups")
  @Valid
  private List<StackGresShardedClusterBackupConfiguration> backups;

  @JsonProperty("credentials")
  @Valid
  private StackGresClusterCredentials credentials;

  public List<StackGresShardedClusterBackupConfiguration> getBackups() {
    return backups;
  }

  public void setBackups(List<StackGresShardedClusterBackupConfiguration> backups) {
    this.backups = backups;
  }

  public StackGresClusterCredentials getCredentials() {
    return credentials;
  }

  public void setCredentials(StackGresClusterCredentials credentials) {
    this.credentials = credentials;
  }

  @Override
  public int hashCode() {
    return Objects.hash(backups, credentials);
  }

  @Override
  public boolean equals(Object obj) {
    if (this == obj) {
      return true;
    }
    if (!(obj instanceof StackGresShardedClusterConfiguration)) {
      return false;
    }
    StackGresShardedClusterConfiguration other = (StackGresShardedClusterConfiguration) obj;
    return Objects.equals(backups, other.backups) && Objects.equals(credentials, other.credentials);
  }

  @Override
  public String toString() {
    return StackGresUtil.toPrettyYaml(this);
  }

}

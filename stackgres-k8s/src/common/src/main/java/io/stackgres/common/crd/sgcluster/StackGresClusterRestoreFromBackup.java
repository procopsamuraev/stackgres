/*
 * Copyright (C) 2019 OnGres, Inc.
 * SPDX-License-Identifier: AGPL-3.0-or-later
 */

package io.stackgres.common.crd.sgcluster;

import java.util.Objects;

import javax.validation.Valid;
import javax.validation.constraints.AssertTrue;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import io.quarkus.runtime.annotations.RegisterForReflection;
import io.stackgres.common.StackGresUtil;
import io.stackgres.common.validation.FieldReference;
import io.stackgres.common.validation.FieldReference.ReferencedField;
import io.sundr.builder.annotations.Buildable;

@RegisterForReflection
@JsonInclude(JsonInclude.Include.NON_DEFAULT)
@JsonIgnoreProperties(ignoreUnknown = true)
@Buildable(editableEnabled = false, validationEnabled = false, lazyCollectionInitEnabled = false)
public class StackGresClusterRestoreFromBackup {

  @JsonProperty("uid")
  private String uid;

  @JsonProperty("name")
  private String name;

  @JsonProperty("pointInTimeRecovery")
  @Valid
  private StackGresClusterRestorePitr pointInTimeRecovery;

  @ReferencedField("name")
  interface Name extends FieldReference { }

  @JsonIgnore
  @AssertTrue(message = "name cannot be null",
      payload = { Name.class })
  public boolean isNameNotNullOrUidNotNull() {
    return (name != null && uid == null) // NOPMD
        || (name == null && uid != null); // NOPMD
  }

  public String getUid() {
    return uid;
  }

  public void setUid(String uid) {
    this.uid = uid;
  }

  public String getName() {
    return name;
  }

  public void setName(String name) {
    this.name = name;
  }

  public StackGresClusterRestorePitr getPointInTimeRecovery() {
    return pointInTimeRecovery;
  }

  public void setPointInTimeRecovery(StackGresClusterRestorePitr pointInTimeRecovery) {
    this.pointInTimeRecovery = pointInTimeRecovery;
  }

  @Override
  public boolean equals(Object obj) {
    if (this == obj) {
      return true;
    }
    if (!(obj instanceof StackGresClusterRestoreFromBackup)) {
      return false;
    }
    StackGresClusterRestoreFromBackup other = (StackGresClusterRestoreFromBackup) obj;
    return Objects.equals(name, other.name)
        && Objects.equals(pointInTimeRecovery, other.pointInTimeRecovery)
        && Objects.equals(uid, other.uid);
  }

  @Override
  public int hashCode() {
    return Objects.hash(name, pointInTimeRecovery, uid);
  }

  @Override
  public String toString() {
    return StackGresUtil.toPrettyYaml(this);
  }
}

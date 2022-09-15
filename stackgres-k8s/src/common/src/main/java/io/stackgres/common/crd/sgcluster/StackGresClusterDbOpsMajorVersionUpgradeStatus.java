/*
 * Copyright (C) 2019 OnGres, Inc.
 * SPDX-License-Identifier: AGPL-3.0-or-later
 */

package io.stackgres.common.crd.sgcluster;

import java.util.Objects;

import javax.validation.constraints.NotNull;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import io.quarkus.runtime.annotations.RegisterForReflection;
import io.stackgres.common.StackGresUtil;
import io.sundr.builder.annotations.Buildable;

@RegisterForReflection
@JsonInclude(JsonInclude.Include.NON_DEFAULT)
@JsonIgnoreProperties(ignoreUnknown = true)
@Buildable(editableEnabled = false, validationEnabled = false, lazyCollectionInitEnabled = false)
public class StackGresClusterDbOpsMajorVersionUpgradeStatus extends ClusterDbOpsRestartStatus {

  @JsonProperty("sourcePostgresVersion")
  @NotNull
  private String sourcePostgresVersion;

  @JsonProperty("targetPostgresVersion")
  @NotNull
  private String targetPostgresVersion;

  @JsonProperty("locale")
  @NotNull
  private String locale;

  @JsonProperty("encoding")
  @NotNull
  private String encoding;

  @JsonProperty("dataChecksum")
  @NotNull
  private Boolean dataChecksum;

  @JsonProperty("link")
  @NotNull
  private Boolean link;

  @JsonProperty("clone")
  @NotNull
  private Boolean clone;

  @JsonProperty("check")
  @NotNull
  private Boolean check;

  public String getSourcePostgresVersion() {
    return sourcePostgresVersion;
  }

  public void setSourcePostgresVersion(String sourcePostgresVersion) {
    this.sourcePostgresVersion = sourcePostgresVersion;
  }

  public String getTargetPostgresVersion() {
    return targetPostgresVersion;
  }

  public void setTargetPostgresVersion(String targetPostgresVersion) {
    this.targetPostgresVersion = targetPostgresVersion;
  }

  public String getLocale() {
    return locale;
  }

  public void setLocale(String locale) {
    this.locale = locale;
  }

  public String getEncoding() {
    return encoding;
  }

  public void setEncoding(String encoding) {
    this.encoding = encoding;
  }

  public Boolean getDataChecksum() {
    return dataChecksum;
  }

  public void setDataChecksum(Boolean dataChecksum) {
    this.dataChecksum = dataChecksum;
  }

  public Boolean getLink() {
    return link;
  }

  public void setLink(Boolean link) {
    this.link = link;
  }

  public Boolean getClone() {
    return clone;
  }

  public void setClone(Boolean clone) {
    this.clone = clone;
  }

  public Boolean getCheck() {
    return check;
  }

  public void setCheck(Boolean check) {
    this.check = check;
  }

  @Override
  public int hashCode() {
    final int prime = 31;
    int result = super.hashCode();
    result = prime * result + Objects.hash(check, clone, dataChecksum, encoding, link, locale,
        sourcePostgresVersion, targetPostgresVersion);
    return result;
  }

  @Override
  public boolean equals(Object obj) {
    if (this == obj) {
      return true;
    }
    if (!super.equals(obj)) {
      return false;
    }
    if (!(obj instanceof StackGresClusterDbOpsMajorVersionUpgradeStatus)) {
      return false;
    }
    StackGresClusterDbOpsMajorVersionUpgradeStatus other =
        (StackGresClusterDbOpsMajorVersionUpgradeStatus) obj;
    return Objects.equals(check, other.check) && Objects.equals(clone, other.clone)
        && Objects.equals(dataChecksum, other.dataChecksum)
        && Objects.equals(encoding, other.encoding) && Objects.equals(link, other.link)
        && Objects.equals(locale, other.locale)
        && Objects.equals(sourcePostgresVersion, other.sourcePostgresVersion)
        && Objects.equals(targetPostgresVersion, other.targetPostgresVersion);
  }

  @Override
  public String toString() {
    return StackGresUtil.toPrettyYaml(this);
  }

}

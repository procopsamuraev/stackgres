/*
 * Copyright (C) 2019 OnGres, Inc.
 * SPDX-License-Identifier: AGPL-3.0-or-later
 */

package io.stackgres.common;

import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import com.google.common.base.Preconditions;
import io.fabric8.kubernetes.api.model.EnvVar;
import io.fabric8.kubernetes.api.model.EnvVarBuilder;

public enum GateCollectorPath implements VolumePath {

  ETC_OTEL_CONTRIB_CONFIG_PATH("/etc/otelcol-contrib/config.yaml");

  private final String path;

  GateCollectorPath(String path) {
    this.path = path;
  }

  GateCollectorPath(String... paths) {
    this(String.join("/", paths));
  }

  GateCollectorPath(GateCollectorPath parent, String... paths) {
    this(parent.path, String.join("/", paths));
  }

  @Override
  public String path() {
    return path(Map.of());
  }

  @Override
  public String path(ClusterContext context) {
    return path(context.getEnvironmentVariables());
  }

  @Override
  public String path(ClusterContext context, Map<String, String> envVars) {
    return path(envVars(context, envVars));
  }

  @Override
  public String path(Map<String, String> envVars) {
    StringBuilder path = new StringBuilder();
    int startIndexOf = this.path.indexOf("$(");
    int endIndexOf = -1;
    while (startIndexOf >= 0) {
      path.append(this.path, endIndexOf + 1, startIndexOf);
      endIndexOf = this.path.indexOf(")", startIndexOf);
      if (endIndexOf == -1) {
        throw new IllegalArgumentException(
            "Path " + this.path + " do not close variable substitution."
                + " Expected a `)` character after position " + startIndexOf);
      }
      String variable = this.path.substring(startIndexOf + 2, endIndexOf);
      String value = envVars.get(variable);
      if (value == null) {
        throw new IllegalArgumentException("Path " + this.path + " specify variable " + variable
            + " for substitution. But was not found in map " + envVars);
      }
      path.append(value);
      startIndexOf = this.path.indexOf("$(", endIndexOf + 1);
    }
    if (endIndexOf == -1) {
      return this.path;
    }
    if (endIndexOf < this.path.length()) {
      path.append(this.path, endIndexOf + 1, this.path.length());
    }
    return path.toString();
  }

  @Override
  public String filename() {
    return filename(Map.of());
  }

  @Override
  public String filename(ClusterContext context) {
    return filename(context.getEnvironmentVariables());
  }

  @Override
  public String filename(ClusterContext context, Map<String, String> envVars) {
    return filename(envVars(context, envVars));
  }

  @Override
  public String filename(Map<String, String> envVars) {
    String pathFile = path(envVars);
    int indexOfLastSlash = pathFile.lastIndexOf('/');
    return indexOfLastSlash != -1 ? pathFile.substring(indexOfLastSlash + 1) : pathFile;
  }

  @Override
  public String subPath() {
    return subPath(Map.of());
  }

  @Override
  public String subPath(ClusterContext context) {
    return subPath(context.getEnvironmentVariables());
  }

  @Override
  public String subPath(ClusterContext context, Map<String, String> envVars) {
    return subPath(envVars(context, envVars));
  }

  @Override
  public String subPath(Map<String, String> envVars) {
    return path(envVars).substring(1);
  }

  @Override
  public String subPath(VolumePath relativeTo) {
    return relativize(subPath(Map.of()), relativeTo.subPath(Map.of()));
  }

  @Override
  public String subPath(ClusterContext context, VolumePath relativeTo) {
    return relativize(subPath(context.getEnvironmentVariables()),
        relativeTo.subPath(context.getEnvironmentVariables()));
  }

  @Override
  public String subPath(ClusterContext context, Map<String, String> envVars,
      VolumePath relativeTo) {
    return relativize(subPath(envVars(context, envVars)),
        relativeTo.subPath(envVars(context, envVars)));
  }

  @Override
  public String subPath(Map<String, String> envVars, VolumePath relativeTo) {
    return relativize(subPath(envVars), relativeTo.subPath(envVars));
  }

  private String relativize(String subPath, String relativeToSubPath) {
    Preconditions.checkArgument(subPath.startsWith(relativeToSubPath + "/"),
        subPath + " is not relative to " + relativeToSubPath + "/");
    return subPath.substring(relativeToSubPath.length() + 1);
  }

  public EnvVar envVar() {
    return envVar(Map.of());
  }

  public EnvVar envVar(ClusterContext context) {
    return envVar(context.getEnvironmentVariables());
  }

  public EnvVar envVar(ClusterContext context, Map<String, String> envVars) {
    return envVar(envVars(context, envVars));
  }

  public EnvVar envVar(Map<String, String> envVars) {
    return new EnvVarBuilder()
        .withName(name())
        .withValue(path(envVars))
        .build();
  }

  private Map<String, String> envVars(ClusterContext context, Map<String, String> envVars) {
    Map<String, String> mergedEnvVars = new HashMap<>(context.getEnvironmentVariables());
    mergedEnvVars.putAll(envVars);
    return Map.copyOf(mergedEnvVars);
  }

  public static List<EnvVar> envVars(ClusterContext context) {
    return Arrays
        .stream(values())
        .map(path -> path.envVar(context))
        .toList();
  }

}

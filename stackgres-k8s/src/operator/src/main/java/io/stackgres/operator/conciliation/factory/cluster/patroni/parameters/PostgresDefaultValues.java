/*
 * Copyright (C) 2019 OnGres, Inc.
 * SPDX-License-Identifier: AGPL-3.0-or-later
 */

package io.stackgres.operator.conciliation.factory.cluster.patroni.parameters;

import java.util.Map;
import java.util.Objects;
import java.util.Properties;

import com.google.common.collect.Maps;
import io.stackgres.common.StackGresUtil;
import io.stackgres.common.StackGresVersion;
import org.jetbrains.annotations.NotNull;

public interface PostgresDefaultValues {

  enum PostgresDefaulValuesProperties {
    PG_DEFAULT_VALUES("/postgresql-default-values.properties"),
    PG_13_VALUES("/postgresql-default-values-pg13.properties"),
    PG_DEFAULT_VALUES_V_1_1("/v1.1/postgresql-default-values-v1.1.properties"),
    PG_13_VALUES_V_1_1("/v1.1/postgresql-default-values-pg13-v1.1.properties");

    private final @NotNull Properties properties;

    PostgresDefaulValuesProperties(@NotNull String file) {
      this.properties = StackGresUtil.loadProperties(file);
    }
  }

  static @NotNull Properties getProperties(
      @NotNull String pgVersion) {
    return getProperties(StackGresVersion.LATEST, pgVersion);
  }

  static @NotNull Properties getProperties(
      @NotNull StackGresVersion version,
      @NotNull String pgVersion) {
    Objects.requireNonNull(version, "operatorVersion parameter is null");
    Objects.requireNonNull(pgVersion, "pgVersion parameter is null");
    int majorVersion = Integer.parseInt(pgVersion.split("\\.")[0]);
    if (version.getVersionAsNumber() > StackGresVersion.V_1_1.getVersionAsNumber()) {
      if (majorVersion >= 13) {
        return PostgresDefaulValuesProperties.PG_13_VALUES.properties;
      }
      return PostgresDefaulValuesProperties.PG_DEFAULT_VALUES.properties;
    } else {
      if (majorVersion >= 13) {
        return PostgresDefaulValuesProperties.PG_13_VALUES_V_1_1.properties;
      }
      return PostgresDefaulValuesProperties.PG_DEFAULT_VALUES_V_1_1.properties;
    }
  }

  static @NotNull Map<String, String> getDefaultValues(
      @NotNull String pgVersion) {
    return getDefaultValues(StackGresVersion.LATEST, pgVersion);
  }

  static @NotNull Map<String, String> getDefaultValues(
      @NotNull StackGresVersion version,
      @NotNull String pgVersion) {
    return Maps.fromProperties(getProperties(version, pgVersion));
  }

}

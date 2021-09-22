/*
 * Copyright (C) 2019 OnGres, Inc.
 * SPDX-License-Identifier: AGPL-3.0-or-later
 */

package io.stackgres.common.postgres;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.util.Properties;

import javax.inject.Singleton;
import javax.validation.constraints.NotNull;

@Singleton
public class PostgresConnectionManager {

  public Connection getConnection(@NotNull String host, @NotNull String database,
      @NotNull String username, @NotNull String password)
      throws SQLException {
    Properties properties = new Properties();
    properties.setProperty("user", username);
    properties.setProperty("password", password);
    return DriverManager.getConnection("jdbc:postgresql://" + host + "/" + database, properties);
  }

}

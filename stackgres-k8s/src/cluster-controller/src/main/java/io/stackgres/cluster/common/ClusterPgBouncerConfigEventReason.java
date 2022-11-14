/*
 * Copyright (C) 2019 OnGres, Inc.
 * SPDX-License-Identifier: AGPL-3.0-or-later
 */

package io.stackgres.cluster.common;

import static io.stackgres.operatorframework.resource.EventReason.Type.NORMAL;

import io.stackgres.common.StackGresContainer;
import io.stackgres.operatorframework.resource.EventReason;

public enum ClusterPgBouncerConfigEventReason implements EventReason {

  CLUSTER_PGBOUNCER_CONFIG_UPDATED(NORMAL, "ClusterPgBouncerConfigUpdated");

  private final Type type;
  private final String reason;

  ClusterPgBouncerConfigEventReason(Type type, String reason) {
    this.type = type;
    this.reason = reason;
  }

  @Override
  public String component() {
    return StackGresContainer.CLUSTER_CONTROLLER.getName();
  }

  @Override
  public String reason() {
    return reason;
  }

  @Override
  public Type type() {
    return type;
  }

}

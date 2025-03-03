/*
 * Copyright (C) 2019 OnGres, Inc.
 * SPDX-License-Identifier: AGPL-3.0-or-later
 */

package io.stackgres.operator.conciliation.factory;

import java.util.Map;

import io.fabric8.kubernetes.api.model.Container;
import io.stackgres.common.StackGresProperty;

public interface ContainerFactory<T extends ContainerContext> {

  default boolean isActivated(T context) {
    return true;
  }

  Container getContainer(T context);

  default Map<String, String> getComponentVersions(T context) {
    return Map.of();
  }

  default String getDefaultPullPolicy() {
    return StackGresProperty.SG_IMAGE_PULL_POLICY.getString();
  }
}

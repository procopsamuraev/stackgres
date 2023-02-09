/*
 * Copyright (C) 2019 OnGres, Inc.
 * SPDX-License-Identifier: AGPL-3.0-or-later
 */

package io.stackgres.common.labels;

import java.util.Map;

import io.stackgres.common.crd.sgconfig.StackGresConfig;

public interface LabelFactoryForConfig
    extends LabelFactory<StackGresConfig> {

  Map<String, String> gateCollectorLabels(StackGresConfig resource);

  Map<String, String> configCrossNamespaceLabels(StackGresConfig resource);

  @Override
  LabelMapperForConfig labelMapper();

}

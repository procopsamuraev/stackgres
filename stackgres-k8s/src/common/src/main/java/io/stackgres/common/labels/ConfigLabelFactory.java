/*
 * Copyright (C) 2019 OnGres, Inc.
 * SPDX-License-Identifier: AGPL-3.0-or-later
 */

package io.stackgres.common.labels;

import static io.stackgres.common.resource.ResourceUtil.labelValue;

import java.util.Map;

import javax.enterprise.context.ApplicationScoped;
import javax.inject.Inject;

import io.stackgres.common.crd.sgconfig.StackGresConfig;

@ApplicationScoped
public class ConfigLabelFactory extends AbstractLabelFactoryForConfig {

  private final LabelMapperForConfig labelMapper;

  @Inject
  public ConfigLabelFactory(LabelMapperForConfig labelMapper) {
    this.labelMapper = labelMapper;
  }

  @Override
  public LabelMapperForConfig labelMapper() {
    return labelMapper;
  }

  public Map<String, String> gateCollectorLabels(StackGresConfig resource) {
    return Map.of(labelMapper().appKey(), labelMapper().appName(),
        labelMapper().resourceNamespaceKey(resource), labelValue(resourceNamespace(resource)),
        labelMapper().resourceUidKey(resource), labelValue(resourceUid(resource)),
        labelMapper().resourceNameKey(resource), labelValue(resourceName(resource)));
  }

  public Map<String, String> configCrossNamespaceLabels(StackGresConfig resource) {
    return Map.of(labelMapper().appKey(), labelMapper().appName(),
        labelMapper().resourceNamespaceKey(resource), labelValue(resourceNamespace(resource)),
        labelMapper().resourceUidKey(resource), labelValue(resourceUid(resource)),
        labelMapper().resourceNameKey(resource), labelValue(resourceName(resource)));
  }

}

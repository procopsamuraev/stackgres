/*
 * Copyright (C) 2019 OnGres, Inc.
 * SPDX-License-Identifier: AGPL-3.0-or-later
 */

package io.stackgres.operator.conciliation.config;

import java.util.Map;
import java.util.function.Function;

import javax.enterprise.context.ApplicationScoped;
import javax.inject.Inject;

import io.fabric8.kubernetes.api.model.HasMetadata;
import io.fabric8.kubernetes.api.model.KubernetesResourceList;
import io.fabric8.kubernetes.client.KubernetesClient;
import io.fabric8.kubernetes.client.dsl.MixedOperation;
import io.fabric8.kubernetes.client.dsl.Resource;
import io.stackgres.common.OperatorProperty;
import io.stackgres.common.crd.sgconfig.StackGresConfig;
import io.stackgres.common.labels.LabelFactoryForConfig;
import io.stackgres.operator.conciliation.DeployedResourcesScanner;
import io.stackgres.operator.conciliation.ReconciliationOperations;
import io.stackgres.operator.configuration.OperatorPropertyContext;

@ApplicationScoped
public class ConfigDeployedResourceScanner
    extends DeployedResourcesScanner<StackGresConfig>
    implements ReconciliationOperations {

  private final KubernetesClient client;
  private final LabelFactoryForConfig labelFactory;
  private final boolean prometheusAutodiscovery;

  @Inject
  public ConfigDeployedResourceScanner(
      KubernetesClient client,
      LabelFactoryForConfig labelFactory,
      OperatorPropertyContext operatorContext) {
    this.client = client;
    this.labelFactory = labelFactory;
    this.prometheusAutodiscovery = operatorContext.getBoolean(
        OperatorProperty.PROMETHEUS_AUTODISCOVERY);
  }

  @Override
  protected Map<String, String> getGenericLabels(StackGresConfig config) {
    return labelFactory.genericLabels(config);
  }

  @Override
  protected Map<String, String> getCrossNamespaceLabels(StackGresConfig config) {
    return labelFactory.configCrossNamespaceLabels(config);
  }

  @Override
  protected KubernetesClient getClient() {
    return client;
  }

  @Override
  protected Map<Class<? extends HasMetadata>,
      Function<KubernetesClient, MixedOperation<? extends HasMetadata,
          ? extends KubernetesResourceList<? extends HasMetadata>,
              ? extends Resource<? extends HasMetadata>>>> getInNamepspaceResourceOperations() {
    return IN_NAMESPACE_RESOURCE_OPERATIONS;
  }

  @Override
  protected Map<Class<? extends HasMetadata>,
      Function<KubernetesClient, MixedOperation<? extends HasMetadata,
          ? extends KubernetesResourceList<? extends HasMetadata>,
              ? extends Resource<? extends HasMetadata>>>> getInAnyNamespaceResourceOperations(
                  StackGresConfig cluster) {
    if (prometheusAutodiscovery) {
      return PROMETHEUS_RESOURCE_OPERATIONS;
    }
    return super.getInAnyNamespaceResourceOperations(cluster);
  }

}

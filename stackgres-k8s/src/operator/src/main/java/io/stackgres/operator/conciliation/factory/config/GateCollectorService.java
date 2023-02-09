/*
 * Copyright (C) 2019 OnGres, Inc.
 * SPDX-License-Identifier: AGPL-3.0-or-later
 */

package io.stackgres.operator.conciliation.factory.config;

import java.util.stream.Stream;

import javax.inject.Inject;
import javax.inject.Singleton;

import com.fasterxml.jackson.databind.ObjectMapper;
import io.fabric8.kubernetes.api.model.HasMetadata;
import io.fabric8.kubernetes.api.model.ServiceBuilder;
import io.stackgres.common.crd.sgconfig.StackGresConfig;
import io.stackgres.common.labels.LabelFactoryForConfig;
import io.stackgres.operator.conciliation.OperatorVersionBinder;
import io.stackgres.operator.conciliation.ResourceGenerator;
import io.stackgres.operator.conciliation.config.StackGresConfigContext;
import org.jetbrains.annotations.NotNull;

@Singleton
@OperatorVersionBinder
public class GateCollectorService
    implements ResourceGenerator<StackGresConfigContext> {

  private final LabelFactoryForConfig labelFactory;
  private final ObjectMapper objectMapper;

  public static String name(StackGresConfigContext context) {
    return context.getSource().getMetadata().getName() + "-gate-collector";
  }

  @Inject
  public GateCollectorService(
      LabelFactoryForConfig labelFactory,
      ObjectMapper objectMapper) {
    this.labelFactory = labelFactory;
    this.objectMapper = objectMapper;
  }

  /**
   * Create the ConfigMap with the configuration for the gate collector.
   */
  @Override
  public @NotNull Stream<HasMetadata> generateResource(StackGresConfigContext context) {
    StackGresConfig config = context.getSource();
    return Stream.of(new ServiceBuilder()
        .withNewMetadata()
        .withNamespace(config.getMetadata().getNamespace())
        .withName(name(context))
        .addToLabels(labelFactory.genericLabels(config))
        .endMetadata()
        .withSpec(config.getSpec().getCollectorServiceSpec(objectMapper))
        .build());
  }

}

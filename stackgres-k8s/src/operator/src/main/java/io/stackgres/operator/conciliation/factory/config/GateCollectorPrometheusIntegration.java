/*
 * Copyright (C) 2019 OnGres, Inc.
 * SPDX-License-Identifier: AGPL-3.0-or-later
 */

package io.stackgres.operator.conciliation.factory.config;

import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.function.Function;
import java.util.stream.Stream;

import javax.inject.Inject;
import javax.inject.Singleton;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.common.collect.ImmutableMap;
import io.fabric8.kubernetes.api.model.HasMetadata;
import io.fabric8.kubernetes.api.model.LabelSelector;
import io.fabric8.kubernetes.api.model.ObjectMetaBuilder;
import io.stackgres.common.crd.sgconfig.StackGresConfig;
import io.stackgres.common.labels.LabelFactoryForConfig;
import io.stackgres.common.prometheus.Endpoint;
import io.stackgres.common.prometheus.NamespaceSelector;
import io.stackgres.common.prometheus.PodMonitor;
import io.stackgres.common.prometheus.PodMonitorSpec;
import io.stackgres.common.prometheus.PrometheusInstallation;
import io.stackgres.operator.common.PrometheusInstallations;
import io.stackgres.operator.conciliation.OperatorVersionBinder;
import io.stackgres.operator.conciliation.ResourceGenerator;
import io.stackgres.operator.conciliation.config.StackGresConfigContext;
import io.stackgres.operatorframework.resource.ResourceUtil;
import org.jetbrains.annotations.NotNull;

@Singleton
@OperatorVersionBinder
public class GateCollectorPrometheusIntegration
    implements ResourceGenerator<StackGresConfigContext> {

  static String podMonitorName(StackGresConfigContext clusterContext) {
    return ResourceUtil.resourceName("stackgres-operator");
  }

  private final LabelFactoryForConfig labelFactory;

  private final ObjectMapper objectMapper;

  @Inject
  public GateCollectorPrometheusIntegration(
      LabelFactoryForConfig labelFactory,
      ObjectMapper objectMapper) {
    this.labelFactory = labelFactory;
    this.objectMapper = objectMapper;
  }

  @Override
  public Stream<HasMetadata> generateResource(StackGresConfigContext context) {
    Optional<Stream<HasMetadata>> podMonitors = context.getPrometheus()
        .filter(c -> Optional.ofNullable(c.getCreatePodMonitor()).orElse(false))
        .map(c -> getPodMonitors(context, c));

    return podMonitors.stream().flatMap(Function.identity());
  }

  @NotNull
  private Stream<HasMetadata> getPodMonitors(
      StackGresConfigContext context,
      PrometheusInstallations prometheusInstallations) {
    return prometheusInstallations.getPrometheusInstallations().stream()
        .map(prometheusInstallation -> getPodMonitor(context, prometheusInstallation));
  }

  private HasMetadata getPodMonitor(StackGresConfigContext context,
      PrometheusInstallation prometheusInstallation) {
    final StackGresConfig config = context.getSource();
    final String clusterNamespace = config.getMetadata().getNamespace();
    final Map<String, String> crossNamespaceLabels = labelFactory
        .configCrossNamespaceLabels(config);
    final Map<String, String> clusterSelectorLabels = labelFactory
        .gateCollectorLabels(config);
    PodMonitor podMonitor = new PodMonitor();
    podMonitor.setMetadata(new ObjectMetaBuilder()
        .withNamespace(prometheusInstallation.getNamespace())
        .withName(podMonitorName(context))
        .withLabels(ImmutableMap.<String, String>builder()
            .putAll(prometheusInstallation.getMatchLabels())
            .putAll(crossNamespaceLabels)
            .build())
        .build());

    PodMonitorSpec spec = new PodMonitorSpec();
    podMonitor.setSpec(spec);
    spec.setJobName(podMonitorName(context));
    LabelSelector selector = new LabelSelector();
    spec.setSelector(selector);
    NamespaceSelector namespaceSelector = new NamespaceSelector();
    namespaceSelector.setMatchNames(List.of(clusterNamespace));
    spec.setNamespaceSelector(namespaceSelector);

    selector.setMatchLabels(clusterSelectorLabels);
    var ports = context.getSource().getSpec().getCollectorContainerPorts(objectMapper);
    spec.setPodMetricsEndpoints(ports.stream()
        .map(port -> {
          Endpoint endpoint = new Endpoint();
          endpoint.setHonorLabels(true);
          endpoint.setHonorTimestamps(true);
          endpoint.setPort(port.getName());
          endpoint.setPath("/metrics");
          return endpoint;
        })
        .toList());
    return podMonitor;
  }

}

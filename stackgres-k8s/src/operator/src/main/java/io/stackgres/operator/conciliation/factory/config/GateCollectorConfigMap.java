/*
 * Copyright (C) 2019 OnGres, Inc.
 * SPDX-License-Identifier: AGPL-3.0-or-later
 */

package io.stackgres.operator.conciliation.factory.config;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.function.Predicate;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import javax.inject.Inject;
import javax.inject.Singleton;

import com.fasterxml.jackson.annotation.JsonInclude.Include;
import com.fasterxml.jackson.dataformat.yaml.YAMLGenerator.Feature;
import com.fasterxml.jackson.dataformat.yaml.YAMLMapper;
import edu.umd.cs.findbugs.annotations.SuppressFBWarnings;
import io.fabric8.kubernetes.api.model.ConfigMapBuilder;
import io.fabric8.kubernetes.api.model.HasMetadata;
import io.stackgres.common.GateCollectorPath;
import io.stackgres.common.StackGresUtil;
import io.stackgres.common.crd.sgconfig.StackGresConfigSpec;
import io.stackgres.common.labels.LabelFactoryForConfig;
import io.stackgres.operator.conciliation.OperatorVersionBinder;
import io.stackgres.operator.conciliation.ResourceGenerator;
import io.stackgres.operator.conciliation.config.StackGresConfigContext;
import org.jetbrains.annotations.NotNull;
import org.jooq.lambda.Unchecked;
import org.jooq.lambda.tuple.Tuple2;

@Singleton
@OperatorVersionBinder
public class GateCollectorConfigMap
    implements ResourceGenerator<StackGresConfigContext> {

  private final LabelFactoryForConfig labelFactory;
  private final YAMLMapper yamlMapper;

  public static String name(StackGresConfigContext context) {
    return context.getSource().getMetadata().getName() + "-gate-collector-config";
  }

  @SuppressFBWarnings(value = "BC_UNCONFIRMED_CAST_OF_RETURN_VALUE",
      justification = "False Positive")
  @Inject
  public GateCollectorConfigMap(LabelFactoryForConfig labelFactory) {
    this.labelFactory = labelFactory;
    this.yamlMapper = YAMLMapper.builder()
        .serializationInclusion(Include.NON_NULL)
        .configure(Feature.WRITE_DOC_START_MARKER, false)
        .build();
  }

  /**
   * Create the ConfigMap with the configuration for the gate collector.
   */
  @Override
  public @NotNull Stream<HasMetadata> generateResource(StackGresConfigContext context) {
    final Map<String, String> data = new HashMap<>();

    String templateConfig =
        """
        extensions:
          health_check:
            endpoint: 0.0.0.0:13133
          ${extensions}
        receivers:
          prometheus:
            config:
              scrape_configs:
                ${clusterConfigs}
        processors:
          ${processors}
        exporters:
          ${exporters}
        service:
          telemetry:
            ${serviceTelemetry}
          extensions:
            - health_check
            ${serviceExtensions}
          pipelines:
            ${servicePipelines}
        """;
    String templatePodsConfig =
        """
        - job_name: "${podName}-postgres"
          scrape_interval: 1m
          static_configs:
            - targets:
              - "${podIp}:9187"
        - job_name: "${podName}-envoy"
          scrape_interval: 1m
          metrics_path: /stats/prometheus
          static_configs:
            - targets:
              - "${podIp}:8001"
        """;
    StackGresConfigSpec spec = context.getSource().getSpec();
    String clusterConfig = context.getClusterPods()
        .stream()
        .map(Tuple2::v2)
        .flatMap(List::stream)
        .filter(pod -> pod.getStatus() != null)
        .filter(pod -> pod.getStatus().getPodIP() != null)
        .map(pod -> Map.of(
            "podName", pod.getMetadata().getName(),
            "podIp", pod.getStatus().getPodIP()))
        .map(map -> map
            .entrySet()
            .stream()
            .reduce(
                templatePodsConfig,
               (c, entry) -> c.replace("${" + entry.getKey() + "}", entry.getValue()),
               (u, v) -> v))
        .collect(Collectors.joining("\n"));
    if (clusterConfig.isEmpty()) {
      clusterConfig =
          """
          - job_name: "dummy"
            scrape_interval: 1y
          """;
    }
    String config = Map.of(
        "extensions",
        asYamlWithNindent(spec.getCollectorGateExtensions(), 2),
        "clusterConfigs",
        nindent(clusterConfig, 8),
        "processors",
        asYamlWithNindent(spec.getCollectorGateProcessors(), 2),
        "exporters",
        asYamlWithNindent(spec.getCollectorGateExporters(), 2),
        "serviceTelemetry",
        asYamlWithNindent(spec.getCollectorGateServiceTelemetry(), 4),
        "serviceExtensions",
        asYamlWithNindent(spec.getCollectorGateServiceExtensions(), 4),
        "servicePipelines",
        asYamlWithNindent(spec.getCollectorGateServicePipelines(), 4))
        .entrySet()
        .stream()
        .reduce(
           templateConfig,
           (c, entry) -> c.replace("${" + entry.getKey() + "}", entry.getValue()),
           (u, v) -> v);
    data.put(
        GateCollectorPath.ETC_OTEL_CONTRIB_CONFIG_PATH.filename(),
        config);
    return Stream.of(new ConfigMapBuilder()
        .withNewMetadata()
        .withNamespace(context.getSource().getMetadata().getNamespace())
        .withName(name(context))
        .withLabels(labelFactory.genericLabels(context.getSource()))
        .endMetadata()
        .withData(StackGresUtil.addMd5Sum(data))
        .build());
  }

  private String asYamlWithNindent(Optional<? extends Object> section, int offset) {
    return section
        .map(Unchecked.function(yamlMapper::writeValueAsString))
        .map(s -> s.replaceFirst("\\n+$", ""))
        .filter(Predicate.not("{}"::equals))
        .filter(Predicate.not("[]"::equals))
        .map(s -> nindent(s, offset))
        .orElse("");
  }

  private String nindent(String section, int offset) {
    return Optional.of(section)
        .map(s -> s.indent(offset))
        .map(s -> s.substring(Math.min(offset, s.length())))
        .orElse("");
  }

}

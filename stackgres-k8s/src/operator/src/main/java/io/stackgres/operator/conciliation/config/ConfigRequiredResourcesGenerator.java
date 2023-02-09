/*
 * Copyright (C) 2019 OnGres, Inc.
 * SPDX-License-Identifier: AGPL-3.0-or-later
 */

package io.stackgres.operator.conciliation.config;

import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.function.Supplier;

import javax.enterprise.context.ApplicationScoped;
import javax.inject.Inject;

import com.fasterxml.jackson.databind.ObjectMapper;
import io.fabric8.kubernetes.api.model.HasMetadata;
import io.fabric8.kubernetes.api.model.LabelSelector;
import io.fabric8.kubernetes.api.model.Pod;
import io.fabric8.kubernetes.client.VersionInfo;
import io.stackgres.common.crd.sgcluster.StackGresCluster;
import io.stackgres.common.crd.sgcluster.StackGresClusterSpec;
import io.stackgres.common.crd.sgconfig.StackGresConfig;
import io.stackgres.common.labels.LabelFactoryForCluster;
import io.stackgres.common.prometheus.Prometheus;
import io.stackgres.common.prometheus.PrometheusInstallation;
import io.stackgres.common.prometheus.PrometheusSpec;
import io.stackgres.common.resource.CustomResourceScanner;
import io.stackgres.common.resource.ResourceScanner;
import io.stackgres.operator.common.PrometheusInstallations;
import io.stackgres.operator.conciliation.RequiredResourceDecorator;
import io.stackgres.operator.conciliation.RequiredResourceGenerator;
import org.jooq.lambda.tuple.Tuple;
import org.jooq.lambda.tuple.Tuple2;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@ApplicationScoped
public class ConfigRequiredResourcesGenerator
    implements RequiredResourceGenerator<StackGresConfig> {

  protected static final Logger LOGGER = LoggerFactory
      .getLogger(ConfigRequiredResourcesGenerator.class);

  private final Supplier<VersionInfo> kubernetesVersionSupplier;

  private final CustomResourceScanner<StackGresCluster> clusterScanner;

  private final ResourceScanner<Pod> podScanner;

  private final LabelFactoryForCluster<StackGresCluster> clusterLabelFactory;

  private final CustomResourceScanner<Prometheus> prometheusScanner;

  private final RequiredResourceDecorator<StackGresConfigContext> decorator;

  private final ObjectMapper objectMapper;

  @Inject
  public ConfigRequiredResourcesGenerator(
      Supplier<VersionInfo> kubernetesVersionSupplier,
      CustomResourceScanner<StackGresCluster> clusterScanner,
      ResourceScanner<Pod> podScanner,
      LabelFactoryForCluster<StackGresCluster> clusterLabelFactory,
      CustomResourceScanner<Prometheus> prometheusScanner,
      RequiredResourceDecorator<StackGresConfigContext> decorator,
      ObjectMapper objectMapper) {
    this.kubernetesVersionSupplier = kubernetesVersionSupplier;
    this.clusterScanner = clusterScanner;
    this.podScanner = podScanner;
    this.clusterLabelFactory = clusterLabelFactory;
    this.prometheusScanner = prometheusScanner;
    this.decorator = decorator;
    this.objectMapper = objectMapper;
  }

  @Override
  public List<HasMetadata> getRequiredResources(StackGresConfig config) {
    VersionInfo kubernetesVersion = kubernetesVersionSupplier.get();

    List<StackGresCluster> clusters = clusterScanner.getResources();

    StackGresConfigContext context = ImmutableStackGresConfigContext.builder()
        .kubernetesVersion(kubernetesVersion)
        .source(config)
        .clusterPods(getClusterPods(clusters))
        .prometheus(getPrometheusInstallations(config, clusters))
        .build();

    return decorator.decorateResources(context);
  }

  private List<Tuple2<StackGresCluster, List<Pod>>> getClusterPods(
      List<StackGresCluster> clusters) {
    return clusters
        .stream()
        .map(cluster -> Tuple.tuple(cluster, podScanner.findByLabelsAndNamespace(
            cluster.getMetadata().getNamespace(),
            clusterLabelFactory.clusterLabels(cluster))))
        .toList();
  }

  public PrometheusInstallations getPrometheusInstallations(
      StackGresConfig config, List<StackGresCluster> clusters) {
    boolean isPrometheusAutobindEnabled = clusters
        .stream()
        .map(StackGresCluster::getSpec)
        .map(StackGresClusterSpec::getPrometheusAutobind)
        .reduce(false, (result, autobind) -> result || autobind);

    if (isPrometheusAutobindEnabled) {
      final List<PrometheusInstallation> prometheusInstallations = prometheusScanner
          .findResources()
          .stream()
          .flatMap(List::stream)
          .map(this::toPrometheusInstallation)
          .toList();
      if (config.getSpec().getCollectorPrometheusOperatorAutodiscoveryEnabled()) {
        LOGGER.trace("Prometheus auto discovery enabled, using all prometheus installations");
        return new PrometheusInstallations(prometheusInstallations);
      } else {
        return new PrometheusInstallations(config.getSpec()
            .getCollectorPrometheusOperatorMonitors(objectMapper)
            .stream()
            .map(prometheusInstallation -> Tuple.tuple(
                prometheusInstallation,
                prometheusInstallations.stream()
                .filter(prometheusInstallation::match)
                .findAny()))
            .filter(t -> t.v2().isPresent())
            .map(t -> {
              t.v1.setMatchLabels(t.v2.get().getMatchLabels());
              return t.v1;
            })
            .toList());
      }
    }
    return new PrometheusInstallations(List.of());
  }

  private PrometheusInstallation toPrometheusInstallation(Prometheus pc) {
    Map<String, String> matchLabels = Optional.ofNullable(pc.getSpec())
        .map(PrometheusSpec::getPodMonitorSelector)
        .map(LabelSelector::getMatchLabels)
        .map(Map::copyOf)
        .orElse(Map.of());
    PrometheusInstallation prometheusInstallation = new PrometheusInstallation();
    prometheusInstallation.setName(pc.getMetadata().getName());
    prometheusInstallation.setNamespace(pc.getMetadata().getNamespace());
    prometheusInstallation.setMatchLabels(matchLabels);
    return prometheusInstallation;
  }

}

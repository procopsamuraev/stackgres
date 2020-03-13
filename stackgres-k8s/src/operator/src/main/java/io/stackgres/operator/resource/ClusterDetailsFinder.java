/*
 * Copyright (C) 2019 OnGres, Inc.
 * SPDX-License-Identifier: AGPL-3.0-or-later
 */

package io.stackgres.operator.resource;

import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

import javax.enterprise.context.ApplicationScoped;
import javax.inject.Inject;

import io.fabric8.kubernetes.api.model.ContainerStatus;
import io.fabric8.kubernetes.client.KubernetesClient;
import io.stackgres.operator.app.KubernetesClientFactory;
import io.stackgres.operator.common.ConfigContext;
import io.stackgres.operator.common.ConfigProperty;
import io.stackgres.operator.common.StackGresUtil;
import io.stackgres.operator.customresource.sgcluster.StackGresCluster;
import io.stackgres.operator.rest.dto.cluster.ClusterPodConfigDto;
import io.stackgres.operator.rest.dto.cluster.ClusterPodStatus;

import org.jooq.lambda.tuple.Tuple;

@ApplicationScoped
public class ClusterDetailsFinder implements CustomResourceFinder<ClusterPodConfigDto> {

  private final CustomResourceFinder<StackGresCluster> clusterFinder;
  private final KubernetesClientFactory kubClientFactory;

  private ConfigContext context;

  @Inject
  public ClusterDetailsFinder(CustomResourceFinder<StackGresCluster> clusterFinder,
      KubernetesClientFactory kubClientFactory,
      ConfigContext context) {
    this.clusterFinder = clusterFinder;
    this.kubClientFactory = kubClientFactory;
    this.context = context;
  }

  @Override
  public Optional<ClusterPodConfigDto> findByNameAndNamespace(String name, String namespace) {

    return clusterFinder.findByNameAndNamespace(name, namespace).map(cluster -> {

      try (KubernetesClient client = kubClientFactory.create()) {
        ClusterPodConfigDto details = new ClusterPodConfigDto();

        List<ClusterPodStatus> clusterPods = getClusterPods(cluster, client);
        details.setPods(clusterPods);

        details.setPodsReady(String.valueOf(clusterPods
            .stream()
            .filter(pod -> pod.getContainers().equals(pod.getContainersReady()))
            .count()));

        boolean isGrafanaEmbedded = isGrafanaEmbeddedEnabled();
        details.setGrafanaEmbedded(isGrafanaEmbedded);
        return details;
      }

    });

  }

  private boolean isGrafanaEmbeddedEnabled() {
    return context.getProperty(ConfigProperty.GRAFANA_EMBEDDED)
        .map(Boolean::parseBoolean)
        .orElse(false);
  }

  private List<ClusterPodStatus> getClusterPods(StackGresCluster cluster, KubernetesClient client) {
    return client.pods()
        .inNamespace(cluster.getMetadata().getNamespace())
        .withLabels(StackGresUtil.patroniClusterLabels(cluster))
        .list()
        .getItems()
        .stream()
        .map(pod -> Tuple.tuple(pod, new ClusterPodStatus()))
        .peek(t -> t.v2.setNamespace(t.v1.getMetadata().getNamespace()))
        .peek(t -> t.v2.setName(t.v1.getMetadata().getName()))
        .peek(t -> t.v2.setRole(
                convertRole(t.v1.getMetadata().getLabels().get("role"))))
        .peek(t -> t.v2.setIp(t.v1.getStatus().getPodIP()))
        .peek(t -> t.v2.setStatus(
                convertPhase(t.v1.getStatus().getPhase())))
        .peek(t -> t.v2.setContainers(String.valueOf(t.v1.getSpec()
            .getContainers().size())))
        .peek(t -> t.v2.setContainersReady(String.valueOf(t.v1.getStatus()
            .getContainerStatuses()
            .stream()
            .filter(ContainerStatus::getReady)
            .count())))
        .map(t -> t.v2)
        .collect(Collectors.toList());
  }

  private String convertRole(String role) {
    if (StackGresUtil.PRIMARY_ROLE.equals(role)) {
      return "primary";
    }

    return role;
  }

  private String convertPhase(String phase) {
    if ("Running".equals(phase)) {
      return "Active";
    }

    return phase;
  }

}

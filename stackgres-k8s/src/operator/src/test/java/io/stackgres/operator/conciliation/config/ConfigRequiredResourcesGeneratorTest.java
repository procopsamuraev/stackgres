/*
 * Copyright (C) 2019 OnGres, Inc.
 * SPDX-License-Identifier: AGPL-3.0-or-later
 */

package io.stackgres.operator.conciliation.config;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.util.List;
import java.util.Optional;

import javax.inject.Inject;

import io.fabric8.kubernetes.api.model.HasMetadata;
import io.quarkus.test.junit.QuarkusTest;
import io.quarkus.test.junit.mockito.InjectMock;
import io.stackgres.common.OperatorProperty;
import io.stackgres.common.crd.sgcluster.StackGresCluster;
import io.stackgres.common.crd.sgconfig.StackGresConfig;
import io.stackgres.common.fixture.Fixtures;
import io.stackgres.common.prometheus.PodMonitor;
import io.stackgres.common.prometheus.Prometheus;
import io.stackgres.common.resource.ClusterScanner;
import io.stackgres.operator.resource.PrometheusScanner;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

@QuarkusTest
class ConfigRequiredResourcesGeneratorTest {

  @InjectMock
  PrometheusScanner prometheusScanner;

  @InjectMock
  ClusterScanner clusterScanner;

  @Inject
  ConfigRequiredResourcesGenerator generator;

  private StackGresConfig config;

  private StackGresCluster cluster;

  private List<Prometheus> prometheusList;

  @BeforeEach
  void setUp() {
    config = Fixtures.config().loadDefault().get();
    cluster = Fixtures.cluster().loadDefault().get();
    cluster.getSpec().setPrometheusAutobind(true);
    prometheusList = Fixtures.prometheusList().loadDefault().get().getItems();
  }

  @Test
  void givenValidCluster_getRequiredResourcesShouldNotFail() {
    generator.getRequiredResources(config);
  }

  @Test
  void givenADefaultPrometheusInstallation_shouldGeneratePodMonitors() {
    try {
      System.setProperty(OperatorProperty.PROMETHEUS_AUTODISCOVERY.getPropertyName(), "true");

      when(prometheusScanner.findResources()).thenReturn(Optional.of(prometheusList));

      when(clusterScanner.getResources()).thenReturn(List.of(cluster));

      List<HasMetadata> generatedResources = generator.getRequiredResources(config);

      var podMonitors = generatedResources.stream()
          .filter(r -> r.getKind().equals(PodMonitor.KIND))
          .count();

      assertEquals(1, podMonitors);
      verify(prometheusScanner).findResources();
    } finally {
      System.clearProperty(OperatorProperty.PROMETHEUS_AUTODISCOVERY.getPropertyName());
    }
  }

  @Test
  void givenAPrometheusInstallationWithNoPodMonitorSelector_shouldGeneratePodMonitors() {
    System.setProperty(OperatorProperty.PROMETHEUS_AUTODISCOVERY.getPropertyName(), "true");

    List<Prometheus> listPrometheus = Fixtures.prometheusList().loadDefault().get()
            .getItems()
            .stream()
            .peek(pc -> pc.getSpec().setPodMonitorSelector(null))
            .toList();

    when(prometheusScanner.findResources()).thenReturn(Optional.of(listPrometheus));

    when(clusterScanner.getResources()).thenReturn(List.of(cluster));

    List<HasMetadata> generatedResources = generator.getRequiredResources(config);

    var podMonitors = generatedResources.stream()
        .filter(r -> r.getKind().equals(HasMetadata.getKind(PodMonitor.class)))
        .count();

    assertEquals(1, podMonitors);
    System.clearProperty(OperatorProperty.PROMETHEUS_AUTODISCOVERY.getPropertyName());
  }

  void assertException(String message) {
    var ex =
        assertThrows(IllegalArgumentException.class, () -> generator
            .getRequiredResources(config));
    assertEquals(message, ex.getMessage());
  }

}

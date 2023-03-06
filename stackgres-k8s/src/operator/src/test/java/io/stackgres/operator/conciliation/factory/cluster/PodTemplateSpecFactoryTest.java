/*
 * Copyright (C) 2019 OnGres, Inc.
 * SPDX-License-Identifier: AGPL-3.0-or-later
 */

package io.stackgres.operator.conciliation.factory.cluster;

import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.when;

import java.util.List;
import java.util.Map;

import io.fabric8.kubernetes.api.model.Container;
import io.fabric8.kubernetes.api.model.PodSecurityContext;
import io.fabric8.kubernetes.api.model.VolumeBuilder;
import io.fabric8.kubernetes.api.model.VolumeMountBuilder;
import io.stackgres.common.ClusterLabelMapper;
import io.stackgres.common.LabelFactoryForCluster;
import io.stackgres.common.StackGresComponent;
import io.stackgres.common.StackGresVolume;
import io.stackgres.common.crd.sgcluster.StackGresCluster;
import io.stackgres.common.fixture.Fixtures;
import io.stackgres.operator.conciliation.ContainerFactoryDiscoverer;
import io.stackgres.operator.conciliation.InitContainerFactoryDiscover;
import io.stackgres.operator.conciliation.cluster.StackGresClusterContext;
import io.stackgres.operator.conciliation.factory.ContainerFactory;
import io.stackgres.operator.conciliation.factory.ResourceFactory;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class PodTemplateSpecFactoryTest {

  private static final String POSTGRES_VERSION =
      StackGresComponent.POSTGRESQL.getLatest().streamOrderedVersions().findFirst().get();

  @Mock
  private ResourceFactory<StackGresClusterContext, PodSecurityContext> podSecurityContext;

  @Mock
  private LabelFactoryForCluster<StackGresCluster> labelFactory;

  @Mock
  private ClusterLabelMapper labelMapper;

  @Mock
  private ContainerFactoryDiscoverer<ClusterContainerContext> containerFactoryDiscoverer;

  @Mock
  private InitContainerFactoryDiscover<ClusterContainerContext>
      initContainerFactoryDiscoverer;

  @Mock
  private ContainerFactory<ClusterContainerContext> patroniContainerFactory;

  @Mock
  private Container patroniContainer;

  @InjectMocks
  private ClusterPodTemplateSpecFactory podTemplateSpecFactory;

  @Mock
  private ClusterContainerContext clusterContainerContext;

  @Mock
  private StackGresClusterContext clusterContext;

  private StackGresCluster cluster;

  @BeforeEach
  public void setupClass() {
    this.podTemplateSpecFactory = new ClusterPodTemplateSpecFactory(
        podSecurityContext, labelFactory, containerFactoryDiscoverer,
        initContainerFactoryDiscoverer);
    cluster = Fixtures.cluster().loadDefault().get();
    cluster.getSpec().getPostgres().setVersion(POSTGRES_VERSION);
    when(clusterContainerContext.getClusterContext()).thenReturn(clusterContext);
    when(clusterContext.getSource()).thenReturn(cluster);
    when(clusterContext.getCluster()).thenReturn(cluster);
    when(labelFactory.labelMapper()).thenReturn(labelMapper);
  }

  @Test
  void clusterWithHugePages_shouldAddHugePagesVolumes() {
    when(containerFactoryDiscoverer.discoverContainers(clusterContainerContext))
        .thenReturn(List.of(patroniContainerFactory));
    when(patroniContainerFactory.getContainer(clusterContainerContext))
        .thenReturn(patroniContainer);
    when(patroniContainer.getVolumeMounts()).thenReturn(List.of(
        new VolumeMountBuilder()
        .withName(StackGresVolume.HUGEPAGES_2M.getName())
        .build(),
        new VolumeMountBuilder()
        .withName(StackGresVolume.HUGEPAGES_1G.getName())
        .build()));
    when(clusterContainerContext.availableVolumes()).thenReturn(Map.of(
        StackGresVolume.HUGEPAGES_2M.getName(),
        new VolumeBuilder()
        .withName(StackGresVolume.HUGEPAGES_2M.getName())
        .withNewEmptyDir()
        .withMedium("HugePages-2Mi")
        .endEmptyDir()
        .build(),
        StackGresVolume.HUGEPAGES_1G.getName(),
        new VolumeBuilder()
        .withName(StackGresVolume.HUGEPAGES_1G.getName())
        .withNewEmptyDir()
        .withMedium("HugePages-1Gi")
        .endEmptyDir()
        .build()));
    var podTemplateSpec = podTemplateSpecFactory.getPodTemplateSpec(clusterContainerContext);
    assertTrue(podTemplateSpec.getSpec().getSpec().getVolumes().stream()
        .anyMatch(volume -> volume.getName()
            .equals(StackGresVolume.HUGEPAGES_2M.getName())));
    assertTrue(podTemplateSpec.getSpec().getSpec().getVolumes().stream()
        .filter(volume -> volume.getName()
            .equals(StackGresVolume.HUGEPAGES_2M.getName()))
        .anyMatch(volume -> volume.getEmptyDir() != null));
    assertTrue(podTemplateSpec.getSpec().getSpec().getVolumes().stream()
        .filter(volume -> volume.getName()
            .equals(StackGresVolume.HUGEPAGES_2M.getName()))
        .anyMatch(volume -> volume.getEmptyDir().getMedium().equals("HugePages-2Mi")));
    assertTrue(podTemplateSpec.getSpec().getSpec().getVolumes().stream()
        .anyMatch(volume -> volume.getName()
            .equals(StackGresVolume.HUGEPAGES_1G.getName())));
    assertTrue(podTemplateSpec.getSpec().getSpec().getVolumes().stream()
        .filter(volume -> volume.getName()
            .equals(StackGresVolume.HUGEPAGES_1G.getName()))
        .anyMatch(volume -> volume.getEmptyDir() != null));
    assertTrue(podTemplateSpec.getSpec().getSpec().getVolumes().stream()
        .filter(volume -> volume.getName()
            .equals(StackGresVolume.HUGEPAGES_1G.getName()))
        .anyMatch(volume -> volume.getEmptyDir().getMedium().equals("HugePages-1Gi")));
  }

}

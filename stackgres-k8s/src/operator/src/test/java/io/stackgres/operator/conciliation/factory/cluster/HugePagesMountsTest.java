/*
 * Copyright (C) 2019 OnGres, Inc.
 * SPDX-License-Identifier: AGPL-3.0-or-later
 */

package io.stackgres.operator.conciliation.factory.cluster;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.when;

import io.stackgres.common.ClusterStatefulSetPath;
import io.stackgres.common.StackGresVolume;
import io.stackgres.common.crd.sgcluster.StackGresCluster;
import io.stackgres.common.crd.sgprofile.StackGresProfile;
import io.stackgres.common.crd.sgprofile.StackGresProfileHugePages;
import io.stackgres.common.fixture.Fixtures;
import io.stackgres.operator.conciliation.cluster.StackGresClusterContext;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class HugePagesMountsTest {

  private HugePagesMounts hugePagesMounts;

  @Mock
  private ClusterContainerContext clusterContainerContext;

  @Mock
  private StackGresClusterContext clusterContext;

  private StackGresCluster cluster;

  private StackGresProfile profile;

  @BeforeEach
  void setUp() {
    hugePagesMounts = new HugePagesMounts();
    profile = Fixtures.instanceProfile().loadSizeS().get();
    cluster = Fixtures.cluster().loadDefault().get();
    when(clusterContainerContext.getClusterContext()).thenReturn(clusterContext);
  }

  @Test
  void givenAClusterWithAProfileWithHugePages_itShouldCreateTheMountsWithHugePages() {
    profile.getSpec().setHugePages(new StackGresProfileHugePages());
    profile.getSpec().getHugePages().setHugepages2Mi("2Mi");
    profile.getSpec().getHugePages().setHugepages1Gi("1Gi");
    when(clusterContext.getSource()).thenReturn(cluster);
    when(clusterContext.getProfile()).thenReturn(profile);

    var volumeMounts = hugePagesMounts.getVolumeMounts(clusterContainerContext);

    assertEquals(2, volumeMounts.size());

    assertTrue(volumeMounts.stream()
        .anyMatch(volumeMount -> volumeMount.getName()
            .equals(StackGresVolume.HUGEPAGES_2M.getName())));
    assertTrue(volumeMounts.stream()
        .filter(volumeMount -> volumeMount.getName()
            .equals(StackGresVolume.HUGEPAGES_2M.getName()))
        .anyMatch(volumeMount -> volumeMount.getMountPath()
            .equals(ClusterStatefulSetPath.HUGEPAGES_2M_PATH.path())));
    assertTrue(volumeMounts.stream()
        .anyMatch(volumeMount -> volumeMount.getName()
            .equals(StackGresVolume.HUGEPAGES_1G.getName())));
    assertTrue(volumeMounts.stream()
        .filter(volumeMount -> volumeMount.getName()
            .equals(StackGresVolume.HUGEPAGES_1G.getName()))
        .anyMatch(volumeMount -> volumeMount.getMountPath()
            .equals(ClusterStatefulSetPath.HUGEPAGES_1G_PATH.path())));

    var envVars = hugePagesMounts.getDerivedEnvVars(clusterContainerContext);

    assertTrue(envVars.stream()
        .anyMatch(envVar -> envVar
            .equals(ClusterStatefulSetPath.HUGEPAGES_2M_PATH.envVar())));
    assertTrue(envVars.stream()
        .anyMatch(envVar -> envVar
            .equals(ClusterStatefulSetPath.HUGEPAGES_1G_PATH.envVar())));
  }

  @Test
  void givenAClusterWithoutAProfileWithHugePages_itShouldNotCreateTheMountsWithHugePages() {
    when(clusterContext.getSource()).thenReturn(cluster);
    when(clusterContext.getProfile()).thenReturn(profile);

    var volumeMounts = hugePagesMounts.getVolumeMounts(clusterContainerContext);

    assertFalse(volumeMounts.stream()
        .anyMatch(volumeMount -> volumeMount.getName()
            .equals(StackGresVolume.HUGEPAGES_2M.getName())));
    assertFalse(volumeMounts.stream()
        .filter(volumeMount -> volumeMount.getName()
            .equals(StackGresVolume.HUGEPAGES_2M.getName()))
        .anyMatch(volumeMount -> volumeMount.getMountPath()
            .equals(ClusterStatefulSetPath.HUGEPAGES_2M_PATH.path())));
    assertFalse(volumeMounts.stream()
        .anyMatch(volumeMount -> volumeMount.getName()
            .equals(StackGresVolume.HUGEPAGES_1G.getName())));
    assertFalse(volumeMounts.stream()
        .filter(volumeMount -> volumeMount.getName()
            .equals(StackGresVolume.HUGEPAGES_1G.getName()))
        .anyMatch(volumeMount -> volumeMount.getMountPath()
            .equals(ClusterStatefulSetPath.HUGEPAGES_1G_PATH.path())));

    var envVars = hugePagesMounts.getDerivedEnvVars(clusterContainerContext);

    assertTrue(envVars.stream()
        .anyMatch(envVar -> envVar
            .equals(ClusterStatefulSetPath.HUGEPAGES_2M_PATH.envVar())));
    assertTrue(envVars.stream()
        .anyMatch(envVar -> envVar
            .equals(ClusterStatefulSetPath.HUGEPAGES_1G_PATH.envVar())));
  }

}

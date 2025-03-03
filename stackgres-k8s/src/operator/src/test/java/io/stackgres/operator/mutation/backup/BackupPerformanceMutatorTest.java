/*
 * Copyright (C) 2019 OnGres, Inc.
 * SPDX-License-Identifier: AGPL-3.0-or-later
 */

package io.stackgres.operator.mutation.backup;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;

import java.io.IOException;

import com.fasterxml.jackson.databind.json.JsonMapper;
import io.stackgres.common.crd.sgbackup.StackGresBackup;
import io.stackgres.common.crd.sgbackupconfig.StackGresBaseBackupPerformance;
import io.stackgres.operator.common.BackupReview;
import io.stackgres.operator.common.fixture.AdmissionReviewFixtures;
import io.stackgres.testutil.JsonUtil;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

class BackupPerformanceMutatorTest {

  protected static final JsonMapper JSON_MAPPER = new JsonMapper();

  private BackupReview review;
  private BackupPerformanceMutator mutator;

  @BeforeEach
  void setUp() throws NoSuchFieldException, IOException {
    review = AdmissionReviewFixtures.backup().loadCreate().get();

    mutator = new BackupPerformanceMutator();
  }

  @Test
  void clusterWithNonDeprecatedValues_shouldSetNothing() {
    review.getRequest().getObject().getStatus().getBackupConfig().getBaseBackups()
        .setPerformance(new StackGresBaseBackupPerformance());
    review.getRequest().getObject().getStatus().getBackupConfig().getBaseBackups()
        .getPerformance().setMaxDiskBandwidth(1L);
    review.getRequest().getObject().getStatus().getBackupConfig().getBaseBackups()
        .getPerformance().setMaxNetworkBandwidth(2L);

    StackGresBackup actualBackupConfig = mutate(review);

    assertEquals(1L, actualBackupConfig.getStatus().getBackupConfig().getBaseBackups()
        .getPerformance().getMaxDiskBandwidth());
    assertEquals(2L, actualBackupConfig.getStatus().getBackupConfig().getBaseBackups()
        .getPerformance().getMaxNetworkBandwidth());
    assertNull(actualBackupConfig.getStatus().getBackupConfig().getBaseBackups()
        .getPerformance().getMaxDiskBandwitdh());
    assertNull(actualBackupConfig.getStatus().getBackupConfig().getBaseBackups()
        .getPerformance().getMaxNetworkBandwitdh());
  }

  @Test
  void clusterWithNullAndDeprecatedMaxDiskBandwidthValue_shouldMoveValueToTheOtherField() {
    review.getRequest().getObject().getStatus().getBackupConfig().getBaseBackups()
        .setPerformance(new StackGresBaseBackupPerformance());
    review.getRequest().getObject().getStatus().getBackupConfig().getBaseBackups()
        .getPerformance().setMaxDiskBandwitdh(1L);
    review.getRequest().getObject().getStatus().getBackupConfig().getBaseBackups()
        .getPerformance().setMaxNetworkBandwidth(2L);

    StackGresBackup actualBackupConfig = mutate(review);

    assertEquals(1L, actualBackupConfig.getStatus().getBackupConfig().getBaseBackups()
        .getPerformance().getMaxDiskBandwidth());
    assertEquals(2L, actualBackupConfig.getStatus().getBackupConfig().getBaseBackups()
        .getPerformance().getMaxNetworkBandwidth());
    assertNull(actualBackupConfig.getStatus().getBackupConfig().getBaseBackups()
        .getPerformance().getMaxDiskBandwitdh());
    assertNull(actualBackupConfig.getStatus().getBackupConfig().getBaseBackups()
        .getPerformance().getMaxNetworkBandwitdh());
  }

  @Test
  void clusterWithNullAndDeprecatedMaxNetworkBandwidthValue_shouldMoveValueToTheOtherField() {
    review.getRequest().getObject().getStatus().getBackupConfig().getBaseBackups()
        .setPerformance(new StackGresBaseBackupPerformance());
    review.getRequest().getObject().getStatus().getBackupConfig().getBaseBackups()
        .getPerformance().setMaxDiskBandwidth(1L);
    review.getRequest().getObject().getStatus().getBackupConfig().getBaseBackups()
        .getPerformance().setMaxNetworkBandwitdh(2L);

    StackGresBackup actualBackupConfig = mutate(review);

    assertEquals(1L, actualBackupConfig.getStatus().getBackupConfig().getBaseBackups()
        .getPerformance().getMaxDiskBandwidth());
    assertEquals(2L, actualBackupConfig.getStatus().getBackupConfig().getBaseBackups()
        .getPerformance().getMaxNetworkBandwidth());
    assertNull(actualBackupConfig.getStatus().getBackupConfig().getBaseBackups()
        .getPerformance().getMaxDiskBandwitdh());
    assertNull(actualBackupConfig.getStatus().getBackupConfig().getBaseBackups()
        .getPerformance().getMaxNetworkBandwitdh());
  }

  @Test
  void clusterWithDeprecatedMaxDiskBandwidthValue_shouldSetValueToNull() {
    review.getRequest().getObject().getStatus().getBackupConfig().getBaseBackups()
        .setPerformance(new StackGresBaseBackupPerformance());
    review.getRequest().getObject().getStatus().getBackupConfig().getBaseBackups()
        .getPerformance().setMaxDiskBandwidth(1L);
    review.getRequest().getObject().getStatus().getBackupConfig().getBaseBackups()
        .getPerformance().setMaxDiskBandwitdh(3L);
    review.getRequest().getObject().getStatus().getBackupConfig().getBaseBackups()
        .getPerformance().setMaxNetworkBandwidth(2L);

    StackGresBackup actualBackupConfig = mutate(review);

    assertEquals(1L, actualBackupConfig.getStatus().getBackupConfig().getBaseBackups()
        .getPerformance().getMaxDiskBandwidth());
    assertEquals(2L, actualBackupConfig.getStatus().getBackupConfig().getBaseBackups()
        .getPerformance().getMaxNetworkBandwidth());
    assertNull(actualBackupConfig.getStatus().getBackupConfig().getBaseBackups()
        .getPerformance().getMaxDiskBandwitdh());
    assertNull(actualBackupConfig.getStatus().getBackupConfig().getBaseBackups()
        .getPerformance().getMaxNetworkBandwitdh());
  }

  @Test
  void clusterWithDeprecatedMaxNetworkBandwidthValue_shouldSetValueToNull() {
    review.getRequest().getObject().getStatus().getBackupConfig().getBaseBackups()
        .setPerformance(new StackGresBaseBackupPerformance());
    review.getRequest().getObject().getStatus().getBackupConfig().getBaseBackups()
        .getPerformance().setMaxDiskBandwidth(1L);
    review.getRequest().getObject().getStatus().getBackupConfig().getBaseBackups()
        .getPerformance().setMaxNetworkBandwidth(2L);
    review.getRequest().getObject().getStatus().getBackupConfig().getBaseBackups()
        .getPerformance().setMaxNetworkBandwitdh(4L);

    StackGresBackup actualBackupConfig = mutate(review);

    assertEquals(1L, actualBackupConfig.getStatus().getBackupConfig().getBaseBackups()
        .getPerformance().getMaxDiskBandwidth());
    assertEquals(2L, actualBackupConfig.getStatus().getBackupConfig().getBaseBackups()
        .getPerformance().getMaxNetworkBandwidth());
    assertNull(actualBackupConfig.getStatus().getBackupConfig().getBaseBackups()
        .getPerformance().getMaxDiskBandwitdh());
    assertNull(actualBackupConfig.getStatus().getBackupConfig().getBaseBackups()
        .getPerformance().getMaxNetworkBandwitdh());
  }

  private StackGresBackup mutate(BackupReview review) {
    return mutator.mutate(review, JsonUtil.copy(review.getRequest().getObject()));
  }
}

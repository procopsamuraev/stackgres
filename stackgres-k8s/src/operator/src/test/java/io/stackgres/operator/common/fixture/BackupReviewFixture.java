/*
 * Copyright (C) 2019 OnGres, Inc.
 * SPDX-License-Identifier: AGPL-3.0-or-later
 */

package io.stackgres.operator.common.fixture;

import io.stackgres.operator.common.BackupReview;
import io.stackgres.testutil.fixture.Fixture;

public class BackupReviewFixture extends Fixture<BackupReview> {

  public static BackupReviewFixture fixture() {
    return new BackupReviewFixture();
  }

  public BackupReviewFixture loadCreate() {
    fixture = readFromJson(STACKGRES_BACKUP_ADMISSION_REVIEW_CREATE_JSON);
    return this;
  }

  public BackupReviewFixture loadUpdate() {
    fixture = readFromJson(STACKGRES_BACKUP_ADMISSION_REVIEW_UPDATE_JSON);
    return this;
  }

}

/*
 * Copyright (C) 2019 OnGres, Inc.
 * SPDX-License-Identifier: AGPL-3.0-or-later
 */

package io.stackgres.operator.validation.backupconfig;

import java.util.Objects;

import javax.inject.Singleton;

import io.stackgres.common.ErrorType;
import io.stackgres.common.crd.sgcluster.StackGresCluster;
import io.stackgres.operator.common.BackupConfigReview;
import io.stackgres.operator.validation.DependenciesValidator;
import io.stackgres.operator.validation.ValidationType;
import io.stackgres.operatorframework.admissionwebhook.validating.ValidationFailed;

@Singleton
@ValidationType(ErrorType.FORBIDDEN_CR_DELETION)
public class BackupConfigDependenciesValidator extends DependenciesValidator<BackupConfigReview>
    implements BackupConfigValidator {

  @Override
  public void validate(BackupConfigReview review, StackGresCluster i) throws ValidationFailed {
    if (Objects.equals(i.getSpec().getConfiguration().getBackupConfig(),
        review.getRequest().getName())) {
      fail(review, i);
    }
  }

}

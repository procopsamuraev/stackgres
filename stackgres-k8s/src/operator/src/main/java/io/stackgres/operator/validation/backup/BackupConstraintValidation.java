/*
 * Copyright (C) 2019 OnGres, Inc.
 * SPDX-License-Identifier: AGPL-3.0-or-later
 */

package io.stackgres.operator.validation.backup;

import javax.inject.Singleton;

import io.stackgres.operator.common.BackupReview;
import io.stackgres.operator.common.ErrorType;
import io.stackgres.operator.validation.ConstraintValidator;
import io.stackgres.operator.validation.ValidationType;

@Singleton
@ValidationType(ErrorType.CONSTRAINT_VIOLATION)
public class BackupConstraintValidation extends ConstraintValidator<BackupReview>
    implements BackupValidator {

}

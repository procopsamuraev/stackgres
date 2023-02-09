/*
 * Copyright (C) 2019 OnGres, Inc.
 * SPDX-License-Identifier: AGPL-3.0-or-later
 */

package io.stackgres.operator.conciliation.config;

import javax.enterprise.context.ApplicationScoped;

import io.stackgres.common.crd.sgconfig.StackGresConfig;
import io.stackgres.operator.conciliation.ReconciliationScope;
import io.stackgres.operator.conciliation.comparator.AbstractComparator;

@ApplicationScoped
@ReconciliationScope(value = StackGresConfig.class, kind = "HasMetadata")
public class ConfigDefaultComparator extends AbstractComparator {

  private static final IgnorePatch[] IGNORE_PATCH_PATTERNS = {
  };

  @Override
  protected IgnorePatch[] getPatchPattersToIgnore() {
    return IGNORE_PATCH_PATTERNS;
  }
}

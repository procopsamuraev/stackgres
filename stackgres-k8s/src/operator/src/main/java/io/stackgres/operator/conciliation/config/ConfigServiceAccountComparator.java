/*
 * Copyright (C) 2019 OnGres, Inc.
 * SPDX-License-Identifier: AGPL-3.0-or-later
 */

package io.stackgres.operator.conciliation.config;

import javax.enterprise.context.ApplicationScoped;

import io.stackgres.common.crd.sgconfig.StackGresConfig;
import io.stackgres.operator.conciliation.ReconciliationScope;
import io.stackgres.operator.conciliation.comparator.ServiceAccountComparator;

@ReconciliationScope(value = StackGresConfig.class, kind = "ServiceAccount")
@ApplicationScoped
public class ConfigServiceAccountComparator extends ServiceAccountComparator {

}

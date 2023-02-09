/*
 * Copyright (C) 2019 OnGres, Inc.
 * SPDX-License-Identifier: AGPL-3.0-or-later
 */

package io.stackgres.operator.conciliation.config;

import javax.enterprise.context.ApplicationScoped;

import io.stackgres.common.crd.sgconfig.StackGresConfig;
import io.stackgres.operator.conciliation.Conciliator;

@ApplicationScoped
public class ConfigConciliator extends Conciliator<StackGresConfig> {

}

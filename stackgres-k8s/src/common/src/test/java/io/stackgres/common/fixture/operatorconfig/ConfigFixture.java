/*
 * Copyright (C) 2019 OnGres, Inc.
 * SPDX-License-Identifier: AGPL-3.0-or-later
 */

package io.stackgres.common.fixture.operatorconfig;

import io.stackgres.common.crd.sgconfig.StackGresConfig;
import io.stackgres.testutil.fixture.Fixture;

public class ConfigFixture extends Fixture<StackGresConfig> {

  public ConfigFixture loadDefault() {
    fixture = readFromJson(STACKGRES_OPERATOR_CONFIG_DEFAULT_JSON);
    return this;
  }

}

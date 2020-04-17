/*
 * Copyright (C) 2019 OnGres, Inc.
 * SPDX-License-Identifier: AGPL-3.0-or-later
 */

package io.stackgres.operator.initialization;

import java.util.List;
import javax.enterprise.context.ApplicationScoped;
import javax.inject.Inject;

import com.google.common.collect.ImmutableList;
import io.stackgres.common.crd.sgpooling.StackGresPoolingConfig;

@ApplicationScoped
public class PoolingFactoryProvider
    implements DefaultFactoryProvider<DefaultCustomResourceFactory<StackGresPoolingConfig>> {

  private final DefaultCustomResourceFactory<StackGresPoolingConfig> factory;

  @Inject
  public PoolingFactoryProvider(DefaultCustomResourceFactory<StackGresPoolingConfig> factory) {
    this.factory = factory;
  }

  @Override
  public List<DefaultCustomResourceFactory<StackGresPoolingConfig>> getFactories() {
    return ImmutableList.of(factory);
  }
}

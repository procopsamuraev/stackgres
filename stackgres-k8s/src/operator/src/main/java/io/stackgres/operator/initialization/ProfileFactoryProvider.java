/*
 * Copyright (C) 2019 OnGres, Inc.
 * SPDX-License-Identifier: AGPL-3.0-or-later
 */

package io.stackgres.operator.initialization;

import java.util.List;
import javax.enterprise.context.ApplicationScoped;
import javax.inject.Inject;

import com.google.common.collect.ImmutableList;
import io.stackgres.common.crd.sgprofile.StackGresProfile;

@ApplicationScoped
public class ProfileFactoryProvider
    implements DefaultFactoryProvider<DefaultCustomResourceFactory<StackGresProfile>> {

  private final DefaultCustomResourceFactory<StackGresProfile> factory;

  @Inject
  public ProfileFactoryProvider(DefaultCustomResourceFactory<StackGresProfile> factory) {
    this.factory = factory;
  }

  @Override
  public List<DefaultCustomResourceFactory<StackGresProfile>> getFactories() {
    return ImmutableList.of(factory);
  }
}

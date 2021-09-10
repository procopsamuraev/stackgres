/*
 * Copyright (C) 2019 OnGres, Inc.
 * SPDX-License-Identifier: AGPL-3.0-or-later
 */

package io.stackgres.operator.conciliation.dbops;

import javax.enterprise.context.ApplicationScoped;
import javax.inject.Inject;

import io.stackgres.common.CdiUtil;
import io.stackgres.operator.conciliation.AbstractDecoratorResource;
import io.stackgres.operator.conciliation.ResourceGenerationDiscoverer;
import io.stackgres.operator.conciliation.factory.DecoratorDiscoverer;

@ApplicationScoped
public class DbOpsDecoratorResource extends AbstractDecoratorResource<StackGresDbOpsContext> {

  @Inject
  public DbOpsDecoratorResource(DecoratorDiscoverer<StackGresDbOpsContext> decoratorDiscoverer,
      ResourceGenerationDiscoverer<StackGresDbOpsContext> generators) {
    super(decoratorDiscoverer, generators);
  }

  public DbOpsDecoratorResource() {
    super(null, null);
    CdiUtil.checkPublicNoArgsConstructorIsCalledToCreateProxy();
  }

}

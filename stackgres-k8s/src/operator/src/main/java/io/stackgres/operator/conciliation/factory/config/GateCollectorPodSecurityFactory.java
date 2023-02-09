/*
 * Copyright (C) 2019 OnGres, Inc.
 * SPDX-License-Identifier: AGPL-3.0-or-later
 */

package io.stackgres.operator.conciliation.factory.config;

import javax.enterprise.context.ApplicationScoped;

import io.fabric8.kubernetes.api.model.PodSecurityContext;
import io.stackgres.operator.conciliation.config.StackGresConfigContext;
import io.stackgres.operator.conciliation.factory.PodSecurityFactory;
import io.stackgres.operator.conciliation.factory.ResourceFactory;

@ApplicationScoped
public class GateCollectorPodSecurityFactory extends PodSecurityFactory
    implements ResourceFactory<StackGresConfigContext, PodSecurityContext> {

  @Override
  public PodSecurityContext createResource(StackGresConfigContext source) {
    return createPodSecurityContext();
  }

  @Override
  protected Long getUser() {
    return 10001L;
  }

  @Override
  protected Long getGroup() {
    return 10001L;
  }

}

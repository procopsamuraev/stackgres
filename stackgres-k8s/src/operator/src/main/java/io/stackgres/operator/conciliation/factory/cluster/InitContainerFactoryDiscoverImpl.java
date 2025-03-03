/*
 * Copyright (C) 2019 OnGres, Inc.
 * SPDX-License-Identifier: AGPL-3.0-or-later
 */

package io.stackgres.operator.conciliation.factory.cluster;

import java.util.List;

import javax.enterprise.context.ApplicationScoped;
import javax.enterprise.inject.Any;
import javax.enterprise.inject.Instance;
import javax.inject.Inject;

import io.stackgres.operator.conciliation.AnnotatedResourceDiscoverer;
import io.stackgres.operator.conciliation.InitContainerFactoryDiscover;
import io.stackgres.operator.conciliation.factory.ContainerFactory;
import io.stackgres.operator.conciliation.factory.InitContainer;

@ApplicationScoped
public class InitContainerFactoryDiscoverImpl
    extends AnnotatedResourceDiscoverer<ContainerFactory<ClusterContainerContext>,
        InitContainer>
    implements InitContainerFactoryDiscover<ClusterContainerContext> {

  @Inject
  public InitContainerFactoryDiscoverImpl(
      @Any
      Instance<ContainerFactory<ClusterContainerContext>> instance) {
    init(instance);
    resourceHub.forEach((key, value) -> {
      value.sort((f1, f2) -> {
        int f1Order = getAnnotation(f1, InitContainer.class)
            .value().ordinal();
        int f2Order = getAnnotation(f2, InitContainer.class)
            .value().ordinal();
        return Integer.compare(f1Order, f2Order);
      });
    });
  }

  @Override
  protected Class<InitContainer> getAnnotationClass() {
    return InitContainer.class;
  }

  @Override
  public List<ContainerFactory<ClusterContainerContext>> discoverContainers(
      ClusterContainerContext context) {
    return resourceHub.get(context.getClusterContext().getVersion()).stream()
        .filter(f -> f.isActivated(context))
        .toList();
  }
}

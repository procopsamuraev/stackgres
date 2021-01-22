/*
 * Copyright (C) 2019 OnGres, Inc.
 * SPDX-License-Identifier: AGPL-3.0-or-later
 */

package io.stackgres.operator.conciliation.factory.cluster.sidecars.fluentbit;

import javax.inject.Inject;
import javax.inject.Singleton;

import io.stackgres.common.LabelFactory;
import io.stackgres.common.crd.sgcluster.StackGresCluster;
import io.stackgres.operator.cluster.factory.ClusterStatefulSetEnvironmentVariables;
import io.stackgres.operator.common.Sidecar;
import io.stackgres.operator.conciliation.OperatorVersionBinder;
import io.stackgres.operator.conciliation.cluster.StackGresVersion;
import io.stackgres.operator.conciliation.factory.RunningContainer;

@Sidecar(AbstractFluentBit.NAME)
@Singleton
@OperatorVersionBinder(startAt = StackGresVersion.V10A1, stopAt = StackGresVersion.V10)
@RunningContainer(order = 2)
public class FluentBit extends AbstractFluentBit {

  @Inject
  public FluentBit(ClusterStatefulSetEnvironmentVariables clusterStatefulSetEnvironmentVariables,
                   LabelFactory<StackGresCluster> labelFactory) {
    super(clusterStatefulSetEnvironmentVariables, labelFactory);
  }
}

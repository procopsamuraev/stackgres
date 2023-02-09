/*
 * Copyright (C) 2019 OnGres, Inc.
 * SPDX-License-Identifier: AGPL-3.0-or-later
 */

package io.stackgres.operator.conciliation.config;

import java.util.List;
import java.util.Optional;

import io.fabric8.kubernetes.api.model.Pod;
import io.fabric8.kubernetes.client.VersionInfo;
import io.stackgres.common.StackGresVersion;
import io.stackgres.common.crd.sgcluster.StackGresCluster;
import io.stackgres.common.crd.sgconfig.StackGresConfig;
import io.stackgres.operator.common.PrometheusInstallations;
import io.stackgres.operator.conciliation.GenerationContext;
import org.immutables.value.Value;
import org.jooq.lambda.tuple.Tuple2;

@Value.Immutable
public interface StackGresConfigContext extends GenerationContext<StackGresConfig> {

  List<Tuple2<StackGresCluster, List<Pod>>> getClusterPods();

  Optional<VersionInfo> getKubernetesVersion();

  Optional<PrometheusInstallations> getPrometheus();

  @Override
  default StackGresVersion getVersion() {
    return StackGresVersion.LATEST;
  }

}

/*
 * Copyright (C) 2019 OnGres, Inc.
 * SPDX-License-Identifier: AGPL-3.0-or-later
 */

package io.stackgres.operator.common;

import java.util.List;

import io.stackgres.common.prometheus.PrometheusInstallation;

public class PrometheusInstallations {

  private final List<PrometheusInstallation> prometheusInstallations;

  public PrometheusInstallations(List<PrometheusInstallation> prometheusInstallations) {
    this.prometheusInstallations = prometheusInstallations;
  }

  public boolean getCreatePodMonitor() {
    return !prometheusInstallations.isEmpty();
  }

  public List<PrometheusInstallation> getPrometheusInstallations() {
    return prometheusInstallations;
  }

}

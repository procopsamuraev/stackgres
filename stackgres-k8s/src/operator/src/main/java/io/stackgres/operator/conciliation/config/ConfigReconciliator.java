/*
 * Copyright (C) 2019 OnGres, Inc.
 * SPDX-License-Identifier: AGPL-3.0-or-later
 */

package io.stackgres.operator.conciliation.config;

import javax.enterprise.context.ApplicationScoped;
import javax.enterprise.context.Dependent;
import javax.enterprise.event.Observes;
import javax.inject.Inject;

import io.fabric8.kubernetes.client.KubernetesClient;
import io.quarkus.runtime.ShutdownEvent;
import io.quarkus.runtime.StartupEvent;
import io.stackgres.common.StackGresVersion;
import io.stackgres.common.crd.sgconfig.ConfigEventReason;
import io.stackgres.common.crd.sgconfig.StackGresConfig;
import io.stackgres.common.crd.sgconfig.StackGresConfigStatus;
import io.stackgres.common.event.EventEmitter;
import io.stackgres.common.resource.CustomResourceScanner;
import io.stackgres.common.resource.CustomResourceScheduler;
import io.stackgres.operator.conciliation.AbstractReconciliator;
import io.stackgres.operator.conciliation.ComparisonDelegator;
import io.stackgres.operator.conciliation.Conciliator;
import io.stackgres.operator.conciliation.HandlerDelegator;
import io.stackgres.operator.conciliation.ReconciliationResult;
import org.slf4j.helpers.MessageFormatter;

@ApplicationScoped
public class ConfigReconciliator
    extends AbstractReconciliator<StackGresConfig> {

  @Dependent
  static class Parameters {
    @Inject CustomResourceScanner<StackGresConfig> scanner;
    @Inject Conciliator<StackGresConfig> conciliator;
    @Inject HandlerDelegator<StackGresConfig> handlerDelegator;
    @Inject KubernetesClient client;
    @Inject EventEmitter<StackGresConfig> eventController;
    @Inject CustomResourceScheduler<StackGresConfig> operatorConfigScheduler;
    @Inject ComparisonDelegator<StackGresConfig> resourceComparator;
  }

  private final String operatorVersion;
  private final EventEmitter<StackGresConfig> eventController;
  private final CustomResourceScheduler<StackGresConfig> operatorConfigScheduler;

  @Inject
  public ConfigReconciliator(Parameters parameters) {
    super(parameters.scanner, parameters.conciliator, parameters.handlerDelegator,
        parameters.client, StackGresConfig.KIND);
    this.operatorVersion = StackGresVersion.LATEST.getVersion();
    this.eventController = parameters.eventController;
    this.operatorConfigScheduler = parameters.operatorConfigScheduler;
  }

  void onStart(@Observes StartupEvent ev) {
    start();
  }

  void onStop(@Observes ShutdownEvent ev) {
    stop();
  }

  @Override
  public void onPreReconciliation(StackGresConfig config) {
  }

  @Override
  public void onPostReconciliation(StackGresConfig config) {
    operatorConfigScheduler.update(config,
        (targetOperatorConfig, operatorConfigWithStatus) -> {
          if (targetOperatorConfig.getStatus() == null) {
            targetOperatorConfig.setStatus(new StackGresConfigStatus());
          }
          targetOperatorConfig.getStatus().setVersion(operatorVersion);
        });
  }

  @Override
  public void onConfigCreated(StackGresConfig cluster, ReconciliationResult result) {
  }

  @Override
  public void onConfigUpdated(StackGresConfig cluster, ReconciliationResult result) {
  }

  @Override
  public void onError(Exception ex, StackGresConfig cluster) {
    String message = MessageFormatter.arrayFormat(
        "Cluster reconciliation cycle failed",
        new String[]{
        }).getMessage();
    eventController.sendEvent(ConfigEventReason.OPERATOR_CONFIG_ERROR,
        message + ": " + ex.getMessage(), cluster);
  }

}

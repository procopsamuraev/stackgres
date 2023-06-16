/*
 * Copyright (C) 2019 OnGres, Inc.
 * SPDX-License-Identifier: AGPL-3.0-or-later
 */

package io.stackgres.operator.conciliation.factory.cluster;

import java.util.stream.Stream;
import javax.inject.Inject;
import javax.inject.Singleton;

import io.fabric8.kubernetes.api.model.HasMetadata;
import io.fabric8.kubernetes.api.model.Secret;
import io.fabric8.kubernetes.api.model.SecretBuilder;
import io.stackgres.common.EnvoyUtil;
import io.stackgres.common.PatroniUtil;
import io.stackgres.common.crd.sgcluster.StackGresCluster;
import io.stackgres.common.crd.sgcluster.StackGresClusterConfigurationServiceBinding;
import io.stackgres.common.crd.sgcluster.StackGresClusterStatus;
import io.stackgres.common.labels.LabelFactoryForCluster;
import io.stackgres.operator.conciliation.OperatorVersionBinder;
import io.stackgres.operator.conciliation.ResourceGenerator;
import io.stackgres.operator.conciliation.cluster.StackGresClusterContext;
import io.stackgres.operator.conciliation.factory.cluster.patroni.PatroniSecret;

@Singleton
@OperatorVersionBinder
public class ServiceBindingSecret implements ResourceGenerator<StackGresClusterContext> {

  private LabelFactoryForCluster<StackGresCluster> labelFactory;

  private static final String DEFAULT_SERVICE_BINDING_TYPE = "postgresql";

  private static final String DEFAULT_SERVICE_BINDING_PROVIDER = "stackgres";

  @Inject
  public void setFactoryFactory(LabelFactoryForCluster<StackGresCluster> labelFactory) {
    this.labelFactory = labelFactory;
  }

  @Override
  public Stream<HasMetadata> generateResource(StackGresClusterContext context) {
    StackGresCluster cluster = context.getCluster();
    StackGresClusterConfigurationServiceBinding serviceBindingConfiguration =
      cluster.getSpec().getConfiguration().getBinding();

    if (serviceBindingConfiguration == null && cluster.getStatus().getBinding() == null) {
      return Stream.empty();
    }

    if (serviceBindingConfiguration != null) {
      return Stream.of(this.createSecretServiceBindingFromSgClusterSpecValues(context,
        serviceBindingConfiguration));
    } else {
      return Stream.of(this.createSecretServiceBindingWithDefaultValues(context));
    }
  }

  private Secret createSecretServiceBindingWithDefaultValues(StackGresClusterContext context) {
    return new SecretBuilder()
      .withType(this.getServiceBindingType())
      .withNewMetadata()
      .withName(this.getServiceBindingName(context.getCluster()))
      .withNamespace(context.getCluster().getMetadata().getNamespace())
      .endMetadata()
      .addToData("type", DEFAULT_SERVICE_BINDING_TYPE)
      .addToData("provider", DEFAULT_SERVICE_BINDING_PROVIDER)
      .addToData("host", this.getPgHost(context))
      .addToData("port", this.getPgPort())
      .addToData("username", this.getPgUsernameFromSuperUserCredentials(context))
      .addToData("password", this.getPgUserPasswordFromSuperUserCredentials(context))
      .addToData("uri", this.buildPgConnectionUri(context,
        this.getPgUsernameFromSuperUserCredentials(context),
        this.getPgUserPasswordFromSuperUserCredentials(context), null))
      .build();
  }

  private Secret createSecretServiceBindingFromSgClusterSpecValues(StackGresClusterContext context,
    StackGresClusterConfigurationServiceBinding serviceBindingConfiguration) {
    StackGresCluster cluster = context.getCluster();

    return new SecretBuilder()
      .withType(this.getServiceBindingType())
      .withNewMetadata()
      .withName(this.getServiceBindingName(cluster))
      .withNamespace(cluster.getMetadata().getNamespace())
      .endMetadata()
      .addToData("type", DEFAULT_SERVICE_BINDING_TYPE)
      .addToData("provider", serviceBindingConfiguration.getProvider())
      .addToData("host", this.getPgHost(context))
      .addToData("port", this.getPgPort())
      .addToData("username", serviceBindingConfiguration.getUsername())
      .addToData("password", context.getUserPasswordForBinding().get())
      .addToData("uri", this.buildPgConnectionUri(context,
        serviceBindingConfiguration.getUsername(),
        context.getUserPasswordForBinding().get(),
        serviceBindingConfiguration.getDatabase()))
      .build();
  }

  private String buildPgConnectionUri(StackGresClusterContext context, String pgUsername,
    String pgUserPassword, String database) {
    if (database == null || database.isEmpty()) {
      return String.format("postgresql://%s:%s@%s:%s", pgUsername, pgUserPassword,
        this.getPgHost(context), this.getPgPort());
    }
    return String.format("postgresql://%s:%s@%s:%s/%s", pgUsername, pgUserPassword,
      this.getPgHost(context), this.getPgPort(), database);
  }

  private String getServiceBindingType() {
    return String.format("servicebinding.io/%s", DEFAULT_SERVICE_BINDING_TYPE);
  }

  private String getPgPort() {
    return String.valueOf(EnvoyUtil.PG_PORT);
  }

  private String getPgHost(StackGresClusterContext context) {
    return PatroniUtil.readWriteName(context.getCluster()).concat(".").
      concat(context.getCluster().getMetadata().getNamespace());
  }

  private String getPgUsernameFromSuperUserCredentials(StackGresClusterContext context) {
    return PatroniSecret.getSuperuserCredentials(context).v1;
  }

  private String getPgUserPasswordFromSuperUserCredentials(StackGresClusterContext context) {
    return PatroniSecret.getSuperuserCredentials(context).v2;
  }

  private String getServiceBindingName(StackGresCluster cluster) {
    StackGresClusterStatus status = cluster.getStatus();
    if (status != null && status.getBinding() != null
      && status.getBinding().getName() != null) {
      return status.getBinding().getName();
    }

    return cluster.getMetadata().getName() + "-binding";
  }
}

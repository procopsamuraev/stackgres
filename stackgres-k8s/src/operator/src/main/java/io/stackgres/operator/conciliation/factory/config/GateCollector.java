/*
 * Copyright (C) 2019 OnGres, Inc.
 * SPDX-License-Identifier: AGPL-3.0-or-later
 */

package io.stackgres.operator.conciliation.factory.config;

import java.util.Map;
import java.util.stream.Stream;

import javax.inject.Inject;
import javax.inject.Singleton;

import com.fasterxml.jackson.databind.ObjectMapper;
import io.fabric8.kubernetes.api.model.ConfigMap;
import io.fabric8.kubernetes.api.model.ConfigMapVolumeSourceBuilder;
import io.fabric8.kubernetes.api.model.ContainerBuilder;
import io.fabric8.kubernetes.api.model.HasMetadata;
import io.fabric8.kubernetes.api.model.IntOrString;
import io.fabric8.kubernetes.api.model.LabelSelectorBuilder;
import io.fabric8.kubernetes.api.model.PodTemplateSpecBuilder;
import io.fabric8.kubernetes.api.model.ProbeBuilder;
import io.fabric8.kubernetes.api.model.SecretVolumeSourceBuilder;
import io.fabric8.kubernetes.api.model.VolumeBuilder;
import io.fabric8.kubernetes.api.model.VolumeMountBuilder;
import io.fabric8.kubernetes.api.model.apps.Deployment;
import io.fabric8.kubernetes.api.model.apps.DeploymentBuilder;
import io.stackgres.common.GateCollectorPath;
import io.stackgres.common.StackGresContext;
import io.stackgres.common.StackGresUtil;
import io.stackgres.common.crd.sgconfig.StackGresConfig;
import io.stackgres.common.labels.LabelFactoryForConfig;
import io.stackgres.operator.conciliation.OperatorVersionBinder;
import io.stackgres.operator.conciliation.ResourceGenerator;
import io.stackgres.operator.conciliation.config.ConfigResourceGenerationDiscoverer;
import io.stackgres.operator.conciliation.config.StackGresConfigContext;

@Singleton
@OperatorVersionBinder
public class GateCollector
    implements ResourceGenerator<StackGresConfigContext> {

  private final LabelFactoryForConfig labelFactory;
  private final GateCollectorPodSecurityFactory podSecurityFactory;
  private final ObjectMapper objectMapper;
  private final ConfigResourceGenerationDiscoverer configResourceGenerationDiscoverer;

  @Inject
  public GateCollector(
      LabelFactoryForConfig labelFactory,
      GateCollectorPodSecurityFactory podSecurityFactory,
      ObjectMapper objectMapper,
      ConfigResourceGenerationDiscoverer configResourceGenerationDiscoverer) {
    this.labelFactory = labelFactory;
    this.podSecurityFactory = podSecurityFactory;
    this.objectMapper = objectMapper;
    this.configResourceGenerationDiscoverer = configResourceGenerationDiscoverer;
  }

  @Override
  public Stream<HasMetadata> generateResource(StackGresConfigContext context) {
    final StackGresConfig config = context.getSource();

    Deployment gateCollector = new DeploymentBuilder()
        .withNewMetadata()
        .withNamespace(config.getMetadata().getNamespace())
        .withName(config.getMetadata().getName() + "-gate-collector")
        .withLabels(labelFactory.genericLabels(config))
        .endMetadata()
        .withNewSpec()
        .withReplicas(1)
        .withSelector(new LabelSelectorBuilder()
            .addToMatchLabels(labelFactory.gateCollectorLabels(config))
            .build())
        .withTemplate(new PodTemplateSpecBuilder()
            .withNewMetadata()
            .withAnnotations(Map.of(
                StackGresContext.CONFIG_HASH_KEY,
                configResourceGenerationDiscoverer
                .getResourceGenerators(context)
                .stream()
                .filter(GateCollectorConfigMap.class::isInstance)
                .findFirst()
                .orElseThrow()
                .generateResource(context)
                .filter(ConfigMap.class::isInstance)
                .map(ConfigMap.class::cast)
                .findAny()
                .map(ConfigMap::getData)
                .map(data -> data.get(StackGresUtil.MD5SUM_KEY))
                .orElseThrow()))
            .withLabels(labelFactory.gateCollectorLabels(config))
            .endMetadata()
            .withNewSpec()
            .withSecurityContext(podSecurityFactory.createResource(context))
            .withContainers(new ContainerBuilder()
                .withName("stackgres-collector")
                .withImage(config.getSpec().getCollectorImage())
                .withCommand(
                    "/otelcol-contrib",
                    "--config",
                    GateCollectorPath.ETC_OTEL_CONTRIB_CONFIG_PATH.path())
                .withPorts(config.getSpec().getCollectorContainerPorts(objectMapper))
                .withLivenessProbe(new ProbeBuilder()
                    .withNewHttpGet()
                    .withPort(new IntOrString(13133))
                    .withScheme("HTTP")
                    .endHttpGet()
                    .withInitialDelaySeconds(5)
                    .withPeriodSeconds(30)
                    .withTimeoutSeconds(10)
                    .build())
                .withReadinessProbe(new ProbeBuilder()
                    .withNewHttpGet()
                    .withPort(new IntOrString(13133))
                    .withScheme("HTTP")
                    .endHttpGet()
                    .withInitialDelaySeconds(0)
                    .withPeriodSeconds(30)
                    .withTimeoutSeconds(2)
                    .build())
                .withResources(config.getSpec().getCollectorResources(objectMapper))
                .withVolumeMounts(
                    new VolumeMountBuilder()
                    .withName("collector-certs")
                    .withMountPath("/etc/otelcol-contrib/certs")
                    .withReadOnly(true)
                    .build(),
                    new VolumeMountBuilder()
                    .withName("collector-otelcol-contrib-etc")
                    .withMountPath(GateCollectorPath.ETC_OTEL_CONTRIB_CONFIG_PATH.path())
                    .withSubPath(GateCollectorPath.ETC_OTEL_CONTRIB_CONFIG_PATH.filename())
                    .withReadOnly(true)
                    .build())
                .addAllToVolumeMounts(config.getSpec().getCollectorVolumeMounts(objectMapper))
                .build())
            .withVolumes(
                new VolumeBuilder()
                .withName("collector-certs")
                .withSecret(new SecretVolumeSourceBuilder()
                    .withSecretName(context.getSource().getSpec().getCollectorCertName()
                        .orElse(context.getSource().getMetadata().getName() + "-col-certs"))
                    .withOptional(true)
                    .build())
                .build(),
                new VolumeBuilder()
                .withName("collector-otelcol-contrib-etc")
                .withConfigMap(new ConfigMapVolumeSourceBuilder()
                    .withName(GateCollectorConfigMap.name(context))
                    .build())
                .build())
            .endSpec()
            .build())
        .endSpec()
        .build();

    return Stream.of(gateCollector);
  }

}

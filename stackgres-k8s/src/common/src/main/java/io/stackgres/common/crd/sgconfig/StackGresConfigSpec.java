/*
 * Copyright (C) 2019 OnGres, Inc.
 * SPDX-License-Identifier: AGPL-3.0-or-later
 */

package io.stackgres.common.crd.sgconfig;

import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.function.Predicate;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import io.fabric8.kubernetes.api.model.ContainerPort;
import io.fabric8.kubernetes.api.model.ResourceRequirements;
import io.fabric8.kubernetes.api.model.ServiceSpec;
import io.fabric8.kubernetes.api.model.VolumeMount;
import io.quarkus.runtime.annotations.RegisterForReflection;
import io.stackgres.common.StackGresUtil;
import io.stackgres.common.crd.JsonArray;
import io.stackgres.common.crd.JsonObject;
import io.stackgres.common.prometheus.PrometheusInstallation;
import org.jooq.lambda.Unchecked;

@RegisterForReflection
@JsonInclude(JsonInclude.Include.NON_DEFAULT)
@JsonIgnoreProperties(ignoreUnknown = true)
public class StackGresConfigSpec extends JsonObject {

  private static final long serialVersionUID = 1L;

  public StackGresConfigSpec() {
    super();
  }

  public StackGresConfigSpec(int initialCapacity, float loadFactor) {
    super(initialCapacity, loadFactor);
  }

  public StackGresConfigSpec(int initialCapacity) {
    super(initialCapacity);
  }

  public StackGresConfigSpec(Map<? extends String, ? extends Object> m) {
    super(m);
  }

  @JsonIgnore
  public String getCollectorImage() {
    return getCollectorImageNameWithRegistry().orElseThrow()
        + ":"
        + getCollectorImageTag().orElseThrow();
  }

  private Optional<String> getCollectorImageNameWithRegistry() {
    return getCollectorImageName()
        .filter(Predicate.not(name -> name.matches("^[^/]+\\.[^/]+/.*$")))
        .flatMap(name -> getContainerRegistry()
            .map(registry -> registry + "/" + name))
        .or(() -> getCollectorImageName());
  }

  private Optional<String> getCollectorImageTag() {
    return getCollectorImageSection()
    .map(image -> image.get("tag"))
    .filter(String.class::isInstance)
    .map(String.class::cast);
  }

  private Optional<String> getCollectorImageName() {
    return getCollectorImageSection()
        .map(collector -> collector.get("name"))
        .filter(String.class::isInstance)
        .map(String.class::cast);
  }

  @JsonIgnore
  public ServiceSpec getCollectorServiceSpec(ObjectMapper objectMapper) {
    return getCollectorSection()
        .map(collector -> collector.getObject("service"))
        .map(service -> service.getObject("spec"))
        .map(objectMapper::valueToTree)
        .map(Unchecked.function(ports -> objectMapper
            .convertValue(ports, ServiceSpec.class)))
        .orElse(null);
  }

  @JsonIgnore
  public List<ContainerPort> getCollectorContainerPorts(ObjectMapper objectMapper) {
    return getCollectorSection()
        .map(collector -> collector.getArray("ports"))
        .map(objectMapper::valueToTree)
        .map(Unchecked.function(ports -> objectMapper
            .convertValue(ports, new ContainerPortsTypeReference())))
        .orElse(List.of());
  }

  @JsonIgnore
  public ResourceRequirements getCollectorResources(ObjectMapper objectMapper) {
    return getCollectorSection()
        .map(collector -> collector.getObject("resources"))
        .map(objectMapper::valueToTree)
        .map(Unchecked.function(requirements -> objectMapper
            .convertValue(requirements, ResourceRequirements.class)))
        .orElse(null);
  }

  @JsonIgnore
  public List<VolumeMount> getCollectorVolumeMounts(ObjectMapper objectMapper) {
    return getCollectorSection()
        .map(collector -> collector.getArray("volumeMounts"))
        .map(objectMapper::valueToTree)
        .map(Unchecked.function(volumeMounts -> objectMapper
            .convertValue(volumeMounts, new VolumeMountsTypeReference())))
        .orElse(null);
  }

  @JsonIgnore
  public Optional<JsonObject> getCollectorGateExtensions() {
    return getCollectorConfigSection()
        .map(config -> config.getObject("extensions"));
  }

  @JsonIgnore
  public Optional<JsonObject> getCollectorGateProcessors() {
    return getCollectorConfigSection()
        .map(config -> config.getObject("processors"));
  }

  @JsonIgnore
  public Optional<JsonObject> getCollectorGateExporters() {
    return getCollectorConfigSection()
        .map(config -> config.getObject("exporters"));
  }

  @JsonIgnore
  public Optional<JsonObject> getCollectorGateServiceTelemetry() {
    return getCollectorConfigSection()
        .map(config -> config.getObject("service"))
        .map(service -> service.getObject("telemetry"));
  }

  @JsonIgnore
  public Optional<JsonArray> getCollectorGateServiceExtensions() {
    return getCollectorConfigSection()
        .map(config -> config.getObject("service"))
        .map(service -> service.getArray("extensions"));
  }

  @JsonIgnore
  public Optional<JsonObject> getCollectorGateServicePipelines() {
    return getCollectorConfigSection()
        .map(config -> config.getObject("service"))
        .map(service -> service.getObject("pipelines"));
  }

  @JsonIgnore
  public boolean getCollectorPrometheusOperatorAutodiscoveryEnabled() {
    return getCollectorSection()
        .map(collector -> collector.getObject("prometheusOperator"))
        .map(prometheusOperator -> prometheusOperator.get("allowDiscovery"))
        .filter(Boolean.class::isInstance)
        .map(Boolean.class::cast)
        .orElse(false);
  }

  @JsonIgnore
  public List<PrometheusInstallation> getCollectorPrometheusOperatorMonitors(
      ObjectMapper objectMapper) {
    return getCollectorSection()
        .map(collector -> collector.getObject("prometheusOperator"))
        .map(prometheusOperator -> prometheusOperator.getArray("monitors"))
        .map(Unchecked.function(monitors -> objectMapper
            .convertValue(monitors, new PrometheusInstallationsTypeReference())))
        .orElse(List.of());
  }

  public Optional<String> getCollectorCertName() {
    return Optional
        .ofNullable(this)
        .map(config -> config.getObject("cert"))
        .map(cert -> cert.get("collectorSecretName"))
        .filter(String.class::isInstance)
        .map(String.class::cast);
  }

  @Override
  public String toString() {
    return StackGresUtil.toPrettyYaml(this);
  }

  private static class PrometheusInstallationsTypeReference
      extends TypeReference<List<PrometheusInstallation>> {
  }

  private static class ContainerPortsTypeReference
      extends TypeReference<List<ContainerPort>> {
  }

  private static class VolumeMountsTypeReference
      extends TypeReference<List<VolumeMount>> {
  }

  private Optional<JsonObject> getCollectorImageSection() {
    return getCollectorSection()
    .map(collector -> collector.getObject("image"));
  }

  private Optional<JsonObject> getCollectorConfigSection() {
    return getCollectorSection()
        .map(collector -> collector.getObject("config"));
  }

  private Optional<JsonObject> getCollectorSection() {
    return Optional
        .ofNullable(this)
        .map(config -> config.getObject("collector"));
  }

  private Optional<String> getContainerRegistry() {
    return Optional
        .ofNullable(this)
        .map(config -> config.get("containerRegistry"))
        .filter(String.class::isInstance)
        .map(String.class::cast);
  }

}

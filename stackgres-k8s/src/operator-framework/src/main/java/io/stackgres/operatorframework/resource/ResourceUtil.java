/*
 * Copyright (C) 2019 OnGres, Inc.
 * SPDX-License-Identifier: AGPL-3.0-or-later
 */

package io.stackgres.operatorframework.resource;

import java.nio.charset.StandardCharsets;
import java.util.Base64;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Optional;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

import com.google.common.base.Preconditions;
import io.fabric8.kubernetes.api.model.HasMetadata;
import io.fabric8.kubernetes.api.model.ObjectMeta;
import io.fabric8.kubernetes.api.model.ObjectReference;
import io.fabric8.kubernetes.api.model.ObjectReferenceBuilder;
import io.fabric8.kubernetes.api.model.OwnerReference;
import io.fabric8.kubernetes.api.model.OwnerReferenceBuilder;
import io.fabric8.kubernetes.api.model.apiextensions.v1.CustomResourceDefinition;
import io.fabric8.kubernetes.client.KubernetesClient;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public interface ResourceUtil {

  Logger LOGGER = LoggerFactory.getLogger(ResourceUtil.class);
  Pattern INDEX_PATTERN = Pattern.compile("^.*-([0-9]+)$");

  String KUBERNETES_NAME_KEY = "app.kubernetes.io/name";
  String KUBERNETES_INSTANCE_KEY = "app.kubernetes.io/instance";
  String KUBERNETES_VERSION_KEY = "app.kubernetes.io/version";
  String KUBERNETES_COMPONENT_KEY = "app.kubernetes.io/component";
  String KUBERNETES_PART_OF_KEY = "app.kubernetes.io/part-of";
  String KUBERNETES_MANGED_BY_KEY = "app.kubernetes.io/managed-by";
  String KUBERNETES_CREATED_BY_KEY = "app.kubernetes.io/created-by";

  /**
   * Filter metadata of resources to find if the name match in the provided list.
   *
   * @param list resources with metadata to filter
   * @param name to check for match in the list
   * @return true if the name exists in the list
   */
  static boolean exists(List<? extends HasMetadata> list, String name) {
    return list.stream()
        .map(HasMetadata::getMetadata)
        .map(ObjectMeta::getName)
        .anyMatch(name::equals);
  }

  static String resourceName(String name) {
    Preconditions.checkArgument(name.length() <= 253);
    return name;
  }

  static String sanitizedResourceName(String name) {
    return name
        .toLowerCase(Locale.US)
        .replaceAll("[^a-z0-9-]+", "-")
        .replaceAll("(^-+|-+$)", "");
  }

  static String volumeName(String name) {
    Preconditions.checkArgument(name.length() <= 63);
    return name;
  }

  static String cutVolumeName(String name) {
    if (name.length() <= 63) {
      return name;
    }
    String cutName = name.substring(0, 63);
    if (cutName.matches("^.*[^A-Za-z0-9]$")) {
      cutName = cutName.substring(0, 62) + 'x';
    }
    return cutName;
  }

  static String containerName(String name) {
    Preconditions.checkArgument(name.length() <= 63);
    return name;
  }

  static String labelKey(String name) {
    Preconditions.checkArgument(name.length() <= 63);
    return name;
  }

  static String labelValue(String name) {
    Preconditions.checkArgument(name.length() <= 63);
    return name;
  }

  static Pattern getIndexPattern() {
    return INDEX_PATTERN;
  }

  static Pattern getNameWithIndexPattern(String name) {
    return Pattern.compile("^" + Pattern.quote(name) + "-([0-9]+)$");
  }

  static Pattern getNameWithHashPattern(String name) {
    return Pattern.compile("^" + Pattern.quote(name) + "-([a-z0-9]+){10}-([a-z0-9]+){5}$");
  }

  /**
   * Get a custom resource definition from Kubernetes.
   *
   * @param client Kubernetes client to call the API.
   * @param crdName Name of the CDR to lookup.
   * @return the CustomResourceDefinition model.
   */
  static Optional<CustomResourceDefinition> getCustomResource(KubernetesClient client,
      String crdName) {
    return Optional.ofNullable(client.apiextensions().v1().customResourceDefinitions()
        .withName(crdName).get());
  }

  /**
   * Get the object reference of any resource.
   */
  static ObjectReference getObjectReference(HasMetadata resource) {
    return new ObjectReferenceBuilder()
        .withApiVersion(resource.getApiVersion())
        .withKind(resource.getKind())
        .withNamespace(resource.getMetadata().getNamespace())
        .withName(resource.getMetadata().getName())
        .withResourceVersion(resource.getMetadata().getResourceVersion())
        .withUid(resource.getMetadata().getUid())
        .build();
  }

  /**
   * Get the owner reference of any resource.
   */
  static OwnerReference getOwnerReference(HasMetadata resource) {
    return new OwnerReferenceBuilder()
        .withApiVersion(resource.getApiVersion())
        .withKind(resource.getKind())
        .withName(resource.getMetadata().getName())
        .withUid(resource.getMetadata().getUid())
        .withController(true)
        .build();
  }

  static Map<String, String> encodeSecret(Map<String, String> data) {
    return data.entrySet().stream()
        .map(entry -> Map.entry(entry.getKey(), encodeSecret(entry.getValue())))
        .collect(Collectors.toMap(Map.Entry::getKey, Map.Entry::getValue));
  }

  static String encodeSecret(String string) {
    return Base64.getEncoder().encodeToString(string.getBytes(StandardCharsets.UTF_8));
  }

  static Map<String, String> decodeSecret(Map<String, String> data) {
    return data.entrySet().stream()
        .map(entry -> Map.entry(entry.getKey(), decodeSecret(entry.getValue())))
        .collect(Collectors.toMap(Map.Entry::getKey, Map.Entry::getValue));
  }

  static String decodeSecret(String string) {
    return new String(Base64.getDecoder().decode(string.getBytes(StandardCharsets.UTF_8)),
        StandardCharsets.UTF_8);
  }

  static boolean isServiceAccountUsername(String username) {
    return username.startsWith("system:serviceaccount:") && username.split(":").length == 4;
  }

  static String getServiceAccountFromUsername(String username) {
    return username.split(":")[3];
  }
}

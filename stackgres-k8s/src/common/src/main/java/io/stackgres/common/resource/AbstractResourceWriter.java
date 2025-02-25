/*
 * Copyright (C) 2019 OnGres, Inc.
 * SPDX-License-Identifier: AGPL-3.0-or-later
 */

package io.stackgres.common.resource;

import io.fabric8.kubernetes.api.model.HasMetadata;
import io.fabric8.kubernetes.api.model.KubernetesResourceList;
import io.fabric8.kubernetes.client.KubernetesClient;
import io.fabric8.kubernetes.client.dsl.MixedOperation;
import io.fabric8.kubernetes.client.dsl.Resource;
import io.stackgres.common.CdiUtil;

public abstract class AbstractResourceWriter<T extends HasMetadata,
    L extends KubernetesResourceList<T>, R extends Resource<T>>
    implements ResourceWriter<T> {

  private final KubernetesClient client;

  protected AbstractResourceWriter(KubernetesClient client) {
    this.client = client;
  }

  public AbstractResourceWriter() {
    CdiUtil.checkPublicNoArgsConstructorIsCalledToCreateProxy(getClass());
    this.client = null;
  }

  @Override
  public T create(T resource) {
    return getResourceEndpoints(client)
        .inNamespace(resource.getMetadata().getNamespace())
        .resource(resource)
        .create();
  }

  @Override
  public T update(T resource) {
    return getResourceEndpoints(client)
        .inNamespace(resource.getMetadata().getNamespace())
        .resource(resource)
        .patch();
  }

  @Override
  public T update(T resource, String patch) {
    return getResourceEndpoints(client)
        .inNamespace(resource.getMetadata().getNamespace())
        .resource(resource)
        .patch(patch);
  }

  @Override
  public void delete(T resource) {
    getResourceEndpoints(client)
        .inNamespace(resource.getMetadata().getNamespace())
        .resource(resource)
        .delete();
  }

  protected abstract MixedOperation<T, L, R> getResourceEndpoints(
      KubernetesClient client);

}

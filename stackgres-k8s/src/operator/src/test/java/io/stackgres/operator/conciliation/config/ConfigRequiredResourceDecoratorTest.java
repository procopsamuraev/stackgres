/*
 * Copyright (C) 2019 OnGres, Inc.
 * SPDX-License-Identifier: AGPL-3.0-or-later
 */

package io.stackgres.operator.conciliation.config;

import java.io.IOException;

import javax.inject.Inject;

import io.fabric8.kubernetes.api.model.HasMetadata;
import io.quarkus.test.junit.QuarkusTest;
import io.stackgres.common.crd.sgconfig.StackGresConfig;
import io.stackgres.common.crd.sgconfig.StackGresConfigStatus;
import io.stackgres.common.fixture.Fixtures;
import io.stackgres.common.resource.ResourceUtil;
import io.stackgres.operator.conciliation.AbstractRequiredResourceDecoratorTest;
import io.stackgres.operator.conciliation.RequiredResourceDecorator;
import org.junit.jupiter.api.BeforeEach;

@QuarkusTest
class ConfigRequiredResourceDecoratorTest
    extends AbstractRequiredResourceDecoratorTest<StackGresConfigContext> {

  @Inject
  ConfigRequiredResourceDecorator resourceDecorator;

  private StackGresConfig resource;

  @BeforeEach
  public void setup() {
    this.resource = Fixtures.config().loadDefault().get();
  }

  @Override
  protected String usingCrdFilename() {
    return "SGConfig.yaml";
  }

  @Override
  protected HasMetadata getResource() {
    return this.resource;
  }

  @Override
  public void assertThatResourceNameIsCompliant(HasMetadata resource) {
    ResourceUtil.nameIsValidService(resource.getMetadata().getName());
  }

  @Override
  protected RequiredResourceDecorator<StackGresConfigContext> getResourceDecorator() {
    return this.resourceDecorator;
  }

  @Override
  protected StackGresConfigContext getResourceContext() throws IOException {
    StackGresConfigStatus status = new StackGresConfigStatus();
    resource.setStatus(status);
    return ImmutableStackGresConfigContext.builder()
        .source(resource)
        .build();
  }

}

/*
 * Copyright (C) 2019 OnGres, Inc.
 * SPDX-License-Identifier: AGPL-3.0-or-later
 */

package io.stackgres.operator.conciliation;

import static io.stackgres.operator.validation.CrdMatchTestHelper.getMaxLengthResourceNameFrom;
import static io.stackgres.testutil.StringUtils.getRandomClusterNameWithExactlySize;
import static java.lang.String.format;
import static org.junit.Assert.assertThrows;

import java.io.IOException;
import java.util.List;
import java.util.Map.Entry;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.google.common.base.Preconditions;
import io.fabric8.kubernetes.api.model.HasMetadata;
import io.fabric8.kubernetes.api.model.PersistentVolumeClaim;
import io.fabric8.kubernetes.api.model.Service;
import io.fabric8.kubernetes.api.model.apps.StatefulSet;
import io.fabric8.kubernetes.api.model.batch.v1.CronJob;
import io.fabric8.kubernetes.api.model.batch.v1.Job;
import io.stackgres.common.crd.sgcluster.StackGresCluster;
import io.stackgres.common.crd.sgscript.StackGresScript;
import io.stackgres.common.resource.ResourceUtil;
import junit.framework.AssertionFailedError;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

public abstract class AbstractRequiredResourceDecoratorTest<T> {

  private int sgClusterMaxLength;
  private int sgScriptMaxLength;

  @BeforeEach
  void setUp() throws Exception {
    sgClusterMaxLength = getMaxLengthResourceNameFrom(StackGresCluster.KIND);
    sgScriptMaxLength = getMaxLengthResourceNameFrom(StackGresScript.KIND);
  }

  @Test
  void shouldCreateResourceSuccessfully_OnceUsingTheCurrentCrdMaxLength() throws IOException {
    String validClusterName =
        getRandomClusterNameWithExactlySize(getMaxLengthResourceNameFrom(usingKind()));
    getResource().getMetadata().setName(validClusterName);

    List<HasMetadata> decorateResources =
        getResourceDecorator().decorateResources(getResourceContext());
    decorateResources.stream().forEach(this::assertNameAndLabels);
  }

  @Test
  void shouldGetAnExceededNameMessage_OnceUsingAnExceededMaxLengthName()
      throws JsonProcessingException, IOException {
    String invalidClusterName =
        getRandomClusterNameWithExactlySize(getMaxLengthResourceNameFrom(usingKind()) + 1);
    getResource().getMetadata().setName(invalidClusterName);

    assertThrows(AssertionFailedError.class, () -> {
      List<HasMetadata> decorateResources =
          getResourceDecorator().decorateResources(getResourceContext());
      decorateResources.stream().forEach(this::assertNameAndLabels);
    });
  }

  protected abstract String usingKind();

  protected abstract HasMetadata getResource();

  protected abstract RequiredResourceDecorator<T> getResourceDecorator();

  protected abstract T getResourceContext() throws IOException;

  private void assertNameAndLabels(HasMetadata resource) {
    try {
      assertThatResourceNameIsComplaint(resource);

      resource.getMetadata().getLabels().entrySet().stream().forEach(label -> {
        asserThatLabelIsComplaint(label);
      });

      assertThatStatefulSetResourceLabelsAreComplaints(resource);
      assertThatCronJobResourceLabelsAreComplaints(resource);
      assertThatJobResourceLabelsAreComplaints(resource);
    } catch (Exception ex) {
      throw new AssertionFailedError(format(
          "Validation for resource %s of kind %s failed: %s",
          resource.getMetadata().getName(), resource.getKind(), ex.getMessage()));
    }
  }

  public void assertThatResourceNameIsComplaint(HasMetadata resource) {
    if (resource instanceof Service) {
      ResourceUtil.nameIsValidService(resource.getMetadata().getName());
    } else if (resource instanceof StatefulSet) {
      ResourceUtil.nameIsValidDnsSubdomainForSts(resource.getMetadata().getName());
    } else if (resource instanceof Job) {
      ResourceUtil.nameIsValidDnsSubdomainForJob(resource.getMetadata().getName());
    } else if (resource instanceof CronJob) {
      ResourceUtil.nameIsValidDnsSubdomainForCronJob(resource.getMetadata().getName());
    } else if (resource instanceof StackGresCluster) {
      final String name = resource.getMetadata().getName();
      ResourceUtil.nameIsValidDnsSubdomain(name);
      Preconditions.checkArgument(name.length() <= sgClusterMaxLength,
          format("Valid name must be %s characters or less. But was %d (%s)",
              sgClusterMaxLength, name.length(), name));
    } else if (resource instanceof StackGresScript) {
      final String name = resource.getMetadata().getName();
      ResourceUtil.nameIsValidDnsSubdomain(name);
      Preconditions.checkArgument(name.length() <= sgScriptMaxLength,
          format("Valid name must be %s characters or less. But was %d (%s)",
              sgScriptMaxLength, name.length(), name));
    } else {
      ResourceUtil.nameIsValidDnsSubdomain(resource.getMetadata().getName());
    }
  }

  public void asserThatLabelIsComplaint(Entry<String, String> label) {
    try {
      ResourceUtil.labelKey(label.getKey());
    } catch (Exception ex) {
      throw new AssertionFailedError(format(
          "Validation of key for label key %s with value %s failed",
          label.getKey(), label.getValue()));
    }
    try {
      ResourceUtil.labelValue(label.getValue());
    } catch (Exception ex) {
      throw new AssertionFailedError(format(
          "Validation of value for label key %s with value %s failed",
          label.getKey(), label.getValue()));
    }
  }

  public void assertThatStatefulSetResourceLabelsAreComplaints(HasMetadata resource) {
    if (resource instanceof StatefulSet) {
      final StatefulSet statefulSet = (StatefulSet) resource;
      statefulSet.getSpec().getTemplate().getMetadata().getLabels().entrySet().stream()
          .forEach(label -> {
            asserThatLabelIsComplaint(label);
          });

      assertThatVolumeClaimLabelsAreComplaints(statefulSet);

      statefulSet.getSpec().getTemplate().getMetadata().getLabels().entrySet().stream()
          .forEach(label -> {
            asserThatLabelIsComplaint(label);
          });
    }
  }

  private void assertThatVolumeClaimLabelsAreComplaints(StatefulSet statefulSet) {
    List<PersistentVolumeClaim> volumeClaims =
        statefulSet.getSpec().getVolumeClaimTemplates();

    volumeClaims.stream().forEach(volume -> {
      volume.getMetadata().getLabels().entrySet().stream().forEach(label -> {
        asserThatLabelIsComplaint(label);
      });
    });
  }

  public void assertThatCronJobResourceLabelsAreComplaints(HasMetadata resource) {
    if (resource instanceof CronJob) {
      ((CronJob) resource).getSpec().getJobTemplate().getMetadata().getLabels().entrySet()
          .stream().forEach(label -> {
            asserThatLabelIsComplaint(label);
          });
    }
  }

  public void assertThatJobResourceLabelsAreComplaints(HasMetadata resource) {
    if (resource instanceof Job) {
      ((Job) resource).getSpec().getTemplate().getMetadata().getLabels().entrySet().stream()
          .forEach(label -> {
            asserThatLabelIsComplaint(label);
          });
    }
  }

}

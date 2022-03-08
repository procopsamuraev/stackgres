/*
 * Copyright (C) 2019 OnGres, Inc.
 * SPDX-License-Identifier: AGPL-3.0-or-later
 */

package io.stackgres.operator.validation;

import io.stackgres.operator.common.PoolingReview;
import io.stackgres.operator.common.fixture.AdmissionReviewFixtures;
import io.stackgres.operatorframework.admissionwebhook.validating.ValidationFailed;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.runner.RunWith;
import org.mockito.junit.MockitoJUnitRunner;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
@RunWith(MockitoJUnitRunner.class)
class PgBouncerValidationResourceTest extends ValidationResourceTest<PoolingReview> {

  @BeforeEach
  public void setUp() {
    final PgBouncerValidationResource resource = new PgBouncerValidationResource();
    resource.setPipeline(pipeline);
    this.resource = resource;

    review = AdmissionReviewFixtures.poolingConfig().loadCreate().get();

    deleteReview = AdmissionReviewFixtures.poolingConfig().loadDelete().get();
  }

  @Test
  void givenAnValidAdmissionReview_itShouldReturnASuccessfulResponse() throws ValidationFailed {

    super.givenAnValidAdmissionReview_itShouldReturnASuccessfulResponse();

  }

  @Test
  void givenAnInvalidAdmissionReview_itShouldReturnAFailedResponse() throws ValidationFailed {

    super.givenAnInvalidAdmissionReview_itShouldReturnAFailedResponse();

  }

  @Test
  void givenAnDeletionReview_itShouldNotFail() throws ValidationFailed {
    super.givenAnDeletionReview_itShouldNotFail();

  }

}

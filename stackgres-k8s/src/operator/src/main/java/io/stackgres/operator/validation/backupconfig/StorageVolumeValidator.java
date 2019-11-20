/*
 * Copyright (C) 2019 OnGres, Inc.
 * SPDX-License-Identifier: AGPL-3.0-or-later
 */

package io.stackgres.operator.validation.backupconfig;

import javax.enterprise.context.ApplicationScoped;

import io.stackgres.operator.customresource.sgbackupconfig.BackupVolume;
import io.stackgres.operator.validation.BackupConfigReview;
import io.stackgres.operatorframework.Operation;
import io.stackgres.operatorframework.ValidationFailed;

@ApplicationScoped
public class StorageVolumeValidator implements BackupConfigValidator {

  @Override
  public void validate(BackupConfigReview review) throws ValidationFailed {

    Operation operation = review.getRequest().getOperation();
    if (operation == Operation.CREATE || operation == Operation.UPDATE) {

      if (review.getRequest().getObject().getSpec()
          .getStorage().getVolume() == null) {
        return;
      }

      BackupVolume volume = review.getRequest().getObject().getSpec()
          .getStorage().getVolume();
      String volumeType = volume.getType();

      if (volumeType.equals("nfs")
          && volume.getNfs() == null) {
        throw new ValidationFailed("Invalid backup configuration,"
            + " nfs source volume must be set when source type is nfs");
      }

      if (volumeType.equals("cephfs")
          && volume.getNfs() == null) {
        throw new ValidationFailed("Invalid backup configuration,"
            + " cephfs source volume must be set when source type is cephfs");
      }

      if (volumeType.equals("glusterfs")
          && volume.getNfs() == null) {
        throw new ValidationFailed("Invalid backup configuration,"
            + " glusterfs source volume must be set when source type is glusterfs");
      }

      if (volume.getNfs() == null
          && volume.getCephfs() == null
          && volume.getGlusterfs() == null) {
        throw new ValidationFailed("Invalid backup configuration,"
            + " source volume requires any of nfs, cephfs or glusterfs to be set");
      }

      if ((volume.getNfs() != null //NOPMD
          && volume.getCephfs() != null)
          || (volume.getNfs() != null //NOPMD
          && volume.getGlusterfs() != null)
          || (volume.getCephfs() != null //NOPMD
          && volume.getGlusterfs() != null)) {
        throw new ValidationFailed("Invalid backup configuration,"
            + " source volume requires only one of nfs, cephfs or glusterfs to be set");
      }

    }
  }
}

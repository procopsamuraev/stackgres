/*
 * Copyright (C) 2019 OnGres, Inc.
 * SPDX-License-Identifier: AGPL-3.0-or-later
 */

package io.stackgres.apiweb.dto.shardedcluster;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import io.quarkus.runtime.annotations.RegisterForReflection;
import io.stackgres.apiweb.dto.cluster.ClusterSpec;

@RegisterForReflection
@JsonInclude(JsonInclude.Include.NON_DEFAULT)
@JsonIgnoreProperties(value = { "instances", "postgres", "postgresServices",
    "initialData", "replicateFrom", "distributedLogs", "toInstallPostgresExtensions",
    "prometheusAutobind", "nonProductionOptions" })
public class ShardedClusterShard extends ClusterSpec {

  @JsonProperty("index")
  private int index;

  @JsonProperty("instancesPerCluster")
  private Integer instancesPerCluster;

  @JsonProperty("replication")
  private ShardedClusterReplication replicationForShards;

  @JsonProperty("configurations")
  private ShardedClusterInnerConfiguration configurationForShards;

  public int getIndex() {
    return index;
  }

  public void setIndex(int index) {
    this.index = index;
  }

  public Integer getInstancesPerCluster() {
    return instancesPerCluster;
  }

  public void setInstancesPerCluster(Integer instancesPerCluster) {
    this.instancesPerCluster = instancesPerCluster;
  }

  public ShardedClusterReplication getReplicationForShards() {
    return replicationForShards;
  }

  public void setReplicationForShards(ShardedClusterReplication replicationForShards) {
    this.replicationForShards = replicationForShards;
  }

  public ShardedClusterInnerConfiguration getConfigurationForShards() {
    return configurationForShards;
  }

  public void setConfigurationForShards(ShardedClusterInnerConfiguration configurationForShards) {
    this.configurationForShards = configurationForShards;
  }

}

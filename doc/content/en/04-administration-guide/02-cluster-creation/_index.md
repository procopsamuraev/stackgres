---
title: Creating a Cluster
weight: 2
url: /administration/cluster-creation
aliases: [ /administration/install/cluster-creation , /tutorial/simple-cluster , /tutorial/complete-cluster, /tutorial/complete-cluster/create-cluster ]
description: Details about how to create a production StackGres cluster.
showToc: true
---

This page will guide you though the creation of a production-ready StackGres cluster using your custom configuration.

## Customizing Your Postgres Clusters

The following shows examples of StackGres' versatile configuration options.
In general, these steps are optional, but we do recommend to consider these features for production setups.

### Configuring an Instance Profile

You can create your cluster with different hardware specifications using an [SGInstanceProfile]({{% relref "06-crd-reference/02-sginstanceprofile" %}}) custom resource (CR) as follows:

```yaml
cat << EOF | kubectl apply -f -
apiVersion: stackgres.io/v1
kind: SGInstanceProfile
metadata:
  namespace: my-cluster
  name: size-small
spec:
  cpu: "2"
  memory: "4Gi"
EOF
```

### Configuring Postgres and PGBouncer

You can also change Postgres' configuration using an [SGPostgresConfig]({{% relref "06-crd-reference/03-sgpostgresconfig" %}}) CR, or the PGBouncer settings using [SGPoolingConfig]({{% relref "06-crd-reference/04-sgpoolingconfig" %}}), the backup storage specification using [SGObjectStorage]({{% relref "06-crd-reference/09-sgobjectstorage" %}}), and more.

The next code snippets will show you how to use these CRs.

Let's start with a custom PostgreSQL configuration, using `SGPostgresConfig`:

```yaml
cat << EOF | kubectl apply -f -
apiVersion: stackgres.io/v1
kind: SGPostgresConfig
metadata:
  namespace: my-cluster
  name: pgconfig1
spec:
  postgresVersion: "12"
  postgresql.conf:
    shared_buffers: '512MB'
    random_page_cost: '1.5'
    password_encryption: 'scram-sha-256'
    log_checkpoints: 'on'
EOF
```

You can configure the variables supported by StackGres.

The connection pooler (currently PgBouncer) is an important part of a Postgres cluster, as it provides connection scaling capabilities.
We'll cover all more details about this in the [Customizing Pooling configuration section]({{% relref "04-administration-guide/03-configuration/03-connection-pooling" %}}).

For improved performance and stability, it is recommended to set the `pool_mode` to `transaction`. An example pooling configuration looks like this:

```yaml
cat << EOF | kubectl apply -f -
apiVersion: stackgres.io/v1
kind: SGPoolingConfig
metadata:
  namespace: my-cluster
  name: poolconfig1
spec:
  pgBouncer:
    pgbouncer.ini:
      pgbouncer:
        pool_mode: transaction
        max_client_conn: '1000'
        default_pool_size: '80'
EOF
```

### Configuring Backups

The [SGObjectStorage]({{% relref "06-crd-reference/09-sgobjectstorage" %}}) CRs are used to configure how backups are being taken.

The following shows and example configuration using [Google Cloud Storage](https://cloud.google.com/storage/):

```yaml
cat << EOF | kubectl apply -f -
apiVersion: stackgres.io/v1beta1
kind: SGObjectStorage
metadata:
  namespace: my-cluster
  name: backupconfig1
spec:
  type: "gcs"
  gcs:
    bucket: backup-my-cluster-of-stackgres-io
    gcpCredentials:
      secretKeySelectors:
        serviceAccountJSON: 
          name: gcp-backup-bucket-secret
          key: my-creds.json
EOF
```

Or alternatively, for [AWS S3](https://aws.amazon.com/s3/):

```yaml
cat << EOF | kubectl apply -f -
apiVersion: stackgres.io/v1beta1
kind: SGObjectStorage
metadata:
  namespace: my-cluster
  name: backupconfig1
spec:
  type: 's3'
  s3:
    bucket: 'backup.my-cluster.stackgres.io'
    awsCredentials:
      secretKeySelectors:
        accessKeyId: {name: 'aws-creds-secret', key: 'accessKeyId'}
        secretAccessKey: {name: 'aws-creds-secret', key: 'secretAccessKey'}
EOF
```

You will need to perform additional steps in order to configure backups in your cloud environment.
Have a look at the section [Backups]({{% relref "04-administration-guide/04-backups" %}}) for full examples using S3, GKE, Digital Ocean, and more.

### Configuring Distributed Logs

You can create an SGDistributedLogs CR to enable a [distributed log cluster]({{% relref "06-crd-reference/07-sgdistributedlogs" %}}):

```yaml
cat << EOF | kubectl apply -f -
apiVersion: stackgres.io/v1
kind: SGDistributedLogs
metadata:
  namespace: my-cluster
  name: distributedlogs
spec:
  persistentVolume:
    size: 50Gi
EOF
```

### Configuring Scripts

Last but not least, StackGres lets you include several `managedSql` scripts, to perform cluster operations at startup.

In this example, we're creating a Postgres user, using a Kubernetes secret:

```
kubectl -n my-cluster create secret generic pgbench-user-password-secret \
  --from-literal=pgbench-create-user-sql="create user pgbench password 'admin123'"
```

Then we reference the secret in a [SGScript]({{% relref "06-crd-reference/10-sgscript" %}}):

```yaml
cat << EOF | kubectl apply -f -
apiVersion: stackgres.io/v1
kind: SGScript
metadata:
  namespace: mycluster
  name: cluster-scripts
spec:
  scripts:
  - name: create-pgbench-user
    scriptFrom:
      secretKeyRef:
        name: pgbench-user-password-secret
        key: pgbench-create-user-sql
  - name: create-pgbench-database
    script: |
      create database pgbench owner pgbench;
EOF
```

The scripts are defined both by the Secret created before and SQL instructions inline.

The SGScript will be referenced in the `managedSql` definition of the cluster, shown below.

Note that we could equally well define the SQL script in a config map, however, since the password represents a credential, we're using a secret.

## Creating the Cluster

All the required steps were performed to create our StackGres Cluster.

Create the SGCluster resource:

```yaml
cat << EOF | kubectl apply -f -
apiVersion: stackgres.io/v1
kind: SGCluster
metadata:
  namespace: my-cluster
  name: cluster
spec:
  postgres:
    version: '12.3'
  instances: 3
  sgInstanceProfile: 'size-small'
  pods:
    persistentVolume:
      size: '10Gi'
  configurations:
    sgPostgresConfig: 'pgconfig1'
    sgPoolingConfig: 'poolconfig1'
    backups:
    - sgObjectStorage: 'backupconfig1'
      cronSchedule: '*/5 * * * *'
      retention: 6
  managedSql:
    scripts:
    - sgScript: cluster-scripts
  distributedLogs:
    sgDistributedLogs: 'distributedlogs'
  prometheusAutobind: true
EOF
```

Notice that each resource has been defined with its own `name`, and is referenced in the StackGres cluster definition.
The order of the CR creation is relevant to successfully create a cluster, that is you create all resources, secrets, and permissions necessary before creating dependent resources.

Another helpful configuration is the [prometheusAutobind: true]({{% relref "04-administration-guide/01-stackgres-installation/02-installation-via-helm/01-operator-parameters" %}}) definition.
This parameter automatically enables monitoring for our cluster.
We can use this since we've installed the Prometheus operator on our Kubernetes environment.

Awesome, now you can sit back and relax while the SGCluster is spinning up.

While the cluster is being created, you may notice a blip in the distributed logs server, where a container is restarted.
This behavior is caused by a re-configuration which requires a container restart, and only temporarily pauses the log collection.
No logs are lost, since they are buffered on the source pods.

Have a look at [Connecting to the Cluster]({{% relref "04-administration-guide/02-connecting-to-the-cluster" %}}), to see how to connect to the created Postgres cluster.

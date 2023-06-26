---
title: Sharded Cluster Passwords
weight: 1
url: /administration/sharded-cluster/connection/passwords
description: Describes how to retrieve the generated database passwords.
showToc: true
---

When creating a sharded cluster, StackGres randomly generates passwords, for the `postgres` superuser and others.
The passwords are stored in a secret (named as the sharded cluster).

By default, a Stackgres sharded cluster initialization creates 3 users:

- `superuser`
- `replication`
- `authenticator`

The passwords are stored in that secret under the keys `<user>-password`.

Assuming that we have a Stackgres sharded cluster named `cluster`, we can get the passwords with the following commands:

- **superuser / postgres:**

```
PASSWORD=$(kubectl get secret cluster --template '{{ printf "%s" (index .data "superuser-password" | base64decode) }}')
echo "user: superuser"
echo "user: postgres"
echo "password: $PASSWORD"
```
> **Note:** the superuser's password is the same as the postgres password

- **replication:**

```
PASSWORD=$(kubectl get secret cluster --template '{{ printf "%s" (index .data "replication-password" | base64decode) }}')
echo "user: replication"
echo "password: $PASSWORD"
```

- **authenticator:**

```
PASSWORD=$(kubectl get secret cluster --template '{{ printf "%s" (index .data "authenticator-password" | base64decode) }}')
echo "user: authenticator"
echo "password: $PASSWORD"
```
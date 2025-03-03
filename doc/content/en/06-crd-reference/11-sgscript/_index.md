---
title: SGScript
weight: 11
url: /reference/crd/sgscript
description: Details about SGScript
showToc: true
---

The `SGScript` custom resource represents an ordered list of versioned SQL scripts.

___

**Kind:** SGScript

**listKind:** SGScriptList

**plural:** sgscripts

**singular:** sgscript
___

**Spec**

## Storage Type Configuration

| Property                                                              | Required               | Updatable | Type    | Default | Description |
|:----------------------------------------------------------------------|------------------------|-----------|:--------|:--------|:------------|
| managedVersions                                                       |                        | ✓         | boolean |         | {{< crd-field-description SGScript.spec.managedVersions >}} |
| continueOnError                                                       |                        | ✓         | boolean |         | {{< crd-field-description SGScript.spec.continueOnError >}} |

Example:

```yaml
apiVersion: stackgres.io/v1beta1
kind: SGScript
metadata:
  name: script
spec:
  managedVersions: true
  continueOnError: false
  scripts:
  - name: create-stackgres-user
    scriptFrom:
      secretKeyRef: # read the user from a Secret to maintain credentials in a safe place
        name: stackgres-secret-sqls-scripts
        key: create-stackgres-user.sql
  - name: create-stackgres-database
    script: |
      CREATE DATABASE stackgres WITH OWNER stackgres;
  - name: create-stackgres-schema
    database: stackgres
    scriptFrom:
      configMapKeyRef: # read long script from a ConfigMap to avoid have to much data in the helm releasea and the sgcluster CR
        name: stackgres-sqls-scripts
        key: create-stackgres-schema.sql
```

## Script entry

| <div style="width:6rem">Property</div> | Required | Updatable | <div style="width:5rem">Type</div> | <div style="width:5rem">Default</div> | Description |
|:---------------------------------------|----------|-----------|:---------|:---------|:------------|
| name                                   |          | ✓         | string   |          | {{< crd-field-description SGScript.spec.scripts.items.name >}} |
| id                                     |          |           | integer  |          | {{< crd-field-description SGScript.spec.scripts.items.id >}} |
| version                                |          | ✓         | integer  |          | {{< crd-field-description SGScript.spec.scripts.items.version >}} |
| database                               |          | ✓         | string   | postgres | {{< crd-field-description SGScript.spec.scripts.items.database >}} |
| user                                   |          | ✓         | string   | postgres | {{< crd-field-description SGScript.spec.scripts.items.user >}} |
| script                                 |          | ✓         | string   |          | {{< crd-field-description SGScript.spec.scripts.items.script >}} |
| [scriptFrom](#script-from)             |          | ✓         | object   |          | {{< crd-field-description SGScript.spec.scripts.items.scriptFrom >}} |
| retryOnError                           |          | ✓         | boolean  |          | {{< crd-field-description SGScript.spec.scripts.items.retryOnError >}} |
| wrapInTransaction                      |          | ✓         | string   |          | {{< crd-field-description SGScript.spec.scripts.items.wrapInTransaction >}} |
| storeStatusInDatabase                  |          | ✓         | boolean  |          | {{< crd-field-description SGScript.spec.scripts.items.storeStatusInDatabase >}} |

### Script from

| Property                                  | Required | Updatable | Type     | Default  | Description |
|:------------------------------------------|----------|-----------|:---------|:---------|:------------|
| [configMapKeyRef](#script-from-configmap) |          | ✓         | object   |          | {{< crd-field-description SGScript.spec.scripts.items.scriptFrom.configMapKeyRef >}} |
| [secretKeyRef](#script-from-configmap)    |          | ✓         | object   |          | {{< crd-field-description SGScript.spec.scripts.items.scriptFrom.secretKeyRef >}} |

#### Script from ConfigMap

| Property  | Required | Updatable | Type     | Default  | Description |
|:----------|----------|-----------|:---------|:---------|:------------|
| name      | ✓        | ✓         | string   |          | {{< crd-field-description SGScript.spec.scripts.items.scriptFrom.configMapKeyRef.name >}} |
| key       | ✓        | ✓         | string   |          | {{< crd-field-description SGScript.spec.scripts.items.scriptFrom.configMapKeyRef.key >}} |

#### Script from Secret

| Property  | Required | Updatable | Type     | Default  | Description |
|:----------|----------|-----------|:---------|:---------|:------------|
| name      | ✓        | ✓         | string   |          | {{< crd-field-description SGScript.spec.scripts.items.scriptFrom.secretKeyRef.name >}} |
| key       | ✓        | ✓         | string   |          | {{< crd-field-description SGScript.spec.scripts.items.scriptFrom.secretKeyRef.key >}} |

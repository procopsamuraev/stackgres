/*
 * Copyright (C) 2019 OnGres, Inc.
 * SPDX-License-Identifier: AGPL-3.0-or-later
 */

package io.stackgres.operator.conciliation.factory.config;

import static org.mockito.Mockito.when;

import java.util.List;
import java.util.Map;

import io.fabric8.kubernetes.api.model.ConfigMap;
import io.fabric8.kubernetes.api.model.PodBuilder;
import io.stackgres.common.GateCollectorPath;
import io.stackgres.common.crd.JsonArray;
import io.stackgres.common.crd.JsonObject;
import io.stackgres.common.crd.sgcluster.StackGresCluster;
import io.stackgres.common.crd.sgconfig.StackGresConfig;
import io.stackgres.common.crd.sgconfig.StackGresConfigSpec;
import io.stackgres.common.fixture.Fixtures;
import io.stackgres.common.labels.ConfigLabelFactory;
import io.stackgres.common.labels.ConfigLabelMapper;
import io.stackgres.operator.conciliation.config.StackGresConfigContext;
import io.stackgres.testutil.JsonUtil;
import org.jooq.lambda.tuple.Tuple;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class GateCollectorConfigMapTest {

  private static final JsonObject DUMMY_OBJECT = new JsonObject(Map.of("test", "test"));
  private static final JsonArray DUMMY_ARRAY =
      new JsonArray(List.of(new JsonObject(Map.of("test", "test"))));

  private GateCollectorConfigMap gateCollectorConfigMap;

  @Mock
  private StackGresConfigContext configContext;

  private StackGresConfig config;

  private StackGresCluster cluster;

  @BeforeEach
  void setUp() {
    gateCollectorConfigMap = new GateCollectorConfigMap(
        new ConfigLabelFactory(new ConfigLabelMapper()));
    config = Fixtures.config().loadDefault().get();
    when(configContext.getSource()).thenReturn(config);
    cluster = Fixtures.cluster().loadDefault().get();
  }

  @Test
  void givenADefaultCollectorConfiguration_itShouldCreateAValidConfig()
      throws Exception {
    var configMap = gateCollectorConfigMap.generateResource(configContext)
        .map(ConfigMap.class::cast)
        .findFirst()
        .orElseThrow();

    Assertions.assertNotNull(configMap.getData());
    Assertions.assertTrue(configMap.getData()
        .containsKey(GateCollectorPath.ETC_OTEL_CONTRIB_CONFIG_PATH.filename()));
    JsonUtil.yamlMapper().readTree(configMap.getData()
        .get(GateCollectorPath.ETC_OTEL_CONTRIB_CONFIG_PATH.filename()));
  }

  @Test
  void givenAComplexCollectorConfiguration_itShouldCreateAValidConfig()
      throws Exception {
    config.setSpec(
        new StackGresConfigSpec(Map.of("collector",
            new JsonObject(Map.of("config",
                new JsonObject(Map.of(
                    "extensions", DUMMY_OBJECT,
                    "processors", DUMMY_OBJECT,
                    "exporters", DUMMY_OBJECT,
                    "service", new JsonObject(Map.of(
                        "telemetry", DUMMY_OBJECT,
                        "extensions", DUMMY_ARRAY,
                        "pipelines", DUMMY_OBJECT))
                    ))
                ))
            )));
    var configMap = gateCollectorConfigMap.generateResource(configContext)
        .map(ConfigMap.class::cast)
        .findFirst()
        .orElseThrow();

    Assertions.assertNotNull(configMap.getData());
    Assertions.assertTrue(configMap.getData()
        .containsKey(GateCollectorPath.ETC_OTEL_CONTRIB_CONFIG_PATH.filename()));
    var configNode = JsonUtil.yamlMapper().readTree(configMap.getData()
        .get(GateCollectorPath.ETC_OTEL_CONTRIB_CONFIG_PATH.filename()));
    Assertions.assertTrue(
        configNode.get("extensions").has("test"));
    Assertions.assertTrue(
        configNode.get("processors").has("test"));
    Assertions.assertTrue(
        configNode.get("exporters").has("test"));
    Assertions.assertTrue(
        configNode.get("service").get("telemetry").has("test"));
    Assertions.assertTrue(
        configNode.get("service").get("extensions").get(1).has("test"));
    Assertions.assertTrue(
        configNode.get("service").get("pipelines").has("test"));
  }

  @Test
  void givenACollectorConfigurationWithClusterPods_itShouldCreateAValidConfig()
      throws Exception {
    when(configContext.getClusterPods()).thenReturn(
        List.of(Tuple.tuple(cluster, List.of(
            new PodBuilder()
            .withNewMetadata()
            .withName("pod-0")
            .endMetadata()
            .withNewStatus()
            .withPodIP("1.2.3.4")
            .endStatus()
            .build()))));

    var configMap = gateCollectorConfigMap.generateResource(configContext)
        .map(ConfigMap.class::cast)
        .findFirst()
        .orElseThrow();

    Assertions.assertNotNull(configMap.getData());
    Assertions.assertTrue(configMap.getData()
        .containsKey(GateCollectorPath.ETC_OTEL_CONTRIB_CONFIG_PATH.filename()));
    var configNode = JsonUtil.yamlMapper().readTree(configMap.getData()
        .get(GateCollectorPath.ETC_OTEL_CONTRIB_CONFIG_PATH.filename()));
    Assertions.assertEquals("pod-0-postgres",
        configNode.get("receivers").get("prometheus").get("config")
        .get("scrape_configs").get(0).get("job_name").asText());
    Assertions.assertEquals("1.2.3.4:9187",
        configNode.get("receivers").get("prometheus").get("config")
        .get("scrape_configs").get(0).get("static_configs").get(0)
        .get("targets").get(0).asText());
    Assertions.assertEquals("pod-0-envoy",
        configNode.get("receivers").get("prometheus").get("config")
        .get("scrape_configs").get(1).get("job_name").asText());
    Assertions.assertEquals("1.2.3.4:8001",
        configNode.get("receivers").get("prometheus").get("config")
        .get("scrape_configs").get(1).get("static_configs").get(0)
        .get("targets").get(0).asText());
  }

}

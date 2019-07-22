/*
 * Copyright 2018 StreamSets Inc.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package com.streamsets.pipeline.stage.processor.tensorflow;

import com.streamsets.pipeline.api.ConfigDef;
import com.streamsets.pipeline.api.FieldSelectorModel;
import com.streamsets.pipeline.api.ListBeanModel;

import java.util.List;

public class TensorFlowConfigBean {
  public static final String MODEL_PATH_CONFIG = "conf.modelPath";

  @ConfigDef(
      required = true,
      type = ConfigDef.Type.STRING,
      defaultValue = "",
      label = "保存模型路径",
      description = "模型的本地路径",
      displayPosition = 10,
      group = "TENSOR_FLOW"
  )
  public String modelPath;

  @ConfigDef(
      required = true,
      type = ConfigDef.Type.LIST,
      defaultValue = "",
      label = "模型标签",
      description = "用于TensorFlow模型的标记",
      displayPosition = 20,
      group = "TENSOR_FLOW"
  )
  @ListBeanModel
  public List<String> modelTags;


  @ConfigDef(
      required = false,
      type = ConfigDef.Type.MODEL,
      label = "输入配置",
      description = "输入在模型的训练和导出过程中配置的张量信息",
      displayPosition = 30,
      group = "TENSOR_FLOW"
  )
  @ListBeanModel
  public List<TensorInputConfig> inputConfigs;

  @ConfigDef(
      required = false,
      type = ConfigDef.Type.MODEL,
      label = "输出配置",
      description = "输出在模型的训练和导出过程中配置的张量信息",
      displayPosition = 40,
      group = "TENSOR_FLOW"
  )
  @ListBeanModel
  public List<TensorConfig> outputConfigs;


  @ConfigDef(
      required = true,
      type = ConfigDef.Type.BOOLEAN,
      defaultValue = "false",
      label = "整批",
      description = "立即计算整个批。当TensorFlow模型期望多个输入生成一个输出时选择。当TensorFlow模型期望一个输入生成一个输出时清除。",
      displayPosition = 50,	
      group = "TENSOR_FLOW"
  )
  public boolean useEntireBatch = false;

  @ConfigDef(
      required = true,
      type = ConfigDef.Type.MODEL,
      defaultValue = "/output",
      label = "输出字段",
      description = "输出字段用于预测或分类结果",
      displayPosition = 60,
      group = "TENSOR_FLOW",
      dependsOn = "useEntireBatch",
      triggeredByValue = "false"
  )
  @FieldSelectorModel(singleValued = true)
  public String outputField;
}

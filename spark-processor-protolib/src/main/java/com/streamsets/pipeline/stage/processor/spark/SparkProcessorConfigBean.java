/*
 * Copyright 2017 StreamSets Inc.
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
package com.streamsets.pipeline.stage.processor.spark;

import com.streamsets.pipeline.api.ConfigDef;
import com.streamsets.pipeline.api.GenerateResourceBundle;
import com.streamsets.pipeline.lib.el.VaultEL;

import java.util.ArrayList;
import java.util.List;

@GenerateResourceBundle
public class SparkProcessorConfigBean {

  public static final String DEFAULT_THREAD_COUNT = "4";
  public static final String DEFAULT_APP_NAME = "SDC Spark App";

  @ConfigDef(
      required = false,
      type = ConfigDef.Type.NUMBER,
      defaultValue = DEFAULT_THREAD_COUNT,
      label = "并行度(单机模式)",
      description = "每批记录要创建的分区数。在群集模式中忽略。",
      group = "SPARK",
      displayPosition = 10
  )
  public int threadCount;

  @ConfigDef(
      required = false,
      type = ConfigDef.Type.STRING,
      defaultValue = DEFAULT_APP_NAME,
      label = "应用程序名称(单机模式)",
      description = "提交给Spark的应用程序的名称。在群集模式中忽略。",
      group = "SPARK",
      displayPosition = 20
  )
  public String appName;

  @ConfigDef(
      required = true,
      type = ConfigDef.Type.STRING,
      label = "Spark Transformer类",
      description = "实现SparkTransformer API的类。",
      group = "SPARK",
      displayPosition = 30
  )
  public String transformerClass;

  @ConfigDef(
      required = false,
      type = ConfigDef.Type.LIST,
      label = "初始化方法参数",
      description = "要传递给Transformer的初始化方法的参数。用于建立外部连接或从外部系统读取配置或预先存在的数据。",
      elDefs = VaultEL.class,
      group = "SPARK",
      displayPosition = 40
  )
  public List<String> preprocessMethodArgs = new ArrayList<>();

}

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
package com.streamsets.pipeline.stage.destination.hive;

import com.streamsets.pipeline.api.ListBeanModel;
import com.streamsets.pipeline.api.ConfigDef;
import com.streamsets.pipeline.api.ConfigGroups;
import com.streamsets.pipeline.api.GenerateResourceBundle;
import com.streamsets.pipeline.api.StageDef;
import com.streamsets.pipeline.api.Target;
import com.streamsets.pipeline.api.base.configurablestage.DTarget;

import java.util.List;
import java.util.Map;

@StageDef(
    version = 1,
    label = "Hive Streaming",
    description = "Writes data to Hive tables using the streaming API. Requires ORC storage format.",
    icon = "hive.png",
    privateClassLoader = true,
    onlineHelpRefUrl ="index.html?contextID=task_cx3_lhh_ht"
)
@ConfigGroups(value = Groups.class)
@GenerateResourceBundle
public class HiveDTarget extends DTarget {

  @ConfigDef(
      required = true,
      label = "Hive Metastore Thrift URL",
      type = ConfigDef.Type.STRING,
      description = "Hive Metastore Thrift URL in the form: thrift://<host>:<port>",
      displayPosition = 10,
      group = "HIVE"
  )
  public String hiveUrl;

  @ConfigDef(
      required = true,
      label = "数据库",
      type = ConfigDef.Type.STRING,
      defaultValue = "default",
      description = "目标表的Hive数据库。有时也称为\\“数据库\\”。",
      displayPosition = 20,
      group = "HIVE"
  )
  public String schema;

  @ConfigDef(
      required = true,
      label = "表",
      type = ConfigDef.Type.STRING,
      displayPosition = 30,
      group = "HIVE"
  )
  public String table;

  @ConfigDef(
      required = false,
      type = ConfigDef.Type.STRING,
      defaultValue = "/etc/hive/conf",
      label = "Hive配置目录",
      description = "加载core-site.xml和Hive-site.xml文件,以配置Hive的绝对路径或SDC资源目录下的目录。",
      displayPosition = 40,
      group = "HIVE"
  )
  public String hiveConfDir;

  @ConfigDef(
      required = true,
      type = ConfigDef.Type.MODEL,
      defaultValue="",
      label = "字段到列映射",
      description = "当输入字段名和列名不匹配时，用于指定其他字段映射。",
      displayPosition = 50,
      group = "HIVE"
  )
  @ListBeanModel
  public List<FieldMappingConfig> columnMappings;

  @ConfigDef(
      required = true,
      label = "创建分区",
      type = ConfigDef.Type.BOOLEAN,
      defaultValue = "true",
      description = "如果不存在分区，则自动创建分区。",
      displayPosition = 60,
      group = "HIVE"
  )
  public boolean autoCreatePartitions;

  @ConfigDef(
      required = true,
      label = "事务每批数",
      type = ConfigDef.Type.NUMBER,
      description = "每个分区每批处理请求的事务数。",
      defaultValue = "1000",
      min = 2,
      displayPosition = 70,
      group = "ADVANCED"
  )
  public int txnBatchSize;

  @ConfigDef(
      required = true,
      label = "最大记录数(KB)",
      type = ConfigDef.Type.NUMBER,
      description = "超过最大记录数将发送到错误类中。",
      defaultValue = "128",
      min = 1,
      displayPosition = 80,
      group = "ADVANCED"
  )
  public int bufferLimitKb;

  @ConfigDef(
      required = false,
      type = ConfigDef.Type.MAP,
      label = "Hive配置",
      description = "其他配置属性。这里的值覆盖从配置文件加载的值。",
      displayPosition = 90,
      group = "ADVANCED"
  )
  public Map<String, String> additionalHiveProperties;

  @Override
  protected Target createTarget() {
    return new HiveTarget(
        hiveUrl,
        schema,
        table,
        hiveConfDir,
        columnMappings,
        autoCreatePartitions,
        txnBatchSize,
        bufferLimitKb,
        additionalHiveProperties
    );
  }
}

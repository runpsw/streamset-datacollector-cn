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
package com.streamsets.pipeline.stage.processor.hive;

import com.streamsets.pipeline.api.ConfigDefBean;
import com.streamsets.pipeline.api.ConfigGroups;
import com.streamsets.pipeline.api.Processor;
import com.streamsets.pipeline.api.StageDef;
import com.streamsets.pipeline.api.ValueChooserModel;
import com.streamsets.pipeline.api.base.configurablestage.DProcessor;
import com.streamsets.pipeline.config.TimeZoneChooserValues;
import com.streamsets.pipeline.api.ListBeanModel;
import com.streamsets.pipeline.api.ConfigDef;
import com.streamsets.pipeline.lib.el.RecordEL;
import com.streamsets.pipeline.lib.el.TimeEL;
import com.streamsets.pipeline.lib.el.TimeNowEL;
import com.streamsets.pipeline.stage.lib.hive.FieldPathEL;
import com.streamsets.pipeline.stage.lib.hive.HiveConfigBean;

import java.time.ZoneId;
import java.util.List;
import java.util.Map;
import java.util.TimeZone;

@StageDef(
    version = 2,
    label="Hive元数据",
    description = "为HDFS生成Hive元数据并写入信息",
    icon="metadata.png",
    outputStreams = HiveMetadataOutputStreams.class,
    privateClassLoader = true,
    onlineHelpRefUrl ="index.html?contextID=task_hpg_pft_zv",
    upgrader = HiveMetadataProcessorUpgrader.class
)

@ConfigGroups(Groups.class)
public class HiveMetadataDProcessor extends DProcessor {

  @ConfigDefBean
  public HiveConfigBean hiveConfigBean;

  @ConfigDef(
      required = false,
      label = "数据库表达式",
      type = ConfigDef.Type.STRING,
      defaultValue = "${record:attribute('database')}",
      description = "使用表达式从记录中获取数据库名称。如果未设置，将应用\"default\"",
      displayPosition = 10,
      group = "TABLE", 
      evaluation = ConfigDef.Evaluation.EXPLICIT,
      elDefs = {RecordEL.class}
  )
  public String dbNameEL;

  @ConfigDef(
      required = true,
      label = "表名",
      type = ConfigDef.Type.STRING,
      defaultValue = "${record:attribute('table_name')}",
      description = "使用表达式从记录中获取表名。注意，Hive在创建表时将名称更改为小写。",
      displayPosition = 20,
      evaluation = ConfigDef.Evaluation.EXPLICIT,
      elDefs = {RecordEL.class},
      group = "TABLE"
  )
  public String tableNameEL;

  @ConfigDef(
      required = true,
      label = "分区配置",
      type = ConfigDef.Type.MODEL,
      defaultValue = "",
      description = "分区信息，通常用于CREATE查询中的Partition BY子句。",
      displayPosition = 30,
      evaluation = ConfigDef.Evaluation.EXPLICIT,
      elDefs = {RecordEL.class},
      group = "TABLE"
  )
  @ListBeanModel
  public List<PartitionConfig> partitionList;

  @ConfigDef(
      required = true,
      label = "外部表",
      type = ConfigDef.Type.BOOLEAN,
      defaultValue = "false",
      description = "数据是否存储在外部表中?如果勾选此项，Hive将不会使用默认位置。否则，hive将使用hive-site.xml中hive.metastore.warehouse.dir的默认位置。",
      displayPosition = 40,
      group = "TABLE"
  )
  public boolean externalTable;

  /* Only when internal checkbox is set to NO */
  @ConfigDef(
      required = false,
      label = "表路径模板",
      type = ConfigDef.Type.STRING,
      defaultValue = "/user/hive/warehouse/${record:attribute('database')}.db/${record:attribute('table_name')}",
      description = "表路径表达式",
      displayPosition = 50,
      group = "TABLE",
      dependsOn = "externalTable",
      triggeredByValue = "true",
      evaluation = ConfigDef.Evaluation.EXPLICIT,
      elDefs = {RecordEL.class}
  )
  public String tablePathTemplate;

  @ConfigDef(
      required = false,
      label = "分区路径模板",
      type = ConfigDef.Type.STRING,
      defaultValue = "dt=${record:attribute('dt')}",
      description = "分区路径表达式",
      displayPosition = 60,
      group = "TABLE",
      dependsOn = "externalTable",
      triggeredByValue = "true",
      evaluation = ConfigDef.Evaluation.EXPLICIT,
      elDefs = {RecordEL.class, TimeEL.class, TimeNowEL.class}
  )
  public String partitionPathTemplate;

  @ConfigDef(
      required = false,
      label = "列注释",
      type = ConfigDef.Type.STRING,
      defaultValue = "",
      description = "计算为列注释的表达式。",
      displayPosition = 70,
      group = "TABLE",
      evaluation = ConfigDef.Evaluation.EXPLICIT,
      elDefs = {RecordEL.class, TimeEL.class, TimeNowEL.class, FieldPathEL.class}
  )
  public String commentExpression;

  @ConfigDefBean
  public DecimalDefaultsConfig decimalDefaultsConfig;

  @ConfigDef(
      required = true,
      type = ConfigDef.Type.STRING,
      defaultValue = "${time:now()}",
      label = "时间的基础",
      description = "以时间为基础进行记录。输入计算为datetime的表达式。 要使用处理时间,输入 ${time:now()}. 要使用字段值, 使用 '${record:value(\"<filepath>\")}'.",
      displayPosition = 100,
      group = "ADVANCED",
      elDefs = {RecordEL.class, TimeEL.class, TimeNowEL.class},
      evaluation = ConfigDef.Evaluation.EXPLICIT
  )
  public String timeDriver;

  @ConfigDef(
      required = true,
      type = ConfigDef.Type.MODEL,
      defaultValue = "UTC",
      label = "数据时区",
      description = "用于记录的时区。",
      displayPosition = 110,
      group = "ADVANCED"
  )
  @ValueChooserModel(TimeZoneChooserValues.class)
  public String timeZoneID;

  @ConfigDef(
      required = false,
      defaultValue = "{}",
      type = ConfigDef.Type.MAP,
      label = "标题属性表达式",
      description = "要插入到元数据记录输出中的标题属性",
      displayPosition = 120,
      elDefs = {RecordEL.class, TimeEL.class, TimeNowEL.class},
      evaluation = ConfigDef.Evaluation.EXPLICIT,
      group = "ADVANCED"
  )
  @ListBeanModel
  public Map<String, String> metadataHeaderAttributeConfigs;

  @ConfigDef(
      required = true,
      type = ConfigDef.Type.MODEL,
      label = "数据格式",
      displayPosition = 10,
      group = "DATA_FORMAT"
  )
  @ValueChooserModel(HMPDataFormatChooserValues.class)
  public HMPDataFormat dataFormat = HMPDataFormat.AVRO;

  @Override
  protected Processor createProcessor() {
    return new HiveMetadataProcessor(
      dbNameEL,
      tableNameEL,
      partitionList,
      externalTable,
      tablePathTemplate,
      partitionPathTemplate,
      hiveConfigBean,
      timeDriver,
      decimalDefaultsConfig,
      TimeZone.getTimeZone(ZoneId.of(timeZoneID)),
      dataFormat,
      commentExpression,
      metadataHeaderAttributeConfigs
    );
  }

}

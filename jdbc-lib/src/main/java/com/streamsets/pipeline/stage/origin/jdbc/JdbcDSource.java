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
package com.streamsets.pipeline.stage.origin.jdbc;

import com.streamsets.pipeline.api.ConfigDef;
import com.streamsets.pipeline.api.ConfigDefBean;
import com.streamsets.pipeline.api.ConfigGroups;
import com.streamsets.pipeline.api.ExecutionMode;
import com.streamsets.pipeline.api.GenerateResourceBundle;
import com.streamsets.pipeline.api.HideConfigs;
import com.streamsets.pipeline.api.Source;
import com.streamsets.pipeline.api.StageDef;
import com.streamsets.pipeline.api.ValueChooserModel;
import com.streamsets.pipeline.api.base.configurablestage.DSource;
import com.streamsets.pipeline.lib.el.OffsetEL;
import com.streamsets.pipeline.lib.el.TimeEL;
import com.streamsets.pipeline.lib.jdbc.HikariPoolConfigBean;
import com.streamsets.pipeline.lib.jdbc.UnknownTypeAction;
import com.streamsets.pipeline.lib.jdbc.UnknownTypeActionChooserValues;

@StageDef(
    version = 10,
    label = "JDBC Query Consumer",
    description = "Reads data from a JDBC source using a query.",
    icon = "rdbms.png",
    execution = ExecutionMode.STANDALONE,
    upgrader = JdbcSourceUpgrader.class,
    recordsByRef = true,
    resetOffset = true,
    producesEvents = true,
    onlineHelpRefUrl ="index.html?contextID=task_ryz_tkr_bs"
)
@ConfigGroups(value = Groups.class)
@GenerateResourceBundle
@HideConfigs({
    "commonSourceConfigBean.allowLateTable",
    "commonSourceConfigBean.enableSchemaChanges",
    "commonSourceConfigBean.queriesPerSecond"
})
public class JdbcDSource extends DSource {

  @ConfigDef(
      required = true,
      type = ConfigDef.Type.BOOLEAN,
      defaultValue = "true",
      label = "增量模式",
      description = "禁用增量模式将始终替换初始偏移量中的值，以替代${Offset}，而不是最近的值。",
      displayPosition = 15,
      group = "JDBC"
  )
  public boolean isIncrementalMode;

  @ConfigDef(
      required = true,
      type = ConfigDef.Type.TEXT,
      mode = ConfigDef.Mode.SQL,
      label = "SQL查询",
      description =
          "SELECT <offset column>, ... FROM <table name> WHERE <offset column>  >  ${OFFSET} ORDER BY <offset column>",
      elDefs = {OffsetEL.class},
      evaluation = ConfigDef.Evaluation.IMPLICIT,
      displayPosition = 20,
      group = "JDBC"
  )
  public String query;

  @ConfigDef(
      required = false,
      type = ConfigDef.Type.STRING,
      label = "初始偏移量",
      description = "要为${offset}插入的初始值。后续查询将使用下一个偏移量查询的结果",
      displayPosition = 40,
      group = "JDBC"
  )
  public String initialOffset;

  @ConfigDef(
      required = false,
      type = ConfigDef.Type.STRING,
      label = "偏移列",
      description = "检查列以跟踪当前偏移量。",
      displayPosition = 50,
      group = "JDBC"
  )
  public String offsetColumn;

  @ConfigDef(
      required = true,
      type = ConfigDef.Type.MODEL,
      defaultValue = "LIST_MAP",
      label = "根字段类型",
      displayPosition = 130,
      group = "JDBC"
  )
  @ValueChooserModel(JdbcRecordTypeChooserValues.class)
  public JdbcRecordType jdbcRecordType;

  @ConfigDef(
      required = true,
      type = ConfigDef.Type.NUMBER,
      defaultValue = "${10 * SECONDS}",
      label = "查询时间间隔",
      displayPosition = 140,
      elDefs = {TimeEL.class},
      evaluation = ConfigDef.Evaluation.IMPLICIT,
      group = "JDBC"
  )
  public long queryInterval;

  @ConfigDefBean
  public CommonSourceConfigBean commonSourceConfigBean;

  @ConfigDef(
      required = false,
      type = ConfigDef.Type.STRING,
      label = "事务列名ID",
      description = "读取变更数据表时，标识变更所属事务的列。",
      displayPosition = 180,
      group = "CDC"
  )
  public String txnIdColumnName;

  @ConfigDef(
      required = true,
      type = ConfigDef.Type.NUMBER,
      label = "最大事务数",
      description = "如果事务超过这个数，它们将被应用于多个批。",
      defaultValue = "10000",
      displayPosition = 190,
      group = "CDC"
  )
  public int txnMaxSize;

  @ConfigDefBean()
  public HikariPoolConfigBean hikariConfigBean;

  @ConfigDef(
      required = true,
      type = ConfigDef.Type.BOOLEAN,
      label = "创建JDBC标题属性",
      description = "生成记录标题属性，这些属性提供关于源数据的附加细节，如原始数据类型或源表名。",
      defaultValue = "true",
      displayPosition = 200,
      group = "ADVANCED"
  )
  public boolean createJDBCNsHeaders = true;

  @ConfigDef(
      required = false,
      type = ConfigDef.Type.STRING,
      label = "JDBC标题前缀",
      description = "标题属性的前缀,使用如下: <prefix>.<field name>.<type of information>. 例如: jdbc.<field name>.precision and jdbc.<field name>.scale",
      defaultValue = "jdbc.",
      displayPosition = 210,
      group = "ADVANCED",
      dependsOn = "createJDBCNsHeaders",
      triggeredByValue = "true"
  )
  public String jdbcNsHeaderPrefix = "jdbc.";

  @ConfigDef(
      required = false,
      type = ConfigDef.Type.BOOLEAN,
      label = "禁用查询验证",
      description = "禁用验证查询，并且不验证查询格式，如是否存在${OFFSET}或ORDER BY子句。",
      defaultValue = "false",
      displayPosition = 220,
      group = "ADVANCED"
  )
  public boolean disableValidation = false;

  @ConfigDef(
      required = true,
      type = ConfigDef.Type.MODEL,
      label = "关于未知类型",
      description = "在结果集中检测到未知类型时应执行的操作。",
      defaultValue = "STOP_PIPELINE",
      displayPosition = 230,
      group = "ADVANCED"
  )
  @ValueChooserModel(UnknownTypeActionChooserValues.class)
  public UnknownTypeAction unknownTypeAction = UnknownTypeAction.STOP_PIPELINE;

  @Override
  protected Source createSource() {
    return new JdbcSource(
        isIncrementalMode,
        query,
        initialOffset,
        offsetColumn,
        disableValidation,
        txnIdColumnName,
        txnMaxSize,
        jdbcRecordType,
        commonSourceConfigBean,
        createJDBCNsHeaders,
        jdbcNsHeaderPrefix,
        hikariConfigBean,
        unknownTypeAction,
        queryInterval
      );
  }
}

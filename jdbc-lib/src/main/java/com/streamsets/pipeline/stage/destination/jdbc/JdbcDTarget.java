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
package com.streamsets.pipeline.stage.destination.jdbc;

import com.streamsets.pipeline.api.ConfigDef;
import com.streamsets.pipeline.api.ConfigDefBean;
import com.streamsets.pipeline.api.ConfigGroups;
import com.streamsets.pipeline.api.GenerateResourceBundle;
import com.streamsets.pipeline.api.HideConfigs;
import com.streamsets.pipeline.api.ListBeanModel;
import com.streamsets.pipeline.api.StageDef;
import com.streamsets.pipeline.api.Target;
import com.streamsets.pipeline.api.ValueChooserModel;
import com.streamsets.pipeline.api.base.configurablestage.DTarget;
import com.streamsets.pipeline.lib.el.RecordEL;
import com.streamsets.pipeline.lib.el.TimeEL;
import com.streamsets.pipeline.lib.el.TimeNowEL;
import com.streamsets.pipeline.lib.operation.ChangeLogFormat;
import com.streamsets.pipeline.lib.jdbc.HikariPoolConfigBean;
import com.streamsets.pipeline.lib.jdbc.JdbcFieldColumnParamMapping;
import com.streamsets.pipeline.lib.jdbc.JDBCOperationType;
import com.streamsets.pipeline.lib.jdbc.JDBCOperationChooserValues;
import com.streamsets.pipeline.lib.operation.ChangeLogFormatChooserValues;
import com.streamsets.pipeline.lib.operation.UnsupportedOperationAction;
import com.streamsets.pipeline.lib.operation.UnsupportedOperationActionChooserValues;

import java.util.List;

@GenerateResourceBundle
@StageDef(
    version = 6,
    label = "JDBC Producer",
    description = "Insert, update, delete data to a JDBC destination.",
    upgrader = JdbcTargetUpgrader.class,
    icon = "rdbms.png",
    onlineHelpRefUrl ="index.html?contextID=task_cx3_lhh_ht"
)
@ConfigGroups(value = Groups.class)
@HideConfigs(value = {
  "hikariConfigBean.readOnly",
  "hikariConfigBean.autoCommit",
})
public class JdbcDTarget extends DTarget {

  @ConfigDef(
      required = false,
      type = ConfigDef.Type.STRING,
      label = "数据库",
      displayPosition = 20,
      group = "JDBC"
  )
  public String schema;

  @ConfigDef(
      required = true,
      type = ConfigDef.Type.STRING,
      elDefs = {RecordEL.class, TimeEL.class, TimeNowEL.class},
      evaluation = ConfigDef.Evaluation.EXPLICIT,
      defaultValue = "${record:attribute('tableName')}",
      label = "表名",
      description = "表名应该只包含表名。数据库对象应该在连接字符串或数据库配置中定义",
      displayPosition = 30,
      group = "JDBC"
  )
  public String tableNameTemplate;

  @ConfigDef(
      required = true,
      type = ConfigDef.Type.MODEL,
      defaultValue = "",
      label = "字段到列的映射",
      description = "当输入字段名和列名不匹配时，可以选择指定其他字段映射。",
      displayPosition = 40,
      group = "JDBC"
  )
  @ListBeanModel
  public List<JdbcFieldColumnParamMapping> columnNames;

  @ConfigDef(
      required = true,
      type = ConfigDef.Type.BOOLEAN,
      label = "备注对象名称",
      description = "用于小写或混合大小写数据库、表和字段名。仅当数据库或表的名称周围带有引号时才选择。",
      displayPosition = 40,
      group = "JDBC",
      defaultValue = "false"
  )
  public boolean encloseTableName;

  @ConfigDef(
      required = false,
      type = ConfigDef.Type.MODEL,
      label = "更改日志格式",
      defaultValue = "NONE",
      description = "如果输入是更改数据捕获日志，请指定格式。",
      displayPosition = 40,
      group = "JDBC"
  )
  @ValueChooserModel(ChangeLogFormatChooserValues.class)
  public ChangeLogFormat changeLogFormat;

  @ConfigDef(
      required = true,
      type = ConfigDef.Type.MODEL,
      defaultValue = "",
      label = "默认的操作",
      description = "如果记录标题中未设置sdc.operation.type，则执行默认操作。",
      displayPosition = 50,
      group = "JDBC"
  )
  @ValueChooserModel(JDBCOperationChooserValues.class)
  public JDBCOperationType defaultOperation;

  @ConfigDef(
      required = true,
      type = ConfigDef.Type.MODEL,
      defaultValue= "DISCARD",
      label = "不支持的操作处理",
      description = "不支持的操作类型时要采取的操作",
      displayPosition = 60,
      group = "JDBC"
  )
  @ValueChooserModel(UnsupportedOperationActionChooserValues.class)
  public UnsupportedOperationAction unsupportedAction;

  @ConfigDef(
      required = true,
      type = ConfigDef.Type.BOOLEAN,
      defaultValue = "false",
      label = "使用多行操作",
      description = "选择“生成多行插入和删除”。显著提高了性能，但并不是所有数据库都支持这种语法。",
      displayPosition = 60,
      group = "JDBC"
  )
  public boolean useMultiRowInsert;

  @ConfigDef(
      required = true,
      type = ConfigDef.Type.NUMBER,
      defaultValue = "-1",
      label = "参数限制声明",
      description = "使用多行插入时，每个批处理插入语句中允许的已准备语句参数的最大数量。设置为-1禁用限制。",
      dependsOn = "useMultiRowInsert",
      triggeredByValue = "true",
      displayPosition = 60,
      group = "JDBC"
  )
  public int maxPrepStmtParameters;

  @ConfigDef(
      required = false,
      type = ConfigDef.Type.NUMBER,
      defaultValue = "-1",
      label = "每批最大缓存数(条)",
      description = "缓存中存储的已准备语句的最大数目。仅当未选中“使用多行操作”复选框时才使用缓存。使用-1可无限次输入。",
      dependsOn = "useMultiRowInsert",
      triggeredByValue = "false",
      displayPosition = 60,
      group = "JDBC"
  )
  public int maxPrepStmtCache;

  @ConfigDef(
      required = true,
      type = ConfigDef.Type.BOOLEAN,
      defaultValue = "false",
      label = "出错时回滚批处理",
      description = "是否在出错时回滚整个批。一些JDBC驱动程序提供有关单个失败行的信息，并且可以插入部分批。",
      displayPosition = 70,
      group = "JDBC"
  )
  public boolean rollbackOnError;

  @ConfigDefBean()
  public HikariPoolConfigBean hikariConfigBean;

  @Override
  protected Target createTarget() {
    return new JdbcTarget(
        schema,
        tableNameTemplate,
        columnNames, encloseTableName,
        rollbackOnError,
        useMultiRowInsert,
        maxPrepStmtParameters,
        maxPrepStmtCache,
        changeLogFormat,
        defaultOperation,
        unsupportedAction,
        hikariConfigBean
    );
  }
}

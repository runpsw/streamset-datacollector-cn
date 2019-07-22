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

import com.google.common.base.Joiner;
import com.streamsets.datacollector.security.HadoopSecurityUtil;
import com.streamsets.pipeline.api.ConfigDef;
import com.streamsets.pipeline.api.ConfigDefBean;
import com.streamsets.pipeline.api.Record;
import com.streamsets.pipeline.api.Stage;
import com.streamsets.pipeline.api.ListBeanModel;
import com.streamsets.pipeline.api.el.ELEval;
import com.streamsets.pipeline.api.el.ELEvalException;
import com.streamsets.pipeline.api.el.ELVars;
import com.streamsets.pipeline.lib.el.ELUtils;
import com.streamsets.pipeline.lib.el.RecordEL;
import com.streamsets.pipeline.lib.el.TimeEL;
import com.streamsets.pipeline.lib.el.TimeNowEL;
import com.streamsets.pipeline.stage.lib.hive.Errors;
import com.streamsets.pipeline.stage.lib.hive.HiveConfigBean;
import com.streamsets.pipeline.stage.lib.hive.HiveMetastoreUtil;
import org.apache.hadoop.fs.FileSystem;
import org.apache.hadoop.security.UserGroupInformation;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.security.PrivilegedExceptionAction;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

public class HMSTargetConfigBean {
  private static final Logger LOG = LoggerFactory.getLogger(HMSTargetConfigBean.class.getCanonicalName());
  private static final Joiner JOINER = Joiner.on(".");
  private static final String HIVE_CONFIG_BEAN = "hiveConfigBean";
  private static final String CONF_DIR = "confDir";


  @ConfigDefBean
  public HiveConfigBean hiveConfigBean;

  @ConfigDef(
      required = true,
      type = ConfigDef.Type.BOOLEAN,
      label = "AVRO存储",
      description = "如果表是Avro，则使用它在表创建SQL中包含存储为as Avro的子句。当选中时，Avro模式URL将不会包含在查询中。",
      defaultValue = "true",
      displayPosition = 30,
      group = "ADVANCED"
  )
  public boolean storedAsAvro = true;

  @ConfigDef(
      required = false,
      type = ConfigDef.Type.STRING,
      label = "Schema文件夹位置",
      description = "如果指定，数据收集器将使用HDFS位置序列化avro模式。如果路径没有以“/”(相对)开头，那么它将相对于hdfs中的表数据位置",
      displayPosition = 40,
      group = "ADVANCED",
      defaultValue = ".schemas",
      evaluation = ConfigDef.Evaluation.EXPLICIT,
      elDefs = {RecordEL.class, TimeEL.class},
      dependsOn = "storedAsAvro",
      triggeredByValue = "false"
  )
  public String schemaFolderLocation;

  //Same as in HDFS origin.
  @ConfigDef(
      required = false,
      type = ConfigDef.Type.STRING,
      label = "HDFS用户",
      description = "如果指定，数据收集器将使用指定的HDFS用户序列化HDFS中的avro模式。数据收集器用户必须在HDFS中配置为代理用户。",
      displayPosition = 50,
      group = "ADVANCED",
      dependsOn = "storedAsAvro",
      triggeredByValue = "false"
  )
  public String hdfsUser;

  @ConfigDef(
      required = false,
      type = ConfigDef.Type.MAP,
      label = "标题属性表达式",
      description = "要插入到事件输出中的标题属性",
      displayPosition = 60,
      group = "ADVANCED",
      defaultValue = "{}",
      evaluation = ConfigDef.Evaluation.EXPLICIT,
      elDefs = {RecordEL.class, TimeEL.class, TimeNowEL.class}
  )
  @ListBeanModel
  public Map<String, String> headerAttributeConfigs = new LinkedHashMap<>();

  private UserGroupInformation userUgi;
  private FileSystem fs;
  private ELEval schemaFolderELEval;
  private ELEval headerAttributeConfigsEL;
  private boolean headersEmpty;

  public FileSystem getFileSystem() {
    return fs;
  }

  public String getSchemaFolderLocation(Stage.Context context, Record metadataRecord) throws ELEvalException {
    ELVars vars = context.createELVars();
    RecordEL.setRecordInContext(vars, metadataRecord);
    return HiveMetastoreUtil.resolveEL(schemaFolderELEval, vars, schemaFolderLocation);
  }

  public boolean isHeadersEmpty() { return headersEmpty; }

  public Map<String, String> getResolvedHeaders(Stage.Context context, Record metadataRecord) throws ELEvalException {
    Map<String, String> resultMap = new LinkedHashMap();
    ELVars vars = context.createELVars();
    RecordEL.setRecordInContext(vars, metadataRecord);
    for (Map.Entry<String, String> entry : this.headerAttributeConfigs.entrySet()) {
      String attributeNameExpression = entry.getKey();
      String nameResult = HiveMetastoreUtil.resolveEL(headerAttributeConfigsEL, vars, attributeNameExpression);
      if (nameResult.isEmpty()) {
        continue;
      }
      String attributeValueExpression = entry.getValue();
      String valueResult = HiveMetastoreUtil.resolveEL(headerAttributeConfigsEL, vars, attributeValueExpression);
      resultMap.put(nameResult, valueResult);
    }
    return resultMap;
  }

  public UserGroupInformation getHDFSUgi() {
    return userUgi;
  }

  public void destroy() {
    hiveConfigBean.destroy();
    if (storedAsAvro) {
      return;
    }
    try {
      getHDFSUgi().doAs((PrivilegedExceptionAction<Void>) () -> {
        if (fs != null) {
          fs.close();
        }
        return null;
      });
    } catch (Exception e) {
      LOG.warn("Error when closing hdfs file system:", e);
    }
  }

  public void init(final Stage.Context context, final String prefix, final List<Stage.ConfigIssue> issues) {
    hiveConfigBean.init(context, JOINER.join(prefix, HIVE_CONFIG_BEAN), issues);
    userUgi = HadoopSecurityUtil.getProxyUser(
      hdfsUser,
      context,
      hiveConfigBean.getUgi(),
      issues,
      Groups.HIVE.name(),
      JOINER.join(prefix, HIVE_CONFIG_BEAN, "hdfsUser")
    );
    headerAttributeConfigsEL = context.createELEval("headerAttributeConfigs");
    if(!headerAttributeConfigs.isEmpty()) {
      headersEmpty = false;
      for (Map.Entry<String, String> entry : headerAttributeConfigs.entrySet()) {
        String attributeNameExpression = entry.getKey();
        String attributeValueExpression = entry.getValue();

        ELUtils.validateExpression(attributeNameExpression,
                context,
                Groups.ADVANCED.getLabel(),
                "headerAttributeConfigs",
                Errors.HIVE_39, issues);
        ELUtils.validateExpression(attributeValueExpression,
                context,
                Groups.ADVANCED.getLabel(),
                "headerAttributeConfigs",
                Errors.HIVE_39, issues);
      }
    } else {
      headersEmpty = true;
    }

    schemaFolderELEval = context.createELEval("schemaFolderLocation");
    if (storedAsAvro) {
      return;
    }
    //use ugi.
    try {
      fs = getHDFSUgi().doAs((PrivilegedExceptionAction<FileSystem>) () -> FileSystem.get(hiveConfigBean.getConfiguration()));
    } catch (Exception e) {
      LOG.error("Error accessing HDFS", e);
      issues.add(
          context.createConfigIssue(
              Groups.HIVE.name(),
              JOINER.join(prefix, HIVE_CONFIG_BEAN, CONF_DIR),
              Errors.HIVE_01,
              e.getMessage()
          )
      );
    }
  }
}

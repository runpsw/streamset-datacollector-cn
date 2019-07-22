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

import com.streamsets.pipeline.api.ConfigDef;
import com.streamsets.pipeline.lib.el.RecordEL;
import com.streamsets.pipeline.stage.lib.hive.FieldPathEL;

public class DecimalDefaultsConfig {
  @ConfigDef(
      required = true,
      type = ConfigDef.Type.STRING,
      defaultValue = "${record:attribute(str:concat(str:concat('jdbc.', field:field()), '.scale'))}",
      label = "十进制表达式",
      description = "该表达式定义十进制字段的比例。对JDBC使用者源生成的数据使用默认值。当与JDBC使用者一起使用时，请确保源创建了JDBC名称空间标题属性。",
      displayPosition = 80,
      group = "TABLE",
      elDefs = {RecordEL.class, FieldPathEL.class},
      evaluation = ConfigDef.Evaluation.EXPLICIT
  )
  public String scaleExpression;

  @ConfigDef(
      required = true,
      type = ConfigDef.Type.STRING,
      defaultValue = "${record:attribute(str:concat(str:concat('jdbc.', field:field()), '.precision'))}",
      label = "十进制精确表达式",
      description = "该表达式定义十进制字段精度。对JDBC使用者源生成的数据使用默认值。当与JDBC使用者一起使用时，请确保源创建了JDBC名称空间标题属性。",
      displayPosition = 90,
      group = "TABLE",
      elDefs = {RecordEL.class, FieldPathEL.class},
      evaluation = ConfigDef.Evaluation.EXPLICIT
  )
  public String precisionExpression;
}

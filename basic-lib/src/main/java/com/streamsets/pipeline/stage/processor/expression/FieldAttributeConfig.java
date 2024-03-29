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
package com.streamsets.pipeline.stage.processor.expression;

import com.streamsets.pipeline.api.ConfigDef;
import com.streamsets.pipeline.api.FieldSelectorModel;
import com.streamsets.pipeline.lib.el.RecordEL;

public class FieldAttributeConfig {

  @ConfigDef(
      required = true,
      type = ConfigDef.Type.MODEL,
      defaultValue = "/",
      label = "字段",
      description = "将接收属性值的现有字段。",
      displayPosition = 10,
      group = "EXPRESSIONS"
  )
  @FieldSelectorModel(singleValued = true)
  public String fieldToSet;

  @ConfigDef(
      required = true,
      type = ConfigDef.Type.STRING,
      label = "属性名称",
      description = "使用现有字段属性或输入新属性。使用现有属性覆盖原始值。",
      displayPosition = 30,
      group = "EXPRESSIONS"
  )
  public String attributeToSet;

  @ConfigDef(
      required = true,
      type = ConfigDef.Type.STRING,
      label = "标题属性表达式",
      description = "使用表达式语言修改或设置字段中的新属性。",
      displayPosition = 50,
      elDefs = {RecordEL.class, ELSupport.class},
      evaluation = ConfigDef.Evaluation.EXPLICIT,
      group = "EXPRESSIONS"
  )
  public String fieldAttributeExpression;

}

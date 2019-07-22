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

import com.streamsets.pipeline.api.ConfigDef;
import com.streamsets.pipeline.api.FieldSelectorModel;

public class FieldMappingConfig {

  /**
   * Constructor used for unit testing purposes
   * @param field
   * @param columnName
   */
  public FieldMappingConfig(final String field, final String columnName) {
    this.field = field;
    this.columnName = columnName;
  }

  /**
   * Parameter-less constructor required.
   */
  public FieldMappingConfig() {}

  @ConfigDef(
      required = true,
      type = ConfigDef.Type.MODEL,
      defaultValue="",
      label = "SDC字段",
      description = "要输出的传入记录中的字段。",
      displayPosition = 10
  )
  @FieldSelectorModel(singleValued = true)
  public String field;

  @ConfigDef(
      required = true,
      type = ConfigDef.Type.STRING,
      defaultValue="",
      label = "列名",
      description="要写入此字段的列名。",
      displayPosition = 20
  )
  public String columnName;
}

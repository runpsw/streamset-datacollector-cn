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
package com.streamsets.pipeline.lib.jdbc;

import com.streamsets.pipeline.api.ConfigDef;

public class JdbcFieldColumnParamMapping extends JdbcFieldColumnMapping {

  /**
   * Constructor used for unit testing purposes
   * @param field
   * @param columnName
   */
  public JdbcFieldColumnParamMapping(final String field, final String columnName) {
    this(field, columnName, "?");
  }

  /**
   * Constructor used for unit testing purposes
   * @param field
   * @param columnName
   * @param paramValue
   */
  public JdbcFieldColumnParamMapping(final String field, final String columnName, final String paramValue) {
    this.field = field;
    this.columnName = columnName;
    this.paramValue = paramValue;
  }

  /**
   * Parameter-less constructor required.
   */
  public JdbcFieldColumnParamMapping() {}

  @ConfigDef(
      required = true,
      type = ConfigDef.Type.STRING,
      defaultValue = "?",
      label = "参数化值",
      description = "要在JDBC insert语句中使用的参数化值。必须包括一个?。",
      displayPosition = 30
      // TODO: Have this depend on a checkbox in the JdbcDTarget. Blocked by SDC-1704
  )
  public String paramValue;
}

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
package com.streamsets.pipeline.lib.salesforce;

import com.streamsets.pipeline.api.ConfigDef;
import com.streamsets.pipeline.api.FieldSelectorModel;

public class ForceFieldMapping {

  /**
   * Constructor used for unit testing purposes
   * @param sdcField
   * @param salesforceField
   */
  public ForceFieldMapping(final String sdcField, final String salesforceField) {
    this.sdcField = sdcField;
    this.salesforceField = salesforceField;
  }

  /**
   * Parameter-less constructor required.
   */
  public ForceFieldMapping() {}

  @ConfigDef(
      required = true,
      type = ConfigDef.Type.MODEL,
      defaultValue = "",
      label = "SDC Field",
      description = "The field in the incoming record.",
      displayPosition = 10
  )
  @FieldSelectorModel(singleValued = true)
  public String sdcField;

  @ConfigDef(
      required = true,
      type = ConfigDef.Type.STRING,
      defaultValue="",
      label = "Salesforce Field",
      description = "The Salesforce field name.",
      displayPosition = 20
  )
  public String salesforceField;
}

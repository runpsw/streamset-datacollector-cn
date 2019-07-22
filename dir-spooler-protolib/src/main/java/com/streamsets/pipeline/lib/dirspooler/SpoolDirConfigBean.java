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
package com.streamsets.pipeline.lib.dirspooler;

import com.streamsets.pipeline.api.ConfigDef;
import com.streamsets.pipeline.api.ConfigDefBean;
import com.streamsets.pipeline.api.ValueChooserModel;
import com.streamsets.pipeline.config.DataFormat;
import com.streamsets.pipeline.config.PostProcessingOptions;
import com.streamsets.pipeline.config.PostProcessingOptionsChooserValues;
import com.streamsets.pipeline.stage.origin.lib.DataParserFormatConfig;

public class SpoolDirConfigBean {

  @ConfigDefBean(groups = "FILES")
  public DataParserFormatConfig dataFormatConfig = new DataParserFormatConfig();

  @ConfigDef(
      required = true,
      type = ConfigDef.Type.MODEL,
      label = "数据格式",
      description = "文件中数据的格式",
      displayPosition = 1,
      group = "DATA_FORMAT"
  )
  @ValueChooserModel(DataFormatChooserValues.class)
  public DataFormat dataFormat;

  @ConfigDef(
      required = true,
      type = ConfigDef.Type.STRING,
      label = "文件目录",
      description = "使用本地目录",
      displayPosition = 10,
      group = "FILES"
  )
  public String spoolDir;

  @ConfigDef(
      required = true,
      type = ConfigDef.Type.NUMBER,
      defaultValue = "1",
      label = "线程数",
      description = "读取数据的并行线程数",
      displayPosition = 11,
      group = "FILES",
      min = 1
  )
  public int numberOfThreads = 1;

  @ConfigDef(
      required = true,
      type = ConfigDef.Type.MODEL,
      label = "文件名模式类别",
      description = "选择指定的文件名模式是使用glob模式语法还是regex语法",
      defaultValue = "GLOB",
      displayPosition = 15,
      group = "FILES"
  )
  @ValueChooserModel(PathMatcherModeChooserValues.class)
  public PathMatcherMode pathMatcherMode;

  @ConfigDef(
      required = true,
      type = ConfigDef.Type.STRING,
      label = "文件名模式",
      description = "定义目录中文件名模式的全局表达式或正则表达式",
      displayPosition = 20,
      group = "FILES"
  )
  public String filePattern;

  @ConfigDef(
      required = false,
      type = ConfigDef.Type.MODEL,
      defaultValue = "LEXICOGRAPHICAL",
      label = "读取指令",
      description = "根据上次修改的时间戳或按字典顺序升序的文件名读取文件,使用时间戳排序时，具有相同时间戳的文件将根据文件名排序",
      group = "FILES"
  )
  @ValueChooserModel(FileOrderingChooseValues.class)
  public FileOrdering useLastModified = FileOrdering.LEXICOGRAPHICAL;

  @ConfigDef(
      required = true,
      type = ConfigDef.Type.BOOLEAN,
      defaultValue = "false",
      label = "处理子目录",
      description = "处理文件目录子目录中的文件。只处理与文件名模式匹配的文件名",
      displayPosition = 40,
      dependsOn = "useLastModified",
      triggeredByValue = "TIMESTAMP",
      group = "FILES"
  )
  public boolean processSubdirectories;

  @ConfigDef(
      required = true,
      type = ConfigDef.Type.BOOLEAN,
      label = "允许迟到的目录",
      description = "开启从迟到到达的目录读取。开启后, 源站不验证配置的路径",
      displayPosition = 50,
      group = "FILES",
      defaultValue = "false"
  )
  public boolean allowLateDirectory = false;

  @ConfigDef(
      required = true,
      type = ConfigDef.Type.NUMBER,
      label = "缓冲上限(KB)",
      defaultValue = "128",
      description = "低级别读卡器缓冲区限制，以避免内存不足错误",
      displayPosition = 70,
      group = "FILES",
      min = 1,
      max = Integer.MAX_VALUE
  )
  public int overrunLimit;

  @ConfigDef(
      required = true,
      type = ConfigDef.Type.NUMBER,
      label = "批量大小(recs)",
      defaultValue = "1000",
      description = "每批最大记录数",
      displayPosition = 43,
      group = "FILES",
      min = 0,
      max = Integer.MAX_VALUE
  )
  public int batchSize;

  @ConfigDef(
      required = false,
      type = ConfigDef.Type.NUMBER,
      defaultValue = "60",
      label = "每批等待时间(秒)",
      description = "发送空批前等待新文件的最长时间",
      displayPosition = 48,
      group = "FILES",
      min = 1
  )
  public long poolingTimeoutSecs;

  @ConfigDef(
      required = true,
      type = ConfigDef.Type.NUMBER,
      defaultValue = "1000",
      label = "最大文件软限制",
      description = "一次添加到处理队列的最大文件数。这是软限制，可以暂时超过。",
      displayPosition = 60,
      group = "FILES",
      min = 1,
      max = Integer.MAX_VALUE
  )
  public int maxSpoolFiles;

  @ConfigDef(
      required = true,
      type = ConfigDef.Type.NUMBER,
      defaultValue = "5",
      label = "假脱机周期(秒)",
      description = "缓冲文件的最长时间周期",
      displayPosition = 61,
      group = "FILES",
      min = 1,
      max = Integer.MAX_VALUE
  )
  public long spoolingPeriod = 5;

  @ConfigDef(
      required = false,
      type = ConfigDef.Type.STRING,
      defaultValue = "",
      label = "要处理的第一个文件",
      description = "配置后，数据收集器不会处理早期（自然升序）的文件名",
      displayPosition = 50,
      group = "FILES"
  )
  public String initialFileToProcess;

  @ConfigDef(
      required = false,
      type = ConfigDef.Type.STRING,
      label = "错误目录",
      description = "无法完全处理的文件目录",
      displayPosition = 100,
      group = "POST_PROCESSING"
  )
  public String errorArchiveDir;

  @ConfigDef(
      required = true,
      type = ConfigDef.Type.MODEL,
      defaultValue = "NONE",
      label = "文件迟到处理",
      description = "处理文件后要采取的操作",
      displayPosition = 110,
      group = "POST_PROCESSING"
  )
  @ValueChooserModel(PostProcessingOptionsChooserValues.class)
  public PostProcessingOptions postProcessing;

  @ConfigDef(
      required = false,
      type = ConfigDef.Type.STRING,
      label = "存档目录",
      description = "文件处理后存档的目录",
      displayPosition = 200,
      group = "POST_PROCESSING",
      dependsOn = "postProcessing",
      triggeredByValue = "ARCHIVE"
  )
  public String archiveDir;

  @ConfigDef(
      required = false,
      type = ConfigDef.Type.NUMBER,
      defaultValue = "0",
      label = "存档保留时间(分)",
      description = "删除前应保留存档文件的时间，值为0表示永久",
      displayPosition = 210,
      group = "POST_PROCESSING",
      dependsOn = "postProcessing",
      triggeredByValue = "ARCHIVE",
      min = 0
  )
  public long retentionTimeMins;
}

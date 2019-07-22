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
package com.streamsets.datacollector.runner.preview;

import com.streamsets.datacollector.runner.SourceOffsetTracker;
import com.streamsets.pipeline.api.Source;

import java.util.HashMap;
import java.util.Map;

public class PreviewSourceOffsetTracker implements SourceOffsetTracker {
  private Map<String, String> offsets;
  private boolean finished;

  public PreviewSourceOffsetTracker(Map<String, String> offset) {
    this.offsets = new HashMap<>(offset);
    finished = false;
  }

  @Override
  public boolean isFinished() {
    return finished;
  }

  @Override
  public void commitOffset(String entity, String newOffset) {
    if(entity == null) {
      return;
    }

    if(Source.POLL_SOURCE_OFFSET_KEY.equals(entity)) {
      finished = (newOffset == null);
    }

    if(newOffset == null) {
      offsets.remove(entity);
    } else {
      offsets.put(entity, newOffset);
    }
  }

  @Override
  public Map<String, String> getOffsets() {
    return offsets;
  }

  @Override
  public long getLastBatchTime() {
    return 0;
  }

}

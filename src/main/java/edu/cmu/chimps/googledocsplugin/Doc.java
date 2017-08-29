/*
  Copyright 2017 CHIMPS Lab, Carnegie Mellon University
  Licensed under the Apache License, Version 2.0 (the "License");
  you may not use this file except in compliance with the License.
  You may obtain a copy of the License at
  http://www.apache.org/licenses/LICENSE-2.0
  Unless required by applicable law or agreed to in writing, software
  distributed under the License is distributed on an "AS IS" BASIS,
  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
  See the License for the specific language governing permissions and
  limitations under the License.
 */

package edu.cmu.chimps.googledocsplugin;


public class Doc {
    private String mDocName;
    private String mDocUrl;
    private Long mCreatedTime;

    public Long getCreatedTime() {
        return mCreatedTime;
    }

    public void setCreatedTime(Long createdTime) {
        mCreatedTime = createdTime;
    }

    public String getDocName() {
        return mDocName;
    }

    public void setDocName(String docName) {
            mDocName = docName;
        }

    public String getDocUrl() {
            return mDocUrl;
        }


    public void setDocUrl(String docUrl) {
            mDocUrl = docUrl;
        }
}

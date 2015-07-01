/*
 * Copyright 2009-2015 DigitalGlobe, Inc.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and limitations under the License.
 */

package org.mrgeo.resources.job;

import javax.xml.bind.annotation.XmlRootElement;
import java.util.ArrayList;
import java.util.List;

@XmlRootElement
public class JobsListResponse
{

  private List<JobInfoResponse> _jobInfos = new ArrayList<JobInfoResponse>();
  public List<JobInfoResponse> getJobInfo() { return _jobInfos; }
  public void setJobInfo(List<JobInfoResponse> jobInfos) { this._jobInfos = jobInfos; }
  
  private String _nextUrl;
  public String getNextURL() { return _nextUrl; }
  public void setNextURL(String nextUrl)
  {
    this._nextUrl = nextUrl;
  }

  private String _prevUrl;
  public String getPrevURL() { return _prevUrl; }
  public void setPrevURL(String prevUrl)
  {
    this._prevUrl = prevUrl;
  }

}

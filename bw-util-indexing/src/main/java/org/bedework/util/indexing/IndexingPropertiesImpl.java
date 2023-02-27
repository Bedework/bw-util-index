/* ********************************************************************
    Licensed to Jasig under one or more contributor license
    agreements. See the NOTICE file distributed with this work
    for additional information regarding copyright ownership.
    Jasig licenses this file to you under the Apache License,
    Version 2.0 (the "License"); you may not use this file
    except in compliance with the License. You may obtain a
    copy of the License at:

    http://www.apache.org/licenses/LICENSE-2.0

    Unless required by applicable law or agreed to in writing,
    software distributed under the License is distributed on
    an "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
    KIND, either express or implied. See the License for the
    specific language governing permissions and limitations
    under the License.
*/
package org.bedework.util.indexing;

import org.bedework.util.config.ConfInfo;
import org.bedework.util.config.ConfigBase;
import org.bedework.util.misc.ToString;

/** These are the system properties that the server needs to know about, either
 * because it needs to apply these limits or just to report them to clients.
 *
 * @author douglm
 *
 */
@ConfInfo(elementName = "index-properties",
          type = "org.bedework.util.indexing.IndexProperties")
public class IndexingPropertiesImpl
        extends ConfigBase<IndexingPropertiesImpl>
        implements IndexingProperties {
  private String indexerURL;

  private String indexerToken;

  private String indexerUser;

  private String indexerPw;

  private String clusterName;

  private String nodeName;

  private String keyStore;

  private String keyStorePw;

  @Override
  public void setIndexerURL(final String val) {
    indexerURL = val;
  }

  @Override
  public String getIndexerURL() {
    return indexerURL;
  }

  @Override
  public void setIndexerToken(final String val) {
    indexerToken = val;
  }

  @Override
  public String getIndexerToken() {
    return indexerToken;
  }

  @Override
  public void setIndexerUser(final String val) {
    indexerUser = val;
  }

  @Override
  public String getIndexerUser() {
    return indexerUser;
  }

  @Override
  public void setIndexerPw(final String val) {
    indexerPw = val;
  }

  @Override
  public String getIndexerPw() {
    return indexerPw;
  }

  @Override
  public void setClusterName(final String val) {
    clusterName = val;
  }

  @Override
  public String getClusterName() {
    return clusterName;
  }

  @Override
  public void setNodeName(final String val) {
    nodeName = val;
  }

  @Override
  public String getNodeName() {
    return nodeName;
  }

  @Override
  public void setKeyStore(final String val) {
    keyStore = val;
  }

  @Override
  public String getKeyStore() {
    return keyStore;
  }

  @Override
  public void setKeyStorePw(final String val) {
    keyStorePw = val;
  }

  @Override
  public String getKeyStorePw() {
    return keyStorePw;
  }

  /* ====================================================================
   *                   Object methods
   * ==================================================================== */

  @Override
  public String toString() {
    final ToString ts = new ToString(this);

    ts.append("indexerURL", getIndexerURL());
    ts.append("clusterName", getClusterName());
    ts.append("nodeName", getNodeName());
    ts.append("keyStore", getKeyStore());

    return ts.toString();
  }
}

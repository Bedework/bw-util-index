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
package org.bedework.util.opensearch;

import org.bedework.util.config.ConfInfo;
import org.bedework.util.jmx.MBeanInfo;

/** These are the properties that the indexer needs to know about.
 *
 * <p>Annotated to allow use by mbeans
 *
 * @author douglm
 *
 */
@ConfInfo(elementName = "index-properties")
public interface IndexingProperties {
  /**
   *
   * @param val the indexer url
   */
  void setIndexerURL(String val);

  /** Get the indexer url
   *
   * @return url of server
   */
  @MBeanInfo("Non-embedded indexer url")
  String getIndexerURL();

  /**
   *
   * @param val the indexer service token
   */
  void setIndexerToken(String val);

  /** Get the indexer service token
   *
   * @return service token for server
   */
  @MBeanInfo("Indexer service token")
  String getIndexerToken();

  /**
   *
   * @param val the indexer user account
   */
  void setIndexerUser(String val);

  /** Get the indexer user account
   *
   * @return user account for server
   */
  @MBeanInfo("Indexer user account")
  String getIndexerUser();

  /**
   *
   * @param val the indexer user password
   */
  void setIndexerPw(String val);

  /** Get the indexer user password
   *
   * @return user password for server
   */
  @MBeanInfo("Indexer user password")
  String getIndexerPw();

  /**
   *
   * @param val the cluster name
   */
  void setClusterName(String val);

  /** Get the cluster name
   *
   * @return name
   */
  @MBeanInfo("cluster name")
  String getClusterName();

  /** 
   *
   * @param val the node name
   */
  void setNodeName(String val);

  /** Get the node name
   *
   * @return name
   */
  @MBeanInfo("node name")
  String getNodeName();

  /** 
   *
   * @param val  the path to a keystore for https root certs
   */
  void setKeyStore(String val);

  /**
   *
   * @return path to keystore
   */
  @MBeanInfo("path to keystore")
  String getKeyStore();

  /**
   *
   * @param val  the keystore password
   */
  void setKeyStorePw(String val);

  /**
   *
   * @return keystore password
   */
  @MBeanInfo("keystore password or null")
  String getKeyStorePw();
}

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

import org.bedework.util.indexing.IndexingPropertiesImpl;
import org.bedework.util.jmx.ConfBase;
import org.bedework.util.logging.BwLogger;
import org.bedework.util.misc.Util;

import java.util.List;
import java.util.Set;

/**
 * @author douglm
 *
 */
public class IndexCtl extends ConfBase<IndexingPropertiesImpl>
        implements IndexCtlMBean {
  /* Name of the directory holding the config data */
  private static final String confDirName = "opensearch";

  public void runInit() {
    /* List the indexes in use - ensures we have an indexer early on */

    info(" * Current indexes: ");
    Set<IndexInfo> is = null;

    try {
      is = getSearchClient().getIndexInfo();
    } catch (final Throwable t) {
      info(" * Exception getting index info:");
      info(" * " + t.getLocalizedMessage());
    }

    info(listIndexes(is));
  }

  private final static String nm = "indexctl";

  private OschUtil oschUtil;
  private SearchClient sch;

  /**
   */
  public IndexCtl() {
    super(getServiceName(nm), confDirName, nm);
  }

  /**
   * @param name of service
   * @return object name value for the mbean with this name
   */
  public static String getServiceName(final String name) {
    return IndexCtlMBean.serviceName;
  }

  @Override
  public void setIndexerURL(final String val) {
    getConfig().setIndexerURL(val);
  }

  @Override
  public String getIndexerURL() {
    return getConfig().getIndexerURL();
  }

  @Override
  public void setIndexerToken(final String val) {
    getConfig().setIndexerToken(val);
  }

  @Override
  public String getIndexerToken() {
    return getConfig().getIndexerToken();
  }

  @Override
  public void setIndexerUser(final String val) {
    getConfig().setIndexerUser(val);
  }

  @Override
  public String getIndexerUser() {
    return getConfig().getIndexerUser();
  }

  @Override
  public void setIndexerPw(final String val) {
    getConfig().setIndexerPw(val);
  }

  @Override
  public String getIndexerPw() {
    return getConfig().getIndexerPw();
  }

  @Override
  public void setClusterName(final String val) {
    getConfig().setClusterName(val);
  }

  @Override
  public String getClusterName() {
    return getConfig().getClusterName();
  }

  @Override
  public void setNodeName(final String val) {
    getConfig().setNodeName(val);
  }

  @Override
  public String getNodeName() {
    return getConfig().getNodeName();
  }

  @Override
  public void setKeyStore(final String val) {
    getConfig().setKeyStore(val);
  }

  @Override
  public String getKeyStore() {
    return getConfig().getKeyStore();
  }

  @Override
  public void setKeyStorePw(final String val) {
    getConfig().setKeyStorePw(val);
  }

  @Override
  public String getKeyStorePw() {
    return getConfig().getKeyStorePw();
  }

  @Override
  public String listIndexes() {
    try {
      return listIndexes(getSearchClient().getIndexInfo());
    } catch (final Throwable t) {
      return t.getLocalizedMessage();
    }
  }

  /* ========================================================================
   * Operations
   * ======================================================================== */

  @Override
  public synchronized void start() {
    runInit();
  }

  @Override
  public synchronized void stop() {
  }

  @Override
  public String loadConfig() {
    return loadConfig(IndexingPropertiesImpl.class);
  }

  /* ========================================================================
   * Private methods
   * ======================================================================== */

  private String listIndexes(final Set<IndexInfo> is) {
    if (Util.isEmpty(is)) {
      return "No indexes found";
    }

    final StringBuilder res = new StringBuilder("Indexes");

    res.append("------------------------\n");

    for (final IndexInfo ii: is) {
      res.append(ii.getIndexName());

      if (!Util.isEmpty(ii.getAliases())) {
        String delim = "<----";

        for (final var a: ii.getAliases()) {
          res.append(delim);
          res.append(a);
          delim = ", ";
        }
      }

      res.append("\n");
    }

    return res.toString();
  }

  private void outLine(final List<String> res,
                       final String msg) {
    res.add(msg + "\n");
  }

  private OschUtil getOschUtil() {
    if (oschUtil != null) {
      return oschUtil;
    }

    oschUtil = new OschUtil(getConfig());

    return oschUtil;
  }

  private SearchClient getSearchClient() {
    if (sch != null) {
      return sch;
    }

    sch = new SearchClient(getConfig());

    return sch;
  }

  /* ====================================================================
   *                   Logged methods
   * ==================================================================== */

  private final BwLogger logger = new BwLogger();

  @Override
  public BwLogger getLogger() {
    if ((logger.getLoggedClass() == null) && (logger.getLoggedName() == null)) {
      logger.setLoggedClass(getClass());
    }

    return logger;
  }
}

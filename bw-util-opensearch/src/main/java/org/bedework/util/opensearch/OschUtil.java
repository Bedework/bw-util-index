/* ********************************************************************
    Appropriate copyright notice
*/
package org.bedework.util.opensearch;

import org.bedework.util.indexing.IndexException;
import org.bedework.util.indexing.IndexingProperties;
import org.bedework.util.jmx.ConfBase;
import org.bedework.util.jmx.MBeanUtil;
import org.bedework.util.logging.BwLogger;
import org.bedework.util.logging.Logged;

import org.opensearch.client.RestHighLevelClient;

import javax.management.ObjectName;

//import org.bedework.util.timezones.DateTimeUtil;

/**
 * User: mike Date: 3/13/16 Time: 23:28
 */
public class OschUtil implements Logged {
  private final HostPortList searchHosts;
  private static RestHighLevelClient theClient;
  private static final Object clientSyncher = new Object();

  private final IndexingProperties idxpars;
  
  public OschUtil(final IndexingProperties idxpars) {
    this.idxpars = idxpars;
    searchHosts = new HostPortList(idxpars.getIndexerURL());
  }
  
  private static EsCtlMBean esCtl;

  static class Configurator extends ConfBase {
    EsCtl esCtl;

    Configurator() {
      super("org.bedework.es:service=es",
            (String)null,
            null);
    }

    @Override
    public String loadConfig() {
      return null;
    }

    @Override
    public void start() {
      String status = null;
      
      try {
        getManagementContext().start();

        esCtl = new EsCtl();
        register(new ObjectName(esCtl.getServiceName()),
                 esCtl);

        status = esCtl.loadConfig();
      } catch (final Throwable t){
        t.printStackTrace();
        throw new RuntimeException(t);
      }

      if (!"OK".equals(status)) {
        throw new RuntimeException("Unable to load configuration. " +
                                           "Status: " + status);
      }
    }

    @Override
    public void stop() {
      try {
        getManagementContext().stop();
      } catch (final Throwable t){
        t.printStackTrace();
      }
    }
    
    boolean isRegistered() {
      try {
        return getManagementContext()
                .getMBeanServer()
                .isRegistered(new ObjectName(EsCtlMBean.serviceName));
      } catch (final Throwable t) {
        t.printStackTrace();
        throw new RuntimeException(t);
      }
    }
  }

  private static final Configurator conf = new Configurator();

  public static EsCtlMBean getEsCtl() throws IndexException {
    if (esCtl != null) {
      return esCtl;
    }

    try {
      /* See if somebody else registered the mbean
       */
      if (!conf.isRegistered()) {
        /* We need to register it */
        conf.start();
      }
      
      esCtl = (EsCtlMBean)MBeanUtil.getMBean(EsCtlMBean.class,
                                             EsCtlMBean.serviceName);
    } catch (final Throwable t) {
      throw new IndexException(t);
    }

    return esCtl;
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

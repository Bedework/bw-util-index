/* ********************************************************************
    Appropriate copyright notice
*/
package org.bedework.util.opensearch;

import java.util.ArrayList;

/**
 * User: mike Date: 1/18/22 Time: 12:14
 */
public class HostPortList extends ArrayList<HostPort>{
  public HostPortList(final String urls) {

    if (urls == null) {
      add(new HostPort("http://localhost"));
    } else {
      final String[] urlsSplit = urls.split(",");

      for (final String url : urlsSplit) {
        if ((url != null) && (url.length() > 0)) {
          add(new HostPort(url));
        }
      }
    }
  }
}

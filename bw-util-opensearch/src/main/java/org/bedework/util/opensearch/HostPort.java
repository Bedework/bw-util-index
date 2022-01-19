/* ********************************************************************
    Appropriate copyright notice
*/
package org.bedework.util.opensearch;

/**
 * User: mike Date: 1/18/22 Time: 12:15
 */
public class HostPort {
  private final String scheme;
  private final String host;
  private int port = 9200;

  HostPort(final String url) {
    int pos = url.indexOf("://");
    int start = 0;

    if (pos > 0) {
      scheme = url.substring(0, pos);
      start = pos + 3;
      pos = url.indexOf(":", start);
    } else {
      scheme = null;
    }

    if (pos < 0) {
      host = url.substring(start);
    } else {
      host = url.substring(start, pos);
      if (pos < url.length()) {
        port = Integer.parseInt(url.substring(pos + 1));
      }
    }
  }

  String getScheme() {
    return scheme;
  }

  String getHost() {
    return host;
  }

  int getPort() {
    return port;
  }
}

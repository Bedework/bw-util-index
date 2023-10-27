/* ********************************************************************
    Appropriate copyright notice
*/
package org.bedework.util.indexing;

/**
 * User: mike Date: 10/26/23 Time: 22:22
 */
public class ContextInfo {
  public static class IndexInfo {
    private final String typeName;
    private final long openContexts;
    private final long scrollTotal;
    private final long scrollCurrent;

    public IndexInfo(final String typeName,
                     final long openContexts,
                     final long scrollTotal,
                     final long scrollCurrent) {
      this.typeName = typeName;
      this.openContexts = openContexts;
      this.scrollTotal = scrollTotal;
      this.scrollCurrent = scrollCurrent;
    }

    public String getTypeName() {
      return typeName;
    }

    public long getOpenContexts() {
      return openContexts;
    }

    public long getScrollTotal() {
      return scrollTotal;
    }

    public long getScrollCurrent() {
      return scrollCurrent;
    }
  }

  private final String nodeName;
  private final IndexInfo searchIndexInfo;

  public ContextInfo(final String nodeName,
                     final IndexInfo searchIndexInfo) {
    this.nodeName = nodeName;
    this.searchIndexInfo = searchIndexInfo;
  }

  public String getNodeName() {
    return nodeName;
  }

  public IndexInfo getSearchIndexInfo() {
    return searchIndexInfo;
  }
}

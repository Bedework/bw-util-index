/* ********************************************************************
    Appropriate copyright notice
*/
package org.bedework.util.opensearch;

import org.bedework.util.http.Headers;
import org.bedework.util.indexing.ContextInfo;
import org.bedework.util.indexing.IndexException;
import org.bedework.util.indexing.IndexingProperties;
import org.bedework.util.logging.BwLogger;
import org.bedework.util.logging.Logged;
import org.bedework.util.misc.Util;
import org.bedework.base.response.GetEntityResponse;
import org.bedework.util.timezones.DateTimeUtil;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.apache.http.Header;
import org.apache.http.HttpHost;
import org.apache.http.auth.AuthScope;
import org.apache.http.auth.UsernamePasswordCredentials;
import org.apache.http.client.CredentialsProvider;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.conn.ssl.TrustSelfSignedStrategy;
import org.apache.http.impl.client.BasicCredentialsProvider;
import org.apache.http.impl.nio.client.HttpAsyncClientBuilder;
import org.apache.http.ssl.SSLContexts;
import org.opensearch.OpenSearchException;
import org.opensearch.action.admin.cluster.health.ClusterHealthRequest;
import org.opensearch.action.admin.cluster.health.ClusterHealthResponse;
import org.opensearch.action.admin.indices.alias.IndicesAliasesRequest;
import org.opensearch.action.admin.indices.alias.IndicesAliasesRequest.AliasActions;
import org.opensearch.action.admin.indices.alias.get.GetAliasesRequest;
import org.opensearch.action.admin.indices.delete.DeleteIndexRequest;
import org.opensearch.action.get.GetRequest;
import org.opensearch.action.get.GetResponse;
import org.opensearch.action.index.IndexRequest;
import org.opensearch.action.index.IndexResponse;
import org.opensearch.action.support.master.AcknowledgedResponse;
import org.opensearch.client.GetAliasesResponse;
import org.opensearch.client.Request;
import org.opensearch.client.RequestOptions;
import org.opensearch.client.RestClient;
import org.opensearch.client.RestClientBuilder;
import org.opensearch.client.RestHighLevelClient;
import org.opensearch.client.indices.CreateIndexRequest;
import org.opensearch.client.indices.CreateIndexResponse;
import org.opensearch.cluster.health.ClusterHealthStatus;
import org.opensearch.cluster.metadata.AliasMetadata;
import org.opensearch.common.xcontent.XContentType;
import org.opensearch.core.rest.RestStatus;
import org.opensearch.index.VersionType;

import java.io.File;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.TreeSet;
import java.util.stream.Stream;

import javax.net.ssl.SSLContext;

import static org.bedework.base.response.Response.Status.failed;

/**
 * User: mike Date: 3/13/16 Time: 23:28
 */
public class SearchClient implements Logged {
  private final HostPortList searchHosts;
  private static RestHighLevelClient theClient;
  private static final Object clientSyncher = new Object();

  private final IndexingProperties idxpars;

  public SearchClient(final IndexingProperties idxpars) {
    this.idxpars = idxpars;
    searchHosts = new HostPortList(idxpars.getIndexerURL());
  }

  public GetEntityResponse<RestHighLevelClient> getClient() {
    final GetEntityResponse<RestHighLevelClient> resp =
            new GetEntityResponse<>();

    try {
      resp.setEntity(getSearchClient());
    } catch (final RuntimeException cfe) {
      resp.setStatus(failed);
      resp.setMessage(cfe.getLocalizedMessage());
    }
    return resp;
  }

  private static class HttpClientConfigCallBack
          implements RestClientBuilder.HttpClientConfigCallback {
    private final CredentialsProvider credentialsProvider;
    private final SSLContext sslcontext;
    HttpClientConfigCallBack(
            final CredentialsProvider credentialsProvider,
            final SSLContext sslcontext) {
      this.credentialsProvider = credentialsProvider;
      this.sslcontext = sslcontext;
    }

    @Override
    public HttpAsyncClientBuilder customizeHttpClient(
    final HttpAsyncClientBuilder httpClientBuilder) {
      final HttpAsyncClientBuilder res = httpClientBuilder;
      if (credentialsProvider != null) {
        res.setDefaultCredentialsProvider(credentialsProvider);
      }

      if (sslcontext != null) {
        res.setSSLContext(sslcontext);
      }

      return res;
    }
  };

  public RestHighLevelClient getSearchClient() {
    if (theClient != null) {
      return theClient;
    }

    synchronized (clientSyncher) {
      if (theClient != null) {
        return theClient;
      }

      final HttpHost[] hosts = new HttpHost[searchHosts.size()];

      for (int i = 0; i < searchHosts.size(); i++) {
        final HostPort hp = searchHosts.get(i);
        hosts[i] = new HttpHost(hp.getHost(), hp.getPort(),
                                hp.getScheme());
      }

      final RestClientBuilder rcb = RestClient.builder(hosts);
      CredentialsProvider credentialsProvider = null;
      SSLContext sslcontext = null;

      if (idxpars.getIndexerToken() != null) {
        final Header[] headers = new Headers().
                add("Authorization", "Bearer " + idxpars.getIndexerToken()).
                asArray();
        rcb.setDefaultHeaders(headers);
      } else if (idxpars.getIndexerUser() != null) {
        credentialsProvider = new BasicCredentialsProvider();

        credentialsProvider.setCredentials(
                AuthScope.ANY,
                new UsernamePasswordCredentials(idxpars.getIndexerUser(),
                                                idxpars.getIndexerPw()));
      }

      if (idxpars.getKeyStore() != null) {
        // Trust own CA and all self-signed certs

        try {
          final char[] pw;

          if (idxpars.getKeyStorePw() == null) {
            pw = null;
          } else {
            pw = idxpars.getKeyStorePw().toCharArray();
          }

          sslcontext = SSLContexts
                  .custom()
                  .loadTrustMaterial(
                          new File(idxpars.getKeyStore()),
                          pw,
                          new TrustSelfSignedStrategy())
                  .build();
        } catch (final Throwable t) {
          throw new RuntimeException(t);
        }
      }

      final HttpClientConfigCallBack hcccb =
              new HttpClientConfigCallBack(credentialsProvider,
                                           sslcontext);
      rcb.setHttpClientConfigCallback(hcccb);

      theClient = new RestHighLevelClient(rcb);

      /* Ensure status is at least yellow */
      int tries = 0;
      int yellowTries = 0;

      for (;;) {
        try {
          final ClusterHealthRequest request = new ClusterHealthRequest();
          final ClusterHealthResponse chr =
                  theClient.cluster().health(request, RequestOptions.DEFAULT);

          if (chr.getStatus() == ClusterHealthStatus.GREEN) {
            break;
          }

          if (chr.getStatus() == ClusterHealthStatus.YELLOW) {
            yellowTries++;

            if (yellowTries > 10) {
              warn("Going ahead anyway on YELLOW status");
              break;
            }
          }

          tries++;

          if (tries % 5 == 0) {
            warn("Cluster status for " + chr.getClusterName() +
                         " is still " + chr.getStatus() +
                         " after " + tries + " tries");
          }

          Thread.sleep(1000);
        } catch(final InterruptedException ex) {
          throw new RuntimeException("Interrupted out of getClient");
        } catch (final Throwable t) {
          throw new RuntimeException(t);
        }
      }

      return theClient;
    }
  }

  public IndexResponse indexDoc(final EsDocInfo di,
                                final String targetIndex) throws IndexException {
    final IndexRequest req =
            new IndexRequest(targetIndex)
                    .id(di.getId())
                    .source(di.getSource())
                    .versionType(VersionType.EXTERNAL);

    if (di.getVersion() != 0) {
      req.version(di.getVersion());
    }

    if (debug()) {
      debug("Indexing to index " + targetIndex +
                    " with DocInfo " + di);
    }

    try {
      return getSearchClient().index(req, RequestOptions.DEFAULT);
    } catch (final Throwable t) {
      throw new IndexException(t);
    }
  }
  
  public GetResponse get(final String index,
                         final String docType,
                         final String id) throws IndexException {
    final GetRequest req = new GetRequest(index,
                                         id);
    try {
      final GetResponse resp = getSearchClient().get(req, RequestOptions.DEFAULT);

      if (!resp.isExists()) {
        return null;
      }

      return resp;
    } catch (final Throwable t) {
      throw new IndexException(t);
    }
  }

  /** create a new index and return its name. No alias will point to 
   * the new index.
   *
   * @param name basis for new name 
   * @param mappingPath path to mapping file.
   * @return name of created index.
   * @throws IndexException for errors
   */
  public String newIndex(final String name,
                         final String mappingPath) throws IndexException {
    try {
      final String newName = name + newIndexSuffix();

      final CreateIndexRequest req = new CreateIndexRequest(newName);

      final String mappingStr = fileToString(mappingPath);

      req.source(mappingStr, XContentType.JSON);

      final CreateIndexResponse resp =
              getSearchClient().indices().create(req, RequestOptions.DEFAULT);

      info("Index created");

      return newName;
    } catch (final OpenSearchException ese) {
      // Failed somehow
      error(ese);
      return null;
    } catch (final IndexException ie) {
      throw ie;
    } catch (final Throwable t) {
      error(t);
      throw new IndexException(t);
    }
  }
  
  public Set<IndexInfo> getIndexInfo() throws IndexException {
    final Set<IndexInfo> res = new TreeSet<>();

    try {
      final GetAliasesRequest req = new GetAliasesRequest();

      final GetAliasesResponse resp =
              getSearchClient().indices().getAlias(req, RequestOptions.DEFAULT);

      final Map<String, Set<AliasMetadata>> aliases = resp.getAliases();

      for (final String inm: aliases.keySet()) {
        final IndexInfo ii = new IndexInfo(inm);
        res.add(ii);

        final Set<AliasMetadata> amds = aliases.get(inm);

        if (amds == null) {
          continue;
        }

        for (final AliasMetadata amd: amds) {
          ii.addAlias(amd.alias());
        }
      }

      return res;
    } catch (final Throwable t) {
      throw new IndexException(t);
    }
  }

  /** Changes the given alias to refer to the supplied index name
   * 
   * @param index the index we were building 
   * @param alias to refer to this index
   * @return 0 fir ok <0 for not ok
   * @throws IndexException on fatal error
   */
  public int swapIndex(final String index,
                       final String alias) throws IndexException {
    //IndicesAliasesResponse resp = null;
    try {
      /* index is the index we were just indexing into
       */

      final GetAliasesRequest req = new GetAliasesRequest(alias);

      final GetAliasesResponse resp =
              getSearchClient().indices().getAlias(req, RequestOptions.DEFAULT);

      if (resp.status() == RestStatus.OK) {
        final Map<String, Set<AliasMetadata>> aliases =
                resp.getAliases();
        for (final String inm: aliases.keySet()) {
          final Set<AliasMetadata> amds = aliases.get(inm);

          if (amds == null) {
            continue;
          }

          for (final AliasMetadata amd: amds) {
            final IndicesAliasesRequest ireq = new IndicesAliasesRequest();
            final AliasActions removeAction =
                    new AliasActions(AliasActions.Type.REMOVE)
                            .index(inm)
                            .alias(amd.alias());
            ireq.addAliasAction(removeAction);
            final AcknowledgedResponse ack =
                    getSearchClient().indices().updateAliases(ireq, RequestOptions.DEFAULT);
        }
      }

      final IndicesAliasesRequest ireq = new IndicesAliasesRequest();
      final AliasActions addAction =
              new AliasActions(AliasActions.Type.ADD)
                      .index(index)
                      .alias(alias);
      ireq.addAliasAction(addAction);
      final AcknowledgedResponse ack =
              getSearchClient().indices().updateAliases(ireq, RequestOptions.DEFAULT);          }

      return 0;
    } catch (final OpenSearchException ese) {
      // Failed somehow
      error(ese);
      return -1;
    } catch (final Throwable t) {
      throw new IndexException(t);
    }
  }

  /** Remove any index that doesn't have an alias and starts with
   * the given prefix
   *
   * @param prefixes Ignore indexes that have names that don't start
   *                 with any of these
   * @return list of purged indexes
   * @throws IndexException on fatal error
   */
  public List<String> purgeIndexes(final Set<String> prefixes)
          throws IndexException {
    final Set<IndexInfo> indexes = getIndexInfo();
    final List<String> purged = new ArrayList<>();

    if (Util.isEmpty(indexes)) {
      return purged;
    }

    purge:
    for (final IndexInfo ii: indexes) {
      final String idx = ii.getIndexName();

      if (!hasPrefix(idx, prefixes)) {
        continue purge;
      }

      /* Don't delete those pointed to by any aliases */

      if (!Util.isEmpty(ii.getAliases())) {
        continue purge;
      }

      purged.add(idx);
    }

    deleteIndexes(purged);

    return purged;
  }

  private final static ObjectMapper jsonOm = new ObjectMapper();

  public List<ContextInfo> getContextInfo() {
    if (theClient == null) {
      throw new RuntimeException("No client for call");
    }

    final Request req = new Request(HttpGet.METHOD_NAME,
                                   "_nodes/stats/indices/search");

    final var response = new ArrayList<ContextInfo>();
    try {
      final var resp = theClient.getLowLevelClient().performRequest(req);

      if ((resp.getStatusLine().getStatusCode() / 100) != 2) {
          return response;
      }

      final var ent = resp.getEntity();

      if ((ent == null) || (ent.getContent() == null)) {
        return response;
      }

      final JsonNode root = jsonOm.readTree(ent.getContent());

      if (root == null) {
        return response;
      }

      final var nodes = root.get("nodes");
      if (nodes == null) {
        return response;
      }

      for (final var prop: nodes.properties()) {
        final var node = prop.getValue();
        final var nameNode = node.get("name");
        if (nameNode == null) {
          continue;
        }

        final var indices = node.get("indices");
        if (indices == null) {
          continue;
        }

        final var snode = indices.get("search");
        if (snode == null) {
          continue;
        }

        final var sninfo = new ContextInfo.IndexInfo(
                "search",
                longVal(snode, "open_contexts"),
                longVal(snode, "scroll_total"),
                longVal(snode, "scroll_current"));
        final var nodeRes = new ContextInfo(nameNode.textValue(),
                                            sninfo);
        response.add(nodeRes);
      }
    } catch (final IOException e) {
      throw new RuntimeException(e);
    }

    return response;
  }

  private long longVal(final JsonNode nd, final String name) {
    final var fld = nd.get(name);
    if (fld == null) {
      return 0;
    }

    return fld.asLong(0);
  }

  private void deleteIndexes(final List<String> names) throws IndexException {
    try {
      final DeleteIndexRequest request = new DeleteIndexRequest(names.toArray(
              new String[0]));

      final AcknowledgedResponse deleteIndexResponse =
              getSearchClient()
                      .indices()
                      .delete(request, RequestOptions.DEFAULT);
    } catch (final Throwable t) {
      throw new IndexException(t);
    }
  }

  private boolean hasPrefix(final String name, 
                           final Set<String> prefixes) {
    for (final String prefix: prefixes) {
      if (name.startsWith(prefix)) {
        return true;
      }
    }
    
    return false;
  }

  private String newIndexSuffix() {
    // ES only allows lower case letters in names (and digits)
    final StringBuilder suffix = new StringBuilder("p");

    final char[] ch = DateTimeUtil.isoDateTimeUTC(new Date()).toCharArray();

    for (int i = 0; i < 8; i++) {
      suffix.append(ch[i]);
    }

    suffix.append('t');

    for (int i = 9; i < 15; i++) {
      suffix.append(ch[i]);
    }

    return suffix.toString();
  }

  private String fileToString(final String path) throws IndexException {
    final StringBuilder content = new StringBuilder();
    try (final Stream<String> stream = Files.lines(Paths.get(path),
                                                   StandardCharsets.UTF_8)) {
      stream.forEach(s -> content.append(s).append("\n"));
    } catch (final Throwable t) {
      throw new IndexException(t);
    }

    return content.toString();
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

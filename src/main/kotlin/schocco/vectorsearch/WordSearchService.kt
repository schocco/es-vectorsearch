package schocco.vectorsearch

import org.apache.http.HttpHost
import org.elasticsearch.ElasticsearchStatusException
import org.elasticsearch.action.DocWriteRequest
import org.elasticsearch.action.bulk.BulkRequest
import org.elasticsearch.action.get.GetRequest
import org.elasticsearch.action.index.IndexRequest
import org.elasticsearch.action.search.SearchRequest
import org.elasticsearch.client.RequestOptions
import org.elasticsearch.client.RestClient
import org.elasticsearch.client.RestHighLevelClient
import org.elasticsearch.client.indices.CreateIndexRequest
import org.elasticsearch.client.indices.GetIndexRequest
import org.elasticsearch.common.xcontent.XContentType
import org.elasticsearch.index.query.QueryBuilders
import org.elasticsearch.index.query.QueryBuilders.matchQuery
import org.elasticsearch.index.query.QueryBuilders.scriptScoreQuery
import org.elasticsearch.index.query.functionscore.ScriptScoreFunctionBuilder
import org.elasticsearch.rest.RestStatus
import org.elasticsearch.script.Script
import org.elasticsearch.script.Script.DEFAULT_SCRIPT_LANG
import org.elasticsearch.script.ScriptType
import org.elasticsearch.search.builder.SearchSourceBuilder
import org.springframework.stereotype.Service


@Service
class WordSearchService {

    private var client: RestHighLevelClient = RestHighLevelClient(
            RestClient.builder(HttpHost("localhost", 9200, "http")))


    fun ensureWordsIndex(dims: Number = 50) {
        val createIndexRequest = CreateIndexRequest(WORDS_INDEX)
        createIndexRequest.mapping(
                """{
                  "properties": {
                    "vector": {
                      "type": "dense_vector",
                      "dims": $dims
                    }
                  }
                }
                """,
                XContentType.JSON
        )
        try {
            client.indices().get(GetIndexRequest(WORDS_INDEX), RequestOptions.DEFAULT)
        } catch (e: ElasticsearchStatusException) {
            if (e.status() == RestStatus.NOT_FOUND) {
                client.indices().create(createIndexRequest, RequestOptions.DEFAULT)
            }
        }

    }

    fun bulkInsert(chunk: List<WordVector>) {
        val bulkRequest = BulkRequest()
        chunk.forEach {
            val indexRequest = IndexRequest(WORDS_INDEX)
                    .id(it.word)
                    .opType(DocWriteRequest.OpType.INDEX)

                    .source(mapOf("word" to it.word, "vector" to it.vector))
            bulkRequest.add(indexRequest)
        }
        client.bulk(bulkRequest, RequestOptions.DEFAULT)
    }

    fun findSimilarWords(word: String): List<WordResult>? {
        val getResponse = client.get(GetRequest(WORDS_INDEX, word), RequestOptions.DEFAULT)
        if (getResponse.isSourceEmpty) {
            return null
        }
        val knownVector = getResponse.source.getValue("vector")
        val script = Script(
                ScriptType.INLINE,
                DEFAULT_SCRIPT_LANG,
                "cosineSimilarity(params.queryVector, doc['vector']) + 1.0",
                mapOf("queryVector" to knownVector))

        val searchRequest = SearchRequest().source(
                SearchSourceBuilder.searchSource()
                        .query(scriptScoreQuery(QueryBuilders.boolQuery().mustNot(matchQuery("_id", getResponse.id)), ScriptScoreFunctionBuilder(script)))
                        .size(NUM_RESULTS)
                        .fetchSource(null, arrayOf("vector"))
        )
        val hits = client.search(searchRequest, RequestOptions.DEFAULT).hits
        return hits.map { hit -> WordResult(hit.sourceAsMap["word"] as String, hit.score) }
    }

    companion object {
        private const val WORDS_INDEX = "words"
        private const val NUM_RESULTS = 20
    }

}

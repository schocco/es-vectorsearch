package schocco.vectorsearch

import com.fasterxml.jackson.databind.ObjectMapper
import org.slf4j.Logger
import org.slf4j.LoggerFactory
import org.springframework.shell.standard.ShellComponent
import org.springframework.shell.standard.ShellMethod
import org.springframework.shell.standard.ShellOption
import java.io.File

@ShellComponent
class ElasticCommands(val service: WordSearchService) {

    @ShellMethod("Load the GLOVE word embeddings into elasticsearch")
    fun import(@ShellOption(defaultValue = "50") dimensions: String = "50"): String {
        service.ensureWordsIndex()
        val fileName = "glove.6B.${dimensions}d.txt"
        File(fileName).bufferedReader().useLines {
            it.chunked(CHUNK_SIZE).forEachIndexed { idx, chunk ->
                LOGGER.info("Processing chunk $idx of $fileName")
                service.bulkInsert(chunk.map { line -> lineToWordVector(line) })
            }
        }
        return "done"
    }

    @ShellMethod("Display potentially similar/related words")
    fun similar(@ShellOption to: String): String {
        val objectMapper: ObjectMapper = ObjectMapper()
        return service.findSimilarWords(to)?.joinToString("\n") { objectMapper.writeValueAsString(it) }
                ?: "word unknown"
    }


    private fun lineToWordVector(line: String): WordVector {
        val tokens = line.split(" ")
        return WordVector(tokens[0], tokens.subList(1, tokens.size).map { it.toFloat() })
    }

    companion object {
        val LOGGER: Logger = LoggerFactory.getLogger(ElasticCommands::class.java)
        const val CHUNK_SIZE = 100
    }
}


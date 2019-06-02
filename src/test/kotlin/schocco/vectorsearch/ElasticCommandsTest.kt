package schocco.vectorsearch

import org.junit.Before
import org.junit.Test
import org.mockito.Mockito.mock

class ElasticCommandsTest {

    @Before
    fun setUp() {
        val logger = org.slf4j.LoggerFactory.getLogger("org.apache.http") as? ch.qos.logback.classic.Logger ?: return
        logger.level = ch.qos.logback.classic.Level.INFO
    }

    @Test
    fun testImport() {
        val service = mock(WordSearchService::class.java)
        ElasticCommands(service).import()
    }
}
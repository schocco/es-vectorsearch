package schocco.vectorsearch

import org.assertj.core.api.Assertions.assertThat
import org.junit.Test

class WordSearchServiceTest {

    @Test
    fun testSimilarWords() {
        assertThat(WordSearchService().findSimilarWords("cat"))
                .isNotNull
                .hasSize(10)
                .anyMatch { it.word == "mouse" }
                .noneMatch { it.word == "cat" }
    }

    @Test
    fun testSearchWithUnknownWord() {
        assertThat(WordSearchService().findSimilarWords("jkhg"))
                .isNull()
    }
}
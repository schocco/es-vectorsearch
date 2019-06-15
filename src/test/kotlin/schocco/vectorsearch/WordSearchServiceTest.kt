package schocco.vectorsearch

import org.assertj.core.api.Assertions.assertThat
import org.junit.Test
import org.junit.runner.RunWith
import org.springframework.test.annotation.IfProfileValue
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner

@IfProfileValue(name="es.test", value="true")
@RunWith(SpringJUnit4ClassRunner::class)
class WordSearchServiceTest {

    @Test
    fun testSimilarWords() {
        assertThat(WordSearchService().findSimilarWords("cat"))
                .isNotNull
                .hasSize(20)
                .anyMatch { it.word == "mouse" }
                .noneMatch { it.word == "cat" }
    }

    @Test
    fun testSearchWithUnknownWord() {
        assertThat(WordSearchService().findSimilarWords("jkhg"))
                .isNull()
    }
}
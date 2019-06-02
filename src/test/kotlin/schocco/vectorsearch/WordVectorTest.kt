package schocco.vectorsearch

import org.assertj.core.api.Assertions.assertThat
import org.junit.Test

class WordVectorTest {

    @Test
    fun getVectorAsJson() {
        assertThat(WordVector("triumphales", listOf(-1.0048, 0.32562, -0.23515, 0.18286, -0.26697, -0.64585, 0.61881, 1.1171, 0.79648, 0.57691, 0.083264, -0.51092)).vectorAsJson)
                .isEqualTo("[-1.0048,0.32562,-0.23515,0.18286,-0.26697,-0.64585,0.61881,1.1171,0.79648,0.57691,0.083264,-0.51092]")
    }
}
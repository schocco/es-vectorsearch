package schocco.vectorsearch

class WordVector(public val word: String, public val vector: List<Number>) {

    public val vectorAsJson
           get() =  vector.joinToString(",", "[", "]")
}
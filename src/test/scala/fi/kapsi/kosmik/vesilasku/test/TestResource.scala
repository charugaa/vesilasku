package fi.kapsi.kosmik.vesilasku.test

trait TestResource {
  def testFile(resourceName: String): String = {
    val resourceStream = getClass.getResourceAsStream(resourceName)
    if (resourceStream == null) {
      throw new IllegalArgumentException(s"resource $resourceName not found")
    }
    scala.io.Source.fromInputStream(resourceStream)
      .getLines().mkString("\n")
  }
}

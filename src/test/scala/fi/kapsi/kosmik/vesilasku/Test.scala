package fi.kapsi.kosmik.vesilasku

object Test {
  def packageResourceFilePath(resource: String): String = {
    getClass.getResource(resource).getPath
  }
}

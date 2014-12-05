import org.w3.banana.rdfstorew.JSStore


package object rww  {

  val rdf = JSStore

  implicit def toReader(string: String): java.io.Reader =
    new org.w3.banana.rdfstorew.StringReader(string)


}

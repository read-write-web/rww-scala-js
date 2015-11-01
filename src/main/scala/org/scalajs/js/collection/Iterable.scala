package scala.scalajs.js.collection

import scala.scalajs.js
import scala.scalajs.js.Dynamic.{global => g}
import scala.scalajs.js.annotation.JSBracketAccess


/**
  * to be replaced by official version once
  * https://github.com/scala-js/scala-js/issues/1141
  * is completed
  * @tparam A
  */

trait Iterable[+A] extends js.Object {
  @JSBracketAccess
  def method[R](symbol: js.Any): js.Function0[R] = js.native
}

@js.native
trait Iterator[+A] extends js.Object {
  def next(): Iterator.Entry[A] = js.native
}

object Iterator {
  val iteratorSymbol = g.Symbol.iterator
  @js.native
  trait Entry[+A] extends js.Object {
    val done: Boolean = js.native
    val value: A = js.native
  }

  implicit def toJSIterator[A](it: Iterator[A]): scala.collection.Iterator[A] =
    new scala.collection.Iterator[A]() {
      var nextEntry: Entry[A] = it.next()

      def hasNext: Boolean = ! nextEntry.done
      def next(): A = {
        val result = nextEntry.value
        nextEntry = it.next()
        result
      }
    }

  implicit class IterableW[+A](it: Iterable[A]) {
    def iterator(): Iterator[A] = {
      val fIt: Function0[Iterator[A]] = it.method[Iterator[A]](iteratorSymbol)
      fIt()
    }
  }


}

@js.native
class Symbol {

}








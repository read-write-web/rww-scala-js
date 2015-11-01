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
@js.native
trait JSIterable[+A] extends js.Object {
  @JSBracketAccess
  def method[R](symbol: js.Any): js.Function0[R] = js.native
}

@js.native
trait JSIterator[+A] extends js.Object {
  def next(): JSIterator.Entry[A] = js.native
}

object JSIterator {
  val iteratorSymbol = g.Symbol.iterator
  @js.native
  trait Entry[+A] extends js.Object {
    val done: Boolean = js.native
    val value: A = js.native
  }

  implicit def toJSIterator[A](it: JSIterator[A]): scala.collection.Iterator[A] =
    new scala.collection.Iterator[A]() {
      var nextEntry: Entry[A] = it.next()

      def hasNext: Boolean = ! nextEntry.done
      def next(): A = {
        val result = nextEntry.value
        nextEntry = it.next()
        result
      }
    }

  implicit class IterableW[+A](it: JSIterable[A]) {
    def iterator(): JSIterator[A] = {
      g.console.log("~>it=",it)
      it.method[JSIterator[A]](iteratorSymbol)()
    }
  }


}




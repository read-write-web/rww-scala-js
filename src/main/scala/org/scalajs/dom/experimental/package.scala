package org.scalajs.dom


import org.scalajs.dom.raw.Promise

import scala.scalajs.js
import scala.scalajs.js.|
/**
  * Created by hjs on 02/11/2015.
  */
package
object experimental extends js.GlobalScope {
  type RequestInfo = String | Request

  /**
    * https://fetch.spec.whatwg.org/#fetch-method
    * @param info
    * @param init
    * @return
    */
  def fetch(info: String | Request, init: RequestInit=null): Promise[Response] = js.native
}

package org.scalajs.dom.experimental

import org.scalajs.dom.raw.Promise

import scala.scalajs.js
import scala.scalajs.js.UndefOr

@js.native
sealed trait WriteableState extends js.Any

object WriteableState {
  /** The stream’s internal queue is full; that is, the stream is exerting backpressure. Use .ready to be notified of
    *  when the pressure subsides. */
  val waiting  = "waiting".asInstanceOf[WriteableState]
  /** The stream’s internal queue is not full; call .write() until backpressure is exerted. */
  val writable = "writable".asInstanceOf[WriteableState]
  /** The stream’s .close() method has been called, and a command to close is in the queue or being processed by the
    * underlying sink; attempts to write will now fail. */
  val closing  = "closing".asInstanceOf[WriteableState]
  /** The underlying sink has been closed; writing is no longer possible. */
  val closed   = "closed".asInstanceOf[WriteableState]
  /** An error occurred interacting with the underlying sink or the stream has been aborted, so the stream is now
    * dead. */
  val errored  = "errored".asInstanceOf[WriteableState]
}

/**
  * https://streams.spec.whatwg.org/#ws-class
  *
  * todo: the constructor
  */
@js.native
trait WriteableStream extends js.Object {

  /**
    * The closed getter returns a promise that will be fulfilled when the stream becomes closed, or rejected if it
    * ever errors.
    */
  def closed: Promise[WriteableStream] = js.native

  /**
    * The ready getter returns a promise that will be fulfilled when the stream transitions away from the "waiting"
    * state to any other state. Once the stream transitions back to "waiting", the getter will return a new promise
    * that stays pending until the next state transition.
    * In essence, this promise gives a signal as to when any backpressure has let up (or that the stream has been
    * closed or errored).
    *
    */
  def ready: Promise[WriteableStream] = js.native

  /**
    * The state getter returns the state of the stream
    */
  def state: WriteableState = js.native

  /**
    * The abort method signals that the producer can no longer successfully write to the stream and it should be
    * immediately moved to an "errored" state, with any queued-up writes discarded. This will also execute any abort
    * mechanism of the underlying sink.
    * @param reason bblfish: not really sure if it is as large as being of type Any
    */
  def abort(reason: Any): Unit = js.native

  /**
    * The close method signals that the producer is done writing chunks to the stream and wishes to move the stream to
    * a "closed" state. This queues an action to close the stream, such that once any currently queued-up writes
    * complete, the close mechanism of the underlying sink will execute, releasing any held resources. In the
    * meantime, the stream will be in a "closing" state.
    *
    * @return a promise of this stream being closed
    */
  def close(): Promise[WriteableStream] = js.native

  /**
    * The write method adds a write to the stream’s internal queue, instructing the stream to write the given chunk of
    * data to the underlying sink once all other pending writes have finished successfully. It returns a promise that
    * will be fulfilled or rejected depending on the success or failure of writing the chunk to the underlying sink.
    * The impact of enqueuing this chunk will be immediately reflected in the stream’s state property; in particular,
    * if the internal queue is now full according to the stream’s queuing strategy, the stream will exert backpressure
    * by changing its state to "waiting".
    * @param chunk
    * @return bblfish: not sure what the type of the promise returned is
    */
  def write(chunk: Array[Byte]): Promise[Any] = js.native
}

/**
  * defined at https://streams.spec.whatwg.org/#readable-stream
  */
@js.native
trait ReadableStream extends js.Object {
  /**
    * The locked getter returns whether or not the readable stream is locked to a reader.
    * @throws scala.scalajs.js.TypeError  if the stream is not readable
    */
  def locked: Boolean = js.native

  /**
    * The cancel method cancels the stream, signaling a loss of interest in the stream by a consumer. The supplied
    * reason argument will be given to the underlying source, which may or may not use it.
    * @param reason the reason <- actually not what type this is
    * @return a Promise, but not quite sure what it can contain
    */
  def cancel(reason: String): Promise[Any] = js.native

  /**
    * The getReader method creates a readable stream reader and locks the stream to the new reader. While the stream
    * is locked, no other reader can be acquired until this one is released. The returned reader provides the ability
    * to directly read individual chunks from the stream via the reader’s read method.
    * This functionality is especially useful for creating abstractions that desire the ability to consume a stream in
    * its entirety. By getting a reader for the stream, you can ensure nobody else can interleave reads with yours or
    * cancel the stream, which would interfere with your abstraction.
    *
    * Note that if a stream becomes closed or errored, any reader it is locked to is automatically released.
    *
    * @throws scala.scalajs.js.TypeError if not a readable stream
    * @return a new ReadableStreamReader
    *
    */
  def getReader(): ReadableStreamReader = js.native

  /**
    * The pipeThrough method provides a convenient, chainable way of piping this readable stream through a transform
    * stream (or any other { writable, readable } pair). It simply pipes the stream into the writable side of the
    * supplied pair, and returns the readable side for further use.
    * Piping a stream will generally lock it for the duration of the pipe, preventing any other consumer from
    * acquiring a
    * reader.
    *
    * This method is intentionally generic; it does not require that its this value be a ReadableStream object. It also
    * does not require that its writable argument be a WritableStream instance, or that its readable argument be a
    * ReadableStream instance.
    */
  def pipeThrough(
    pair: (WriteableStream, ReadableStream),
    options: Any = js.undefined
  ): ReadableStream = js.native

  /**
    * The pipeTo method pipes this readable stream to a given writable stream. The way in which the piping process
    * behaves under various error conditions can be customized with a number of passed options. It returns a promise
    * that fulfills when the piping process completes successfully, or rejects if any errors were encountered.
    * Piping a stream will generally lock it for the duration of the pipe, preventing any other consumer from
    * acquiring a reader.
    *
    * This method is intentionally generic; it does not require that its this value be a ReadableStream object.
    *
    * @param dest
    */
  def pipeTo(dest: WriteableStream, options: Any = js.undefined)


  /**
    * The pipeTo method pipes this readable stream to a given writable stream. The way in which the piping process
    * behaves under various error conditions can be customized with a number of passed options. It returns a promise
    * that fulfills when the piping process completes successfully, or rejects if any errors were encountered.
    * Piping a stream will generally lock it for the duration of the pipe, preventing any other consumer from
    * acquiring a reader.
    *
    * This method is intentionally generic; it does not require that its this value be a ReadableStream object.
    */
  def  tee(): Array[ReadableStream] = js.native
}

/**
  * https://streams.spec.whatwg.org/#reader-class
  * todo: add the constructor
  */
@js.native
class ReadableStreamReader extends js.Object {

  /**
    * The closed getter returns a promise that will be fulfilled when the stream becomes closed or the reader’s lock
    * is released, or rejected if the stream ever errors.
    */
  def closed: Promise[ReadableStreamReader] = js.native

  /**
    * If the reader is active, the cancel method behaves the same as that for the associated stream. When done, it
    * automatically releases the lock.
    * @param reason - not actually sure what type of object this should be
    */
  def cancel(reason: Any): Promise[Any] = js.native //not actually sure what the return type is here

  /**
    * The read method will return a promise that allows access to the next chunk from the stream’s internal queue, if
    * available.
    * If the chunk does become available, the promise will be fulfilled with an object of the form { value: theChunk,
    * done: false }.
    * If the stream becomes closed, the promise will be fulfilled with an object of the form { value: undefined, done:
    * true }.
    * If the stream becomes errored, the promise will be rejected with the relevant error.
    * If reading a chunk causes the queue to become empty, more data will be pulled from the underlying source.
    */
  def read(): Promise[Chunk] = js.native

  /**
    * The releaseLock method releases the reader’s lock on the corresponding stream. After the lock is released, the
    * reader is no longer active. If the associated stream is errored when the lock is released, the reader will
    * appear errored in the same way from now on; otherwise, the reader will appear closed.
    * A reader’s lock cannot be released while it still has a pending read request, i.e., if a promise returned by the
    * reader’s read() method has not yet been settled. Attempting to do so will throw a TypeError and leave the reader
    * locked to the stream.
    *
    * @throws scala.scalajs.js.TypeError
    */
  def releaseLock(): Unit = js.native
}

/**
  *
  * https://streams.spec.whatwg.org/#rs-controller-class
  * The ReadableStreamController constructor cannot be used directly; it only works on a ReadableStream that is in the
  * middle of being constructed.
  *
  * bblfish: not sure what that means
  *
  * @param stream can be null, not sure what the type is
  */
@js.native
class ReadableStreamController(stream: ReadableStream = null) extends js.Object {

  /**
    *
    * The desiredSize getter returns the desired size to fill the controlled stream’s internal queue. It can be
    * negative, if the queue is over-full. An underlying source should use this information to determine when and how
    * to apply backpressure.
    *
    * @return the size of the strema - no idea if this actually is an int
    */
  def desiredSize: Int = js.native


  /**
    * The close method will close the controlled readable stream. Consumers will still be able to read any
    * previously-enqueued chunks from the stream, but once those are read, the stream will become closed
    * @throws scalajs.js.TypeError if this is not a readable controller
    */
  def close(): Unit = js.native

  /**
    * The enqueue method will enqueue a given chunk in the controlled readable stream.
    *
    * @param chunk
    * @throws scalajs.js.RangeError if size is too big ( can this ever be thrown here ? )
    *
    * @return  seems like its an undefOr[Int] of the size
    *
    */
  def enqueue(chunk: js.Array[Byte]): UndefOr[Int] = js.native

  /**
    * The error method will error the readable stream, making all future interactions with it fail with the given
    * error e.
    * @param e : an error - can this be any type?
    * @throws scalajs.js.TypeError
    */
  def error(e: Any): Unit = js.native
}

/**
  * See
  */
@js.native
trait Chunk extends js.Object {
  /* The actual chunk */
  def value: js.Array[Byte] = js.native

  //actually this has a type: what is it?
  def done: Boolean = js.native
}

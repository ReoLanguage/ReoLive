package widgets

import org.scalajs.dom.raw.{Event, MessageEvent, WebSocket}
import scala.sys.process._

/**
  * Offers reusable functions to use when building widgets that require interaction with a server.
  */
object RemoteBox {

  /**
    * Sends a call using websockets to a `service` with a message `send`,
    * and reacts to the result by invoking a `callback` function.
    * @param service name of the service to be called.
    * @param send message to be sent.
    * @param callback function that is called when receiving the message.
    */
  def remoteCall(service: String, send:String, callback: String=>Unit): Unit = {
    val socket = new WebSocket(s"ws://arcatools.org/$service")

    socket.onmessage = { e: MessageEvent => {callback(e.data.toString); socket.close()}}

    val cleanSend = send.replace("\\","\\\\")
                        .replace("\n","\\n")

    socket.addEventListener("open", (e: Event) => {
        socket.send(cleanSend)
    })
  }


//  def lazySageCall(path:String, callback: String => Unit):
//      (String=>Unit, ()=>Int) = {
//    var writer: java.io.PrintWriter = null
//
//    // limit scope of any temporary variables
//    // locally {
//    val sage = s"$path/sage"
//    // strings are implicitly converted to ProcessBuilder
//    // via scala.sys.process.ProcessImplicits.stringToProcess(_)
//    val calcProc = sage.run(new ProcessIO(
//    // Handle subprocess's stdin
//    // (which we write via an OutputStream)
//    in => {
//      writer = new java.io.PrintWriter(in)
//      // later do writer.println(..); writer.flush; writer.close()
//    },
//    // Handle subprocess's stdout
//    // (which we read via an InputStream)
//    out => {
//      val src = scala.io.Source.fromInputStream(out)
//      for (line <- src.getLines()) {
//        callback(line)
//        //println("Answer: " + line)
//      }
//      src.close()
//    },
//    // We don't want to use stderr, so just close it.
//    _.close()
//    ))
//
//    // Using ProcessBuilder.run() will automatically launch
//    // a new thread for the input/output routines passed to ProcessIO.
//    // We just need to wait for it to finish.
//
//    def put(value:String): Unit = {
//      writer.println(value)
//      writer.flush()
//    }
//    def finished(): Int = {
//      writer.close()
//      val code = calcProc.exitValue()
//      //println(s"Subprocess exited with code $code.")
//      code
//    }
//    (put,finished)
//  }

//  def lazySyncSageCall(path:String): String
  

  // failed experiments
  def remoteSageCall(send:String, callback: String=>Unit): Unit = {
//    val s1 = new WebSocket(" ws://localhost:8888/?token=4685499ca13b223b69e9072263def519a11c5d1a53349f22")
    val socket = new WebSocket(s"ws://http://localhost:8888/notebooks/Untitled.ipynb?token=4685499ca13b223b69e9072263def519a11c5d1a53349f22")


    socket.onmessage = { e: MessageEvent => {callback(e.data.toString); socket.close()}}

    val cleanSend = send.replace("\\","\\\\")
      .replace("\n","\\n")

    socket.addEventListener("open", (e: Event) => {
      socket.send(cleanSend)
    })
  }
}

package services

import java.io.{File, PrintWriter}
import scala.sys.process.ProcessLogger
import sys.process._

/**
  * Created by guillerminacledou on 2019-10-04
  */

/**
  * Binder to 'verifyta' from Uppaal tools
  */
object UppaalBind {


  //private val timeout = "timeout 10" // linux
  private val timeout = "gtimeout 10" // macOs

  def verifyta(): (Int, String) = {
    val id = Thread.currentThread().getId
    val stdout = new StringBuilder
    val stderr = new StringBuilder
    val status = s"$timeout verifyta /tmp/uppaal_$id.xml /tmp/uppaal_$id.q".!(ProcessLogger(stdout append _, stderr append _))
    if(status == 0) (status, stdout.toString)
    else (status, stderr.toString)
  }


  def storeInFile(model:String): Unit = {
    val id = Thread.currentThread().getId
    val file = new File(s"/tmp/uppaal_$id.xml")
    file.setExecutable(true)
    val pw = new PrintWriter(file)
    pw.write(model)
    pw.close()
  }
}

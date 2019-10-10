package services

import java.io.{File, PrintWriter}
import scala.sys.process.ProcessLogger
import sys.process._


/**
  * Created by guillecledou on 2019-10-04
  */

/**
  * Binder to 'verifyta' from Uppaal tools
  */
object UppaalBind {


  //private val timeout = "timeout 10" // linux
  private val timeout = "gtimeout 10" // macOs

  def verifyta(modelPath:String,queryPath:String,opt:String): (Int, String) = {
//    val id = Thread.currentThread().getId
    val stdout = new StringBuilder
    val stderr = new StringBuilder
    val status = s"$timeout verifyta $opt $modelPath $queryPath".!(ProcessLogger(stdout append _, stderr append _))
    if(status == 0) (status, stdout.toString)
    else (status, stderr.toString)
  }


  def storeInFile(content:String,path:String): Unit = {
    val file = new File(path)
    file.setExecutable(true)
    val pw = new PrintWriter(file)
    pw.write(content)
    pw.close()
  }
}

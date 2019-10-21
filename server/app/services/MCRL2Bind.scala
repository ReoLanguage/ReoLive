package services

import preo.frontend.mcrl2.Model
import java.io.{File, FileInputStream, PrintWriter}
import java.util.Properties

import scala.sys.process.ProcessLogger
import sys.process._


object MCRL2Bind {
<<<<<<< HEAD
  // private val mcrl2path = "/Applications/mCRL2.app/Contents/bin/"
  private val mcrl2path = "/usr/bin/"
=======
//  private val mcrl2path = "/Applications/mCRL2.app/Contents/bin/"
//  private val mcrl2path = "/usr/bin/"
>>>>>>> master
  private val timeout = "timeout 10"

  private val props: Properties = new Properties
  props.load(new FileInputStream("global.properties"))
  private val mcrl2Path = props.getProperty("mcrl2Path")+"/"


  def savepbes(): (Int, String) = {
    val id = Thread.currentThread().getId
    val stdout = new StringBuilder
    val stderr = new StringBuilder
    val status = s"$timeout ${mcrl2Path}lts2pbes /tmp/model_$id.lts /tmp/modal_$id.pbes --formula=/tmp/modal_$id.mu".!(ProcessLogger(stdout append _, stderr append _))
    if(status == 0) (status, stdout.toString)
    else (status, stderr.toString)
  }

  def solvepbes(): String = {
    val id = Thread.currentThread().getId
    s"$timeout ${mcrl2Path}pbes2bool /tmp/modal_$id.pbes".!!
  }

  def solvepbes2(): String = {
    val id = Thread.currentThread().getId
    s"$timeout ${mcrl2Path}mcrl22lps /tmp/model_$id.mcrl2 /tmp/model_$id.lps".!
    //s"$timeout lps2pbes -c -f {name}/{name}.prop.mcf {name}/{name}.lps {name}/{name}.prop.pbes')"

      // .... under construction...

    s"$timeout ${mcrl2Path}pbessolve -v --file=/tmp/model_$id.lps /tmp/modal_$id.pbes".!!
//    s"${mcrl2path}lps2lts /tmp/modal_$id.pbes.evidence.lps /tmp/modal_$id.pbes.evidence.lts".!!
    s"$timeout ${mcrl2Path}lpspp /tmp/modal_$id.pbes.evidence.lps".!!

  }

  def storeInFile(model:Model): Unit = {
    val id = Thread.currentThread().getId
    val file = new File(s"/tmp/model_$id.mcrl2")
    file.setExecutable(true)
    val pw = new PrintWriter(file)
    pw.write(model.toString)
    pw.close()
  }

  def generateLPS(): Int = {
    val id = Thread.currentThread().getId
    s"$timeout ${mcrl2Path}mcrl22lps /tmp/model_$id.mcrl2 /tmp/model_$id.lps".!
  }

  def generateLTS(): Int = {
    val id = Thread.currentThread().getId
    generateLPS()
    s"$timeout ${mcrl2Path}lps2lts /tmp/model_$id.lps /tmp/model_$id.lts".!
  }

  // TODO: minimisation not in use while experimenting with formulas
  def minimiseLTS(): Int = {
    //val id = Thread.currentThread().getId
    generateLTS()
//    s"${mcrl2path}ltsconvert -ebranching-bisim /tmp/model1_$id.lts /tmp/model_$id.lts".!
  }

  def callLtsGraph(): Unit = {
    val id = Thread.currentThread().getId
    minimiseLTS()
    s"${mcrl2Path}ltsgraph /tmp/model_$id.lts".run()
  }

}

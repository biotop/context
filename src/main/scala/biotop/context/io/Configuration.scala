package biotop.context.io

import java.io.File
import java.io.PrintWriter

import scala.collection.mutable.HashMap
import scala.collection.mutable.Map
import scala.io.Source
import scala.reflect.ClassTag
import scala.reflect.classTag

import com.fasterxml.jackson.databind.ObjectMapper
import com.fasterxml.jackson.databind.SerializationFeature
import com.fasterxml.jackson.module.scala.DefaultScalaModule

import biotop.context.core.LocationSet
import biotop.context.core.RefSeq
import biotop.context.util.PersistentObject
import biotop.context.core.DiscLocationSet

/**
 * Main configuration
 * @author niko.popitsch
 */
class Configuration(val path: String) extends PersistentObject {
  var refSeq: Map[String, RefSeq] = new HashMap()
  var locSet: Map[String, DiscLocationSet] = new HashMap()

  /**
   * create data dir
   */
  var homedir = new File(path).getParentFile
  var datadir = new File(homedir, "data")
  if (!datadir.exists())
    if (!datadir.mkdir())
      throw new RuntimeException("could not create data dir at " + datadir)

  def add(o: Any) = {
    o match {
      case r: RefSeq => if (refSeq.contains(r.id)) throw new RuntimeException("RefSeq ID must be unique") else refSeq += r.id -> r
      case l: DiscLocationSet => if (refSeq.contains(l.id)) throw new RuntimeException("LocationSet ID must be unique") else locSet += l.id -> l
    }
  }

  def del(o: AnyRef, id: String) = {
    o match {
      case r if r == classOf[RefSeq] => refSeq.remove(id)
      case l if l == classOf[LocationSet] => locSet.remove(id)
      case _ => println("unknown class " + o)
    }
  }

  override def save() {
    var s: String = this.toString()
    new PrintWriter(path) { write(s); close }
  }

}

object Configuration {

  var refSeq: Map[String, RefSeq] = new HashMap()

  var locSet: Map[String, LocationSet] = new HashMap()

  /**
   * initialize jackson mapper.
   */
  val jacksonMapper = new ObjectMapper().registerModule(DefaultScalaModule).enable(SerializationFeature.INDENT_OUTPUT);
  jacksonMapper.registerSubtypes(classOf[DiscLocationSet])
  //jacksonMapper .configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false)

  /**
   * Serialize MAP to JSON
   */
  def toJson(value: Map[Symbol, Any]): String = {
    toJson(value map { case (k, v) => k.name -> v })
  }

  /**
   * Serialize object to JSON
   */
  def toJson(value: Any): String = {
    jacksonMapper.writeValueAsString(value)
  }

  /**
   * Deserialize object from the passed JSON string
   */
  def fromJson[T: ClassTag](json: String): T = {
    jacksonMapper.readValue[T](json, classTag[T].runtimeClass.asInstanceOf[Class[T]])
  }

  def load(path: String): Configuration = {
    if (!new File(path).exists()) {
      println("Creating new configuration")
      return new Configuration(path)
    }
    var str = Source.fromFile(path).mkString
    //    println("configuration: " + str + "\n-------------------------------")
    return fromJson[Configuration](str)
  }

  def main(args: Array[String]): Unit = {
    //        // example: serialize and deserialize a refseq object.
    //        var ser = RefSeq.hg19.toString()
    //        println(ser)
    //        var des: RefSeq = RefSeq.fromString(ser)
    //        println(des)

    //        // example simple configuration
    //        var conf = new Configuration("c:/data/context/context.conf.json")
    //        conf.add(RefSeq.hg19)
    //        conf.add(LocationSet.importFromFile("i1", "desc1", RefSeq.hg19, "src/test/resources/TestIntervals1.bed", "C:/data/context/i1.bed"))
    //        conf.save()
    var test = Configuration.load("c:/data/context/context.conf.json")
    println("--------------------------")
    println(test)
    println("--------------------------")

    test.locSet.get("i1").get.iterator.foreach { println }

  }
}
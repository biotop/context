package biotop.context.io

import java.io.PrintWriter

import scala.collection.mutable.HashMap
import scala.collection.mutable.Map
import scala.io.Source
import scala.reflect.ClassTag
import scala.reflect.classTag

import com.fasterxml.jackson.databind.ObjectMapper
import com.fasterxml.jackson.module.scala.DefaultScalaModule

import biotop.context.core.RefSeq
import biotop.context.util.PersistentObject
import com.fasterxml.jackson.databind.SerializationFeature
import java.io.File

/**
 * Main configuration
 * @author niko.popitsch
 */
class Configuration(val path: String) extends PersistentObject {
  var refSeq: Map[String, RefSeq] = new HashMap()

  def add(o: Any) = {
    o match {
      case r: RefSeq => refSeq += r.id -> r
    }
  }

  override def save() {
    var s : String = this.toString()
    new PrintWriter(path) { write(s); close }
  }

}

object Configuration {

  var refSeq: Map[String, RefSeq] = new HashMap()

  /**
   * initialize jackson mapper.
   */
  val jacksonMapper = new ObjectMapper().registerModule(DefaultScalaModule).enable(SerializationFeature.INDENT_OUTPUT);
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
    if (! new File(path).exists()) {
      println("Creating new configuration")
      return new Configuration(path)
    }
    var str = Source.fromFile(path).mkString
    println("configuration: " + str + "\n-------------------------------")
    return fromJson[Configuration](str)
  }

  def main(args: Array[String]): Unit = {
//        // example: serialize and deserialize a refseq object.
//        var ser = RefSeq.hg19.toString()
//        println(ser)
//        var des: RefSeq = RefSeq.fromString(ser)
//        println(des)

//    // example simple configuration
    var conf = new Configuration("c:/data/context/context.conf.json")
    conf.add(RefSeq.hg19)
    conf.save()
    var test = Configuration.load("c:/data/context/context.conf.json")
    println(test)

  }
}
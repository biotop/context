package biotop.context.util

import java.io.BufferedInputStream
import java.io.File
import java.io.FileInputStream
import java.io.FileNotFoundException
import java.util.zip.GZIPInputStream

import scala.collection.mutable.HashMap

import biotop.context.io.BEDCodec
import biotop.context.io.Codec
import biotop.context.io.VCFCodec
import biotop.context.core.Location

/**
 * various tools
 * @author niko.popitsch
 */
object DivTools {

  /**
   * this method will execute the passed block and print the elapsed time in seconds.
   * Example: time { dosomething() }
   */
  def time[R](block: => R): R = {
    val t0 = System.nanoTime()
    val result = block // call-by-name
    val t1 = System.nanoTime()
    val formatter = java.text.NumberFormat.getIntegerInstance

    println(f"Elapsed time: " + "%1.2f".format((t1 - t0) / 1000000000.0) + "s")
    result
  }

  /**
   * Create a buffered file input stream. Supports .gz extensions.
   */
  def fis(s: String): BufferedInputStream = {
    var f = new File(s);
    if (!f.exists())
      throw new FileNotFoundException(s)
    return fis(f)
  }

  /**
   * Create a buffered file input stream. Supports .gz extensions.
   */
  def fis(f: File): BufferedInputStream = {
    if (f.getName.endsWith(".gz"))
      return new BufferedInputStream(new GZIPInputStream(new FileInputStream(f)))
    else
      return new BufferedInputStream(new FileInputStream(f))
  }

  /**
   * Guess a codec from a file extension
   */
  def guessCodec(f: String): Codec = {
    if (f.endsWith(".bed") || f.endsWith(".bed.gz"))
      return BEDCodec
    if (f.endsWith(".vcf") || f.endsWith(".vcf.gz"))
      return VCFCodec
    throw new ParseException("Cannot guess codec from " + f)
  }

  /**
   * Checks whether an iterable is sorted by comparing each pair in the input sequence. @see https://stackoverflow.com/questions/33328923/how-to-check-whether-given-listint-is-sorted-in-scala
   *
   */
  //  def isSorted[T](l: Iterable[T])(implicit ord: Ordering[T]): Boolean = l.sliding(2).forall { case List(x, y) => ord.lt(x, y)  }

  def isSorted(l: Iterable[Location]): Boolean = {
    println("Check sort")
    l.sliding(2).foreach { a: Iterable[Location] =>
      if (a.head.compareTo(a.last) > 0) {
        System.err.println("Order breached at " + a.head + " vs " + a.last)
        return false
      }
    }
    return true
  }

  /**
   * Parses a tab-separated file and loads into a string map
   *
   */
  def loadMap(f: String): Map[String, String] = {
    var m: scala.collection.immutable.HashMap[String, String] = scala.collection.immutable.HashMap()
    val in = io.Source.fromInputStream(DivTools.fis(f))
    for (line <- in.getLines) {
      val cols = line.split("\t").map(_.trim)
      if (!cols(0).startsWith("#") && cols.length == 2) {
        m += (cols(0) -> cols(1))
      }
    }
    in.close
    m
  }
}
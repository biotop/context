package biotop.context.io

import biotop.context.core._
import scala.collection.mutable._
import java.io._
import java.util.zip._
import util.control.Breaks._

/**
 * Import location sets from various serialization formats.
 */
abstract class LocationSetImporter {
  def loadFromStream(r: RefSeq, s: String): Seq[Location]

  def fis(s: String): InputStream = {
    var f = new File(s);
    if (!f.exists())
      throw new FileNotFoundException(s)
    if (s.endsWith(".gz"))
      return new GZIPInputStream(new BufferedInputStream(new FileInputStream(s)))
    else
      return new BufferedInputStream(new FileInputStream(s))
  }
}

/**
 * Load locations from BED files.
 *
 * Please note that coordinates in bed files are 0-based (i.e., first genomic position is e.g. "chr1:0")
 */
object BedImporter extends LocationSetImporter {

  /**
   * Loads a sequence of locations from the passed bed file
   */
  override def loadFromStream(r: RefSeq, f: String): Seq[Location] = {
    val l = Buffer[Location]()
    val bed = io.Source.fromInputStream(fis(f))
    for (line <- bed.getLines) {
      val cols = line.split("\t").map(_.trim)
      breakable {
        if (cols(0).startsWith("#")) break;
        if (cols(0).startsWith("track")) break;
        if (cols.length < 3)
          break;
        l += new Interval(r, cols(0), (cols(1).toInt + 1), (cols(2).toInt + 1))
      }
    }
    bed.close
    return l
  }

}

object Main {
  def main(args: Array[String]) {
    
    // load from BED file and print sorted
    var i = BedImporter.loadFromStream(RefSeq.hg19, "src/test/resources/TestIntervals1.bed")
    println(s"found ${i.size} intervals")
    i.sorted.foreach { println }
    
    // load from gzip-ed bed file
    i = BedImporter.loadFromStream(RefSeq.hg19, "src/test/resources/ucsc.example.bed.gz")
    println(s"found ${i.size} intervals")
    i.sorted.foreach { println }
  }
}

package biotop.context.io

import java.io.BufferedInputStream
import java.io.File
import java.io.FileInputStream
import java.io.FileNotFoundException
import java.io.InputStream
import java.util.zip.GZIPInputStream

import scala.collection.mutable.Buffer
import scala.collection.mutable.Seq

import biotop.context.core.Location
import biotop.context.core.RefSeq
import biotop.context.util.ParseException

/**
 * Import location sets from various serialization formats.
 * @author niko.popitsch
 */
object LocationSetImporter {
  /**
   * Create a buffered file input stream. Supports .gz extensions.
   */
  def fis(s: String): InputStream = {
    var f = new File(s);
    if (!f.exists())
      throw new FileNotFoundException(s)
    if (s.endsWith(".gz"))
      return new GZIPInputStream(new BufferedInputStream(new FileInputStream(s)))
    else
      return new BufferedInputStream(new FileInputStream(s))
  }

  /**
   * Loads a sequence of locations from the passed file. The codec is guessed from the filename extension
   */
  def loadFromStream(r: RefSeq, f: String): Seq[Location] = {
    if (f.endsWith(".bed") || f.endsWith(".bed.gz"))
      return loadFromStream(r, f, BEDCodec)
    if (f.endsWith(".vcf") || f.endsWith(".vcf.gz"))
      return loadFromStream(r, f, VCFCodec)
      throw new ParseException("Could not determine codec for file " + f)
  }
  /**
   * Loads a sequence of locations from the passed bed file
   */
  def loadFromStream(r: RefSeq, f: String, codec: Codec): Seq[Location] = {
    val l = Buffer[Location]()
    val bed = io.Source.fromInputStream(fis(f))
    for (line <- bed.getLines) {
      l ++= codec.decode(line, r)
    }
    bed.close
    return l
  }
}

/**
 * Debugging
 */
object Main {
  def main(args: Array[String]) {

    // load from BED file and print sorted
    var i = LocationSetImporter.loadFromStream(RefSeq.hg19, "src/test/resources/TestIntervals1.bed", BEDCodec)
    println(s"found ${i.size} intervals")
    i.sorted.foreach { println }

    // load from gzip-ed bed file
    i = LocationSetImporter.loadFromStream(RefSeq.hg19, "src/test/resources/ucsc.example.bed.gz")
    println(s"found ${i.size} intervals")
    i.sorted.foreach { println }
    
        // load from vcffile
    i = LocationSetImporter.loadFromStream(RefSeq.hg19, "src/test/resources/TestPositions1.vcf")
    println(s"found ${i.size} positions")
    i.sorted.foreach { println }
  }
}

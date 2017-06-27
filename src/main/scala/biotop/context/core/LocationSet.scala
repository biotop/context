package biotop.context.core

import java.io.File
import java.io.PrintWriter

import scala.collection.mutable.ListBuffer

import org.apache.commons.io.FileUtils

import com.fasterxml.jackson.annotation.JsonIgnore
import com.fasterxml.jackson.annotation.JsonTypeInfo
import com.fasterxml.jackson.core.JsonParser
import com.fasterxml.jackson.databind.DeserializationContext
import com.fasterxml.jackson.databind.JsonDeserializer
import com.fasterxml.jackson.databind.annotation.JsonDeserialize

import biotop.context.io.Configuration
import biotop.context.io.ContextCodec
import biotop.context.util.DivTools
import biotop.context.util.NamedObject
import biotop.context.util.PersistentObject
import com.sun.xml.internal.bind.v2.runtime.reflect.opt.Bean

/**
 * (Immutable) set of locations.
 *
 * By convention only normalized (sorted, canonical chromosome ids, non-overlapping, only positions OR intervals, etc.) location lists should be passed to this class
 *
 * @author niko.popitsch
 */
abstract class LocationSet(val id: String, val desc: String, val ref: RefSeq) extends NamedObject {

  /**
   * If set, the input list is checked for order and an exception is throw if not sorted
   */
  final val debug = true

  // equals
  override def equals(that: Any): Boolean =
    that match {
      case that: LocationSet => this.id == that.id
      case _ => false
    }

  override def hashCode: Int = {
    id.hashCode
  }

  def iterator: LocationSetIterator
  def size: Option[Int]
  def remove
}

/**
 * (Immutable) set of locations.
 *
 * @author niko.popitsch
 */
class DiscLocationSet(override val id: String, override val desc: String, override val ref: RefSeq, val path: String) extends LocationSet(id, desc, ref) with PersistentObject {

  /**
   * load locations from persistent path into memory
   */
  def load(): MemoryLocationSet = {

    // NOTE: ListBuffer is a mutable list which has constant-time append, and constant-time conversion into a List
    val l = ListBuffer[Location]()
    if (path != null) {
      println("loading from " + path)
      val in = DivTools.fis(path)
      val dat = io.Source.fromInputStream(in)
      for (line <- dat.getLines) {
        l ++= ContextCodec.decode(line, ref)
      }
      dat.close
      println("loaded " + l.size)
    }
    var locations = l.toList

    if (debug) {
      if (!DivTools.isSorted(locations))
        throw new RuntimeException("ERROR: Location list passed to LocationSet list is not sorted by coordinates")
    }
    return new MemoryLocationSet(id, desc, ref, locations)
  }

  def size: Option[Int] = {
    // cannot determine size of disc location set
    None
  }

  def iterator: LocationSetIterator = {
    return new DiscLocationSetIterator(this)
  }

  override def save() {
    // readonly
  }

  override def remove() {
    FileUtils.deleteQuietly(new File(path))
  }
}

/**
 * (Immutable) set of in-memory locations.
 *
 * @author niko.popitsch
 */
class MemoryLocationSet(override val id: String, override val desc: String, override val ref: RefSeq, @JsonIgnore val locations: List[Location]) extends LocationSet(id, desc, ref) {

  def size: Option[Int] = {
    Option(locations.size)
  }

  def iterator: LocationSetIterator = {
    return new MemoryLocationSetIterator(this)
  }

  override def remove() {
    // do nothing
  }

}

/**
 * A (buffered) iterator that can return the current as well as the previously delivered item.
 */
abstract class LocationSetIterator(val ls: LocationSet) extends Iterator[Location] {

  def prev: Option[Location]
  var here: Option[Location]

}
class MemoryLocationSetIterator(override val ls: MemoryLocationSet) extends LocationSetIterator(ls) {

  var it = ls.locations.iterator
  var prev: Option[Location] = None
  var here: Option[Location] = None

  def hasNext: Boolean = it.hasNext
  def next(): Location = {
    prev = here
    here = Option(it.next())
    here.get
  }

}
class DiscLocationSetIterator(override val ls: DiscLocationSet) extends LocationSetIterator(ls) {

  val in = DivTools.fis(ls.path)
  val dat = io.Source.fromInputStream(in)
  var it = dat.getLines()
  var prev: Option[Location] = None
  var here: Option[Location] = None

  def hasNext: Boolean = it.hasNext
  def next(): Location = {
    prev = here
    var line = it.next()
    here = ContextCodec.decode(line, ls.ref)
    here.get
  }

}

object LocationSet {

  val empty: LocationSet = new MemoryLocationSet("empty", "Empty Set", RefSeq.hg19, List[Location]())

  def fromString(s: String) = Configuration.fromJson[RefSeq](s)

  /**
   * Loads a sequence of locations from the passed bed file and creates a normalized location set.
   */
  def importFromFile(id: String, desc: String, ref: RefSeq, inpath: String, conf: Configuration): LocationSet = {
    return importFromFile(id, desc, ref, inpath, conf.datadir)
  }
  /**
   * Loads a sequence of locations from the passed bed file and creates a normalized location set.
   */
  def importFromFile(id: String, desc: String, ref: RefSeq, inpath: String, outdir: File): LocationSet = {

    println("importing from " + inpath)

    // NOTE: ListBuffer is a mutable list which has constant-time append, and constant-time conversion into a List
    val locations = ListBuffer[Location]()
    val in = io.Source.fromInputStream(DivTools.fis(inpath))
    val inCodec = DivTools.guessCodec(inpath)

    for (line <- in.getLines) {
      var loc = inCodec.decode(line, ref)
      if (loc.isDefined) {
        locations ++= loc
      }
    }
    in.close

    // TODO normalize.
    val outpath = new File(outdir, id + ".ctx")

    // sort and store to new file
    val outCodec = ContextCodec
    val out = new PrintWriter(outpath)
    for (l <- locations.toList.sorted) {
      out.println(outCodec.code(l))
    }
    out.close()

    println("imported " + locations.size)

    var ret = new DiscLocationSet(id, desc, ref, outpath.getAbsolutePath)
    ret.load()
    println("importing finished")
    return ret
  }

  def main(args: Array[String]) {

    println("start")

    // load from BED file and print sorted
    var i = LocationSet.importFromFile("i1", "desc1", RefSeq.hg19, "src/test/resources/TestIntervals1.bed", new File("C:/data/context/"))
    println(s"found ${i.size} intervals")
    var it = i.iterator
    while (it.hasNext) {
      println(it.next() + "(" + it.prev + ")")
    }

    // load from gzip-ed bed file
    i = LocationSet.importFromFile("ucsc", "descucsc", RefSeq.hg19, "src/test/resources/ucsc.example.bed.gz", new File("C:/data/context/"))
    println(s"found ${i.size} intervals")
    it = i.iterator
    while (it.hasNext) {
      println(it.next() + "(" + it.prev + ")")
    }

    // load from vcffile
    i = LocationSet.importFromFile("p1", "descpos1", RefSeq.hg19, "src/test/resources/TestPositions1.vcf", new File("C:/data/context/"))
    println(s"found ${i.size} positions")
    it = i.iterator
    while (it.hasNext) {
      println(it.next() + "(" + it.prev + ")")
    }

  }

}







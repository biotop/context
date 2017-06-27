package biotop.context.core

import java.io.File

import scala.collection.SortedSet
import scala.collection.mutable.ListBuffer
import scala.collection.mutable.TreeSet

/**
 * Iterates over a location set and filters by a given set of locationset + bool operators to combine them.
 */
class FilteredLocationIterator(val p: LocationSet, val scopes: List[LocationSet], val scopeFilterString: String) extends Iterator[Location] {
  val all = List.concat(List(p), scopes)
  val filterString =  "(" + scopeFilterString+" ) and "+p.id
  val it: SynchronizedLeftEndpointIterator = new SynchronizedLeftEndpointIterator(all.reverse)
  val filter = new LocationSetFilter(all,filterString)
  println(filterString + " - " + filter.allowedPatterns)
  var nextLocation: Location = null
  next()

  override def hasNext = {
    if (nextLocation == null)
      nextLocation = next()
    nextLocation != null
  }

  override def next: Location = {
    var ret = nextLocation
    var valid = false
    do {
      if (it.hasNext) nextLocation = it.next() else nextLocation = null
      if (nextLocation == null)
        return ret
      valid = filter.allowedPatterns.contains(it.emsk)
//      println( nextLocation + " / " + it.emsk + " / " + valid ) 
    } while (!valid)
    return ret
  }

}

/**
 * Iterates over location set endpoints
 */
class SynchronizedLeftEndpointIterator(val d: List[LocationSet]) extends Iterator[Position] {

  /**
   * Number of location sets.
   */
  var n = d.length

  /**
   * Number of LocationSets that reached eof
   */
  var eof: Int = 0

  /**
   * Sorted set of current LocationSet locations.
   *
   *  performance: lookup / add / remove: Log. @see http://docs.scala-lang.org/overviews/collections/performance-characteristics.html
   */
  implicit val po = PositionOrdering
  var s: SortedSet[Position] = TreeSet()

  /**
   * Current (last emited) position
   */
  var epos: Location = null

  /**
   * Bitmask indicating which LocationSet contains current position
   */
  var emsk: Int = 0

  /**
   * location set iterators.
   */
  var dit: ListBuffer[LocationSetIterator] = ListBuffer()

  /*
   * init data
   */
  for (i <- 0 to n - 1) {
    dit += d(i).iterator
    if (!dit(i).hasNext) {
      // empty list
      eof += 1
    } else {
      // add first position to location buffer
      s += dit(i).next().toPos
    }
  }

  override def hasNext = {
    (eof < n) || (!s.isEmpty)
  }

  override def next: Position = {
    emsk = 0
    epos = s.firstKey
    s = s.drop(1)
    for (i <- 0 to n - 1) {
      if (dit(i).here.get.toPos == epos) { emsk += 1 << i }
      if (dit(i).hasNext) {
        if (dit(i).here.get.toPos <= epos) {
          s += dit(i).next().toPos
          //         println("Added " +      dit(i).here)     
          if (!dit(i).hasNext) {
            eof += 1
          }
        }
      } // not eof
    }
    epos.toPos
  }

  /**
   * Consume the whole iterator. Use only for small sets or debugging.
   */
  def consume: List[Location] = {
    var b: ListBuffer[Location] = ListBuffer()
    while (hasNext)
      b += next()
    b.toList
  }

}

/**
 * Debugging
 */
object Main {
  def main(args: Array[String]) {
    //    var s: scala.collection.SortedSet[Location] = TreeSet()
    //    val A = new Interval(RefSeq.hg19, "1", 1, 100)
    //    val B = new Interval(RefSeq.hg19, "2", 50, 200)
    //    val C = new Position(RefSeq.hg19, "1", 99)
    //    s += (A)
    //    s += (B)
    //    s += (C)
    //    println(s)
    //    var pop = s.firstKey
    //    s = s.drop(1)
    //    println(pop + "\t" + s)

    // load from BED file and print sorted
    var ls1 = LocationSet.importFromFile("i1", null, RefSeq.hg19, "src/test/resources/TestIntervals1.bed", new File("C:/data/context/"))
    var ls2 = LocationSet.importFromFile("i2", null, RefSeq.hg19, "src/test/resources/TestIntervals2.bed", new File("C:/data/context/"))
    var pos = LocationSet.importFromFile("i3", null, RefSeq.hg19, "src/test/resources/TestPositions1.vcf", new File("C:/data/context/"))

//    var iter = new SynchronizedLeftEndpointIterator(List(pos, ls1, ls2))
//    while (iter.hasNext)
//      println(iter.next() + "\t" + iter.emsk.toBinaryString)

    //      
        var fit = new FilteredLocationIterator(pos, List(ls1, ls2), "i2 or i1")
        while (fit.hasNext)
          println("> " + fit.next())

    println("done")

  }
}

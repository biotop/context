package biotop.context.core

import scala.util.hashing.MurmurHash3
import scala.collection.mutable.TreeSet
import biotop.context.util.DivTools

/**
 * Abstract location on a given reference genome
 * @author niko.popitsch
 */
abstract class Location(val ref: RefSeq, val chrIdx: Int) extends Ordered[Location] {
  def overlaps(l: Location): Boolean = {
    return ref == l.ref && chrIdx == l.chrIdx
  }
  override def compare(that: Location) = (this.chrIdx) compare (that.chrIdx)
  def canEqual(a: Any) = a.isInstanceOf[Location]

  def toPos: Position = {
    this match {
      case p: Position => return p
      case i: Interval => return i.left()
    }
  }

  def toDisk: String
}

class Chromosome(override val ref: RefSeq, override val chrIdx: Int) extends Location(ref, chrIdx) {
  override def toDisk = ref.mapChr(chrIdx)
}

/**
 * genomic position
 */
class Position(override val ref: RefSeq, override val chrIdx: Int, val pos: Int) extends Location(ref, chrIdx) {

  /**
   * Constructor that allows passing a chr:pos string.
   */
  def this(ref: RefSeq, pos: String) = {
    this(ref, ref.mapChrIdx(pos.split(":")(0)).get, pos.split(":")(1).toInt)
  }

  /**
   * Constructor that allows passing a chr string.
   */
  def this(ref: RefSeq, chr: String, pos: Int) = {
    this(ref, ref.mapChrIdx(chr).get, pos)
  }

  override def toString = ref.mapChr(chrIdx) + ":" + pos

  override def toDisk = ref.mapChr(chrIdx) + "\t" + pos

  def +(i: Int): Position = {
    return new Position(ref, chrIdx, pos + i)
  }
  def -(i: Int): Position = {
    return new Position(ref, chrIdx, pos - i)
  }

  // check for overlaps
  override def overlaps(l: Location): Boolean = {
    l match {
      case p: Position => return super.overlaps(l) && pos == p.pos
      case i: Interval => return super.overlaps(l) && i.overlaps(this)
    }
  }

  // "interval" representation
  def interval(): Interval = {
    return new Interval(ref, chrIdx, pos, pos)
  }

  // implicit conversion of positions to intervals
  implicit def intToPos(int: Interval): Position = int.left()

  // ordering
  override def compare(that: Location): Int = {
    var s = super.compare(that)
    if (s != 0)
      return s
    that match {
      case p: Position => {
        if (pos < p.pos)
          return -1
        if (pos > p.pos)
          return 1
        return 0
      }
      case i: Interval => {
        if (pos < i.start)
          return -1
        if (pos > i.start)
          return 1
        return 0
      }
    }
  }

  // equals
  override def equals(that: Any): Boolean =
    that match {
      case that: Position => this.chrIdx == that.chrIdx && this.pos == that.pos
      case _ => false
    }
  // hashCode
  override def hashCode: Int = {
    MurmurHash3.arrayHash(Array(chrIdx, pos))
  }

}

/**
 * genomic interval
 */
class Interval(override val ref: RefSeq, override val chrIdx: Int, val start: Int, val end: Int) extends Location(ref, chrIdx) {

  /**
   * Constructor that allows passing a chr:start-stop string.
   */
  def this(ref: RefSeq, pos: String) = {
    this(ref, ref.mapChrIdx(pos.split(":")(0)).get, pos.split(":")(1).split("-")(0).toInt, pos.split(":")(1).split("-")(1).toInt)
  }
  
  /**
   * Constructor that allows passing a chr string.
   */
  def this(ref: RefSeq, chr: String, start: Int, end: Int) = {
    this(ref, ref.mapChrIdx(chr).get, start, end)
  }

  override def toString = ref.mapChr(chrIdx) + ":" + start + "-" + end

  override def toDisk = ref.mapChr(chrIdx) + "\t" + start + "\t" + end

  // add offset to interval
  def +(i: Int): Interval = {
    return new Interval(ref, chrIdx, start + i, end + i)
  }
  // subtract offset from interval
  def -(i: Int): Interval = {
    return new Interval(ref, chrIdx, start - i, end - i)
  }
  // "left" endpoint
  def left(): Position = {
    return new Position(ref, chrIdx, start)
  }
  // "right" endpoint
  def right(): Position = {
    return new Position(ref, chrIdx, end)
  }

  def contains(l: Location): Boolean = {
    l match {
      case p: Position => return super.overlaps(p) && start <= p.pos && end >= p.pos
      case i: Interval => return super.overlaps(l) && start <= i.start && end >= i.end
    }
  }

  // check for overlaps
  override def overlaps(l: Location): Boolean = {
    l match {
      case p: Position => return contains(p)
      case i: Interval => return super.overlaps(i) && (contains(i.left()) || i.contains(left()))
    }
  }

  // implicit conversion of positions to intervals
  implicit def posToInt(pos: Position): Interval = pos.interval()

  // ordering
  override def compare(that: Location): Int = {
    var s = super.compare(that)
    if (s != 0)
      return s
    that match {
      case p: Position => {
        if (start < p.pos)
          return -1
        if (start > p.pos)
          return 1
        return 0
      }
      case i: Interval => {
        if (start < i.start)
          return -1
        if (start > i.start)
          return 1
        return 0
      }
    }
  }

  // equals
  override def equals(that: Any): Boolean =
    that match {
      case that: Interval => this.chrIdx == that.chrIdx && this.start == that.start && this.end == that.end
      case _ => false
    }

  // hash code
  override def hashCode: Int = {
    //chr.hashCode + start.hashCode + end.hashCode
    //List(chr, start, end).hashCode
    MurmurHash3.arrayHash(Array(chrIdx, start, end))
  }

}

/**
 * Required to enable implicit ordering.
 * @see https://stackoverflow.com/questions/1818777/extend-scala-class-that-extends-ordered
 */
object PositionOrdering extends Ordering[Position] {
  def compare(a: Position, b: Position) = a.compare(b)
}
object IntervalOrdering extends Ordering[Interval] {
  def compare(a: Interval, b: Interval) = a.compare(b)
}

/**
 * Debugging
 */
object a {
  def main(args: Array[String]) {
    implicit val po = PositionOrdering
    var s: scala.collection.SortedSet[Position] = new TreeSet()
    for (i <- 0 to 11) {
      val B = new Position(RefSeq.hg19, i % 3, i + 1)
      s += B
    }
    println(s)
    println(DivTools.isSorted(s.toList))

    println(DivTools.isSorted(List(new Position(RefSeq.hg19, "1:10"), new Position(RefSeq.hg19, "1:9")).toList))

  }
}

//case class A(x: Int) extends Ordered[A] {
//  def compare(that: A) = x.compare(that.x)
//}
//
//// Compiles but can't be used to define a TreeMap key
//class B(var y: Int) extends A(1) {
//  override def compare(that: A) = that match {
//    case b: B => x.compare(b.x) match {
//      case 0 => y.compare(b.y)
//      case res => res
//    }
//    case _: A => super.compare(that)
//  }
//}
//
//object BOrdering extends Ordering[B] {
//    def compare(a: B, b: B) = a.compare(b)
//  }
//
//object x {
//  def main(args: Array[String]) {
//    implicit val bo = BOrdering
//    collection.immutable.TreeMap[B, Int]()
//  }
//}


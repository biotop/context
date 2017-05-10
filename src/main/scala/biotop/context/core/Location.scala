package biotop.context.core

import scala.util.hashing.MurmurHash3

/**
 * Abstract location on a given reference genome
 * @author niko.popitsch
 */
abstract class Location(val ref: RefSeq, val chr: String) extends Ordered[Location] {
  def overlaps(l: Location): Boolean = {
    return ref == l.ref && chr == l.chr
  }
  override def compare(that: Location) = (this.chr) compare (that.chr)
  def canEqual(a: Any) = a.isInstanceOf[Location]
}

/**
 * genomic position
 */
class Position(override val ref: RefSeq, override val chr: String, val pos: Int) extends Location(ref, chr) {

  override def toString = chr + ":" + pos
  def +(i: Int): Position = {
    return new Position(ref, chr, pos + i)
  }
  def -(i: Int): Position = {
    return new Position(ref, chr, pos - i)
  }
  // check for overlaps
  override def overlaps(l: Location): Boolean = {
    l match {
      case p: Position => return super.overlaps(l) && pos == p.pos
      case i: Interval => return super.overlaps(l) && i.overlaps(this)
    }
  }
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
      case that: Position => this.chr == that.chr && this.pos == that.pos
      case _ => false
    }
  override def hashCode: Int = {
    MurmurHash3.arrayHash(Array(chr, pos))
  }
}

/**
 * genomic interval
 */
class Interval(override val ref: RefSeq, override val chr: String, val start: Int, val end: Int) extends Location(ref, chr) {
  override def toString = chr + ":" + start + "-" + end
  // add offset to interval
  def +(i: Int): Interval = {
    return new Interval(ref, chr, start + i, end + i)
  }
  // subtract offset from interval
  def -(i: Int): Interval = {
    return new Interval(ref, chr, start - i, end - i)
  }
  // "left" endpoint
  def left(): Position = {
    return new Position(ref, chr, start)
  }
  // "right" endpoint
  def right(): Position = {
    return new Position(ref, chr, end)
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
      case that: Interval => this.chr == that.chr && this.start == that.start && this.end == that.end
      case _ => false
    }
  override def hashCode: Int = {
    //chr.hashCode + start.hashCode + end.hashCode
    //List(chr, start, end).hashCode
    MurmurHash3.arrayHash(Array(chr, start, end))
  }

}

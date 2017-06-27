package biotop.context.io

import biotop.context.core.Interval
import biotop.context.core.Location
import biotop.context.core.Position
import biotop.context.core.RefSeq
import biotop.context.util.ParseException

trait Codec {
  def decode(line: String, r: RefSeq): Option[Location];

  def code(loc: Location): String;
}

/**
 * Parses BED formatted lines and normalizes the data
 */
object BEDCodec extends Codec {

  override def decode(line: String, r: RefSeq): Option[Location] = {
    try {
      val cols = line.split("\t").map(_.trim)

      if (cols(0).startsWith("#")) return None;
      if (cols(0).startsWith("track")) return None;
      if (cols.length < 3)
        return None;
      var chr = r.normalizeChrom(cols(0))
      if (chr.isDefined)
        return Option(new Interval(r, r.mapChrIdx(chr.get).get, (cols(1).toInt + 1), (cols(2).toInt + 1)))
      None
    } catch {
      case t: Throwable => println(t); throw new ParseException("Parsing error at '" + line + "'")
    }
  }

  override def code(loc: Location): String = {
    loc.toDisk
  }

}

/**
 * Parses VCF formatted lines
 */
object VCFCodec extends Codec {
  override def decode(line: String, r: RefSeq): Option[Location] = {
    try {
      val cols = line.split("\t").map(_.trim)
      if (cols(0).startsWith("#")) return None;
      if (cols.length < 2)
        return None;
      var chr = r.normalizeChrom(cols(0))
      if (chr.isDefined)
        return Option(new Position(r, r.mapChrIdx(chr.get).get, cols(1).toInt))
      None
    } catch {
      case t: Throwable => println(t); throw new ParseException("Parsing error at '" + line + "'")
    }
  }

  override def code(loc: Location): String = {
    loc.toDisk
  }
}

/**
 * Internal context format. TODO: compression.
 */
object ContextCodec extends Codec {
  override def decode(line: String, r: RefSeq): Option[Location] = {
    try {
      val cols = line.split("\t").map(_.trim)
      if (cols.length == 2)
        return Option(new Position(r, cols(0).toInt, cols(1).toInt))
      else
        return Option(new Interval(r, cols(0).toInt, cols(1).toInt, cols(2).toInt))
      None
    } catch {
      case t: Throwable => println(t); throw new ParseException("Parsing error at '" + line + "'")
    }
  }

  override def code(loc: Location): String = {
    loc match {
      case p: Position => return p.chrIdx + "\t" + p.pos
      case i: Interval => return i.chrIdx + "\t" + i.start + "\t" + i.end
    }
  }
}

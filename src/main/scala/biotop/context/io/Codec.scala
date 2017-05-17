package biotop.context.io

import biotop.context.core.Interval
import biotop.context.core.Location
import biotop.context.core.RefSeq
import biotop.context.util.ParseException
import biotop.context.core.Position

trait Codec {
  def decode(line: String, r: RefSeq): Option[Location];
}

/**
 * Parses BED formatted lines
 */
object BEDCodec extends Codec {

  @Override
  def decode(line: String, r: RefSeq): Option[Location] = {
    try {
      val cols = line.split("\t").map(_.trim)

      if (cols(0).startsWith("#")) return None;
      if (cols(0).startsWith("track")) return None;
      if (cols.length < 3)
        return None;
      return Option(new Interval(r, cols(0), (cols(1).toInt + 1), (cols(2).toInt + 1)))
    } catch {
      case t: Throwable => println(t); throw new ParseException("Parsing error at '" + line + "'")
    }

  }

}

/**
 * Parses VCF formatted lines
 */
object VCFCodec extends Codec {

  @Override
  def decode(line: String, r: RefSeq): Option[Location] = {
    try {
      val cols = line.split("\t").map(_.trim)

      if (cols(0).startsWith("#")) return None;
      if (cols.length < 3)
        return None;
      return Option(new Position(r, cols(0), cols(1).toInt ))
    } catch {
      case t: Throwable => println(t); throw new ParseException("Parsing error at '" + line + "'")
    }

  }

}

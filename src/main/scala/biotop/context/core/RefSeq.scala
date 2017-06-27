package biotop.context.core

import scala.collection.mutable.HashMap
import scala.collection.mutable.ListBuffer

import biotop.context.io.Configuration
import biotop.context.util.DivTools
import biotop.context.util.NamedObject
import biotop.context.util.PersistentObject

/**
 * TBD
 * @author niko.popitsch
 */
class RefSeq(val id: String, val desc: String, val chroms: List[String], val chromLen: Map[String, Int], val chromAliases: Option[Map[String, String]], val path: String = null) extends NamedObject with PersistentObject {

  // equals
  override def equals(that: Any): Boolean =
    that match {
      case that: RefSeq => this.id == that.id
      case _ => false
    }

  override def hashCode: Int = {
    id.hashCode
  }

  override def save() {
    // readonly
  }
  
  def normalizeChrom(chr: String): Option[String] = {
    if ( chroms.contains(chr))
      return Option(chr)
    if ( chromAliases.isDefined )
      return Option(chromAliases.get.getOrElse(chr, null))
    None
  }
  
  def mapChr(chrIdx: Int): String = {
    chroms(chrIdx)
  }
  
  def mapChrIdx(chr: String): Option[Int] = {
    for ( i<-0 to chroms.length )
      if ( chroms(i) == chr )
        return Option(i)
    None
  }

}

object RefSeq {

  val hg19Chroms = List("1", "2", "3", "4", "5", "6", "7", "8", "9", "10", "11", "12", "13", "14", "15", "16", "17", "18", "19", "20", "21", "22", "MT", "X", "Y");
  val hg19Lens = Map("1" -> 249250621,
    "2" -> 243199373,
    "3" -> 198022430,
    "4" -> 191154276,
    "5" -> 180915260,
    "6" -> 171115067,
    "7" -> 159138663,
    "8" -> 146364022,
    "9" -> 141213431,
    "10" -> 135534747,
    "11" -> 135006516,
    "12" -> 133851895,
    "13" -> 115169878,
    "14" -> 107349540,
    "15" -> 102531392,
    "16" -> 90354753,
    "17" -> 81195210,
    "18" -> 78077248,
    "19" -> 59128983,
    "20" -> 63025520,
    "21" -> 48129895,
    "22" -> 51304566,
    "MT" -> 16569,
    "X" -> 155270560,
    "Y" -> 59373566)
  val hg19Aliases = Map("chr1" -> "1",
    "chr2" -> "2",
    "chr3" -> "3",
    "chr4" -> "4",
    "chr5" -> "5",
    "chr6" -> "6",
    "chr7" -> "7",
    "chr8" -> "8",
    "chr9" -> "9",
    "chr10" -> "10",
    "chr11" -> "11",
    "chr12" -> "12",
    "chr13" -> "13",
    "chr14" -> "14",
    "chr15" -> "15",
    "chr16" -> "16",
    "chr17" -> "17",
    "chr18" -> "18",
    "chr19" -> "19",
    "chr20" -> "20",
    "chr21" -> "21",
    "chr22" -> "22",
    "chrX" -> "X",
    "chrY" -> "Y",
    "chrM" -> "MT",
    "chrMT" -> "MT",
    "M" -> "MT")

  val hg19: RefSeq = new RefSeq("hg19", "Human genome GRCh37", hg19Chroms, hg19Lens, Option(hg19Aliases), null)

  def fromString(s: String) = Configuration.fromJson[RefSeq](s)

  /**
   * Import from file
   */
  // note: the => is just a hack to make the compire understand that these are two distinct method signatures
  def importFromFile(id: String, desc: String, inpath: String, consideredChromsStr: => Option[String], chromAliasesFile: Option[String]): RefSeq = {
    var consideredChroms = if (consideredChromsStr.isDefined) Option(consideredChromsStr.get.split(",").map(_.trim).toList) else None
    var chromAliases = if ( chromAliasesFile.isDefined) Option(DivTools.loadMap(chromAliasesFile.get)) else None
    return importFromFile(id, desc, inpath, consideredChroms, chromAliases)
  }
  
  /**
   * Import from file
   */
  def importFromFile(id: String, desc: String, inpath: String, consideredChroms: Option[List[String]], chromAliases: Option[Map[String, String]]): RefSeq = {
    // extract chrom names
    val refChroms = ListBuffer[String]()
    val refLens = HashMap[String, Int]()
    val in = io.Source.fromInputStream(DivTools.fis(inpath))
    var chr: String = null
    var len = 0
    println("Importing Refseq from " + inpath)
    println("Considered chromosome ids: " + consideredChroms.getOrElse("ALL"))
    println("Chromosome aliases: " + chromAliases.getOrElse("None"))
    for (line <- in.getLines) {
      if (line.startsWith(">")) {
        if (chr != null) {
          if (! consideredChroms.isDefined || consideredChroms.get.contains(chr) || (chromAliases.isDefined && consideredChroms.get.contains(chromAliases.getOrElse(chr, None)))) {
            refChroms += chr
            refLens += (chr -> len)
          }
          len = 0
        }
        if (line.indexOf(' ', 1) < 0)
          chr = line.substring(1).trim
        else
          chr = line.substring(1, line.indexOf(' ', 1))
        println("parsed chromosome [" + chr + "]")
      } else {
        len += line.length()
      }
    }
    if (chr != None) {
      if (consideredChroms == None || consideredChroms.get.contains(chr)) {
        refChroms += chr
        refLens += (chr -> len)
      }
    }
    in.close
    return new RefSeq(id, desc, refChroms.toList, refLens.toMap, chromAliases, inpath)
  }

  def main(args: Array[String]) = {
    var r = RefSeq.importFromFile("hs37d5", "hg19 + decoy", "Z:/ref/genomes/hs37d5/hs37d5.fa", Option(hg19Chroms), Option(hg19Aliases))
    println(r)
  }

}



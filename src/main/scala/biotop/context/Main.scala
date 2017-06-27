package biotop.context

import org.rogach.scallop.ScallopConf
import org.rogach.scallop.Subcommand

import biotop.context.core.LocationSet
import biotop.context.core.RefSeq
import biotop.context.io.Configuration

/**
 * Main app
 * @author niko.popitsch
 */
object Main extends App {
  def VERSION = "0.1"

  /**
   * This class represents a CONTEXT configuration
   */
  case class ConfigData(arguments: Seq[String]) extends ScallopConf(arguments) {
    guessOptionName = true
    shortSubcommandsHelp(false)
    version("CONTEXT " + VERSION + " (c) 2017 Biotop")
    banner("Usage: ctx [--version] [--help] [-c <path>] <command> [<args>]")
    footer("\n'ctx [subcommand] --help' for more help on available subcommands.\n")

    shortSubcommandsHelp(true)
    val version = opt[Boolean](hidden = true)

    // add refseq
    val addref = new Subcommand("addref") {
      descr("Add a reference sequence")
      banner("\nusage: ctx addref [OPTIONS] <pathspec>\n")
      val id = opt[String]("id", short = 'i', descr = "ID", required = true)
      val chroms = opt[String]("chr", short = 'c', descr = "Imported chromosomes (optional)", required = false)
      val alias = opt[String]("alias", short = 'a', descr = "Chromosome aliases file (optional)", required = false)
      val desc = opt[String]("desc", short = 'd', descr = "Description (human readable)", required = false)
      val pathSpec = trailArg[String](required = true,
        name = "<pathspec>",
        descr = "FASTA file containing the reference seqeunce.")
    }
    addSubcommand(addref)

    // del refseq
    val delref = new Subcommand("delref") {
      descr("Remove a reference sequence")
      banner("\nusage: ctx delref [OPTIONS]\n")
      val id = opt[String]("id", short = 'i', descr = "ID", required = true)
    }
    addSubcommand(delref)

    // add locationset
    val addls = new Subcommand("addls") {
      descr("Add a location set")
      banner("\nusage: ctx addls [OPTIONS] <pathspec>\n")

      val id = opt[String]("id", short = 'i', descr = "ID", required = true)
      val rid = opt[String]("rid", short = 'r', descr = "RefSeq id", required = true)
      val desc = opt[String]("desc", short = 'd', descr = "Description (human readable)", required = false)
      val pathSpec = trailArg[String](required = true,
        name = "<pathspec>",
        descr = "BED/VCF file containing the location set.")
    }
    addSubcommand(addls)

    // del locationset
    val dells = new Subcommand("dells") {
      descr("Remove a location set")
      banner("\nusage: ctx delref [OPTIONS]\n")
      val id = opt[String]("id", short = 'i', descr = "ID", required = true)
    }
    addSubcommand(dells)

    val configPath = opt[String]("config", short = 'c', descr = "Configuration", required = false, default = Option("src/test/resources/testconf.json"))

    errorMessageHandler = { message =>
      Console.err.println("Error: %s\n" format (message))
      builder.printHelp
      sys.exit(1)
    }
    verify
  }

  /**
   * Generic Tool Interface for stitching configs and command handlers ...
   * @see https://gist.github.com/omnisis/1252de6ea9f054cd84ae
   */
  class Tool[C <: ScallopConf](conf: C) {
    type Handler = (C) => Unit

    var subcmdHandlers = Map[ScallopConf, Handler]()
    var primaryHandler: Option[Handler] = None

    def registerSubcmdHandler(subcmd: ScallopConf, handler: Handler): Unit = {
      subcmdHandlers += (subcmd -> handler)
    }

    def registerPrimaryHandler(handler: Handler): Unit = {
      primaryHandler = Some(handler)
    }

    def run(): Unit = {
      conf.subcommand match {
        case None => conf.printHelp()
        case Some(cmd) =>
          //          println(s"You ran a subcmd: ${cmd}")
          if (subcmdHandlers.keySet.contains(cmd)) {
            subcmdHandlers(cmd).apply(conf)
          } else {
            println(s"No Handler registered for ${cmd.printedName}!")
          }
      }
    }
  }

  /**
   * some commandline argument rewrites for testing
   */
//  override val args: Array[String] = Array("addls",
//    "-i", "RefSeqGenesExons",
//    "-r", "hg19",
//    "-d", "RefSeq gene exon annotations from longest transcript.",
//    "Z:/ref/geneanno/exons/UCSC-refseq.exons.sorted.bed.gz")
//      override val args: Array[String] = Array( "addls",
//      "-i", "RefSeqGenes",
//      "-r", "hg19",
//      "-d", "RefSeq gene annotations as downloaded from UCSC",
//      "Z:/ref/geneanno/exons/UCSC-refseq.genes.sorted.bed.gz")
  //  override val args: Array[String] = Array("addref",
  //    "-i", "hg19",
  //    "-c", "1,2,3,4,5,6,7,8,9,10,11,12,13,14,15,16,17,18,19,20,21,22,X,Y,MT",
  //    "-a", "Z:/ref/genomes/hs37d5/hs37d5.fa.alias",
  //    "Z:/ref/genomes/hs37d5/hs37d5.fa"
  //    )
  //  override val args: Array[String] = Array("addref", "-i", "GRCh38", "-d", "hg20", "Z:/ref/genomes/GRCh38/GCA_000001405.15_GRCh38_full_plus_hs38d1_analysis_set")
  //  override val args: Array[String] = Array("delref", "-i", "GRCh38")
  //    override val args: Array[String] = Array("addref", "--help")
  val toolConf = new ConfigData(args.toSeq)
  val context = new Tool(toolConf)
  context.registerSubcmdHandler(toolConf.addref, (c: ConfigData) => {
    /**
     * Add a reference sequence
     */
    println(s"Add ${c.addref.id.apply()} reference")
    var conf = Configuration.load(c.configPath.apply())
    val id = c.addref.id.apply()
    if (conf.refSeq.contains(id))
      throw new RuntimeException("RefSeq Set id must be unique! " + id)
    conf.add(RefSeq.importFromFile(id, c.addref.desc.getOrElse("Reference sequence " + id), c.addref.pathSpec.apply(), c.addref.chroms.toOption, c.addref.alias.toOption))
    conf.save()
  })
  context.registerSubcmdHandler(toolConf.delref, (c: ConfigData) => {
    /**
     * Remove a reference sequence
     */
    println(s"Remove ${c.delref.id.apply()} reference")
    var conf = Configuration.load(c.configPath.apply())
    conf.del(classOf[RefSeq], c.delref.id.apply())
    conf.save()
  })
  context.registerSubcmdHandler(toolConf.addls, (c: ConfigData) => {
    /**
     * Add a location set
     */
    println(s"Add ${c.addls.id.apply()} location set")
    var conf = Configuration.load(c.configPath.apply())
    val id = c.addls.id.apply()
    val rid = c.addls.rid.apply()
    val ref = conf.refSeq.get(rid)
    if (!ref.isDefined)
      throw new RuntimeException("Cannot find reference sequence with id " + rid)
    if (conf.locSet.contains(id))
      throw new RuntimeException("Location Set id must be unique! " + id)
    conf.add(LocationSet.importFromFile(id, c.addls.desc.getOrElse("Location set " + id), ref.get, c.addls.pathSpec.apply(), conf))
    conf.save()
  })
  context.registerSubcmdHandler(toolConf.dells, (c: ConfigData) => {
    /**
     * Remove a location set
     */
    println(s"Remove ${c.delref.id.apply()} location set")
    var conf = Configuration.load(c.configPath.apply())
    // TODO delete cached location set file.
    val id = c.dells.id.apply()
    val ls = conf.locSet.get(id)
    if (!ls.isDefined)
      throw new RuntimeException("Cannot find location set with id " + id)
    ls.get.remove
    conf.del(classOf[RefSeq], c.dells.id.apply())
    conf.save()
  })
  context.run()

}
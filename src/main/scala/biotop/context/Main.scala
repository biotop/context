package biotop.context

import org.rogach.scallop.ScallopConf
import org.rogach.scallop.Subcommand

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
    footer("\n'ctx help -a' and 'ctx help -g' lists available subcommands.\n")

    shortSubcommandsHelp(true)
    val version = opt[Boolean](hidden = true)

    val addref = new Subcommand("addref") {
      descr("Add a reference sequence")
      banner("\nusage: ctx addref [OPTIONS] <pathspec>\n")

      val id = opt[String]("id", short = 'i', descr = "ID", required = true)
      val desc = opt[String]("desc", short = 'd', descr = "Description (human readable)", required = false)
      val pathSpec = trailArg[String](required = true,
        name = "<pathspec>",
        descr = "FASTA file containing the reference seqeunce.")
    }
    addSubcommand(addref)

    val delref = new Subcommand("delref") {
      descr("Remove a reference sequence")
      banner("\nusage: ctx delref [OPTIONS]\n")
      val id = opt[String]("id", short = 'i', descr = "ID", required = true)
    }
    addSubcommand(delref)

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
  //  override val args: Array[String] = Array("addref", "-i", "hg19", "Z:/ref/genomes/hs37d5/hs37d5.fa")
  //  override val args: Array[String] = Array("addref", "-i", "GRCh38", "-d", "hg20", "Z:/ref/genomes/GRCh38/GCA_000001405.15_GRCh38_full_plus_hs38d1_analysis_set")
  //  override val args: Array[String] = Array("delref", "-i", "GRCh38")
  //  override val args: Array[String] = Array("--help")
  //  override val args: Array[String] = Array("addref")
  val toolConf = new ConfigData(args.toSeq)
  val context = new Tool(toolConf)
  context.registerSubcmdHandler(toolConf.addref, (c: ConfigData) => {
    /**
     * Add a reference sequence
     */
    println(s"Add ${c.addref.id.apply()} reference")
    var conf = Configuration.load(c.configPath.apply())
    conf.add(new RefSeq(c.addref.id.apply(), c.addref.desc.getOrElse("Reference sequence " + c.addref.id.apply()), c.addref.pathSpec.apply()))
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
  context.run()

}
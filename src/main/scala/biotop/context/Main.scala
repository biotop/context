package biotop.context

import biotop.context.core.RefSeq
import biotop.context.io.Configuration
import scopt.OptionParser

case class Config(
  configPath: String = "src/test/resources/testconf.json",
  cmd: String = null,
  subcmd: String = null,
  path: String = null,
  id: String = null,
  desc: String = "")

/**
 * Main app
 * @author niko.popitsch
 */
object Main extends App {
  def VERSION = "0.1"

  /**
   * Default parameters
   */
  //override val args: Array[String] = Array("add", "refSeq", "-i", "hg19", "-p", "Z:/ref/genomes/hs37d5/hs37d5.fa")
  //  override val args: Array[String] = Array("add", "refSeq", "-i", "GRCh38", "-p", "Z:/ref/genomes/GRCh38/GCA_000001405.15_GRCh38_full_plus_hs38d1_analysis_set")
  //  override val args: Array[String] = Array("--help")
  //  override val args: Array[String] = Array("--s")

  println(args.deep.mkString("\n"))

  val parser = new scopt.OptionParser[Config]("scopt") {
    head("CONTEXT", VERSION)

    help("help").text("prints this usage text")
    opt[String]('c', "conf").optional().valueName("<json>").action((x, c) =>
      c.copy(configPath = x)).text("Config file (default: src/test/resources/testconf.json")

    cmd("add").action((_, c) => c.copy(cmd = "add")).required().hidden().
      text("Adds something.").
      children(

        cmd("refSeq").action((_, c) => c.copy(subcmd = "refSeq")).required().
          text("Add a reference sequence.").
          children(
            opt[String]('p', "path").required().valueName("<File>").action((x, c) =>
              c.copy(path = x)).text("FASTA file"),
            opt[String]('i', "id").required().valueName("<String>").action((x, c) =>
              c.copy(id = x)).text("ID"),
            opt[String]('d', "desc").optional().valueName("<String>").action((x, c) =>
              c.copy(desc = x)).text("Human readable descriptiin")))

  }
  
  
  println( parser.usage.head)

  /**
   * Parse configuration and execute commands
   */
  parser.parse(args, Config()) match {
    case Some(config) =>
      var conf = Configuration.load(config.configPath)
      config.cmd match {
        case "add" =>
          config.subcmd match {
            case "refSeq" =>
              println("Adding RefSeq " + config.id)
              conf.add(new RefSeq(config.id, config.desc, config.path))
              conf.save()
          }
        case null =>
          println("error: " + parser.showUsage())
      }
    case None =>
    // arguments are bad, error message will have been displayed
    //parser.showUsage()
  }

}
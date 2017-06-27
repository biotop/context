package biotop.context.core

import scala.collection.mutable.HashMap
import scala.util.parsing.combinator.JavaTokenParsers

class LocationSetFilter(val scopes: List[LocationSet], var filterString: String) {
  val ids = for ( l <- scopes.reverse ) yield l.id
  var allowedPatterns = LogicalExpressionParser.iterateBitmaps(filterString, ids)
  
}

/**
 * A simple logical expression parser, @see https://gist.github.com/sofoklis/3343973
 *
 *
 *  <b-expression>::= <b-term> [<orop> <b-term>]*
 *  <b-term>      ::= <not-factor> [AND <not-factor>]*
 *  <not-factor>  ::= [NOT] <b-factor>
 *  <b-factor>    ::= <b-literal> | <b-variable> | (<b-expression>)
 */
case class LogicalExpressionParser(variableMap: Map[String, Boolean]) extends JavaTokenParsers {
  private lazy val b_expression: Parser[Boolean] = b_term ~ rep("or" ~ b_term) ^^ { case f1 ~ fs ⇒ (f1 /: fs)(_ || _._2) }
  private lazy val b_term: Parser[Boolean] = (b_not_factor ~ rep("and" ~ b_not_factor)) ^^ { case f1 ~ fs ⇒ (f1 /: fs)(_ && _._2) }
  private lazy val b_not_factor: Parser[Boolean] = opt("not") ~ b_factor ^^ (x ⇒ x match { case Some(v) ~ f ⇒ !f; case None ~ f ⇒ f })
  private lazy val b_factor: Parser[Boolean] = b_literal | b_variable | ("(" ~ b_expression ~ ")" ^^ { case "(" ~ exp ~ ")" ⇒ exp })
  private lazy val b_literal: Parser[Boolean] = "true" ^^ (x ⇒ true) | "false" ^^ (x ⇒ false)
  // This will construct the list of variables for this parser
  private lazy val b_variable: Parser[Boolean] = variableMap.keysIterator.map(Parser(_)).reduceLeft(_ | _) ^^ (x ⇒ variableMap(x))

  def parseExpression(expression: String): ParseResult[Boolean] = this.parseAll(b_expression, expression)

}

object LogicalExpressionParser {

  def fmt(s: String, z: Int): String = {
    var ret = ""
    for (i <- 0 to z - s.length() - 1) ret += "0"
    return ret + s
  }

  def eval(expression: String, variables: Map[String, Boolean]): Boolean = {
    try {
      LogicalExpressionParser(variables).parseExpression(expression).get
    } catch {
      case e: RuntimeException =>
        println("Parsing error ["+expression+" / " + variables + "]: " + e.getMessage)
        false
    }
  }

  /**
   * Iterates over all possible bitmaps for the passed variables and returns a set of Ints that are compatible with the input given the passed logical expression.
   * Example:
   *
   * var s = LogicalExpressionParser.iterateBitmaps("a or (b and c) or not d", List("a", "b", "c", "d"))
   * // contains the indices (0, 1, 2, 3, 4, 5, 6, 7, 9, 11, 13, 14, 15) according to the following table
   * a or (b and c) or not d
   *
   * dcba result
   * 0000	TRUE
   * 0001	TRUE
   * 0010	TRUE
   * 0011	TRUE
   * 0100	TRUE
   * 0101	TRUE
   * 0110	TRUE
   * 0111	TRUE
   * 1000	FALSE
   * 1001	TRUE
   * 1010	FALSE
   * 1011	TRUE
   * 1100	FALSE
   * 1101	TRUE
   * 1110	TRUE
   * 1111	TRUE
   *
   */
  def iterateBitmaps(expr: String, variables: List[String]): Set[Int] = {
    var s = Set[Int]()
    if (variables.isEmpty)
      return s

    for (i <- 0 to (1 << variables.size) - 1) {
      val vmap = HashMap[String, Boolean]()
      for ((v, ii) <- variables.view.zipWithIndex) {
        vmap.put(v, (i & (1 << ii)) == (1 << ii))
      }
      //      println(vmap)
      //      println(i + "\t" + LogicalExpressionParser.fmt(i.toBinaryString, variables.size) + "\t" + expr + "\t" + LogicalExpressionParser.eval(expr, vmap.toMap) + "\t" + vmap.toMap)
      if (LogicalExpressionParser.eval(expr, vmap.toMap))
        s += i
    }
    s
  }

}

/**
 * debugging.
 */
object run {

  def main(args: Array[String]) {
    //    println("testing parser")
    //    //    var s = LogicalExpressionParser.iterateBitmaps("a or (b and c) or not d", List("a", "b", "c", "d"))
    //    var s = LogicalExpressionParser.iterateBitmaps("(a or (b and c)) and d", List("a", "b", "c", "d"))
    //    println(s.toSeq.sorted)
//    val l = List(new Position(RefSeq.hg19, "1:100"), new Position(RefSeq.hg19, "1:150"))
//    val n = for (e <- l) yield e.pos
//    println(n + " / " + n.getClass)
    val l = List(1,2,3)
    val n = for (e <- l) yield e + 10 
    println(n + " / " + n.getClass)
  }
}
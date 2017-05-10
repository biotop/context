package biotop.context.util

/**
 * various tools
 * @author niko.popitsch
 */
object DivTools {

  /**
   * this method will execute the passed block and print the elapsed time in seconds.
   * Example: time { dosomething() }
   */
  def time[R](block: => R): R = {
    val t0 = System.nanoTime()
    val result = block // call-by-name
    val t1 = System.nanoTime()
    val formatter = java.text.NumberFormat.getIntegerInstance

    println(f"Elapsed time: " + "%1.2f".format((t1 - t0) / 1000000000.0) + "s")
    result
  }

}
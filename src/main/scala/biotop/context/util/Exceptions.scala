package biotop.context.util

/**
 * Thrown if some parsing process was unsuccessful.
 */
case class ParseException(private val message: String = "",
  private val cause: Throwable = None.orNull)
    extends Exception(message, cause)

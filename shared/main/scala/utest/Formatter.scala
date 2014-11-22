package utest
import utest.framework.Result
import utest.util.Tree
import scala.util.{Failure, Success}
import java.io.{PrintWriter, StringWriter}

/**
 * Represents something that can format a single test result or a [[Tree]] of 
 * them. 
 */
abstract class Formatter{
  /**
   * Prettyprints a single result.
   */
  def formatSingle(path: Seq[String], r: Result): String

  /**
   * Prettyprints a tree of results; may or may not use `formatResult`.
   */
  def format(results: Tree[Result]): String
}
object DefaultFormatter{
  def apply(implicit args: Array[String]) = {
    val color = utest.util.ArgParse.find("--color", _.toBoolean, true, true)
    val truncate = utest.util.ArgParse.find("--truncate", _.toInt, 500, 500)
    val trace = utest.util.ArgParse.find("--trace", _.toBoolean, true, true)

    new DefaultFormatter(color, truncate, trace)
  }
}
/**
 * Default implementation of [[Formatter]], also used by the default SBT test
 * framework. Allows some degree of customization of the formatted test results.
 */
class DefaultFormatter(color: Boolean = true, 
                       truncate: Int = 500,
                       trace: Boolean = true) extends Formatter{
  
  def prettyTruncate(r: Result, length: Int = truncate): String = {

    val colorStr  = if (r.value.isSuccess) Console.GREEN else Console.RED
    val cutUnit = r.value match {
      case Success(_) => "Success"
      case Failure(ex) =>
        val optCause = Option(ex.getCause).fold("")(cause => s" caused by '$cause'")
        s"Failure('$ex'$optCause)"
      case _ => r.value.toString
    }

    val truncUnit =
      if (cutUnit.length > length) cutUnit.take(length) + "..."
      else cutUnit

    if (color) colorStr + truncUnit + Console.RESET else truncUnit
  }

  def formatSingle(path: Seq[String], r: Result): String = {
    path.map("." + _).mkString + "\t\t" + prettyTruncate(r)
  }

  def format(results: Tree[Result]): String = {
    results.map(r => r.name + "\t\t" + prettyTruncate(r))
           .reduce(_ + _.map("\n" + _).mkString.replace("\n", "\n    "))
  }
}


package biotop.context.util

import biotop.context.io.Configuration

/**
 * A persistent object
 * @author niko.popitsch
 */
trait PersistentObject {

  def path: String

  override def toString() = Configuration.toJson(this)
  
  def save()
  
}
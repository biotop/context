package biotop.context.core

import biotop.context.util.NamedObject
import biotop.context.util.PersistentObject
import biotop.context.io.Configuration


/**
 * TBD
 * @author niko.popitsch
 */
class RefSeq(val id: String, val desc: String, val path: String) extends NamedObject with PersistentObject {
  
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

}

object RefSeq {
  val hg19: RefSeq = new RefSeq("hg19", "Human genome GRCh37", null) 
  
    def fromString( s: String ) = Configuration.fromJson[RefSeq](s)
}



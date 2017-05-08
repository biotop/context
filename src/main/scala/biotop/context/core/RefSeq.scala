package biotop.context.core

// TBD
class RefSeq(val gid: String) {

  // equals
  override def equals(that: Any): Boolean =
    that match {
      case that: RefSeq => this.hashCode == that.hashCode
      case _ => false
    }
  override def hashCode: Int = {
    gid.hashCode
  }

}

object RefSeq {
  val hg19: RefSeq = new RefSeq("hg19")
}



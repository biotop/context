package biotop.context.core

import org.junit._
import org.junit.Assert._
import scala.collection.mutable._

/**
 * Test the location classes
 */
@Test
class LocationTest {

  val A = new Interval(RefSeq.hg19, "1", 1, 100)
  val B = new Interval(RefSeq.hg19, "2", 50, 200)
  val C = new Position(RefSeq.hg19, "1", 99)
  val D = new Position(RefSeq.hg19, "1", 44)
  val E = new Position(RefSeq.hg19, "1", 44)
  val F = new Interval(RefSeq.hg19, "1", 10, 100)
  val G = new Interval(RefSeq.hg19, "1", 11, 110)
  val H = new Position(RefSeq.hg19, "1", 45)
  val I = new Interval(RefSeq.hg19, "1", 2, 99)

  @Before def init() {
    // do some initialization
  }

  @Test
  def testOverlap() {

    // intervals and positions overlap with themselves.
    assertTrue(A.overlaps(A))
    assertTrue(D.overlaps(D))

    // intervals / positions on different chroms do not overlap
    assertFalse(A.overlaps(B))
    assertFalse(B.overlaps(C))

    // intervals overlap positions if they contain them
    assertTrue(A.overlaps(C))

    // test intervals overlap
    assertTrue(A.overlaps(F))
    assertTrue(F.overlaps(A))
    assertTrue(A.overlaps(G))
    assertTrue(G.overlaps(A))

  }

  @Test
  def testEquals() {
    // test equality
    assertEquals(A, A)
    assertEquals(D, E)
    assertTrue(A == A)
    assertTrue(A != B)
    // previous problem with hashCode function wrongly asserted A == I due to hashCode of an Int being its value
    assertFalse(A == I)
  }

  @Test
  def testList() {
    var s = List(A, B, C)
    // test sorting
    assertEquals(s.sorted, List(A, C, B))
  }

  @Test
  def testInc() {
    //test offset functions
    assertEquals(D + 1, H)
    assertNotEquals(D + 2, H)
    assertEquals(A + 10, G)
  }

}

package biotop.context.core

import scala.util.Random
import org.junit._
import org.junit.Assert._
import scala.collection.mutable._
import biotop.context.util.DivTools

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
    assertFalse(A == I)
    assertNotEquals(A,I)
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


  /**
   * Test performance 
   */
  @Ignore("slow")
  @Test
  def hashCodePerformance() {
    val rand = new Random()
    val chrs = List("1", "2", "3")
    val set = scala.collection.mutable.Set[Location]()
    val N = 100000
    val M = 100
    DivTools.time {
      for (a <- 1 to M) {
        for (b <- 1 to N) {
          var c = chrs(rand.nextInt(chrs.length))
          var l = new Interval(RefSeq.hg19, c, rand.nextInt(100), rand.nextInt(100))
          set.add(l)
          assertTrue(set.contains(l))
        }
      }
    }
  }

}

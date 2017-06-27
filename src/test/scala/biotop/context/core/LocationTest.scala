package biotop.context.core

import scala.collection.mutable.TreeSet
import scala.util.Random

import org.junit.Assert
import org.junit.Before
import org.junit.Ignore
import org.junit.Test

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
    Assert.assertTrue(A.overlaps(A))
    Assert.assertTrue(D.overlaps(D))

    // intervals / positions on different chroms do not overlap
    Assert.assertFalse(A.overlaps(B))
    Assert.assertFalse(B.overlaps(C))

    // intervals overlap positions if they contain them
    Assert.assertTrue(A.overlaps(C))

    // test intervals overlap
    Assert.assertTrue(A.overlaps(F))
    Assert.assertTrue(F.overlaps(A))
    Assert.assertTrue(A.overlaps(G))
    Assert.assertTrue(G.overlaps(A))

  }

  @Test
  def testEquals() {
    // test equality
    Assert.assertEquals(A, A)
    Assert.assertEquals(D, E)
    Assert.assertTrue(A == A)
    Assert.assertTrue(A != B)
    Assert.assertFalse(A == I)
    Assert.assertNotEquals(A, I)
  }

  @Test
  def testList() {
    var s = List(A, B, C)
    // test sorting
    Assert.assertEquals(s.sorted, List(A, C, B))
  }

  @Test
  def testInc() {
    //test offset functions
    Assert.assertEquals(D + 1, H)
    Assert.assertNotEquals(D + 2, H)
    Assert.assertEquals(A + 10, G)
  }

  @Test
  def testOrdering() {
    Assert.assertTrue(A < B)
    Assert.assertTrue(B > C)
    Assert.assertTrue(A < C)
    Assert.assertTrue(C > D)
    var shouldList: List[Location] = List(A, D, C, B)
    var isList: scala.collection.SortedSet[Location] = TreeSet(D, C, B, A)
    (shouldList, isList).zipped.foreach { (a, b) => Assert.assertEquals(a, b) }

    Assert.assertEquals(isList.firstKey, A)
    Assert.assertTrue(isList.lastKey == B)
  }

  @Test
  def testSyncIt() {

    /**
     * NOTE list must be sorted!
     */
    var l1 = new MemoryLocationSet("i1", "", RefSeq.hg19, List(A, B).asInstanceOf[List[Location]].sorted)
    var l2 = new MemoryLocationSet("i2", "", RefSeq.hg19, List(C, D).asInstanceOf[List[Location]].sorted)

    var shouldList: List[Location] = List(A.toPos, D.toPos, C.toPos, B.toPos)
    var isList = new SynchronizedLeftEndpointIterator(List(l1, l2)).consume
    println(shouldList)
    println(isList)
    (shouldList, isList).zipped.foreach { (a, b) => Assert.assertEquals(a, b) }
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
          Assert.assertTrue(set.contains(l))
        }
      }
    }
  }

}

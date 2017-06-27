package biotop.context.core

import scala.collection.mutable.ListBuffer

import org.junit.Assert
import org.junit.Before
import org.junit.Test

/**
 * Test the location classes
 */
@Test
class SynchronizedIteratorTest {

  var grid = new Array[MemoryLocationSet](10)
  var gridEndpoints = new Array[MemoryLocationSet](10)

  @Before def init() {
    // create grids with 100 intervals on 2 chromosomes each
    for (i <- 0 to grid.length - 1) {
      var spacing = i + 1
      var intervals = ListBuffer[Location]()
      var endpoints = ListBuffer[Location]()
      for (chr <- 0 to 1)
        for (p <- 0 to 100 by spacing * 2) {
          intervals += new Interval(RefSeq.hg19, chr, p, p + spacing)
          endpoints += new Position(RefSeq.hg19, chr, p)
        }
      grid(i) = new MemoryLocationSet("grid" + i, "Grid with spacing " + i, RefSeq.hg19, intervals.toList)
      gridEndpoints(i) = new MemoryLocationSet("gridEndpoints" + i, "Grid endpoints with spacing " + i, RefSeq.hg19, endpoints.toList)
    }

  }

  @Test
  def testLeftEndpointIterator() {

    // first simple test: iterate a list of positions / intervals and assert that all are read.
    // compare with endpoint lists
    var isList = new SynchronizedLeftEndpointIterator(List(gridEndpoints(0))).consume
    (gridEndpoints(0).locations, isList).zipped.foreach { (a, b) => Assert.assertEquals(a, b) }
    // compare with left endpoint list of grid
    isList = new SynchronizedLeftEndpointIterator(List(grid(0))).consume
    (gridEndpoints(0).locations, isList).zipped.foreach { (a, b) => Assert.assertEquals(a, b) }

  }

  @Test
  def testEMSK() {

    // iterate endpoint bitmasks
    // 0 2 4 6 8 10 12 14 16 18 20 22 24 [0]
    // 0   4   8    12    16    20    24 [1]
    // 0     6      12       18       24 [2]
    // -------------------------------------
    // 7 1 3 5 3  1 ...                  [msk]
    var it = new SynchronizedLeftEndpointIterator(List(grid(0), gridEndpoints(1), grid(2)))
    for ((l, i) <- it.zipWithIndex) {
      //      println(pos + " " + it.emsk.toBinaryString)
      l.pos % 12 match {
        case 0 => Assert.assertEquals(it.emsk, 7)
        case 2 => Assert.assertEquals(it.emsk, 1)
        case 4 => Assert.assertEquals(it.emsk, 3)
        case 6 => Assert.assertEquals(it.emsk, 5)
        case 8 => Assert.assertEquals(it.emsk, 3)
        case 10 => Assert.assertEquals(it.emsk, 1)
      }
    }
  }

  @Test
  def testFilteredLocationIterator() {

    // tests whether the expected positions are returned by the filterediterator
    // 0 2 4 6 8 10 12 14 16 18 20 22 24 [0]
    // 0   4   8    12    16    20    24 [1]
    // 0     6      12       18       24 [2]
    // -------------------------------------
    // 0            12                24 [1 and 2]
    // 0   4 6 8    12    16 18 20    24 [1 or 2]
    //     4   8          16    20    24 [1 and not 2]
    // 0 2   6   10 12 14    18    22 24 [not 1 or 2]

    var it = new FilteredLocationIterator(grid(0), List(grid(1), grid(2)), "(grid1 and grid2)")
    var s = collection.mutable.Set[Int]()
    for ((l, i) <- it.zipWithIndex) {
      s += l.toPos.pos % 12
    }
    Assert.assertEquals(Set(0), s)

    it = new FilteredLocationIterator(grid(0), List(grid(1), grid(2)), "(grid1 or grid2)")
    s = collection.mutable.Set[Int]()
    for ((l, i) <- it.zipWithIndex) {
      s += l.toPos.pos % 12
    }
    Assert.assertEquals(Set(0, 4, 6, 8), s)

    it = new FilteredLocationIterator(grid(0), List(grid(1), grid(2)), "(grid1 and not grid2)")
    s = collection.mutable.Set[Int]()
    for ((l, i) <- it.zipWithIndex) {
      s += l.toPos.pos % 12
    }
    Assert.assertEquals(Set(4, 8), s)

    it = new FilteredLocationIterator(grid(0), List(grid(1), grid(2)), "(not grid1 or grid2)")
    s = collection.mutable.Set[Int]()
    for ((l, i) <- it.zipWithIndex) {
      s += l.toPos.pos % 12
    }
    Assert.assertEquals(Set(0, 2, 6, 10), s)

    it = new FilteredLocationIterator(grid(0), List(grid(1), grid(2), grid(3)), "(grid1 and grid2) and grid3")
    s = collection.mutable.Set[Int]()
    for ((l, i) <- it.zipWithIndex) {
      s += l.toPos.pos % 24
    }
    Assert.assertEquals(Set(0), s)

  }

}

/**
 * 
 */
package SlidingWindow;

import static org.junit.Assert.*;

import java.util.HashMap;
import java.util.Random;

import org.junit.Test;

/**
 * @author Yaron
 *
 */
public class SpaceSavingTest {
	


	@Test
	public void test() {
		int niters = 1<<17;
		int size = 1<<7;
		SpaceSaving ss = new SpaceSaving(niters,size, 17);
		Random rand = new Random(1);
		
		HashMap<Long, Integer> exactCount = new HashMap<Long, Integer>();
		HashMap<Long, Integer> overflows = new HashMap<Long, Integer>();
		
		long x;
		double exact, estimated;
		for (int i=0; i<niters; ++i) {
			// generate a random number
			x = rand.nextInt(200);
			// count it 
			if (exactCount.containsKey(x)) {
				exactCount.put(x, exactCount.get(x)+1);
			}
			else {
				exactCount.put(x,1);
			}
			boolean overflow = ss.inc(x);
			if (overflow) {
				if (overflows.containsKey(x)) {
					overflows.put(x, overflows.get(x)+1);
				}
				else {
					overflows.put(x, 1);
				}
			}
			// query a random number
			x = rand.nextInt(128);
			// count it in window
			if (exactCount.containsKey(x)) {
				exact = exactCount.get(x);
			}
			else {
				exact = 0;
			}
			estimated = ss.bound(x);
			if (overflows.containsKey(x)) {
				estimated += overflows.get(x)*(niters/size);
			}
			if (estimated > (niters/size)) {
				estimated -= niters/size;
			}
			//System.out.println(ss.toString(Integer.SIZE - Integer.numberOfLeadingZeros(niters-1)));
			//System.out.println("Stats for "+x+": "+exact+" "+estimated+" "+ss.bound(x));
			assertTrue(exact <= estimated);
			assertTrue(exact + ss.error(x) >= estimated);
			
		}
	}

	@Test
	public void testAddTwice() {
		int n = 1<<10;
		int size = 1<<5;
		SpaceSaving ss = new SpaceSaving(n, size, 10);
/*		ss.inc(123);
		assertEquals("1: 123", ss.toString());
		ss.inc(123);
		assertEquals("2: 123", ss.toString());
*/
		n = 512;
		size = 64;
		ss = new SpaceSaving(n, size, 10);
		ss.inc(2);
		assertEquals("1: 2", ss.toString());
		ss.inc(3);
		assertEquals("1: 3 2", ss.toString());
		//ss.inc(0);
		//assertEquals("1: 0 123 93", ss.toString());
		ss.inc(3);
		assertEquals("1: 2\n2: 3", ss.toString());
		System.out.print('.');
		
		
		n = 512;
		size = 64;
		ss = new SpaceSaving(n, size, 10);
		ss.inc(93);
		assertEquals("1: 93", ss.toString());
		ss.inc(123);
		assertEquals("1: 123 93", ss.toString());
		//ss.inc(0);
		//assertEquals("1: 0 123 93", ss.toString());
		ss.inc(123);
		assertEquals("1: 93\n2: 123", ss.toString());

		
		n = 512;
		size = 64;
		ss = new SpaceSaving(n, size, 10);
		ss.inc(93);
		assertEquals("1: 93", ss.toString());
		ss.inc(52);
		assertEquals("1: 52 93", ss.toString());
		ss.inc(26);
		assertEquals("1: 26 52 93", ss.toString());
		ss.inc(42);
		assertEquals("1: 42 26 52 93", ss.toString());
		ss.inc(123);
		assertEquals("1: 123 42 26 52 93", ss.toString());
		ss.inc(0);
		assertEquals("1: 0 123 42 26 52 93", ss.toString());
		ss.inc(123);
		assertEquals("1: 0 93 42 26 52\n2: 123", ss.toString());

		
	
	}
	
	@Test
	public void testTwoTwice() {
		int n = 1<<10;
		int size = 1<<5;
		SpaceSaving ss = new SpaceSaving(n, size, 10);
		ss.inc(3);
		ss.inc(4);
		ss.inc(3);
		ss.inc(4);
		assertEquals(2, ss.bound(4));
	}
	

	
	/**
	 * Test method for {@link il.technion.ewolf.BloomFilters.Tools.SlidingWindow.SpaceSaving#inc(long)}.
	 */
	@Test
	public void testInc() {
		long key = 611;
		int n = 1<<10;
		int size = 1<<5;
		SpaceSaving ss = new SpaceSaving(n, size, 10);
		// increment same key size-1 times and make sure there are no overflows.
		for (int i=0; i < size - 1;++i) {
			System.out.println(i);
			assertEquals(ss.bound(key), i);
			assertFalse(ss.inc(key));
		}
		// increment once more and assert that there is an overflow.
		assertTrue(ss.inc(key));
		assertEquals(ss.bound(key), size);
	}
	

	/**
	 * Test method for {@link il.technion.ewolf.BloomFilters.Tools.SlidingWindow.SpaceSaving#getMin()}.
	 */
	@Test
	public void testGetMin() {
		long key = 611;
		int n = 1<<10;
		int size = 1<<5;
		SpaceSaving ss = new SpaceSaving(n, size, 10);
		assertTrue(ss.getMin() == 0);
		assertFalse(ss.inc(key));
		assertTrue(ss.getMin() == 1);
	}

	/**
	 * Test method for {@link il.technion.ewolf.BloomFilters.Tools.SlidingWindow.SpaceSaving#error(long)}.
	 */
	@Test
	public void testError() {
		long key = 611;
		int n = 1<<10;
		int size = 1<<5;
		SpaceSaving ss = new SpaceSaving(n, size, 10);
		assertTrue(ss.getMin() == ss.error(5));
		assertFalse(ss.inc(key));
		assertTrue(ss.getMin() == ss.error(7));
	}

	/**
	 * Test method for {@link il.technion.ewolf.BloomFilters.Tools.SlidingWindow.SpaceSaving#clear()}.
	 */
	@Test
	public void testClear() {
		long key = 611;
		int n = 1<<10;
		int size = 1<<5;
		SpaceSaving ss = new SpaceSaving(n, size, 10);
		assertFalse(ss.inc(key));
		assertTrue(ss.containsKey(key));
		ss.clear();
		assertFalse(ss.containsKey(key));
	}

}

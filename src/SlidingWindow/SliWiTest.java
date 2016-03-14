/**
 * 
 */
package SlidingWindow;

import static org.junit.Assert.*;

import java.util.Arrays;
import java.util.Random;

import org.junit.Test;

/**
 * @author Yaron
 *
 */
public class SliWiTest {
	
	final static private int niters = 1<<20;
	
	@Test
	public void testSameNumber() {
		int windowSize = 16;
		int keySize = 3;
		SliWi sliwi = new SliWi(4,windowSize, keySize);
		int[] window = new int[windowSize];
		int x;
		double exact, estimated;
		for (int i=0; i<niters; ++i) {
			// generate a random number
			x = 1;
			// count it
			window[i%windowSize] = x;
			sliwi.push(x);
			// query
			x = 1;
			// count it in window
			exact = 0;
			for (int j=0; j<windowSize; ++j) {
				if (window[j] == x) {
					exact += 1;
				}
			}
			estimated = sliwi.get(x);
			//System.out.println("for "+x+" exact: "+exact+", estimated: "+estimated+", allowed error: "+sliwi.error(x));
			if (!((exact - sliwi.error(x) <= estimated) &&
					( estimated <= exact + sliwi.error(x)))) {
				System.out.println("for "+x+" exact: "+exact+", estimated: "+estimated+", allowed error: "+sliwi.error(x));
				System.err.println(sliwi);
				estimated = sliwi.get(x);
				fail();
			}
		}
	}
	
	@Test
	public void test() {
		int windowSize = 64;
		int blockSize = 8;
		int keySize = 3;
		int nKeys = 1<<keySize;
		SliWi sliwi = new SliWi(blockSize, windowSize, keySize);
		Random rand = new Random(1);
		int[] window = new int[windowSize];
		Arrays.fill(window,0,windowSize,-1);
		int x;
		double exact, estimated;
		for (int i=0; i<niters; ++i) {
			// generate a random number
			x = rand.nextInt(nKeys);
			// count it
			window[i%windowSize] = x;
			sliwi.push(x);
			// query a random number
			x = rand.nextInt(nKeys);
			// count it in window
			exact = 0;
			for (int j=0; j<windowSize; ++j) {
				if (window[j] == x) {
					exact += 1;
				}
			}
			estimated = sliwi.get(x);
			
			if (!((exact - sliwi.error(x) <= estimated) &&
					( estimated <= exact + sliwi.error(x)))) {
				System.err.println("for "+x+" exact: "+exact+", estimated: "+estimated+", allowed error: "+sliwi.error(x));
				System.err.println(sliwi);
				estimated = sliwi.get(x);
				fail();
			}
			else {
				//System.out.println(Arrays.toString(window));
				//System.out.println(sliwi);
			}
		}
	}
}

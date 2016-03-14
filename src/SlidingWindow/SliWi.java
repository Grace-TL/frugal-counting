/**
 * The main class. 
 * Implementation of a sliding window approximate counting data structure.
 */
package SlidingWindow;

import HashTables.TinyTableWithValues;


import java.util.BitSet;


//import org.apache.commons.collections4.queue.CircularFifoQueue;

/**
 * @author Yaron Kassner
 *
 */
public class SliWi {
	private int t;
	private final int blockSize;
	private final int windowSize;
	private final int nBlocks;
	private BitSet index; // set bit means end of block is not reached yet
	private int tail;
	private int nOverflows;
	private int indexTail;
	private int head;
	private int indexHead;
	private TinyTableWithValues totalOverflows;
	private long[] overflows; // elements that overflowed.
	private SpaceSaving spaceSaving;
	final private int indexSize;
	private int maxOverflows;
	final private static double ALPHA = 0.2;
	
	public SliWi(int blockSize, int windowSize, int keySize){
		
		this.blockSize = blockSize;
		this.windowSize = windowSize;
		this.nBlocks = windowSize / blockSize;
		assert(Integer.bitCount(nBlocks) == 1);
		// nBlocks for first space savings and nBlocks for second
		this.maxOverflows = nBlocks * 2; 
		this.indexSize = maxOverflows + nBlocks;
		t = tail = indexTail = nOverflows = 0;
		head = maxOverflows-1;
		indexHead = nBlocks-1; // index starts with nBlocks ends of blocks.
		index = new BitSet(indexSize);
		overflows = new long[maxOverflows];
		spaceSaving = new SpaceSaving(windowSize, nBlocks, keySize);
		
		totalOverflows = new TinyTableWithValues(Math.min(maxOverflows, 1<<keySize), keySize, 
				Integer.SIZE - Integer.numberOfLeadingZeros(maxOverflows-1), ALPHA);
		
	}
	
	long getSpace() {
		return indexSize/8+overflows.length*Long.SIZE/8+spaceSaving.getSpace()+totalOverflows.getSpace();
	}
	
	long getSpaceSavingSpace() {
		return spaceSaving.getSpace();
	}
	
	long getbSpace() {
		return indexSize/8+overflows.length*Long.SIZE/8;
	}
	
	long getBSpace() {
		return totalOverflows.getSpace();
	}
	/**
	 * Enter a new element into the sliding window.
	 * @param x element to enter 
	 */
	public void push(long x)
	{	
		// TICK
		// if block is ending
		if ((++t) % blockSize == 0) {
			assert(index.get(indexTail) == false);
			// remove end of oldest block
			//index.clear(indexTail);
			indexTail = (indexTail + 1)%indexSize;
		}
		if (t == windowSize)
		{
			// starting new frame
			t = 0;
			spaceSaving.clear();
		}
		if (t%blockSize == 0) {
			// if block is starting
			indexHead = (indexHead + 1)%indexSize;
			index.clear(indexHead);
		}

		// MAKE ROOM
		// remove oldest element in oldest block if exists
		if (index.get(indexTail)) {
			
			long exiting = overflows[tail];
			short newValue = (short) (totalOverflows.get(exiting) - 1);
			if (newValue == 0) totalOverflows.remove(exiting);
			else totalOverflows.put(exiting, newValue);
			tail = (tail+1)%maxOverflows;
			--nOverflows;
			indexTail = (indexTail + 1) % indexSize;
		}
		
		// ADD
		// add x to space savings
		boolean overflowed = spaceSaving.inc(x);
		// add x to overflows if necessary
		if (overflowed) {
			head = (head+1)%maxOverflows;
			++nOverflows;
			assert(nOverflows <= maxOverflows); // head should never catch up with tail
			overflows[head] = x;
			indexHead = (indexHead + 1) % indexSize;
			index.set(indexHead);
			// count new overflow
			totalOverflows.put(x, 
					(short) (totalOverflows.getOrDefault(x, (short) 0)+1));
		}
		

	}
	

	/**
	 * estimate frequency in sliding window
	 * @param x the object to estimate
	 * @return estimated frequency
	 */
	public double get(long x)
	{
		long minimumOverflows = totalOverflows.getOrDefault(x, 0);
		// We might have removed one overflow prematurely so we should consider it.
		double overflowsEstimate = blockSize * minimumOverflows + (blockSize-1) / 2.;
		// There could have been less than this amount of overflows because maybe 
		// blockSize-1 arrived just before.
		overflowsEstimate -= (blockSize-1)/2.;
		// There could also have been more than this amount because space savings 
		// could have flushed right before another overflow
		overflowsEstimate += (blockSize-1)/2.;
		// SpaceSavings only bounds the number of overflows. 
		// We could get an error of up to (N-t)/nblocks
		overflowsEstimate -= (double) Math.ceil((windowSize-t)/nBlocks)/2.;
		// The space-savings error in the new frame adds an error of t/nblocks
		overflowsEstimate -= (double) Math.ceil(t/nBlocks)/2.;
		// we should use the info we have from space savings about the current block.
		int ssEstimate = spaceSaving.bound(x);
		if (minimumOverflows > 0) {
			ssEstimate = ssEstimate%blockSize;	
		}
		overflowsEstimate += ssEstimate;
		
		return  overflowsEstimate;
	}
	
	/**
	 * Calculate the maximum error of the estimation of x.
	 * @param x
	 * @return if the return value is e then the real value of x lies in 
	 * [ceil(get(x)-e), floor(get(x)+e)]
	 */
	public double error(long x) {
		return 3*(blockSize-1) / 2. + 
				Math.ceil(t/nBlocks)/2.+
				Math.ceil((windowSize-t)/nBlocks)/2.;
	}
	
	public String toString() {
		StringBuilder sb = new StringBuilder();
		sb.append("t: "+t+"\n");
		sb.append("B:\n");
		int j=tail, i=indexTail;
		do  {
			if (index.get(i)) {
				sb.append(" "+overflows[j]);
				j = (j+1)%maxOverflows;
			}
			else {
				// end of block
				sb.append(" |");
			}
			i = (i+1)%indexSize;
		} while (i!=indexHead);
		
		if (index.get(i)) {
			sb.append(" "+overflows[j]);
			j = (j+1)%maxOverflows;
		}
		else {
			// end of block
			sb.append(" |");
		}
		
		sb.append("\n");
		sb.append("Space Saving:\n");
		sb.append(spaceSaving.toString());
		return sb.toString();
	}
}

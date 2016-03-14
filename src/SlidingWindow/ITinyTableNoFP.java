package SlidingWindow;

public interface ITinyTableNoFP {
	// Only in second table
	/**
	 * Gets an element according to its key and index in the chain.
	 * This function is for the situation where no finger-print exists.
	 * In that case a key may be insufficient to uniquely identify an element.
	 * It can only identify a chain.
	 * @param key
	 * @param indexInChain
	 * @return value of element or -1 if not found.
	 */
	long get(long key, int indexInChain); // maybe replace with while
	long getChainSize(long key); // cannot be easily computed
	/**
	 * @param key
	 * Add the value according to key, EVEN if the key already exists.
	 * @param value
	 */
	void add(long key, long value);
	/**
	 * Decrement the specified element in the specified index in the chain to the new value. 
	 * If key does not exist, throw exception. 
	 * @param key
	 */
	void decrement(long key, int indexInChain);
	void remove(long key, int indexInChain);
	
	// In both tables
	long getBucketChainID(long key);
	
}

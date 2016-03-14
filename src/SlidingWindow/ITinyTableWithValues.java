/**
 * 
 */
package SlidingWindow;


/**
 * @author kassnery
 * Should also be able to work with finger-print of size zero.
 */
public interface ITinyTableWithValues {
	
	// Only in first table
	long get(long key);
	long getOrDefault(long key, long def);
	/**
	 * Add the value according to key, and replace it if it already exists.
	 * @param key
	 * @param value
	 */
	void put(long key, long value);
	
	/**
	 * Replace the records with specified chain-ID and the old value to the new value. 
	 * If key does not exist, throw exception. 
	 * @param key
	 * @return 
	 */
	boolean replaceByValue(long chainID, long oldValue, long newValue);
	
	/**
	 * Remove the records with specified key from hash table. 
	 * If key does not exist, do nothing. 
	 * @param key
	 */
	void remove(long key);
	
	/**
	 * Remove the record with specified chain ID and value.
	 * If there is none, do nothing. 
	 * @param hash
	 * @return true if succeeded false, otherwise. 
	 */
	boolean removeByValue(long chainID, long value);
	
	boolean containsKey(long key);

	// In both tables
	long getBucketChainID(long key);
	
}

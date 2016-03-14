/**
 * 
 */
package SlidingWindow;

import java.io.IOException;

import BitArray.WordArray;
import HashFunctions.FingerPrintAux;
import HashTables.TinyTableWithValues;

/**
 * @author Yaron Kassner
 * n/size must fit int.
 */
public class SpaceSaving {
	private TinyTableWithValues indices;
	private TinyTableWithValues lastsOfQuantity;
	//private long[] chainIndices;
	private WordArray chainIndices;
	private WordArray quantities;
	private int argmin; // the index of the next element to add
	final private int n; // total number of elements to count
	final private int size;
	final private static double ALPHA = 0.2;
	final private int keySize;
		
	/**
	 * overflows every n/size items.
	 * @param n - how many items to expect before clear (window size).
	 * @param size - number of heavy hitters to keep.
	 * @throws IOException 
	 */
	SpaceSaving(int n, int size, int keySize) {
		// check that the variables are not to big
		assert(size <= (1L<<31));
		assert(n/size <= (1L<<31));
		int indexSize = Integer.SIZE-Integer.numberOfLeadingZeros(size-1);
		this.keySize = keySize; 
		int itemsInTable = 1<<(Integer.SIZE-Integer.numberOfLeadingZeros(size-1)); // size rounded up
		indices = new TinyTableWithValues(itemsInTable, keySize, indexSize, ALPHA);
		
		
		
		int valueSize = Integer.SIZE-Integer.numberOfLeadingZeros(n-1);
		itemsInTable = 1<<(Integer.SIZE-Integer.numberOfLeadingZeros(Math.min(size, n/size)-1));
		
		lastsOfQuantity = new TinyTableWithValues(itemsInTable, valueSize, indexSize, ALPHA);
		
		this.n = n;
		this.size = size;
		long nChains = indices.getNumberOfChains();
		int chainIndexSize = Long.SIZE-Long.numberOfLeadingZeros(nChains-1);
		chainIndices = new WordArray(size, chainIndexSize);
		int quantitySize = Long.SIZE-Long.numberOfLeadingZeros((long) (2*Math.ceil(n/size)-1));
		quantities = new WordArray(size,quantitySize);
		clear();
	}
	
	long getSpace() {
		//System.out.println(quantities.getSpace()+ " "+indices.getSpace()+" " + lastsOfQuantity.getSpace() 
		//		+ " " + chainIndices.getSpace());
		return quantities.getSpace()+ indices.getSpace() + lastsOfQuantity.getSpace() + chainIndices.getSpace();
	}
	
	SpaceSaving(int n, int size) {
		this(n,size, 32);
	}
	
	public boolean containsKey(long e) {
		return (indices.containsKey(e) && indices.get(e) >= argmin);
	}

	boolean indexMatchesQuantity(int index, int quantity) {
		assert (quantity < n/size);
		if (argmin > index) {
			return false;
		} else if (quantities.get(index) == quantity) {
			if (index + 1 < size) {
				return quantities.get(index + 1) > quantity;
			}
			else {
				return true;
			}
		} else {
			return false;
		}
	}
	
	/**
	 * Bound the quantity of e in the current frame. 
	 * The bound is no more than n/size greater than the real quantity.
	 * @param e
	 * @return the bound
	 */
	public int bound(long e) {
		if (containsKey(e)) {
			return (int) quantities.get((int) indices.get(e));
		} else {
			return getMin();
		}
	}
	
	public int getMin() {
		if (argmin == size) {
			return 0;
		} else {
			return (int) quantities.get(argmin);
		}
	}
	
	/**
	 * The maximum -error of the valuation of e. 
	 * @param e
	 * @return if the return value is r then the real value is in [bound(e)-r, bound(e)]
	 */
	public int error(long e) {
		return getMin();
	}
	
	private int getQuantityIndexInChain(int quantity, FingerPrintAux quantityHash) {
		assert(quantity < n/size);
		long chainSize = lastsOfQuantity.getChainSize(quantityHash);
		int index;
		for (int i=0; i<chainSize; ++i) {
			index = (int) lastsOfQuantity.readyGet(quantityHash.bucketId, i);
			//System.out.println("Checking index "+index+" for quantity "+quantity);
			if (indexMatchesQuantity(index, quantity)) {
				return i;
			}
		}
		return -1;
	}
	
	/**
	 * Get the last index of the specified quantity
	 * @param quantity
	 * @return last index if exists. Otherwise -1.
	 */
	private int getLastOfQuantity(int quantity, FingerPrintAux quantityHash) {
		int i = getQuantityIndexInChain(quantity, quantityHash);
		if (i!=-1) {
			return (int) lastsOfQuantity.readyGet(quantityHash.bucketId, i);
		}
		else {
			return -1;
		}
		/*
		assert(quantity < n/size);
		long chainSize = lastsOfQuantity.getChainSize(quantityHash);
		for (int i=0; i<chainSize; ++i) {
			int index = (int) lastsOfQuantity.readyGet(quantityHash.bucketId, i);
			//System.out.println("Checking index "+index+" for quantity "+quantity);
			if (indexMatchesQuantity(index, quantity)) {
				return index;
			}
		}
		// quantity should exist. Should not get here.
		return -1;
		*/
	}
	
	/**
	 * Remove the last index of the specified quantity if it exists.
	 * @param quantity
	 */
	private void removeLastOfQuantity(int quantity, FingerPrintAux quantityHash) {
		int i = getQuantityIndexInChain(quantity, quantityHash);
		if (i!=-1) {
			// TODO change to ready remove?
			lastsOfQuantity.remove(quantityHash.bucketId, quantityHash.chainId, i); 
		}
		/*
		//TODO can access chain directly.
		assert(quantity < n/size);
		long chainSize = lastsOfQuantity.getChainSize(quantityHash);
		for (int i=0; i<chainSize; ++i) {
			int index = (int) lastsOfQuantity.readyGet(quantityHash.bucketId, i);
			if (indexMatchesQuantity(index, quantity)) {
				lastsOfQuantity.remove(quantityHash.bucketId, quantityHash.chainId, i);
				return;
			}
		}
		*/
	}
	
	/**
	 * Remove the last index of the specified quantity
	 * @param quantity
	 * @return true if successful, otherwise false.
	 */
	private boolean decrementLastOfQuantity(int quantity, FingerPrintAux quantityHash) {
		int i = getQuantityIndexInChain(quantity, quantityHash);
		if (i!=-1) {
			lastsOfQuantity.readyDecrement(quantityHash, i);
			return true;
		}
		else {
			return false;
		}
		/*
		assert(quantity < n/size);
		long chainSize = lastsOfQuantity.getChainSize(quantityHash);
		for (int i=0; i<chainSize; ++i) {
			int index = (int) lastsOfQuantity.readyGet(quantityHash.bucketId, i);
			if (indexMatchesQuantity(index, quantity)) {
				lastsOfQuantity.decrement((long) quantity, i);
				return true;
			}
		}
		return false;
		*/
	}
	
	/**
	 * Add the new index as the last of the specified quantity, but only if it does not exist yet.
	 * @param quantity 
	 */
	void addNewQuantity(int quantity,  int newIndex) {
		/*
		assert(quantity < n/size); // should not add a heavy hitter quantity
		
		FingerPrintAux quantityHash = lastsOfQuantity.getHash(quantity);
		int chainSize = (int) lastsOfQuantity.getChainSize(quantity);
		for (int i=0; i<chainSize; ++i) {
			int index = (int) lastsOfQuantity.readyGet(quantityHash.bucketId, i);
			if (indexMatchesQuantity(index, quantity)) {
				return;
			}
		}
		*/
		FingerPrintAux quantityHash = lastsOfQuantity.getHash(quantity);
		
		/*
		int i = getQuantityIndexInChain(quantity, quantityHash);
		if (i!=-1) {
			return;
		}
		else {*/
			// TODO should I use the fact that I know the chain is empty now?
			// TODO replace with readyAdd
		lastsOfQuantity.add(quantityHash, newIndex); 
		assert(getLastOfQuantity(quantity, quantityHash)==newIndex);
		//}
		/*
		lastsOfQuantity.add(quantity, newIndex);
		assert(getLastOfQuantity(quantity, quantityHash)==newIndex);
		*/
	}
	
	/**
	 * make room for one more key
	 */
	void makeRoom() {
		int quantity = (int) quantities.get(--argmin);
		// remove this index from last of quantity if necessary
		if (quantity < n/size) {
			
			FingerPrintAux quantityHash = lastsOfQuantity.getHash(quantity);
			long chainSize = lastsOfQuantity.getChainSize(quantityHash);
			for (int i=0; i< chainSize;++i) {
				int index = (int) lastsOfQuantity.readyGet(quantityHash.bucketId, i);
				if (index == argmin) {
					// TODO replace with readyRemove
					lastsOfQuantity.remove(quantityHash.bucketId, quantityHash.chainId, i); 
					break;
				}
			}
			
		}
		quantities.put(argmin, 0);
		FingerPrintAux quantityHash = lastsOfQuantity.getHash(0);
		lastsOfQuantity.add(quantityHash, argmin); 
		//addNewQuantity((int) 0, argmin);
	}
	
	
	/**
	 * Put the key into the specified index. If index exists, leave quantity as is.
	 * @param key
	 * @param index
	 */
	private void put(FingerPrintAux hash, int index) {

		assert(index >= 0);
		assert(index >= argmin - 1);
		if (index == argmin-1) {
			makeRoom();
		}
		long oldChainID = chainIndices.get(index);
		//long newChainID = indices.getBucketChainID(key);
		long newChainID = indices.getBucketChainID(hash);
		// if needed, remove what's in the old index.
		/*if (oldChainID == newChainID) {
			// all of this if is new remove it if something is not working
			// Same chain as before. Only need to replace fingerprint.
			indices.putByValue(key, index);
		}
		else {*/
		// switched chains. Need to remove old index and enter new one.
		//if (quantities[index] > 0) {
		//index needs to be removed
		indices.removeByValue(oldChainID, index);
		//}
		// write down key's index.
		indices.put(hash, index);
		//}
		// write down the chain index.
		chainIndices.put(index, newChainID);
	}
	
	
	
	/**
	 * put key1 at index2 and what's in index2 at key1's location.
	 * @param key1
	 * @param index2
	 */
	private void replace(FingerPrintAux hash1, int index2) {
		int index1 = (int) indices.get(hash1);
		long chain2 = chainIndices.get(index2);
		chainIndices.put(index1, chain2);
		indices.replaceByValue(chain2, index2, index1);
		indices.put(hash1, index2);		
		chainIndices.put(index2, indices.getBucketChainID(hash1));
	}
	
	/**
	 * Increment the quantity at the given index. Should be called only once the key is in place.
	 * @param index
	 * @return the new quantity
	 */
	private int incrementQuantity(int index, FingerPrintAux quantityHash) {
		int quantity = (int) quantities.get(index);
		//FingerPrintAux quantityHash = lastsOfQuantity.getHash(quantity);
		int maxQuantity = n/size;
		if (quantity < maxQuantity) {
			// update last of old quantity
			if (index == argmin || quantities.get(index - 1)!=quantity){
				// old quantity is no longer needed
				removeLastOfQuantity(quantity, quantityHash);
			} else {
				decrementLastOfQuantity(quantity, quantityHash);
			}	
		}
		
		// compute new quantity
		if (++quantity == 2*(maxQuantity)) {
			quantity = (int) (maxQuantity);
		}
		// write down the quantity
		quantities.put(index, quantity);
		
		
		// if the next quantity does not exist yet
		if ((quantity < maxQuantity) && ((index + 1 >= size) || (quantities.get(index+1) != quantity))) {
			// last of this quantity should not exist. 
			// TODO But what if the lastOfQuantity is there from deleted table entrees?
			//assert(!lastsOfQuantity.containsKey(quantity));
			quantityHash = lastsOfQuantity.getHash(quantity);
			// TODO problem here. adding 2 even though it is already in the hash (next index is out of table bounds).
			lastsOfQuantity.put(quantityHash, index); 
		}
		
		/*
		// update lasts of quantity if this was the first of this quantity.
		if (quantity < n/size ){
			// no need for: && getLastOfQuantity(quantity, newQuantityHash) == -1) { (checked in next function)
			addNewQuantity(quantity, index);
		}
		*/
		
		return quantity;
	}
	/*
	private int getNewIndex(long key) {
		// find location for key - current location if exists, 
		// or place of smallest number.
		int index = Math.max(0, argmin - 1);
		if (indices.containsKey(key)) {
			// key exists. If it is not up to date, set index to argmin - 1.
			int ni = (int) indices.get(key);
			if (ni > index) {
				index = ni;
			}
		}
		return index;
	}
	*/
	
	/**
	 * increment the value of key by 1.
	 * @param key element to increment
	 * @return true if there was an overflow 
	 */
	public boolean inc(long key) {
		// find location for key
		int index = 0;
		FingerPrintAux hash = indices.getHash(key);
		// find current location of key in set
		index = (int) indices.getOrDefault(hash, -1);
		// if key is not in set
		if (index < argmin) {
			index = Math.max(0, argmin-1);
			// put key in place
			this.put(hash, index); 
		}
		//	STATE: key exists now.
		assert (indices.get(hash) == index):"error";
		
		// replace key with the last of its quantity
		int quantity = (int) quantities.get(index);
		
		FingerPrintAux quantityHash = lastsOfQuantity.getHash(quantity);

		int last;
		if (quantity < n/size) {
			last = getLastOfQuantity(quantity, quantityHash);
			assert(last != -1);
			/*
			if (last == -1) {
				// should be able to find old quantity
				getLastOfQuantity(quantity, quantityHash);
				throw new RuntimeException(""+key);
			}*/
			this.replace(hash, last);
			//	STATE: key is the last of its quantity
			assert(indices.get(hash) == getLastOfQuantity(quantity, quantityHash));
		}
		else {
			last = index;
		}
		// increment quantity
		quantity = incrementQuantity(last, quantityHash);
		//	STATE: data structure ready.
		//	return true if there was an overflow.
		return quantity == n/size;
	}
	/*
	public boolean oldInc(long e) {
		int index,newIndex;
		
		int quantity;
		// find the current index and quantity for the key.		
		if (containsKey(e)) {
			// key exists. find next position
			index = (int) indices.get(e);
			quantity = quantities[index];
			// TODO move this out
			if (quantity < n/size) { 
				newIndex = getLastOfQuantity(quantity);
				assert(newIndex != -1);
			} else {
			// no need to change index because it is a heavy hitter.
				newIndex = index;
			}
		}
		else {
			// key does not exist.
			
			if (argmin > 0) {
				// there is still enough room for a new element
				--argmin;
				// make sure current quantity is set to zero.
				quantity = 0;
				newIndex = argmin;
			}
			else {
				// element should replace smallest element
				quantity = quantities[argmin];
				newIndex = getLastOfQuantity(quantity);
				if (newIndex == -1) {
					//System.out.println("Quantity "+quantity+" does not exist.");
				}
			}
			index = argmin;
			indices.removeByValue(chainIndices[argmin],argmin);
			// write down new index for key
			indices.put(e, newIndex);
			// write down new chainIndex for key
			chainIndices[newIndex] = indices.getBucketChainID(e);			
		}
		
		if (quantity < n/size) {
			FingerPrintAux quantityHash = lastsOfQuantity.getHash(quantity);
			if (index == newIndex){
				if (index == argmin || quantities[index - 1]!=quantity){
					// old quantity is no longer needed
					removeLastOfQuantity(quantity, quantityHash);
				}
				else {
					decrementLastOfQuantity(quantity);
					System.out.println(getLastOfQuantity(quantity));
				}		
			} else {
				// REPLACE THE ELEMENT WITH THE LAST OF ITS VALUE				
				long chainIndex = chainIndices[newIndex];
				// update indices
				indices.replaceByValue(chainIndex, newIndex, index);
				indices.put(e, newIndex);
				// update inverse mapping
				chainIndices[index] = chainIndex;
				chainIndices[newIndex] = indices.getBucketChainID(e);
				// update lasts of quantity
				decrementLastOfQuantity(quantity);
			}			
		}
		
		// update quantity
		++quantity;
		boolean overflow = false;
		if (quantity == 2*(n/size)) {
			quantity = (int) (n/size);
			overflow = true;
		}
		else if (quantity == n/size) {
			overflow = true;
		}
		quantities[newIndex] = quantity;
		// update lasts of quantity if this was the first of this quantity.
		if (quantity < n/size && getLastOfQuantity(quantity) == -1) {
			addNewQuantity(quantity, newIndex);
		}
				
		return overflow;
	}
	*/
	/**
	 * make room for more data. Remove data from last window.
	 */
	void clear() {
		argmin = size;
	}
	
	public String toString() {
		// arrange all keys by index
		long[] keys = new long[size - argmin];
		int index;
		for (long k=0;k<(1L<<keySize);++k) {
			if (indices.containsKey(k)) {
				index = (int) indices.get(k);
				if (index >= argmin) {
					keys[index-argmin] = k;
				}
			}
		}
		
		StringBuilder res = new StringBuilder();
		int quantity, oldQuantity = -1;
		for (int i=argmin; i<size; ++i) {
			quantity = (int) quantities.get(i);
			if (quantity != oldQuantity) {
				if (oldQuantity != -1) {
					res.append("\n");
				}
				res.append(quantity+":");
				oldQuantity = quantity;
			}
			res.append(" "+keys[i-argmin]);
		}
		return res.toString();
	}
}

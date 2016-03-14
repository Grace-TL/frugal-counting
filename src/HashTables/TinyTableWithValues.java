package HashTables;

import BitArray.SimpleBitwiseArray;
import HashFunctions.FingerPrintAux;
import HashFunctions.TrivialHashMaker;
import SlidingWindow.ITinyTableNoFP;
import SlidingWindow.ITinyTableWithValues;

import java.io.IOException;


/**
 * fingerprint is actually tag|value
 * @author kassnery
 *
 */
public class TinyTableWithValues extends SimpleBitwiseArray implements ITinyTableWithValues, ITinyTableNoFP
{
	// first level index. 
	protected int nrItems;
	public long I0[];
	public long[] IStar;
	public int[] A;
	private final long fpMask;
	private final long valueMask;
	private final int valueSize; // size of value in bits.


	private final static int NCHAINS_PER_BUCKET = 64;
	// Really small so we will never have 64 items in the same logical bucket. 
	//private final static double AVG_ITEMS_PER_BUCKET = 4; 
	// Really small so we will never have 64 items in the same logicalbucket. 
	private final static double AVG_ITEMS_PER_BUCKET = 8; 

	TrivialHashMaker hashFunc;

	/**
	 * 32*(1+alpha) items on average per bucket.
	 * nrBuckets = nrItems / 32
	 * bucketCapactity = 40
	 * 64 chains per bucket.
	 * Construct the table
	 * @param nrBuckets - number of buckets.
	 * @param bucketcapacity - number of items in each bucket
	 * @param fpSize - size of finger print; for no collisions choose keysize - log(nrBuckets) - 6
	 * @param valueSize - size of value
	 * @throws IOException 
	 */
	private TinyTableWithValues(int nrBuckets, int bucketcapacity, int fpSize, int valueSize ) 
	{
		super(bucketcapacity*nrBuckets, valueSize + fpSize, bucketcapacity);
		assert(valueSize+fpSize <= Long.SIZE):"cannot fit "+valueSize+"+"+fpSize+" into long";
		itemSize = valueSize + fpSize;
		//this.maxAdditionalSize = 0;
		this.valueSize =  valueSize;
		this.nrItems = 0;
		I0 = new long[nrBuckets];
		IStar = new long[nrBuckets];
		A = new int[nrBuckets];
		assert(fpSize+Long.SIZE - Long.numberOfLeadingZeros(nrBuckets*NCHAINS_PER_BUCKET-1) < Long.SIZE):"ERRORRR";
		hashFunc = new TrivialHashMaker(fpSize, nrBuckets); // constantly NCHAINS_PER_BUCKET
		fpMask = ((1l<<fpSize)-1)<<valueSize;
		valueMask = (1l<<valueSize)-1;
		assert(bucketcapacity < 128);
		this.BucketCapacity = bucketcapacity;
	}
	
	public long getNumberOfChains() {
		return I0.length*NCHAINS_PER_BUCKET;
	}
	
	public long getSpace() {
		return (I0.length * Long.SIZE+IStar.length*Long.SIZE
				+A.length*Integer.SIZE+words.length*Long.SIZE)/8;
	}

	/**
	 * Construct 
	 * @param nrItems - maximum number of items to be in the hash table at any given time.
	 * @param keySize - size of a key
	 * @param valueSize - size of a value
	 * @param alpha - slack factor
	 */
	public TinyTableWithValues(int nrItems, int keySize, int valueSize, double alpha) {
		this( (int) Math.ceil(nrItems / AVG_ITEMS_PER_BUCKET), // number of buckets
				(int) Math.ceil(AVG_ITEMS_PER_BUCKET *(1+alpha)), // bucket capacity
				Math.max(1, keySize+1 - Integer.SIZE + Integer.numberOfLeadingZeros( 
						(int) Math.ceil(nrItems / AVG_ITEMS_PER_BUCKET) 
						* NCHAINS_PER_BUCKET  - 1)), // finger-print size
						valueSize // value size
				);
		assert(Long.bitCount(nrItems)==1):"Number of items must be a power of two.";
		assert(keySize< 63);
		//TODO
		
		//assert(this.keySize + this.valueSize< 64);
		assert(nrItems <= (1L<<keySize));
	}

	public void add(long item, long value)
	{

		//FingerPrintAux fpaux = ;
		this.add(hashFunc.createHash(item), value);	
	}


	public void remove(long item)
	{

		FingerPrintAux fpaux = hashFunc.createHash(item);
		this.remove(fpaux);	
	}

	public boolean containsKey(long item)
	{
		//		FingerPrintAux fpaux = ;
		//System.out.println(fpaux.toString());
		return this.containsKey(hashFunc.createHash(item));
	}

	/*
	public long howmany(int bucketId, int chainId,long fingerprint)
	{

		long[] chain = this.getChain(bucketId, chainId);
		return ChainHelper.howmany(chain, fingerprint, this.itemSize-1);


	}
	 */

	/**
	 * Get offset of bucket start.
	 */
	@Override
	public int getBucketStart(int bucketId)
	{
		return this.bucketBitSize*bucketId + this.A[bucketId]*this.itemSize;
	}

	/*
	 *    Shitty but simple solution... the difficult one is to flip the representation of IStar so Popcnt works. 
	 * 
	 * (non-Javadoc)
	 * @see il.technion.ewolf.BloomFilters.Tools.BitArray.SimpleBitwiseArray#getNrItems(int)
	 */
	@Override
	public int getNrItems(int bucketId)
	{

		return (64- Long.numberOfLeadingZeros(IStar[bucketId]));


	}


	/**
	 * Adds a new fingerPrint to the following bucketNumber and chainNumber, the maximal size 
	 * of supported fingerprint is 64 bits, and it is assumed that the actual data sits on the LSB bits of
	 * long. 
	 * 
	 * According to our protocol, addition of a fingerprint may result in expending the bucket on account of neighboring buckets, 
	 * or down sizing the stored fingerprints to make room for the new one. 
	 * 
	 * In order to support deletions, deleted items are first logically deleted, and are fully 
	 * deleted only upon addition. 
	 * 
	 * @param bucketNumber
	 * @param chainNumber
	 * @param fingerPrint
	 */
	public void add(FingerPrintAux fpAux, long value) {
		assert(value < (1<<valueSize));
		// We do not support more than 64 items in a logical bucket at this point.
		//		assert(this.getNrItems(fpAux.bucketId) <= 64 );
		int nextBucket = this.findFreeBucket(fpAux.bucketId);
		upscaleBuckets(fpAux.bucketId,nextBucket);
		//int idxToAdd = RankIndexHashing.addItem(fpAux, I0, IStar,offsets,chain);
		int idxToAdd = TinySetIndexingTechnique.addItem(fpAux, I0, IStar);
		// if we need to, we steal items from other buckets. 
		this.putAndPush(fpAux.bucketId, idxToAdd, fpAux.fingerprint, value);
		return;
	}

	/**
	 * Remove the first item that matches this hash-value. If can't find, do nothing.
	 * @param fpaux
	 */
	protected void remove(FingerPrintAux fpaux) 
	{
		TinySetIndexingTechnique.getChain(fpaux, I0, IStar);
		int offset = findItem(fpaux);
		assert(offset>=0);
		int indexInChain = offset - TinySetIndexingTechnique.chainStart;
		remove(fpaux.bucketId, fpaux.chainId, indexInChain);
	}

	/**
	 * Find first item that matches fpaux. Assume someone called getChain
	 * @param fpaux
	 * @return item location in bucket or -1 if not found
	 */
	private int findItem(FingerPrintAux fpaux)
	{	
		int bucketStart = getBucketStart(fpaux.bucketId);
		long fpToCompare;
		for (int i=TinySetIndexingTechnique.chainStart; i<TinySetIndexingTechnique.chainEnd;i++ ) {
			fpToCompare = extractFingerPrint(this.FastGet(bucketStart, i));
			if(fpToCompare == fpaux.fingerprint)
				return i;
		}
		return -1;

	}
	/**
	 * Added this function to avoid performing 'get' twice. 
	 *  it returns items value or Long.MIN_VALUE. If the value is not found. 
	 * @param fpaux
	 * @return
	 */
	private long findItemsValue(FingerPrintAux fpaux)
	{	
		int bucketStart = getBucketStart(fpaux.bucketId);
		for (int i=TinySetIndexingTechnique.chainStart; i<TinySetIndexingTechnique.chainEnd;i++ ) {
			long item =this.FastGet(bucketStart, i);
			if(extractFingerPrint(item) == fpaux.fingerprint)
				return extractValue(item);
		}
		return Long.MIN_VALUE;

	}


	/**
	 * assumes chain is already loaded.
	 * @param fpaux
	 * @param value
	 * @return
	 */
	private int findItemByValue(int bucketId, long value)
	{	
		int bucketStart = getBucketStart(bucketId);
		for (int i=TinySetIndexingTechnique.chainStart; i<TinySetIndexingTechnique.chainEnd;i++ ) {
			long valueToCompare = extractValue(this.FastGet(bucketStart, i));
			if(valueToCompare == value)
				return i;
		}
		return -1;

	}


	private long buildRecord(long fingerPrint, long value) {
		return (fingerPrint << valueSize ) | value;
	}

	private long extractFingerPrint(long record) {
		return (record&fpMask) >> valueSize;
	}
	private long extractValue(long record) {
		return record&valueMask;
	}



	protected void removeItemFromIndex(FingerPrintAux fpaux) {
		TinySetIndexingTechnique.removeItem(fpaux,I0, IStar);
	}

	protected void removeItemFromIndex(int bucketId, int chainId) {
		TinySetIndexingTechnique.removeItem(bucketId, chainId, I0, IStar);
	}


	/**
	 * finds a the closest bucket that can accept the new item. 
	 * if the current bucket is under maximal capacity it is the current bucket, otherwise we steal fingerprints from buckets until we reach
	 * a free bucket. 
	 * @param bucketId
	 * @return
	 */
	private int findFreeBucket(int bucketId) {
		int origBucketId = bucketId;
		bucketId = bucketId%this.A.length;

		while(this.getNrItems(bucketId)+this.A[bucketId] >=this.BucketCapacity) {
			//		while(this.A[bucketId+1]!= 0)
			bucketId++;
			bucketId = bucketId%this.A.length;
			
			
			if(origBucketId == bucketId)
			{
				throw new RuntimeException("Table is full! " + this.BucketCapacity + " " + this.nrItems +" "+ this.A.length);
			}
			
		}
		return bucketId;
	}

	private void resizeBuckets(int bucketId,boolean IncrementAnchor) {
		if(!IncrementAnchor)
			return;
		this.replaceMany(bucketId, 0, 0l,this.getBucketStart(bucketId));
		this.A[bucketId]++;
		return;
	}

	/**
	 * get all the records in an entire chain 
	 * @param bucketId
	 * @param chainId
	 * @return
	 */
	/*
	protected long[] getChain(int bucketId, int chainId)
	{
		int chainSize = TinySetIndexingTechnique.getChain(bucketId, chainId, I0, IStar);
		long[] result = new long[chainSize];
		for (int i=TinySetIndexingTechnique.chainStart; i<TinySetIndexingTechnique.chainEnd; i++) {
			long item = this.Get(bucketId, i);
			result[i++] = item;
		}
		return result;
	}*/

	private void upscaleBuckets(int bucketNumber, int lastBucket)
	{
		//Bucket may be wrapped around too! 
		while(lastBucket!=bucketNumber)
		{

			resizeBuckets(lastBucket,true);


			if(--lastBucket<0)
			{
				lastBucket = A.length-1;
			}
		}
		return;

	}

	boolean containsKey(FingerPrintAux fpaux)
	{	
		TinySetIndexingTechnique.getChain(fpaux, I0, IStar);
		return (this.findItem(fpaux)>=0);
	}


	/**
	 * Put a value at location idx, if the location is taken shift the items to
	 * be left until an open space is discovered.
	 * 
	 * @param idx
	 *            - index to put in
	 * @param value
	 *            - value to put in
	 * @param item 
	 * @param mod 
	 * 				- bucket mod, (in order to decode bucket)
	 * @param size 
	 * 				- bucket item size. (in order to decode bucket)
	 * @param chainNumber 
	 */
	protected void putAndPush(int bucketId, int idx, final long fingerPrint, long value) {
		long record =  buildRecord(fingerPrint, value);
		this.replaceMany(bucketId, idx, record, this.getBucketStart(bucketId));
		this.nrItems++;
		return;
	}


	/**
	 * move all items backwards until we fill a hole in the bucket (a record that is zero).
	 * @param bucketId
	 */
	protected void removeAndShrink(int bucketId) {
		this.replaceBackwards(bucketId,this.getBucketStart(bucketId));
		return;
	}

	public int getNrItems() {

		return this.nrItems;
	}

	/**
	 * return -1 if cannot find key
	 */
	public long get(long key) {
		FingerPrintAux fpAux = hashFunc.createHash(key);
		return get(fpAux);
//		int index = findItem(fpAux);
//		
//
//		if (index != -1) {
//			long value = extractValue(this.Get(fpAux.bucketId, index));
//			return value;
//		}
//		else {
//			throw new RuntimeException();
//		}
	}
	public long get(FingerPrintAux fpAux) {
		assert(TinySetIndexingTechnique.chainExist(I0[fpAux.bucketId], fpAux.chainId));


		TinySetIndexingTechnique.getChain(fpAux, I0, IStar);
		
		long $ = this.findItemsValue(fpAux);
		assert($!=-1);
		return $;
//		int index = findItem(fpAux);
//		
//
//		if (index != -1) {
//			long value = extractValue(this.Get(fpAux.bucketId, index));
//			return value;
//		}
//		else {
//			throw new RuntimeException();
//		}
	}
	public FingerPrintAux getHash(long key) {
		return hashFunc.createHash(key);
	}
	
/**
 * same as get... changed to used new function for faster operation. 
 */
	public long getOrDefault(long key, long def) {
		
		FingerPrintAux fpAux = hashFunc.createHash(key);
		return getOrDefault(fpAux,def);
	}
	
	/**
	 * same as get... changed to used new function for faster operation. 
	 */
	public long getOrDefault(FingerPrintAux fpAux, long def) {			
		TinySetIndexingTechnique.getChain(fpAux, I0, IStar);
		long $ = this.findItemsValue(fpAux);
		if($== Long.MIN_VALUE)
			return def;
		return $;
	}

	private int extractChainID(long bucketAndChainID) {
		return (int) (bucketAndChainID % NCHAINS_PER_BUCKET); //TODO ask Gil if this is right
	}
	private int extractBucketID(long bucketAndChainID) {
		return (int) (bucketAndChainID / NCHAINS_PER_BUCKET); //TODO ask Gil if this is right
	}

	public boolean replaceByValue(long bucketAndChainID, long oldValue, long newValue) {
		assert(bucketAndChainID >= 0);
		// if we need to, we steal items from other buckets.
		int bucketId = extractBucketID(bucketAndChainID);
		int chainId = extractChainID(bucketAndChainID);
		TinySetIndexingTechnique.getChain(bucketId, chainId, I0, IStar);
		long oldItem;
		int bucketStart = getBucketStart(bucketId);
		for (int i=TinySetIndexingTechnique.chainStart; i<TinySetIndexingTechnique.chainEnd; i++) {
			oldItem = this.FastGet(bucketStart, i);
			if (extractValue(oldItem)==oldValue) {
				this.FastPut(bucketStart, i, buildRecord(extractFingerPrint(oldItem),newValue));
				return true;
			}
		}
		return false;	
	}


	public void putByValue(long key, long value) {
		FingerPrintAux fpAux = hashFunc.createHash(key);
		TinySetIndexingTechnique.getChain(fpAux, I0, IStar);
		int index = findItemByValue(fpAux.bucketId, value);
		int bucketStart = getBucketStart(fpAux.bucketId);
		if (index == -1) {
			add(fpAux, value);
		}
		else {
			this.FastPut(bucketStart, index, buildRecord(fpAux.fingerprint,value));
		}
	}


	/**
	 * Remove the first item that matches this hash-value. If can't find, do nothing.
	 * @param fpaux
	 */
	public boolean removeByValue(long bucketAndChainID, long value) 
	{
		// if we need to, we steal items from other buckets.
		int bucketId = extractBucketID(bucketAndChainID);
		int chainId = extractChainID(bucketAndChainID);
		TinySetIndexingTechnique.getChain(bucketId, chainId, I0, IStar);
		int indexInChain = 0;
		int bucketStart = getBucketStart(bucketId);
		for (int i=TinySetIndexingTechnique.chainStart; i < TinySetIndexingTechnique.chainEnd; i++) {
			// TODO remove the correct offset and not some random offset.
			if (extractValue(this.FastGet(bucketStart,i))==value) {
				remove(bucketId, chainId, indexInChain);
				return true;
			}
			++indexInChain;
		}
		return false;
	}



	public void remove(int bucketId, int chainId, int indexInChain) {
		assert(TinySetIndexingTechnique.chainExist(I0[bucketId], chainId));

		//TinySetIndexingTechnique.getChain(bucketId, chainId, I0, IStar); //TODO necessary?
		int itemOffset = TinySetIndexingTechnique.chainStart + indexInChain;
		assert(itemOffset<TinySetIndexingTechnique.chainEnd);


		int firstOffset = TinySetIndexingTechnique.chainStart;
		int bucketStart = getBucketStart(bucketId);
		long firstItem = this.FastGet(bucketStart, firstOffset);
		//		Assert.assertTrue(chain.containsitemOffset));
		this.FastPut(bucketStart, itemOffset, firstItem);
		this.FastPut(bucketStart, firstOffset, 0l);

		int bucket =0;
		this.nrItems--;
		this.removeAndShrink(bucketId);
		removeItemFromIndex(bucketId, chainId); 
		
		for(int i =bucketId+1; i<bucketId+this.I0.length;i++)
		{
			bucket = (i)%this.I0.length;
			if(A[bucket]>0)
			{
				removeAndShrink(bucket);
				A[bucket]--;
				continue;
			}
			else
			{

				break;
			}
		}		
	}


	/*
	private void remove(int bucketId, int chainId, int indexInChain) {
		byte[] chain = getChainIndices(chainId,I0[bucketId],IStar[bucketId]);
		remove(bucketId, chainId, indexInChain, chain);
		}
	 */
	public void remove(long key, int indexInChain) {
		FingerPrintAux fpaux = hashFunc.createHash(key);
		// TODO should I distinguish between an index in chain and an offset in chain?
		remove(fpaux.bucketId, fpaux.chainId, indexInChain);
	}

	/*
	public void removeOld(long key, int indexInChain) {
		FingerPrintAux fpaux = hashFunc.createHash(key);

		int chainoffset= RankIndexHashing.getChainAndUpdateOffsets(fpaux,I0,IStar,offsets,chain);

		int lastOffset = chain[chainoffset];
		long lastItem = this.Get(fpaux.bucketId, lastOffset);
		//		Assert.assertTrue(chain.containsitemOffset));
		this.Put(fpaux.bucketId, indexInChain, lastItem);
		this.Put(fpaux.bucketId, lastOffset, 0l);

		int bucket =0;
		this.removeAndShrink(fpaux.bucketId);
		removeItemFromIndex(fpaux); 

		for(int i =fpaux.bucketId+1; i<fpaux.bucketId+this.I0.length;i++)
		{
			bucket = (i)%this.I0.length;
			if(A[bucket]>0)
			{
				removeAndShrink(bucket);
				A[bucket]--;
				continue;
			}
			else
			{

				break;
			}
		}		
	}
	 */
	public long getBucketChainID(long key) {
		FingerPrintAux fpaux = hashFunc.createHash(key);
		return getBucketChainID(fpaux);
	}
	
	public long getBucketChainID(FingerPrintAux fpAux) {
		return fpAux.bucketId*NCHAINS_PER_BUCKET + fpAux.chainId;
	}
	public void put(long key, long value) {

		FingerPrintAux fpAux = hashFunc.createHash(key);
		put(fpAux, value);
	}

	public void put(FingerPrintAux fpaux, long value) {

		//FingerPrintAux fpAux = hashFunc.createHash(key);
		TinySetIndexingTechnique.getChain(fpaux, I0, IStar);
		int index = findItem(fpaux);
		int bucketStart = getBucketStart(fpaux.bucketId);
		if (index == -1) {
			add(fpaux, value);
		}
		else {
			this.FastPut(bucketStart, index, buildRecord(fpaux.fingerprint,value));
		}
	}
	public void decrement(long key, int indexInChain) {
		FingerPrintAux fpAux = hashFunc.createHash(key);
		assert(TinySetIndexingTechnique.chainExist(I0[fpAux.bucketId], fpAux.chainId));
		TinySetIndexingTechnique.getChain(fpAux, I0, IStar);
		int itemOffset = TinySetIndexingTechnique.chainStart+indexInChain;
		int bucketStart = getBucketStart(fpAux.bucketId);
		long record = this.FastGet(bucketStart, itemOffset);
		this.FastPut(bucketStart, itemOffset, buildRecord(extractFingerPrint(record),
				extractValue(record)-1));

	}
	
	public void readyDecrement(FingerPrintAux fpAux, int indexInChain) {
		assert(TinySetIndexingTechnique.chainExist(I0[fpAux.bucketId], fpAux.chainId));
		int itemOffset = TinySetIndexingTechnique.chainStart+indexInChain;
		int bucketStart = getBucketStart(fpAux.bucketId);
		long record = this.FastGet(bucketStart, itemOffset);
		this.FastPut(bucketStart, itemOffset, buildRecord(extractFingerPrint(record),
				extractValue(record)-1));

	}
	
	public long get(long key, int indexInChain) {
		FingerPrintAux fpAux = hashFunc.createHash(key);		
		assert(TinySetIndexingTechnique.chainExist(I0[fpAux.bucketId], fpAux.chainId));
		TinySetIndexingTechnique.getChain(fpAux, I0, IStar);
		int bucketStart = getBucketStart(fpAux.bucketId);
		return extractValue(this.FastGet(bucketStart,
				TinySetIndexingTechnique.chainStart+ indexInChain));
	}
	
	/**
	 * can only be called after chain is prepared.
	 * @param indexInChain
	 * @return
	 */
	public long readyGet(int bucketId, int indexInChain) {
		int bucketStart = getBucketStart(bucketId);
		return extractValue(this.FastGet(bucketStart,
				TinySetIndexingTechnique.chainStart+ indexInChain));
	}
	
	public long get(FingerPrintAux fpAux, int indexInChain) {
		assert(TinySetIndexingTechnique.chainExist(I0[fpAux.bucketId], fpAux.chainId));
		TinySetIndexingTechnique.getChain(fpAux, I0, IStar); //TODO unnecessary to call every time.
		int bucketStart = getBucketStart(fpAux.bucketId);
		return extractValue(this.FastGet(bucketStart,
				TinySetIndexingTechnique.chainStart+ indexInChain));
	}

	public long getChainSize(long key) {
		FingerPrintAux fpAux = hashFunc.createHash(key);
		return TinySetIndexingTechnique.getChain(fpAux, I0, IStar);
	}
	
	/**
	 * calculates chain size and prepares chain.
	 * @param fpAux
	 * @return
	 */
	public long getChainSize(FingerPrintAux fpAux) {
		return TinySetIndexingTechnique.getChain(fpAux, I0, IStar);
	}


	


}

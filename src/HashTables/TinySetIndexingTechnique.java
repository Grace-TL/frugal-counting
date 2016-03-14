package HashTables;

import HashFunctions.FingerPrintAux;

public class TinySetIndexingTechnique {


	// for performance - for functions that need to know both the start and the end of the chain. 
	public static int chainStart; 
	public static int chainEnd; 
	public static int rank(long Istar, int idx)
	{
		return Long.bitCount(Istar&((1l<<idx)-1));
	}





	public static int getChain(FingerPrintAux fpaux,long[] I0, long[] IStar)
	{
		return getChain(fpaux.bucketId, fpaux.chainId, I0, IStar);
	}

	public static int getChain(int bucketId, int chainId, long[] I0, long[] IStar)
	{
		// number of set bits in I0 until chainId not including. 
		// Meaning the number of chains to look for.
		int requiredChainNumber = rank(I0[bucketId], chainId);
		//int currentChainNumber = rank(IStar[bucketId], requiredChainNumber); // does not add speed
		//int currentOffset = requiredChainNumber;
		//long tempIStar = IStar[bucketId]>>>requiredChainNumber;
		//int toShift;
		int i =0;
		int currentOffset =0;
		long tempIStar= IStar[bucketId];
		
		if(requiredChainNumber!=0)  {
			for (i=1;i<requiredChainNumber; ++i) {
				tempIStar &= tempIStar-1L;
			} 
			currentOffset = Long.numberOfTrailingZeros(tempIStar)+1;//Long.bitCount(tempIStar^(tempIStar-1L));
			tempIStar >>>= currentOffset;
		}
		TinySetIndexingTechnique.chainStart = currentOffset;

		if((I0[bucketId]|(1l<<chainId))==I0[bucketId])//TinySetIndexingTechnique.chainExist(I0[bucketId], chainId))
		{
			currentOffset += Long.numberOfTrailingZeros(tempIStar)+1;
		}
		TinySetIndexingTechnique.chainEnd = currentOffset;

		return chainEnd-chainStart; 



	}

	public static boolean chainExist(long I0, int chainId)
	{
		//		long mask = ;
		return (I0|(1l<<chainId))==I0;
	}


	public static int addItem(FingerPrintAux fpaux, long[] I0, long[] IStar) {
		getChain(fpaux,I0,IStar);
		int offset  = TinySetIndexingTechnique.chainStart;
		long mask = 1l<<fpaux.chainId;
		IStar[fpaux.bucketId] = extendZero(IStar[fpaux.bucketId],offset);

		//if the item is new... 
		if((mask|I0[fpaux.bucketId]) != I0[fpaux.bucketId])
		{
			// add new chain to IO.
			I0[fpaux.bucketId]|=mask;
			// mark item as last in IStar.
			IStar[fpaux.bucketId]|=(1l<<offset);

		}

		return offset;
	}

	private static long extendZero(final long IStar, final int offset) {
		long constantPartMask = (1l<<offset)-1;
		return (IStar&constantPartMask)|((IStar<<1l)& (~(constantPartMask))&(~(1l<<offset)));

	}

	private static long shrinkOffset(long IStar, int offset) {
		long conMask = ((1l<<offset) -1);
		return  (IStar&conMask)|(((~conMask)&IStar)>>>1);
	}
	public static void removeItem(FingerPrintAux fpaux, long[] I0,long[] IStar)
	{
		removeItem(fpaux.bucketId, fpaux.chainId, I0, IStar);

	}

	public static void removeItem(int bucketId, int chainId, long[] I0,long[] IStar)
	{
		getChain(bucketId, chainId,I0,IStar);
		int chainStart = TinySetIndexingTechnique.chainStart;
		// avoid an if command: either update I0 to the new state or keep it the way it is. 
		I0[bucketId] = (IStar[bucketId]&(1l<<chainStart))!=0l?I0[bucketId]&~(1l<<chainId):I0[bucketId];
		// update IStar.
		IStar[bucketId] = shrinkOffset(IStar[bucketId],chainStart);
	}









}

package HashFunctions;

public class TrivialHashMaker {

	//currently chain is bounded to be 64. 

	private final int fpSize;
	private final long fpMask; 
	private final long chainMask=63l; 
	private final long bucketMask;
	private final int bucketMaskSize;
	public FingerPrintAux fpaux;
	public TrivialHashMaker(int fingerprintsize, int bucketrange)
	{
		this.fpSize = fingerprintsize;
		// finger print cannot be zero so you must choose a finger-print size greater than zero.
		assert(fpSize > 0); 
		assert(Long.bitCount(bucketrange)==1): "bucket range must be a power of two";
		this.bucketMask = bucketrange-1;
		this.bucketMaskSize = Long.bitCount(bucketMask);
		fpMask = (1l<<fpSize)-1;
		fpaux = new FingerPrintAux(0,0,0); 
	}




	public FingerPrintAux createHash(long item) {
		long hash =  item;
		
		//hash /= bucketRange;
		fpaux.bucketId = (int) (hash&bucketMask);//(int) (item - hash*bucketRange);
		hash>>>=bucketMaskSize;
		//(int) (hash&Long.MAX_VALUE)%bucketRange;
		
		
		fpaux.chainId = (int) (hash&chainMask);
		hash>>>=6;
		
		fpaux.fingerprint = (long) ((hash+1)&fpMask);
		assert fpaux.fingerprint != 0 : "You may not hash the max fingerprint! It is reserved";
		/*
		if (fpaux.fingerprint == 0) {
			throw new RuntimeException("You may not hash the max fingerprint! It is reserved");
		}
		*/
		
		return fpaux;

	}


	//	public  FingerPrintAux createHash(final byte[] data) {
	//
	//		long hash =  MurmurHashTinyTable.hash64(data, data.length,0xe17a1465);
	//		
	//		fpaux.fingerprint = hash&fpMask;
	//		if(fpaux.fingerprint ==0l)
	//		{
	//			fpaux.fingerprint++;
	//		}
	//
	//
	//
	//		hash>>>=fpSize;
	//		fpaux.chainId = (int) (hash&chainMask);
	//		hash>>>=6;
	//		fpaux.bucketId =  (int) ((hash&Long.MAX_VALUE)%bucketRange);
	//		
	//		return fpaux;
	//		
	//	}



}

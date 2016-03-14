package SlidingWindow;

import HashTables.TinyTableWithValues;

public class CountingSpaceSaving {
	private SpaceSaving internal;
	private TinyTableWithValues totalOverflows;
	int size;
	int n;
	
	CountingSpaceSaving(int n, int size, int keySize) {
		this.n = n;//1<<(Integer.SIZE-Integer.numberOfLeadingZeros(n-1));
		this.size = size;//1<<(Integer.SIZE-Integer.numberOfLeadingZeros(size-1));
		internal = new SpaceSaving(this.n,this.size,keySize);
		int maxOverflows = (int) Math.ceil((float) n/size);
		totalOverflows = new TinyTableWithValues(1<<(Integer.SIZE - Integer.numberOfLeadingZeros(size-1)), 
				keySize, 
				(Integer.SIZE - Integer.numberOfLeadingZeros(maxOverflows-1)), 
				0.2);
	}
	
	void inc(long key) {
		boolean overflow = internal.inc(key);
		if (overflow) {
			totalOverflows.put(key, (short) (totalOverflows.getOrDefault(key, (short) 0)+1));
		}		
	}
	
	long bound(long key) {
		int ssEstimate = internal.bound(key);
		int overflows = (int) totalOverflows.getOrDefault(key,0);
		if (overflows > 0) {
			ssEstimate = ssEstimate%(n/size);	
		}
		return (ssEstimate + (n/size)*overflows);
	}
}

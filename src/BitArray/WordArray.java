package BitArray;

import BitHelpers.AuxilaryBitSet;

public class WordArray extends AuxilaryBitSet {
	protected final int wordSize;
	public final long length;
	public WordArray(long length, int wordSize) {
		super((int) Math.ceil(((float) length*wordSize)/Long.SIZE));
		this.length = length;
		this.wordSize = wordSize;
	}
	
	public long getSpace() {
		return this.words.length*Long.SIZE/8;
	}
	
	public void put(int i, long v) {
		setBits(i*wordSize,(i+1)*wordSize,v);
	}
	
	public long get(int i) {
		return getBits(i*wordSize,(i+1)*wordSize);
	}
}

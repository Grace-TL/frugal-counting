package HashTables;

import static org.junit.Assert.*;

import java.io.IOException;

import org.junit.Assert;
import org.junit.Test;

public class TinyTableTester {
	
	@Test
	public void addTest()
	{
		int nrItems = 2048; 
		int keySize = 21; 
		int valueSize = 12;
		double alpha = 0.01;
		
		TinyTableWithValues ap;
		ap = new TinyTableWithValues(nrItems, keySize, valueSize, alpha);
		
		for(int i =0; i<nrItems-1;i++)
		{
			ap.add(i, i);
			for(int j=0; j<i;j++)
				Assert.assertTrue(ap.containsKey(j));
		System.out.println("i=" + i);
		}
		for(int i=0; i<nrItems-1;i++)
		{
			ap.remove(i);
			for(int j= nrItems-2;j>i;j--){
				Assert.assertTrue(ap.containsKey(j));
				
			}
		}
		
	}
	@Test
	public void valuesDecrementTest() throws IOException
	{
		int nrItems = 2048; 
		int keySize = 21; 
		int valueSize = 6;
		double alpha = 0.25;
		
		TinyTableWithValues ap;
		ap = new TinyTableWithValues(nrItems, keySize, valueSize, alpha);
		
		ap.add(611,3);
		ap.decrement(611, 0);
		assertEquals(2, ap.get(611));
	}

	/**
	 * Test that there are no collisions in the hash table.
	 */
	@Test
	public void valuesCollisionTest()
	{
		int valueSize = 0;
		int nrItems;
		double alpha = 0.25;
		TinyTableWithValues ap;
		for (int keySize = 1; keySize <= 23; ++keySize) {
			nrItems = 1<<keySize;
			ap = new TinyTableWithValues(nrItems, keySize, valueSize, alpha);
			for (int x=0; x<nrItems; ++x) {
				if (ap.containsKey(x)) {
					System.out.println("key-size "+keySize+" collision of "+x);
					for (int y=0; y<x;y++) {
						ap.remove(y);
						if (!ap.containsKey(x)) {
							System.out.println("collided with "+y);
							fail();
						}
					}
				}
				ap.add(x, 0);
			}
		}
		
	}
	
	@Test
	public void valuesSanityTest() throws IOException
	{
		int nrItems = 2048; 
		int keySize = 21; 
		int valueSize = 6;
		double alpha = 0.25;
		
		TinyTableWithValues ap;
		ap = new TinyTableWithValues(nrItems, keySize, valueSize, alpha);
		
		ap.add(611,1);
		Assert.assertTrue(ap.containsKey(611));
		ap.add(54717,2);
		Assert.assertTrue(ap.containsKey(611));
		Assert.assertTrue(ap.containsKey(54717));
		ap.add(74706,3);
		Assert.assertTrue(ap.containsKey(611));
		Assert.assertTrue(ap.containsKey(54717));
		Assert.assertTrue(ap.containsKey(74706));
	}

	@Test
	public void valuesGetChainSizeTest() throws IOException
	{
		int nrItems = 2048; 
		int keySize = 21; 
		int valueSize = 6;
		double alpha = 0.25;
		
		TinyTableWithValues ap;
		ap = new TinyTableWithValues(nrItems, keySize, valueSize, alpha);
		
		Assert.assertEquals(0,(int) ap.getChainSize(611));
		ap.add(611,1);
//		System.out.println(ap.getChainSize(611));
		Assert.assertEquals(1,(int) ap.getChainSize(611));
		ap.remove(611,0);
		Assert.assertEquals(0,(int) ap.getChainSize(611));
		for (int i=0;i<16;i++) {
			ap.add(611, i);
			Assert.assertEquals(i+1,(int) ap.getChainSize(611));
		}
		for (int i=0;i<16;i++) {
			ap.remove(611, 0);
			Assert.assertEquals(15-i,(int) ap.getChainSize(611));
		}
	}
	
	
	@Test
	public void valuesReplaceTest() throws IOException
	{int nrItems = 2048; 
	int keySize = 21; 
	int valueSize = 6;
	double alpha = 0.25;
	
	TinyTableWithValues ap;
	ap = new TinyTableWithValues(nrItems, keySize, valueSize, alpha);
			ap.add(611, 3);
		assertEquals(3, ap.get(611));
		ap.replaceByValue(ap.getBucketChainID(611), 3, 5);
		assertEquals(5, ap.get(611));
		assertFalse(ap.containsKey(0));
	}
	
	@Test 
	public void valuesConfusedRemoveTest() {
		TinyTableWithValues tt = new TinyTableWithValues(1<<5,32, 5, 0.25);
		tt.add(1, 1);
		tt.add(4, 1);
		tt.removeByValue(tt.getBucketChainID(1), 1);
		assertTrue(tt.containsKey(4));
		//assertTrue(tt.containsKey(4));
	}
	@Test
	public void valuesRemoveByValueAdd() throws IOException
	{
		int nrItems = 2048; 
		int keySize = 21; 
		int valueSize = 6;
		double alpha = 0.25;
		
		TinyTableWithValues ap;
		ap = new TinyTableWithValues(nrItems, keySize, valueSize, alpha);
		int iter = 20;
		for(int i =0; i<iter; i++)
		{
			if (i==1) {
				System.err.println("breakpoint");
			}
			ap.add(611,i);
			Assert.assertTrue(ap.containsKey(611));
		}
		for(int i =0; i<(iter-1); i++)
		{
			if (i==16) {
				System.err.println("breakpoint");
			}
			ap.removeByValue(ap.getBucketChainID(611),i);
			Assert.assertTrue(ap.containsKey(611));
		}
		ap.remove(611);
		Assert.assertTrue(!ap.containsKey(611));
	}
	
	@Test
	public void valuesRemoveAdd() throws IOException
	{
		int nrItems = 2048; 
		int keySize = 21; 
		int valueSize = 6;
		double alpha = 0.25;
		
		TinyTableWithValues ap;
		ap = new TinyTableWithValues(nrItems, keySize, valueSize, alpha);
		int iter =20;
		for(int i =0; i<iter; i++)
		{
			ap.add(611,i);
			Assert.assertTrue(ap.containsKey(611));
		}
		for(int i =0; i<(iter-1); i++)
		{
			ap.remove(611);
			if(!ap.containsKey(611))
			{
				System.out.println("i="+i);
			}
			Assert.assertTrue(ap.containsKey(611));
		}
		ap.remove(611);
		Assert.assertTrue(!ap.containsKey(611));
	}


	@Test
	public void valuesStatisticsTest()
	{
		int nrItems = 2048; 
		int keySize = 21; 
		int valueSize = 14;
		double alpha = 0.25;
		
		TinyTableWithValues ap;
			ap = new TinyTableWithValues(nrItems, keySize, valueSize, alpha);
			long value = 631l;
			ap.put(611, value);
			System.out.println(ap.get(611));
			Assert.assertTrue(ap.get(611)==value);

			value = 1l;
			ap.put(611, value);
			System.out.println(ap.get(611));
			Assert.assertTrue(ap.get(611)==value);

			value = 10000l;
			ap.replaceByValue(ap.getBucketChainID(611), 1l, value);
			ap.get(611);
			System.out.println(ap.replaceByValue(ap.getBucketChainID(611), 1l, value));
			System.out.println(ap.get(611));
			Assert.assertTrue(ap.get(611)==value);

			value = 0l;
			ap.put(611, value);
			System.out.println(ap.get(611));
			Assert.assertTrue(ap.get(611)==value);


			for(int i =1; i<1990;i++)
			{

				value=12;
				ap.put(i, value);
				if (!ap.containsKey(i) || ap.get(i)!=value) {
					System.out.println(i);
					fail();				
				}

			}
	

	}

	@Test
	public void valuesSameChainTest()
	{
		int nrItems = 2048; 
		int keySize = 21; 
		int valueSize = 6;
		double alpha = 0.25;
		
		TinyTableWithValues ap = null;
		
		ap = new TinyTableWithValues(nrItems, keySize, valueSize, alpha);
		
		int value=12;
		ap.put(230, value);
		assertTrue(ap.containsKey(230));
		
		ap.put(231, value);
		assertTrue(ap.containsKey(231));
		
		ap.put(237, value);
		assertTrue(ap.containsKey(237));
		
	}

	
	@Test
	public void values237Test() throws IOException
	{
		int nrItems = 2048; 
		int keySize = 21; 
		int valueSize = 6;
		double alpha = 0.25;
		
		TinyTableWithValues ap;
		ap = new TinyTableWithValues(nrItems, keySize, valueSize, alpha);
		
		for(int i =1; i<1990;i++)
		{

			int value=12;
			System.out.println(i);
			ap.put(i, value);
			for (int j=1; j <= i;j++) {
				assertTrue(ap.containsKey(j));
			}
			if (i == 1250) {
				System.err.println("breakpoint");
			}
			System.out.println("okay!" +i);
		}
		
		
		for(int i =1989; i>0;i--)
		{

			System.out.println(i);

			
			ap.remove(i);
			
			for (int j=1; j < i;j++) {
				if (!ap.containsKey(j)) {
					System.err.println(j+" not contained.");
					fail();
				}
			}
			
			for (int j=i; j < 1990;j++) {
				assertFalse(ap.containsKey(j));
			}
		}
		System.out.println(ap.getNrItems());
	}

}


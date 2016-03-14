package HashTables;

import org.junit.Test;

import HashFunctions.FingerPrintAux;

public class IndexingTester {

	@Test
	public void test()
	{
		long[] I0 = new long[1];
		long[] IStar = new long[1];

		FingerPrintAux fpaux = new FingerPrintAux(0,0,0);

		fpaux.chainId = 8; 
		TinySetIndexingTechnique.addItem(fpaux, I0, IStar);
		System.out.println(Long.toBinaryString(I0[0]) );
		System.out.println(Long.toBinaryString(IStar[0]) );
		//System.out.println("Chain StartAt: "+ TinySetIndexingTechnique.getChainStart(fpaux, I0, IStar));
		//System.out.println("Chain End At: "+ TinySetIndexingTechnique.getChainEnd(fpaux, I0, IStar));

		fpaux.chainId = 8; 
		TinySetIndexingTechnique.addItem(fpaux, I0, IStar);
		System.out.println(Long.toBinaryString(I0[0]) );
		System.out.println(Long.toBinaryString(IStar[0]) );
		//System.out.println("Chain StartAt: "+ TinySetIndexingTechnique.getChainStart(fpaux, I0, IStar));
		//System.out.println("Chain End At: "+ TinySetIndexingTechnique.getChainEnd(fpaux, I0, IStar));
		fpaux.chainId = 8; 
		TinySetIndexingTechnique.addItem(fpaux, I0, IStar);
		System.out.println(Long.toBinaryString(I0[0]) );
		System.out.println(Long.toBinaryString(IStar[0]) );
		fpaux.chainId = 5; 
		//System.out.println("Chain StartAt: "+ TinySetIndexingTechnique.getChainStart(fpaux, I0, IStar));
		//System.out.println("Chain End At: "+ TinySetIndexingTechnique.getChainEnd(fpaux, I0, IStar));

		TinySetIndexingTechnique.addItem(fpaux, I0, IStar);
		System.out.println(Long.toBinaryString(I0[0]) );
		System.out.println(Long.toBinaryString(IStar[0]) );
		fpaux.chainId = 5; 
		//System.out.println("Chain StartAt: "+ TinySetIndexingTechnique.getChainStart(fpaux, I0, IStar));
		//System.out.println("Chain End At: "+ TinySetIndexingTechnique.getChainEnd(fpaux, I0, IStar));

		TinySetIndexingTechnique.addItem(fpaux, I0, IStar);
		System.out.println(Long.toBinaryString(I0[0]) );
		System.out.println(Long.toBinaryString(IStar[0]) );
		fpaux.chainId = 2; 
		TinySetIndexingTechnique.addItem(fpaux, I0, IStar);
		System.out.println(Long.toBinaryString(I0[0]) );
		System.out.println(Long.toBinaryString(IStar[0]) );

		System.out.println("Removing: 2");
		fpaux.chainId = 2; 
		//TinySetIndexingTechnique.RemoveItem(fpaux, I0, IStar);
		System.out.println(Long.toBinaryString(I0[0]) );
		System.out.println(Long.toBinaryString(IStar[0]) );
		System.out.println("Removing: 8");
		fpaux.chainId = 8; 
		//TinySetIndexingTechnique.RemoveItem(fpaux, I0, IStar);
		System.out.println(Long.toBinaryString(I0[0]) );
		System.out.println(Long.toBinaryString(IStar[0]) );
		System.out.println("Removing: 5");
		fpaux.chainId = 5; 
		//TinySetIndexingTechnique.RemoveItem(fpaux, I0, IStar);
		System.out.println(Long.toBinaryString(I0[0]) );
		System.out.println(Long.toBinaryString(IStar[0]) );
		System.out.println("Removing: 8");
		fpaux.chainId = 8; 
		//TinySetIndexingTechnique.RemoveItem(fpaux, I0, IStar);
		System.out.println(Long.toBinaryString(I0[0]) );
		System.out.println(Long.toBinaryString(IStar[0]) );
		System.out.println("Removing: 5");
		fpaux.chainId = 5; 
		//TinySetIndexingTechnique.RemoveItem(fpaux, I0, IStar);
		System.out.println(Long.toBinaryString(I0[0]) );
		System.out.println(Long.toBinaryString(IStar[0]) );
		System.out.println("Removing: 8");
		fpaux.chainId = 8; 
		//TinySetIndexingTechnique.RemoveItem(fpaux, I0, IStar);
		System.out.println(Long.toBinaryString(I0[0]) );
		System.out.println(Long.toBinaryString(IStar[0]) );

		//		System.out.println("Chain At: "+ TinySetIndexingTechnique.getChainStart(fpaux, I0, IStar));





	}



}

package org.mrdlib.translator;

import java.util.AbstractMap;
import java.util.ArrayList;
import java.util.List;
import java.util.AbstractMap.SimpleEntry;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;

import org.mrdlib.partnerContentManager.gesis.Abstract;

public class SingleAbstractThread implements Callable<AbstractMap.SimpleEntry<Long,Abstract>>{
	private Long documentId;
	//private String originalLanguage;
	private String abstractText;
	
	public SingleAbstractThread(AbstractMap.SimpleEntry<Long,Abstract> abstractDocIdPair){
		documentId=abstractDocIdPair.getKey();
		//originalLanguage=abstractDocIdPair.getValue().getLanguage();
		abstractText = abstractDocIdPair.getValue().getContent();
	}
	
	@Override
	public SimpleEntry<Long, Abstract> call() throws Exception {
		String[] germanText = abstractText.split("\\. |\\? |! ");
		ExecutorService pool = Executors.newFixedThreadPool(4);
		List<Future<String>> futures = new ArrayList<Future<String>>(10);
		for(int i = 0; i<germanText.length; i++){
			futures.add(pool.submit(new SendPacket(germanText[i])));
		}
		
		List<String> translatedAbstract = new ArrayList<>();
		for (Future<String> future : futures) {
			String result = future.get();
			translatedAbstract.add(result); 
		}
		
		return new SimpleEntry<Long,Abstract>(documentId, new Abstract(String.join(". ", translatedAbstract),"de"));
	}

}

package org.mrdlib.translator;

import java.util.AbstractMap;
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
		//String[] germanText = abstractText.split("\\. |\\? |! ");
		ExecutorService pool = Executors.newSingleThreadExecutor();
		Future<String> future = pool.submit(new SendPacket(abstractText));
		
		
		//List<String> translatedAbstract = new ArrayList<>();
		String result = (String) future.get();
		/*for (Future<String> future : futures) {
			//System.out.println("Waiting for line");
			result = future.get();
			//translatedAbstract.add(result); 
		}*/
		
		return new SimpleEntry<Long,Abstract>(documentId, new Abstract(result,"de"));
	}

}

package org.mrdlib.translator;

import java.util.AbstractMap;
import java.util.AbstractMap.SimpleEntry;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;

import org.mrdlib.database.DBConnection;
import org.mrdlib.partnerContentManager.gesis.Abstract;

public class AbstractTranslator {

	long totalGermanAbstracts;
	DBConnection con = null;

	public AbstractTranslator() {

		try {
			con = new DBConnection("jar");
		} catch (Exception e) {
			e.printStackTrace();
		}

	}

	public static void main(String[] args) {

		AbstractTranslator at = new AbstractTranslator();
		at.translate();

	}

	private void translate() {
		List<SimpleEntry<Long, Abstract>> abstracts = new ArrayList<AbstractMap.SimpleEntry<Long, Abstract>>();

		totalGermanAbstracts = con.getNumberOfAbstractsInLanguage("de");

		for (int i = 0; i < totalGermanAbstracts; i += 500) {
			abstracts = con.fillAbstractsList("de", i);
			ExecutorService pool = Executors.newFixedThreadPool(20);
			List<Future<SimpleEntry<Long, Abstract>>> futures = new ArrayList<Future<AbstractMap.SimpleEntry<Long, Abstract>>>(
					500);
			for (int k = 0; k < abstracts.size(); k++) {
				futures.add(pool.submit(new SingleAbstractThread(abstracts.get(k))));
			}
			for (Future<SimpleEntry<Long, Abstract>> future : futures) {
				AbstractMap.SimpleEntry<Long, Abstract> translatedAbstract;
				try {
					//System.out.println("Trying to get " + i);
					translatedAbstract = future.get();
					//System.out.println(i);
					//System.out.println("Document id" + translatedAbstract.getKey());
					//System.out.println("Abstract:");
					//System.out.println(translatedAbstract.getValue().getContent());
					//System.out.println("------------------------------");
					int returnValue = con.addTranslatedAbstract(translatedAbstract);
					if (returnValue<0) {
						System.out.println(translatedAbstract.getKey());
					}
				} catch (InterruptedException | ExecutionException e) {

					e.printStackTrace();
				}

			}
			if(i%1000==0)System.out.println("Completed Translating " + i + " documents. At documentId"); 
		}

	}
}

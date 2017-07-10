package org.mrdlib.partnerContentManager.general.languageDetection;

import org.mrdlib.api.response.DisplayDocument;
import org.mrdlib.database.DBConnection;
import org.mrdlib.api.manager.Constants;

import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.TimeZone;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;
import java.util.function.Supplier;
import java.util.stream.Collector;
import java.util.stream.Collectors;
import java.sql.Types;

import org.apache.tika.language.detect.LanguageDetector;
import org.apache.tika.langdetect.OptimaizeLangDetector;

/**
 * LanguageDetection
 * 
 * @author Fabian Richter
 */
public class LanguageDetection {
    private DBConnection db;
    private static final long TIMEOUT = 30; // how long each awaitTermination blocks
    private static final long BATCH_SIZE = 10000; // how many entries to query from the DB at once
    public LanguageDetection (DBConnection db) {
        this.db = db;
    }

    // actual detection happens here
    private class LanguageDetectionTask implements Callable<String> {
        private String title;
        private ThreadLocal<LanguageDetector> detector;
        public LanguageDetectionTask(String title, ThreadLocal<LanguageDetector> detector) {
            this.title = title;
            this.detector = detector;
        }
        public String call() {
            // fetch local instance
            LanguageDetector ld = detector.get();
            ld.reset();
            return ld.detect(this.title).getLanguage().substring(0, 2);
        }
    }

    /** 
     * Go trough documents, look at titles and detect languages from them using Apache Tika
     * 
     * @param documents Document IDs
     *  
    */
    public List<String> detectLanguage(List<DisplayDocument> documents) throws InterruptedException {

        // parallize task
        ExecutorService pool = Executors.newWorkStealingPool();
        List<Future<String>> languages = new ArrayList<Future<String>>(documents.size());
        // initialize only one detector for each thread: 
        // loading models is bottleneck, but we also don't want to block
        ThreadLocal<LanguageDetector> detectors = ThreadLocal.withInitial(new Supplier<LanguageDetector>() {
            // create detector
            public LanguageDetector get() {
                LanguageDetector detector = new OptimaizeLangDetector(); // TODO: Test other implementations
                try {
                    detector.loadModels();
                } catch (IOException e) {
                    e.printStackTrace();
                }
                return detector;
            }
        });

        for (DisplayDocument doc : documents) {
            languages.add(pool.submit(new LanguageDetectionTask(doc.getTitle(), detectors)));
        }
        pool.shutdown();

        long passed = 0;
        // wait for task to finish, print 'progress'
        while(!pool.awaitTermination(TIMEOUT, TimeUnit.SECONDS)){
            passed += TIMEOUT;
            System.out.println("Waiting for language detection (" + languages.size() + " tasks) to finish." + passed * TIMEOUT + "s passed since process started.");
        }
        // collect results
        return languages.stream().map( (Future<String> result) -> { 
            try { 
                return result.get();
            } catch(Exception e) { 
                return null; 
            }
         }).collect(Collectors.toList());
    }

    public static void main(String[] args) {
        try {
	        Constants constants = new Constants();
            DBConnection connection = new DBConnection("jar");
            LanguageDetection detection = new LanguageDetection(connection);
            int processed = 0;
            SimpleDateFormat elapsed = new SimpleDateFormat("HH:mm:ss");
            elapsed.setTimeZone(TimeZone.getTimeZone("UTC"));
            while(true) {
                List<DisplayDocument> docs = connection.getDocumentsWithMissingValue(constants.getLanguageDetected(), BATCH_SIZE);
                if (docs.size() == 0) break;
                processed += docs.size();
                System.out.println("Processing next batch...");
                long startTime = System.currentTimeMillis();

                List<Object> languages = (List) detection.detectLanguage(docs);
                List<Object> ids = docs.stream()
                    .map( (DisplayDocument d) -> d.getDocumentId())
                    .collect(Collectors.toList());
                connection.setDocumentValues(constants.getDocumentId(), ids, Types.BIGINT,
                    constants.getLanguageDetected(), languages, Types.CHAR);
                long time = System.currentTimeMillis() - startTime;
                System.out.format("Finished processing batch of %d documents (%s).%n%d documents processed.%n", BATCH_SIZE, 
                    elapsed.format(time), processed);
            }
            System.out.println("Finished processing documents.");
            connection.close();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
package org.mrdlib.partnerContentManager.general;

import org.mrdlib.api.response.DisplayDocument;
import org.mrdlib.database.DBConnection;
import org.mrdlib.api.manager.Constants;

import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.TimeZone;
import java.util.ArrayList;
import java.util.List;
import java.util.Arrays;
import java.util.HashMap;
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
    private static final long TIMEOUT = 30; // how long each awaitTermination blocks
    private static final long BATCH_SIZE = 10000; // how many entries to query from the DB at once

    // actual detection happens here
    private static class LanguageDetectionTask implements Callable<String> {
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
     * Go trough text and detect languages from it using Apache Tika
     * 
     * @param documents Document IDs
     *  
    */
    public static List<String> detectLanguage(List<String> documents) throws InterruptedException {

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

        for (String doc : documents) {
            languages.add(pool.submit(new LanguageDetectionTask(doc, detectors)));
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
		e.printStackTrace();
                return null; 
            }
         }).collect(Collectors.toList());
    }

    enum Mode { ABSTRACT, TITLE };

    // usage: ./gradlew languageDetection:run -Dexec.args=title|abstracts
    public static void main(String[] args) {
        Mode mode;
        if (args.length > 0) {
            String modeArg = args[0].toLowerCase();
            if ("abstracts".startsWith(modeArg)) {
                mode = Mode.ABSTRACT;
                System.out.println("Processing abstracts.");
            } else if ("titles".startsWith(modeArg)) {
                mode = Mode.TITLE;
                System.out.println("Processing titles.");
            } else {
                System.out.println("Could not parse arguments: " + args[0]);
                System.out.println("Usage: [program] titles|abstracts");
                return;
            }
        } else {
            System.out.println("Error: No argument given. Usage: [program] titles|abstracts");
            return;
        }


        try {
	        Constants constants = new Constants();
            DBConnection connection = new DBConnection("jar");
            int processed = 0;
            SimpleDateFormat elapsed = new SimpleDateFormat("HH:mm:ss");
            elapsed.setTimeZone(TimeZone.getTimeZone("UTC"));
            List<String> texts = new ArrayList<String>((int)BATCH_SIZE);
            List<Object> ids = new ArrayList<Object>((int)BATCH_SIZE);

            while(true) {
                texts.clear();
                ids.clear();

                // fetch entries from database
                if (mode == Mode.TITLE) {
                    List<DisplayDocument> docs = connection.getDocumentsWithMissingValues(constants.getLanguageDetected(), BATCH_SIZE);
                    for (DisplayDocument doc : docs) {
                        texts.add(doc.getTitle());
                        ids.add(doc.getDocumentId());
                    }
                } else if (mode == Mode.ABSTRACT) {
                    List<String> attributes = Arrays.asList(new String[] { constants.getAbstr(), constants.getAbstractId() });
                    List<HashMap<String,Object>> abstracts = connection.getEntriesWithMissingValue(
                        constants.getAbstracts(), constants.getLanguageDetected(), attributes, BATCH_SIZE);
                    for (HashMap<String, Object> entry : abstracts) {
                        texts.add(entry.get(constants.getAbstr()).toString());
                        ids.add(entry.get(constants.getAbstractId()));
                    }
                }

                if (texts.size() == 0) break;
                processed += texts.size();
                System.out.println("Processing next batch...");
                long startTime = System.currentTimeMillis();

                List<Object> languages = (List) detectLanguage(texts);

                // save results
                if (mode == Mode.TITLE) {
                    connection.setRowValues(constants.getDocuments(), constants.getDocumentId(), ids, Types.BIGINT,
                        constants.getLanguageDetected(), languages, Types.CHAR);
                } else if (mode == Mode.ABSTRACT) {
                    connection.setRowValues(constants.getAbstracts(), constants.getAbstractId(), ids, Types.BIGINT,
                    constants.getLanguageDetected(), languages, Types.CHAR);
                }
                long time = System.currentTimeMillis() - startTime;
                System.out.format("Finished processing batch of %d entries (%s).%n%d entries processed.%n", BATCH_SIZE, 
                    elapsed.format(time), processed);
            }
            System.out.format("Finished processing %s.%n", mode == Mode.TITLE ? "titles" : "abstracts");
            connection.close();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}

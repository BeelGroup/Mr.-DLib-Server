package org.mrdlib.translator;

import java.io.PrintStream;
import java.util.ArrayList;
import java.util.List;
import org.mrdlib.api.manager.Constants;
import org.mrdlib.database.DBConnection;

public class KeywordTranslator {
    private DBConnection con;
    private Constants constants = new Constants();

    public KeywordTranslator() {
        try {
            this.con = new DBConnection("jar");
        }
        catch (Exception e) {
            e.printStackTrace();
        }
    }

    public static void main(String[] args) {
        KeywordTranslator keyword = new KeywordTranslator();
        keyword.translate();
    }

    public void translate() {
        long numberOfDocuments = this.con.getNumberOfKeywordsInLanguage("de");
        int k = 0;
        while ((long)k < numberOfDocuments) {
            List documentDataList = new ArrayList();
            documentDataList = this.con.getKeywordsInBatches(k, k + 1000);
            System.out.println(String.valueOf(k) + "/" + numberOfDocuments);
            k += 1000;
        }
    }
}
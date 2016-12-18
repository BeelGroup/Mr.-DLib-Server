package org.mrdlib.translator;

import java.io.PrintStream;
import java.util.ArrayList;
import java.util.List;
import org.mrdlib.database.DBConnection;

public class TitleTranslator {
    DBConnection con = null;

    public TitleTranslator() {
        try {
            this.con = new DBConnection("jar");
        }
        catch (Exception e) {
            e.printStackTrace();
        }
    }

    public static void main(String[] args) {
        TitleTranslator title = new TitleTranslator();
        title.translate();
    }

    public void translate() {
        long numberOfDocuments = this.con.getNumberOfTitlesInLanguage("de");
        int k = 0;
        while ((long)k < numberOfDocuments) {
            List documentDataList = new ArrayList();
            documentDataList = this.con.getTitlesInBatches(k, k + 1000);
            System.out.println(String.valueOf(k) + "/" + numberOfDocuments);
            k += 1000;
        }
    }
}
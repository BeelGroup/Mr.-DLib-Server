package org.mrdlib.recommendation.framework;

import java.util.AbstractMap;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Scanner;

import org.mrdlib.database.DBConnection;
import org.mrdlib.partnerContentManager.gesis.Tuple;

import com.opencsv.CSVReader;
import java.io.FileReader;
import java.io.IOException;

public class ManageStereotypes {

	public static void main(String[] args) {
		System.out.println("This module can only be used to manage the stereotype or most popular table."
				+ " This module takes as input a csv file in one of the two following specifications.");
		System.out.println("Document Id, Stereotype or Most Popular Category");
		System.out.println("OR");
		System.out.println("URL to Document, Stereotype or Most Popular Category");
		System.out.println("---------------------------------------------");
		System.out.println("The csv file should not contain a header row.");
		System.out.println("The csv file should have fields comma separated "
				+ "and lines split by a unix newline '\n' character");
		System.out.println("Please input the absolute path to the csv file now:");
		System.out.println("This path should be of the form: \\path\\to\\file.csv");
		Scanner sc = new Scanner(System.in);
		String path = sc.nextLine();
		if (path.equals("\n")) {
			System.out.println("No path inserted. Aborting!");
			return;
		}
		CSVReader reader = null;
		String[] line;
		boolean readLine = false;
		ArrayList<AbstractMap.SimpleEntry<String, String>> updates = new ArrayList<AbstractMap.SimpleEntry<String, String>>();
		try {
			reader = new CSVReader(new FileReader(path));
			while ((line = reader.readNext()) != null) {
				readLine = true;
				AbstractMap.SimpleEntry<String, String> entry = new AbstractMap.SimpleEntry<String, String>(line[0],
						line[1]);
				updates.add(entry);
			}
		} catch (IOException e) {
			System.out.println("There is no file in this path");
			sc.close();
			return;
		} catch (NullPointerException f) {
			System.out.println("The file is not in the prescribed format. The number of lines currently read is:");
			if (readLine) {
				System.out.println(reader.getLinesRead());
			} else {
				System.out.println("No lines have been read");
			}
			sc.close();
			try {
				reader.close();
			} catch (IOException e) {
				e.printStackTrace();
			}
			return;
		}

		DBConnection con;
		boolean updated = false;
		try {
			con = new DBConnection("jar");
			updated = con.updateStereotypes(updates);
		} catch (Exception e1) {
			// TODO Auto-generated catch block
			System.out.println(
					"Could not connect to Database. Please check the config files of this build that the db password is correct");
		}
		
		if(updated) System.out.println("Successfuly added " + updates.size() + " entries to the corresponding table");
		sc.close();
		try {
			reader.close();
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

}

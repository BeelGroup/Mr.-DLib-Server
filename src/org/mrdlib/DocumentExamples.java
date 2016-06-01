package org.mrdlib;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class DocumentExamples {
	public List<Related_article> getAllDocuments() {
		List<Related_article> documentList = null;
		try {
			File file = new File("Documents.dat");
			if (!file.exists()) {
				//Hardcode Example documents
				Related_article document1 = new Related_article("5524543354", "exmp-thepowerofscience-4423232", "thepowerofscience", 5,
						"The Power of Science. A. Einstein and J Doe. Journal of Science. 4:42. 1960",
						"&lt;span class='title'&gt;The Power of Science&lt;/span&gt;. &lt;span class='authors'&gt;A. Einstein and J Doe&lt;/span&gt;. &lt;span class='journal'&gt;Journal of Science&lt;/span&gt;. &lt;span class='volume_and_number'&gt;4:42&lt;/span&gt;. &lt;span class='year'&gt;1960&lt;/span&gt;",
						"https://api.mr-dlib.org/related_articles/5524543354/",
						"http://partner.com/library/documents/thepowerofscience/");
				/*RelatedArticle document2 = new RelatedArticle("This is the title of another recommendation", 2012,
						new ArrayList<>(Arrays.asList("Rebecca Reb", "Tanja Tan")),
						"http://api.docear.org/recommendation/389723/?action=direct_forward",
						"http://partner.com/document/he3hj32jhh", 84);*/
				documentList = new ArrayList<Related_article>();
				documentList.add(document1);
				//documentList.add(document2);
				saveDocumentList(documentList);
			} else {
				FileInputStream fis = new FileInputStream(file);
				ObjectInputStream ois = new ObjectInputStream(fis);
				documentList = (List<Related_article>) ois.readObject();
				ois.close();
			}
		} catch (IOException e) {
			e.printStackTrace();
		} catch (ClassNotFoundException e) {
			e.printStackTrace();
		}
		return documentList;
	}

	private void saveDocumentList(List<Related_article> documentList) {
		try {
			File file = new File("Documents.dat");
			FileOutputStream fos;

			fos = new FileOutputStream(file);

			ObjectOutputStream oos = new ObjectOutputStream(fos);
			oos.writeObject(documentList);
			oos.close();
		} catch (FileNotFoundException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		}
	}
}
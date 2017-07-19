package org.mrdlib.partnerContentManager.core;

import java.util.List;
import java.util.Arrays;
import java.util.ArrayList;

import org.mrdlib.api.response.DisplayDocument;

import org.junit.Before;
import org.junit.Test;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import static org.junit.Assert.*;
import static org.mockito.Mockito.*;

public class CoreCheckTest {


    private DocumentCheck check;

    @Before
    public void setUp() throws Exception {
        MockitoAnnotations.initMocks(this);
	check = new DocumentCheck();
    }

    // @Test
    public void getDocumentsFromDB() throws Exception {
	List<DisplayDocument> docs = check.getCoreDocumentsById(9506000);

	String[] titles = {
"Intellectual property and the EU rules on private international law: match or mismatch?",
"Re-anchorage of a ruptured tendon in bonded post-tensioned concrete beams: model validation",
"Modeling the re-anchoring of a ruptured tendon in bonded post-tensioned concrete",
"Constructing a social subject: autism and human sociality in the 1980s",
"Norms, normality and normalization: papers from the postgraduate summer school in German Studies, Nottingham, July 2013",
"www.sudacon.net for construction news in Sudan",
"Highly sensitive multipoint real-time kinetic detection of Surface Plasmon bioanalytes with custom CMOS cameras",
"Grey sets and greyness",
"Nottingham's Owd 'Oss Mummers and their scrapbooks",
"Rural Enterprise as an Agent for Technology Development and Facilitation in the Digital Economy"
	};

	String[] ids = {
"core-33563629",
"core-33563632",
"core-33563633",
"core-33563636",
"core-33563642",
"core-33563647",
"core-33562653",
"core-33562670",
"core-17179590",
"core-17179591"
	};
	List<Integer> idValues = Arrays.asList(new Integer[] {
33563629,
33563632,
33563633,
33563636,
33563642,
33563647,
33562653,
33562670,
17179590,
17179591
	});
	for (int i = 0; i < 10 && i < docs.size(); i++) {
	    DisplayDocument doc = docs.get(i);
	    assertEquals(titles[i], doc.getTitle());
	    assertEquals(ids[i], doc.getOriginalDocumentId());
	}
	assertEquals(idValues, check.getCoreIdsFromDocuments(docs.subList(0, 10)));
    }

}

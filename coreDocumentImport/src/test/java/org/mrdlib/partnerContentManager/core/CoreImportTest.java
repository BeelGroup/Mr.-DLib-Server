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

public class CoreImportTest {

    private DocumentImport importer;

    @Before
    public void setUp() throws Exception {
        MockitoAnnotations.initMocks(this);
		importer = new DocumentImport();
    }

    @Test
    public void testDocumentInDB() throws Exception {
		assertTrue(importer.hasDocumentInDB("core-10000"));
		assertFalse(importer.hasDocumentInDB("core-42"));
    }

}

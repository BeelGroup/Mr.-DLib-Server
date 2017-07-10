package org.mrdlib.partnerContentManager.general;

import java.util.Arrays;
import java.util.List;

import org.junit.Before;
import org.junit.Test;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import static org.junit.Assert.*;
import static org.mockito.Mockito.*;

public class LanguageDetectionTest {

    @Before
    public void setUp() throws Exception {
        MockitoAnnotations.initMocks(this);
    }

    @Test
    public void detectLanguages() throws Exception {
        List<String> text = Arrays.asList(new String[]{
            "Testsatz. Hier stehen Worte. Dies sollte als Deutsch erkannt werden. Das reicht.",
            "Test sentence. These are english words. Library. Natural language processing. Table. Apple."
        });
        List<String> result = LanguageDetection.detectLanguage(text);
        assertArrayEquals(result.toArray(), new String[]{"de", "en"});
    }
    

}
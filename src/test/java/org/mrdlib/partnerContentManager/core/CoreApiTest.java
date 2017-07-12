package org.mrdlib.partnerContentManager.core;

import org.mrdlib.partnerContentManager.core.model.*;

import java.util.Arrays;
import java.util.List;
import java.util.ArrayList;
import java.util.function.Consumer;
import java.util.function.Consumer;

import org.junit.Before;
import org.junit.Test;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import static org.junit.Assert.*;
import static org.mockito.Mockito.*;

@FunctionalInterface()
interface UncheckedConsumer<T> {
    void accept(T value) throws Exception;
}


public class CoreApiTest {


    private CoreApi api;

    @Before
    public void setUp() throws Exception {
        MockitoAnnotations.initMocks(this);
	api = new CoreApi();
    }

    // @Test
    public void articleBatchRequest() throws Exception {
	List<Integer> ids = Arrays.asList(new Integer[] {1});
	List<Article> articles = api.getArticles(ids);

	assertEquals(articles.size(), ids.size());

	Article a = articles.get(0);
	assertEquals(a.getTitle(), "The OU goes digital");
	assertEquals(a.getYear(), new Integer(2003));
	assertEquals(a.getAuthors().size(), 1);
	assertEquals(a.getAuthors().get(0), "Ramsden, Anne");
    }

    // @Test
    public void nonExistingArticleBatchRequest() throws Exception {
	List<Integer> ids = Arrays.asList(new Integer[] {-1});
	List<Article> articles = api.getArticles(ids);

	assertEquals(1, articles.size());
	assertNull(articles.get(0));
    }

    @Test
    public void articleListRequest() throws Exception {
	// test single page with limit
	// List<Article> articles = api.listArticles(2017, 0, 10);
	// assertEquals("simple fetching with limit", articles.size(), 10);
	// for (Article a : articles) {
	//     System.out.println("1: " + a.getTitle());
	//     assertNotNull(a);
	//     assertEquals("correct year", new Integer(2017), a.getYear());
	// }
	// // test page offset
	// List<Article> first = articles;
	// articles = api.listArticles(2017, 1, 10);
	// assertEquals(10, articles.size());
	// for (Article a : articles) {
	//     System.out.println("2: " + a.getTitle());
	//     assertNotNull(a);
	//     assertEquals(new Integer(2017), a.getYear());
	//     // should be distinct from first
	//     assertFalse("fetching with offset: results should be distinct", first.contains(a));
	// }
	// paging
	UncheckedConsumer<Integer> fetchNArticles = (Integer size) -> {
	    List<Article> list = api.listArticles(2017, 0, size);
	    List<Article> other = new ArrayList(size);
	    assertEquals(size.intValue(), list.size());
	    for (Article a : list) {
		// no duplicates
		other.clear();
		other.addAll(list);
		other.remove(a);
		assertFalse("no duplicates", other.contains(a));
	    }
	};
	// fetchNArticles.accept(CoreApi.MAX_PAGE_SIZE * 2); // one reques
	fetchNArticles.accept(CoreApi.MAX_PAGE_SIZE * (CoreApi.MAX_BATCH_SIZE + 1)); // multiple requests
    }

}

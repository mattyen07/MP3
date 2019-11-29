package cpen221.mp3;

import cpen221.mp3.cache.NotFoundException;
import cpen221.mp3.wikimediator.WikiMediator;
import cpen221.mp3.cache.Cache;
import cpen221.mp3.cache.CacheObject;
import fastily.jwiki.core.Wiki;
import org.junit.Test;

import java.util.Set;
import java.util.HashSet;
import java.util.Collections;
import java.util.List;
import java.util.ArrayList;
import java.util.concurrent.TimeUnit;

import static org.junit.Assert.*;

public class Tests {
    /*
        You can add your tests here.
        Remember to import the packages that you need, such
        as cpen221.mp3.cache.
     */

    @Test
    public void simpleSearchTest1() {
        WikiMediator wm = new WikiMediator();
        List<String> answer;
        Wiki wiki = new Wiki("en.wikipedia.org");
        answer = wiki.search("Barack Obama", 5);
        assertEquals(answer, wm.simpleSearch("Barack Obama", 5));
    }

    @Test
    public void simpleSearchTest2() {
        WikiMediator wm = new WikiMediator();
        List<String> answer;
        Wiki wiki = new Wiki("en.wikipedia.org");
        answer = wiki.search("Barack Obama", 3);
        wm.simpleSearch("Barack Obama", 2);
        assertEquals(answer, wm.simpleSearch("Barack Obama", 3));
    }
    @Test
    public void simpleSearchTest3() {
        WikiMediator wm = new WikiMediator();
        List<String> answer = new ArrayList<>();
        assertEquals(answer, wm.simpleSearch("Barack Obama", 0));
    }

    @Test
    public void getPageTest1() {
        WikiMediator wm = new WikiMediator();
        String answer;
        Wiki wiki = new Wiki("en.wikipedia.org");
        answer = wiki.getPageText("Barack Obama");
        assertEquals(answer, wm.getPage("Barack Obama"));
    }

    @Test
    public void getPageTest2() {
        WikiMediator wm = new WikiMediator();
        String answer;
        Wiki wiki = new Wiki("en.wikipedia.org");
        answer = wiki.getPageText("Barack Obama");
        wm.getPage("Barack Obama");
        assertEquals(answer, wm.getPage("Barack Obama"));
    }

    @Test
    public void getConnectedPagesTest1() {
        WikiMediator wm = new WikiMediator();
        List<String> answer = new ArrayList<>();

        answer.add("Barack Obama");

        assertEquals(answer, wm.getConnectedPages("Barack Obama", 0));
    }
    @Test
    public void getConnectedPagesTest2() {
        WikiMediator wm = new WikiMediator();
        Wiki wiki = new Wiki("en.wikipedia.org");
        wiki.enableLogging(false);
        List<String> answer = new ArrayList<>();

        answer.add("Barack Obama");
        answer.addAll(wiki.getLinksOnPage("Barack Obama"));
        Collections.sort(answer);

        assertEquals(answer, wm.getConnectedPages("Barack Obama", 1));
    }
    @Test
    public void getConnectedPagesTest3() {
        WikiMediator wm = new WikiMediator();
        Wiki wiki = new Wiki("en.wikipedia.org");
        wiki.enableLogging(false);
        Set<String> answer = new HashSet<>();

        answer.add("Galojan");
        answer.addAll(wiki.getLinksOnPage("Galojan"));
        for (String title : wiki.getLinksOnPage("Galojan")) {
            answer.addAll(wiki.getLinksOnPage(title));
        }

        List<String> answerList = new ArrayList<>(answer);
        Collections.sort(answerList);

        assertEquals(answerList, wm.getConnectedPages("Galojan", 2));
    }

    @Test
    public void zeitgeistTest1() {
        WikiMediator wm = new WikiMediator();
        List<String> answer = new ArrayList<>();
        wm.simpleSearch("Obama", 1);
        wm.simpleSearch("Obama", 2);
        wm.simpleSearch("Obama", 3);
        wm.getPage("hockey");
        wm.getPage("hockey");
        wm.getPage("soccer");
        wm.getPage("soccer");
        wm.getPage("ultimate");
        answer.add("Obama");
        answer.add("hockey");

        assertEquals(answer, wm.zeitgeist(2));
    }

    @Test
    public void zeitgeistTest2() {
        WikiMediator wm = new WikiMediator();
        List<String> answer = new ArrayList<>();
        wm.simpleSearch("Obama", 1);
        wm.simpleSearch("Obama", 2);
        wm.simpleSearch("Obama", 3);
        wm.getPage("hockey");
        wm.getPage("hockey");
        wm.getPage("soccer");
        wm.getPage("soccer");
        wm.getPage("ultimate");
        answer.add("Obama");
        answer.add("hockey");
        answer.add("soccer");

        assertEquals(answer, wm.zeitgeist(3));
    }

    @Test
    public void zeitgeistTest3() {
        WikiMediator wm = new WikiMediator();
        List<String> answer = new ArrayList<>();
        wm.simpleSearch("Obama", 1);
        wm.simpleSearch("Obama", 2);
        wm.simpleSearch("Obama", 3);
        wm.getPage("hockey");
        wm.getPage("hockey");
        wm.getPage("soccer");
        wm.getPage("soccer");
        wm.getPage("ultimate");

        assertEquals(answer, wm.zeitgeist(0));
    }

    @Test
    public void trendingTest1() {
        WikiMediator wm = new WikiMediator();
        List<String> answer = new ArrayList<>();
        wm.simpleSearch("Obama", 1);
        wm.simpleSearch("Obama", 2);
        wm.simpleSearch("Obama", 3);
        wm.getPage("hockey");
        wm.getPage("hockey");
        wm.getPage("soccer");
        wm.getPage("soccer");
        wm.getPage("ultimate");
        answer.add("Obama");
        answer.add("hockey");
        answer.add("soccer");

        assertEquals(answer, wm.trending(3));
    }

    @Test
    public void trendingTest2() {
        WikiMediator wm = new WikiMediator();
        List<String> answer = new ArrayList<>();
        wm.simpleSearch("Obama", 1);
        wm.simpleSearch("Obama", 2);
        wm.simpleSearch("Obama", 3);
        wm.getPage("hockey");
        try {
            TimeUnit.SECONDS.sleep(30);
        } catch (InterruptedException e) {
            fail();
        }
        wm.getPage("hockey");
        wm.getPage("soccer");
        wm.getPage("soccer");
        wm.getPage("ultimate");
        wm.getPage("hockey");
        answer.add("hockey");
        answer.add("soccer");
        answer.add("ultimate");

        assertEquals(answer, wm.trending(3));
    }

    @Test
    public void trendingTest3() {
        WikiMediator wm = new WikiMediator();
        List<String> answer = new ArrayList<>();
        wm.simpleSearch("Obama", 1);
        wm.simpleSearch("Obama", 2);
        wm.simpleSearch("Obama", 3);
        wm.getPage("hockey");
        try {
            TimeUnit.SECONDS.sleep(30);
        } catch (InterruptedException e) {
            fail();
        }
        wm.getPage("hockey");
        wm.getPage("soccer");
        wm.getPage("soccer");
        wm.getPage("ultimate");
        wm.getPage("hockey");
        wm.simpleSearch("soccer", 3);
        answer.add("soccer");
        answer.add("hockey");
        answer.add("ultimate");

        assertEquals(answer, wm.trending(3));
    }

    @Test
    public void trendingTest4() {
        WikiMediator wm = new WikiMediator();
        List<String> answer = new ArrayList<>();
        wm.simpleSearch("Obama", 1);
        wm.simpleSearch("Obama", 2);
        wm.simpleSearch("Obama", 3);
        wm.getPage("hockey");
        wm.getPage("hockey");
        wm.getPage("soccer");
        wm.getPage("soccer");
        wm.getPage("ultimate");

        assertEquals(answer, wm.trending(0));
    }

    @Test
    public void peakLoadTest1() {
        WikiMediator wm = new WikiMediator();

        wm.simpleSearch("Obama", 1);
        wm.simpleSearch("Obama", 2);
        wm.simpleSearch("Obama", 3);
        wm.getPage("hockey");
        wm.getPage("hockey");
        wm.getPage("soccer");
        wm.getPage("soccer");
        wm.getPage("ultimate");
        wm.getPage("hockey");
        wm.simpleSearch("soccer", 3);

        assertEquals(11, wm.peakLoad30s());
    }

    @Test
    public void peakLoadTest2() {
        WikiMediator wm = new WikiMediator();

        wm.simpleSearch("Obama", 1);
        wm.simpleSearch("Obama", 2);
        wm.simpleSearch("Obama", 3);
        wm.getPage("hockey");
        wm.getPage("hockey");
        wm.getPage("soccer");
        wm.getPage("soccer");
        try {
            TimeUnit.SECONDS.sleep(30);
        } catch (InterruptedException e) {
            fail();
        }
        wm.getPage("ultimate");
        wm.getPage("hockey");
        wm.simpleSearch("soccer", 3);

        assertEquals(7, wm.peakLoad30s());
    }

    @Test
    public void peakLoadTest3() {
        WikiMediator wm = new WikiMediator();

        wm.simpleSearch("Obama", 1);
        wm.simpleSearch("Obama", 2);
        wm.simpleSearch("Obama", 3);
        wm.getPage("hockey");
        wm.getPage("hockey");
        wm.getPage("soccer");
        wm.getPage("soccer");
        try {
            TimeUnit.SECONDS.sleep(31);
        } catch (InterruptedException e) {
            fail();
        }
        wm.getPage("ultimate");
        wm.getPage("hockey");
        wm.simpleSearch("soccer", 3);

        assertEquals(7, wm.peakLoad30s());
    }

    @Test
    public void peakLoadTest4() {
        WikiMediator wm = new WikiMediator();

        wm.simpleSearch("Obama", 1);
        wm.simpleSearch("Obama", 2);
        wm.simpleSearch("Obama", 3);
        wm.getPage("hockey");
        wm.getPage("hockey");
        wm.getPage("soccer");
        wm.getPage("soccer");
        wm.getPage("ultimate");
        wm.getPage("hockey");
        try {
            TimeUnit.SECONDS.sleep(40);
        } catch (InterruptedException e) {
            fail();
        }
        wm.simpleSearch("soccer", 3);

        assertEquals(9, wm.peakLoad30s());
    }

    @Test
    public void peakLoadTest5() {
        WikiMediator wm = new WikiMediator();

        wm.simpleSearch("Obama", 1);
        wm.simpleSearch("Obama", 2);
        wm.simpleSearch("Obama", 3);
        try {
            TimeUnit.SECONDS.sleep(35);
        } catch (InterruptedException e) {
            fail();
        }
        wm.zeitgeist(2);
        wm.getPage("hockey");
        wm.getPage("hockey");
        wm.getPage("soccer");
        wm.getPage("soccer");
        wm.getPage("ultimate");
        wm.getPage("hockey");
        wm.simpleSearch("soccer", 3);

        assertEquals(9, wm.peakLoad30s());
    }
    @Test
    public void peakLoadTest6() {
        WikiMediator wm = new WikiMediator();

        assertEquals(1, wm.peakLoad30s());
    }

    @Test
    public void peakLoadTest7() {
        WikiMediator wm = new WikiMediator();
        wm.getConnectedPages("Galojan", 1);
        wm.simpleSearch("hockey", 3);
        wm.getPage("hockey");
        wm.getPage("soccer");
        wm.getPage("hockey");
        wm.zeitgeist(3);
        wm.peakLoad30s();
        wm.trending(4);
        wm.simpleSearch("soccer", 9);
        try {
            TimeUnit.SECONDS.sleep(35);
        } catch (InterruptedException e) {
            fail();
        }
        wm.getConnectedPages("hockey", 0);
        wm.getPage("hockey");
        wm.simpleSearch("hockey", 20);
        wm.getPage("hockey");
        wm.trending(3);
        wm.zeitgeist(2);
        wm.getPage("soccer");

        assertEquals(9, wm.peakLoad30s());
    }

    @Test
    public void putTest1() {
        Cache cache = new Cache();
        CacheObject co = new CacheObject("hockey", "aa");
        assertTrue(cache.put(co));
    }

    @Test
    public void putTest2() {
        Cache cache = new Cache(1, 5);
        CacheObject co = new CacheObject("hockey", "aa");
        CacheObject co1 = new CacheObject("soccer", "aa");
        cache.put(co);
        assertTrue(cache.put(co1));
    }

    @Test
    public void putTest3() {
        Cache cache = new Cache(1, 5);
        CacheObject co = new CacheObject("hockey", "aa");
        cache.put(co);
        assertFalse(cache.put(co));
    }

    @Test
    public void putTest4() {
        Cache cache = new Cache(2, 30);
        CacheObject co = new CacheObject("hockey", "aa");
        cache.put(co);
        assertFalse(cache.put(co));
    }

    @Test
    public void putTest5() {
        Cache cache = new Cache(2, 30);
        CacheObject co = new CacheObject("hockey", "aa");
        CacheObject co1 = new CacheObject("soccer", "aa");
        CacheObject co2 = new CacheObject("ultimate", "aa");
        cache.put(co);
        try {
            TimeUnit.SECONDS.sleep(2);
        } catch (InterruptedException e) {
            fail();
        }
        cache.put(co1);

        assertTrue(cache.put(co2));
    }

    @Test
    public void putTest6() {
        Cache cache = new Cache(2, 30);
        CacheObject co = new CacheObject("hockey", "aa");
        CacheObject co1 = new CacheObject("soccer", "aa");
        cache.put(co);
        try {
            TimeUnit.SECONDS.sleep(2);
        } catch (InterruptedException e) {
            fail();
        }
        cache.put(co1);

        assertFalse(cache.put(co1));
    }

    @Test
    public void getTest1() throws NotFoundException {
        Cache cache = new Cache();
        CacheObject co = new CacheObject("hockey", "aa");
        cache.put(co);
        assertEquals(co, cache.get(co.id()));
    }

    @Test (expected = NotFoundException.class)
    public void getTest2() throws NotFoundException {
        Cache cache = new Cache();
        CacheObject co = new CacheObject("hockey", "aa");
        assertEquals(co, cache.get(co.id()));
    }

    @Test (expected = NotFoundException.class)
    public void getTest3() throws NotFoundException {
        Cache cache = new Cache(3, 3);
        CacheObject co = new CacheObject("hockey", "aa");
        cache.put(co);
        try {
            TimeUnit.SECONDS.sleep(4);
        } catch (InterruptedException e) {
            fail();
        }
        assertEquals(co, cache.get(co.id()));
    }

    @Test (expected = NotFoundException.class)
    public void getTest4() throws NotFoundException {
        Cache cache = new Cache(2, 30);
        CacheObject co = new CacheObject("hockey", "aa");
        CacheObject co1 = new CacheObject("soccer", "aa");
        CacheObject co2 = new CacheObject("ultimate", "aa");
        cache.put(co);
        cache.put(co1);

        try {
            TimeUnit.SECONDS.sleep(1);
            cache.get(co.id());
        } catch (InterruptedException e) {
            fail();
        }

        cache.put(co2);
        assertEquals(co1, cache.get(co1.id()));
    }

    @Test
    public void touchTest1() {
        Cache cache = new Cache();
        CacheObject co = new CacheObject("hockey", "aa");
        cache.put(co);
        try {
            TimeUnit.SECONDS.sleep(2);
        } catch (InterruptedException e) {
            fail();
        }
        assertTrue(cache.touch(co.id()));
    }

    @Test
    public void touchTest2() {
        Cache cache = new Cache();
        CacheObject co = new CacheObject("hockey", "aa");
        assertFalse(cache.touch(co.id()));
    }

    @Test
    public void touchTest3() {
        Cache cache = new Cache();
        CacheObject co = new CacheObject("hockey", "aa");
        CacheObject co1 = new CacheObject("soccer", "aa");
        cache.put(co);
        assertFalse(cache.touch(co1.id()));
    }

    @Test
    public void updateTest1() {
        Cache cache = new Cache();
        CacheObject co = new CacheObject("hockey", "aa");
        cache.put(co);
        try {
            TimeUnit.SECONDS.sleep(2);
        } catch (InterruptedException e) {
            fail();
        }
        assertTrue(cache.update(co));
    }

    @Test
    public void updateTest2() {
        Cache cache = new Cache();
        CacheObject co = new CacheObject("hockey", "aa");
        try {
            TimeUnit.SECONDS.sleep(2);
        } catch (InterruptedException e) {
            fail();
        }
        assertFalse(cache.update(co));
    }
}

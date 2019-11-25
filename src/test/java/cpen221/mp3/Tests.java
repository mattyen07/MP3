package cpen221.mp3;

import cpen221.mp3.cache.CacheObject;
import cpen221.mp3.wikimediator.WikiMediator;
import fastily.jwiki.core.Wiki;
import org.junit.Test;

import java.util.Set;
import java.util.HashSet;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import java.util.List;
import java.util.ArrayList;

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
        List<String> answer = new ArrayList<>();
        Wiki wiki = new Wiki("en.wikipedia.org");
        answer = wiki.search("Barack Obama", 5);
        assertEquals(answer, wm.simpleSearch("Barack Obama", 5));
    }

    @Test
    public void simpleSearchTest2() {
        WikiMediator wm = new WikiMediator();
        List<String> answer;
        Wiki wiki = new Wiki("en.wikipedia.org");
        answer = wiki.search("Barack Obama", 10);
        wm.simpleSearch("Barack Obama", 2);
        assertEquals(answer, wm.simpleSearch("Barack Obama", 10));
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
        wm.getPage("china");
        answer.add("hockey");
        answer.add("Obama");

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
        wm.getPage("china");
        answer.add("soccer");
        answer.add("hockey");
        answer.add("Obama");

        assertEquals(answer, wm.zeitgeist(3));
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
        wm.getPage("china");
        answer.add("soccer");
        answer.add("hockey");
        answer.add("Obama");

        assertEquals(answer, wm.trending(3));
    }

    @Test
    public void trendingTest2() {
        WikiMediator wm = new WikiMediator();
        List<String> answer = new ArrayList<>();
        long time = System.currentTimeMillis();
        wm.simpleSearch("Obama", 1);
        wm.simpleSearch("Obama", 2);
        wm.simpleSearch("Obama", 3);
        wm.getPage("hockey");
        while(System.currentTimeMillis() < (time + 30*1000)) {

        }
        wm.getPage("hockey");
        wm.getPage("soccer");
        wm.getPage("soccer");
        wm.getPage("china");
        wm.getPage("hockey");
        answer.add("china");
        answer.add("soccer");
        answer.add("hockey");

        assertEquals(answer, wm.trending(3));
    }

    @Test
    public void trendingTest3() {
        WikiMediator wm = new WikiMediator();
        List<String> answer = new ArrayList<>();
        long time = System.currentTimeMillis();
        wm.simpleSearch("Obama", 1);
        wm.simpleSearch("Obama", 2);
        wm.simpleSearch("Obama", 3);
        wm.getPage("hockey");
        while(System.currentTimeMillis() < (time + 30*1000)) {

        }
        wm.getPage("hockey");
        wm.getPage("soccer");
        wm.getPage("soccer");
        wm.getPage("china");
        wm.getPage("hockey");
        wm.simpleSearch("soccer", 3);
        answer.add("china");
        answer.add("hockey");
        answer.add("soccer");

        assertEquals(answer, wm.trending(3));
    }
}

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
}

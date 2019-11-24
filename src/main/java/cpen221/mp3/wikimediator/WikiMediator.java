package cpen221.mp3.wikimediator;

import java.util.List;
import java.util.ArrayList;
import java.util.Set;
import java.util.HashSet;
import java.util.Map;
import java.util.HashMap;
import java.util.Collections;

import cpen221.mp3.cache.Cache;
import cpen221.mp3.cache.CacheObject;
import cpen221.mp3.cache.Cacheable;
import cpen221.mp3.cache.NotFoundException;
import fastily.jwiki.core.Wiki;

public class WikiMediator {

    /* TODO: Implement this datatype

        You must implement the methods with the exact signatures
        as provided in the statement for this mini-project.

        You must add method signatures even for the methods that you
        do not plan to implement. You should provide skeleton implementation
        for those methods, and the skeleton implementation could return
        values like null.

     */
    private Wiki wiki;
    private Cache cache;
    private Map<String, Integer> popularityMap;

    public WikiMediator() {
        this.wiki = new Wiki("en.wikipedia.org");
        this.wiki.enableLogging(false);
        this.popularityMap = new HashMap<>();
        this.cache = new Cache<>(256,3600);
    }

    public List<String> simpleSearch(String query, int limit) {
        if (popularityMap.containsKey(query)) {
            int count = popularityMap.get(query);
            count++;
            popularityMap.replace(query, count);
        } else {
            popularityMap.put(query, 1);
        }

        return this.wiki.search(query, limit);
    }

    public String getPage(String pageTitle) {
        String text;
        if (popularityMap.containsKey(pageTitle)) {
            int count = popularityMap.get(pageTitle);
            count++;
            popularityMap.replace(pageTitle, count);
        } else {
            popularityMap.put(pageTitle, 1);
        }

        if (cache.get(pageTitle) == null) {
            text = this.wiki.getPageText(pageTitle);
            cache.put(new CacheObject(pageTitle, this.wiki));
        } else {
            CacheObject co = (CacheObject) cache.get(pageTitle);
            text = co.getText();
            cache.update(co);
        }

        return text;
    }

    public List<String> getConnectedPages(String pageTitle, int hops) {
        Set<String> pageLinks = new HashSet<>();

        pageLinks.add(pageTitle);

        if (hops == 0) {
            return new ArrayList<>(pageLinks);
        } else {
            pageLinks.addAll(getConnectedPagesHelper(pageTitle, hops));
        }

        ArrayList<String> connectedPages = new ArrayList<>(pageLinks);
        Collections.sort(connectedPages);
        return connectedPages;
    }

    private List<String> getConnectedPagesHelper(String pageTitle, int hops) {
        List<String> allPages = new ArrayList<>();
        List<String> titleOnly = new ArrayList<>();

        if (hops <= 0) {
            titleOnly.add(pageTitle);
            return titleOnly;
        }

        allPages.addAll(this.wiki.getLinksOnPage(pageTitle));
        hops--;
        for (String title : this.wiki.getLinksOnPage(pageTitle)) {
            allPages.addAll(getConnectedPagesHelper(title, hops));
        }

        return allPages;
    }

    public List<String> zeitgeist(int limit) {
        List<String> mostCommon = new ArrayList<>();
        int maxOccurrences = 0;
        int count = 0;
        String mostOccurringSearch = "";

        while (count < limit) {
            for (String search : this.popularityMap.keySet()) {
                if (this.popularityMap.get(search) > maxOccurrences && !mostCommon.contains(search)) {
                    maxOccurrences = this.popularityMap.get(search);
                    mostOccurringSearch = search;
                }
            }
            count++;
            mostCommon.add(mostOccurringSearch);
        }

        Collections.reverse(mostCommon);

        return mostCommon;
    }

    public List<String> trending(int limit) {
        return null;
    }

    public int peakLoad30s() {
        return 0;
    }

    /* Task 3 */
    public List<String> getPath(String startPage, String stopPage) {
        return null;
    }

    public List<String> excuteQuery(String query) {
        return null;
    }

}

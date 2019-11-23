package cpen221.mp3.wikimediator;

import java.util.List;
import java.util.ArrayList;
import java.util.Set;
import java.util.HashSet;

import cpen221.mp3.cache.Cache;
import cpen221.mp3.cache.CacheObject;
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

    public WikiMediator() {
        this.wiki = new Wiki("en.wikipedia.org");
        this.wiki.enableLogging(false);
        this.cache = new Cache(256,3600);
    }

    public List<String> simpleSearch(String query, int limit) {
        List<String> queryList = this.wiki.search(query, limit);

        for (String pageTitle : queryList) {
            CacheObject addToCache = new CacheObject (pageTitle, this.wiki);
            this.cache.put(addToCache);
        }

        return queryList;
    }

    public String getPage(String pageTitle) {
        String text = this.wiki.getPageText(pageTitle);
        CacheObject addToCache = new CacheObject (pageTitle, this.wiki);
        this.cache.put(addToCache);
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

        return new ArrayList<>(pageLinks);
    }

    private Set<String> getConnectedPagesHelper(String pageTitle, int hops) {
        Set<String> allPages = new HashSet<>();
        Set<String> titleOnly = new HashSet<>();

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
        return null;
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

package cpen221.mp3.wikimediator;

import java.util.List;
import java.util.ArrayList;
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

    public WikiMediator() {
        this.wiki = new Wiki("en.wikipedia.org");
        wiki.enableLogging(false);
    }

    public List<String> simpleSearch(String query, int limit) {
        List<String> queryList = this.wiki.search(query, limit);

        return queryList;
    }

    public String getPage(String pageTitle) {
        String text = this.wiki.getPageText(pageTitle);
        return text;
    }

    public List<String> getConnectedPages(String pageTitle, int hops) {
        int count = 0;
        List<String> pageLinks = new ArrayList<>();

        while (count < hops) {


        }

        return null;
    }

}

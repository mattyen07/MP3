package cpen221.mp3.wikimediator;

import java.time.LocalDateTime;
import java.util.List;
import java.util.ArrayList;
import java.util.Set;
import java.util.HashSet;
import java.util.Map;
import java.util.Collections;
import java.util.concurrent.ConcurrentHashMap;

import cpen221.mp3.cache.Cache;
import cpen221.mp3.cache.CacheObject;
import cpen221.mp3.cache.NotFoundException;
import fastily.jwiki.core.Wiki;

public class WikiMediator {
    /*
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
    private Map<String, List<LocalDateTime>> timeMap;
    private Map<String, List<LocalDateTime>> requestMap;
    private LocalDateTime startTime;

    public WikiMediator() {
        this.wiki = new Wiki("en.wikipedia.org");
        this.wiki.enableLogging(false);
        this.popularityMap = new ConcurrentHashMap<>();
        this.timeMap = new ConcurrentHashMap<>();
        this.requestMap = new ConcurrentHashMap<>();
        this.cache = new Cache<>(256,43200);
        this.startTime = LocalDateTime.now();

        this.requestMap.put("search", new ArrayList<>());
        this.requestMap.put("getPage", new ArrayList<>());
        this.requestMap.put("connectedPages", new ArrayList<>());
        this.requestMap.put("zeitgeist", new ArrayList<>());
        this.requestMap.put("trending", new ArrayList<>());
        this.requestMap.put("peakLoad", new ArrayList<>());
    }

    /**
     * A simple search function that returns a list of pages that match the query string
     * @param query is a String to use with Wikipedia's search function
     * @param limit is the maximum number of items that simpleSearch will return
     * @modifies requestMap, adds a time the method was called into the request map
     * @return  a list of strings with limit strings that appear from the query using
     * wikipedia's search service.
     * If limit is equal to 0, returns an empty list of strings
     */
    public List<String> simpleSearch(String query, int limit) {
        addToMaps(query);
        List<LocalDateTime> requestDates = this.requestMap.get("search");
        requestDates.add(LocalDateTime.now());
        this.requestMap.replace("search", requestDates);

        if (limit == 0) {
            return new ArrayList<>();
        } else {
            return this.wiki.search(query, limit);
        }
    }

    /**
     * Returns the page text of a given page title. If the page title has been requested already
     * the page text will be obtained from the cache instead of accessing wikipedia
     * @param pageTitle is a page that we wish to find the wikipedia page
     * @modifies requestMap, adds a time the method was called into the request map
     * @return a string that contains the text of the given page title
     */
    public String getPage(String pageTitle) {
        String text;
        addToMaps(pageTitle);

        try {
            CacheObject co = (CacheObject) this.cache.get(pageTitle);
            text = co.getText();
            this.cache.update(co);
        } catch (NotFoundException e) {
            text = this.wiki.getPageText(pageTitle);
            this.cache.put(new CacheObject(pageTitle, text));
        }

        List<LocalDateTime> requestDates = this.requestMap.get("getPage");
        requestDates.add(LocalDateTime.now());
        this.requestMap.replace("getPage", requestDates);
        return text;
    }

    /**
     * Helper method to add request to the instance popularity map and time map.
     * Method is synchronized so only one thread can access and add to map at the same time
     * @param request the query or pageTitle to be added to the map
     * @modifies popularityMap, adds a query or pageTitle if it is not in the map,
     *                          otherwise, increases the count of the key by 1
     * @modifies timeMap, adds a query or pageTitle if it is not in the map,
     *                    otherwise, adds the current time to the list of times the
     *                    request has been accessed
     */
    synchronized private void addToMaps(String request) {
        if (popularityMap.containsKey(request)) {
            int count = popularityMap.get(request);
            count++;
            popularityMap.replace(request, count);
        } else {
            popularityMap.put(request, 1);
        }

        if(!timeMap.containsKey(request)) {
            ArrayList<LocalDateTime> timeList = new ArrayList<>();
            timeList.add(LocalDateTime.now());
            timeMap.put(request, timeList);
        } else {
            List<LocalDateTime> timeList = timeMap.get(request);
            timeList.add(LocalDateTime.now());
            timeMap.replace(request, timeList);
        }
    }

    /**
     * Find a list of pages that are connected to the given page in a certain number of hops
     * @param pageTitle The starting page
     * @param hops The number of links we jump through
     * @modifies requestMap, adds a time the method was called into the request map
     * @return A list of pages that reachable within a certain number of hops.
     * Removes all duplicate pages and thus, only returns a list of unique pages that can
     * be found through links from the initial pageTitle
     */
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

        List<LocalDateTime> requestDates = this.requestMap.get("connectedPages");
        requestDates.add(LocalDateTime.now());
        this.requestMap.replace("connectedPages", requestDates);
        return connectedPages;
    }

    /**
     * Recursive Helper method for getConnectedPages
     * Base Case is if hops <=0, returns a list of just the current page title
     * otherwise subtracts 1 from hops and calls helper again for each link in the list
     * @param pageTitle initial page we are starting from
     * @param hops The number of links to jump through
     * @modifies requestMap, adds a time the method was called into the request map
     * @return a list of pages that can be found within a certain number of hops
     * starting from pageTitle
     */
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

    /**
     * Returns a list of the most common strings used in the simple search and getPage methods
     * @param limit the maximum number of items to return from the method call
     * @modifies requestMap, adds a time the method was called into the request map
     * @return a list of strings where strings are sorted by the amount of times they have been
     * called by the getPage or simple search method. These strings are sorted into non-increasing
     * order of appearance.
     */
    public List<String> zeitgeist(int limit) {
        List<String> mostCommon = new ArrayList<>();
        int maxOccurrences;
        int count = 0;
        String mostOccurringSearch = "";

        while (count < limit) {
            maxOccurrences = 0;
            for (String search : this.popularityMap.keySet()) {
                if (this.popularityMap.get(search) > maxOccurrences && !mostCommon.contains(search)) {
                    maxOccurrences = this.popularityMap.get(search);
                    mostOccurringSearch = search;
                }
            }
            count++;
            mostCommon.add(mostOccurringSearch);
        }

        List<LocalDateTime> requestDates = this.requestMap.get("zeitgeist");
        requestDates.add(LocalDateTime.now());
        this.requestMap.replace("zeitgeist", requestDates);
        return mostCommon;
    }

    /**
     * Returns a list of the most common Strings used in the getPage and simpleSearch method
     * from the past 30 seconds
     * @param limit the maximum number of items to return from method call
     * @modifies requestMap, adds a time the method was called into the request map
     * @return a list of strings where strings are sorted by the amount of times they have been
     * called by the getPage or simple search method. These strings are sorted into non-increasing
     * order of appearance.
     */
    public List<String> trending(int limit) {
        List<String> trendingList = new ArrayList<>();
        LocalDateTime currentTime = LocalDateTime.now();
        Map<String, Integer> frequencyList = new ConcurrentHashMap<>();

        for (String request : this.timeMap.keySet()) {
            List<LocalDateTime> requestList= this.timeMap.get(request);
            int count = 0;
            for (LocalDateTime time : requestList) {
                LocalDateTime compareTime = currentTime.minusSeconds(30);
                if (time.isAfter(compareTime)) {
                    count++;
                }
            }
            frequencyList.put(request, count);
        }

        int limitCount = 0;
        int maxFrequency;
        String frequencyString = "";

        while (limitCount < limit) {
            maxFrequency = 0;
            for (String request : frequencyList.keySet()) {
                if (frequencyList.get(request) > maxFrequency && !trendingList.contains(request)) {
                    frequencyString = request;
                    maxFrequency = frequencyList.get(request);
                }
            }

            limitCount++;
            trendingList.add(frequencyString);
        }

        List<LocalDateTime> requestDates = this.requestMap.get("trending");
        requestDates.add(LocalDateTime.now());
        this.requestMap.replace("trending", requestDates);
        return trendingList;
    }

    /**
     * Returns the maximum number of requests in any 30 second interval during the duration of
     * an instance of WikiMediator
     * @modifies requestMap, adds a time the method was called into the request map
     * @return the maximum number of request in any 30 second interval. Will always be >= 1
     *
     */
    public int peakLoad30s() {
        List<LocalDateTime> requestDates = this.requestMap.get("peakLoad");
        requestDates.add(LocalDateTime.now());
        this.requestMap.replace("peakLoad", requestDates);
        LocalDateTime startingTime = this.startTime;
        LocalDateTime endTime = LocalDateTime.now().minusSeconds(29);
        int maxLoad = 0;
        List<Integer> intervalRequestsList = new ArrayList<>();

        if (endTime.isBefore(startingTime)) {
            for (String request : this.requestMap.keySet()) {
                maxLoad += this.requestMap.get(request).size();
            }
            return maxLoad;
        }

        while (startingTime.isBefore(endTime)) {
            int intervalRequests = 0;
            LocalDateTime intervalTime = startingTime.plusSeconds(30);
            for (String request : this.requestMap.keySet()) {
                for (LocalDateTime time : this.requestMap.get(request)) {
                    if (time.isBefore(intervalTime) && (time.isAfter(startingTime) || time.isEqual(startingTime))) {
                        intervalRequests++;
                    }
                }
            }
            intervalRequestsList.add(intervalRequests);
            startingTime = startingTime.plusSeconds(1);
        }

        for (int intervalLoads : intervalRequestsList) {
            if (intervalLoads > maxLoad) {
                maxLoad = intervalLoads;
            }
        }

        return maxLoad;
    }

    /* Task 3 */
    public List<String> getPath(String startPage, String stopPage) {
        return null;
    }

    public List<String> executeQuery(String query) {
        return null;
    }

}

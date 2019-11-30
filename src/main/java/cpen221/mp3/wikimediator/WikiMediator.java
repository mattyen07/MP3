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
     RI: methodNames is not null and contains all public methods within the WikiMediator Class
         wiki is not null and is the English domain of Wikipedia
         cache is not null
         timeMap is not null. All times in the map must be after this.startTime
         requestMap is not null. All times in the map must be after this.startTime
         startTime is not null
     */

    /*
    AF(wm): A mediator between the user and wikipedia such that
            cache is the cache used by the WikiMediator
            wiki is the instance of wikipedia used by the wikiMediator.
            timeMap is a map of all searches/queries that are made to the times that they were made.
            requestMap is a map of all method calls to the times that said methods were called.
            methodNames is an array of all non-constructor public methods.


     */

    /* Default Cache Capacity */
    private static final int DEFAULTCAPACITY = 256;

    /* Default Cache Expiry Time */
    private static final int DEFAULTTIMEOUT = 43200;

    /* The Wikipedia Instance of the WikiMediator */
    private Wiki wiki;

    /* The Cache Instance of the WikiMediator */
    private Cache cache;

    /* The time map of searches and queries (strings) to the time they were made */
    private Map<String, List<LocalDateTime>> timeMap;

    /* The request map of a method to the time the method was called */
    private Map<String, List<LocalDateTime>> requestMap;

    /* The starting time of the WikiMediator */
    private LocalDateTime startTime;

    /* The names of all methods in the WikiMediator Class */
    private final String[] methodNames =
            new String[]{"simpleSearch", "getPage", "getConnectedPages",
                    "zeitgeist", "trending", "peakLoad30s", "getPath", "executeQuery"};

    /**
     * Constructs an instance of the WikiMediator.
     * This instance creates a new English Wikipedia access, a new default cache object
     * and creates appropriate maps to store statistics in the WikiMediator
     *
     */
    public WikiMediator() {
        this.wiki = new Wiki("en.wikipedia.org");
        this.wiki.enableLogging(false);
        this.timeMap = new ConcurrentHashMap<>();
        this.requestMap = new ConcurrentHashMap<>();
        this.cache = new Cache<>(WikiMediator.DEFAULTCAPACITY, WikiMediator.DEFAULTTIMEOUT);
        this.startTime = LocalDateTime.now();

        /* adds the method names into the requestMap */
        for (String name : this.methodNames) {
            this.requestMap.put(name, Collections.synchronizedList(new ArrayList<>()));
        }
    }


    /**
     * Constructs an instance of the WikiMediator that uses an existing Cache object
     * Creates a new English Wikipedia access point, and appropriate maps to store
     * statistics in the WikiMediator instance
     * @param cache is not null
     *
     */
    public WikiMediator(Cache cache) {
        this.wiki = new Wiki("en.wikipedia.org");
        this.wiki.enableLogging(false);
        this.timeMap = new ConcurrentHashMap<>();
        this.requestMap = new ConcurrentHashMap<>();
        this.cache = cache;
        this.startTime = LocalDateTime.now();

        /* adds the method names into the requestMap */
        for (String name : this.methodNames) {
            this.requestMap.put(name, Collections.synchronizedList(new ArrayList<>()));
        }
    }

    /**
     * A simple search function that returns a list of pages that match the query string
     * @param query is a String to use with Wikipedia's search function
     * @param limit is the maximum number of items that simpleSearch will return
     * @modifies requestMap, adds a time the method was called into the request map (under "simpleSearch" key)
     * @return  a list of strings with  size <= limit that appear from the query using
     * wikipedia's search service.
     * If limit is equal to 0, returns an empty list of strings
     */
    public List<String> simpleSearch(String query, int limit) {
        List<LocalDateTime> requestDates = this.requestMap.get("simpleSearch");
        requestDates.add(LocalDateTime.now());
        this.requestMap.replace("simpleSearch", requestDates);

        addToMap(query);

        if (limit == 0) {
            return new ArrayList<>();
        } else {
            return this.wiki.search(query, limit);
        }
    }

    /**
     * Returns the page text of a given page title. If the page title has been requested already
     * the page text will be obtained from the cache instead of accessing wikipedia.
     * @param pageTitle is a page that we wish to find the wikipedia page for.
     * @modifies requestMap, adds a time the method was called into the request map
     * @return a string that contains the text of the given page title.
     */
    public String getPage(String pageTitle) {
        List<LocalDateTime> requestDates = this.requestMap.get("getPage");
        requestDates.add(LocalDateTime.now());
        this.requestMap.replace("getPage", requestDates);

        String text;
        addToMap(pageTitle);

        try {
            CacheObject co = (CacheObject) this.cache.get(pageTitle);
            text = co.getText();
        } catch (NotFoundException e) {
            text = this.wiki.getPageText(pageTitle);
            this.cache.put(new CacheObject(pageTitle, text));
        }

        return text;
    }

    /**
     * Helper method to add request to the instance time map.
     * Method is synchronized so only one thread can access and add to map at the same time
     * @param request the query or pageTitle to be added to the map
     * @modifies timeMap, adds a query or pageTitle if it is not in the map,
     *                    otherwise, adds the current time to the list of times the
     *                    request has been accessed
     */
    private synchronized void addToMap(String request) {

        if (!this.timeMap.containsKey(request)) {
            List<LocalDateTime> timeList = Collections.synchronizedList(new ArrayList<>());
            timeList.add(LocalDateTime.now());
            this.timeMap.put(request, timeList);
        } else {
            List<LocalDateTime> timeList = this.timeMap.get(request);
            timeList.add(LocalDateTime.now());
            this.timeMap.replace(request, timeList);
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
        List<LocalDateTime> requestDates = this.requestMap.get("getConnectedPages");
        requestDates.add(LocalDateTime.now());
        this.requestMap.replace("getConnectedPages", requestDates);

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
        List<LocalDateTime> requestDates = this.requestMap.get("zeitgeist");
        requestDates.add(LocalDateTime.now());
        this.requestMap.replace("zeitgeist", requestDates);

        List<String> mostCommon = new ArrayList<>();
        int maxOccurrences;
        int count = 0;
        String mostOccurringSearch = "";

        while (count < limit) {
            maxOccurrences = 0;
            for (String search : this.timeMap.keySet()) {
                if ((this.timeMap.get(search).size() > maxOccurrences)
                        && !mostCommon.contains(search)) {
                    maxOccurrences = this.timeMap.get(search).size();
                    mostOccurringSearch = search;
                }
            }
            count++;
            mostCommon.add(mostOccurringSearch);
        }

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
        List<LocalDateTime> requestDates = this.requestMap.get("trending");
        requestDates.add(LocalDateTime.now());
        this.requestMap.replace("trending", requestDates);

        List<String> trendingList = new ArrayList<>();
        LocalDateTime currentTime = LocalDateTime.now();
        Map<String, Integer> frequencyList = new ConcurrentHashMap<>();

        for (String request : this.timeMap.keySet()) {
            List<LocalDateTime> requestList = this.timeMap.get(request);
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

        return trendingList;
    }

    /**
     * Returns the maximum number of requests in any 30 second interval during the duration of
     * an instance of WikiMediator
     * @modifies requestMap, adds a time the method was called into the request map
     * @return the maximum number of request in any 30 second interval.
     * The return value will always be >= 1 since we consider the method call of peakLoad30s
     * to be within the last 30 seconds
     *
     */
    public int peakLoad30s() {
        List<LocalDateTime> requestDates = this.requestMap.get("peakLoad30s");
        requestDates.add(LocalDateTime.now());
        this.requestMap.replace("peakLoad30s", requestDates);
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
                    if (time.isBefore(intervalTime)
                            && (time.isAfter(startingTime) || time.isEqual(startingTime))) {
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

    /* Task 2 */
    /**
     * Writes this.timeMap and this.requestMap to the local directory under the file ".keep"
     */
    private void writeStatsToFile() {

    }

    /**
     * Writes this.cache to the local directory under the file ".Cache"
     */
    private void writeCacheToFile() {

    }

    /* Task 3 */

    /**
     * Finds a path through links from the startPage to the stopPage
     * @param startPage a page on Wikipedia
     * @param stopPage a page on Wikipedia
     * @return A list of strings on the path between the start Page and stop Page
     * returns an empty list of strings if no such path exists
     */
    public List<String> getPath(String startPage, String stopPage) {
        return null;
    }

    /**
     * Returns a list of pages that match a certain criteria
     * @param query a string that defines the search
     * @return a list of pages that match the criteria query
     * returns an empty list if no such pages exist
     */
    public List<String> executeQuery(String query) {
        return null;
    }

}

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
    private Map<String, List<LocalDateTime>> timeMap;
    private Map<String, List<LocalDateTime>> requestMap;
    private LocalDateTime startTime;

    public WikiMediator() {
        this.wiki = new Wiki("en.wikipedia.org");
        this.wiki.enableLogging(false);
        this.popularityMap = new ConcurrentHashMap<>();
        this.timeMap = new ConcurrentHashMap<>();
        this.requestMap = new ConcurrentHashMap<>();
        this.cache = new Cache<>(256,3600);

        this.requestMap.put("search", new ArrayList<LocalDateTime>());
        this.requestMap.put("getPage", new ArrayList<LocalDateTime>());
        this.requestMap.put("connectedPages", new ArrayList<LocalDateTime>());
        this.requestMap.put("zeitgeist", new ArrayList<LocalDateTime>());
        this.requestMap.put("trending", new ArrayList<LocalDateTime>());
        this.requestMap.put("peakLoad", new ArrayList<LocalDateTime>());
        this.startTime = LocalDateTime.now();
    }

    public List<String> simpleSearch(String query, int limit) {
        if (popularityMap.containsKey(query)) {
            int count = popularityMap.get(query);
            count++;
            popularityMap.replace(query, count);
        } else {
            popularityMap.put(query, 1);
        }

        if(!timeMap.containsKey(query)) {
            ArrayList<LocalDateTime> timeList = new ArrayList<>();
            timeList.add(LocalDateTime.now());
            timeMap.put(query, timeList);
        } else {
            List<LocalDateTime> timeList = timeMap.get(query);
            timeList.add(LocalDateTime.now());
            timeMap.replace(query, timeList);
        }
        List<LocalDateTime> requestDates = this.requestMap.get("search");
        requestDates.add(LocalDateTime.now());
        this.requestMap.replace("search", requestDates);
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

        if(!timeMap.containsKey(pageTitle)) {
            ArrayList<LocalDateTime> timeList = new ArrayList<>();
            timeList.add(LocalDateTime.now());
            timeMap.put(pageTitle, timeList);
        } else {
            List<LocalDateTime> timeList = timeMap.get(pageTitle);
            timeList.add(LocalDateTime.now());
            timeMap.replace(pageTitle, timeList);
        }

        CacheObject co = (CacheObject) this.cache.get(pageTitle);
        if (co.id().equals("")) {
            text = this.wiki.getPageText(pageTitle);
            this.cache.put(new CacheObject(pageTitle));
        } else {
            CacheObject a = (CacheObject) this.cache.get(pageTitle);
            text = a.getText();
            this.cache.update(co);
        }

        List<LocalDateTime> requestDates = this.requestMap.get("getPage");
        requestDates.add(LocalDateTime.now());
        this.requestMap.replace("getPage", requestDates);
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

        List<LocalDateTime> requestDates = this.requestMap.get("connectedPages");
        requestDates.add(LocalDateTime.now());
        this.requestMap.replace("connectedPages", requestDates);
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

        Collections.reverse(mostCommon);
        List<LocalDateTime> requestDates = this.requestMap.get("zeitgeist");
        requestDates.add(LocalDateTime.now());
        this.requestMap.replace("zeitgeist", requestDates);
        return mostCommon;
    }

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

        Collections.reverse(trendingList);
        List<LocalDateTime> requestDates = this.requestMap.get("trending");
        requestDates.add(LocalDateTime.now());
        this.requestMap.replace("trending", requestDates);
        return trendingList;
    }

    public int peakLoad30s() {
        List<LocalDateTime> requestDates = this.requestMap.get("peakLoad");
        requestDates.add(LocalDateTime.now());
        this.requestMap.replace("peakLoad", requestDates);
        int startingTime = startTime.getSecond();
        int endTime = LocalDateTime.now().getSecond();
        int maxLoad = 0;
        List<Integer> intervalRequestsList = new ArrayList<>();

        while (startingTime <= endTime - 30) {
            int intervalRequests = 0;
            int intervalTime = startingTime + 30;
            for (String request : this.requestMap.keySet()) {
                for (LocalDateTime times : this.requestMap.get(request)) {
                    int seconds = times.getSecond();
                    if (seconds >= startingTime && seconds < intervalTime) {
                        intervalRequests++;
                    }
                }
            }
            intervalRequestsList.add(intervalRequests);
            startingTime++;
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

package cpen221.mp3.wikimediator;

import java.time.LocalDateTime;
import java.util.List;
import java.util.ArrayList;
import java.util.Set;
import java.util.HashSet;
import java.util.Map;
import java.util.Collections;
import java.util.Queue;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.Stack;
import org.antlr.v4.runtime.ANTLRInputStream;
import org.antlr.v4.runtime.CharStream;
import org.antlr.v4.runtime.CommonTokenStream;
import org.antlr.v4.runtime.TokenStream;
import org.antlr.v4.runtime.tree.ParseTree;
import org.antlr.v4.runtime.tree.ParseTreeWalker;

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
     * This constructor creates a new English Wikipedia access, a new default cache object
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
     * Creates a new English Wikipedia access point, and initializes appropriate maps to store
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
     * @param query is not null and is a String to use with Wikipedia's search function
     * @param limit >= 0 is the maximum number of items that simpleSearch will return
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
     * @param pageTitle is not null and is a page that we wish to find the wikipedia page for.
     * @modifies requestMap, adds a time the method was called into the request map (under "getPage" key)
     * @return a string that contains the text of the given page title.
     * If page title is invalid, getPage follows the behaviour of the jWiki API
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
     * Helper method to add a string request to the instance time map.
     * Method is synchronized so only one thread can access and add to map at the same time
     * @param request the query or pageTitle to be added to the map
     * @modifies timeMap, adds a query or pageTitle if it is not in the map,
     *                    otherwise, adds the current time to the list of times the
     *                    string has been used
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
     * @param pageTitle is not null and is the starting page
     * @param hops >= 0 and is the number of links that we jump through
     * @modifies requestMap, adds a time the method was called into the request map (under "getConnectedPages" key)
     * @return A list of pages that are reachable within a certain number of hops from pageTitle.
     * This list contains no duplicate pages and thus, only returns a list of unique pages that can
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
     * @param pageTitle is not null and is the initial page we are starting from
     * @param hops >= 0 and is the number of links to jump through
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
     * Returns a list of the most common strings used in the simpleSearch and getPage methods
     * @param limit >= 0 and is the maximum number of items to return from the method call
     * @modifies requestMap, adds a time the method was called into the request map (under "zeitgeist" key)
     * @return a list of strings where strings are sorted by the amount of times they have been
     * called by the getPage or simple search method. These strings are sorted into non-increasing
     * order of appearance.
     * If limit = 0, returns an empty list of strings
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
     * @param limit >= 0 and is the maximum number of items to return from method call
     * @modifies requestMap, adds a time the method was called into the request map (under "trending" key)
     * @return a list of strings where strings are sorted by the amount of times they have been
     * called by the getPage or simple search method. These strings are sorted into non-increasing
     * order of appearance.
     * If limit = 0, returns an empty list of strings
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
     * @modifies requestMap, adds a time the method was called into the request map (under "peakLoad30s" key)
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
     * @param startPage a page on en.wikipedia.org
     * @param stopPage a page on en.wikipedia.org
     * @return A list of strings on the path between the start Page and stop Page
     * Returns an empty list of strings if no such path exists or getPath exceeds 5 minutes
     * If start Page equals stop Page, returns a list of the single page
     */
    public List<String> getPath(String startPage, String stopPage) {
        List<LocalDateTime> requestDates = this.requestMap.get("getPath");
        requestDates.add(LocalDateTime.now());
        this.requestMap.replace("getPath", requestDates);
        LocalDateTime startTime = LocalDateTime.now();
        Queue<String> queue = new LinkedBlockingQueue<>();
        Map<String, String> parentMap = new ConcurrentHashMap<>();
        parentMap.put(startPage, startPage);
        queue.add(startPage);
        boolean pageFound = false;

        if (startPage.equals(stopPage)) {
            List<String> returnList = new ArrayList<>();
            returnList.add(startPage);
            return returnList;
        }

        // if we reach end of queue without finding the page, no possible path
        while (!queue.isEmpty() && LocalDateTime.now().isBefore(startTime.plusMinutes(5))) {
            String checkPage = queue.remove();
            List<String> linksOnPage = this.wiki.getLinksOnPage(checkPage);
            for (String page : linksOnPage) {
                // if parentMap doesn't contain page, we haven't visited it yet,
                // so we add it to the queue
                if (!parentMap.containsKey(page)) {
                    parentMap.put(page, checkPage);
                    queue.add(page);
                }

                // if page is the destination, break out of loop and set break flag in while loop
                // to true so we break out of queue loop
                if (page.equals(stopPage)) {
                    pageFound = true;
                    break;
                }

            }

            //want to break out of loop if we have found destination
            if (pageFound) {
                break;
            }

        }

        // if we completely exhaust the queue, then stop Page is either invalid or an orphan page
        if (!pageFound) {
            return new ArrayList<>();
        } else {
            // we have a path from startPage to stopPage
            List<String> pathList = new ArrayList<>();
            pathList.add(stopPage);
            String parentPage = stopPage;

            while (!pathList.contains(startPage)) {
                pathList.add(parentMap.get(parentPage));
                parentPage = parentMap.get(parentPage);
            }

            List<String> pagePath = new ArrayList<>();
            for (int i = pathList.size() - 1; i >= 0; i--) {
                pagePath.add(pathList.get(i));

            }

            return pagePath;
        }
    }

    /**
     * Returns a list of pages that match a certain criteria
     * @param query a string that defines the search
     * @return a list of pages that match the criteria query
     * Returns an empty list if no such pages exist
     */
    public List<String> executeQuery(String query) {
        List<LocalDateTime> requestDates = this.requestMap.get("executeQuery");
        requestDates.add(LocalDateTime.now());
        this.requestMap.replace("executeQuery", requestDates);
        CharStream stream = new ANTLRInputStream(query);
        QueryLexer lexer = new QueryLexer(stream);
        lexer.reportErrorsAsExceptions();
        TokenStream tokens = new CommonTokenStream(lexer);

        // Feed the tokens into the parser.
        QueryParser parser = new QueryParser(tokens);
        parser.reportErrorsAsExceptions();

        // Generate the parse tree using the starter rule.
        ParseTree tree = parser.query();

        System.err.println(tree.toStringTree(parser));

        ParseTreeWalker walker = new ParseTreeWalker();
        QueryListener_QueryCreator listener = new QueryListener_QueryCreator();
        walker.walk(listener, tree);

        List<String> queryList = listener.getQueries();
        return queryList;
    }

    private static class QueryListener_QueryCreator extends QueryBaseListener {
        boolean categoryFlag = false;
        boolean titleFlag = false;
        boolean authorFlag = false;
        boolean checkAuthors = false;
        String author = "";
        Stack<String> results = new Stack<>();
        List<String> returnList = new ArrayList<>();
        List<String> queryList = new ArrayList<>();
        Wiki wiki = new Wiki ("en.wikipedia.org");

        @Override
        public void exitSimpleCondition(QueryParser.SimpleConditionContext ctx) {
            if (ctx.CATEGORY() != null) {
                int length = ctx.STRING().getText().length();
                String category = "Category:" + ctx.STRING().getText().substring(1, length - 1);

                if (authorFlag) {
                    for (String c : wiki.getCategoryMembers(category)) {
                        String editor = wiki.getLastEditor(c);
                        if (!returnList.contains(editor)) {
                            returnList.add(editor);
                        }
                    }
                } else if (titleFlag) {
                    returnList.addAll(wiki.getCategoryMembers(category));
                } else {
                    returnList.addAll(wiki.getCategoriesOnPage(category));
                }

            } else if (ctx.TITLE() != null) {
                int length = ctx.STRING().getText().length();
                String pageTitle = ctx.STRING().getText().substring(1, length - 1);

                if (authorFlag) {
                    returnList.add(wiki.getLastEditor(pageTitle));
                } else if (categoryFlag) {
                    returnList.addAll(wiki.getCategoriesOnPage(pageTitle));
                } else {
                    returnList.add(pageTitle);
                }

            } else {
                int length = ctx.STRING().getText().length();
                author = ctx.STRING().getText().substring(1, length - 1);
                checkAuthors = true;
            }
        }

        @Override
        public void exitCondition(QueryParser.ConditionContext ctx) {
            if (ctx.LPAREN() != null) {
                for (String s : returnList) {
                    results.push(s);
                }
                returnList = new ArrayList<>();
            }

            if (ctx.RPAREN() != null) {
                if (ctx.OR() != null) {
                    while (!results.isEmpty()) {
                        returnList.add(results.pop());
                    }
                } else {
                    List<String> duplicates = new ArrayList<>();
                    while (!results.isEmpty()) {
                        String checkQuery = results.pop();
                        if (checkAuthors) {
                            if (wiki.exists(checkQuery) && wiki.getLastEditor(checkQuery).equals(author)) {
                                duplicates.add(checkQuery);
                            }
                            else if (checkQuery.equals(author)) {
                                duplicates.add(checkQuery);
                            }

                        } else {
                            if (returnList.contains(checkQuery)) {
                                duplicates.add(checkQuery);
                            } else {
                                returnList.add(checkQuery);
                            }

                        }
                    }

                    for (String q : duplicates) {
                        queryList.add(q);

                    }
                    returnList = new ArrayList<>();
                }
            }
        }

        @Override public void exitQuery(QueryParser.QueryContext ctx) {

            for (String s : returnList) {
                if (!queryList.contains(s)) {
                    queryList.add(s);
                }
            }

            if (ctx.SORTED() != null) {
                Collections.sort(queryList);
                if (ctx.SORTED().getText().equals("desc")) {
                    Collections.reverse((queryList));
                }
            }
        }

        @Override
        public void enterQuery(QueryParser.QueryContext ctx) {
            if (ctx.ITEM().getText().equals("author")) {
                authorFlag = true;
            } else if (ctx.ITEM().getText().equals("page")) {
                titleFlag = true;
            } else {
                categoryFlag = true;
            }
        }

        public List<String> getQueries() { return queryList; }
    }

}

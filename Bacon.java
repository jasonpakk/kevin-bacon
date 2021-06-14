import java.io.BufferedReader;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.util.*;

/**
 * Command-Line Game Interface for Bacon Game;
 *
 * @author Jason Pak and Perry Zhang, Dartmouth CS 10, Fall 2020
 */

public class Bacon {
    public static String center = "Kevin Bacon";    //center of universe starts off as Kevin Bacon
    public static Graph<String, Set<String>> mainGraph; //graph with all actors and edges
    public static Graph<String, Set<String>> shortestPathTree; //current shortest path tree
    public static Graph<String, Set<String>> baconGraph; //shortest path tree when Kevin Bacon is center of the universe

    public static void main(String[] args) {
        mainGraph = new AdjacencyMapGraph<>();

        //txt files:
        String actorFile = "inputs/bacon/actors.txt";
        String movieFile = "inputs/bacon/movies.txt";
        String movieactorFile = "inputs/bacon/movie-actors.txt";

        /*
        //txt files for testing:
        String actorFile = "inputs/bacon/actorsTest.txt";
        String movieFile = "inputs/bacon/moviesTest.txt";
        String movieactorFile = "inputs/bacon/movie-actorsTest.txt"; */


        Map<String, String> idActors = readActors(actorFile);   //Key: actor id, Value: actor names
        Map<String, String> idMovies = readMovies(movieFile);   //Key: movie id, Value: movie names
        Map<String, Set<String>> moviesActors = readMoviesActors(idActors, idMovies, movieactorFile); //Key: movie name, Value: set of actors

        buildGraph(moviesActors); //adds edges to graph

        /*
        System.out.println("\nMain Graph:");
        System.out.println(mainGraph);
        System.out.println("\nPath Tree:");
        System.out.println(shortestPathTree + "\n"); */

        //Instructions for Command Interface:
        System.out.println("Commands:");
        System.out.println("c <#>: list top (positive number) or bottom (negative) <#> centers of the universe, sorted by average separation");
        System.out.println("d <low> <high>: list actors sorted by degree, with degree between low and high");
        System.out.println("i: list actors with infinite separation from the current center");
        System.out.println("p <name>: find path from <name> to current center of the universe");
        System.out.println("s <low> <high>: list actors sorted by non-infinite separation from the current center, with separation between low and high");
        System.out.println("u <name>: make <name> the center of the universe");
        System.out.println("q: quit game\n");

        setCenter(center); //set center
        baconGraph = shortestPathTree;

        Scanner in = new Scanner(System.in);
        String line;
        System.out.println("\n" + center + " game > ");

        line = in.nextLine(); //reading user input from keyboard

        //keep playing until input is q:
        while(!line.equals("q")) {
            String[] param = line.split(" ", 2);

            if(param[0].equals("c")) {
                if (param.length == 1) {
                    System.out.println("invalid entry, try again"); //didn't input a number
                }
                else {
                    int num;
                    try {
                        num = Integer.parseInt(param[1]);
                        System.out.println(avgSeperationList(num));
                    }
                    catch (NumberFormatException e) {
                        System.out.println("invalid entry, try again"); //didn't input a number
                    }
                }
            }
            else if(param[0].equals("d")) {
                if (param.length == 1) {
                    System.out.println("invalid entry, try again"); //didn't input value for low
                }
                else {
                    param = param[1].split(" ");
                    if (param.length == 1) {
                        System.out.println("invalid entry, try again"); //didn't input value for high
                    } else {
                        int low, high;
                        try {
                            low = Integer.parseInt(param[0]);
                            high = Integer.parseInt(param[1]);
                            System.out.println(degreeList(low, high));
                        } catch (NumberFormatException e) {
                            System.out.println("invalid entry, try again"); //didn't input a number
                        }
                    }
                }
            }
            else if(param[0].equals("i")) {
                findInfinite();
            }
            else if(param[0].equals("p")) {
                if(param.length == 1) {
                    System.out.println("invalid entry, try again"); //didn't input a name
                }
                else {
                    if(param[1].equals(center)) {
                        System.out.println("this actor is the center, try again");
                    }
                    else {
                        findPath(param[1]);
                    }
                }
            }
            else if(param[0].equals("s")) {
                if (param.length == 1) {
                    System.out.println("invalid entry, try again"); //didn't input a value for low
                }
                else {
                    param = param[1].split(" ");
                    if (param.length == 1) {
                        System.out.println("invalid entry, try again"); //didn't input a value for high
                    }
                    else {
                        int low, high;
                        try {
                            low = Integer.parseInt(param[0]);
                            high = Integer.parseInt(param[1]);
                            System.out.println(separationList(low, high));
                        } catch (NumberFormatException e) {
                            System.out.println("invalid entry, try again"); //didn't input a number
                        }
                    }
                }
            }
            else if(param[0].equals("u")) {
                if(param.length == 1) {
                    System.out.println("invalid entry, try again"); //didn't input a name
                }
                else {
                    setCenter(param[1]);
                }
            }
            else {
                System.out.println("invalid entry, try again");
            }

            System.out.println("\n" + center + " game > ");
            line = in.nextLine();
        }

    }

    /**
     * Takes filename as parameter and returns a Map with Key: Actor ID and Value: Actor Name
     * Adds actors as vertices to the main graph
     */
    public static Map<String, String> readActors(String s) {
        Map<String, String> map = new HashMap<>();
        BufferedReader input = null;

        //trying to read the file
        try {
            input = new BufferedReader(new FileReader(s));
        }
        catch (FileNotFoundException e) {
            System.err.println("Cannot open file.\n" + e.getMessage());
        }

        try {
            String line;
            while ((line = input.readLine()) != null) {
                String[] lineArray = line.split("\\|");
                map.put(lineArray[0], lineArray[1]); //index 0 contains the ID, index 1 contains the name
                mainGraph.insertVertex(lineArray[1]); //adds actors as vertices in the main graph
            }

        }
        catch (IOException e) {
            System.err.println("IO error while reading.\n" + e.getMessage());
        }

        try {
            input.close();
        }
        catch (IOException e) {
            System.err.println("Cannot close file.\n" + e.getMessage());
        }
        return map;
    }

    /**
     * Takes filename as parameter and returns a Map with Key: Movie ID and Value: Movie Name
     */
    public static Map<String, String> readMovies(String s) {
        Map<String, String> map = new HashMap<>();
        BufferedReader input = null;

        //trying to read the file
        try {
            input = new BufferedReader(new FileReader(s));
        }
        catch (FileNotFoundException e) {
            System.err.println("Cannot open file.\n" + e.getMessage());
        }

        try {
            String line;
            while ((line = input.readLine()) != null) {
                String[] lineArray = line.split("\\|");
                map.put(lineArray[0], lineArray[1]); //index 0 contains the ID, index 1 contains the name
            }

        }
        catch (IOException e) {
            System.err.println("IO error while reading.\n" + e.getMessage());
        }

        try {
            input.close();
        }
        catch (IOException e) {
            System.err.println("Cannot close file.\n" + e.getMessage());
        }
        return map;
    }

    /**
     * Takes filename as parameter and returns a Map with Key: Movie Name and Value: Set of Actors in that Movie
     */
    public static Map<String, Set<String>> readMoviesActors(Map<String, String> actors, Map<String, String> movies, String s) {
        Map<String, Set<String>> map = new HashMap<>();
        BufferedReader input = null;

        //trying to read the file
        try {
            input = new BufferedReader(new FileReader(s));
        }
        catch (FileNotFoundException e) {
            System.err.println("Cannot open file.\n" + e.getMessage());
        }

        try {
            String line;
            while ((line = input.readLine()) != null) {
                String[] lineArray = line.split("\\|");
                String m = lineArray[0];    //index 0 contains the movie id
                String a = lineArray[1];    //index 1 contains the actor id
                if(!map.containsKey(movies.get(m))) {
                    map.put(movies.get(m), new HashSet<String>());
                }
                map.get(movies.get(m)).add(actors.get(a)); //adds actor to the set of actors for a movie
            }
        }
        catch (IOException e) {
            System.err.println("IO error while reading.\n" + e.getMessage());
        }

        try {
            input.close();
        }
        catch (IOException e) {
            System.err.println("Cannot close file.\n" + e.getMessage());
        }
        return map;
    }

    /**
     * Given Map of Movie Names (key) and Set of Actors in that movie (Value), adds edges
     * between actors in the main graph
     */
    public static void buildGraph (Map<String, Set<String>> m){
        for(String movie : m.keySet()) {
            Object[] actors = m.get(movie).toArray();   //actors in the set put into array
            int currIndex = 0;  //pointer to keep track of current index
            while (currIndex < actors.length) {
                for (int i = currIndex + 1; i < actors.length; i++) {
                    if (!mainGraph.hasEdge(actors[currIndex].toString(), actors[i].toString())) {
                        mainGraph.insertUndirected(actors[currIndex].toString(), actors[i].toString(), new HashSet<String>());
                    }
                    //add movie name to the label in the main graph between the two actors:
                    mainGraph.getLabel(actors[currIndex].toString(), actors[i].toString()).add(movie);
                }
                currIndex++;
            }
        }
    }

    /**
     * Returns a List of the top (if num is pos) or bottom (if num is neg)
     * centers of the universe, sorted by avg separation
     */
    public static List<String> avgSeperationList(int num) {
        List<String> toReturn = new ArrayList<>();

        if(num == 0) {
            return toReturn;
        }

        //Key: actor name, Value: avg separation when they are the center
        Map<String, Double> avgSepGraph = new HashMap<>();

        for(String actor : mainGraph.vertices()) {
            if(baconGraph.hasVertex(actor)) { //only add actors who are in the Bacon universe
                toReturn.add(actor);

                //calculate avg separation for the actor and add the avg sep to map
                avgSepGraph.put(actor, BaconGraphLib.averageSeparation(BaconGraphLib.bfs(mainGraph, actor), actor));
            }
        }

        //custom Comparator to compare avg sep between two actors
        class AvgSepComparator implements Comparator<String> {
            @Override
            public int compare(String o1, String o2) {
                //use graph to retrieve an actor's avg separation
                double d = avgSepGraph.get(o1) - avgSepGraph.get(o2);

                if(d < 0) {
                    return -1;
                }
                else if (d > 0) {
                    return 1;
                }
                else {
                    return 0;
                }
            }
        }

        Comparator<String> ascompare = new AvgSepComparator();

        if (num > 0 && num <= baconGraph.numVertices()) {
            toReturn.sort(ascompare); //sort using custom comparator
            return toReturn.subList(0, num); //return list with num entries
        } else if (num < 0 && num * -1 <= baconGraph.numVertices()) {
            toReturn.sort(ascompare.reversed()); //reverse comparator if num is neg
            return toReturn.subList(0, -1 * num); //return list with num entries
        }
        else {
            //num is zero or we can't have num entries in final list bc num is too large
            System.out.println("Invalid #");
            return new ArrayList<>();
        }
    }

    /**
     * Returns a List of actors sorted by degree, with a range of degrees
     * between low and high
     */
    public static List<String> degreeList(int low, int high) {
        List<String> toReturn = new ArrayList<>();
        for(String vertex : shortestPathTree.vertices()) {
            int degree = mainGraph.inDegree(vertex);
            if(degree >= low && degree <= high) {   //only adds to list if degree is between low-high
                toReturn.add(vertex);
            }
        }
        toReturn.sort((String s1, String s2) -> mainGraph.inDegree(s2) - mainGraph.inDegree(s1));
        return toReturn;
    }

    /**
     * Finds actors with infinite separation from the current center
     */
    public static void findInfinite() {
        //finds differences between mainGraph and shortestPathTree
        System.out.println(BaconGraphLib.missingVertices(mainGraph, shortestPathTree));
    }

    /**
     * Finds a path from actor with name s to current center of universe
     */
    public static void findPath(String s) {
        //finds shortest path from s to center
        List<String> list = BaconGraphLib.getPath(shortestPathTree, s);

        if(!list.isEmpty()) {
            System.out.println(s + "'s number is " + (list.size() - 1));
        }

        //uses list to print the path
        int count = 0;
        for(String actor: list) {
            if(count != 0) {
                System.out.println(s + " appeared in " + shortestPathTree.getLabel(s, actor) + " with " + actor);
            }
            s = actor;
            count++;
        }
    }

    /**
     * Returns a List of actors sorted by separation from current center, with a range
     * of separation between low and high; sorts only actors with non-infinite separation
     */
    public static List<String> separationList(int low, int high) {
        List<String> toReturn = new ArrayList<>();
        //Key: actor name, Value: separation from center
        Map<String, Integer> stepsMap = new HashMap<>();

        for(String vertex : shortestPathTree.vertices()) {
            if(!vertex.equals(center)) {
                //calculate separation from current center
                int steps = BaconGraphLib.getPath(shortestPathTree, vertex).size() - 1;
                //only add if between low-high
                if (steps >= low && steps <= high) {
                    toReturn.add(vertex);
                    stepsMap.put(vertex, steps);
                }
            }
        }

        //sort by comparing an actor's separation from center which is now stored in the map
        toReturn.sort((String s1, String s2) -> stepsMap.get(s1) - stepsMap.get(s2));
        return toReturn;
    }

    /**
     * Changes center of universe to given actor
     */
    public static void setCenter(String s) {
        if(!mainGraph.hasVertex(s)) {
            System.out.println("Vertex not in graph");
        }
        else {
            //update shortest path tree for the new center of the universe
            shortestPathTree = BaconGraphLib.bfs(mainGraph, s);
            //update new center
            center = s;
            System.out.println(s + " is now the center of the acting universe, connected to " + (shortestPathTree.numEdges())
                    + "/" + mainGraph.numVertices() + " actors with average separation " + BaconGraphLib.averageSeparation(shortestPathTree, s));
        }
    }
}

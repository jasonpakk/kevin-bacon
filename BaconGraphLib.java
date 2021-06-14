import java.util.*;

/**
 * Graph Library Class for Bacon Game;
 *
 * @author Jason Pak and Perry Zhang, Dartmouth CS 10, Fall 2020
 */


public class BaconGraphLib {

    /**
     * BFS to find shortest path tree for a current center of the universe. Returns a path tree as a Graph.
     */
    public static <V,E> Graph<V,E> bfs(Graph<V,E> g, V source) {
        Graph<V, E> pathTree = new AdjacencyMapGraph<V, E>(); //graph representing path tree

        Set<V> visited = new HashSet<V>(); //set which tracks which vertices have already been visited
        Queue<V> queue = new LinkedList<V>(); //queue to implement BFS

        queue.add(source); //enqueue start vertex
        visited.add(source); //add start to visited Set
        while (!queue.isEmpty()) { //loop until no more vertices
            V u = queue.remove(); //dequeue
            pathTree.insertVertex(u); //adding as vertex to path tree
            for (V v : g.outNeighbors(u)) { //loop over out neighbors
                if (!visited.contains(v)) { //if neighbor not visited, then neighbor is discovered from this vertex
                    visited.add(v); //add neighbor to visited Set
                    queue.add(v); //enqueue neighbor
                    pathTree.insertVertex(v); //adding as vertex to path tree
                    pathTree.insertDirected(v, u, g.getLabel(u,v)); //edge pointing from child to parent
                }
            }
        }

        return pathTree;
    }

    /**
     * Given a shortest path tree and a vertex, constructs a path from the vertex back to the center of the universe.
     */
    public static <V,E> List<V> getPath(Graph<V,E> tree, V v) {
        List<V> path = new ArrayList<>();

        if (tree == null ||
                (tree.hasVertex(v) && tree.outDegree(v) == 0)) {
            System.out.println("Error");
            return path;
        }
        if (!tree.hasVertex(v)) {
            System.out.println("No path available");
            return path;
        }

        V current = v;
        //loop from vertex v back to center of the universe
        while (tree.outDegree(current) != 0) {
            path.add(current); //add this vertex to list
            for(V parent : tree.outNeighbors(current)) { //get parent of current vertex
                current = parent;
            }
        } path.add(current); //add center of universe to list
        return path;
    }

    /**
     * Given a graph and a subgraph (here shortest path tree), determines which
     * vertices are in the graph but not the subgraph (here, not reached by BFS).
     */
    public static <V,E> Set<V> missingVertices(Graph<V,E> graph, Graph<V,E> subgraph) {
        Set<V> set = new HashSet<>();

        //loops through every vertex in graph to check if it exists in subgraph
        //if does not exist, add to set of missing vertices:
        for(V vertex: graph.vertices()) {
            if(!subgraph.hasVertex(vertex)) {
                set.add(vertex);
            }
        }
        return set;
    }

    /**
     * Finds the average distance-from-root in a shortest path tree.
     */
    public static <V,E> double averageSeparation(Graph<V,E> tree, V root) {
        //divide sum of separations by the number of vertices
        return averageSeparationHelper(tree, root, 1) / (tree.numEdges());
    }

    /**
     * Recursive helper method to calculate average separation
     */
    private static <V,E> double averageSeparationHelper(Graph<V,E> tree, V root, int lvl) {
       //calculate separation from root for all the vertices in the current level
        double sum = tree.inDegree(root) * lvl;

        //recurse through neighbors
        for(V next: tree.inNeighbors(root)) {
            sum += averageSeparationHelper(tree, next, lvl+1);
        }
        return sum;
    }
}

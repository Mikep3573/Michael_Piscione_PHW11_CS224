// Michael Piscione CS224 Programming Assignment Homework 11

import java.util.ArrayList;
import java.util.List;
import java.util.Queue;
import java.util.Stack;
public class Graph {
    List<Node> nodes;

    public Graph() {
        this.nodes = new ArrayList<Node>();
    }

    public void addNode(Node n) {
        this.nodes.add(n);
    }

    public void addEdge(Node n1, Node n2, int capacity) {
        this.addEdge(n1, n2, capacity, 0);
    }

    //----------------------------------------------------------------

    public void addEdge(Node n1, Node n2, int capacity, int flow) {
        Edge e1 = new Edge(n1, n2, capacity, flow);
        assert(flow <= capacity);
        int idx1 = this.nodes.indexOf(n1);
        if (idx1 >= 0) {
            this.nodes.get(idx1).add(e1);
        } else {
            System.out.println("node " + n1.name + " not found in graph");
        }
    } // addEdge()

    //----------------------------------------------------------------

    private void addResidualEdge(Node n1, Node n2, int capacity, boolean isBackward) {
        Edge e1 = new Edge(n1, n2, capacity, isBackward);
        int idx1 = this.nodes.indexOf(n1);
        if (idx1 >= 0) {
            this.nodes.get(idx1).addResidualEdge(e1);
        } else {
            System.out.println("node " + n1.name + " not found in graph");
        }
    } // addResidualEdge()

    //----------------------------------------------------------------

    public void print() {
        for (Node n: this.nodes) {
            System.out.print("Node " + n.name + ":");
            for (Edge edge: n.adjlist) {
                System.out.print(" " + edge.n2.name + " (c=" + edge.capacity);
                System.out.print(", f=" + edge.flow + ")");
            }
            System.out.println();
        }
    } // print()

    //----------------------------------------------------------------

    public void printResidual() {
        for (Node n: this.nodes) {
            System.out.print("Node " + n.name + ":");
            for (Edge edge: n.adjlistResid) {
                System.out.print(" " + edge.n2.name + " (c=" + edge.capacity);
                if (edge.isBackward)
                    System.out.print(" <=");
                System.out.print(")");
            }
            System.out.println();
        }
    } // printResidual()

    //----------------------------------------------------------------

    private List<Edge> findPathInResid(Node s, Node t) {
        int i, k, idx;
        boolean done, found;
        Node n1, n2;

        List<Edge> path = new ArrayList<Edge>();

        Stack<Node> stack = new Stack<Node>();
        boolean explored[] = new boolean[1 + this.nodes.size()];
        int parent[] = new int[1 + this.nodes.size()];

        for (i=0; i<=this.nodes.size(); ++i)
            explored[i] = false;

        done = false;
        stack.push(s);
        while ( ! done && ! stack.empty() ) {
            n1 = stack.pop();
            if ( ! explored[n1.name] ) {
                explored[n1.name] = true;
                if (parent[n1.name] != 0)
                    System.out.println("tree: " + n1.name + " -> " + parent[n1.name]);
                for (Edge edge: n1.adjlistResid) {
                    n2 = edge.n2;
                    if ( ! explored[n2.name] ) {
                        stack.push(n2);
                        parent[n2.name] = n1.name;
                        if (n2.name == t.name)
                            done = true;
                    }
                }
            }
        }

        System.out.println("here's the backward path from " + t.name);
        done = false;
        idx = t.name;
        while ( ! done ) {
            if (parent[idx] == 0)
                done = true;
            else {
                System.out.println(parent[idx] + " to " + idx);
                // find the edge from parent[idx] to idx
                found = false;
                k = 0;
                while ( ! found && k < nodes.size()) {
                    if (nodes.get(k).name == parent[idx])
                        found = true;
                    else
                        k = k + 1;
                }
                n1 = nodes.get(k);
                found = false;
                for (k=0; ! found && k<n1.adjlistResid.size(); ++k) {
                    Edge e = n1.adjlistResid.get(k);
                    if (e.n2.name == idx) {
                        path.add(e);
                        found = true;
                    }
                }
                idx = parent[idx];
            }
        }

        System.out.println();
        return path;
    } // findPathInResid()

    //----------------------------------------------------------------

    /** Homework checkFlow function **/
    public boolean checkFlow(Node s, Node t) {
        // Keep totals for the flow coming out of the source and into the sink
        // Also keep a total of the flow leaving any given node (variable) and coming into any given node (array)
        int startFlow = 0, endFlow = 0, flowOut = 0;
        int flowIn[] = new int[nodes.size() + 1];

        // check that flow out of s = flow into t
        for (Node n: nodes) {
            for (Edge e: n.adjlist) {
                // Check capacity condition
                if (e.flow > e.capacity) {
                    System.out.println("Flow > Capacity");
                    System.out.println(e);
                    return false;
                }

                // Sum flow out of s
                if (n.name == s.name) {
                    startFlow += e.flow;
                }

                // Sum flow into t
                if (e.n2.name == t.name) {
                    endFlow += e.flow;
                }

                // For node i, flowIn[i] holds the summed flow coming in
                flowIn[e.n2.name] += e.flow;
            }
        }

        // Notify the user if the flow out of s and into t do not equal
        if (startFlow != endFlow) {
            System.out.println("Flow out of " + s.name + " does not equal flow into " + t.name);
            return false;
        }

        // Check conservation condition at each node
        for (Node n: nodes) {
            if (n != s && n != t) {
                // Obviously s and t don't have incoming and outgoing edges respectively so this wouldn't work for them
                for (int i = 0; i < n.adjlist.size(); i++) {
                    flowOut += n.adjlist.get(i).flow; // Sum flow out of a node
                }
                if (flowOut != flowIn[n.name]) { // Check that the flow out = flow in at every node
                    System.out.println("Flow out of " + n.name + " != flow in (" + flowOut + " != " + flowIn[n.name] + ")");
                    return false;
                }

                // Reset flowOut for next node
                flowOut = 0;
            }
        }

        // Return true if capacity and conservation conditions are met
        return true;
    } // checkFlow()

    //----------------------------------------------------------------

    /** Homework getEdges function **/
    public List<Edge> getEdges() {
        // Create an ArrayList to house the found edges
        List<Edge> edges = new ArrayList<>();

        // Scan through all edges and add them to the list as they are found
        for (Node n: this.nodes) {
            for (Edge e: n.adjlist) {
                edges.add(e);
            }
        }

        // Return the list of edges
        return edges;
    } // getEdges()

    //----------------------------------------------------------------

    /** Homework constructResidualGraph function **/
    private void constructResidualGraph() {
        // Clear the list of adjacent nodes (in the residual) for each node
        for (Node n: this.nodes) {
            n.adjlistResid.clear();
        }

        // Read through each node's adjacent nodes in the graph G
        for (Node n: this.nodes) {
            // Adjust the residual graph at each node
            for (Edge e: n.adjlist) {
                if (e.flow != 0) {
                    // Flow contributes to backwards edges in the residual, don't create an edge if it's zero
                    addResidualEdge(e.n2, e.n1, e.flow, true);
                }
                if (e.capacity != e.flow){
                    // Residual capacity contributes to forward edges in the residual, don't create
                    // an edge if it's zero
                    addResidualEdge(e.n1, e.n2, e.capacity - e.flow, false);
                }
            }
        }

    } // constructResidualGraph()

    //----------------------------------------------------------------

    /** Homework findBottleneck function **/
    private int findBottleneck(List<Edge> path) {
        // Keep a variable for the bottleneck
        int bottleneck = 0;

        // Find the bottleneck
        for (int i = 0; i < path.size(); i++) {
            if (i == 0) {
                // If this is the first element in the list, set bottleneck equal to the edge's flow value
                bottleneck = path.get(i).capacity;
            }
            if (path.get(i).capacity < bottleneck) {
                // Otherwise check if it's a smaller value than the current bottleneck value before updating
                bottleneck = path.get(i).capacity;
            }
        }

        // Return the bottleneck value
        return bottleneck;
    } // findBottleneck()

    //----------------------------------------------------------------

    /** Homework augment function **/
    private void augment(List<Edge> path) {
        // Find the bottleneck
        int bottleneck = findBottleneck(path);

        // Keep an index into the list of nodes
        int idx;

        // Construct the new path
        for (Edge e: path) {
            if (!e.isBackward) { // Forward edges add flow
                idx = nodes.indexOf(e.n1);
                for (Edge edge: nodes.get(idx).adjlist) {
                    if (edge.n2 == e.n2) { // Update the correct edge
                        edge.flow += bottleneck;
                    }
                }
            }
            else { // Backward edges remove flow
                idx = nodes.indexOf(e.n1);
                for (Edge edge: nodes.get(idx).adjlist) {
                    if (edge.n2 == e.n2) { // Update the correct edge
                        edge.flow -= bottleneck;
                    }
                }
            }
        }
    } // augment()

    //----------------------------------------------------------------


    /** Homework maxFlow function **/
    public int maxFlow(Node s, Node t) {
        // Keep a running total of the flow
        int flow = 0;

        // Set the flow of each edge to zero to start
        for (Edge e: getEdges()) {
            e.flow = 0;
        }

        // Construct the initial residual graph
        constructResidualGraph();

        // Pick a path in the initial residual
        List<Edge> path = findPathInResid(s, t);

        // IN LOOP:
        //  Augment the path using the bottleneck of the chosen path
        //  Construct a new residual based on the new flow
        //  Find another path
        while (path.size() != 0) {
            flow += findBottleneck(path);
            augment(path);
            if (!checkFlow(s, t)) {
                break;
            }
            constructResidualGraph();
            path = findPathInResid(s, t);
        }

        // State the maximum flow
        System.out.println("max flow is " + flow);

        // Return the flow value
        return flow;
    } // maxFlow()
}

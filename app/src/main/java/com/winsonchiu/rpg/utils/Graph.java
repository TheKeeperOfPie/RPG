package com.winsonchiu.rpg.utils;

import java.util.HashSet;
import java.util.Set;

/**
 * Created by TheKeeperOfPie on 5/23/2015.
 */
public class Graph {

    private Set<Edge> edgeSet;
    private Set<Integer> connectedVertices;

    public Graph() {
        edgeSet = new HashSet<>();
        connectedVertices = new HashSet<>();
    }

    public void addEdge(int source, int destination, int cost) {
        edgeSet.add(new Edge(source, destination, cost));
        connectedVertices.add(source);
        connectedVertices.add(destination);
    }

    public Set<Edge> getEdgeSet() {
        return edgeSet;
    }

    public Set<Integer> getConnectedVertices() {
        return connectedVertices;
    }
}

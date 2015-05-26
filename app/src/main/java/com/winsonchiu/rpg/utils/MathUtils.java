package com.winsonchiu.rpg.utils;

import android.graphics.Point;
import android.graphics.PointF;
import android.util.Log;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.PriorityQueue;
import java.util.Set;

/**
 * Created by TheKeeperOfPie on 5/11/2015.
 */
public class MathUtils {

    private static final String TAG = MathUtils.class.getCanonicalName();

    public static double distance(float xFirst, float yFirst, float xSecond, float ySecond) {
        return Math.sqrt(Math.pow(xFirst - xSecond, 2) + Math.pow(yFirst - ySecond, 2));
    }

    public static double distance(Point first, Point second) {
        return Math.sqrt(Math.pow(first.x - second.x, 2) + Math.pow(first.y - second.y, 2));
    }

    public static double distance(PointF first, PointF second) {
        return Math.sqrt(Math.pow(first.x - second.x, 2) + Math.pow(first.y - second.y, 2));
    }

    public static Set<Edge> createMinimumSpanningTree(Graph graph) {


        List<Edge> allEdges = new ArrayList<>(graph.getEdgeSet());
        PriorityQueue<Edge> edges = new PriorityQueue<>();
        Set<Integer> visitedVertices = new HashSet<>();
        Set<Edge> resultTree = new HashSet<>();
        visitedVertices.add(0);
        int maxVertices = graph.getConnectedVertices().size();

        while (visitedVertices.size() < maxVertices) {
            edges.clear();

            for (Edge edge : allEdges) {
                if (visitedVertices.contains(edge.getSource()) ^
                        visitedVertices.contains(edge.getDestination())) {
                    edges.add(edge);
                }
            }

            Edge edge = edges.poll();
            allEdges.remove(edge);

            resultTree.add(edge);
            visitedVertices.add(edge.getSource());
            visitedVertices.add(edge.getDestination());

        }

        return resultTree;

    }

}

package com.winsonchiu.rpg.utils;

/**
 * Created by TheKeeperOfPie on 5/23/2015.
 */
public class Edge implements Comparable<Edge> {

    private int source;
    private int destination;
    private int cost;

    public Edge(int source, int destination, int cost) {
        this.source = source;
        this.destination = destination;
        this.cost = cost;
    }

    public int getSource() {
        return source;
    }

    public int getDestination() {
        return destination;
    }

    public int getCost() {
        return cost;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (o == null || getClass() != o.getClass()) {
            return false;
        }

        Edge edge = (Edge) o;

        if (getCost() == edge.getCost()) {
            if (getSource() == edge.getSource() && getDestination() == edge.getDestination()) {
                return true;
            }
            if (getSource() == edge.getDestination() && getDestination() == edge.getSource()) {
                return true;
            }
        }

        return false;

    }

    @Override
    public int hashCode() {
        int result = getSource();
        result = 31 * result + getDestination();
        result = 31 * result + getCost();
        return result;
    }

    @Override
    public int compareTo(Edge another) {
        return getCost() - another.getCost();
    }

    @Override
    public String toString() {
        return "Edge{" +
                "source=" + source +
                ", destination=" + destination +
                ", cost=" + cost +
                '}';
    }
}

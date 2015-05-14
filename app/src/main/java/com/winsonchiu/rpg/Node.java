package com.winsonchiu.rpg;

import android.graphics.Point;
import android.graphics.PointF;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by TheKeeperOfPie on 5/11/2015.
 */
public class Node implements Comparable<Node> {

    private Point point;
    private double cost;
    private Node parent;

    public Node(Point point, double cost) {
        this.point = point;
        this.cost = cost;
    }

    public Node(Point point) {
        this.point = point;
    }

    public Point getPoint() {
        return point;
    }

    public void setPoint(Point point) {
        this.point = point;
    }

    public double getCost() {
        return cost;
    }

    public void setCost(double cost) {
        this.cost = cost;
    }

    public List<Node> getAdjacentNodes() {

        List<Node> nodes = new ArrayList<>();

        for (int offsetX = -1; offsetX <= 1; offsetX++) {
            for (int offsetY = -1; offsetY <= 1; offsetY++) {
                if (offsetX == 0 && offsetY == 0) {
                    continue;
                }

                nodes.add(new Node(new Point(point.x + offsetX, point.y + offsetY)));

            }
        }

        return nodes;
    }

    public void calculateCostTo(Point target) {
        cost = MathUtils.distance(getPoint(), target);
    }

    public void calculateCostTo(PointF target) {
        cost = MathUtils.distance(new PointF(getPoint()), target);
    }

    @Override
    public String toString() {
        return "Node{" +
                "point=" + point +
                ", cost=" + cost +
                '}';
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (o == null || getClass() != o.getClass()) {
            return false;
        }

        Node node = (Node) o;

        return getPoint().equals(node.getPoint());

    }

    @Override
    public int hashCode() {
        return getPoint().hashCode();
    }

    @Override
    public int compareTo(Node another) {
        return Double.compare(getCost(), another.getCost());
    }

    public void setParent(Node parent) {
        this.parent = parent;
    }

    public Node getParent() {
        return parent;
    }
}

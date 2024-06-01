package org.example.hygyhgvkju;

import java.io.Serializable;

/**

 Represents a stick connecting two nodes on the game board.
 Implements Serializable to support object serialization for saving/loading the game state.
 */
public class Stick implements Serializable {
    private int node1X;
    private int node1Y;
    private int node2X;
    private int node2Y;

    public Stick(int node1X, int node1Y, int node2X, int node2Y) {
        this.node1X = node1X;
        this.node1Y = node1Y;
        this.node2X = node2X;
        this.node2Y = node2Y;
    }

    public int getNode1X() {
        return node1X;
    }

    public int getNode1Y() {
        return node1Y;
    }

    public int getNode2X() {
        return node2X;
    }

    public int getNode2Y() {
        return node2Y;
    }
}




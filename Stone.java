package org.example.hygyhgvkju;
import java.io.Serializable;

/**

 Represents a stone placed on the game board.
 Implements Serializable to support object serialization for saving/loading the game state.
 */
public class Stone implements Serializable {
    private int x;
    private int y;
    private String color;

    public Stone(int x, int y, String color) {
        this.x = x;
        this.y = y;
        this.color = color;
    }

    public int getX() {
        return x;
    }

    public int getY() {
        return y;
    }

    public String getColor() {
        return color;
    }
}



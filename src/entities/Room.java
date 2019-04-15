package entities;

import java.util.ArrayList;
import java.util.HashMap;

public class Room {
    public int id;
    public int capacity;
    public HashMap<Integer, Integer> distanceToRooms;
    public ArrayList<String> unaivailableweeks;
}

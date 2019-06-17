package entities;

import java.util.BitSet;

public class Unavailability {
    public BitSet weeks;
    public BitSet days;
    public int length;
    public int start;

    public Unavailability(BitSet weeks, BitSet days, int length, int start){
        this.start=start;
        this.length= length;
        this.days= days;
        this.weeks=weeks;
    }
}
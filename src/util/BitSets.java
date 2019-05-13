package util;

import java.util.BitSet;

public class BitSets {
    public static BitSet and(BitSet set1, BitSet set2) {
        BitSet result = (BitSet) set1.clone();
        result.and(set2);
        return result;
    }

    public static BitSet or(BitSet set1, BitSet set2) {
        BitSet result = (BitSet) set1.clone();
        result.or(set2);
        return result;
    }


    public static BitSet xor(BitSet set1, BitSet set2) {
        BitSet result = (BitSet) set1.clone();
        result.xor(set2);
        return result;
    }

    public static BitSet andNot(BitSet set1, BitSet set2) {
        BitSet result = (BitSet) set1.clone();
        result.andNot(set2);
        return result;
    }
}


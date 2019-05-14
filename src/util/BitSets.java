package util;

import javax.swing.plaf.basic.BasicButtonUI;
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

    public static BitSet fromString(String val) {
        BitSet bitSet = new BitSet(val.length());
        for (int i = 0; i < val.length(); i++) {
            char c = val.charAt(i);
            if(c == '1'){
                bitSet.set(i);
            }else if(c!='0'){
                throw new IllegalArgumentException("Input string contains character that is not a '1' or '0'");
            }
        }
        return bitSet;
    }

    public static String toBitString(BitSet set) {
        StringBuilder stringBuilder = new StringBuilder();
        for (int i = 0; i < set.length(); i++) {
            if(set.get(i)){
                stringBuilder.append("1");
            }
            else {
                stringBuilder.append("0");
            }
        }
        return stringBuilder.toString();
    }
}


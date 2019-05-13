package ILP;

public class OverlappingPair {
    public GRBtimeObject left;
    public GRBtimeObject right;

    public OverlappingPair(GRBtimeObject left, GRBtimeObject right){
        this.left=left;
        this.right=right;
    }

    public GRBtimeObject getRight() {
        return right;
    }

    public GRBtimeObject getLeft() {
        return left;
    }
}

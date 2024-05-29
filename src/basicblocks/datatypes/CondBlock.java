package basicblocks.datatypes;

public class CondBlock extends BBlock {

    private Integer alt;

    // Konstr. für if-Blöcke ohne else-Block
    public CondBlock(int positionInArray, int next) {
        super(positionInArray, next);
    }
    public CondBlock(int positionInArray) {
        super(positionInArray);
    }
    // für if- und else-Blöcke
    public CondBlock(int positionInArray, int next, int alt) {
        super(positionInArray, next);
        this.alt = alt;
    }

    public String toString() {
        return "Cond Block";
     }
    public void setNext2(int next2){
        this.alt =next2;
    }
    
}

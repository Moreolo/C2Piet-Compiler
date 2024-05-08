package basicblocks.datatypes;

public class CondBlock extends BBlock {

    private int alt;

    // Konstr. für if-Blöcke ohne else-Block
    public CondBlock(int next) {
        super(next);
    }
    // für if- und else-Blöcke
    public CondBlock(int next, int alt) {
        super(next);
        this.alt = alt;
    }

    public String toString() {
        return "Cond Block";
     }
    
}

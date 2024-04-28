package basicblocks.datatypes;

import java.util.ArrayList;

import ast.datatypes.Node;


public class BBlock {

    private int next;

    // kann der gleiche Listentyp sein wie vom AST-Team
    private ArrayList<Node> body = new ArrayList<>();




    public BBlock(int next) {
        this.next = next;
    }

    public void setBody(ArrayList<Node> body) {
        this.body.addAll(body);
    }
    public void addToBody(Node node) {
        this.body.add(node);
    }
}

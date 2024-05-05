package basicblocks.datatypes;

import java.util.ArrayList;

import ast.datatypes.Node;


public class BBlock {

    private Integer next;

    // kann der gleiche Listentyp sein wie vom AST-Team
    private ArrayList<Node> body = new ArrayList<>();


    public BBlock(){

    }

    public BBlock(Integer next) {
        this.next = next;
    }

    public Integer getNext(){
        return this.next;
    }
    
    public void setNext(Integer next){
        this.next = next;
    }

    public void setBody(ArrayList<Node> body) {
        this.body.addAll(body);
    }
    public void addNodeToBody(Node node) {
        this.body.add(node);
    }

    @Override
    public String toString() {
       return "Classic Block";
    }
}

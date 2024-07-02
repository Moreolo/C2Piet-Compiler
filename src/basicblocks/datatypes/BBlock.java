package basicblocks.datatypes;

import java.util.ArrayList;

import ast.datatypes.Node;


public class BBlock {
    private int positionInArray;

    private Integer next = null;

    // kann der gleiche Listentyp sein wie vom AST-Team
    private ArrayList<Node> body = new ArrayList<>();

    public BBlock(int positionInArray){
        this.positionInArray = positionInArray;
    }

    public int getPositionInArray(){
        return this.positionInArray;
    }

    public void setPositionInArray(Integer position){
        this.positionInArray = position;
    }

    public Integer getNext(){
        return this.next;
    }

    public void setNext(Integer next){
        this.next = next;
    }

    public ArrayList<Node> getBody(){
        return this.body;
    }

    public BBlock(int positionInArray, Integer next) {
        this.next = next;
        this.positionInArray = positionInArray;
    }

    public void setBody(ArrayList<Node> body) {
        this.body = body;
    }

    public ArrayList<Node> getBody() {
        return this.body;
    }
    public void addNodeToBody(Node node) {
        this.body.add(node);
    }
}

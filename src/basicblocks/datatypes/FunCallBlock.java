package basicblocks.datatypes;

import java.util.ArrayList;

import ast.datatypes.Node;

public class FunCallBlock extends BBlock {
    // hat bisher keine besondere Funktionalitäten, wird nur bei Paramenterverwendung und bei return values interessantc
    Integer returnAdress; // kann auch null sein
    ArrayList<Node> parameterList;
    public FunCallBlock(int next, Integer returnAdress, Node NodewithFunctionCall, ArrayList<Node> parameterList) {
        super(next);
        this.returnAdress = returnAdress;
        this.addNodeToBody(NodewithFunctionCall);
        this.parameterList = parameterList;
    }
    
}

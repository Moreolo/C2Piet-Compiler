package basicblocks.datatypes;

import java.util.ArrayList;

import ast.datatypes.Node;

public class FunDefBlock extends BBlock{

    ArrayList<Node> parameterList;

    public FunDefBlock(int positionInArray) {
        super(positionInArray);
        
    }

    public void setParameters(ArrayList<Node> paramList) {
        this.parameterList = paramList;
    }

    public ArrayList<Node> getParameters() {
        return this.parameterList;
    }



}

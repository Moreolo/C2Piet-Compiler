package basicblocks.datatypes;

import java.util.ArrayList;

import ast.datatypes.Node;

/*
das normale next des FunCallBlock (geerbt von BBlock) zeigt auf die "return Adress"
mit nameOfFunction (String - als key) kann der Coder der Funktion aus einer HashListe geholt werden
returnObjekt soll die Variable darstellen, in die der return value gelegt wird
 */
public class FunCallBlock extends BBlock {

    ArrayList<Node> parameterList;
    String nameOfFunction;
    FunctionTempReturn returnObject;

    public FunCallBlock(Integer positionInArray, ArrayList<Node> parameterList, String functionName, FunctionTempReturn funReturn) {

        super(positionInArray);

        this.parameterList = parameterList;
        this.nameOfFunction = functionName;
        this.returnObject = funReturn;
    }
    
}

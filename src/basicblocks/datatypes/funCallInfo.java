package basicblocks.datatypes;

import java.util.ArrayList;

import ast.datatypes.Node;

/*
 Datenklasse, die einige Informationen beinhaltet, die gebraucht werden,
 um einen FunctionCall-BasicBlock zu erstellen
 */
public class funCallInfo {
    private FunctionTempReturn returnTempVar;
    private String functionName;
    private ArrayList<Node> parameterList;

    public funCallInfo(FunctionTempReturn returnTempVar, String functionName, ArrayList<Node> parameterList){
        this.functionName = functionName;
        this.parameterList = parameterList;
        this.returnTempVar = returnTempVar;
    }

    public FunctionTempReturn getReturnTempVar() {
        return returnTempVar;
    }
    public String getFunctionName() {
        return functionName;
    }
    public ArrayList<Node> getParameterList() {
        return parameterList;
    }

    public void setReturnTempVar(FunctionTempReturn returnTempVar) {
        this.returnTempVar = returnTempVar;
    }
    public void setFunctionName(String functionName) {
        this.functionName = functionName;
    }
    public void setParameterList(ArrayList<Node> parameterList) {
        this.parameterList = parameterList;
    }
    
    

}

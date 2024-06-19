package basicblocks.datatypes;

import java.util.ArrayList;

import ast.datatypes.Node;

/*
 Klasse, die Funktionalität bietet, um Binary Expressions oder auch
 die Parameter eines Funktionsaufrufs nach (weiteren) Funktionsaufrufen zu durchsuchen
 - gefundene Funktionsaufrufe werden als Datenobjekt (funCallInfo) in Liste (funCallInfos)
 gespeichert
 */
public class SearchFunCall {
    private ArrayList<funCallInfo> funCallInfos = new ArrayList<>();
    
    //durchsucht Parameter einer Funktion
        public void searchCallsOriginFun(Node functionCall){
        ArrayList<Node> parameters = (ArrayList) functionCall.getAlternative();
        for(int i= 0; i < parameters.size(); i++){
            Node parameter = parameters.get(i);
            switch (parameter.getType()) {
                case BINARY_EXPRESSION:
                    searchCallsOriginBin(parameter);
                    break;
                    
                case FUNCTION_CALL:
                    searchCallsOriginFun(parameter);
                    FunctionTempReturn returnTempVar = new FunctionTempReturn();
                    //Eintragen von Infos für Callblock in Liste
                    parameters.set(i, returnTempVar);
                    funCallInfo funCall = new funCallInfo(returnTempVar, parameter.getValue(), (ArrayList) parameter.getAlternative());
                    funCallInfos.add(funCall);

                default:
                
            }
        }
    }
    
    //durchsucht links und rechts einer Binary Expression
    public void searchCallsOriginBin(Node binaryExpression){
        ArrayList<Node> paths = new ArrayList<>();
        paths.add(binaryExpression.getLeft());
        paths.add(binaryExpression.getRight());
        for(int i= 0; i < paths.size(); i++){ 
            Node path = paths.get(i);
            switch (path.getType()) {
                case BINARY_EXPRESSION:
                    searchCallsOriginBin(path);
                    break;
                case FUNCTION_CALL:
                    searchCallsOriginFun(path);
                    FunctionTempReturn returnTempVar = new FunctionTempReturn();
                    //Eintragen von Infos für Callblock in Liste
                    if(i == 0){
                        binaryExpression.setLeft(returnTempVar);
                    }else{
                        binaryExpression.setRight(returnTempVar);
                    }
                    funCallInfo funCall = new funCallInfo(returnTempVar, path.getValue(), (ArrayList) path.getAlternative());
                    funCallInfos.add(funCall);
                    
                default:
                
                
            }
    }
    }
    public ArrayList<funCallInfo> getFunCallInfos(){
        return this.funCallInfos;
    }

}

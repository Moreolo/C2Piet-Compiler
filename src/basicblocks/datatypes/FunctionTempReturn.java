package basicblocks.datatypes;

import ast.datatypes.Node;
import ast.datatypes.NodeTypesEnum;

/*
 Klasse, die als temporärer bzw. behelfsmäßiger Returnvalue eingetragen wird, wenn
 Funktionsaufrufe aus Strukturen "herausgezogen werden"

 Beispiel:
 y = x + doSomething();
 -->
 temp = doSomething()
 y = x + temp

 temp ist hier der FunctionTempReturn
 */
public class FunctionTempReturn extends Node{
    private static int communalCounter = 0;
    private int id;
    //Instanzen hiervon werden als return values genutzt
    public FunctionTempReturn(){
        super(NodeTypesEnum.FUNCTION_TEMP_RETURN);
        this.id = communalCounter;
        communalCounter = communalCounter + 1;
    }


}

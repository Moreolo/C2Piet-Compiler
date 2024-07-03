package piet;

import java.util.ArrayList;
import java.util.Dictionary;
import java.util.HashMap;
import java.util.Hashtable;
import java.util.LinkedList;

import ast.datatypes.Node;
import ast.datatypes.NodeTypesEnum;
import basicblocks.BBMain.BlockLists;
import basicblocks.datatypes.BBlock;
import basicblocks.datatypes.CondBlock;
import basicblocks.datatypes.FunCallBlock;
import basicblocks.datatypes.FunDefBlock;
import piet.datatypes.Block;
import piet.datatypes.Command;
import piet.datatypes.Operation;

public class Piet {

    LinkedList<String> VariablenSpeicher = new LinkedList<>();
    //LinkedList<String> FunktionsVariablenSpeicher = new LinkedList<>();
    Dictionary<String, Integer> functionVariableSpeicher = new Hashtable<>(); // key ist der param name und der wert ist die position auf dem stack
    Dictionary<String, LinkedList<String>> functionParamsDict = new Hashtable<>();
    Dictionary<String, Integer> functionIDsDict = new Hashtable<>(); // dictionary in dem funktionname zu id des anfangsblocks gemapped wird
    LinkedList<Integer> returnIds = new LinkedList<>(); //linked list in der die ids gespeichert werden zu denen returnt werden muss -> last in first out

    int ProgramCounter = 0;

    int return_tmp_pos = -1;

    public LinkedList<Block> parse(BlockLists list) throws Error{
        ArrayList<BBlock> bblocks = list.blockList;
        HashMap<String,Integer> funcMap = list.functionIndexMap;
        
        LinkedList<Block> finalBlocks = new LinkedList<>();  
        int num = 1;
        for (BBlock block : bblocks) {
            if (block instanceof CondBlock) finalBlocks.add(parseConditionBlock((CondBlock)block, num));
            else if (block instanceof FunDefBlock) finalBlocks.add(parseFunctionDefBlock((FunDefBlock) block, funcMap));
            else if (block instanceof FunCallBlock) finalBlocks.add(parseFunctionCallBlock((FunCallBlock) block, num));
            else if (block instanceof BBlock) finalBlocks.add(parseBBlock(block, num));
            else  return finalBlocks; //throw new Error("Unbekannter BlockTyp");  
            num += 1;
        }
        return finalBlocks;
    }

    private Block parseFunctionDefBlock(FunDefBlock bblock, HashMap<String,Integer> funcMap) throws Error{
        boolean return_flag = false;

        Node func_def_node = bblock.getBody().get(0);
        if (func_def_node.getType() != NodeTypesEnum.FUNCTION_DEF){
            return null;//throw new Error("Erster Node muss vom Typ FUNCTION_DEF sein");
        }
        String functionName = parseFunctionDef(func_def_node);
        
        int func_id = funcMap.get(functionName);
        var block = new Block(func_id);
        functionIDsDict.put(functionName, func_id);

        for (int i=1; i<bblock.getBody().size(); i++){ //fange bei 1 zu loopen da erster Node schon analysiert wurde
            Node node = bblock.getBody().get(i);
            //if(node.getType() == NodeTypesEnum.BLOCK_STATEMENT) ; //return as BLOCK_STATEMENT
            if(node.getType() == NodeTypesEnum.BINARY_EXPRESSION){
                block = solveBinaryExpresssion(block, node); //return as BINARY_EXPRESSION
            } 
            if(node.getType() == NodeTypesEnum.DECLARATION){
                block = parseDeclarations(block, node, functionName);
            }
            if(node.getType() == NodeTypesEnum.FUNCTION_CALL){
                block = parseFunctionCall(block, node, bblock.getNext()); //return as FUNCTION_CALL
            }
            if(node.getType() == NodeTypesEnum.RETURN_STATEMENT){ 
                block = parseReturnStatement(block, node, functionName); //return as RETURN_STATEMENT
                return_flag = true;
            }
            else{
                throw new Error("Unvalider NodeTyp in FunctionDefBlock");
            }
        }
        if (!return_flag){
            block.addOperation(new Operation(Command.PUSH, bblock.getNext()));
        }
        return block;
    }

    private Block parseFunctionCallBlock(BBlock bblock, int num) throws Error{
        var block = new Block(num);
        var nodes = bblock.getBody();

        //Überprüfen, ob der Condition-BBlock richtiges Format hat (darf nur einen Node enthalten)
        if (nodes.size() != 1) {
            return block;//throw new Error("FunctionCallBlock darf maximal einen Node enthalten");
        }

        Node node = nodes.get(0);

        if (node.getType() != NodeTypesEnum.FUNCTION_CALL){
            return block;//throw new Error("Node in FunctionCallBlock muss vom Typ FUNCTION_CALL sein");
        }
        block = parseFunctionCall(block, node, bblock.getNext()); //return as FUNCTION_CALL
        return block;
    }

    private Block parseBBlock(BBlock bblock, int num) throws Error{
        var block = new Block(num);
        for (Node node : bblock.getBody()) {
            if(node.getType() == NodeTypesEnum.BINARY_EXPRESSION){
                block = solveBinaryExpresssion(block, node); //return as BINARY_EXPRESSION
            } 
            if(node.getType() == NodeTypesEnum.DECLARATION){
                block = parseDeclarations(block, node, "");
            }
            else{
                return block;//throw new Error("Unvalider NodeTyp in BBlock");
            }
        }
        // Set Pointer for next Block
        if (bblock.getNext() == null) {
            block.addOperation(new Operation(Command.PUSH, 0));
        } else {
            block.addOperation(new Operation(Command.PUSH, bblock.getNext()));
        }
        return block;
    }

    private Block parseConditionBlock(CondBlock bblock, int num) throws Error{
        /**
        * parseCondition parsed die Condition-BBlocks
        * @param BBlock bblock ist der Condition-BBLock der als input dient
        * @return int num gibt die id des BBlocks an
        */

        //Initialisierung der nodes und des Blocks in dem die Piet-Commands gespeichert werden
        var nodes = bblock.getBody();
        var block = new Block(num);

        //Überprüfen, ob der Condition-BBlock richtiges Format hat (darf nur einen Node enthalten)
        if (nodes.size() != 1) {
            return block;//throw new Error("ConditionBlock darf maximal einen Node enthalten");
        }
        //über Node loopen
        try {
            for (Node node : nodes) {
                //Condition aus dem Node ziehen
                if (node.getCondition() != null) {   
                    var condition = node.getCondition();
                    //Condition analysieren -> Block mit commands kommt zurück
                    block = analyseConditionNode(block, condition);
                    //Pointer Command noch zu Block hinzufügen 
                    block.addOperation(new Operation(Command.POINTER, bblock.getNext(), bblock.getAlt())); // für condition bblock bitte noch getAlt function hinzufügen!!!
                } else {
                    block = analyseConditionNode(block, node);
                    //Pointer Command noch zu Block hinzufügen 
                    block.addOperation(new Operation(Command.POINTER, bblock.getNext(), bblock.getAlt())); // für condition bblock bitte noch getAlt function hinzufügen!!!
                }
            }    
        } catch (Exception e) {
            //Possible null reference from get.Next / get.Alt
        }
        return block;
    }

    private Block analyseConditionNode(Block block, Node condition) throws Error{
        /**
        * analyseConditionNode analysiert Condition-Nodes
        * @param Block block ist der Block in dem die Piet-Commands gespeichert werden
        * @return Node condition ist die condition aus dem Condition-BBlock
        */

        //Initialisierung von Variablen
        var left = condition.getLeft();
        var right = condition.getRight();
        var op = condition.getOperator();

        //Überprüfung ob linke Seite der Condition richtiges Format hat
        block = resolveType(block, left);

        //Überprüfung ob rechte Seite der Condition richiges Format hat -> und befördere wert auf die spitze des stacks
        block = resolveType(block, right);

        //Switch between different operators
        switch (op) {
            //Für "==" erst größer Check dann kleiner Check, wenn beide falsch wissen wir dass ergebniss gleich ist
            case "==":
                //Führe erst Größer Check aus
                block.addOperation(new Operation(Command.GREATER));
                ProgramCounter -= 1;
                // Kopiere die WErte die zu vergleichen sind für einen weiteren Check auf die Spitze
                // Da wir dieses mal kleiner Check durchführen, kopiere Vergleichswerte in anderer Reihenfolge auf Stack
                if(right.getType() == NodeTypesEnum.LITERAL){
                    block.addOperation(new Operation(Command.PUSH, Integer.parseInt(right.getValue())));
                    ProgramCounter += 1;
                }
                else if(right.getType() == NodeTypesEnum.BINARY_EXPRESSION){
                    //lösen der Binary Expression
                    block = solveBinaryExpresssion(block, condition);
                }
                else if(right.getType() == NodeTypesEnum.IDENTIFIER){
                    block = rotateVariable(block, right.getValue());
                }
                block = rotateVariable(block, left.getValue());
                block.addOperation(new Operation(Command.GREATER));
                ProgramCounter -= 1;
                //Addiere die Ergebnisse der beiden Checks -> wenn addition 0 ergibt wissen wird dass == true ist -> noch NOT Command ausführen
                block.addOperation(new Operation(Command.ADD));
                ProgramCounter -= 1;
                block.addOperation(new Operation(Command.NOT));
                break;
            //Für ">" einfach Greater Command ausführen
            case ">":
                block.addOperation(new Operation(Command.GREATER));
                ProgramCounter -= 1;
                break;
            //Für "<" Vergleichswerte vertauschen und dann Greater Command ausführen
            case "<":
                // rotatiere die obersten werte des Stacks und dann Greater Vergleich
                block.addOperation(new Operation(Command.PUSH, ProgramCounter-1));
                block.addOperation(new Operation(Command.PUSH, ProgramCounter));
                block.addOperation(new Operation(Command.ROLL));
                block.addOperation(new Operation(Command.GREATER));
                ProgramCounter -= 1;
                break;
            //Für ">=" Vergleichswerte vertauschen und dann Greater Command ausführen und Ergebnis 
            // umkehren mit NOT
            case ">=":
                // rotatiere die obersten werte des Stacks und dann Greater Vergleich dann NOT
                block.addOperation(new Operation(Command.PUSH, ProgramCounter-1));
                block.addOperation(new Operation(Command.PUSH, ProgramCounter));
                block.addOperation(new Operation(Command.ROLL));
                block.addOperation(new Operation(Command.GREATER));
                ProgramCounter -= 1;
                block.addOperation(new Operation(Command.NOT));
                break;
            //Für "<=" Greater Vergleich und Ergebnis mit NOT umkehren
            case "<=":
                block.addOperation(new Operation(Command.GREATER));
                ProgramCounter -= 1;
                block.addOperation(new Operation(Command.NOT));
                break;
        
            default:
            throw new Error("Unbekannter Vergleichs-Operator");
        }
        return block;
    }

    private Block rotateVariable(Block block, String var_name) throws Error{
        /**
        * rotateVariable rotiert die gewünschte Variable an die Spitze des Stacks dupliziert den Wert 
            und rotiert die originale Variable wieder an die ursprüngliche Position zurück
        * @param Block block ist der Block in dem die Piet-Commands gespeichert werden
        * @return String var2rotate variable die an die Spitze des Stacks rotiert werden soll
        */

        int var_pos = -1;
        if ((functionVariableSpeicher.get(var_name) != null)){
            var_pos = functionVariableSpeicher.get(var_name);
        }
        else if (VariablenSpeicher.contains(var_name)){
            var_pos = VariablenSpeicher.indexOf(var_name);
        }
        else {
            throw new Error("Variable wurde nicht definiert (existiert nicht)");
        }

        block.addOperation(new Operation(Command.PUSH, var_pos));
        block.addOperation(new Operation(Command.PUSH, ProgramCounter));
        block.addOperation(new Operation(Command.ROLL));
        block.addOperation(new Operation(Command.DUPLICATE));
        ProgramCounter += 1;
        block.addOperation(new Operation(Command.PUSH, ProgramCounter));
        block.addOperation(new Operation(Command.PUSH, var_pos));
        block.addOperation(new Operation(Command.ROLL));
        return block;
    }

    private Block resolveType(Block block, Node node) throws Error{
        /**
        * resolveType checkt ob node von richtigem Type ist und befördert den Wert des Nodes an die Spitze des Stacks -> löst Variablen und BinaryExpressions direkt auf
        * @param Block block ist der Block in dem die Piet-Commands gespeichert werden
        * @param Node node ist die Node der Assignment Expression
        * @return Block block der mit Commands erweiterte block wird returned
        */
        try {
            //Überprüfung ob Condition richiges Format hat (Rechts darf nur Typ IDENTIFIER oder LITERAL sein)
            if(node.getType() == NodeTypesEnum.LITERAL){
                //Pushe Wert auf die Spitze des Stacks, um später Vergleich darauf auszuführen
                block.addOperation(new Operation(Command.PUSH, Integer.parseInt(node.getValue())));
                ProgramCounter += 1;
            }
            else if(node.getType() == NodeTypesEnum.BINARY_EXPRESSION){
                //lösen der Binary Expression
                block = solveBinaryExpresssion(block, node);
            }
            else if(node.getType() == NodeTypesEnum.IDENTIFIER){
                //Kopiere Wert der Variable auf die Spitze des Stacks, um später Vergleich darauf auszuführen
                String var_name = node.getValue();
                block = rotateVariable(block, var_name);
            }
            else if(node.getType() == NodeTypesEnum.FUNCTION_TEMP_RETURN){
                //Return wert der Funktion der zwischenzeitig auf Stack liegt an position return_tmp_position
                if (return_tmp_pos != -1){
                    block.addOperation(new Operation(Command.PUSH, return_tmp_pos));
                    block.addOperation(new Operation(Command.PUSH, ProgramCounter));
                    block.addOperation(new Operation(Command.ROLL));
                    return_tmp_pos = -1;
                }
                else{
                    throw new Error("kein Return Wert auf Stack");
                }
            }
            else{
                throw new Error("Es können nur die NodeTypen LITERAL, BINARY_EXPRESSION, IDENTIFIER und FUNCTION_TEMP_RETURN resolved werden");
            }
        } catch (Exception e) {
            // Falscher Datentyp z.B. buchstabe in LITERAL
        }
        return block;
    }
    
    private Block parseAssignmentExpression(Block block, Node node) throws Error{
        /**
        * parseAssignmentExpression parsed Assignment Expressions
        * @param Block block ist der Block in dem die Piet-Commands gespeichert werden
        * @param Node node ist die Node der Assignment Expression
        * @return Block block der mit Commands erweiterte block wird returned
        */

        // Check ob linke seite vom Type IDENTIFIER ist
        Node left = node.getLeft();
        String varString = "";
        if (left.getType() != NodeTypesEnum.IDENTIFIER){
            return block;//throw new Error("Linker Node bei AssignmentExpression muss vom NodeTyp IDENTIFIER sein");
        }

        varString = left.getValue();

        // Check ob rechte seite der assignment expression richtigen typ hat
        Node right = node.getRight();
        // pushe werte der rechten Seite auf den Stack
        block = resolveType(block, right);

        // Check ob variable schon auf Stack gespeichert ist
        if (VariablenSpeicher.contains(varString)){
            // wenn Variable schon auf Stack liegt -> rotiere Variable zur Spitze des Stacks und update Wert
            int varpos = VariablenSpeicher.indexOf(varString);
            block.addOperation(new Operation(Command.PUSH, varpos));
            block.addOperation(new Operation(Command.PUSH, ProgramCounter));
            block.addOperation(new Operation(Command.ROLL));
            block.addOperation(new Operation(Command.POP));
            ProgramCounter -= 1;
            block.addOperation(new Operation(Command.PUSH, ProgramCounter));
            block.addOperation(new Operation(Command.PUSH, varpos));
            block.addOperation(new Operation(Command.ROLL));
        }
        else if (functionVariableSpeicher.get(varString) != null){
            // wenn es sich um eine Funktionsvariable handelt
            int varpos = functionVariableSpeicher.get(varString);
            block.addOperation(new Operation(Command.PUSH, varpos));
            block.addOperation(new Operation(Command.PUSH, ProgramCounter));
            block.addOperation(new Operation(Command.ROLL));
            block.addOperation(new Operation(Command.POP));
            ProgramCounter -= 1;
            block.addOperation(new Operation(Command.PUSH, ProgramCounter));
            block.addOperation(new Operation(Command.PUSH, varpos));
            block.addOperation(new Operation(Command.ROLL));
        }
        else{
            // Variable nicht definiert
            block.addOperation(new Operation(Command.POP));
            throw new Error("Undefinierte Variable");
        }
        return block;
    }

    private String parseFunctionDef(Node node) {
        /**
        * parseFunctionDef parsed Functions Definitionen
        * @param Node node ist die Node der Function Definition
        * @return Block block der mit Commands erweiterte block wird returned
        */

        String functionName = node.getValue();
        // Initialisiere Linked List in der die Namen der Parameter der Funktion gespeichert werden
        LinkedList<String> params = new LinkedList<>();
        // Loope über alle Parameter der Funktion
        for (int p=0; p < node.getAlternative().size(); p++){
            // Füge Parametername zu LinkedList hinzu
            String param_name = node.getAlternative().get(p).getValue();
            params.add(param_name);
        }
        // erstelle neuen Eintrag im Functions Dictionary -> dort wird der Funktionsname + die zugehörigen Parameternamen gespeichert
        functionParamsDict.put(functionName, params);

        return functionName;
    }

    private Block parseFunctionCall(Block block, Node node, int id2return2){
        /**
        * parseFunctionCall parsed FunctionCalls
        * @param Block block ist der Block in dem die Piet-Commands gespeichert werden
        * @param Node node ist die Node dem Function Call
        * @return Block block der mit Commands erweiterte block wird returned
        */
        
        // Get Funktionsname
        String function_name = node.getValue();
        // Get Paramternamen der Funktion
        LinkedList<String> param_names = functionParamsDict.get(function_name);

        // Pushe BlockId an die nach Funktionscall zurückgekehrt werden soll (wird dann bei return gepopped)
        block.addOperation(new Operation(Command.PUSH, id2return2));
        VariablenSpeicher.add("RETURN_ID"); //Platzhalter auf Variablenspeicher legen, da diese Return BlockID länger auf Stack liegen bleibt und sonst die Positionen nicht mehr stimmen würden
        ProgramCounter += 1;
        // Safe id where Programm needs to return after function call
        returnIds.add(ProgramCounter);

        //Loope über alle Parameter der Funktion
        for (int p = 0; p < node.getAlternative().size(); p++){
            Node param = node.getAlternative().get(p);
            String param_name = param_names.get(p);
            block = resolveType(block, param);
            functionVariableSpeicher.put(param_name, ProgramCounter); // param wird auf top des stacks gepusht und diese position wird im dictionary mit dem param name(key) gespeichert
        }

        // Navigate to Function-Block
        int func_id = functionIDsDict.get(function_name);
        block.addOperation(new Operation(Command.PUSH, func_id));
        return block;
    }

    private Block parseReturnStatement(Block block, Node node, String functionName) throws Error{
        /**
        * parseReturnStatement behandelt return statements und stellt sicher das an den funktionsaufruf zurückgesprungen wird
        * @param Block block ist der Block in dem die Piet-Commands gespeichert werden
        * @param Node node ist die Node dem Function Call
        * @param String functionName ist der Name der Funktion aus der returnt wird
        * @return Block block der mit Commands erweiterte block wird returned
        */

        boolean return_value_flag = false;
        // get wert des return nodes
        Node value = node.getCondition();
        // checken ob überhaupt etwas returnt wird
        if (value != null){
            // wenn ja pushe return wert auf stack und speichere in als temporäre Variable ab
            block = resolveType(block, value); // berechne return wert und pushe in an die spitze des stacks
            return_value_flag = true;
        }
        
        // get alle funktions parameter der funktion aus der returnt wird und lösche sie aus functionsvariablenspeicher
        LinkedList<String> func_params = functionParamsDict.get(functionName); // get liste von parametern der funktion aus der returnt wird
        for(String func_param : func_params){
            // lösche werte der Parameter und Variablen der Funktion vom Stack
            int varpos = functionVariableSpeicher.get(func_param);
            block.addOperation(new Operation(Command.PUSH, varpos));
            block.addOperation(new Operation(Command.PUSH, ProgramCounter));
            block.addOperation(new Operation(Command.ROLL));
            block.addOperation(new Operation(Command.POP));
            ProgramCounter -= 1;
            functionVariableSpeicher.remove(func_param); // lösche die parameter aus dem functionsvariablenspeicher

            //Wie kann ich noch Variablen die in Funktion definiert wurden von Stack löschen??
        }

        // Check ob StackPointer auf richtige Position zeigt
        if (ProgramCounter != returnIds.getLast()){
            throw new Error("StackPointer zeigt nicht auf die korrekte Return-Adresse");
        }

        if (return_value_flag){
            //tausche return Value mit return Block-ID zu der zurückgekehrt werden muss (BLock ID muss oben liegen)
            block.addOperation(new Operation(Command.PUSH, ProgramCounter-1));
            block.addOperation(new Operation(Command.PUSH, ProgramCounter));
            block.addOperation(new Operation(Command.ROLL));
            VariablenSpeicher.removeLast(); //remove den letzten RETURN_ID-Eintrag aus VariablenSpeicher
            ProgramCounter -= 1; // Setze Programm Counter schon mal eins herunter da Block ID automatisch vom Design Team gepopped wird.
            return_tmp_pos =  ProgramCounter; // -> return_tmp_pos befindet sich an Position ProgramCounter (eins unter Spitze des Stacks)
        }
        else{ //return value und return block-id müssen nicht getauscht werden, wenn es keinen return value gibt
            VariablenSpeicher.removeLast(); //remove den letzten RETURN_ID-Eintrag aus VariablenSpeicher
            ProgramCounter -= 1; // Setze Programm Counter schon mal eins herunter da Block ID automatisch vom Design Team gepopped wird.
        }
        
        //Bei Return wird keine Block-ID(für den nächsten Block) auf Stack gepusht, da dies schon im dazugehörigen Function-Call gemacht wurde
        return block;
    }

    private Block solveBinaryExpresssion(Block block, Node node){
        /**
        * solveBinaryExpresssion behandelt binaryExpressions und löst diese auf -> das ergebniss wird einer variablen zugewiesen oder auf die spitze des stacks gepusht
        * @param Block block ist der Block in dem die Piet-Commands gespeichert werden
        * @param Node node ist die Node dem Function Call
        * @return Block block der mit Commands erweiterte block wird returned
        */
        //x = (2+3)*(4*8)

        // Get ersten Operator der BinaryExpression und checke ob es sich um eine Assignment Expression handelt
        String operator = node.getOperator();
        if (operator == "="){
            parseAssignmentExpression(block, node);
        }
        else {
            // get linken wert der binary expression und löse diesen auf
            Node left = node.getLeft();
            block = resolveType(block, left);
            // get rechten wert der binary expression und löse diesen auf
            Node right = node.getRight();
            block = resolveType(block, right);
            // führe die operationen aus
            switch (node.getOperator()) {
                case "+":
                    block.addOperation(new Operation(Command.ADD));
                    ProgramCounter -= 1;
                    break;
                case "-":
                    block.addOperation(new Operation(Command.SUBTRACT));
                    ProgramCounter -= 1;
                    break;
                case "*":
                    block.addOperation(new Operation(Command.MULTIPLY));
                    ProgramCounter -= 1;
                    break;
                case "/":
                    block.addOperation(new Operation(Command.DIVIDE));
                    ProgramCounter -= 1;
                    break;
                case "%":
                    block.addOperation(new Operation(Command.MOD));
                    ProgramCounter -= 1;
                    break;     
            
                default:
                    break;
            }
        }
        return block;
    }
    private Block parseDeclarations(Block block, Node node, String functionName){
        /**
        * parseDeclarations setzt die Deklaration von Variablen um
        * @param Block block ist der Block in dem die Piet-Commands gespeichert werden
        * @param Node node ist die Node dem Function Call
        * @return Block block der mit Commands erweiterte block wird returned
        */

        // Check ob linke seite vom Type IDENTIFIER ist
        Node left = node.getLeft();
        String varString = "";
        if (left == null) {
            if (node.getCondition() == null) return block;
            
            return parseDeclarations(block, node.getCondition(), functionName);
        }

        varString = left.getValue();

        // Check ob rechte seite der assignment expression richtigen typ hat
        Node right = node.getRight();
        if (right == null) {
            return block;
        }
        // pushe werte der rechten Seite auf den Stack
        block = resolveType(block, right);
        
        if(functionName == ""){
            // initialisiere Variable und speichere Position des Wertes auf Stack
            VariablenSpeicher.add(varString);
        }
        else{
            // initialisiere Variable die in Funktion initialisiert wurde und speichere Position des Wertes auf Stack
            var param_list = functionParamsDict.get(functionName);
            param_list.add(varString);
            functionParamsDict.put(functionName, param_list);
            functionVariableSpeicher.put(varString, ProgramCounter);
        }
        return block;
    }

}


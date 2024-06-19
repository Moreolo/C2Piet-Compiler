package piet;

import static org.junit.Assert.assertEquals;

import java.util.ArrayList;
import java.util.Dictionary;
import java.util.Hashtable;
import java.util.LinkedList;

import ast.datatypes.Node;
import ast.datatypes.NodeTypesEnum;
import basicblocks.datatypes.BBlock;
import basicblocks.datatypes.CondBlock;
import basicblocks.datatypes.FunCallBlock;
import basicblocks.datatypes.TermBlock;
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

    boolean func_flag = false;

    public LinkedList<Block> parse(ArrayList<BBlock> bblocks) {
        LinkedList<Block> finalBlocks = new LinkedList<>();  
        int num = 1;
        for (BBlock block : bblocks) {
            if (block instanceof CondBlock) finalBlocks.add(parseCondition((CondBlock)block, num));
            if (block instanceof FunBlock) finalBlocks.add(parseFunction((FunBlock) block, num));
            else finalBlocks.add(parseBBlock(block, num));  
            num += 1;
        }
        return finalBlocks;
    }

    private Block parseFunction(FunBlock bblock, int num){
        var block = new Block(num);

        boolean return_flag = false;

        Node func_def_node = bblock.getBody().get(0);
        if (func_def_node.getType() != NodeTypesEnum.FUNCTION_DEF){
            System.err.println("First node of function block needs to be function def");
            return block;
        }
        String functionName = parseFunctionDef(func_def_node);
        functionIDsDict.put(functionName, num);
        
        for (Node node : bblock.getBody()) {
            //if(node.getType() == NodeTypesEnum.BLOCK_STATEMENT) ; //return as BLOCK_STATEMENT
            if(node.getType() == NodeTypesEnum.BINARY_EXPRESSION){
                block = solveBinaryExpresssion(block, node); //return as BINARY_EXPRESSION
            } 
            if(node.getType() == NodeTypesEnum.FUNCTION_CALL){
                block = parseFunctionCall(block, node, bblock.getNext()); //return as FUNCTION_CALL
            }
            if(node.getType() == NodeTypesEnum.RETURN_STATEMENT){ 
                block = parseReturnStatement(block, node, functionName); //return as RETURN_STATEMENT
                return_flag = true;
            }
        }
        if (!return_flag){
            block.addOperation(new Operation(Command.PUSH, bblock.getNext()));
        }
        return block;
    }

    private Block parseBBlock(BBlock bblock, int num){
        var block = new Block(num);
        for (Node node : bblock.getBody()) {
            if(node.getType() == NodeTypesEnum.BINARY_EXPRESSION){
                block = solveBinaryExpresssion(block, node); //return as BINARY_EXPRESSION
            } 
            if(node.getType() == NodeTypesEnum.FUNCTION_CALL){
                block = parseFunctionCall(block, node, bblock.getNext()); //return as FUNCTION_CALL
            } 
        }
        // Set Pointer for next Block
        block.addOperation(new Operation(Command.PUSH, bblock.getNext()));
        return block;
    }

    private Block parseCondition(CondBlock bblock, int num){
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
            System.err.println("Condition block can only take one node");
            return block;
        }
        //über Node loopen
        for (Node node : nodes) {
            var type = node.getType();
            //Überprüfung, ob Node vom Typ IF_STATEMENT ist 
            if (!type.equals(NodeTypesEnum.IF_STATEMENT)){
                System.err.println("Conditions block must contain if statement");
                return block;
            }
            //Condition aus dem Node ziehen
            var condition = node.getCondition();
            //Condition analysieren -> Block mit commands kommt zurück
            block = analyseConditionNode(block, condition);
            //Pointer Command noch zu Block hinzufügen 
            block.addOperation(new Operation(Command.POINTER, bblock.getNext(), bblock.getAlt())); // für condition bblock bitte noch getAlt function hinzufügen!!!
        }
        return block;
    }

    private Block analyseConditionNode(Block block, Node condition){
        /**
        * analyseConditionNode analysiert Condition-Nodes
        * @param Block block ist der Block in dem die Piet-Commands gespeichert werden
        * @return Node condition ist die condition aus dem Condition-BBlock
        */

        //Initialisierung von Variablen
        var left = condition.getLeft();
        var right = condition.getRight();
        var op = condition.getOperator();

        if(left.getType() == NodeTypesEnum.LITERAL){
            block.addOperation(new Operation(Command.PUSH, Integer.valueOf(left.getValue())));
            ProgramCounter += 1;
        }

        //Überprüfung ob Condition richiges Format hat (Links darf nur Typ IDENTIFIER sein)
        if(left.getType() == NodeTypesEnum.IDENTIFIER){
            //Kopiere Wert der Variable auf die Spitze des Stacks, um später Vergleich darauf auszuführen
            block = rotateVariable(block, left.getValue());
        }
        else{
            System.err.println("Left Side of Condition needs to be Identifier or Literal");
        }
        //Überprüfung ob Condition richiges Format hat (Rechts darf nur Typ IDENTIFIER oder LITERAL sein)
        if(right.getType() == NodeTypesEnum.LITERAL){
            //Pushe Wert auf die Spitze des Stacks, um später Vergleich darauf auszuführen
            block.addOperation(new Operation(Command.PUSH, Integer.parseInt(right.getValue())));
            ProgramCounter += 1;
        }
        else if(right.getType() == NodeTypesEnum.BINARY_EXPRESSION){
            //lösen der Binary Expression
            block = solveBinaryExpresssion(block, condition);
        }
        else if(right.getType() == NodeTypesEnum.IDENTIFIER){
            //Kopiere Wert der Variable auf die Spitze des Stacks, um später Vergleich darauf auszuführen
            block = rotateVariable(block, right.getValue());
        }
        else{
            System.err.println("Right Side of Condition needs to be Identifier or Literal");
        }

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
            //Für "<" Vergleichswerte vertauschen und dann Greater Command ausführen und Ergebnis umkehren mit NOT
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
                System.err.println("Unknown Operator");
                break;
        }
        return block;
    }

    private Block rotateVariable(Block block, String var_name){
        /**
        * rotateVariable rotiert die gewünschte Variable an die Spitze des Stacks dupliziert den Wert 
            und rotiert die originale Variable wieder an die ursprüngliche Position zurück
        * @param Block block ist der Block in dem die Piet-Commands gespeichert werden
        * @return String var2rotate variable die an die Spitze des Stacks rotiert werden soll
        */

        int var_pos = -1;
        if ((functionVariableSpeicher.get(var_name) != null) && func_flag){
            var_pos = functionVariableSpeicher.get(var_name);
        }
        else if (VariablenSpeicher.contains(var_name)){
            var_pos = VariablenSpeicher.indexOf(var_name);
        }
        else {
            System.err.println("Variable existiert nicht");
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

    private Block resolveType(Block block, Node node){
        /**
        * resolveType checkt ob node von richtigem Type ist und befördert den Wert des Nodes an die Spitze des Stacks -> löst Variablen und BinaryExpressions direkt auf
        * @param Block block ist der Block in dem die Piet-Commands gespeichert werden
        * @param Node node ist die Node der Assignment Expression
        * @return Block block der mit Commands erweiterte block wird returned
        */

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
            //Return wert der Funktion der zwischenzeitig als return_tmp auf Variablenspeicher liegt
            block = rotateVariable(block, "return_tmp"); 
            VariablenSpeicher.remove("return_tmp"); // lösche tmp return value, wenn er einmal benutzt wurde
        }
        else{
            System.err.println("Node must be of Type Identifier or Literal or Binary Expression");
        }
        return block;
    }
    
    private Block parseAssignmentExpression(Block block, Node node){
        /**
        * parseAssignmentExpression parsed Assignment Expressions
        * @param Block block ist der Block in dem die Piet-Commands gespeichert werden
        * @param Node node ist die Node der Assignment Expression
        * @return Block block der mit Commands erweiterte block wird returned
        */

        // Check ob linke seite vom Type IDENTIFIER ist
        Node left = node.getLeft();
        String varString = "";
        if (left.getType() == NodeTypesEnum.IDENTIFIER){
            varString = left.getValue();
        }
        else{
            System.err.println("Right side of assignment expression must be of Type Identifier");
        }

        // Check ob rechte seite der assignment expression richtigen typ hat
        Node right = node.getRight();
        // pushe werte der rechten Seite auf den Stack
        block = resolveType(block, right);

        // Check ob variable schon auf Stack gespeichert ist
        if (VariablenSpeicher.contains(varString)){
            // wenn Variable schon auf Stack liegt -> rotiere Variable zur Spitze des Stacks und update Wert
            int varpos = VariablenSpeicher.indexOf(varString);
            block = rotateVariable(block, varString);
            block.addOperation(new Operation(Command.POP));
            block.addOperation(new Operation(Command.PUSH, ProgramCounter));
            block.addOperation(new Operation(Command.PUSH, varpos));
            block.addOperation(new Operation(Command.ROLL));
        }
        else if (!varString.equals("")){
            // wenn Variable noch nicht auf Stack liegt -> assigne Variable zum Wert an der Spitze des Stacks (Wert der rechten Seite der Assignment Expression)
            VariablenSpeicher.add(varString);
        }
        return block;
    }

    private String parseFunctionDef(Node node){
        /**
        * parseFunctionDef parsed Functions Definitionen
        * @param Node node ist die Node der Function Definition
        * @return Block block der mit Commands erweiterte block wird returned
        */

        //Überprüfung ob Node vom Type FUNCTION_DEF ist
        Node func = node.getBody().get(0);
        if (func.getType() != NodeTypesEnum.FUNCTION_DEF){
            System.err.println("Node must be of Type FUNCTION_DEF");
            return "";
        }

        String functionName = func.getValue();
        // Initialisiere Linked List in der die Namen der Parameter der Funktion gespeichert werden
        LinkedList<String> params = new LinkedList<>();
        // Loope über alle Parameter der Funktion
        for (int p=0; p < func.getAlternative().size(); p++){
            // Füge Parametername zu LinkedList hinzu
            String param_name = func.getAlternative().get(p).getValue();
            params.add(param_name);
        }
        // erstelle neuen Eintrag im Functions Dictionary -> dort wird der Funktionsname + die zugehörigen Parameternamen gespeichert
        functionParamsDict.put(functionName, params);
        // macht das wirklich sinn den code von der function hier direkt zu parsen??? Also so ists zumindest gerade im ast team gemacht
        // macht iwi weniger sinn. würde tbh mehr sinn machen abzuspeichern zu welchem block gesprungen werden soll wenn die function aufgerufen wird
        
        //var nodes = func.getBody();
        //parseFunction

        return functionName;
    }

    private Block parseFunctionCall(Block block, Node node, int id2return2){
        /**
        * parseFunctionCall parsed FunctionCalls
        * @param Block block ist der Block in dem die Piet-Commands gespeichert werden
        * @param Node node ist die Node dem Function Call
        * @return Block block der mit Commands erweiterte block wird returned
        */
        
        //Überprüfung ob Node vom Type FUNCTION_CALL ist
        Node body = node.getBody().get(0);
        if (body.getType() != NodeTypesEnum.FUNCTION_CALL){
            System.err.println("Function Call muss vom type FUNCTION CALL SEIN");
            return block;
        }
        // Get Funktionsname
        String function_name = body.getValue();
        // Get Paramternamen der Funktion
        LinkedList<String> param_names = functionParamsDict.get(function_name);

        func_flag = true;

        //Loope über alle Parameter der Funktion
        for (int p = 0; p < body.getAlternative().size(); p++){
            Node param = body.getAlternative().get(p);
            String param_name = param_names.get(p);
            //FunktionsVariablenSpeicher.add(param_name); // Wie speicher ich die Position der Variablen, weil mit ProgrammCounter geht ja nicht mehr, weil ich ja n offset habe
            //wenn man mit Variablenspeicher macht, dann hat man aber den nachteil mit dem offset=programmcounter
            if (param.getType() == NodeTypesEnum.LITERAL){
                block.addOperation(new Operation(Command.PUSH, Integer.valueOf(param.getValue())));
                ProgramCounter += 1;
            }
            else if (param.getType() == NodeTypesEnum.IDENTIFIER){
                block = rotateVariable(block, param.getValue());
            }
            else if (param.getType() == NodeTypesEnum.BINARY_EXPRESSION){
                block = solveBinaryExpresssion(block, param);
            }
            else {
                System.err.println("Parameter needs to be Identifier or Literal or Binary Expression");
            }
            functionVariableSpeicher.put(param_name, ProgramCounter); // param wird auf top des stacks gepusht und diese position wird im dictionary mit dem param name(key) gespeichert
        }

        // Safe id where Programm needs to return after function call
        returnIds.add(id2return2);

        // Navigate to Function-Block
        int func_id = functionIDsDict.get(function_name);
        block.addOperation(new Operation(Command.PUSH, func_id)); 
        return block;
    }

    private Block parseReturnStatement(Block block, Node node, String functionName){
        /**
        * parseReturnStatement behandelt return statements und stellt sicher das an den funktionsaufruf zurückgesprungen wird
        * @param Block block ist der Block in dem die Piet-Commands gespeichert werden
        * @param Node node ist die Node dem Function Call
        * @param String functionName ist der Name der Funktion aus der returnt wird
        * @return Block block der mit Commands erweiterte block wird returned
        */

        // get wert des return nodes
        Node value = node.getCondition();
        // checken ob überhaupt etwas returnt wird
        if (value != null){
            // wenn ja pushe return wert auf stack und speichere in als temporäre Variable ab
            block = resolveType(block, value); // berechne return wert und pushe in an die spitze des stacks
            VariablenSpeicher.add("return_tmp"); // teile den namen "return_tmp" der spitze des stacks zu
        }

        // get alle funktions parameter der funktion aus der returnt wird und lösche sie aus functionsvariablenspeicher
        LinkedList<String> func_params = functionParamsDict.get(functionName); // get liste von parametern der funktion aus der returnt wird
        for(String func_param : func_params){
            functionVariableSpeicher.remove(func_param); // lösche die parameter aus dem functionsvariablenspeicher
        }

        // Jump back to Program after Function Call
        if (returnIds.size() > 0){
            // get letzten Wert in return id list -> wert der zum letzten Funktionsaufruf passt
            int return_id = returnIds.getLast();
            returnIds.removeLast(); //verwendete return id aus der liste löschen

            // Pushe die return id die Block verweist der nach dem Funktionsaufruf drannkommt
            block.addOperation(new Operation(Command.PUSH, return_id)); // für condition bblock bitte noch getAlt function hinzufügen!!!
            if (returnIds.size() == 0){
                func_flag = false;
            }
        } else{
            System.err.println("There was no return adress left in the return id list");
        }
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
}


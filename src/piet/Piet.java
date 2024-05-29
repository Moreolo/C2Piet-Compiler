package piet;

import java.util.ArrayList;
import java.util.LinkedList;

import ast.datatypes.Node;
import ast.datatypes.NodeTypesEnum;
import basicblocks.datatypes.*;
import piet.datatypes.*;

public class Piet {

    LinkedList<String> VariablenSpeicher = new LinkedList<>();
    LinkedList<String> FunktionsVariablenSpeicher = new LinkedList<>();

    int ProgramCounter = 0;

    public LinkedList<Block> parse(ArrayList<BBlock> bblocks) {
        LinkedList<Block> finalBlocks = new LinkedList<>();  
        int num = 1;
        for (BBlock block : bblocks) {
            if (block instanceof CondBlock) finalBlocks.add(parseCondition(block, num));
            if (block instanceof FunBlock) finalBlocks.add(parseFunction(block, num));
            if (block instanceof TermBlock) finalBlocks.add(parseTerm(block, num));
            else finalBlocks.add(parseBBlock(block, num));  
            num += 1;
        }
        return finalBlocks;
    }

    private Block parseCondition(BBlock bblock, int num){
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
            if (type.equals(NodeTypesEnum.IF_STATEMENT)){
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
        int num;

        //Initialisierung von Variablen
        var left = condition.getLeft();
        var right = condition.getRight();
        var op = condition.getOperator();

        if(left.getType() == NodeTypesEnum.LITERAL){
            block.addOperation(new Operation(Command.PUSH, Integer.parseInt(left.getValue())));
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
            solveBinaryExpresssion(condition, num);
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

    private Block rotateVariable(Block block, String var2rotate){
        /**
        * rotateVariable rotiert die gewünschte Variable an die Spitze des Stacks dupliziert den Wert 
            und rotiert die originale Variable wieder an die ursprüngliche Position zurück
        * @param Block block ist der Block in dem die Piet-Commands gespeichert werden
        * @return String var2rotate variable die an die Spitze des Stacks rotiert werden soll
        */
        var varpos = VariablenSpeicher.indexOf(var2rotate);
        block.addOperation(new Operation(Command.PUSH, varpos));
        block.addOperation(new Operation(Command.PUSH, ProgramCounter));
        block.addOperation(new Operation(Command.ROLL));
        block.addOperation(new Operation(Command.DUPLICATE));
        ProgramCounter += 1;
        block.addOperation(new Operation(Command.PUSH, ProgramCounter));
        block.addOperation(new Operation(Command.PUSH, varpos));
        block.addOperation(new Operation(Command.ROLL));
        return block;
    }
    
    private Block parseFunction(BBlock block, int num){
        return new Block(num);
    }
    
    private Block parseTerm(BBlock block, int num){
        return new Block(num);
    }

    private Block parseBBlock(BBlock bblock, int num){
        var block = new Block(num);
        for (Node node : bblock.getBody()) {
            if(node.getType() == NodeTypesEnum.ASSIGNMENT_EXPRESSION) return parseAssignmentExpression(block, node); //return as ASSIGNMENT_EXPRESSION
            if(node.getType() == NodeTypesEnum.BLOCK_STATEMENT) ; //return as BLOCK_STATEMENT
            if(node.getType() == NodeTypesEnum.BINARY_EXPRESSION) return solveBinaryExpresssion(block, node); //return as BINARY_EXPRESSION
            if(node.getType() == NodeTypesEnum.FUNCTION_CALL) return parseFunctionCall(block, node); //return as FUNCTION_CALL
            if(node.getType() == NodeTypesEnum.FUNCTION_DEF) ; //return as FUNCTION_DEF
            if(node.getType() == NodeTypesEnum.RETURN_STATEMENT) ; //return as RETURN_STATEMENT
        }
        return block;
    }

    private Block parseAssignmentExpression(Block block, Node node){
        String varString = node.getLeft().getValue();// wie zur Hölle soll ich bitte den variablen namen bekommen, weil mit getValue gehts nicht (kommt ja nur int) und sonst gibts ja nichts anderes. 
        Node right = node.getRight();
        if (right.getType() == NodeTypesEnum.BINARY_EXPRESSION){
            solveBinaryExpresssion(block, node);
        }
        else if (right.getType() == NodeTypesEnum.LITERAL){
            block.addOperation(new Operation(Command.PUSH, right.getValue()));
        }
        else {
            System.err.println("Right Part of Assignment must be of Type LITERAL OR BINARY EXPRESSION");
        }
        if (VariablenSpeicher.contains(varString)){
            int varpos = VariablenSpeicher.indexOf(varString);
            block = rotateVariable(block, varString);
            block.addOperation(new Operation(Command.POP));
            block.addOperation(new Operation(Command.PUSH, ProgramCounter));
            block.addOperation(new Operation(Command.PUSH, varpos));
            block.addOperation(new Operation(Command.ROLL));
        }
        else{
            VariablenSpeicher.add(varString);
            ProgramCounter += 1;
        }
        return block;
    }

    private Block parseFunctionCall(Block block, Node node){
        // wie sieht ein Function Call als BasicBlock aus???
        var body = node.getBody();
        for (String param : body.getValue()){
            block = rotateVariable(block, param);
            VariablenSpeicher.add(param + "function"); //vlt mit functionsvariablenspeicher machen, dann hat man aber den nachteil mit dem offset=programmcounter
        }
        return block;
    }


    private static Block solveBinaryExpresssion(Block block, Node node){
        // WEiß nicht ob wir das einfach so implementieren können 
        // was ist z.b. mit: x = (2+3)*(4*8) ??
        // das sind ja mehrere binary expressions ineinander verschachtelt das müssen wir ja iwi auflösen oder nicht 
        // oder wird das schon von den teams davor aufgelöst in: 
        // x = 2 + 3
        // temp = 4*8
        // x = x * temp

        int leftValue = node.getLeft().getValue();
        int rightValue = node.getRight().getValue();
        block.addOperation(new Operation(Command.PUSH, leftValue));
        block.addOperation(new Operation(Command.PUSH, rightValue));
        
        switch (node.getOperator()) {
            case "=":
                block = parseAssignmentExpression(block, node);
                break;
            case "+":
                block.addOperation(new Operation(Command.ADD));
                break;
            case "-":
                block.addOperation(new Operation(Command.SUBTRACT));
                break;
            case "*":
                block.addOperation(new Operation(Command.MULTIPLY));
                break;
            case "/":
                block.addOperation(new Operation(Command.DIVIDE));
                break;
            case "%":
                block.addOperation(new Operation(Command.MOD));
                break;     
        
            default:
                break;
        }


        if(node.getLeft().getType() != NodeTypesEnum.LITERAL) solveBinaryExpresssion(block, node.getLeft());
        if(node.getRight().getType() != NodeTypesEnum.LITERAL) solveBinaryExpresssion(block, node.getRight());
        
        
        return new Block(0);
    }
}


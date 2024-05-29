package piet;

import java.util.ArrayList;
import java.util.LinkedList;

import ast.datatypes.Node;
import ast.datatypes.NodeTypesEnum;
import basicblocks.datatypes.BBlock;
import basicblocks.datatypes.CondBlock;
import basicblocks.datatypes.FunBlock;
import basicblocks.datatypes.TermBlock;
import piet.datatypes.Block;
import piet.datatypes.Command;
import piet.datatypes.Operation;

public class Piet {

    LinkedList<String> VariablenSpeicher = new LinkedList<>();

    int ProgramCounter = 0;

    public LinkedList<Block> parse(ArrayList<BBlock> blocks) {
        LinkedList<Block> finalBlocks = new LinkedList<>();
        int num = 1;
        for (BBlock block : blocks) {
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
            block.addOperation(new Operation(Command.POINTER, block.left.getValue(), block.right.getValue()));
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

        //if(left.getType() == NodeTypesEnum.LITERAL){
        //    block.addOperation(new Operation(Command.PUSH), left.getValue());
        //    ProgramCounter += 1;
        //}

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
                // Kopiere die WErte die zu vergleichen sind für einen weiteren Check auf die Spitze
                // Da wir dieses mal kleiner Check durchführen, kopiere Vergleichswerte in anderer Reihenfolge auf Stack
                if(right.getType() == NodeTypesEnum.LITERAL){
                    block.addOperation(new Operation(Command.PUSH, Integer.parseInt(right.getValue())));
                    ProgramCounter += 1;
                }
                else if(right.getType() == NodeTypesEnum.IDENTIFIER){
                    block = rotateVariable(block, right.getValue());
                }
                block = rotateVariable(block, left.getValue());
                block.addOperation(new Operation(Command.GREATER));
                //Addiere die Ergebnisse der beiden Checks -> wenn addition 0 ergibt wissen wird dass == true ist -> noch NOT Command ausführen
                block.addOperation(new Operation(Command.ADD));
                block.addOperation(new Operation(Command.NOT));
                break;
            //Für ">" einfach Greater Command ausführen
            case ">":
                block.addOperation(new Operation(Command.GREATER));
                break;
            //Für "<" Vergleichswerte vertauschen und dann Greater Command ausführen
            case "<":
                // rotatiere die obersten werte des Stacks und dann Greater Vergleich
                block.addOperation(new Operation(Command.PUSH, ProgramCounter-1));
                block.addOperation(new Operation(Command.PUSH, ProgramCounter));
                block.addOperation(new Operation(Command.ROLL));
                block.addOperation(new Operation(Command.GREATER));
                break;
            //Für "<" Vergleichswerte vertauschen und dann Greater Command ausführen und Ergebnis umkehren mit NOT
            case ">=":
                // rotatiere die obersten werte des Stacks und dann Greater Vergleich dann NOT
                block.addOperation(new Operation(Command.PUSH, ProgramCounter-1));
                block.addOperation(new Operation(Command.PUSH, ProgramCounter));
                block.addOperation(new Operation(Command.ROLL));
                block.addOperation(new Operation(Command.GREATER));
                block.addOperation(new Operation(Command.NOT));
                break;
            //Für "<=" Greater Vergleich und Ergebnis mit NOT umkehren
            case "<=":
                block.addOperation(new Operation(Command.GREATER));
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
        * rotateVariable rotiert die gewünschte Variable an die Spitze des Stacks dupliziert den Wert und rotiert die originale Variable wieder an die ursprüngliche Position zurück
        * @param Block block ist der Block in dem die Piet-Commands gespeichert werden
        * @return String var2rotate variable die an die Spitze des Stacks rotiert werden soll
        */
        var varpos = VariablenSpeicher.indexOf(var2rotate);
        block.addOperation(new Operation(Command.PUSH, varpos));
        block.addOperation(new Operation(Command.PUSH, ProgramCounter));
        block.addOperation(new Operation(Command.ROLL));
        block.addOperation(new Operation(Command.DUPLICATE));
        block.addOperation(new Operation(Command.PUSH, ProgramCounter));
        block.addOperation(new Operation(Command.PUSH, varpos));
        block.addOperation(new Operation(Command.ROLL));
        return block;
    }
    
    private Block parseFunction(BBlock block, int num){
        return new Block(num);
    }
    
    private Block parseTerm(BBlock block, int num){
        return new Block(0);
    }

    private Block parseBBlock(BBlock block, int num){
        for (Node node : block.getBody()) {
            if(node.getType() == NodeTypesEnum.ASSIGNMENT_EXPRESSION) return parseAssignmentExpression(node, num); //return as ASSIGNMENT_EXPRESSION
            if(node.getType() == NodeTypesEnum.BLOCK_STATEMENT){ BBlock subBlock = new BBlock(num); ArrayList<Node> list = new ArrayList<>(); list.addAll(node.getBody());
                subBlock.setBody(list);  
                return parseBBlock(subBlock, num);
            }
            if(node.getType() == NodeTypesEnum.BINARY_EXPRESSION) return solveBinaryExpresssion(node, num); //return as BINARY_EXPRESSION
            if(node.getType() == NodeTypesEnum.FUNCTION_CALL) ; //return as FUNCTION_CALL
            if(node.getType() == NodeTypesEnum.FUNCTION_DEF) ; //return as FUNCTION_DEF
            if(node.getType() == NodeTypesEnum.RETURN_STATEMENT) ; //return as RETURN_STATEMENT
        }
        return new Block(0);
    }

    private Block parseAssignmentExpression(Node node, int num){
        Block block = new Block(num);
        String varString = node.getLeft().getOperator(); // wie zur Hölle soll ich bitten den variablen namen bekommen, weil mit getValue gehts nicht (kommt ja nur int) und sonst gibts ja nichts anderes. 
        int rightval = Integer.parseInt(node.getRight().getValue());
        VariablenSpeicher.add(varString);
        ProgramCounter += 1;
        block.addOperation(new Operation(Command.PUSH, rightval));
        return block;
    }


    private static Block solveBinaryExpresssion(Node node, int num){
        Block finalBlock = new Block(num);
        int leftValue = Integer.parseInt(node.getLeft().getValue());
        int rightValue = Integer.parseInt(node.getRight().getValue());
        finalBlock.addOperation(new Operation(Command.PUSH, leftValue));
        finalBlock.addOperation(new Operation(Command.PUSH, rightValue));
        
        switch (node.getOperator()) {
            case "+":
                finalBlock.addOperation(new Operation(Command.ADD));
                break;
            case "-":
                finalBlock.addOperation(new Operation(Command.SUBTRACT));
                break;
            case "*":
                finalBlock.addOperation(new Operation(Command.MULTIPLY));
                break;
            case "/":
                finalBlock.addOperation(new Operation(Command.DIVIDE));
                break;
            case "%":
                finalBlock.addOperation(new Operation(Command.MOD));
                break;
                
        
            default:
                break;
        }


        if(node.getLeft().getType() != NodeTypesEnum.LITERAL) solveBinaryExpresssion(node.getLeft(), num);
        if(node.getRight().getType() != NodeTypesEnum.LITERAL) solveBinaryExpresssion(node.getRight(), num);
        
        
        return new Block(0);
    }

}


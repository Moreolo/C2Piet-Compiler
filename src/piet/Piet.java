package piet;

import java.util.ArrayList;
import java.util.LinkedList;

import ast.datatypes.Node;
import ast.datatypes.NodeTypesEnum;
import basicblocks.datatypes.*;
import piet.datatypes.Block;
import piet.datatypes.Operation;
import piet.datatypes.*;

public class Piet {

    LinkedList<String> VariablenSpeicher = new LinkedList<>();

    int ProgramCounter = 0;

    public static LinkedList<Block> parse(ArrayList<BBlock> blocks) {
        LinkedList<Block> finalBlocks = new LinkedList<>();

        for (BBlock block : blocks) {
            if (block instanceof CondBlock) finalBlocks.add(parseCondition(block));
            else if (block instanceof FunBlock) finalBlocks.add(parseFunction(block));
            else if (block instanceof TermBlock)finalBlocks.add(parseTerm(block));
            else parseBBlock(block);    
        }

        return finalBlocks;
    }

    private static Block parseCondition(BBlock block){
        return new Block(0);
    }
    
    private static Block parseFunction(BBlock block){
        return new Block(0);
    }
    
    private static Block parseTerm(BBlock block){
        return new Block(0);
    }

    private static Block parseBBlock(BBlock block){
        for (Node node : block.getBody()) {
            if(node.getType() == NodeTypesEnum.ASSIGNMENT_EXPRESSION) ; //return as ASSIGNMENT_EXPRESSION
            if(node.getType() == NodeTypesEnum.BLOCK_STATEMENT) ; //return as BLOCK_STATEMENT
            if(node.getType() == NodeTypesEnum.BINARY_EXPRESSION) return solveBinaryExpresssion(node); //return as BINARY_EXPRESSION
            if(node.getType() == NodeTypesEnum.FUNCTION_CALL) ; //return as FUNCTION_CALL
            if(node.getType() == NodeTypesEnum.FUNCTION_DEF) ; //return as FUNCTION_DEF
            if(node.getType() == NodeTypesEnum.RETURN_STATEMENT) ; //return as RETURN_STATEMENT
        }
        return new Block(0);
    }


    private static Block solveBinaryExpresssion(Node node){
        Block finalBlock = new Block(0);
        int leftValue = node.getLeft().getValue();
        int rightValue = node.getRight().getValue();
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


        if(node.getLeft().getType() != NodeTypesEnum.LITERAL) solveBinaryExpresssion(node.getLeft());
        if(node.getRight().getType() != NodeTypesEnum.LITERAL) solveBinaryExpresssion(node.getRight());
        
        
        return new Block(0);
    }


}

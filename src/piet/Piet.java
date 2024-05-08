package piet;

import java.util.ArrayList;
import java.util.LinkedList;

import basicblocks.datatypes.*;
import piet.datatypes.Block;

public class Piet {

    LinkedList<Integer> VariablenSpeicher = new LinkedList<>();

    int ProgramCounter = 0;

    public static LinkedList<Block> parse(ArrayList<BBlock> blocks) {
        for (BBlock block : blocks) {
            if (block instanceof CondBlock) parseCondition(block);
            if (block instanceof FuncBlock) parseFunction(block);
            if (block instanceof TermBlock) parseTerm(block);
        }
    }

    private Block parseCondition(Block block){

    }
    
    private Block parseFunction(Block block){

    }
    
    private Block parseTerm(Block block){

    }
}

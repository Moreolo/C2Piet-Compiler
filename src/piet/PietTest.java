package piet;

import java.util.ArrayList;
import java.util.LinkedList;

import org.junit.Test;

import ast.datatypes.Node;
import ast.datatypes.NodeTypesEnum;
import basicblocks.datatypes.BBlock;
import piet.datatypes.Block;

public class PietTest {

    @Test
    public void testParse() {
        ArrayList<BBlock> blocks = new ArrayList<>();

        BBlock block1 = new BBlock(2);
        block1.addNodeToBody(
            new Node(NodeTypesEnum.ASSIGNMENT_EXPRESSION, null, 0,
             new Node(NodeTypesEnum.IDENTIFIER, null, 0, null, null, null, null), "=", 
              new Node(NodeTypesEnum.LITERAL, null, 5, null, null, null, null), null));

              blocks.add(block1);
        try {
            Piet piet = new Piet();
            
            LinkedList<Block> testCase = piet.parse(blocks);
            
        } catch (Error e) {
            
        }
    }
}
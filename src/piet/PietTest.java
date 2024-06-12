package piet;

import static org.junit.Assert.assertEquals;

import java.util.ArrayList;
import java.util.LinkedList;
import java.util.LinkedList;

import org.junit.Test;

import ast.datatypes.Node;
import ast.datatypes.NodeTypesEnum;
import basicblocks.datatypes.BBlock;
import piet.datatypes.Block;

public class PietTest {

    @Test
    public void testAssignment() {
        ArrayList<BBlock> blocks = new ArrayList<>();

        BBlock block1 = new BBlock(2);
        block1.addNodeToBody(
            new Node(NodeTypesEnum.BINARY_EXPRESSION, null, "",
             new Node(NodeTypesEnum.IDENTIFIER, null, "x", null, "", null, null, null),
              "=", 
             new Node(NodeTypesEnum.LITERAL, null, "5", null, null, null, null, null),
             null, null));

              blocks.add(block1);
        try {
            Piet piet = new Piet();
            LinkedList<Block> testCase = piet.parse(blocks);
            
            assertEquals(testCase.get(0).getOperations().get(0).getName(), "PUSH");
            int x = 0;


        } catch (Error e) {
            System.out.println(e);
        }
    }

    @Test
    public void testCondition() {
        ArrayList<BBlock> blocks = new ArrayList<>();
        ArrayList<Node> nodeBody = new ArrayList<>();
        nodeBody.add(
            new Node(NodeTypesEnum.BINARY_EXPRESSION, null, "",
            new Node(NodeTypesEnum.IDENTIFIER, null, "x", null, "", null, null, null),
            "=", 
            new Node(NodeTypesEnum.LITERAL, null, "5", null, null, null, null, null),
            null, null)
        );
        nodeBody.add(
            new Node(NodeTypesEnum.BINARY_EXPRESSION, null, "",
            new Node(NodeTypesEnum.IDENTIFIER, null, "x", null, "", null, null, null),
            "=", 
            new Node(NodeTypesEnum.LITERAL, null, "2", null, null, null, null, null),
            null, null)
        );

        BBlock block1 = new BBlock(2);
        block1.addNodeToBody(
            new Node(NodeTypesEnum.IF_STATEMENT, nodeBody, "",
            new Node(NodeTypesEnum.IDENTIFIER, null, "x", null, "", null, null, null),
            "=", 
            new Node(NodeTypesEnum.LITERAL, null, "5", null, null, null, null, null),
            new Node(NodeTypesEnum.BINARY_EXPRESSION, null, "",
                new Node(NodeTypesEnum.IDENTIFIER, null, "x", null, "", null, null, null),
                "=", 
                new Node(NodeTypesEnum.LITERAL, null, "2", null, null, null, null, null),
                null, null),
            null));


            blocks.add(block1);
        try {
            Piet piet = new Piet();
            LinkedList<Block> testCase = piet.parse(blocks);
            
        } catch (Error e) {
            System.out.println(e);
        }
    }
}
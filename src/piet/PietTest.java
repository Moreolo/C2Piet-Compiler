package piet;

import static org.junit.Assert.assertEquals;

import java.util.ArrayList;
import java.util.LinkedList;

import org.junit.Test;

import ast.datatypes.Node;
import ast.datatypes.NodeTypesEnum;
import basicblocks.datatypes.BBlock;
import basicblocks.datatypes.CondBlock;
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

        CondBlock block1 = new CondBlock(2);
        block1.setNext(1);
        block1.setNext2(2);
        block1.addNodeToBody(
            new Node(NodeTypesEnum.IF_STATEMENT, nodeBody, "",
            null,
            "=", 
            null,
            new Node(NodeTypesEnum.BINARY_EXPRESSION, null, "",
                new Node(NodeTypesEnum.IDENTIFIER, null, "x", null, "", null, null, null),
                "==", 
                new Node(NodeTypesEnum.LITERAL, null, "2", null, null, null, null, null),
                null, null),
            new Node(NodeTypesEnum.TERMINATOR)));


            blocks.add(block1);

            Block firstBlock = new Block(0);
        try {
            Piet piet = new Piet();
            LinkedList<Block> testCase = piet.parse(blocks);
            firstBlock = testCase.get(0);
        } catch (Error e) {
            System.out.println(e);
        }

            assertEquals("PUSH", firstBlock.getOperations().get(0).getName());
            assertEquals("PUSH", firstBlock.getOperations().get(1).getName());
            assertEquals("ROLL", firstBlock.getOperations().get(2).getName());
            assertEquals("DUPLICATE", firstBlock.getOperations().get(3).getName());
            assertEquals("PUSH", firstBlock.getOperations().get(4).getName());
            assertEquals("PUSH", firstBlock.getOperations().get(5).getName());
            assertEquals("ROLL", firstBlock.getOperations().get(6).getName());
            assertEquals("PUSH", firstBlock.getOperations().get(7).getName());
    }

    @Test
    public void testVariables() {
        ArrayList<BBlock> blocks = new ArrayList<>();

        BBlock block1 = new BBlock(2);
        block1.addNodeToBody(
            new Node(NodeTypesEnum.BINARY_EXPRESSION, null, "",
             new Node(NodeTypesEnum.IDENTIFIER, null, "x", null, "", null, null, null),
              "=", 
             new Node(NodeTypesEnum.LITERAL, null, "5", null, null, null, null, null),
             null, null));
        
        block1.addNodeToBody(
        new Node(NodeTypesEnum.BINARY_EXPRESSION, null, "",
            new Node(NodeTypesEnum.IDENTIFIER, null, "y", null, "", null, null, null),
            "=", 
            new Node(NodeTypesEnum.LITERAL, null, "3", null, null, null, null, null),
            null, null));
    
            
              blocks.add(block1);
        try {
            Piet piet = new Piet();
            LinkedList<Block> testCase = piet.parse(blocks);
            
            assertEquals("PUSH", testCase.get(0).getOperations().get(0).getName());


        } catch (Error e) {
            System.out.println(e);
        }
    }
}
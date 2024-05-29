package testing.basicblock;


import org.junit.Test;
import ast.datatypes.*;
import basicblocks.BBMain;
import basicblocks.datatypes.BBlock;

import java.util.ArrayList;



public class NodeTest {

    int a = 0;
    @Test
    public void testNode() {

        Node if1 = new Node(NodeTypesEnum.IF_STATEMENT);
        Node if2 = new Node(NodeTypesEnum.IF_STATEMENT);
        Node bs1 = new Node(NodeTypesEnum.BLOCK_STATEMENT);
        Node bs2 = new Node(NodeTypesEnum.BLOCK_STATEMENT);
        Node bs3 = new Node(NodeTypesEnum.BLOCK_STATEMENT);
        Node bs4 = new Node(NodeTypesEnum.BLOCK_STATEMENT);


        ArrayList<Node> bs1Body = new ArrayList<>();
        ArrayList<Node> bs2Body = new ArrayList<>();
        ArrayList<Node> bs3Body = new ArrayList<>();
        ArrayList<Node> bs4Body = new ArrayList<>();


        if1.setCondition(new Node(NodeTypesEnum.BINARY_EXPRESSION));
        if1.setLeft(bs1);
        if1.setRight(bs4);   

        if2.setLeft(bs2);
        if2.setRight(bs3); 
        

        

        bs1Body.add(new Node(NodeTypesEnum.ASSIGNMENT_EXPRESSION));
        bs1Body.add(if2);
        bs1Body.add(new Node(NodeTypesEnum.ASSIGNMENT_EXPRESSION));
        bs1Body.add(new Node(NodeTypesEnum.ASSIGNMENT_EXPRESSION));

        bs2Body.add(new Node(NodeTypesEnum.ASSIGNMENT_EXPRESSION));
        bs2Body.add(new Node(NodeTypesEnum.ASSIGNMENT_EXPRESSION));

        bs3Body.add(new Node(NodeTypesEnum.ASSIGNMENT_EXPRESSION));

        bs4Body.add(new Node(NodeTypesEnum.ASSIGNMENT_EXPRESSION));


        bs1.setBody(bs1Body);
        bs2.setBody(bs2Body);
        bs3.setBody(bs3Body);
        bs4.setBody(bs4Body);


        if2.setCondition(new Node(NodeTypesEnum.BINARY_EXPRESSION));
        /*
        if (a <= 1) {               // Block 1

            if (a <= 2) {           // Block 2

                a += 2;             // Block 3
                a++;                //

            } else {                // Block 4
                a++;
            }
        } else {                    // Block 5
            a--;
        }
                                    // Block 6 TERM
        */

        ArrayList<BBlock> TestList = BBMain.parse(if1);

        System.out.println(TestList);

    }
    

}
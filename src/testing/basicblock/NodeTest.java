package testing.basicblock;


import org.junit.Test;
import ast.datatypes.*;
import ast.lexer.Lexer;
import ast.lexer.Scan;
import ast.lexer.Token;
import ast.parser.Parser;
import basicblocks.BBMain;
import basicblocks.BBMain.BlockLists;
import basicblocks.datatypes.BBlock;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;



public class NodeTest {

    int a = 0;
    // @Test
    // public void testIf() {

    //     Node if1 = new Node(NodeTypesEnum.IF_STATEMENT);
    //     Node if2 = new Node(NodeTypesEnum.IF_STATEMENT);
    //     Node bs1 = new Node(NodeTypesEnum.BLOCK_STATEMENT);
    //     Node bs2 = new Node(NodeTypesEnum.BLOCK_STATEMENT);
    //     Node bs3 = new Node(NodeTypesEnum.BLOCK_STATEMENT);
    //     Node bs4 = new Node(NodeTypesEnum.BLOCK_STATEMENT);


    //     ArrayList<Node> bs1Body = new ArrayList<>();
    //     ArrayList<Node> bs2Body = new ArrayList<>();
    //     ArrayList<Node> bs3Body = new ArrayList<>();
    //     ArrayList<Node> bs4Body = new ArrayList<>();


    //     if1.setCondition(new Node(NodeTypesEnum.BINARY_EXPRESSION));
    //     if1.setLeft(bs1);
    //     if1.setRight(bs4);   

    //     if2.setLeft(bs2);
    //     if2.setRight(bs3); 
        

        

    //     bs1Body.add(new Node(NodeTypesEnum.DECLARATION));
    //     bs1Body.add(if2);
    //     bs1Body.add(new Node(NodeTypesEnum.DECLARATION));
    //     bs1Body.add(new Node(NodeTypesEnum.DECLARATION));

    //     bs2Body.add(new Node(NodeTypesEnum.DECLARATION));
    //     bs2Body.add(new Node(NodeTypesEnum.DECLARATION));

    //     bs3Body.add(new Node(NodeTypesEnum.DECLARATION));

    //     bs4Body.add(new Node(NodeTypesEnum.DECLARATION));


    //     bs1.setBody(bs1Body);
    //     bs2.setBody(bs2Body);
    //     bs3.setBody(bs3Body);
    //     bs4.setBody(bs4Body);


    //     if2.setCondition(new Node(NodeTypesEnum.BINARY_EXPRESSION));
    //     /*
    //     if (a <= 1) {               // Block 1

    //         if (a <= 2) {           // Block 2

    //             a += 2;             // Block 3
    //             a++;                //

    //         } else {                // Block 4
    //             a++;
    //         }
    //     } else {                    // Block 5
    //         a--;
    //     }
    //                                 // Block 6 TERM
    //     */
    //     BBMain basicBlockMaker = new BBMain();
    //     ArrayList<BBlock> TestList = basicBlockMaker.parse(if1);

    //     System.out.println(TestList);

    // }

    // @Test
    // public void testWhile() {
    //     /* Test Case:
    //     while(condition){
    //         Declaration
    //         while(condition){
    //             Declaration
    //         }
    //         Declaration
    //     }
    //     */
    //     Node while1 = new Node(NodeTypesEnum.WHILE_STATEMENT);
    //     Node while2 = new Node(NodeTypesEnum.WHILE_STATEMENT);
    //     Node dec1 = new Node(NodeTypesEnum.DECLARATION);
    //     Node dec2 = new Node(NodeTypesEnum.DECLARATION);
    //     Node dec3 = new Node(NodeTypesEnum.DECLARATION);
    //     Node condition1 = new Node(NodeTypesEnum.BINARY_EXPRESSION);
    //     Node condition2 = new Node(NodeTypesEnum.BINARY_EXPRESSION);

    //     while1.setCondition(condition1);
    //     while2.setCondition(condition2);

    //     ArrayList<Node> while1Body = new ArrayList<>();
    //     ArrayList<Node> while2Body = new ArrayList<>();
    //     while1.setBody(while1Body);
    //     while2.setBody(while2Body);

    //     while1Body.add(dec1);
    //     while1Body.add(while2);
    //     while1Body.add(dec3);

    //     while2Body.add(dec2);

    //     BBMain basicBlockMaker = new BBMain();
    //     ArrayList<BBlock> TestList = basicBlockMaker.parse(while1);

    //     System.out.println(TestList);
    


    // }
    // @Test
    // public void testFuncDef() {
    //     /*
    //     AST-Ausgangslage:
    //     eine Function definition, die nur eine Deklaration im Inneren hat 
    //     */
    //     Node func1 = new Node(NodeTypesEnum.FUNCTION_DEF);
    //     func1.setValue("normalFunction"); //function Name
    //     ArrayList<Node> funcBody = new ArrayList<>();
    //     Node dec1 = new Node(NodeTypesEnum.DECLARATION);
    //     funcBody.add(dec1);
    //     func1.setBody(funcBody);

    //     BBMain basicBlockMaker = new BBMain();
    //     ArrayList<BBlock> TestList = basicBlockMaker.parse(func1);
    //     System.out.println(TestList);


    // }
    // @Test
    // public void testFunCall1() {
    //     /*
    //     AST-Ausgangslage:
    //     function call, der eine binary expression als parameter hat, in der 
    //     wieder ein function call ist (im rechten Ast der binary expression)
    //     */
    //     Node funcCall1 = new Node(NodeTypesEnum.FUNCTION_CALL);
    //     funcCall1.setValue("normalFunction"); //function Name
    //     ArrayList<Node> parameters = new ArrayList<>();
    //     Node bin1 = new Node(NodeTypesEnum.BINARY_EXPRESSION);
    //     bin1.setLeft(new Node(NodeTypesEnum.LITERAL));
    //     Node funcCall2 = new Node(NodeTypesEnum.FUNCTION_CALL);
    //     funcCall2.setValue("secondFunction");
    //     funcCall2.setAlternative(new ArrayList<>());
    //     bin1.setRight(funcCall2);
    //     parameters.add(bin1);
    //     funcCall1.setAlternative(parameters);

    //     BBMain basicBlockMaker = new BBMain();
    //     ArrayList<BBlock> TestList = basicBlockMaker.parse(funcCall1);
    //     System.out.println(TestList);
    // }

    // @Test
    // public void testFunCall2() {
    //     /*
    //     AST-Ausgangslage:
    //     eine Binary Expression, die im rechten Ast einen Funktionsaufruf hat
    //     */
    //     Node bin1 = new Node(NodeTypesEnum.BINARY_EXPRESSION);
    //     bin1.setLeft(new Node(NodeTypesEnum.LITERAL));
    //     Node funcCall1 = new Node(NodeTypesEnum.FUNCTION_CALL);
    //     funcCall1.setValue("FunctionCall");
    //     funcCall1.setAlternative(new ArrayList<>());
    //     bin1.setRight(funcCall1);
    //     BBMain basicBlockMaker = new BBMain();
    //     ArrayList<BBlock> TestList = basicBlockMaker.parse(bin1);
    //     System.out.println(TestList);
    // }

    // @Test
    // public void testFunCallInDec() {
    //     /*
    //     AST-Ausgangslage:
    //     eine Declaration, die eine Binary Expression hat, die im rechten Ast einen Funktionsaufruf hat
    //     verpackt in einer main function, welche wieder in einer Program Node steckt
    //     */
    //     Node mainFunc = new Node(NodeTypesEnum.FUNCTION_DEF);
    //     mainFunc.setValue("main");
        
    //     Node rootNode = new Node(NodeTypesEnum.PROGRAM);
    //     Node dec1 = new Node(NodeTypesEnum.DECLARATION);
    //     Node bin1 = new Node(NodeTypesEnum.BINARY_EXPRESSION);
    //     bin1.setLeft(new Node(NodeTypesEnum.LITERAL));
    //     Node funcCall1 = new Node(NodeTypesEnum.FUNCTION_CALL);
    //     funcCall1.setValue("FunctionCall");
    //     funcCall1.setAlternative(new ArrayList<>());
    //     bin1.setRight(funcCall1);
    //     dec1.setCondition(bin1);
    //     dec1.setValue("c");
    //     ArrayList<Node> rootBody = new ArrayList<>();
    //     rootBody.add(dec1);

    //     ArrayList<Node> programmBody = new ArrayList<>();
    //     programmBody.add(mainFunc);
    //     rootNode.setBody(programmBody);
    //     mainFunc.setBody(rootBody);
    //     ArrayList<BBlock> TestList = BBMain.parse(rootNode);
    //     System.out.println(TestList);
    // }



        @Test
        public void testWithASTGenerator() {
            /*
            kleines Programm nun mit AST Code zu AST gemacht und dann mit unserer parse verarbeitet

            Bisher: generierter AST enthält komischerweise Variablen vom Typ Node, die aber auf null gesetzt sind
            --> das kann unser Code noch nicht verarbeiten
            */
            /*Node rootNode;

            Scan scanner = new Scan(
                                "int main() {int i = 1; while (i <= 5) {i = (i + 1);}return 0;}");
            List<Token> tokens = scanner.scanTokens();
            Parser parser = new Parser(tokens);
            rootNode = parser.parse(tokens); */
            Node rootNode = null;
            try{
            rootNode = Lexer.runFile("test\\ressources\\ifFunction.c");
            }catch(Exception e){
                System.out.println(e);
            }
            

            BlockLists TestList = BBMain.parse(rootNode);
            System.out.println(TestList);
        }

}
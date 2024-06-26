package ast.parser;

import ast.datatypes.Node;
import ast.datatypes.NodeTypesEnum;
import ast.lexer.Lexer;
import ast.lexer.Scan;
import ast.lexer.Token;
import org.junit.Assert;
import org.junit.Test;

import java.io.IOException;
import java.util.List;

import static org.junit.Assert.assertEquals;

public class ParserTest {

    /**
     * Tests a nested binary Expression
     */
    @Test
    public void testBinExp() {
        Scan scanner = new Scan("i = ((3*4)+2)");
        List<Token> tokens = scanner.scanTokens();
        Parser parser = new Parser(tokens);
        Node testNode = parser.parse(tokens);
        Node program = testNode.getBody().get(0);
        assertEquals(NodeTypesEnum.BINARY_EXPRESSION, program.getType());
        assertEquals("i", program.getLeft().getValue());
        assertEquals("=", program.getOperator());
        Node binExp1 = program.getRight().getLeft();
        assertEquals(NodeTypesEnum.BINARY_EXPRESSION, binExp1.getType());
        assertEquals("3", binExp1.getLeft().getValue());
        assertEquals("*", binExp1.getOperator());
        assertEquals("4", binExp1.getRight().getValue());
        Node binExp2 = program.getRight();
        assertEquals("+", binExp2.getOperator());
        assertEquals("2", binExp2.getRight().getValue());
    }

    /**
     * Tests an if, else if, else construct
     */
    @Test
    public void testIf() {
        Scan scanner = new Scan("if(3 > 4) { x = 3; y = 4; } else if (a > d) {y = (x + y) } else { x = (x + 4) }");
        List<Token> tokens = scanner.scanTokens();
        Parser parser = new Parser(tokens);
        Node program = parser.parse(tokens).getBody().get(0);
        assertEquals(NodeTypesEnum.IF_STATEMENT, program.getType());
        // test if condition
        Node ifCondtion = program.getCondition();
        assertEquals(NodeTypesEnum.BINARY_EXPRESSION, ifCondtion.getType());
        assertEquals("3", ifCondtion.getLeft().getValue());
        assertEquals(">", ifCondtion.getOperator());
        assertEquals("4", ifCondtion.getRight().getValue());
        // test if body
        Node ifBody1 = program.getBody().get(0);
        Node ifBody2 = program.getBody().get(1);
        assertEquals(NodeTypesEnum.BINARY_EXPRESSION, ifBody1.getType());
        assertEquals("x", ifBody1.getLeft().getValue());
        assertEquals("=", ifBody1.getOperator());
        assertEquals("3", ifBody1.getRight().getValue());
        assertEquals(NodeTypesEnum.BINARY_EXPRESSION, ifBody2.getType());
        assertEquals("y", ifBody2.getLeft().getValue());
        assertEquals("=", ifBody2.getOperator());
        assertEquals("4", ifBody2.getRight().getValue());
        // test else if condition
        Node elseIf = program.getAlternative().get(0);
        assertEquals(NodeTypesEnum.ELSE_STATEMENT, elseIf.getType());
        Node elseIfCondtion = elseIf.getCondition();
        assertEquals(NodeTypesEnum.BINARY_EXPRESSION, elseIfCondtion.getType());
        assertEquals("a", elseIfCondtion.getLeft().getValue());
        assertEquals(">", elseIfCondtion.getOperator());
        assertEquals("d", elseIfCondtion.getRight().getValue());
        // test else if body
        Node elseIfBody = elseIf.getBody().get(0);
        assertEquals(NodeTypesEnum.BINARY_EXPRESSION, elseIfBody.getType());
        assertEquals("y", elseIfBody.getLeft().getValue());
        assertEquals("=", elseIfBody.getOperator());
        Node binExp = elseIfBody.getRight();
        assertEquals(NodeTypesEnum.BINARY_EXPRESSION, binExp.getType());
        assertEquals("x", binExp.getLeft().getValue());
        assertEquals("+", binExp.getOperator());
        assertEquals("y", binExp.getRight().getValue());
        // test else
        Node else1 = program.getAlternative().get(1);
        assertEquals(NodeTypesEnum.ELSE_STATEMENT, else1.getType());
        Node else1Body = else1.getBody().get(0);
        assertEquals(NodeTypesEnum.BINARY_EXPRESSION, else1Body.getType());
        assertEquals("x", else1Body.getLeft().getValue());
        assertEquals("=", else1Body.getOperator());
        Node binExp2 = else1Body.getRight();
        assertEquals(NodeTypesEnum.BINARY_EXPRESSION, binExp.getType());
        assertEquals("x", binExp2.getLeft().getValue());
        assertEquals("+", binExp2.getOperator());
        assertEquals("4", binExp2.getRight().getValue());
    }

    /**
     * Tests the basic increment i++;
     */
    @Test
    public void testInc() {
        Scan scanner = new Scan("i++;"); // i = (i + 1);
        List<Token> tokens = scanner.scanTokens();
        Parser parser = new Parser(tokens);
        Node program = parser.parse(tokens).getBody().get(0);
        assertEquals(NodeTypesEnum.BINARY_EXPRESSION, program.getType());
        assertEquals("i", program.getLeft().getValue());
        assertEquals("=", program.getOperator());
        Node binExp = program.getRight();
        assertEquals(NodeTypesEnum.BINARY_EXPRESSION, binExp.getType());
        assertEquals("i", binExp.getLeft().getValue());
        assertEquals("+", binExp.getOperator());
        assertEquals("1", binExp.getRight().getValue());
    }

    /**
     * Tests constructs like += and -=
     */
    @Test
    public void testOpEqual() {
        Scan scanner = new Scan("x -= y ;"); // x = (x - y);
        List<Token> tokens = scanner.scanTokens();
        Parser parser = new Parser(tokens);
        Node program = parser.parse(tokens).getBody().get(0);
        assertEquals(NodeTypesEnum.BINARY_EXPRESSION, program.getType());
        assertEquals("x", program.getLeft().getValue());
        assertEquals("=", program.getOperator());
        Node binExp = program.getRight();
        assertEquals(NodeTypesEnum.BINARY_EXPRESSION, binExp.getType());
        assertEquals("x", binExp.getLeft().getValue());
        assertEquals("-", binExp.getOperator());
        assertEquals("y", binExp.getRight().getValue());
    }

    /**
     * Tests a for-loop
     */
    @Test
    public void testFor() {
        Scan scanner = new Scan("for(int i = 0; i < 3; i--) { y = (4 + 1); }");
        List<Token> tokens = scanner.scanTokens();
        Parser parser = new Parser(tokens);
        Node program = parser.parse(tokens).getBody().get(0);
        assertEquals(NodeTypesEnum.WHILE_STATEMENT, program.getType());
        // test init
        Node init = program.getLeft();
        assertEquals(NodeTypesEnum.DECLARATION, init.getType());
        assertEquals("i", init.getValue());
        //assertEquals("int", init.getOperator()); // TODO emtpy but should be set
        Node initCon = init.getCondition();
        assertEquals(NodeTypesEnum.BINARY_EXPRESSION, initCon.getType());
        assertEquals("i", initCon.getLeft().getValue());
        assertEquals("=", initCon.getOperator());
        assertEquals("0", initCon.getRight().getValue());
        // test condition
        Node condtion = program.getCondition();
        assertEquals(NodeTypesEnum.BINARY_EXPRESSION, condtion.getType());
        // test increment
        Node inc = program.getRight();
        assertEquals(NodeTypesEnum.BINARY_EXPRESSION, inc.getType());
        assertEquals("i", inc.getLeft().getValue());
        assertEquals("=", inc.getOperator());
        Node binExp = inc.getRight();
        assertEquals(NodeTypesEnum.BINARY_EXPRESSION, binExp.getType());
        assertEquals("i", binExp.getLeft().getValue());
        assertEquals("-", binExp.getOperator());
        assertEquals("1", binExp.getRight().getValue());
        // test for body
        Node loopBody = program.getBody().get(0);
        assertEquals(NodeTypesEnum.BINARY_EXPRESSION, loopBody.getType());
        assertEquals("y", loopBody.getLeft().getValue());
        assertEquals("=", loopBody.getOperator());
        Node binExp1 = loopBody.getRight();
        assertEquals(NodeTypesEnum.BINARY_EXPRESSION, binExp1.getType());
        assertEquals("4", binExp1.getLeft().getValue());
        assertEquals("+", binExp1.getOperator());
        assertEquals("1", binExp1.getRight().getValue());
    }

    /**
     * Tests a while loop
     */
    @Test
    public void testWhile() {
        Scan scanner = new Scan("while(i < 3) { x = 3; }");
        List<Token> tokens = scanner.scanTokens();
        Parser parser = new Parser(tokens);
        Node program = parser.parse(tokens).getBody().get(0);
        assertEquals(NodeTypesEnum.WHILE_STATEMENT, program.getType());
        Node condition = program.getCondition();
        assertEquals(NodeTypesEnum.BINARY_EXPRESSION, condition.getType());
        assertEquals("i", condition.getLeft().getValue());
        assertEquals("<", condition.getOperator());
        assertEquals("3", condition.getRight().getValue());
        Node loopBody = program.getBody().get(0);
        assertEquals(NodeTypesEnum.BINARY_EXPRESSION, loopBody.getType());
        assertEquals("x", loopBody.getLeft().getValue());
        assertEquals("=", loopBody.getOperator());
        assertEquals("3", loopBody.getRight().getValue());
    }

    /**
     * Tests a function call
     */
    @Test
    public void testFunCall() {
        Scan scanner = new Scan("add(x,6);");
        List<Token> tokens = scanner.scanTokens();
        Parser parser = new Parser(tokens);
        Node program = parser.parse(tokens).getBody().get(0);
        assertEquals(NodeTypesEnum.FUNCTION_CALL, program.getType());
        assertEquals("add", program.getValue());
        Node param1 = program.getAlternative().get(0);
        assertEquals(NodeTypesEnum.IDENTIFIER, param1.getType());
        assertEquals("x", param1.getValue());
        Node param2 = program.getAlternative().get(1);
        assertEquals(NodeTypesEnum.LITERAL, param2.getType());
        assertEquals("6", param2.getValue());
    }


    /**
     * Tests the return statement
     */
    @Test
    public void testReturn() {
        Scan scanner = new Scan("return x(5);");
        List<Token> tokens = scanner.scanTokens();
        Parser parser = new Parser(tokens);
        Node program = parser.parse(tokens).getBody().get(0);
        assertEquals(NodeTypesEnum.RETURN_STATEMENT, program.getType());
        Node value = program.getCondition();
        assertEquals(NodeTypesEnum.FUNCTION_CALL, value.getType());
        assertEquals("x", value.getValue());
        assertEquals("5", value.getAlternative().get(0).getValue());
    }

    /**
     * Tests a function definition
     */
    @Test
    public void testFunDef() {
        Scan scanner = new Scan("int sum(int a, int b) {return (a + b); }");
        List<Token> tokens = scanner.scanTokens();
        Parser parser = new Parser(tokens);
        Node program = parser.parse(tokens).getBody().get(0);
        assertEquals(NodeTypesEnum.FUNCTION_DEF, program.getType());
        // test function def
        assertEquals("sum", program.getValue());
        assertEquals("int", program.getOperator());
        Node param1 = program.getAlternative().get(0);
        assertEquals(NodeTypesEnum.IDENTIFIER, param1.getType());
        assertEquals("a", param1.getValue());
        assertEquals("int", param1.getOperator());
        Node param2 = program.getAlternative().get(1);
        assertEquals(NodeTypesEnum.IDENTIFIER, param2.getType());
        assertEquals("b", param2.getValue());
        assertEquals("int", param2.getOperator());
        // test function body
        Node body = program.getBody().get(0);
        assertEquals(NodeTypesEnum.RETURN_STATEMENT, body.getType());
        Node value = body.getCondition();
        assertEquals(NodeTypesEnum.BINARY_EXPRESSION, value.getType());
        assertEquals("a", value.getLeft().getValue());
        assertEquals("+", value.getOperator());
        assertEquals("b", value.getRight().getValue());
    }

    /**
     * Tests a switch case construct
     */
    @Test
    public void testSwitchCase() {
        Scan scanner = new Scan("switch (days) { case 1: add(x,y); break; case 2: y=3; break; default: x+3; } ");
        List<Token> tokens = scanner.scanTokens();
        Parser parser = new Parser(tokens);
        Node switchh = parser.parse(tokens).getBody().get(0);
        assertEquals(NodeTypesEnum.IF_STATEMENT, switchh.getType());
        // assert If statement (1st case)
        Node case1Condition = switchh.getCondition();
        assertEquals(NodeTypesEnum.BINARY_EXPRESSION, case1Condition.getType());
        assertEquals("days", case1Condition.getLeft().getValue());
        assertEquals("==", case1Condition.getOperator());
        assertEquals("1", case1Condition.getRight().getValue());
        Node case1Body = switchh.getBody().get(0);
        assertEquals(NodeTypesEnum.FUNCTION_CALL, case1Body.getType());
        assertEquals("add", case1Body.getValue());
        assertEquals("x", case1Body.getAlternative().get(0).getValue());
        assertEquals("y", case1Body.getAlternative().get(1).getValue());
        // assert second case
        Node case2Condition = switchh.getAlternative().get(0).getCondition();
        assertEquals(NodeTypesEnum.BINARY_EXPRESSION, case2Condition.getType());
        assertEquals("days", case2Condition.getLeft().getValue());
        assertEquals("==", case2Condition.getOperator());
        assertEquals("2", case2Condition.getRight().getValue());
        Node case2Body = switchh.getAlternative().get(0).getBody().get(0);
        assertEquals(NodeTypesEnum.BINARY_EXPRESSION, case2Body.getType());
        assertEquals("y", case2Body.getLeft().getValue());
        assertEquals("=", case2Body.getOperator());
        assertEquals("3", case2Body.getRight().getValue());
        // assert default
        Node case3Body = switchh.getAlternative().get(1).getBody().get(0);
        assertEquals(NodeTypesEnum.BINARY_EXPRESSION, case3Body.getType());
        assertEquals("x", case3Body.getLeft().getValue());
        assertEquals("+", case3Body.getOperator());
        assertEquals("3", case3Body.getRight().getValue());
    }


    /**
     * Tests #include
     */
    @Test
    public void testInclude() {
        Scan scanner = new Scan("#include \"hello.c\"; while(i < 3) { x = 3; } int sum(int a, int b) {return (a + b); }");
        //Scan scanner = new Scan("#include \"D:\\git\\C2Piet-Compiler\\test\\ressources\\hello.c\"; while(i < 3) { x = 3; } int sum(int a, int b) {return (a + b); }");
        List<Token> tokens = scanner.scanTokens();
        Parser parser = new Parser(tokens);
        Node testNode = parser.parse(tokens);
        // check that the first node is the included function definition
        Node inclFunDef = testNode.getBody().get(0);
        assertEquals(inclFunDef.getType(), NodeTypesEnum.FUNCTION_DEF);
        assertEquals(inclFunDef.getBody().size(), 6);
        // assert that the rest follows
        Node whileNode = testNode.getBody().get(1);
        assertEquals(whileNode.getType(), NodeTypesEnum.WHILE_STATEMENT);
        Node funDef = testNode.getBody().get(2);
        assertEquals(funDef.getType(), NodeTypesEnum.FUNCTION_DEF);
        assertEquals(funDef.getBody().size(), 1);
    }


    /**
     * Tests constants definition
     */
    @Test
    public void testDefine() {
        Scan scanner = new Scan("#define MAX 3 #define MIN 4");
        List<Token> tokens = scanner.scanTokens();
        Parser parser = new Parser(tokens);
        Node testNode = parser.parse(tokens);
        Node node1 = testNode.getBody().get(0);
        assertEquals(NodeTypesEnum.BINARY_EXPRESSION, node1.getType());
        assertEquals("MAX", node1.getLeft().getValue());
        assertEquals("=", node1.getOperator());
        assertEquals("3", node1.getRight().getValue());
        Node node2 = testNode.getBody().get(1);
        assertEquals(NodeTypesEnum.BINARY_EXPRESSION, node2.getType());
        assertEquals("MIN", node2.getLeft().getValue());
        assertEquals("=", node2.getOperator());
        assertEquals("4", node2.getRight().getValue());
    }

    /**
     * Test that the unary operator ! is handled correctly in front of
     * 1. variables, 2. conditions, 3. functionCalls
     */
    @Test
    public void testHandleNot() {
        // test not variable
        Node notVariable = Lexer.run("if(!var1) {x = 3;}").getBody().get(0);
        assertEquals(notVariable.getType(), NodeTypesEnum.IF_STATEMENT);
        assertEquals(notVariable.getCondition().getLeft().getOperator(), "!");
        assertEquals(notVariable.getCondition().getLeft().getType(), NodeTypesEnum.IDENTIFIER);
        assertEquals(notVariable.getCondition().getLeft().getValue(), "var1");
        // test not condition
        Node notCondition = Lexer.run("if(!(var1 && var2)) {y = 4;}").getBody().get(0);
        System.out.println();
        assertEquals(notCondition.getType(), NodeTypesEnum.IF_STATEMENT);
        Node notCondi = notCondition.getCondition();
        assertEquals(notCondi.getOperator(), "!");
        Node nestedCon = notCondi.getLeft();
        assertEquals(nestedCon.getType(), NodeTypesEnum.BINARY_EXPRESSION);
        assertEquals(nestedCon.getLeft().getValue(), "var1");
        assertEquals(nestedCon.getOperator(), "&&");
        assertEquals(nestedCon.getRight().getValue(), "var2");
    }


    /**
     * inspections for now
     */
    @Test
    public void testInspection() throws IOException {
        Node testNode = Lexer.run("int diffToTen(int x) { if (x<= 10) { return 0; } int y = 0; while (x <= 10) { y++;} return y;}");
        Node testNode2 = Lexer.run("i = add(4, y);");

        Assert.assertTrue(true);
    }
}

// TODO enums
// TODO bool not for variables only
// TODO enable stuff like (fun1(x) > fun2(y))  (just do y = fun1(x) -> (y >...))
// TODO not for expressions like (!(a && b) && a)
// TODO (precedence)
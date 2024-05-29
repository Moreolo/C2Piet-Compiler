package ast.parser;

import ast.datatypes.Node;
import ast.datatypes.NodeTypesEnum;
import ast.lexer.Scan;
import ast.lexer.Token;
import org.junit.Assert;
import org.junit.Test;

import java.util.List;

import static org.junit.Assert.assertEquals;

public class ParserTest {

    /**
     * Tests a nested binary Expression
     */
    @Test
    public void testBinExp() {
        Scan scanner = new Scan("i = (3+4)");
        List<Token> tokens = scanner.scanTokens();
        Parser parser = new Parser(tokens);
        Node testNode = parser.parse(tokens);
        Node program =  testNode.getBody().get(0);
        assertEquals(NodeTypesEnum.BINARY_EXPRESSION, program.getType());
        assertEquals("i", program.getLeft().getValue());
        assertEquals("=", program.getOperator());
        Node binExp  = program.getRight();
        assertEquals(NodeTypesEnum.BINARY_EXPRESSION, binExp.getType());
        assertEquals("3", binExp.getLeft().getValue());
        assertEquals("+", binExp.getOperator());
        assertEquals("4", binExp.getRight().getValue());
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
        Node ifCondtion  = program.getCondition();
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
        Node binExp  = elseIfBody.getRight();
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
        Node binExp2  = else1Body.getRight();
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
        Node binExp  = program.getRight();
        assertEquals(NodeTypesEnum.BINARY_EXPRESSION, binExp.getType());
        assertEquals("i", binExp.getLeft().getValue());
        assertEquals("+", binExp.getOperator());
        assertEquals("1", binExp.getRight().getValue());
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
        Node condtion  = program.getCondition();
        assertEquals(NodeTypesEnum.BINARY_EXPRESSION, condtion.getType());
        // test increment
        Node inc = program.getRight();
        assertEquals(NodeTypesEnum.BINARY_EXPRESSION, inc.getType());
        assertEquals("i", inc.getLeft().getValue());
        assertEquals("=", inc.getOperator());
        Node binExp  = inc.getRight();
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
        Scan scanner = new Scan("add(5,6);");
        List<Token> tokens = scanner.scanTokens();
        Parser parser = new Parser(tokens);
        Node program = parser.parse(tokens).getBody().get(0);
        assertEquals(NodeTypesEnum.FUNCTION_CALL, program.getType());
        assertEquals("add", program.getValue());
        Node param1 = program.getAlternative().get(0);
        assertEquals(NodeTypesEnum.LITERAL, param1.getType());
        assertEquals("5", param1.getValue());
        Node param2 = program.getAlternative().get(1);
        assertEquals(NodeTypesEnum.LITERAL, param1.getType());
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
        assertEquals(NodeTypesEnum.LITERAL, param1.getType());
        assertEquals("a", param1.getValue());
        assertEquals("int", param1.getOperator());
        Node param2 = program.getAlternative().get(1);
        assertEquals(NodeTypesEnum.LITERAL, param2.getType());
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
     * just for inspections for now
     */
    @Test
    public void testProgram() {
        Scan scanner = new Scan("int main(int a, int b) {return (a + b); }");
        List<Token> tokens = scanner.scanTokens();
        Parser parser = new Parser(tokens);
        Node testNode = parser.parse(tokens);
        

        Assert.assertTrue(true);
    }

}

// TODO next: switch
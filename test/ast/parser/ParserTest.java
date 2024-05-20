package ast.parser;

import ast.datatypes.Node;
import ast.lexer.Scan;
import ast.lexer.Token;
import org.junit.Assert;
import org.junit.Test;

import java.util.List;

public class ParserTest {

    /**
     * just for inspections for now
     */
    @Test
    public void testBinExp() {
        Scan scanner = new Scan("i = (3+4)");
        List<Token> tokens = scanner.scanTokens();
        Parser parser = new Parser(tokens);
        Node testNode = parser.parse(tokens);

        Assert.assertTrue(true);
    }

    /**
     * just for inspections for now
     */
    @Test
    public void testIf() {
        Scan scanner = new Scan("if(3 > 4) { x = 3; y = 4; } else if (a > d) {x + 3} else{ x +4 }");
        List<Token> tokens = scanner.scanTokens();
        Parser parser = new Parser(tokens);
        Node testNode = parser.parse(tokens);

        Assert.assertTrue(true);
    }

    /**
     * just for inspections for now
     */
    @Test
    public void testInc() {
        Scan scanner = new Scan("i++;");
        List<Token> tokens = scanner.scanTokens();
        Parser parser = new Parser(tokens);
        Node testNode = parser.parse(tokens);

        Assert.assertTrue(true);
    }

    /**
     * just for inspections for now
     */
    @Test
    public void testFor() {
        Scan scanner = new Scan("for(int i = 0; i < 3; i++) { x = 3; y = (4 + 1); }");
        List<Token> tokens = scanner.scanTokens();
        Parser parser = new Parser(tokens);
        Node testNode = parser.parse(tokens);

        Assert.assertTrue(true);
    }

    /**
     * just for inspections for now
     */
    @Test
    public void testWhile() {
        Scan scanner = new Scan("while(i < 3) { x = 3; y = (4 + 1); }");
        List<Token> tokens = scanner.scanTokens();
        Parser parser = new Parser(tokens);
        Node testNode = parser.parse(tokens);

        Assert.assertTrue(true);
    }

    /**
     * just for inspections for now
     */
    @Test
    public void testFunCall() {
        Scan scanner = new Scan("x(5,6);");
        List<Token> tokens = scanner.scanTokens();
        Parser parser = new Parser(tokens);
        Node testNode = parser.parse(tokens);

        Assert.assertTrue(true);
    }


    /**
     * just for inspections for now
     */
    @Test
    public void testReturn() {
        Scan scanner = new Scan("return x(5);");
        List<Token> tokens = scanner.scanTokens();
        Parser parser = new Parser(tokens);
        Node testNode = parser.parse(tokens);

        Assert.assertTrue(true);
    }

    /**
     * just for inspections for now
     */
    @Test
    public void testFunDef() {
        Scan scanner = new Scan("int sum(int a, int b) {return (a + b); }");
        List<Token> tokens = scanner.scanTokens();
        Parser parser = new Parser(tokens);
        Node testNode = parser.parse(tokens);

        Assert.assertTrue(true);
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
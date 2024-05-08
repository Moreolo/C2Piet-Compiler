package ast.parser;

import ast.datatypes.Node;
import ast.datatypes.NodeTypesEnum;
import ast.lexer.Scan;
import ast.lexer.Token;
import org.junit.Assert;
import org.junit.Test;
import java.io.File;
import java.io.FileWriter;
import java.util.List;

public class parserTest {

    public String getTestCodeFile(final String code) throws Exception {
        String fileName = "testCFile.txt";
        File file = new File(fileName);
        FileWriter writer = new FileWriter(file);
        writer.write(code);
        return file.getAbsolutePath();
    }

    /**
     * just for inspections for now
     * @throws Exception
     */
    @Test
    public void testBinExp() throws Exception {
        Scan scanner = new Scan("(3+4) > ((4*5)+4)");
        List<Token> tokens = scanner.scanTokens();
        Parser parser = new Parser(tokens);
        Node testNode = parser.handleBinaryExp(new Node(NodeTypesEnum.BINARY_EXPRESSION));

        Assert.assertTrue(true);
    }

    /**
     * just for inspections for now
     * @throws Exception
     */
    @Test
    public void testIf() throws Exception {
        Scan scanner = new Scan("if(3 > 4) { }");
        List<Token> tokens = scanner.scanTokens();
        Parser parser = new Parser(tokens);
        Node testNode = parser.parse(tokens);

        Assert.assertTrue(true);
    }

}

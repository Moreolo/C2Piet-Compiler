package ast.lexer;

import ast.datatypes.Node;
import ast.parser.Parser;
import java.io.IOException;
import java.nio.charset.Charset;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.List;

public class Lexer {

    public Lexer() {
    }

    public static Node runFile(String path) throws IOException {
        byte[] bytes = Files.readAllBytes(Paths.get(path));
        return run(new String(bytes, Charset.defaultCharset()));
    }

    public static Node run(String source) {
        Scan scanner = new Scan(source);
        List<Token> tokens = scanner.scanTokens();
        Parser parser = new Parser(tokens);
        return parser.parse(tokens);
    }

}
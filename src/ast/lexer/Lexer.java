package ast.lexer;

//import java.io.BufferedReader;

import ast.datatypes.Node;
import ast.parser.Parser;

import java.io.IOException;
//import java.io.InputStreamReader;
import java.nio.charset.Charset;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.List;


public class Lexer {

    static boolean hadError = false;

    public Lexer() {
    }


    public static Node runFile(String path) throws IOException {
        byte[] bytes = Files.readAllBytes(Paths.get(path));
        Node res = run(new String(bytes, Charset.defaultCharset()));
        if (hadError) {
            System.exit(65);
        }
        return res;
    }

    public static Node run(String source) {
        Scan scanner = new Scan(source);
        List<Token> tokens = scanner.scanTokens();
        Parser parser = new Parser(tokens);
        return parser.parse(tokens);
    }

    static void error(int line, String message) {
        report(line, "", message);
    }

    private static void report(int line, String where, String message) {
        System.err.println(
                "[line " + line + "] Error" + where + ": " + message);
        hadError = true;
    }
}
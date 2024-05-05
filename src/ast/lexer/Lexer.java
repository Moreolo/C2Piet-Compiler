package ast.lexer;

//import java.io.BufferedReader;
import ast.parser.Parser;
import java.io.IOException;
//import java.io.InputStreamReader;
import java.nio.charset.Charset;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.List;


public class Lexer {
	
	static boolean hadError = false;
	
	public Lexer(String path)throws IOException {
		
		runFile(path);
	}


private static void runFile(String path) throws IOException {
    byte[] bytes = Files.readAllBytes(Paths.get(path));
    run(new String(bytes, Charset.defaultCharset()));
	  if (hadError) {
      System.exit(65);
    }
  }
  
private static void run(String source) {
    Scan scanner = new Scan(source);
    List<Token> tokens = scanner.scanTokens();

    // For now, just print the tokens.
    for (Token token : tokens) {
      System.out.println(token);
    }

    Parser parser = new Parser();
    parser.parse(tokens);
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
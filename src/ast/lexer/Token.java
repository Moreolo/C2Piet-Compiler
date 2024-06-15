package ast.lexer;

public class Token {
    private final TokenType type;
    private final String lexeme;
    private final Object literal;
    private final int line;
  
    public Token(TokenType type, String lexeme, Object literal, int line) {
      this.type = type;
      this.lexeme = lexeme;
      this.literal = literal;
      this.line = line;
    }

    public TokenType getType() {
        return this.type;
    }

    public String getLexeme() {
        return this.lexeme;
    }

    public Object getLiteral() {
        return this.literal;
    }

    public int getLine() {
        return this.line;
    }
  
    public String toString() {
      return type + " " + lexeme + " " + literal + " "+ line;
    }


  }

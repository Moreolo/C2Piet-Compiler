package ast.lexer;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static ast.lexer.TokenType.*;

public class Scan{
    private  String source = "";
    private final List<Token> tokens = new ArrayList<>();
    private int start = 0;
    private int current = 0;
    private int line = 1;
    private static final Map<String, TokenType> keywords;
    
    static {
      
      keywords = new HashMap<>();
      keywords.put("if",     IF);
      keywords.put("else",   ELSE);
      keywords.put("for",    FOR);
      keywords.put("do",    DO);
      keywords.put("while",  WHILE);
      keywords.put("goto",    GOTO);
      keywords.put("switch",  SWITCH);
      keywords.put("case", CASE);
      keywords.put("break",  BREAK);
      keywords.put("static",   STATIC);
      keywords.put("extern",   EXTERN);
      keywords.put("void",    VOID);
      keywords.put("const",    CONST);
      keywords.put("return",    RETURN);
      keywords.put("int",    INT);
      keywords.put("float",    FLOAT);
      keywords.put("double",    DOUBLE);
      keywords.put("long",    LONG);
      keywords.put("short",    SHORT);
      
    }
  
    public Scan(String source) {
      this.source = source;
    }

    public List<Token> scanTokens() {
        while (!isAtEnd()) {
          // We are at the beginning of the next lexeme.
          start = current;
          scanToken();
        }
    
        tokens.add(new Token(EOF, "", null, line));
        return tokens;
    }

    private boolean isAtEnd() {
        return current >= source.length();
    }

    private char advance() {
        return source.charAt(current++);
      }
    
      private void addToken(TokenType type) {
        addToken(type, null);
      }
    
      private void addToken(TokenType type, Object literal) {
        String text = source.substring(start, current);
        tokens.add(new Token(type, text, literal, line));
      }

    private void scanToken() {
        char c = advance();
        switch (c) {
          case '(': addToken(LEFT_PAREN); break;
          case ')': addToken(RIGHT_PAREN); break;
          case '{': addToken(LEFT_BRACE); break;
          case '}': addToken(RIGHT_BRACE); break;
          case ',': addToken(COMMA); break;
          case '.': addToken(DOT); break;
          case '-': addToken(match('-')? DECREMENT : MINUS); break;
          case '+': addToken(match('+')? INCREMENT : PLUS); break;
          case ';': addToken(SEMICOLON); break;
          case '*': addToken(STAR); break; 
          case '[': addToken(SQUARE_BRACE_LEFT); break;
          case ']': addToken(SQUARE_BRACE_RIGHT); break;
          case ':': addToken(COLON); break;
          case '~': addToken(TILDA); break;
          case '&': addToken(match('&')? LOGICAL_AND : AND); break;
          case '|': addToken(match('|')? LOGICAL_OR : PIPE); break;
          case '!': addToken(match('=') ? BANG_EQUAL : BANG); break;
          case '=': addToken(match('=') ? EQUAL_EQUAL : EQUAL); break;
          case '<': addToken(match('=') ? LESS_EQUAL : LESS); break;
          case '>': addToken(match('=') ? GREATER_EQUAL : GREATER); break;
          case '/':
            if (match('/')) {// Single Line Comments
              while (peek() != '\n' && !isAtEnd()){
              advance();
            }
            break;
            }else if (match('*')) { //Multi-Line Comments
              while (peek() != '*' || !isAtEnd()){
                if(peek() == '*' && source.charAt(current+3) == '/'){
                  current = current+2;
                  break;}
                else{
                  if(peek() == '\n'){
                    line++;
                  }
                  advance();
                }
              }
            } 
            else {
              addToken(SLASH);
              break;
            }
          case ' ':
          case '\r':
          case '\t': break;
          case '\n':
              line++;
              break;
          case '"': string(); break;
          default:
            if (isDigit(c)) {
              number();
            }else if (isAlpha(c)) {
              identifier();
            }
            else {Lexer.error(line, "Unexpected character.");}
              break;
          }
      }

      private boolean match(char expected) {
        if (isAtEnd()) return false;
        if (source.charAt(current) != expected) return false;
    
        current++;
        return true;
      }

      private char peek() {
        if (isAtEnd()) return '\0';
        return source.charAt(current);
      }

      private void string() {
        while (peek() != '"' && !isAtEnd()) {
          if (peek() == '\n') line++;
          advance();
        }
    
        if (isAtEnd()) {
          Lexer.error(line, "Unterminated string.");
          return;
        }
    
        // The closing ".
        advance();
    
        // Trim the surrounding quotes.
        String value = source.substring(start + 1, current - 1);
        addToken(STRING, value);
      }

      private boolean isDigit(char c) {
        return c >= '0' && c <= '9';
      } 

      private void number() {
        while (isDigit(peek())) advance();
    
        // Look for a fractional part.
        if (peek() == '.' && isDigit(peekNext())) {
          // Consume the "."
          advance();
    
          while (isDigit(peek())) advance();
        }
    
        addToken(NUMBER,
            Double.parseDouble(source.substring(start, current)));
      }

      private char peekNext() {
        if (current + 1 >= source.length()) return '\0';
        return source.charAt(current + 1);
      } 

      private void identifier() {
        while (isAlphaNumeric(peek())) advance();

        String text = source.substring(start, current);
        TokenType type = keywords.get(text);
        if (type == null) type = IDENTIFIER;
        addToken(type);
      }

      private boolean isAlpha(char c) {
        return (c >= 'a' && c <= 'z') ||
               (c >= 'A' && c <= 'Z') ||
                c == '_';
      }
    
      private boolean isAlphaNumeric(char c) {
        return isAlpha(c) || isDigit(c);
      }
}


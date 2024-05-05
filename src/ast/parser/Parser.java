package ast.parser;

import ast.datatypes.Node;
import ast.datatypes.NodeTypesEnum;
import ast.lexer.Token;
import ast.lexer.TokenType;

import java.util.List;

public class Parser {

    /**
     * The Token-Stream-List.
     * Will be cleared when iterating over.
     */
    public List<Token> tokens;

    public void parse(List<Token> tokens) {
        for (Token token : tokens) {
            switch (token.getType()) {
                case IF, SWITCH, CASE:
                    handleIf(new Node(NodeTypesEnum.IF_STATEMENT));
                    break;
                case FOR, DO, WHILE:
                    handleFor(new Node(NodeTypesEnum.WHILE_STATEMENT));
                    break;
                case RETURN:
                    break;
            }

        }
    }

    /**
     * Delete/consume the token on top of the list
     * @param tokenType the expected tokenType
     */
    public void consume(TokenType tokenType) {
        if(tokenType == tokens.get(0).getType()) {
            tokens.remove(0);
        }
        throw new RuntimeException("Unexpected token: " + tokenType);
    }

    /**
     * Look ahead at next token
     * @param type the type of the token
     * @return true if the next token is of supplied type
     */
    public boolean peek(TokenType type) {
        return tokens.get(1).getType() == type;
    }

    /**
     * @return the token on top of the list and delete it
     */
    public Token popToken() {
        Token res = tokens.get(0);
        tokens.remove(0);
        return res;
    }

    /**
     * Sets all necessary fields of the If-Node
     * @param node to set values of
     * @return fully initialized If-Node
     */
    public Node handleIf(Node node) {
        consume(TokenType.LEFT_PAREN);
        node.setCondition(handleBinaryExp(new Node(NodeTypesEnum.BINARY_EXPRESSION)));

        //...
        return handleBlock();
    }

    private boolean isOperator(TokenType type) {
        return type == TokenType.LESS || type == TokenType.LESS_EQUAL || type == TokenType.GREATER || type == TokenType.GREATER_EQUAL || type == TokenType.EQUAL_EQUAL
        || type == TokenType.PLUS || type == TokenType.MINUS || type == TokenType.EQUAL;
    }

    /**
     * Handles any binary expression, also takes care of nesting
     * @param node the node to attach the binExp to
     * @return the node with the binExp attached
     */
    public Node handleBinaryExp(Node node) {
        // if there is a nest on the left side
        if (peek(TokenType.LEFT_PAREN)) {
            consume(TokenType.LEFT_PAREN);
            node.setLeft(handleBinaryExp(new Node(NodeTypesEnum.BINARY_EXPRESSION)));
        } else if (!isOperator(tokens.get(0).getType())) {
            node.setLeft(new Node(NodeTypesEnum.LITERAL).setValue(popToken().getLexeme()));
        }

        node.setOperator(popToken().getLexeme());

        // if there is a nest on the right side
        if (peek(TokenType.LEFT_PAREN)) {
            consume(TokenType.LEFT_PAREN);
            node.setRight(handleBinaryExp(new Node(NodeTypesEnum.BINARY_EXPRESSION)));
        } else if (!isOperator(tokens.get(0).getType())){
            node.setRight(new Node(NodeTypesEnum.LITERAL).setValue(popToken().getLexeme()));
        }

        // end recursion if statement is closed
        if (peek(TokenType.RIGHT_PAREN)) {
            consume(TokenType.RIGHT_PAREN);
            return node;
        }

        return node;
    }

    public Node handleFor(Node node) {

        //...
        return handleBlock();
    }

    public Node handleBlock() {


        return new Node(NodeTypesEnum.BLOCK_STATEMENT);
    }



}

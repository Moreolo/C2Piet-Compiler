package ast.parser;

import ast.datatypes.Node;
import ast.datatypes.NodeTypesEnum;
import ast.lexer.Token;
import ast.lexer.TokenType;

import java.util.ArrayList;
import java.util.List;

public class Parser {

    /**
     * The Token-Stream-List.
     * Will be cleared when iterating over.
     */
    public List<Token> tokens;

    public Parser(List<Token> tokens) {
        this.tokens = tokens;
    }

    /**
     * Main functions for navigating the recursion
     * @param tokens the List of tokens
     * @return the entry node to the program
     */
    public Node parse(List<Token> tokens) {
        for (Token token : tokens) {
            switch (token.getType()) {
                case IF:
                    return handleIf(new Node(NodeTypesEnum.IF_STATEMENT));
                case FOR:
                    return handleFor(new Node(NodeTypesEnum.WHILE_STATEMENT));
                case STRING, INT, FLOAT, DOUBLE, SHORT, LONG:
                    return handleDeclaration(new Node(NodeTypesEnum.DECLARATION));
                case IDENTIFIER:
                    if(tokens.get(1).getType() != TokenType.LEFT_PAREN){
                        return handleBinaryExp(new Node(NodeTypesEnum.BINARY_EXPRESSION));
                    }
                    // else handleFunctionCall
                default:
                    // dummy
                    return new Node(NodeTypesEnum.PROGRAM);

            }
        }
        return null;
    }

    /**
     * Delete/consume the token on top of the list
     * @param tokenType the expected tokenType
     */
    public void consume(TokenType tokenType) {
        if(tokenType == tokens.get(0).getType()) {
            tokens.remove(0);
            return;
        }
        throw new RuntimeException("Expected token " + tokenType + " but was " + tokens.get(0).getType());
    }

    /**
     * Look ahead at next token
     * @param type the type of the token
     * @return true if the next token is of supplied type
     */
    public boolean peek(TokenType type) {
        return tokens.get(0).getType() == type;
    }

    /**
     * @return the token on top of the list and delete it
     */
    public Token popToken() {
        Token token = tokens.get(0);
        tokens.remove(0);
        return token;
    }

    /**
     * Sets all necessary fields of the If-Node
     * @param node to set values of
     * @return fully initialized If-Node
     */
    public Node handleIf(Node node) {
        consume(TokenType.IF);
        // handle condition
        consume(TokenType.LEFT_PAREN);
        node.setCondition(handleBinaryExp(new Node(NodeTypesEnum.BINARY_EXPRESSION)));
        //consume(TokenType.RIGHT_PAREN);
        consume(TokenType.LEFT_BRACE);
        node.setBody(handleBlock(new Node(NodeTypesEnum.BLOCK_STATEMENT)));
        consume(TokenType.RIGHT_BRACE);

        if (peek(TokenType.ELSE)){
            consume(TokenType.ELSE);
            consume(TokenType.LEFT_BRACE);
            node.setAlternative(handleBlock(new Node(NodeTypesEnum.BLOCK_STATEMENT)));
            consume(TokenType.RIGHT_BRACE);
        }

        return node;
    }

    /**
     * Check if a token is an operator
     * @param type the token to check
     * @return true if token is one of the following <, <=, >, >=, ==, +, -, =, *, /
     */
    private boolean isOperator(TokenType type) {
        return type == TokenType.LESS || type == TokenType.LESS_EQUAL || type == TokenType.GREATER || type == TokenType.GREATER_EQUAL || type == TokenType.EQUAL_EQUAL
        || type == TokenType.PLUS || type == TokenType.MINUS || type == TokenType.EQUAL || type == TokenType.STAR || type == TokenType.SLASH;
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
            if (tokens.get(1).getType() == TokenType.INCREMENT || tokens.get(1).getType() == TokenType.DECREMENT) {
                return handleCrement();
            }
            node.setLeft(handleBinaryExp(new Node(NodeTypesEnum.BINARY_EXPRESSION)));
        } else if (!isOperator(tokens.get(0).getType())) {
            if (tokens.get(1).getType() == TokenType.INCREMENT || tokens.get(1).getType() == TokenType.DECREMENT) {
                return handleCrement();
            }
            node.setLeft(new Node(NodeTypesEnum.LITERAL).setValue(popToken().getLexeme()));
        }

        if (isOperator(tokens.get(0).getType())) {
            node.setOperator(popToken().getLexeme());
        }

        // if there is a nest on the right side
        if (peek(TokenType.LEFT_PAREN)) {
            consume(TokenType.LEFT_PAREN);
            if (tokens.get(1).getType() == TokenType.INCREMENT || tokens.get(1).getType() == TokenType.DECREMENT) {
                return handleCrement();
            }
            node.setRight(handleBinaryExp(new Node(NodeTypesEnum.BINARY_EXPRESSION)));
        } else if (!isOperator(tokens.get(0).getType())){
            if (tokens.get(1).getType() == TokenType.INCREMENT || tokens.get(1).getType() == TokenType.DECREMENT) {
                return handleCrement();
            }
            node.setRight(new Node(NodeTypesEnum.LITERAL).setValue(popToken().getLexeme()));
        }

        // end recursion if statement is closed
        if (peek(TokenType.RIGHT_PAREN)) {
            consume(TokenType.RIGHT_PAREN);
        }

        if (peek(TokenType.SEMICOLON)) {
            consume(TokenType.SEMICOLON);
        }
        return node;
    }

    /**
     * Handle both increment and decrement like  i++;
     * @return Binary expression like i = i + 1;
     */
    public Node handleCrement() {
        Node resNode = new Node(NodeTypesEnum.BINARY_EXPRESSION);
        // for i++ the identifier would be i
        String identifier = popToken().getLexeme();
        Node identifierNode = new Node(NodeTypesEnum.IDENTIFIER, null, identifier, null, null, null, null, null);
        Node node1 = new Node(NodeTypesEnum.LITERAL, null, "1", null, null, null, null, null);

        resNode.setLeft(new Node(NodeTypesEnum.IDENTIFIER, null, identifier, null, null, null, null, null));
        resNode.setOperator("=");
        if (peek(TokenType.INCREMENT)) {
            resNode.setRight(new Node(NodeTypesEnum.BINARY_EXPRESSION, null, null, identifierNode, "+", node1, null, null));
            consume(TokenType.INCREMENT);
        } else {
            resNode.setRight(new Node(NodeTypesEnum.BINARY_EXPRESSION, null, null, identifierNode, "-", node1, null, null));
            consume(TokenType.DECREMENT);
        }
        if (peek(TokenType.RIGHT_PAREN)) {
            consume(TokenType.RIGHT_PAREN);
        }

        if (peek(TokenType.SEMICOLON)) {
            consume(TokenType.SEMICOLON);
        }
        return resNode;
    }

    /**
     * Handle For loops
     * @param node for the loop
     * @return a node with the first statement in left, second statement in condition and third statement in right
     */
    public Node handleFor(Node node) {
        consume(TokenType.FOR);
        consume(TokenType.LEFT_PAREN);
        // init loop variable
        node.setLeft(handleDeclaration(new Node(NodeTypesEnum.DECLARATION)));
        // condition
        node.setCondition(handleBinaryExp(new Node(NodeTypesEnum.BINARY_EXPRESSION)));
        // increment
        node.setRight(handleBinaryExp(new Node(NodeTypesEnum.BINARY_EXPRESSION)));
        // body
        consume(TokenType.LEFT_BRACE);
        node.setBody(handleBlock(new Node(NodeTypesEnum.BLOCK_STATEMENT)));
        consume(TokenType.RIGHT_BRACE);
        return node;
    }

    /**
     * Continue recursion through parse
     * @param node for the block
     * @return filled block node
     */
    public List<Node> handleBlock(Node node) {
        // check for curly brackets
        List<Node> res = new ArrayList<>();
        while (!peek(TokenType.RIGHT_BRACE)) {
            res.add(parse(tokens));
        }
        return res;
    }

    /**
     * Handle declarations like int x; and int y = 3 + 3;
     * @param node for the Declaration
     * @return node with the name as value (and initialization as condition)
     */
    public Node handleDeclaration(Node node) {
        // delete string, int, long...
        popToken();
        // store name of variable in value
        node.setValue(tokens.get(0).getLexeme());
        // if there is an init like (int i = 0;)
        // create a binExp
        if(tokens.get(1).getType() == TokenType.EQUAL) {
            node.setCondition(handleBinaryExp(new Node(NodeTypesEnum.BINARY_EXPRESSION)));
        }
       return node;
    }

}

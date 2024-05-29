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
     *
     * @param tokens the List of tokens
     * @return the entry node to the program
     */
    public Node parse(List<Token> tokens) {
        for (Token token : tokens) {
            switch (token.getType()) {
                case PROGRAM:
                    return handleProgram(new Node(NodeTypesEnum.PROGRAM));
                case IF:
                    return handleIf(new Node(NodeTypesEnum.IF_STATEMENT));
                case FOR:
                    return handleFor(new Node(NodeTypesEnum.WHILE_STATEMENT));
                case WHILE:
                    return handleWhile(new Node(NodeTypesEnum.WHILE_STATEMENT));
                case STRING, INT, FLOAT, DOUBLE, SHORT, LONG:
                    if (tokens.get(1).getType() == TokenType.IDENTIFIER) {
                        return handleFunctionDeclaration(new Node(NodeTypesEnum.FUNCTION_DEF));
                    } else {
                        return handleDeclaration(new Node(NodeTypesEnum.DECLARATION));
                    }
                case IDENTIFIER:
                    if (tokens.get(1).getType() == TokenType.LEFT_PAREN) {
                        return handleFunctionCall(new Node(NodeTypesEnum.FUNCTION_CALL));
                    } else {
                        return handleBinaryExp(new Node(NodeTypesEnum.BINARY_EXPRESSION));
                    }
                case RETURN:
                    return handleReturn(new Node(NodeTypesEnum.RETURN_STATEMENT));
                default:
                    // delete unknown tokens
                    popToken();
            }
        }
        return null;
    }

    /**
     * Delete/consume the token on top of the list
     *
     * @param tokenType the expected tokenType
     */
    public void consume(TokenType tokenType) {
        if (tokenType == tokens.get(0).getType()) {
            tokens.remove(0);
            return;
        }
        throw new RuntimeException("Expected token " + tokenType + " but was " + tokens.get(0).getType());
    }

    /**
     * Only consume a token if it is the expected type
     *
     * @param tokenType the expected tokenType
     */
    public void consumeErrorFree(TokenType tokenType) {
        if (tokenType == tokens.get(0).getType()) {
            tokens.remove(0);
        }
    }

    /**
     * Look ahead at next token
     *
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
     * Entry point -> EVERYTHING will be in the body
     *
     * @param node to fill
     * @return program node with everything in the body
     */
    public Node handleProgram(Node node) {
        consume(TokenType.PROGRAM);
        List<Node> res = new ArrayList<>();
        while (!peek(TokenType.EOF)) {
            res.add(parse(tokens));
        }
        res.add(new Node((NodeTypesEnum.TERMINATOR)));
        node.setBody(res);
        return node;
    }

    /**
     * Sets all necessary fields of the If-Node
     *
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
        node.setBody(handleBlock());
        consume(TokenType.RIGHT_BRACE);

        List<Node> res = new ArrayList<>();
        while (peek(TokenType.ELSE_IF)) {
            res.add(handleElse(new Node(NodeTypesEnum.ELSE_STATEMENT)));
        }

        if (peek(TokenType.ELSE)) {
            res.add(handleElse(new Node(NodeTypesEnum.ELSE_STATEMENT)));
        }
        node.setAlternative(res);

        return node;
    }

    private Node handleElse(Node node) {
        if (peek(TokenType.ELSE)) {
            consume(TokenType.ELSE);
            consume(TokenType.LEFT_BRACE);
            node.setBody(handleBlock());
            consume(TokenType.RIGHT_BRACE);
        } else {
            consume(TokenType.ELSE_IF);
            consume(TokenType.LEFT_PAREN);
            node.setCondition(handleBinaryExp(new Node(NodeTypesEnum.BINARY_EXPRESSION)));
            consume(TokenType.LEFT_BRACE);
            node.setBody(handleBlock());
            consume(TokenType.RIGHT_BRACE);
        }
        return node;
    }

    /**
     * Check if a token is an operator
     *
     * @param type the token to check
     * @return true if token is one of the following <, <=, >, >=, ==, +, -, =, *, /
     */
    private boolean isOperator(TokenType type) {
        return type == TokenType.LESS || type == TokenType.LESS_EQUAL || type == TokenType.GREATER || type == TokenType.GREATER_EQUAL || type == TokenType.EQUAL_EQUAL
                || type == TokenType.PLUS || type == TokenType.MINUS || type == TokenType.EQUAL || type == TokenType.STAR || type == TokenType.SLASH;
    }

    /**
     * Handles any binary expression, also takes care of nesting
     *
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
        } else if (!isOperator(tokens.get(0).getType())) {
            if (tokens.get(1).getType() == TokenType.INCREMENT || tokens.get(1).getType() == TokenType.DECREMENT) {
                return handleCrement();
            } else if (tokens.get(1).getType() == TokenType.LEFT_PAREN) {
                node.setRight(handleFunctionCall(new Node(NodeTypesEnum.FUNCTION_CALL)));
            } else {
                node.setRight(new Node(NodeTypesEnum.LITERAL).setValue(popToken().getLexeme()));
            }

        }
        // end recursion if statement is closed
        consumeErrorFree(TokenType.RIGHT_PAREN);
        consumeErrorFree(TokenType.SEMICOLON);
        return node;
    }

    /**
     * Handle For loops
     *
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
        node.setBody(handleBlock());
        consume(TokenType.RIGHT_BRACE);
        return node;
    }

    public Node handleWhile(Node node) {
        consume(TokenType.WHILE);
        consume(TokenType.LEFT_PAREN);
        node.setCondition(handleBinaryExp(new Node(NodeTypesEnum.BINARY_EXPRESSION)));
        consume(TokenType.LEFT_BRACE);
        node.setBody(handleBlock());
        consume(TokenType.RIGHT_BRACE);
        return node;
    }

    /**
     * Handle both increment and decrement like  i++;
     *
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
        consumeErrorFree(TokenType.RIGHT_PAREN);
        consumeErrorFree(TokenType.SEMICOLON);
        return resNode;
    }

    /**
     * Handle declarations like int x; and int y = 3 + 3;
     *
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
        if (tokens.get(1).getType() == TokenType.EQUAL) {
            node.setCondition(handleBinaryExp(new Node(NodeTypesEnum.BINARY_EXPRESSION)));
        }
        return node;
    }

    public Node handleFunctionDeclaration(Node node) {
        node.setOperator(popToken().getLexeme()); // return type
        node.setValue(popToken().getLexeme());  // function name
        List<Node> params = new ArrayList<>();
        consume(TokenType.LEFT_PAREN);
        while (!peek(TokenType.RIGHT_PAREN)) {
            // for each param, a node with param type in operator and value is name of param
            params.add(new Node(NodeTypesEnum.LITERAL, null, tokens.get(1).getLexeme(), null, tokens.get(0).getLexeme(), null, null, null));
            popToken();
            popToken(); // pop type and identifier
            consumeErrorFree(TokenType.COMMA);
        }
        node.setAlternative(params);
        consume(TokenType.RIGHT_PAREN);
        consume(TokenType.LEFT_BRACE);
        node.setBody(handleBlock());
        consume(TokenType.RIGHT_BRACE);
        return node;
    }


    /**
     * Handles function calls like add(2,3);
     *
     * @param node to fill
     * @return node with the block in the body and params in the alternative
     */
    public Node handleFunctionCall(Node node) {
        node.setValue(tokens.get(0).getLexeme());
        consume(TokenType.IDENTIFIER);
        consume(TokenType.LEFT_PAREN);
        List<Node> params = new ArrayList<>();
        while (!peek(TokenType.RIGHT_PAREN)) {
            params.add(new Node(NodeTypesEnum.LITERAL, null, tokens.get(0).getLexeme(), null, null, null, null, null));
            popToken(); // pop param
            consumeErrorFree(TokenType.COMMA);
        }
        node.setAlternative(params);
        popToken(); // pop semicolon or right brace
        return node;
    }

    /**
     * Handle return statements
     *
     * @param node to fill
     * @return a return node with the return value in condition
     */
    public Node handleReturn(Node node) {
        consume(TokenType.RETURN);
        consumeErrorFree(TokenType.LEFT_PAREN);
        node.setCondition(parse(tokens)); // for stuff like: return add(2,3) + 3;
        consumeErrorFree(TokenType.RIGHT_PAREN);
        return node;
    }

    /**
     * Continue recursion through parse
     *
     * @return filled block node
     */
    public List<Node> handleBlock() {
        // check for curly brackets
        List<Node> res = new ArrayList<>();
        while (!peek(TokenType.RIGHT_BRACE)) {
            res.add(parse(tokens));
        }
        return res;
    }

}
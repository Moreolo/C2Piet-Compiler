package ast.parser;

import ast.datatypes.Node;
import ast.datatypes.NodeTypesEnum;
import ast.lexer.Lexer;
import ast.lexer.Token;
import ast.lexer.TokenType;

import java.io.IOException;
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
        Node programmNode = new Node(NodeTypesEnum.PROGRAM);
        for (Token token : tokens) {
            switch (token.getType()) {
                case PROGRAM:
                    return handleProgram(programmNode);
                case HASHTAG:
                    if (tokens.get(1).getType() == TokenType.INCLUDE) {
                        programmNode.setBody(handleInclude());
                        return programmNode;
                    } else if (tokens.get(1).getType() == TokenType.DEFINE) {
                        return handleDefine();
                    }
                case IF:
                    return handleIf(new Node(NodeTypesEnum.IF_STATEMENT));
                case SWITCH:
                    return handleSwitch(new Node(NodeTypesEnum.IF_STATEMENT));
                case BREAK:
                    return null;
                case FOR:
                    return handleFor(new Node(NodeTypesEnum.WHILE_STATEMENT));
                case WHILE:
                    return handleWhile(new Node(NodeTypesEnum.WHILE_STATEMENT));
                case STRING, INT, FLOAT, DOUBLE, SHORT, LONG, VOID:
                    if (tokens.get(1).getType() == TokenType.IDENTIFIER && tokens.get(2).getType() == TokenType.LEFT_PAREN) {
                        return handleFunctionDeclaration(new Node(NodeTypesEnum.FUNCTION_DEF));
                    } else {
                        return handleDeclaration(new Node(NodeTypesEnum.DECLARATION));
                    }
                case IDENTIFIER:
                    if (tokens.get(1).getType() == TokenType.LEFT_PAREN) {
                        return handleFunctionCall(new Node(NodeTypesEnum.FUNCTION_CALL));
                    } else if (tokens.get(1).getType() == TokenType.PLUS_EQUAL || tokens.get(1).getType() == TokenType.MINUS_EQUAL) {
                        return handleOpEqual();
                    } else if (tokens.get(1).getType() == TokenType.SEMICOLON) {
                        return new Node(NodeTypesEnum.IDENTIFIER).setValue(popToken().getLexeme());
                    } else if (tokens.get(1).getType() == TokenType.EQUAL && !(peekAhead(TokenType.PLUS) || peekAhead(TokenType.MINUS) ||
                            peekAhead(TokenType.STAR) || peekAhead(TokenType.SLASH) || peekAhead(TokenType.MOD)) && peekAhead(TokenType.LEFT_PAREN)) {
                        Node temp = new Node(NodeTypesEnum.BINARY_EXPRESSION);
                        temp.setLeft(new Node(NodeTypesEnum.IDENTIFIER).setValue(popToken().getLexeme()));
                        temp.setOperator("=");
                        popToken();
                        temp.setRight((handleFunctionCall(new Node(NodeTypesEnum.FUNCTION_CALL))));
                        return temp;
                    } else {
                        return handleBinaryExp(new Node(NodeTypesEnum.BINARY_EXPRESSION));
                    }
                case RETURN:
                    return handleReturn(new Node(NodeTypesEnum.RETURN_STATEMENT));
                case LEFT_PAREN:
                    consumeErrorFree(TokenType.LEFT_PAREN);
                    return handleBinaryExp(new Node(NodeTypesEnum.BINARY_EXPRESSION));
                case NUMBER:
                    return new Node(NodeTypesEnum.LITERAL).setValue(popToken().getLexeme());
                default:
                    // delete unknown tokens
                    popToken();
                    return null;
            }
        }
        return programmNode;
    }

    /**
     * Delete/consume the token on top of the list
     *
     * @param tokenType the expected tokenType
     */
    private void consume(TokenType tokenType) {
        if (tokenType == tokens.get(0).getType()) {
            System.out.println("consumed: " + tokens.get(0));
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
    private void consumeErrorFree(TokenType tokenType) {
        if (tokenType == tokens.get(0).getType()) {
            System.out.println("consumed: " + tokens.get(0));
            tokens.remove(0);
        }
    }

    /**
     * Look ahead at next token
     *
     * @param type the type of the token
     * @return true if the next token is of supplied type
     */
    private boolean peek(TokenType type) {
        return tokens.get(0).getType() == type;
    }

    /**
     * checks for a certain type of token upto the next semicolon
     *
     * @param type type to look for
     * @return true if the token appears, false if not
     */
    private boolean peekAhead(TokenType type) {
        int i = 0;
        while(true){
            if (tokens.get(i).getType() == TokenType.SEMICOLON) {
                return false;
            } else if (tokens.get(i).getType() == type) {
                return true;
            }
            i++;
        }
    }

    /**
     * Pop the token on top of the stack
     *
     * @return the token on top of the list and delete it
     */
    private Token popToken() {
        Token token = tokens.get(0);
        System.out.println("consumed: " + tokens.get(0));
        tokens.remove(0);
        return token;
    }

    /**
     * Entry point -> EVERYTHING will be in the body
     *
     * @param node to fill
     * @return program node with everything in the body
     */
    private Node handleProgram(Node node) {
        consume(TokenType.PROGRAM);
        List<Node> res = new ArrayList<>();
        while (!peek(TokenType.EOF)) {
            Node curNode = parse(tokens);
            if (curNode != null) {
                // handle included files
                // -> extract their body and prepend to program node
                if (curNode.getType() == NodeTypesEnum.PROGRAM) {
                    List<Node> t = curNode.getBody();
                    // remove terminator
                    t.remove(t.size() - 1);
                    res.addAll(t);
                } else {
                    res.add(curNode);
                }
            }
        }
        res.add(new Node((NodeTypesEnum.TERMINATOR)));
        node.setBody(res);
        return node;
    }

    /**
     * Handle #include "file.c".
     * We can only handle locally stored c files.
     * The input from the file will be pasted into the original file
     *
     * @return a list of nodes (body of the parsed program node)
     */
    private List<Node> handleInclude() {
        consume(TokenType.HASHTAG);
        consume(TokenType.INCLUDE);
        String path = popToken().getLexeme();
        path = path.replace("\"", ""); // remove quotes
        try {
            Node include = Lexer.runFile(path);
            return include.getBody();
        } catch (final IOException e) {
            throw new RuntimeException("Unable to find file " + path + " to include");
        }
    }

    /**
     * Constant definitions like #define MAX 5.
     * Will simply be stored as variables.
     *
     * @return a BinEpx node with the value and name
     */
    private Node handleDefine() {
        consume(TokenType.HASHTAG);
        consume(TokenType.DEFINE);
        String name = popToken().getLexeme();
        // name of variable
        Node leftNode = new Node(NodeTypesEnum.IDENTIFIER, null, name, null, null, null, null, null);
        // value of variable
        Node rightNode = new Node(NodeTypesEnum.LITERAL, null, popToken().getLexeme(), null, null, null, null, null);
        return new Node(NodeTypesEnum.BINARY_EXPRESSION, null, null, leftNode, "=", rightNode, null, null);
    }


    /**
     * Sets all necessary fields of the If-Node
     *
     * @param node to set values of
     * @return fully initialized If-Node
     */
    private Node handleIf(Node node) {
        consume(TokenType.IF);
        // handle condition
        consume(TokenType.LEFT_PAREN);
        node.setCondition(handleBinaryExp(new Node(NodeTypesEnum.BINARY_EXPRESSION)));
        // check that the condition returns a boolean
        String conOp = node.getCondition().getOperator();
        if (!(conOp.equals(">") || conOp.equals("<") || conOp.equals(">=") || conOp.equals("<=") ||
                conOp.equals("!=") || conOp.equals("==") || conOp.equals("||") || conOp.equals("&&"))
                && node.getCondition().getRight() != null) {
            throw new RuntimeException("Not a boolean expression!");
        }
        consume(TokenType.LEFT_BRACE);
        node.setBody(handleBlock());
        consume(TokenType.RIGHT_BRACE);

        // handle (multiple) else (if) cases
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
        } else {
            consume(TokenType.ELSE_IF);
            consume(TokenType.LEFT_PAREN);
            node.setCondition(handleBinaryExp(new Node(NodeTypesEnum.BINARY_EXPRESSION)));
        }
        consume(TokenType.LEFT_BRACE);
        node.setBody(handleBlock());
        consume(TokenType.RIGHT_BRACE);
        return node;
    }

    /**
     * Handle a switch case construct, puts everything in an if-statement.
     * First case is in the if, remaining cases and default will be in the alternatives as Else-Nodes.
     *
     * @param node to fill
     * @return filled If_Statement node
     */
    private Node handleSwitch(Node node) {
        consume(TokenType.SWITCH);
        consume(TokenType.LEFT_PAREN);
        Object expression = popToken().getLexeme();
        consume(TokenType.RIGHT_PAREN);
        consume(TokenType.LEFT_BRACE);
        consume(TokenType.CASE);
        node.setCondition(handleSwitchBinaryExp(new Node(NodeTypesEnum.BINARY_EXPRESSION), expression));
        node.setBody(handleSwitchBody());
        List<Node> res = new ArrayList<>();
        while (!peek(TokenType.RIGHT_BRACE)) {
            if (peek(TokenType.DEFAULT)) {
                break;
            }
            consume(TokenType.CASE);
            Node temp = new Node(NodeTypesEnum.ELSE_STATEMENT);
            temp.setCondition(handleSwitchBinaryExp(new Node(NodeTypesEnum.BINARY_EXPRESSION), expression));
            temp.setBody(handleSwitchBody());
            res.add(temp);
        }
        if (peek(TokenType.DEFAULT)) {
            consume(TokenType.DEFAULT);
            consume(TokenType.COLON);
            res.add(handleDefault(new Node(NodeTypesEnum.ELSE_STATEMENT)));
        }
        node.setAlternative(res);
        consume(TokenType.RIGHT_BRACE);
        return node;
    }

    /**
     * Handle switch cases and transform them into comparison.
     *
     * @param node       to fill with binExp
     * @param expression for the switch declaration
     * @return binaryExp with expression == case
     */
    private Node handleSwitchBinaryExp(Node node, Object expression) {
        node.setLeft(new Node(NodeTypesEnum.IDENTIFIER, null, String.valueOf(expression), null, null, null, null, null));
        node.setOperator("==");
        node.setRight(new Node(NodeTypesEnum.LITERAL, null, String.valueOf(popToken().getLexeme()), null, null, null, null, null));
        consume(TokenType.COLON);
        return node;
    }

    /**
     * Handle the switch body, putting everything up to the break into the body
     *
     * @return the filled body node
     */
    private List<Node> handleSwitchBody() {
        List<Node> res = new ArrayList<>();
        while (!peek(TokenType.BREAK)) {
            Node temp = parse(tokens);
            if (temp != null) {
                res.add(temp);
            }
        }
        consume(TokenType.BREAK);
        consume(TokenType.SEMICOLON);
        return res;
    }

    /**
     * Handles default cases of switches
     *
     * @param node ElseNode to fill with
     * @return filled ElseNode
     */
    private Node handleDefault(Node node) {
        node.setBody(handleBlock());
        return node;
    }

    /**
     * Check if a token is an operator
     *
     * @param type the token to check
     * @return true if token is one of the following <, <=, >, >=, ==, !=, +, -, =, *, /, %, &&, ||
     */
    private boolean isOperator(TokenType type) {
        return type == TokenType.LESS || type == TokenType.LESS_EQUAL || type == TokenType.GREATER || type == TokenType.GREATER_EQUAL || type == TokenType.BANG_EQUAL ||
                type == TokenType.EQUAL_EQUAL || type == TokenType.PLUS || type == TokenType.MINUS || type == TokenType.EQUAL ||
                type == TokenType.STAR || type == TokenType.SLASH || type == TokenType.MOD || type == TokenType.LOGICAL_AND || type == TokenType.LOGICAL_OR;
    }

    /**
     * Handles any binary expression, also takes care of nesting
     *
     * @param node the node to attach the binExp to
     * @return the node with the binExp attached
     */
    private Node handleBinaryExp(Node node) {
        // if there is a nest on the left side
        if (peek(TokenType.LEFT_PAREN)) {
            consume(TokenType.LEFT_PAREN);
            // check for increments
            if (tokens.get(1).getType() == TokenType.INCREMENT || tokens.get(1).getType() == TokenType.DECREMENT) {
                return handleCrement();
            }
            node.setLeft(handleBinaryExp(new Node(NodeTypesEnum.BINARY_EXPRESSION)));
            // checking for valid left side
        } else if (!isOperator(tokens.get(0).getType())) {
            // check for increment on other side of operator
            if (tokens.get(1).getType() == TokenType.INCREMENT || tokens.get(1).getType() == TokenType.DECREMENT) {
                return handleCrement();
            }
            // in case of not operator in front of expressions
            if (peek(TokenType.BANG) && tokens.get(1).getType() == TokenType.LEFT_PAREN) {
                consume(TokenType.BANG);
                consume(TokenType.LEFT_PAREN);
                node.setOperator("!");
                node.setLeft(handleBinaryExp(new Node(NodeTypesEnum.BINARY_EXPRESSION)));
                // in case of not operator in front of single variables
            } else if (peek(TokenType.BANG)) {
                consume(TokenType.BANG);
                node.setLeft(new Node(NodeTypesEnum.IDENTIFIER).setValue(popToken().getLexeme()).setOperator("!"));
                // final literal
            } else {
                node.setLeft(new Node(NodeTypesEnum.LITERAL).setValue(popToken().getLexeme()));
            }
        }

        // set the operator for the corresponding binExp
        if (isOperator(tokens.get(0).getType())) {
            node.setOperator(popToken().getLexeme());
        }

        // in case of unary expression (not)
        if (peek(TokenType.RIGHT_PAREN)) {
            popToken();
            return node;
        }

        // if there is a nest on the right side
        if (peek(TokenType.LEFT_PAREN)) {
            consume(TokenType.LEFT_PAREN);
            if (tokens.get(1).getType() == TokenType.INCREMENT || tokens.get(1).getType() == TokenType.DECREMENT) {
                return handleCrement();
            }
            node.setRight(handleBinaryExp(new Node(NodeTypesEnum.BINARY_EXPRESSION)));
            // check for a valid right side
        } else if (!isOperator(tokens.get(0).getType())) {
            // handle increment on the right side
            if (tokens.get(1).getType() == TokenType.INCREMENT || tokens.get(1).getType() == TokenType.DECREMENT) {
                return handleCrement();
                // no increment
            } else {
                // in case of not operator in front of expressions
                if (peek(TokenType.BANG) && tokens.get(1).getType() == TokenType.LEFT_PAREN) {
                    // nested negated expression
                    consume(TokenType.BANG);
                    consume(TokenType.LEFT_PAREN);
                    node.setOperator("!");
                    node.setLeft(handleBinaryExp(new Node(NodeTypesEnum.BINARY_EXPRESSION)));
                    // in case of not operator in front of single variables
                } else if (peek(TokenType.BANG)) {
                    // just negated variable
                    consume(TokenType.BANG);
                    node.setLeft(new Node(NodeTypesEnum.IDENTIFIER).setValue(popToken().getLexeme()).setOperator("!"));
                    // final literal
                } else {
                    node.setRight(new Node(NodeTypesEnum.LITERAL).setValue(popToken().getLexeme()));
                }
            }
        }
        // end recursion if statement is closed and consume tokens if any remain
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
    private Node handleFor(Node node) {
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

    private Node handleWhile(Node node) {
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
    private Node handleCrement() {
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
     * Handle operators like += and -= (i.e. x += y)
     *
     * @return a binary node with the operation transformed into x = x + y
     */
    private Node handleOpEqual() {
        Node resNode = new Node(NodeTypesEnum.BINARY_EXPRESSION);
        // for example x += y; --> x = (x + y)
        String identifier1 = popToken().getLexeme();
        Node identifierNode1 = new Node(NodeTypesEnum.IDENTIFIER, null, identifier1, null, null, null, null, null);

        String identifier2 = tokens.get(1).getLexeme();
        Node identifierNode2 = new Node(NodeTypesEnum.IDENTIFIER, null, identifier2, null, null, null, null, null);

        resNode.setLeft(identifierNode1);
        resNode.setOperator("=");
        if (peek(TokenType.PLUS_EQUAL)) {
            resNode.setRight(new Node(NodeTypesEnum.BINARY_EXPRESSION, null, null, identifierNode1, "+", identifierNode2, null, null));
            consume(TokenType.PLUS_EQUAL);
            popToken();
        } else {
            resNode.setRight(new Node(NodeTypesEnum.BINARY_EXPRESSION, null, null, identifierNode1, "-", identifierNode2, null, null));
            consume(TokenType.MINUS_EQUAL);
            popToken();
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
    private Node handleDeclaration(Node node) {
        // delete string, int, long...
        popToken();
        // store name of variable in value
        node.setValue(tokens.get(0).getLexeme());
        // if there is an init like (int i = 0;)
        // create a binExp
        if (tokens.get(1).getType() == TokenType.EQUAL) {
            node.setCondition(handleBinaryExp(new Node(NodeTypesEnum.BINARY_EXPRESSION)));
        }
        popToken();
        //consumeErrorFree(TokenType.SEMICOLON);
        return node;
    }

    private Node handleFunctionDeclaration(Node node) {
        node.setOperator(popToken().getLexeme()); // return type
        node.setValue(popToken().getLexeme());  // function name
        List<Node> params = new ArrayList<>();
        consume(TokenType.LEFT_PAREN);
        while (!peek(TokenType.RIGHT_PAREN)) {
            // for each param, a node with param type in operator and value is name of param
            params.add(new Node(NodeTypesEnum.IDENTIFIER, null, tokens.get(1).getLexeme(), null, tokens.get(0).getLexeme(), null, null, null));
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
    private Node handleFunctionCall(Node node) {
        node.setValue(tokens.get(0).getLexeme()); // name of function
        consume(TokenType.IDENTIFIER);
        consume(TokenType.LEFT_PAREN);
        List<Node> params = new ArrayList<>();
        while (!peek(TokenType.RIGHT_PAREN)) {
            // handle parameters (values vs variables)
            if (tokens.get(0).getLiteral() instanceof String || tokens.get(0).getLiteral() instanceof Number) {
                params.add(new Node(NodeTypesEnum.LITERAL, null, tokens.get(0).getLexeme(), null, null, null, null, null));
                popToken(); // pop param
            } else if (tokens.get(0).getType() == TokenType.IDENTIFIER && tokens.get(1).getType()==TokenType.LEFT_PAREN){
                params.add(handleFunctionCall(new Node(NodeTypesEnum.FUNCTION_CALL)));
            }
            else {
                params.add(new Node(NodeTypesEnum.IDENTIFIER, null, tokens.get(0).getLexeme(), null, null, null, null, null));
                popToken(); // pop param
            }
            //popToken(); // pop param
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
    private Node handleReturn(Node node) {
        consume(TokenType.RETURN);
        //consumeErrorFree(TokenType.LEFT_PAREN);
        node.setCondition(parse(tokens)); // for stuff like: return add(2,3) + 3;
        //consumeErrorFree(TokenType.RIGHT_PAREN);
        return node;
    }

    /**
     * Continue recursion through parse
     *
     * @return filled block node
     */
    private List<Node> handleBlock() {
        // check for curly brackets
        List<Node> res = new ArrayList<>();
        while (!peek(TokenType.RIGHT_BRACE)) {
            res.add(parse(tokens));
        }
        return res;
    }

}

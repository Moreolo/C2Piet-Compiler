package ast.lexer;

import ast.datatypes.Node;
import ast.datatypes.NodeTypesEnum;

import java.util.List;

public class AST {
    private Node programRoot = new Node(NodeTypesEnum.PROGRAM);

    private final String[] testTokens = new String[25];

    /**
     * the following c code as a token array
     * if (1 > 2) {
     *  x = 3;
     *  //this is a comment, will be ignored...
     *  y = 4;
     * } else {
     *  z = 5
     * }
     */
    private void fillTestTokens() {
        testTokens[0] = "if";
        testTokens[1] = "(";
        testTokens[2] = "1";
        testTokens[3] = ">";
        testTokens[4] = "2";
        testTokens[5] = ")";
        testTokens[6] = "{";
        testTokens[7] = "x";
        testTokens[8] = "=";
        testTokens[9] = "3";
        testTokens[10] = ";";
        testTokens[11] = "y";
        testTokens[12] = "=";
        testTokens[13] = "4";
        testTokens[14] = "}";
        testTokens[15] = "else";
        testTokens[16] = "{";
        testTokens[17] = "z";
        testTokens[18] = "=";
        testTokens[19] = "5";
        testTokens[20] = "}";
    }

    /**
     * Build the AST for a given Input Array of Tokens
     * Will return only the starting node of the program
     * @param tokens the Array of tokens
     * @return entry Node to tree
     */
    public Node buildTree(List<Token> tokens) {
        tokens.forEach(this::buildNode);
        return programRoot;
    }

    // TODO: dummy method -> implement behaviour
    // recursive decent parser
    // CONSUME; LOOK AHEAD
    // pares if -> parse condition -> parse block -> return back up...
    private void buildNode(Token token) {
        switch (token.getType()) {
            case IF:
                //handleIfStatement();
                break;
            case FOR, WHILE, DO:
                //handleWhileStatement();
                break;
            case EQUAL:
                //handleAssignmentStatement();
                break;
            default:
                throw new RuntimeException("unexpected token: " + token);
        }

    }

}

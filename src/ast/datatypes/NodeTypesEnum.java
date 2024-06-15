package ast.datatypes;

public enum NodeTypesEnum {
    /**
     * for if else constructs
     */
    IF_STATEMENT,
    /**
     * for else parts
     */
    ELSE_STATEMENT,

    FUNCTION_TEMP_RETURN,
    /**
     * any block of code
     * i.e. in if's, loops or methods
     */
    BLOCK_STATEMENT,
    /**
     * for loop constructs
     */
    WHILE_STATEMENT,
    /**
     * for return calls
     */
    RETURN_STATEMENT,
    /**
     * any math expression or comparison
     */
    BINARY_EXPRESSION,
    /**
     * Declaring a variable
     */
    DECLARATION,
    /**
     * call of a function
     */
    FUNCTION_CALL,
    /**
     * declaration of a function
     */
    FUNCTION_DEF,
    /**
     * a concrete value
     */
    LITERAL,
    /**
     * for assignment expressions
     */
    IDENTIFIER,
    /**
     * entry point to a program
     */
    PROGRAM,
    /**
     * end of program and execution
     */
    TERMINATOR

}
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
     * for the next team
     */
    FUNCTION_TEMP_RETURN,
    /**
     * a concrete value
     */
    LITERAL,
    /**
     * for assignment expressions or variables
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

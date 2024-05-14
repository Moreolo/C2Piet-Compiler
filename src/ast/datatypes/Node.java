package ast.datatypes;

import java.util.ArrayList;


/**
 * Datatype for nodes of the Abstract-Syntax-Tree
 */
public class Node {
    /**
     * Type of the node
     */
    private NodeTypesEnum type;

    /**
     * Set of first-level child-nodes contained in this node
     */
    private ArrayList<Node> body;

    /**
     * The value of leaf-nodes
     */
    private int value;

    /**
     * Node to the left
     */
    private Node left;

    /**
     * Node to the right
     */
    private Node right;

    /**
     * Conditions for If and WhileStats
     */
    private Node condition;

    /**
     * Operator for binExp
     */
    private String operator;


    public NodeTypesEnum getType() {
        return type;
    }

    public void setType(NodeTypesEnum type) {
        this.type = type;
    }

    public ArrayList<Node> getBody() {
        return body;
    }

    public void setBody(ArrayList<Node> body) {
        this.body = body;
    }

    public int getValue() {
        return value;
    }

    public void setValue(int value) {
        this.value = value;
    }

    public Node getRight() {
        return right;
    }

    public void setRight(Node right) {
        this.right = right;
    }

    public Node getCondition() {
        return condition;
    }

    public void setCondition(Node condition) {
        this.condition = condition;
    }

    public Node getLeft() {
        return left;
    }

    public void setLeft(Node left) {
        this.left = left;
    }

    public String getOperator() {
        return operator;
    }

    public void setOperator(String operator) {
        this.operator = operator;
    }

    /**
     * Constructor for a complete node
     */
    public Node(NodeTypesEnum type, ArrayList<Node> body, int value, Node left, String operator, Node right, Node condition) {
       this.type = type;
       this.body = body;
       this.value = value;
       this.left = left;
       this.operator = operator;
       this.right = right;
       this.condition = condition;
   }

    /**
     * Creates a blank node of a specific type
     * @param type of the node to be created
     */
   public Node(NodeTypesEnum type) {
       this(type, null, 0, null, "",null, null);
   }
}

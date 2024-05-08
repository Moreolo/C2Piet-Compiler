package ast.datatypes;

import java.util.List;
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
    private List<Node> body;

    /**
     * The value of leaf-nodes
     */
    private String value;

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

    public Node setType(NodeTypesEnum type) {
        this.type = type;
        return this;
    }

    public List<Node> getBody() {
        return body;
    }

    public Node setBody(List<Node> body) {
        this.body = body;
        return this;
    }

    public String getValue() {
        return value;
    }

    public Node setValue(String value) {
        this.value = value;
        return this;
    }

    public Node getRight() {
        return right;
    }

    public Node setRight(Node right) {
        this.right = right;
        return this;
    }

    public Node getCondition() {
        return condition;
    }

    public Node setCondition(Node condition) {
        this.condition = condition;
        return this;
    }

    public Node getLeft() {
        return left;
    }

    public Node setLeft(Node left) {
        this.left = left;
        return this;
    }

    public String getOperator() {
        return operator;
    }

    public Node setOperator(String operator) {
        this.operator = operator;
        return this;
    }

    /**
     * Constructor for a complete node
     */
    public Node(NodeTypesEnum type, List<Node> body, String value, Node left, String operator, Node right, Node condition) {
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
       this(type, null, "", null, "",null, null);
   }
}

package piet.datatypes;

import java.util.LinkedList;

public class Block {
    private int num;
    private LinkedList<Operation> operations;

    public Block(int num) {
        this.num = num;
        this.operations = new LinkedList<>();
    }

    public void addOperation(Operation operation) {
        this.operations.add(operation);
    }

    public int getNum() {
        return this.num;
    }

    public LinkedList<Operation> getOperations() {
        return this.operations;
    }
}

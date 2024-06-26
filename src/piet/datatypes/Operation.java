package piet.datatypes;

public class Operation {
    private Command name;
    private int val1;
    private int val2;

    public Operation(Command name, int val1, int val2) {
        this.name = name;
        this.val1 = val1;
        this.val2 = val2;
    }

    public Operation(Command name, int val1) {
        this.name = name;
        this.val1 = val1;
        this.val2 = -1;
    }

    public Operation(Command name) {
        this.name = name;
        this.val1 = -1;
        this.val2 = -1;
    }

    public Command getName() {
        return this.name;
    }

    public int getVal1() {
        return this.val1;
    }

    public int getVal2() {
        return this.val2;
    }
}
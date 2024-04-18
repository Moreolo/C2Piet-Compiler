package piet.datatypes;

public class Operation {
    private String name;
    private int val1;
    private int val2;

    public Operation(String name, int val1, int val2) {
        this.name = name;
        this.val1 = val1;
        this.val2 = val2;
    }

    public Operation(String name, int val1) {
        this.name = name;
        this.val1 = val1;
        this.val2 = -1;
    }

    public Operation(String name) {
        this.name = name;
        this.val1 = -1;
        this.val2 = -1;
    }

    public String getName() {
        return this.name;
    }

    public int getVal1() {
        return this.val1;
    }

    public int getVal2() {
        return this.val2;
    }
}
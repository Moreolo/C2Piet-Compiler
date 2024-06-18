package design;

import java.awt.image.BufferedImage;
import java.util.LinkedList;

import piet.datatypes.Block;
import piet.datatypes.Command;
import piet.datatypes.Operation;

public class BlockGenerator {
    public static int blockWidth = 6;

    private LinkedList<PietColor[]> chooser;
    private LinkedList<PietColor[]> block;

    private int pos;
    private PietColor color;
    private PietColor[] row;

    private LinkedList<Operation> operations;
    private LinkedList<Operation> chooserPush;

    private int collision;
    private boolean specialPushPossible;
    private PietColor funkyColor;

    public BlockGenerator(Block block) {
        generateChooser(block.getNum());
        generateBlock(block.getOperations());
    }

    private void generateChooser(int blockNum) {
        this.chooser = new LinkedList<>();

        color = new PietColor(4, 1);
        row = new PietColor[blockWidth - 1];

        // erste 2 Zeilen

        // Zeile 1
        row[0] = new PietColor(true, false);
        row[1] = new PietColor(true, false);
        row[2] = color;
        row[3] = color;
        row[4] = new PietColor(false, true);
        pushRowToChooser();

        // Zeile 2
        row[0] = new PietColor(true, false);
        row[1] = new PietColor(true, false);
        row[2] = new PietColor(false, true);
        row[3] = new PietColor(true, false);
        row[4] = new PietColor(true, false);
        pushRowToChooser();

        // blockNum push
        chooserPush(blockNum);

        // letzte 2 Zeilen
        // Zeile 1
        color = new PietColor(4, 1);
        row[0] = new PietColor(false, true);
        row[1] = new PietColor(false, true);
        row[2] = new PietColor(false, true);
        row[3] = new PietColor(false, true);
        color.add(Command.PUSH);
        row[4].set(color);
        pushRowToChooser();

        // Zeile 2
        row[0] = new PietColor(false, true);
        row[1] = new PietColor(3, 0);
        row[2] = new PietColor(2, 2);
        row[3] = new PietColor(true, false);
        color.add(Command.ADD);
        row[4].set(color);
        pushRowToChooser();

    }

    private void chooserPush(int number) {

        // temporäre Zahlenliste
        LinkedList<Integer> numberParts = new LinkedList<>();

        // zerteilen der Zahl in ein Basis-10-System; letzte "Ziffer" kann Wert bis 18
        // annehmen
        while (number > 19) {

            int temp = number % 10;
            numberParts.addFirst(temp);
            number -= temp;
            number /= 10;

        }
        numberParts.addFirst(number);

        // Anlegen der Kommando-Struktur
        chooserPush.addFirst(new Operation(Command.PUSH, numberParts.get(0)));

        for (int i = 1; i < numberParts.size(); i++) {
            chooserPush.add(new Operation(Command.PUSH, 10));
            chooserPush.add(new Operation(Command.MULTIPLY));

            int j = numberParts.get(i);
            if (j != 0) {
                chooserPush.add(new Operation(Command.PUSH, j));
                chooserPush.add(new Operation(Command.ADD));
            }

        }

        for (Operation element : chooserPush) {
            switch (element.getName()) {
                case PUSH:
                    color.add(Command.PUSH);
                    // in 2-3 Zeilen aufteilen
                    int rowVal = (element.getVal1());

                    // Zeile füllen
                    for (int i = 0; i < (rowVal / 5); i++) {
                        for (int j = 4; j < 1; j--) {
                            row[j].set(color);
                            pushRowToChooser();
                        }
                    }
                    ;
                    if (rowVal % 5 != 0) {
                        for (int j = 0; j < 5 - (rowVal % 5); j++) {
                            row[j] = new PietColor(true, false);
                        }
                        for (int j = 0; j > rowVal % 5; j++) {
                            row[4 - j].set(color);
                            pushRowToChooser();
                        }
                    }
                    break;
                case MULTIPLY:
                    color.add(Command.MULTIPLY);
                    row[4].set(color);
                    for (int j = 0; j < 4; j++) {
                        row[j] = new PietColor(true, false);
                    }
                    pushRowToChooser();
                    break;
                case ADD:
                    color.add(Command.ADD);
                    row[4].set(color);
                    for (int j = 0; j < 4; j++) {
                        row[j] = new PietColor(true, false);
                    }
                    pushRowToChooser();
                    break;

                default:
                    break;
            }

        }

    }

    private void generateBlock(LinkedList<Operation> operations) {
        this.block = new LinkedList<>();

        pos = 2;
        color = new PietColor(2, 2);
        row = new PietColor[blockWidth - 1];

        for (int i = 0; i < row.length; i++)
            row[i] = new PietColor(true, false);

        this.operations = operations;

        collision = -1;
        specialPushPossible = false;
        funkyColor = new PietColor(false, true);

        while (!operations.isEmpty())
            generateOperation(operations.pop());
        generateLastOperation();
    }

    private void generateOperation(Operation operation) {
        // Setzt die Pixel für die vorherige Operation
        // Das muss in dieser Reihenfolge gemacht werden damit die push und pointer
        // Operation richtig gezeichnet werden können
        Command c = operation.getName();
        if (c == Command.PUSH)
            generatePush(operation.getVal1());
        else if (c == Command.POINTER) {
            // if (operation.getVal1() < 1 || operation.getVal2() < 1)
            // throw new Error("Der Pointer Command braucht Werte höher als 0.");
            generatePointer(operation.getVal1(), operation.getVal2());
        } else
            generateOtherOperation();

        // Verändert den Farbwert für den nächsten Pixel für die Operation
        // Nicht bei Pointer, weil diese Veränderung passiert schon in generatePointer
        if (c != Command.POINTER)
            color.add(c);
        // Überprüft Kollision bei pos = 9
        // Setzt Kollisionswert für Pushblock
        // Bei keiner Kollision und schwarzen Pixel bei 0: pos = 0
        if (pos == 9 && !operations.isEmpty()) {
            collision = -1;
            specialPushPossible = false;
            PietColor futureColor = color.getCopy();
            int pushLeft = 1;
            int opIndex = 1;
            Operation futureOp = operations.getFirst();
            futureColor.add(futureOp.getName());
            if (futureOp.getName() == Command.PUSH) {
                pushLeft = futureOp.getVal1();
            }
            int opSize = operations.size();
            for (int x = 1; x < blockWidth - 1 && opIndex < opSize; x++) {
                if (pushLeft > 1) {
                    pushLeft--;
                } else {
                    futureOp = operations.get(opIndex++);
                    futureColor.add(futureOp.getName());
                    if (futureOp.getName() == Command.PUSH)
                        pushLeft = futureOp.getVal1();
                    else if (futureOp.getName() == Command.POINTER && x == blockWidth - 3)
                        pushLeft = blockWidth - 3;
                }
                if (futureColor.is(block.getLast()[blockWidth - 2 - x])) {
                    collision = x;
                    break;
                }
            }
            if (collision == -1 && pushLeft <= 1)
                if (block.getLast()[0].isBlack())
                    pos = 0;
                else
                    specialPushPossible = true;
        }
        if (pos != 9)
            collision = -1;
    }

    // Generiert die Operation außer Push und Pointer
    private void generateOtherOperation() {
        if (pos < blockWidth - 2) {
            // 0-3
            // Generiert Operation
            row[blockWidth - 2 - pos].set(color);
        } else if (pos == blockWidth - 2) {
            generateTurn();
        } else if (pos < blockWidth * 2 - 4) {
            // 5-7
            // Generiert Operation
            row[pos - blockWidth + 2].set(color);
        } else if (pos == blockWidth * 2 - 4) {
            // 8
            // Generiert Operation
            // Pusht Reihe
            row[blockWidth - 2].set(color);
            pushRowToBlock();
        } else if (pos == blockWidth * 2 - 3) {
            // 9
            // Generiert schwarz plus Operation
            // Pusht Reihe
            for (int i = 0; i < blockWidth - 2; i++)
                row[i].setBlack();
            row[blockWidth - 2].set(color);
            if (!operations.isEmpty()) {
                Operation next = operations.getFirst();
                if (next.getName() != Command.PUSH)
                    pushRowToBlock();
                else if (next.getVal1() > 3)
                    pos = 0;
                else
                    pushRowToBlock();
            } else {
                pushRowToBlock();
            }
        }
        // Erhöht Position der Operation
        pos = (pos + 1) % (blockWidth * 2 - 2);
    }

    private void generateTurn() {
        // 4
        // Generiert 3er-Block Operation plus schwarz
        // Pusht 2 Reihen
        row[0].set(color);
        pushRowToBlock();
        row[0].set(color);
        // Fügt funky ein
        row[1].set(funkyColor);
        funkyColor.setBlack();
        pushRowToBlock();
        row[0].set(color);
    }

    private void generatePush(int val) {
        // Teilt den Push Block in Multiplikationen auf
        if (val == 0) {
            val = 1;
            operations.addFirst(new Operation(Command.NOT));
        } else if (val < 0) {
            operations.addFirst(new Operation(Command.SUBTRACT));
            operations.addFirst(new Operation(Command.PUSH, val + 1));
            val = 1;
        }

        while (val > 19) {
            int num = val % 10;
            if (num != 0) {
                operations.addFirst(new Operation(Command.ADD));
                operations.addFirst(new Operation(Command.PUSH, num));
            }
            operations.addFirst(new Operation(Command.MULTIPLY));
            operations.addFirst(new Operation(Command.PUSH, 10));
            val /= 10;
        }

        if (pos == 0)
            generatePushDown(val, true);
        else if (pos < blockWidth - 1)
            generatePushToLeft(val);
        else if (pos < blockWidth * 2 - 3)
            generatePushToRight(val);
        else
            generatePushDown(val, false);
    }

    private void generatePushDown(int val, boolean xAsPos) {
        int x = 0;
        while (val > 0) {
            if (x == collision) {
                x = 0;
                collision = -1;
                pushRowToBlock();
                xAsPos = false;
            }
            row[blockWidth - 2 - x].set(color);
            val--;
            x = (x + 1) % (blockWidth - 1);
            if (x == 0) {
                pushRowToBlock();
                xAsPos = false;
            }
        }
        if (xAsPos)
            pos = x;
        else {
            if (x != 0)
                pushRowToBlock();
            pos = 9;
        }
    }

    private void generatePushToLeft(int val) {
        if (pos + val < blockWidth - 1) {
            while (val > 0) {
                row[blockWidth - 2 - pos].set(color);
                val--;
                pos++;
            }
        } else if (pos == 1) {
            while (val > 2 && pos < 5) {
                row[blockWidth - 2 - pos].set(color);
                val--;
                pos++;
            }
            pushRowToBlock();
            generatePushDown(val, false);
        } else {
            int space = blockWidth - 3 - pos;
            if (space == -1) {
                generateTurn();
                pos = blockWidth - 1;
                if (val < 3) {
                    row[1].setWhite();
                    pos = blockWidth;
                    generatePushToRight(val);
                } else if (val > 3)
                    generatePushToRight(val - 3);
            } else if (space == 0 && val == 2) {
                row[1].set(color);
                funkyColor = color.getCopy();
                pos = blockWidth - 2;
            } else {
                int leftCol = val % 3;
                int cols = val / 3;
                if (leftCol != 0)
                    cols++;
                int startX = 0;
                if (val <= space * 3) {
                    row[blockWidth - 2 - pos].set(color);
                    row[blockWidth - 3 - pos].setWhite();
                    val = 0;
                } else {
                    startX = space + 2 - cols;
                    if (startX < 0) {
                        leftCol = 0;
                        startX = 0;
                        cols = space + 2;
                        val -= cols * 3;
                    } else
                        val = 0;
                }
                for (int x = 0; x < startX; x++)
                    row[x].setWhite();
                for (int y = 0; y < 3; y++) {
                    boolean noFirstPixel = (y == 1 && leftCol == 1) || (y == 2 && leftCol != 0);
                    for (int x = 0; x < cols; x++) {
                        if (!(x == 0 && noFirstPixel))
                            row[startX + x].set(color);
                    }
                    if (y != 2)
                        pushRowToBlock();
                }
                pos = blockWidth - 2 + startX + cols;
                if (val > 0)
                    generatePushToRight(val);
            }
        }
    }

    private void generatePushToRight(int val) {
        while (pos < blockWidth * 2 - 3 && val > 0) {
            row[pos - blockWidth + 2].set(color);
            val--;
            pos++;
        }
        if (pos == 9) {
            boolean sameColor = true;
            for (int x = 0; x < blockWidth - 1; x++) {
                if (!row[blockWidth - 2 - x].is(color)) {
                    sameColor = false;
                } else if (!sameColor) {
                    collision = x;
                    break;
                }
            }
            pushRowToBlock();
        }
        if (val != 0) {
            generatePushDown(val, false);
        }
    }

    private void generatePointer(int val1, int val2) {
        // Generiert richtige Position
        generateOtherOperation();
        if (pos == blockWidth * 2 - 4) {
            // 8
            row[blockWidth - 2].setWhite();
            pushRowToBlock();
            pos = blockWidth * 2 - 3;
        }
        if (pos == blockWidth * 2 - 3) {
            // 9
            row[blockWidth - 2].setWhite();
            pushRowToBlock();
            pos = 0;
        }
        if (pos < blockWidth - 2) {
            // 0-3
            for (int x = blockWidth - 2 - pos; x > 0; x--) {
                row[x].setWhite();
            }
            generateTurn();
            pos = blockWidth - 1;
        } else if (pos == blockWidth - 2) {
            // 4
            row[0].setWhite();
            pushRowToBlock();
            row[1].set(color);
            pushRowToBlock();
            row[1].set(color);
            pos = blockWidth;
        }
        color.add(Command.POINTER);
        // Generiert ersten Push Block
        generateSlimPushBlock(val1, true);
        // Generiert Zwischenplatz
        for (int x = 0; x < blockWidth - 2; x++)
            row[x].set(color);
        newLine(true);
        for (int x = 0; x < blockWidth - 3; x++)
            row[x].setWhite();
        newLine(false);
        // Generiert zweiten Push Block
        generateSlimPushBlock(val2, false);
        pos = 0;
    }

    // Generiert dünnen Push Block
    private void generateSlimPushBlock(int val, boolean left) {
        boolean negative = false;
        if (val == 0) {
            // Generiert PUSH 1 und NOT und beendet Funktion
            if (left) {
                row[pos - blockWidth + 2].set(color);
                for (int x = pos + 1 - blockWidth + 2; x < blockWidth - 1; x++)
                    row[x].setWhite();
                pushRowToBlock();
                row[blockWidth - 2].setWhite();
            } else {
                row[blockWidth - 2].set(color);
                newLine(left);
            }
            color.add(Command.PUSH);
            if (left)
                row[pos - blockWidth + 2].set(color);
            else
                row[blockWidth - 2].set(color);
            newLine(left);
            color.add(Command.NOT);
            return;
        } else if (val < 0) {
            // Generiert PUSH 1, erhöht Wert um 1 und setzt negativ
            if (left) {
                row[pos - blockWidth + 2].set(color);
                for (int x = pos + 1 - blockWidth + 2; x < blockWidth - 1; x++)
                    row[x].setWhite();
                pushRowToBlock();
                row[blockWidth - 2].setWhite();
            } else {
                row[blockWidth - 2].set(color);
                newLine(left);
            }
            color.add(Command.PUSH);
            val = 1 - val;
            negative = true;
        }
        // Teilt Wert auf
        LinkedList<Integer> num = new LinkedList<>();
        while (val > 19) {
            num.addFirst(val % 10);
            val /= 10;
        }
        if (left) {
            // Erste Reihe bei links
            for (; pos < blockWidth * 2 - 4 && val > 0; pos++) {
                row[pos - blockWidth + 2].set(color);
                val--;
            }
            if (!negative)
                for (; pos < blockWidth * 2 - 3; pos++)
                    row[pos - blockWidth + 2].setWhite();
            // pos refunction
            newLine(left);
        }
        if (val != 0)
            generateSlimPush(val, left);
        else
            color.add(Command.PUSH);
        while (!num.isEmpty()) {
            // 10
            generateSlimPush(10, left);
            // *
            row[pos].set(color);
            newLine(left);
            color.add(Command.MULTIPLY);
            // num
            int digit = num.pop();
            if (digit != 0) {
                // digit
                generateSlimPush(digit, left);
                // +
                row[pos].set(color);
                newLine(left);
                color.add(Command.ADD);
            }
        }
        if (negative) {
            int startX = 0;
            if (!left)
                startX = 1;
            for (int x = 0; x < blockWidth - 2; x++)
                row[startX + x].set(color);
            newLine(left);
            color.add(Command.SUBTRACT);
        }
    }

    private void generateSlimPush(int val, boolean left) {
        for (; val > 0; val--) {
            if (pos < 0 && left || pos < 1 && !left)
                newLine(left);
            row[pos--].set(color);
        }
        newLine(left);
        color.add(Command.PUSH);
    }

    private void newLine(boolean left) {
        pushRowToBlock();
        if (left) {
            row[blockWidth - 2].setWhite();
            pos = blockWidth - 3;
        } else {
            row[0].setWhite();
            pos = blockWidth - 2;
        }
    }

    private void generateLastOperation() {
        if (pos == 0 || pos == blockWidth * 2 - 3) {
            // 0, 9
            row[blockWidth - 2].set(color);
        } else if (pos == 1) {
            // 1
            pushRowToBlock();
            row[blockWidth - 2].set(color);
        } else if (pos < blockWidth - 1) {
            // 2-4
            row[blockWidth - 2 - pos].set(color);
            for (int x = 0; x < blockWidth - 2 - pos; x++)
                row[x].setWhite();
            pushRowToBlock();
            row[blockWidth - 2 - pos].set(color);
            for (int x = blockWidth - 2 - pos + 1; x < blockWidth - 2; x++)
                row[x].setWhite();
        } else {
            // 5-8
            row[pos - blockWidth + 2].set(color);
            for (; pos < blockWidth * 2 - 3; pos++)
                row[pos - blockWidth + 2].setWhite();
        }
        pushRowToBlock();
    }

    // Pusht die Reihe auf den Block
    private void pushRowToBlock() {
        block.add(row);
        row = new PietColor[blockWidth - 1];
        for (int i = 0; i < row.length; i++)
            row[i] = new PietColor(false, true);
    }

    // Pusht die Reihe auf den Block
    private void pushRowToChooser() {
        chooser.add(row);
        row = new PietColor[blockWidth - 1];
        for (int i = 0; i < row.length; i++)
            row[i] = new PietColor(false, true);
    }

    public int getHeight() {
        return 0;
    }

    public LinkedList<PietColor[]> getBlock() {
        return block;
    }

    public void paint(BufferedImage image, int xBlockPos, int yBlockPos) {

    }
}

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

    private int collision;

    public BlockGenerator(Block block) {
        this.chooser = new LinkedList<>();
        generateChooser(block.getNum());
        generateBlock(block.getOperations());
    }

    private void generateChooser(int blockNum) {

    }

    private void generateBlock(LinkedList<Operation> operations) {
        this.block = new LinkedList<>();

        pos = 2;
        color = new PietColor(2, 2);
        row = new PietColor[blockWidth - 1];

        for(int i = 0; i < row.length; i++)
            row[i] = new PietColor(true, false);

        this.operations = operations;

        collision = -1;

        while(!operations.isEmpty())
            generateOperation(operations.pop());
        generateOtherOperation();
        block.add(row);
    }

    private void generateOperation(Operation operation) {
        // Setzt die Pixel für die vorherige Operation
        // Das muss in dieser Reihenfolge gemacht werden damit die push und pointer
        // Operation richtig gezeichnet werden können
        Command c = operation.getName();
        if (c == Command.PUSH)
            generatePush(operation.getVal1());
        else if (c == Command.POINTER) {
            if (operation.getVal1() < 1 || operation.getVal2() < 1)
                throw new Error("Der Pointer Command braucht Werte höher als 0.");
            generatePointer(operation.getVal1(), operation.getVal2());
        } else
            generateOtherOperation();

        // Verändert den Farbwert für den nächsten Pixel für die Operation
        color.add(c);
        //Überprüft Kollision bei pos = 9
        //Setzt Kollisionswert für Pushblock
        //Bei keiner Kollision und schwarzen Pixel bei 0: pos = 0
        if(pos == 9 && !operations.isEmpty()) {
            collision = -1;
            PietColor futureColor = color.getCopy();
            int pushLeft = 1;
            int opIndex = 1;
            Operation futureOp = operations.getFirst();
            futureColor.add(futureOp.getName());
            if(futureOp.getName() == Command.PUSH) {
                pushLeft = futureOp.getVal1();
            }
            int opSize = operations.size();
            for(int x = 1; x < blockWidth - 1 && opIndex < opSize; x++) {
                if(pushLeft > 1) {
                    pushLeft--;
                } else {
                    futureOp = operations.get(opIndex++);
                    futureColor.add(futureOp.getName());
                    if(futureOp.getName() == Command.PUSH) {
                        pushLeft = futureOp.getVal1();
                    }
                }
                if(futureColor.is(block.getLast()[blockWidth - 2 - x])) {
                    collision = x;
                    break;
                }
            }
            if(block.getLast()[0].isBlack() && collision == -1)
                pos = 0;
        }
        if(pos != 9)
            collision = -1;
    }

    //Generiert die Operation außer Push und Pointer
    private void generateOtherOperation() {
        if(pos < blockWidth - 2) {
            //0-3
            //Generiert Operation
            row[blockWidth - 2 - pos].set(color);
        } else if(pos == blockWidth - 1) {
            //4
            //Generiert 3er-Block Operation plus schwarz
            //Pusht 2 Reihen
            row[0].set(color);
            pushRowToBlock();
            row[0].set(color);
            //Fügt schwarz ein
            for(int i = 1; i < blockWidth - 1; i++)
                row[i].setBlack();
            pushRowToBlock();
            row[0].set(color);
        } else if(pos < blockWidth * 2 - 4) {
            //5-7
            //Generiert Operation
            row[pos - blockWidth + 2].set(color);
        } else if(pos == blockWidth * 2 - 4) {
            //8
            //Generiert Operation
            //Pusht Reihe
            row[blockWidth - 2].set(color);
            pushRowToBlock();
        } else if(pos == blockWidth * 2 - 3) {
            //9
            //Generiert schwarz plus Operation
            //Pusht Reihe
            for(int i = 0; i < blockWidth - 2; i++)
                row[i].setBlack();
            row[blockWidth - 2].set(color);
            pushRowToBlock();
        }
        //Erhöht Position der Operation
        pos = (pos + 1) % (blockWidth * 2 - 2);
    }

    private void generatePush(int val) {
        //Teilt den Push Block in Multiplikationen auf
        if(val == 0) {
            val = 1;
            operations.addFirst(new Operation(Command.NOT));
        }

        while(val > 19) {
            int num = val % 10;
            if(num != 0) {
                operations.addFirst(new Operation(Command.ADD));
                operations.addFirst(new Operation(Command.PUSH, num));
            }
            operations.addFirst(new Operation(Command.MULTIPLY));
            operations.addFirst(new Operation(Command.PUSH, 10));
            val /= 10;
        }

        if(pos == 0)
            generatePushDown(val, true);
        if(pos < blockWidth - 1)
            generatePushToLeft();
        else if(pos < blockWidth * 2 - 1)
            generatePushToRight(val);
        else
            generatePushDown(val, true);
    }

    private void generatePushDown(int val, boolean xAsPos) {
        int x = 0;
        while(val > 0) {
            if(x == collision) {
                x = 0;
                collision = -1;
                pushRowToBlock();
            }
            row[blockWidth - 1 - x].set(color);
            val--;
            x = (x + 1) % (blockWidth - 1);
            if(x == 0) {
                pushRowToBlock();
                xAsPos = false;
            }
        }
        if(xAsPos)
            pos = x;
        else
            pos = 9;
    }

    private void generatePushToLeft() {
        
    }

    private void generatePushToRight(int val) {
        while(pos < blockWidth * 2 - 3 && val > 0) {
            row[pos - blockWidth + 2].set(color);
            val --;
            pos ++;
        }
        if(val != 0) {
            generatePushDown(val, false);
        }
    }

    private boolean colorColision(int pixelBehind, int pixelFront, boolean single) {
        return true;
    }

    private void generatePointer(int val1, int val2) {

    }

    //Pusht die Reihe auf den Block
    private void pushRowToBlock() {
        block.add(row);
        row = new PietColor[blockWidth - 1];
        for(int i = 0; i < row.length; i++)
            row[i] = new PietColor(false, true);
    }

    //Pusht die Reihe auf den Block
    private void pushRowToChooser() {
        chooser.add(row);
        row = new PietColor[blockWidth - 1];
        for(int i = 0; i < row.length; i++)
            row[i] = new PietColor(false, true);
    }

    public int getHeight() {
        return 0;
    }

    public void paint(BufferedImage image, int xBlockPos, int yBlockPos) {

    }
}

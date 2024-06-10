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
    private PietColor[] spacer;

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
        spacer = new PietColor[blockWidth - 3];

        for(int i = 0; i < row.length; i++)
            row[i] = new PietColor(true, false);
        for(int i = 0; i < spacer.length; i++)
            spacer[i] = new PietColor(false, true);

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

        //TODO: 9 möglicherweise auf 0 setzen wenn keine Kollision
        //TODO: bei 9 wird ein Kollisionswert gesetzt für Pushblöcke bei 9
        // Verändert den Farbwert für den nächsten Pixel für die Operation
        color.add(c);
    }

    //Generiert die Operation außer Push und Pointer
    private void generateOtherOperation() {
        //Setzt Spacer bei neuer Zeile zurück
        if(pos == 0)
            for(int i = 0; i < spacer.length; i++)
                spacer[i].setBlack();

        if(pos < blockWidth - 2) {
            //0-3
            //Generiert Operation
            row[blockWidth - 2 - pos].set(color);
        } else if(pos == blockWidth - 1) {
            //4
            //Generiert 3er-Block Operation plus Spacer
            //Pusht 2 Reihen
            row[0].set(color);
            pushRow();
            row[0].set(color);
            //Fügt Spacer ein
            for(int i = 1; i < blockWidth - 1; i++)
                row[i].set(spacer[i - 1]);
            pushRow();
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
            pushRow();
        } else if(pos == blockWidth * 2 - 3) {
            //9
            //Generiert schwarz plus Operation
            //Pusht Reihe
            for(int i = 0; i < blockWidth - 2; i++)
                row[i].setBlack();
            row[blockWidth - 2].set(color);
            pushRow();
        }
        //Erhöht Position der Operation
        pos = (pos + 1) % (blockWidth * 2 - 2);
    }

    private void generatePush(int val) {
        //Teilt den Push Block in Multiplikationen auf
        if(val == 0) {
            val = 1;
            operations.addFirst(new Operation(Command.NOT));
        } else if(val > 19) {
            LinkedList<Integer> split = new LinkedList<>();
            while(val > 19) {
                split.addFirst(val % 10);
                val /= 10;
            }
            while(!split.isEmpty()) {
                operations.addFirst(new Operation(Command.PUSH, 10));
                operations.addFirst(new Operation(Command.MULTIPLY));
                int num = split.pop();
                if(num != 0) {
                    operations.addFirst(new Operation(Command.PUSH, num));
                    operations.addFirst(new Operation(Command.ADD));
                }
            }
        }

        if(pos == 0)
            generatePushDown(val);
        if(pos < blockWidth - 1)
            generatePushToLeft();
        else if(pos < blockWidth * 2 - 1)
            generatePushToRight();
        else
            generatePushDown(val);
    }

    private void generatePushDown(int val) {
        int x = 0;
        boolean xAsPos = true;
        while(val > 0) {
            if(x == collision) {
                x = 0;
                collision = -1;
                pushRow();
            }
            row[blockWidth - 1 - x].set(color);
            val--;
            x = (x + 1) % (blockWidth - 1);
            if(x == 0) {
                pushRow();
                xAsPos = false;
            }
        }
        if(xAsPos)
            pos = x;
        else
            pos = 9;
    }

    private void generatePushToLeft() {
        //1
        //1-6, 7-8, 9-12

        //2
        //check 1
        //1-4, 5, 6-11 (-10)

        //3
        //check 2
        //1-2, -, 3-10 (-8)

        //4
        //check 3
        //1-2, 3-9 (-6)
    }

    private void generatePushToRight() {

    }

    private boolean colorColision(int pixelBehind, int pixelFront, boolean single) {
        return true;
    }

    private void generatePointer(int val1, int val2) {

    }

    //Pusht die Reihe auf den Block
    private void pushRow() {
        block.add(row);
        row = new PietColor[blockWidth - 1];
        for(int i = 0; i < row.length; i++)
            row[i] = new PietColor(true, false);
    }

    public int getHeight() {
        return 0;
    }

    public void paint(BufferedImage image, int xBlockPos, int yBlockPos) {

    }
}

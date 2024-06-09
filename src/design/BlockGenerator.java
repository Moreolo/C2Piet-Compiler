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

    private int pos = 2;
    private PietColor color;
    private PietColor[] row;

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
        for(int i = 0; i < row.length; i++) {
            row[i] = new PietColor(true, false);
        }

        for(Operation operation: operations) {
            // Setzt die Pixel für die vorherige Operation
            // Das muss in dieser Reihenfolge gemacht werden damit die push und pointer
            // Operation richtig gezeichnet werden können
            Command c = operation.getName();
            if (c == Command.PUSH) {
                if (operation.getVal1() < 1)
                    throw new Error("Der Push Command braucht einen Wert höher als 0.");
                generatePush(operation.getVal1());
            } else if (c == Command.POINTER) {
                if (operation.getVal1() < 1 || operation.getVal2() < 1)
                    throw new Error("Der Pointer Command braucht Werte höher als 0.");
                generatePointer(operation.getVal1(), operation.getVal2());
            } else
                generateOtherOperation();
            // Verändert den Farbwert für den nächsten Pixel für die Operation
            switch (c) {
                case PUSH:
                    color.add(0, 1);
                    break;
                case POP:
                    color.add(0, 2);
                    break;
                case ADD:
                    color.add(1, 0);
                    break;
                case SUBTRACT:
                    color.add(1, 1);
                    break;
                case MULTIPLY:
                    color.add(1, 2);
                    break;
                case DIVIDE:
                    color.add(2, 0);
                    break;
                case MOD:
                    color.add(2, 1);
                    break;
                case NOT:
                    color.add(2, 2);
                    break;
                case GREATER:
                    color.add(3, 0);
                    break;
                case POINTER:
                    color.add(3, 1);
                    break;
                case SWITCH:
                    color.add(3, 2);
                    break;
                case DUPLICATE:
                    color.add(4, 0);
                    break;
                case ROLL:
                    color.add(4, 1);
                    break;
                case INNUMBER:
                    color.add(4, 2);
                    break;
                case INCHAR:
                    color.add(5, 0);
                    break;
                case OUTNUMBER:
                    color.add(5, 1);
                    break;
                case OUTCHAR:
                    color.add(5, 2);
                    break;
                default:
                    break;
            }
        }
        generateOtherOperation();
        block.add(row);
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
            pushRow();
            row[0].set(color);
            for(int i = 1; i < blockWidth - 1; i++) {
                row[i].setBlack();
            }
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
            for(int i = 0; i < blockWidth - 2; i++) {
                row[i].setBlack();
            }
            row[blockWidth - 2].set(color);
            pushRow();
        }
        //Erhöht Position der Operation
        pos = (pos + 1) % (blockWidth * 2 - 2);
    }

    private void generatePush(int val) {

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

    private boolean colorColision(int pixelBehind, int pixelFront) {
        return true;
    }

    private void generatePointer(int val1, int val2) {

    }

    //Pusht die Reihe auf den Block
    private void pushRow() {
        block.add(row);
        row = new PietColor[blockWidth - 1];
        for(int i = 0; i < row.length; i++) {
            row[i] = new PietColor(true, false);
        }
    }

    public int getHeight() {
        return 0;
    }

    public void paint(BufferedImage image, int xBlockPos, int yBlockPos) {

    }
}

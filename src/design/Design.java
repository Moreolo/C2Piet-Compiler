package design;

import java.awt.image.BufferedImage;
import java.util.LinkedList;

import piet.datatypes.*;

public class Design {
    // Die Breite und Höhe des Bilds
    int width, height;
    // Die akutelle Farbe (auch aktuelle Operation, weil der Pixel noch gesetzt
    // werden muss)
    PietColor currentColor;
    // Der aktuelle Block
    int currentBlock;
    // Die aktuelle Reihe innerhalb des Blocks
    int currentYOffset;
    // Die zusätzlichen Pixel nach oben (Wenn die Nummern der Blöcke größer als 13
    // werden, dann braucht man mehr Platz nach oben)
    int addedRowsTop;
    // Das Bild, in das der Piet Code übertragen wird
    BufferedImage image;

    // Erstellt ein kompaktes Bild
    public static BufferedImage compactParse(LinkedList<Block> blocks, boolean original) {
        // Generiert Blöcke
        BlockGenerator[] blockGenerators = new BlockGenerator[blocks.size()];
        for (int i = 0; i < blockGenerators.length; i++)
            blockGenerators[i] = new BlockGenerator(blocks.pop(), original);
        // Rechnet Breite und Höhe aus
        int width = blockGenerators.length * BlockGenerator.blockWidth + 2;
        int heightTop = 0;
        int heightBottom = 0;
        for (BlockGenerator blockGenerator : blockGenerators) {
            heightTop = Math.max(heightTop, blockGenerator.getHeightTop());
            heightBottom = Math.max(heightBottom, blockGenerator.getHeightBottom());
        }
        int height = heightTop + heightBottom;
        // Erstellt Bild
        BufferedImage image = new BufferedImage(width, height, BufferedImage.TYPE_INT_RGB);
        // Setzt alle Pixel auf weiß
        for (int x = 0; x < width; x++)
            for (int y = 0; y < height; y++)
                image.setRGB(x, y, PietColor.white);
        // Setzt Grundpixel
        // Start
        image.setRGB(0, 0, PietColor.darkRed);
        image.setRGB(1, 0, PietColor.lightRed);
        image.setRGB(6, 0, PietColor.black);
        // Ende
        image.setRGB(width - 1, heightTop - 5, PietColor.black);
        image.setRGB(width - 2, heightTop - 4, PietColor.black);
        image.setRGB(width - 1, heightTop - 4, PietColor.red);
        image.setRGB(width - 1, heightTop - 3, PietColor.red);
        image.setRGB(width - 2, heightTop - 2, PietColor.black);
        image.setRGB(width - 1, heightTop - 2, PietColor.red);
        image.setRGB(width - 1, heightTop - 1, PietColor.black);
        // Setzt Blöcke ins Bild
        for (int i = 0; i < blockGenerators.length; i++)
            blockGenerators[i].paint(image, i * BlockGenerator.blockWidth + 1, heightTop);
        return image;
    }

    // Wandelt eine Block Liste mit Piet Commands in ein Bild um
    public static BufferedImage parse(LinkedList<Block> blocks) throws Error {
        if (blocks.size() > 0)
            if (blocks.get(0).getNum() > 4)
                throw new Error("Der erste Block kann nur eine maximale Nummer von 4 haben.");

        Design design = new Design(blocks);
        // Iteriert durch die Blöcke
        for (int i = 0; i < blocks.size(); i++) {
            // Setzt aktuellen Block
            design.currentBlock = i + 1;
            // Setzt y offset zurück
            design.currentYOffset = 0;
            // Setzt Farbe auf dunkelgrün
            design.currentColor = new PietColor(2, 2);
            // Iteriert durch die Operationen
            for (Operation op : blocks.get(i).getOperations()) {
                design.paintOperation(op);
            }
            // Finale NOOP Operation
            // Wird benötigt, weil Piet Commands auf Farbübergängen ausgeführt werden
            design.paintOperation(new Operation(Command.NOOP));
        }
        return design.image;
    }

    // Initialisiert alle Variablen und setzt die konstanten Pixel
    public Design(LinkedList<Block> blocks) {
        currentColor = new PietColor(0, 0);
        currentBlock = 1;
        currentYOffset = 0;
        calcImageWidth(blocks);
        calcImageHeight(blocks);

        image = new BufferedImage(width, height, BufferedImage.TYPE_INT_RGB);
        // Setzt alle Pixel auf weiß
        for (int x = 0; x < width; x++)
            for (int y = 0; y < height; y++)
                image.setRGB(x, y, PietColor.white);

        paintNoBlockPixels(blocks);
    }

    // Rechnet die Breite des Bildes aus
    private void calcImageWidth(LinkedList<Block> blocks) {
        if (blocks.size() < 1) {
            // Ist kein Block in der Liste ist die Standardbreite 10
            width = 10;
        } else {
            // Jeder Block ist 7 breit plus 3 fixe Pixel
            width = 3 + (blocks.size() * 7);
        }

    }

    // Rechnet die Höhe des Bildes aus
    private void calcImageHeight(LinkedList<Block> blocks) {
        int max_block_height = 0;
        int max_block_num = 0;
        // Iteriert durch alle Blöcke
        for (Block block : blocks) {
            int block_height = 0;
            // Rechnet die Höhe aller Operationen im Block zusammen
            for (Operation op : block.getOperations()) {
                switch (op.getName()) {
                    case PUSH:
                        block_height += 1;
                        // Die Werte des Push Befehls halten pro Reihe maximal 6 Pixel
                        block_height += (op.getVal1() - 1) / 6;
                        break;
                    case POINTER:
                        // Der Pointer Befehl hat mindestens Höhe 4
                        block_height += 4;
                        // Die Werte des Pointer Befehls halten pro Reihe maximal 5 Pixel
                        block_height += (op.getVal1() - 1) / 5;
                        block_height += (op.getVal2() - 1) / 5;
                        break;
                    default:
                        block_height += 1;
                        break;
                }
            }
            // Überschreibt die maximale Blockhöhe falls größer
            max_block_height = Math.max(max_block_height, block_height);
            // Überschreibt die maximale Blocknummber falls größer
            max_block_num = Math.max(max_block_num, block.getNum());
        }
        // Rechnet die extra Reihen nach oben aus
        // Hängt von der Blocknummer ab
        addedRowsTop = Math.max(0, (max_block_num - 2) / 6 - 1);
        // Rechnet die Höhe zusammen plus 7 fixe Pixel
        height = 7 + addedRowsTop + max_block_height;
    }

    // Setzt alle Pixel die nicht Teil der Piet Commands der Blöcke sind
    private void paintNoBlockPixels(LinkedList<Block> blocks) {
        // Setzt Start Pixel links oben
        image.setRGB(4, 0, PietColor.black);
        image.setRGB(0, 1 + addedRowsTop, PietColor.black);
        image.setRGB(3, 4 + addedRowsTop, PietColor.black);
        image.setRGB(3, 3 + addedRowsTop, PietColor.darkGreen);

        // Setzt End Pixel rechts oben
        image.setRGB(width - 1, 0 + addedRowsTop, PietColor.black);
        image.setRGB(width - 2, 1 + addedRowsTop, PietColor.black);
        image.setRGB(width - 2, 3 + addedRowsTop, PietColor.black);
        image.setRGB(width - 1, 4 + addedRowsTop, PietColor.black);

        for (int y = 1; y <= 3; y++) {
            image.setRGB(width - 1, y + addedRowsTop, PietColor.red);
        }

        // Setzt Pixel links unten
        // Pixel sind notwendig um den Codel Chooser zu korrigieren
        image.setRGB(1, height - 2, PietColor.black);
        image.setRGB(0, height - 1, PietColor.green);
        image.setRGB(1, height - 1, PietColor.green);

        // Setzt alle Pixel oberhalb der Blöcke
        // Die Pixel überprüfen ob die Block Nummer mit dem aktuellen Wert auf dem Stack
        // übereinstimmt
        for (int i = 0; i < blocks.size(); i++) {
            // Rechnet die Position des Blocks aus
            int blockPosi = i * 7;

            // Erster Pixel
            image.setRGB(blockPosi + 2, 2 + addedRowsTop, PietColor.blue);
            // Duplicate
            image.setRGB(blockPosi + 3, 2 + addedRowsTop, PietColor.green);
            // Holt die Nummer des Blocks
            int blocknum = blocks.get(i).getNum();
            // Iteriert durch alle zu setzende Pixel durch
            for (int j = 0; j < blocknum - 1; j++) {
                // Rechnet den x und y Offset aus
                int xOffset = j % 6;
                int yOffset = j / 6;
                // Setzt den Pixel
                image.setRGB((blockPosi + 3) - xOffset, 1 + addedRowsTop - yOffset, PietColor.green);
            }

            // Push
            image.setRGB(blockPosi + 4, 2 + addedRowsTop, PietColor.darkGreen);
            // Subtract
            image.setRGB(blockPosi + 5, 2 + addedRowsTop, PietColor.lightCyan);
            // Not
            image.setRGB(blockPosi + 6, 2 + addedRowsTop, PietColor.darkMagenta);
            // Pointer
            image.setRGB(blockPosi + 7, 2 + addedRowsTop, PietColor.lightGreen);
            // Pop
            image.setRGB(blockPosi + 6, 3 + addedRowsTop, PietColor.darkGreen);
            image.setRGB(blockPosi + 7, 3 + addedRowsTop, PietColor.darkGreen);
            image.setRGB(blockPosi + 6, 4 + addedRowsTop, PietColor.black);
        }
    }

    // Setzt die Pixel passend zu der übergebenen Operation
    private void paintOperation(Operation operation) throws Error {
        // Setzt die Pixel für die vorherige Operation
        // Das muss in dieser Reihenfolge gemacht werden damit die push und pointer
        // Operation richtig gezeichnet werden können
        Command c = operation.getName();
        if (c == Command.PUSH) {
            if (operation.getVal1() < 1)
                throw new Error("Der Push Command braucht einen Wert höher als 0.");
            paintPush(operation.getVal1(), 6);
        } else if (c == Command.POINTER) {
            if (operation.getVal1() < 1 || operation.getVal2() < 1)
                throw new Error("Der Pointer Command braucht Werte höher als 0.");
            paintPointer(operation.getVal1(), operation.getVal2());
        } else
            paintPixel(0);
        // Erhöht den y offset für die Operation
        currentYOffset += 1;
        // Verändert den Farbwert für den nächsten Pixel für die Operation
        if (c != Command.POINTER)
            currentColor.add(c);
    }

    // Setzt den Pixel mit den akutellen Werten und der x Position im Block
    private void paintPixel(int xPos) {
        // xPos muss innerhalb des Blocks liegen
        if (xPos >= 0 && xPos <= 5)
            image.setRGB(currentBlock * 7 - xPos, 5 + addedRowsTop + currentYOffset,
                    currentColor.get());
    }

    // Setzt den Pixel mit der x Position im Block schwarz
    private void paintPixelBlack(int xPos) {
        image.setRGB(currentBlock * 7 - xPos, 5 + addedRowsTop + currentYOffset, PietColor.black);
    }

    // Setzt alle Pixel für die push Operation
    // width ist die maximal zulässige Breite des Push Blocks
    private void paintPush(int val1, int width) {
        // Iteriert durch die Werte
        for (int val = 0; val < val1; val++) {
            // Rechnet die x Position aus
            int xPos = val % width;
            // Bei einer neuen Zeile (außer der ersten) wird der yOffset erhöht
            if (xPos == 0 && val != 0)
                currentYOffset += 1;
            // Setzt den Pixel
            paintPixel(xPos);
        }
    }

    // Setzt alle Pixel für die pointer Operation
    private void paintPointer(int val1, int val2) {
        // yOffset speichern für setzen der schwarzen und push Pixel
        int startYOffset = currentYOffset;

        // Pixel von vorher setzen und current auf pointer Operation setzen
        paintPixel(0);
        currentYOffset += 1;
        currentColor.add(3, 1);

        // val1 setzen
        paintPush(val1, 5);
        // yOffset erhöhen
        currentYOffset += 2;
        // val2 setzen
        paintPush(val2, 5);

        // Breite und Höhe ausrechnen der push Blöcke
        int width1 = Math.min(val1, 5);
        int width2 = Math.min(val2, 5);
        int height1 = (val1 - 1) / 5 + 1;
        int height2 = (val2 - 1) / 5 + 1;
        // yOffset zurücksetzen und Operation auf push setzen
        currentYOffset = startYOffset;
        currentColor.add(0, 1);
        // Setzt den oberen Pixel schwarz, damit Pointer nicht nach oben geht
        paintPixelBlack(width1);
        currentYOffset += 1;

        // Falls nötig, setzt den links oberen Pixel schwarz, damit der Pointer nicht
        // nach links geht
        if (width1 >= width2)
            paintPixelBlack(width1 + 1);
        // Iteriert durch die Höhe des ersten Push blocks und setzt die Pixel
        for (int h = 0; h < height1; h++) {
            paintPixel(width1);
            currentYOffset += 1;
        }

        // Falls nötig, setzt den links unteren bzw. oberen Pixel schwarz, damit der
        // Pointer nicht nach links geht
        if (width1 > width2)
            paintPixelBlack(width1 + 1);
        else if (width2 > width1)
            paintPixelBlack(width2 + 1);

        // Setzt die Leiste zwischen den Push blocks
        // Richtung der Leiste
        int dir = 1;
        if (width1 > width2)
            dir = -1;
        // Iteriert durch die Leiste
        for (int w = width1; w != width2; w += dir) {
            paintPixel(w);
        }
        // Setzt den letzten Pixel der Leiste
        paintPixel(width2);
        currentYOffset += 1;

        // Iteriert durch die Höhe des zweiten Push blocks und setzt die Pixel
        for (int h = 0; h < height2; h++) {
            paintPixel(width2);
            currentYOffset += 1;
        }

        // Falls nötig, setzt den links unteren Pixel schwarz, damit der Pointer nicht
        // nach links geht
        if (width2 >= width1)
            paintPixelBlack(width2 + 1);
        // Setzt die Leiste nach dem zweiten Push Block
        for (int w = width2; w > 0; w--) {
            paintPixel(w);
        }
        // Setzt den rechten Pixel schwarz, damit der Pointer nicht nach rechts geht
        paintPixelBlack(-1);
        // Der letzte Pixel der Leiste wird nicht gesetzt, da er bei der letzten NOOP
        // Operation gesetzt wird
        // Dafür muss yOffset nochmal kleiner gemacht werden
        currentYOffset -= 1;
    }
}

package design;

import java.awt.image.BufferedImage;
import java.util.LinkedList;

import piet.datatypes.*;

public class Design {

    // Farben
    // Rot
    int red = (255 << 16) | (0 << 8) | 0;
    int lightRed = (255 << 16) | (192 << 8) | 192;
    int darkRed = (192 << 16) | (0 << 8) | 0;

    // Gelb
    int yellow = (255 << 16) | (255 << 8) | 0;
    int lightYellow = (255 << 16) | (255 << 8) | 192;
    int darkYellow = (192 << 16) | (192 << 8) | 0;

    // Grün
    int green = (0 << 16) | (255 << 8) | 0;
    int lightGreen = (192 << 16) | (255 << 8) | 192;
    int darkGreen = (0 << 16) | (192 << 8) | 0;

    // Cyan
    int cyan = (0 << 16) | (255 << 8) | 255;
    int lightCyan = (192 << 16) | (255 << 8) | 255;
    int darkCyan = (0 << 16) | (192 << 8) | 192;

    // Blau
    int blue = (0 << 16) | (0 << 8) | 255;
    int lightBlue = (192 << 16) | (192 << 8) | 255;
    int darkBlue = (0 << 16) | (0 << 8) | 192;

    // Magenta
    int magenta = (255 << 16) | (0 << 8) | 255;
    int lightMagenta = (255 << 16) | (192 << 8) | 255;
    int darkMagenta = (192 << 16) | (0 << 8) | 192;

    // Weiß
    int white = (255 << 16) | (255 << 8) | 255;

    // Schwarz
    int black = (0 << 16) | (0 << 8) | 0;

    // verschachteltes FarbenArray
    int[][] matrixOfColor = {
            { lightRed, red, darkRed },
            { lightYellow, yellow, darkYellow },
            { lightGreen, green, darkGreen },
            { lightCyan, cyan, darkCyan },
            { lightBlue, blue, darkBlue },
            { lightMagenta, magenta, darkMagenta },

    };

    LinkedList<Block> ops;
    int width, height, currentHue, currentShade, currentBlock, currentYOffset;
    BufferedImage image;

    public static BufferedImage parse(LinkedList<Block> blocks) {
        Design design = new Design(blocks);
        //TODO
        //loop erstellen mit block und operation iteration
        //paint noop
        //current block anpassen
        //möglicherweise current hue und shade zurücksetzen
        return design.image;
    }

    //Initialisiert alle Variablen und setzt die konstanten Pixel
    public Design(LinkedList<Block> blocks) {
        currentHue = 0;
        currentShade = 0;
        currentBlock = 1;
        currentYOffset = 0;
        ops = blocks;
        calcImageWidth(blocks);
        calcImageHeight(blocks);

        image = new BufferedImage(width, height, BufferedImage.TYPE_INT_RGB);
        paintNoBlockPixels();
    }

    private void calcImageWidth(LinkedList<Block> blocks) {
        //TODO
        //calculate the required image width
        width = 100;
    }

    private void calcImageHeight(LinkedList<Block> blocks) {
        //TODO
        //calculate the required image height
        //aufpassen auf platz nach oben
        //dafür auch variable anpassen für yoffset oder so
        height = 100;
    }

    private void paintNoBlockPixels() {
        //TODO: Sarah
        //Malt alle Pixel, die nicht zu den Blöcken gehören
        //das sind unten links, oben links und finish oben rechts
        //auch Block Leiste oben Überprüfung
    }

    //Setzt die Pixel passend zu der übergebenen Operation
    private void paintOperation(Operation operation) {
        //Setzt die Pixel für die vorherige Operation
        //Das muss in dieser Reihenfolge gemacht werden damit die push und pointer Operation richtig gezeichnet werden können
        Command c = operation.getName();
        if(c == Command.PUSH)
            paintPush(operation.getVal1(), 6);
        else if(c == Command.POINTER)
            paintPointer(operation.getVal1(), operation.getVal2());
        else
            paintPixel(0);
        //Erhöht den y offset für die Operation
        currentYOffset += 1;
        //Verändert den Farbwert für den nächsten Pixel für die Operation
        switch (c) {
            case PUSH:
                addColor(0, 1);
                break;
            case POP:
                addColor(0, 2);
                break;
            case ADD:
                addColor(1, 0);
                break;
            case SUBTRACT:
                addColor(1, 1);
                break;
            case MULTIPLY:
                addColor(1, 2);
                break;
            case DIVIDE:
                addColor(2, 0);
                break;
            case MOD:
                addColor(2, 1);
                break;
            case NOT:
                addColor(2, 2);
                break;
            case GREATER:
                addColor(3, 0);
                break;
            case POINTER:
                //Die Farbe wird bereits in der paintPointer Funktion angepasst
                break;
            case SWITCH:
                addColor(3, 2);
                break;
            case DUPLICATE:
                addColor(4, 0);
                break;
            case ROLL:
                addColor(4, 1);
                break;
            case INNUMBER:
                addColor(4, 2);
                break;
            case INCHAR:
                addColor(5, 0);
                break;
            case OUTNUMBER:
                addColor(5, 1);
                break;
            case OUTCHAR:
                addColor(5, 2);
                break;
            default:
                break;
        }
    }

    //Setzt den Pixel mit den akutellen Werten und der x Position im Block
    private void paintPixel(int xPos) {
        //xPos muss innerhalb des Blocks liegen
        if(xPos >= 0 && xPos <= 5)
            image.setRGB(currentBlock * 7 - xPos, 5 + currentYOffset, matrixOfColor[currentHue][currentShade]);
    }

    //Setzt den Pixel mit der x Position im Block schwarz
    private void paintPixelBlack(int xPos) {
        image.setRGB(currentBlock * 7 - xPos, 5 + currentYOffset, black);
    }

    //Setzt alle Pixel für die push Operation
    //width ist die maximal zulässige Breite des Push Blocks
    private void paintPush(int val1, int width) {
        //Iteriert durch die Werte
        for(int val = 0; val < val1; val++) {
            //Rechnet die x Position aus
            int xPos = val % width;
            //Bei einer neuen Zeile (außer der ersten) wird der yOffset erhöht
            if(xPos == 0 && val != 0)
                currentYOffset += 1;
            //Setzt den Pixel
            paintPixel(xPos);
        }
    }

    //Setzt alle Pixel für die pointer Operation
    private void paintPointer(int val1, int val2) {
        //yOffset speichern für setzen der schwarzen und push Pixel
        int startYOffset = currentYOffset;

        //Pixel von vorher setzen und current auf pointer Operation setzen
        paintPixel(0);
        currentYOffset += 1;
        addColor(3, 1);

        //val1 setzen
        paintPush(val1, 5);
        //yOffset erhöhen
        currentYOffset += 2;
        //val2 setzen
        paintPush(val2, 5);

        //Breite und Höhe ausrechnen der push Blöcke
        int width1 = Math.min(val1, 5);
        int width2 = Math.min(val2, 5);
        int height1 = (val1 - 1) / 5 + 1;
        int height2 = (val2 - 1) / 5 + 1;
        //yOffset zurücksetzen und Operation auf push setzen
        currentYOffset = startYOffset;
        addColor(0, 1);
        //Setzt den oberen Pixel schwarz, damit Pointer nicht nach oben geht
        paintPixelBlack(width1);
        currentYOffset += 1;

        //Falls nötig, setzt den links oberen Pixel schwarz, damit der Pointer nicht nach links geht
        if(width1 >= width2)
            paintPixelBlack(width1 + 1);
        //Iteriert durch die Höhe des ersten Push blocks und setzt die Pixel
        for(int h = 0; h < height1; h++) {
            paintPixel(width1);
            currentYOffset += 1;
        }

        //Falls nötig, setzt den links unteren bzw. oberen Pixel schwarz, damit der Pointer nicht nach links geht
        if(width1 > width2)
            paintPixelBlack(width1 + 1);
        else if(width2 > width1)
            paintPixelBlack(width2 + 1);

        //Setzt die Leiste zwischen den Push blocks
        //Richtung der Leiste
        int dir = 1;
        if(width1 > width2)
            dir = -1;
        //Iteriert durch die Leiste
        for(int w = width1; w != width2; w += dir) {
            paintPixel(w);
        }
        //Setzt den letzten Pixel der Leiste
        paintPixel(width2);
        currentYOffset += 1;

        //Iteriert durch die Höhe des zweiten Push blocks und setzt die Pixel
        for(int h = 0; h < height2; h++) {
            paintPixel(width2);
            currentYOffset += 1;
        }

        //Falls nötig, setzt den links unteren Pixel schwarz, damit der Pointer nicht nach links geht
        if(width2 >= width1)
            paintPixelBlack(width2 + 1);
        //Setzt die Leiste nach dem zweiten Push Block
        for(int w = width2; w > 0; w--) {
            paintPixel(w);
        }
        //Setzt den rechten Pixel schwarz, damit der Pointer nicht nach rechts geht
        paintPixelBlack(-1);
        //Der letzte Pixel der Leiste wird nicht gesetzt, da er bei der letzten NOOP Operation gesetzt wird
        //Dafür muss yOffset nochmal kleiner gemacht werden
        currentYOffset -= 1;
    }

    //Passt die aktuelle Farbe an, sodass eine Piet Operation leicht in die passende Farbe übersetzt werden kann
    private void addColor(int hue, int shade) {
        //Ein Wert wird addiert und es wird der passende Modulo genommen (Die Farben der Operationen sind zyklisch)
        currentHue = (currentHue + hue) % 6;
        currentShade = (currentShade + shade) % 3;
    }
}

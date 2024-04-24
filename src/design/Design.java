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

    LinkedList<Operation> ops = new LinkedList<Operation>();

    int currentHue, currentShade, currentBlock, currentYOffset;
    BufferedImage image;

    public Design() {
        currentHue = 0;
        currentShade = 0;
        currentBlock = 1;
        currentYOffset = 0;

        image = new BufferedImage(100, 100, BufferedImage.TYPE_INT_RGB);
    }
    public static void parse(LinkedList<Block> blocks) {
    }

    private void opIter() {
        for (Operation name : ops) {
            paintOperation(name);
        }
        ;
    }

    private void paintOperation(Operation operation) {
        switch (operation.getName()) {
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
                addColor(3, 1);
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
        image.setRGB(currentBlock * 7, 5 + currentYOffset, matrixOfColor[currentHue][currentShade]);
        currentYOffset += 1;
    }

    private void addColor(int hue, int shade) {
        currentHue = (currentHue + hue) % 6;
        currentShade = (currentShade + shade) % 3;
    }
}

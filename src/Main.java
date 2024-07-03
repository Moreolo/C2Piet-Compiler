import java.io.File;
import java.io.IOException;

import javax.imageio.ImageIO;

import java.awt.image.BufferedImage;

import ast.lexer.Lexer;
import basicblocks.BBMain;
import design.Design;
import piet.datatypes.Block;
import piet.Piet;

import java.util.LinkedList;

public class Main {
    public static void main(String[] args) {
        if (args.length < 1) {
            System.out.println("Please enter a valid c file");
            return;
        }
        int version = 1;
        if (args.length > 1) {
            if(args[1].equals("-b"))
                version = 0;
            else if(args[1].equals("-c"))
                version = 1;
            else if(args[1].equals("-a"))
                version = 2;
        }
        BufferedImage image;
        try {
            LinkedList<Block> blocks = Piet.parse(BBMain.parse(Lexer.runFile(args[0])));
            if (version == 0)
                image = Design.parse(blocks);
            else if (version == 2)
                image = Design.compactParse(blocks, false);
            else
                image = Design.compactParse(blocks, true);
        } catch (IOException e) {
            System.out.println("Please enter a valid c file");
            return;
        }
        File out = new File("piet.png");
        try {
            ImageIO.write(image, "png", out);
        } catch (IOException e) {
            System.out.println("Failed to export png file");
        }
    }
}

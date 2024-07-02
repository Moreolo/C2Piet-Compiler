import java.io.File;
import java.io.IOException;

import javax.imageio.ImageIO;

import java.awt.image.BufferedImage;

import ast.lexer.Lexer;
import design.Design;
import piet.Piet;

public class Main {
    public static void main(String[] args) {
   if (args.length < 1) {
            System.out.println("Please enter a valid c file");
            return;
        }
        BufferedImage image;
        try {
            image = Design.parse(Piet.parse(BBMain.parse(Lexer.runFile(args[0]))));
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

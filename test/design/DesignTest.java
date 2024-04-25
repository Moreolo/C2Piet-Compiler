package design;

import org.junit.Test;

import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.util.LinkedList;

import javax.imageio.ImageIO;

import piet.datatypes.*;

public class DesignTest {
    
    @Test
    public void testParse() {
        LinkedList<Block> blocks = new LinkedList<>();

        Block block1 = new Block(1);
        block1.addOperation(new Operation(Command.INNUMBER));
        block1.addOperation(new Operation(Command.INNUMBER));
        block1.addOperation(new Operation(Command.PUSH, 1));
        block1.addOperation(new Operation(Command.DUPLICATE));
        block1.addOperation(new Operation(Command.SUBTRACT));
        block1.addOperation(new Operation(Command.PUSH, 2));
        blocks.add(block1);

        Block block2 = new Block(2);
        block2.addOperation(new Operation(Command.PUSH, 3));
        block2.addOperation(new Operation(Command.PUSH, 2));
        block2.addOperation(new Operation(Command.ROLL));
        block2.addOperation(new Operation(Command.DUPLICATE));
        block2.addOperation(new Operation(Command.PUSH, 4));
        block2.addOperation(new Operation(Command.PUSH, 1));
        block2.addOperation(new Operation(Command.ROLL));
        block2.addOperation(new Operation(Command.PUSH, 1));
        block2.addOperation(new Operation(Command.DUPLICATE));
        block2.addOperation(new Operation(Command.SUBTRACT));
        block2.addOperation(new Operation(Command.GREATER));
        block2.addOperation(new Operation(Command.POINTER, 3, 4));
        blocks.add(block2);

        Block block3 = new Block(3);
        block3.addOperation(new Operation(Command.PUSH, 2));
        block3.addOperation(new Operation(Command.PUSH, 1));
        block3.addOperation(new Operation(Command.ROLL));
        block3.addOperation(new Operation(Command.DUPLICATE));
        block3.addOperation(new Operation(Command.PUSH, 3));
        block3.addOperation(new Operation(Command.PUSH, 1));
        block3.addOperation(new Operation(Command.ROLL));
        block3.addOperation(new Operation(Command.ADD));
        block3.addOperation(new Operation(Command.PUSH, 3));
        block3.addOperation(new Operation(Command.PUSH, 2));
        block3.addOperation(new Operation(Command.ROLL));
        block3.addOperation(new Operation(Command.PUSH, 1));
        block3.addOperation(new Operation(Command.SUBTRACT));
        block3.addOperation(new Operation(Command.PUSH, 3));
        block3.addOperation(new Operation(Command.PUSH, 1));
        block3.addOperation(new Operation(Command.ROLL));
        block3.addOperation(new Operation(Command.PUSH, 2));
        blocks.add(block3);

        Block block4 = new Block(4);
        block4.addOperation(new Operation(Command.DUPLICATE));
        block4.addOperation(new Operation(Command.OUTNUMBER));
        block4.addOperation(new Operation(Command.PUSH, 1));
        block4.addOperation(new Operation(Command.DUPLICATE));
        block4.addOperation(new Operation(Command.SUBTRACT));
        blocks.add(block4);

        BufferedImage image = Design.parse(blocks);

        try {
            File outFile = new File("manmult.png");
            ImageIO.write(image, "png", outFile);
        } catch(IOException e) {

        }
    }
}

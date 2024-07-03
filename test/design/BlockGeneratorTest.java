package design;

import org.junit.Test;

import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.util.LinkedList;

import javax.imageio.ImageIO;

import piet.datatypes.*;

public class BlockGeneratorTest {
    @Test
    public void testGenerateBlock() {
        Block block = new Block(3);
        block.addOperation(new Operation(Command.PUSH, 2));
        block.addOperation(new Operation(Command.PUSH, 1));
        block.addOperation(new Operation(Command.ROLL));
        block.addOperation(new Operation(Command.DUPLICATE));
        block.addOperation(new Operation(Command.PUSH, 3));
        block.addOperation(new Operation(Command.PUSH, 1));
        block.addOperation(new Operation(Command.ROLL));
        block.addOperation(new Operation(Command.ADD));
        block.addOperation(new Operation(Command.PUSH, 3));
        block.addOperation(new Operation(Command.PUSH, 2));
        block.addOperation(new Operation(Command.ROLL));
        block.addOperation(new Operation(Command.PUSH, 1));
        block.addOperation(new Operation(Command.SUBTRACT));
        block.addOperation(new Operation(Command.PUSH, 3));
        block.addOperation(new Operation(Command.PUSH, 1));
        block.addOperation(new Operation(Command.ROLL));
        block.addOperation(new Operation(Command.POINTER, 0, -20));

        BlockGenerator blockGenerator = new BlockGenerator(block, false);
        LinkedList<PietColor[]> colorList = blockGenerator.getBlock();
        BufferedImage image = new BufferedImage(BlockGenerator.blockWidth - 1, colorList.size(), BufferedImage.TYPE_INT_RGB);
        for(int y = 0; !colorList.isEmpty(); y++) {
            PietColor[] row = colorList.pop();
            for(int x = 0; x < BlockGenerator.blockWidth - 1; x++)
                image.setRGB(x, y, row[x].get());
        }

        try {
            File outFile = new File("block.png");
            ImageIO.write(image, "png", outFile);
        } catch (IOException e) {

        }
    }
}

package design;

import java.awt.image.BufferedImage;
import java.util.LinkedList;

import piet.datatypes.Block;
import piet.datatypes.Operation;

public class BlockGenerator {
    public static int blockWidth = 6;

    private LinkedList<PietColor[]> chooser;
    private LinkedList<PietColor[]> block;

    public BlockGenerator(Block block) {
        this.chooser = new LinkedList<>();
        this.block = new LinkedList<>();
        generateChooser(block.getNum());
        generateBlock(block.getOperations());
    }

    private void generateChooser(int blockNum) {

    }

    private void generateBlock(LinkedList<Operation> operations) {

    }

    public int getHeight() {
        return 0;
    }

    public void paint(BufferedImage image, int xBlockPos, int yBlockPos) {

    }
}

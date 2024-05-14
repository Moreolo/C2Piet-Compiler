package basicblocks;

import basicblocks.datatypes.*;
import ast.datatypes.*;

import java.util.ArrayList;


public class BBMain {

    private static ArrayList<BBlock> blockList = new ArrayList<>();
    private static int iterator = 0;


    private static BBlock splitBlock(BBlock block, ArrayList<Node> newBody) {
        //neuen Block erstellen
        Integer oldNext = block.getNext();
        BBlock newBlock = new BBlock(++iterator);
        ArrayList<Node> oldBody = block.getBody();
        oldBody.removeAll(newBody);
        newBlock.setBody(oldBody);
        blockList.add(newBlock);
        block.setBody(newBody);
        block.setNext(iterator);
        //next pointer des alten blocks auslesen
        if (oldNext != null){
            //nach dem alten Block ist noch ein Block
            //->wir fügen mitten in der Kette einen neuen Block ein -> pointer umstecken
            newBlock.setNext(oldNext);
        }
        return newBlock;
    }
    private static ArrayList<BBlock> createFork(BBlock oldBBlock, ArrayList<Node> leftPath, ArrayList<Node> rightPath){
        BBlock leftBlock = new BBlock(++iterator);
        blockList.add(leftBlock);
        leftBlock.setBody(leftPath);
        CondBlock replaceOld = new CondBlock(oldBBlock.getPositionInArray());
        int position = oldBBlock.getPositionInArray();
        blockList.set(position, replaceOld);
        replaceOld.setNext(leftBlock.getPositionInArray());
        ArrayList<BBlock> paths = new ArrayList<>();
        paths.add(leftBlock);
        if (!rightPath.isEmpty()){
            BBlock rightBlock = new BBlock(++iterator);
            rightBlock.setBody(rightPath);
            blockList.add(rightBlock);
            replaceOld.setNext2(rightBlock.getPositionInArray());
            paths.add(rightBlock);
        }
        return paths;
    }

    /*
     * Block-Statements
     * Cond-Statements
     * Function-Block
     * 
     */

     /*
      * Für AST-Team:
      es muss der deklarierungsvorgang als Type definiert werden, wegen Scope-Problemen, vor allem bei Funktionsaufrufen.

      Beispiel:     { x = x + 1;      -> Body darf kein Set sein, muss ne Liste sein
                    x = x + 1; }      -> auch wegen Ordnung
      */

      /* ----------------------- GEDANKEN-WIESE -------------------------------------------------
       * if (cond1) {
       * 
       *    do;
       * 
       *    if (cond 2) {
       * 
       *        foo;
       *        faa2;
       *    }
       * 
       *    faa;
       *   
       * }
       * 
       * resultList[];
       * int start = 0;
       * arr[1,2,3,4,5]
       * 
       * for (i = 0; i < arr.length; i++) {
       *    
       *    if (arr[i] % 2 == 0) {
       *        int a = 0;
       *        for( ; start < i; start++) {
       *            a += arr[start];
       *        }
       * 
       *        resultList.add(a);
       *        start++;
       *        
       *    }
       * }
       * 
       * if (arr.getLast() % 2 != 0) {
       *    if (arr[i] % 2 == 0) {
       *        int a = 0;
       *        for( ; start < i; start++) {
       *            a += arr[start];
       *        }
       * 
       *        resultList.add(a);
       *        start++;
       *        
       *    }
       * }y
       */

    // Grundlegende Idee ist der rekursive durchlauf durch den Node-Baum
    private static void walkTree(BBlock blockOfCode) {
        ArrayList<Node> nodesForOneBasicBlock = new ArrayList<>();
        boolean everythingSimple = true;
        ArrayList<Node> nodeList = blockOfCode.getBody();
        int counter = 0;
        for(Node currentNode :nodeList){ 
            counter = counter + 1;
            switch (currentNode.getType()) {  
                case WHILE_STATEMENT:
                case IF_STATEMENT:
                    if (!nodesForOneBasicBlock.isEmpty()){
                        BBlock nextBlockLookInto = splitBlock(blockOfCode, nodesForOneBasicBlock);
                        walkTree(nextBlockLookInto);
                        return;
                    }
                    if (counter != nodeList.size()){
                        int positionOfOldBlock = blockOfCode.getPositionInArray();
                        ArrayList<Node> ifBlock = new ArrayList<>();
                        ifBlock.add(currentNode);
                        BBlock nextBlockLookInto = splitBlock(blockOfCode, ifBlock);
                        walkTree(blockList.get(positionOfOldBlock));
                        walkTree(nextBlockLookInto);
                        return;
                    }
                    everythingSimple = false; //unsicher ob wir das brauchen, wenn nach if noch was kommt
                    Integer oldNext = blockOfCode.getNext();
                    Node leftNode = currentNode.getLeft();
                    Node rightNode = currentNode.getRight();
                    ArrayList<BBlock> paths;
                    if (rightNode != null){
                        paths = createFork(blockOfCode, leftNode.getBody(), rightNode.getBody());
                    }else{
                        paths = createFork(blockOfCode, leftNode.getBody(), new ArrayList<>());
                    }

                    if (oldNext != null){
                        for (BBlock path: paths){
                            path.setNext(oldNext);
                        }
                    
                    }
                    for (BBlock path: paths){
                        walkTree(path);
                    }
                    return;



    
 
    
                
            
                default:
                    nodesForOneBasicBlock.add(currentNode);
                    break;
            }
        }

        
    }



    // Aufruf für die "Interface"-Abfolge.
    public static ArrayList<BBlock> parse(Node rootNode) {
        BBlock starterBlock = new BBlock(0);
        blockList.add(starterBlock);
        starterBlock.addToBody(rootNode);
        walkTree(starterBlock);

        return blockList;
    }
}

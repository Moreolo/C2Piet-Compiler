package basicblocks;

import basicblocks.datatypes.*;
import ast.datatypes.*;

import java.util.ArrayList;


public class BBMain {

    private static ArrayList<BBlock> blockList = new ArrayList<>();
    private static int iterator = 1;


    private static BBlock splitBlock(BBlock oldBlock, ArrayList<Node> stayBehind) {
        //die alten Nodes im alten Block werden aufgeteil:
        //stayBehind Teil bleibt im alten Block während alle anderen Nodes 
        //in den neuen Block wandern
        Integer oldNext = oldBlock.getNext();
        BBlock newBlock = new BBlock(++iterator);
        ArrayList<Node> oldBody = oldBlock.getBody();
        oldBody.removeAll(stayBehind);
        newBlock.setBody(oldBody);
        blockList.add(newBlock);
        oldBlock.setBody(stayBehind);
        oldBlock.setNext(iterator);
        //next pointer des alten blocks auslesen
        if (oldNext != null){
            //nach dem alten Block ist noch ein Block
            //->wir fügen mitten in der Kette einen neuen Block ein -> pointer umstecken
            newBlock.setNext(oldNext);
        }
        return newBlock;
    }
    private static ArrayList<BBlock> createFork(BBlock oldBBlock, ArrayList<Node> leftPath, ArrayList<Node> rightPath, Node condition){
        //alter Block wir mit CondBlock ersetzt und mit condition node befüllt
        //ein bzw. zwei neue Blöcke werden erstellt für true bzw. else Blöcke 
        //diese werden mit den jeweiligen Nodes für diese Blöcke befüllt 
        BBlock leftBlock = new BBlock(++iterator);
        blockList.add(leftBlock);
        leftBlock.setBody(leftPath);
        CondBlock replaceOld = new CondBlock(oldBBlock.getPositionInArray());
        replaceOld.addToBody(condition);
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

            Terminator-Block hat keinen next -> 
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

                    Integer oldNext = blockOfCode.getNext();
                    Node leftNode = currentNode.getLeft();
                    Node rightNode = currentNode.getRight();
                    Node condition = currentNode.getCondition();
                    ArrayList<BBlock> paths;
                    if (rightNode != null){
                        paths = createFork(blockOfCode, leftNode.getBody(), rightNode.getBody(), condition);
                    }else{
                        paths = createFork(blockOfCode, leftNode.getBody(), new ArrayList<>(), condition);
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
        starterBlock.addNodeToBody(rootNode);
        walkTree(starterBlock);

        return blockList;
    }
}

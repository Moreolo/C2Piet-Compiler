package basicblocks;

import basicblocks.datatypes.*;
import ast.datatypes.*;

import java.util.ArrayList;


public class BBMain {

    private static ArrayList<BBlock> blockList = new ArrayList<>();
    private static int iterator = 1;



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
    private static void walkTree(Node rootNode) {
        switch (rootNode.getType()) {
            case WHILE_STATEMENT:
            case IF_STATEMENT:

                CondBlock condBlock = new CondBlock(iterator++);
                condBlock.addToBody(rootNode.getCondition());
                blockList.add(condBlock);

                walkTree(rootNode.getLeft());
                condBlock.setNext2(iterator - 1);
                BBlock lastBlockInList = blockList.get(blockList.size()-1);
                // Unterscheidung, ob else-Block benötig wird.
                if (rootNode.getRight() != null) {
                    walkTree(rootNode.getRight());

                }
                lastBlockInList.setNext(iterator);
                break;

            case BLOCK_STATEMENT:
            // TODO: Es muss unterschieden werden, ob innerhalb des BLOCK_STATEMENTS irgendwelche nicht-atomaren instruktionen 
            // vorkommen, da hierfür die Blockstruktur aufgetrennt werden muss.
                ArrayList<Node> nodesForOneBLock = new ArrayList<>();

                for (Node n : rootNode.getBody()) {
                    if (n.getType().equals(NodeTypesEnum.IF_STATEMENT)) {
                        if (!nodesForOneBLock.isEmpty()){
                            BBlock newBlock = new BBlock(iterator++);
                            newBlock.setBody(nodesForOneBLock);
                            blockList.add(newBlock);
                            nodesForOneBLock.clear();
                        }
                        walkTree(n);
                    }else{
                        nodesForOneBLock.add(n);
                    }  
                }

                if (!nodesForOneBLock.isEmpty()){
                    BBlock newBlock = new BBlock(iterator++);
                    newBlock.setBody(nodesForOneBLock);
                    blockList.add(newBlock);
                    nodesForOneBLock.clear();
                }

            
        
            default:
                //faa
                break;
        }
    }



    // Aufruf für die "Interface"-Abfolge.
    public static ArrayList<BBlock> parse(Node rootNode) {
        walkTree(rootNode);

        return blockList;
    }
}

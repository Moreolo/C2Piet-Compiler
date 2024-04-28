package basicblocks;

import basicblocks.datatypes.*;
import ast.datatypes.*;

import java.util.ArrayList;


public class BBMain {

    private static ArrayList<BBlock> blockList = new ArrayList<>();
    private static int iterator = 0;



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
       * }
       */

    // Grundlegende Idee ist der rekursive durchlauf durch den Node-Baum
    private static void walkTree(Node rootNode) {
        switch (rootNode.getType()) {
            case WHILE_STATEMENT:
            case IF_STATEMENT:

                walkTree(rootNode.getLeft());
                // Unterscheidung, ob else-Block benötig wird.
                if (rootNode.getRight() != null) {
                    walkTree(rootNode.getRight());

                }
                break;

            case BLOCK_STATEMENT:
            // TODO: Es muss unterschieden werden, ob innerhalb des BLOCK_STATEMENTS irgendwelche nicht-atomaren instruktionen 
            // vorkommen, da hierfür die Blockstruktur aufgetrennt werden muss.

                int start = 0;
                int stop = 0;
                boolean ifFound = false;

                for (Node n : rootNode.getBody()) {
                    if (n.getType().equals(NodeTypesEnum.IF_STATEMENT)) {

                        ifFound = true;

                        BBlock block = new BBlock(iterator++);
                        //Baue baum von start bis stop
                        for ( ; start < stop; start++) {
                           // block.addToBody(rootNode.getBody().get(start);
                        }

                        // Laufe in den If-Block hinein
                        walkTree(n);
                        start++;
                    }  
                    stop++;
                }

                if (!ifFound) {
                    if (rootNode.getBody().size() == stop && start != stop) {
                        stop--;
                    }
                    BBlock block = new BBlock(iterator++);
                    //Baue baum von start bis stop
                    for ( ; start <= stop; start++) {
                      //  block.addToBody(rootNode.getBody().get(start);
                    } 

                } else {

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

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
       * }
       * 
       * 
       * 
       * 
       * Prinzipiell benötigen wir für die Basic-Blöcke "nur" 2 Funktionalitäten: Einen Conditional Block anlegen und einen linearen Block spalten.
       * Der Algorithmus soll rekursiv durch den Baum iterieren.
       * Wenn der Algorithmus einen Sprung bemerkt, legt er direkt einen neuen Block an und verknüpft sie jeweils.
       * Wenn der Algorithmus dann bemerkt, dass es sich um ein Conditional handelt, dann "spaltet" er die blöcke erneut auf und legt diesmal eine abgesprochene 4er-Konstellation an
       * 
       *                1. split():
       * 
       *                    ( B1 ) ==> ( B1 ) -> ( B2 )
       * 
       *                2. createConditional():
       *                    
       *                                ( Bx+3 ) ----           ( Bx+7 ) -----------              
       *                                   ^        |               ^               |
       *                                   |        v               |               v
       *                    ( Bx ) ==>  ( Bx )      ( Bx+1 ) -> ( Bx + 4 )      ( Bx + 5)
       *                                   |        ^               |               ^
       *                                   v        |               v               |
       *                                ( Bx+2 ) ----            ( Bx+6) -----------
       */
    private void createCondtitional() {

        // ist ein standard-split am Anfang, der den Start- und End-Block des gesamten Conditional definiert 

        // Je nachdem, ob die Left- und/oder Right-Nodes vergeben sind, müssen zusätzliche splits vom Ursprungsblock aus gemacht werden
        // Diese Blöcke zeigen dann auf den zuvor angelegten End-Block


    }

    private void splitBlock(BBlock block, Node newRoot) {

        // Es muss geprüft werden, ob das Next-element vom block vergeben ist.

        // Wenn es nicht vergeben ist, handelt es sich um einen normalen linearen jump 
        //  -> neuer block mit iterator + 1, das next vom Ursprungsblock wird auf den iterator gesetzt

        // Wenn das next bereits vergeben wurde, dann handelt es sich um einen jump innerhalb eines Conditionals
        //  -> Der next-Wert wird in den neuen Basic Block gesetzt, 
        //      der next-Wert vom alten Block wird mit dem iterator vom neuen Block überschrieben
    }

    // Grundlegende Idee ist der rekursive durchlauf durch den Node-Baum
    private static void walkTree(Node rootNode) {}


/*
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

                ArrayList<Node> nodesForOneBlock = new ArrayList<>();

                for (Node n : rootNode.getBody()) {

                    if (!n.getType().equals(NodeTypesEnum.IF_STATEMENT)) {
                        nodesForOneBlock.add(n);

                    } else {

                        if (!nodesForOneBlock.isEmpty()) {
                            BBlock block = new BBlock(iterator);
                            blockList.add(block);
                            iterator++;
                        }

                    }















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
                //füge die Nodes in eine BlockListe
                break;
        }
    }

*/

    // Aufruf für die "Interface"-Abfolge.
    public static ArrayList<BBlock> parse(Node rootNode) {

        // Lege einen "großen" Basic Block an

        walkTree(rootNode);

        return blockList;
    }
}

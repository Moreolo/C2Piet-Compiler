package basicblocks;

import basicblocks.datatypes.*;
import ast.datatypes.*;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;


public class BBMain {

    private ArrayList<BBlock> blockList = new ArrayList<>();
    private int iterator = 0;
    HashMap<String, ArrayList<BBlock>> allFunctionDefinitions;

    public BBMain(){
        this.allFunctionDefinitions = new HashMap<>();
    }

    public BBMain(HashMap<String, ArrayList<BBlock>> storesFunctionDefinitions){
        this.allFunctionDefinitions = storesFunctionDefinitions;
    }


    private BBlock splitBlock(BBlock oldBlock, ArrayList<Node> stayBehind) {
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
    private ArrayList<BBlock> createFork(BBlock oldBBlock, List<Node> leftPath, List<Node> rightPath, Node condition){
        //alter Block wir mit CondBlock ersetzt und mit condition node befüllt
        //ein bzw. zwei neue Blöcke werden erstellt für true bzw. else Blöcke 
        //diese werden mit den jeweiligen Nodes für diese Blöcke befüllt 
        BBlock leftBlock = new BBlock(++iterator);
        blockList.add(leftBlock);
        leftBlock.setBody((ArrayList) leftPath);
        CondBlock replaceOld = new CondBlock(oldBBlock.getPositionInArray());
        replaceOld.addNodeToBody(condition);
        int position = oldBBlock.getPositionInArray();
        blockList.set(position, replaceOld);
        replaceOld.setNext(leftBlock.getPositionInArray());
        ArrayList<BBlock> paths = new ArrayList<>();
        paths.add(leftBlock);
        if (!rightPath.isEmpty()){
            BBlock rightBlock = new BBlock(++iterator);
            rightBlock.setBody((ArrayList) rightPath);
            blockList.add(rightBlock);
            replaceOld.setNext2(rightBlock.getPositionInArray());
            paths.add(rightBlock);
        }else{
            Integer oldNext = oldBBlock.getNext();
            if (oldNext != null){
                replaceOld.setNext2(oldBBlock.getNext());
            }
        }
        return paths;
    }

    //die Funktion fügt einen BasicBlock VOR einem anderen BasicBlock ein
    private void addBefore(BBlock addBlock, BBlock oldBBlock){
        int position = oldBBlock.getPositionInArray();
        addBlock.setPositionInArray(position);
        blockList.set(position, addBlock);
        blockList.add(oldBBlock);
        iterator = iterator+1;
        oldBBlock.setPositionInArray(iterator);
        addBlock.setNext(iterator);
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
    private void walkTree(BBlock blockOfCode) {
        ArrayList<Node> nodesForOneBasicBlock = new ArrayList<>();
        ArrayList<Node> nodeList = blockOfCode.getBody();
        int counter = 0;
        for(Node currentNode :nodeList){ 
            counter = counter + 1;
            switch (currentNode.getType()) {
                //Function Call Node wird in Function Call Block umgewandelt
                case FUNCTION_CALL:
                    if (!nodesForOneBasicBlock.isEmpty()){
                        BBlock nextBlockLookInto = splitBlock(blockOfCode, nodesForOneBasicBlock);
                        walkTree(nextBlockLookInto);
                        return;
                    }
                    
                    if (counter != nodeList.size()){
                        int positionOfOldBlock = blockOfCode.getPositionInArray();
                        ArrayList<Node> funCallBlock = new ArrayList<>();
                        funCallBlock.add(currentNode);
                        BBlock nextBlockLookInto = splitBlock(blockOfCode, funCallBlock);
                        walkTree(blockList.get(positionOfOldBlock));
                        walkTree(nextBlockLookInto);
                        return;
                    }
                    //Ausgangslage des Funktion Calls:
                    // doSOmething( add(5,3))
                    
                    //wird verwandelt in:
                    //x = add(5,3)
                    //doSOmething(x)
                    SearchFunCall searcher = new SearchFunCall();
                    searcher.searchCallsOriginFun(currentNode);
                    ArrayList<funCallInfo> funCallInfos = searcher.getFunCallInfos();
                    for(funCallInfo funInfo: funCallInfos){
                        FunCallBlock callFunction = new FunCallBlock(-1, funInfo.getParameterList(), funInfo.getFunctionName(), funInfo.getReturnTempVar());
                        addBefore(callFunction, blockOfCode);

                    }
                    FunCallBlock callFunction = new FunCallBlock(blockOfCode.getPositionInArray(), (ArrayList) currentNode.getAlternative(), currentNode.getValue(), null);
                    blockList.set(iterator, callFunction);
                    return;
                
                //Ausgangslage der Binary Expression:
                // x= doSOmething() + 4
                
                //wird verwandelt in:
                // y = doSOmething()
                //x = y + 4
                case BINARY_EXPRESSION:
                    //zuerst schauen, ob da überhaupt mind. ein funktionsaufruf dinnen ist
                    SearchFunCall binSearcher = new SearchFunCall();
                    binSearcher.searchCallsOriginBin(currentNode);
                    ArrayList<funCallInfo> binCallInfos = binSearcher.getFunCallInfos();
                    if (binCallInfos.size() != 0){

                        //wenn ja, dann muss man davor abtrennen und danach
                        if (!nodesForOneBasicBlock.isEmpty()){
                            BBlock nextBlockLookInto = splitBlock(blockOfCode, nodesForOneBasicBlock); //neuer wichtiger block
                            blockOfCode = nextBlockLookInto;
                            nodesForOneBasicBlock.clear();
                        }

                        //alle Funktionen werden vor die Binary Expression geschrieben
                        for(funCallInfo funInfo: binCallInfos){
                            FunCallBlock callFun = new FunCallBlock(-1, funInfo.getParameterList(), funInfo.getFunctionName(), funInfo.getReturnTempVar());
                            addBefore(callFun, blockOfCode);
    
                        }
                        
                        }
                    nodesForOneBasicBlock.add(currentNode);
                    break;

                
                //Function Definition Node -> Code der Funktion wird wie ein normales Programm abgearbeitet
                //die Basic Blocks einer Funktion werden in einer Liste gespeichert
                //diese Liste kommt in eine HashList, in der alle Funktionsdefinitionen gespeichert sind
                //der Funktionsname (string) ist der key
                case FUNCTION_DEF:
                    //function def node muss evtl. entfernt werden aus bb; wenn dann nix mehr drin ist -> komplett löschen
                    BBMain parseFunction = new BBMain(this.allFunctionDefinitions);
                    ArrayList<Node> codeInFunction = (ArrayList) currentNode.getBody();
                    BBlock blockForFunctionCode = new BBlock(parseFunction.iterator);
                    blockForFunctionCode.setBody(codeInFunction);
                    parseFunction.blockList.add(blockForFunctionCode);
                    parseFunction.walkTree(blockForFunctionCode);
                    ArrayList<BBlock> functionAsBlocks = parseFunction.blockList;
                    this.allFunctionDefinitions.put(currentNode.getValue(), functionAsBlocks); //erster Parameter ist der Funktionsname
                    break;

                case WHILE_STATEMENT:
                    //wenn normaler Code vor While -> Trennen: Code vor While - restlicher Code
                    if (!nodesForOneBasicBlock.isEmpty()){
                        BBlock nextBlockLookInto = splitBlock(blockOfCode, nodesForOneBasicBlock);
                        walkTree(nextBlockLookInto);
                        return;
                    }

                    //wenn normaler Code nach While -> Trennen: While - restlicher Code nach WHile
                    if (counter != nodeList.size()){
                        int positionOfOldBlock = blockOfCode.getPositionInArray();
                        ArrayList<Node> whileBlock = new ArrayList<>();
                        whileBlock.add(currentNode);
                        BBlock nextBlockLookInto = splitBlock(blockOfCode, whileBlock);
                        walkTree(blockList.get(positionOfOldBlock));
                        walkTree(nextBlockLookInto);
                        return;
                    }

                    // in diesem Fall hat man nur ein While ohne Code davor und danach vorliegen
                    Node whileCondition = currentNode.getCondition();
                    ArrayList<Node> whileBody = (ArrayList) currentNode.getBody();
                    ArrayList<BBlock> whileBodyBB;
                    whileBodyBB = createFork(blockOfCode, whileBody, new ArrayList<>(), whileCondition);
                    for (BBlock basicBlock: whileBodyBB){
                        basicBlock.setNext(blockOfCode.getPositionInArray());
                        walkTree(basicBlock);
                    }
                    return;



                // behandelt elseif Struktur gegebenenfalls auch mit else am Ende
                case ELSE_STATEMENT:
                    Node conditionElseIf = currentNode.getCondition();
                    //elseif
                    if (conditionElseIf != null){
                        nodeList.remove(0);
                        ArrayList<BBlock> paths = createFork(blockOfCode, currentNode.getBody(), nodeList, conditionElseIf);
                        for (BBlock path: paths){
                            walkTree(path);
                        }
                    //normales else am Ende
                    }else{
                        blockOfCode.setBody( (ArrayList) currentNode.getBody());
                    }
                    return;

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
    public ArrayList<BBlock> parse(Node rootNode) {
        BBlock starterBlock = new BBlock(0);
        blockList.add(starterBlock);
        starterBlock.addNodeToBody(rootNode);
        walkTree(starterBlock);

        return blockList;
    }
}

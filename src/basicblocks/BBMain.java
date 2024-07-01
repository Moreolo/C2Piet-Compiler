package basicblocks;

import basicblocks.datatypes.*;
import ast.datatypes.*;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;


public class BBMain {

    /*
     * Return-Object für die gleichzeitige Übergabe von der BBlock- und der Funktionsindex-Liste
     */
    public static class BlockLists {

        public ArrayList<BBlock> blockList;
        public HashMap<String,Integer> functionIndexMap;

        public BlockLists(ArrayList<BBlock> blockList, HashMap<String,Integer> funcMap) {
            this.blockList = blockList;
            this.functionIndexMap = funcMap;
        }        
    }

    private static ArrayList<BBlock> blockList = new ArrayList<>();
    private static int iterator = 0;

    static HashMap<String, Integer> functionIndicesMap = new HashMap<>();



    private static BBlock splitBlock(BBlock oldBlock, ArrayList<Node> stayBehind) {
        //die alten Nodes im alten Block werden aufgeteilt:
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
    private static ArrayList<BBlock> createFork(BBlock oldBBlock, List<Node> leftPath, List<Node> rightPath, Node condition){
        //alter Block wir mit CondBlock ersetzt und mit condition node befüllt
        //ein bzw. zwei neue Blöcke werden erstellt für true bzw. else Blöcke 
        //diese werden mit den jeweiligen Nodes für diese Blöcke befüllt 
        int position = oldBBlock.getPositionInArray();

        BBlock leftBlock = new BBlock(++iterator);
        CondBlock replaceOld = new CondBlock(position);
        
        ArrayList<BBlock> paths = new ArrayList<>();


        blockList.add(leftBlock);
        leftBlock.setBody((ArrayList) leftPath);
        replaceOld.addNodeToBody(condition);
        
        blockList.set(position, replaceOld);
        replaceOld.setNext(leftBlock.getPositionInArray());
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
    private static void addBefore(BBlock addBlock, BBlock oldBBlock){

        int position = oldBBlock.getPositionInArray();

        addBlock.setPositionInArray(position);
        blockList.set(position, addBlock);
        blockList.add(oldBBlock);
        iterator = iterator+1;
        oldBBlock.setPositionInArray(iterator);
        addBlock.setNext(iterator);

    }


    // Grundlegende Idee ist der rekursive durchlauf durch den Node-Baum
    private static void walkTree(BBlock blockOfCode) {

        ArrayList<Node> nodesForOneBasicBlock = new ArrayList<>();
        ArrayList<Node> nodeList = blockOfCode.getBody();
        int counter = 0;

        for(Node currentNode :nodeList){ 
            counter = counter + 1;

            // überbleibsel vom AST-Team, kann ausgefiltert werden
            if (currentNode ==null){
                continue;
            }

            

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

                    FunCallBlock callFunction = new FunCallBlock(blockOfCode.getPositionInArray(), (ArrayList) currentNode.getAlternative(), currentNode.getValue(), blockOfCode.getNext());
                    blockList.set(callFunction.getPositionInArray(), callFunction);

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
                    //condition an den searcher geben -> falls funktionsinformationen zurückbekommen:
                    //splitBlock 
                    //next vom true Block des While zur vorgezogenen Funktion setzen
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
                    if (conditionElseIf != null) {

                        nodeList.remove(0);
                        ArrayList<BBlock> paths = createFork(blockOfCode, currentNode.getBody(), nodeList, conditionElseIf);

                        for (BBlock path: paths){

                            walkTree(path);

                        }

                    //normales else am Ende
                    }else{

                        blockOfCode.setBody( (ArrayList) currentNode.getBody());
                        walkTree(blockOfCode);

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
                    ArrayList<Node> trueBody = (ArrayList) currentNode.getBody();
                    ArrayList<Node> elseNodes = (ArrayList) currentNode.getAlternative();

                    //condition nach funktionen durchsuchen --> wenn gefunden:
                    //mit addbefore alle funktionen vor den Conditionblock schieben
                    Node condition = currentNode.getCondition();
                    ArrayList<BBlock> paths;

                    if (elseNodes != null){

                        paths = createFork(blockOfCode, trueBody, elseNodes, condition);

                    }else{

                        paths = createFork(blockOfCode, trueBody, new ArrayList<>(), condition);

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
                
                case DECLARATION:
                //zuerst schauen, ob da überhaupt mind. ein funktionsaufruf dinnen ist
                SearchFunCall decSearcher = new SearchFunCall();
                decSearcher.searchCallsOriginDec(currentNode);
                ArrayList<funCallInfo> decCallInfos = decSearcher.getFunCallInfos();

                if (decCallInfos.size() != 0){

                    //wenn ja, dann muss man davor abtrennen und danach
                    if (!nodesForOneBasicBlock.isEmpty()){

                        BBlock nextBlockLookInto = splitBlock(blockOfCode, nodesForOneBasicBlock); //neuer wichtiger block
                        blockOfCode = nextBlockLookInto;
                        nodesForOneBasicBlock.clear();

                    }

                    //alle Funktionen werden vor die Binary Expression geschrieben
                    for(funCallInfo funInfo: decCallInfos){

                        FunCallBlock callFun = new FunCallBlock(-1, funInfo.getParameterList(), funInfo.getFunctionName(), funInfo.getReturnTempVar());
                        addBefore(callFun, blockOfCode);

                    }
                    
                }

                nodesForOneBasicBlock.add(currentNode);
                break;

            
                default:

                    nodesForOneBasicBlock.add(currentNode);

                    break;

            }
        }

        
    }


    // Aufruf für die "Interface"-Abfolge; erhält Programm-Node des gesamten AST
    public static BlockLists parse(Node rootNode) {

        TermBlock termBlock = new TermBlock(iterator++);
        blockList.add(termBlock);

        BBlock starterBlock = new BBlock(iterator);
        blockList.add(starterBlock);

        for (Node node : rootNode.getBody()) {

            // im Body einer Program-Node können eig. nur Function-DEFs und Variable-Declarations stehen
            switch (node.getType()) {

                case TERMINATOR:
                // nichts machen
                continue;

                case FUNCTION_DEF:

                    
                    if (node.getValue().equals("main")) {
                    //main gefunden, starterblock muss auf Main zeigen
                    // main braucht keinen Verweis in der function map
                        
                        BBlock funcBlock = new BBlock(++iterator);
                        starterBlock.setNext(funcBlock.getPositionInArray());
                        funcBlock.setBody((ArrayList) node.getBody());
                        blockList.add(funcBlock);
                        walkTree(funcBlock);

                    } else {
                    // es handelt sich um normale Func-Def, Index und Name des Startblocks der Funktion müssen abgespeichert werden
                        FunDefBlock funcBlock = new FunDefBlock(++iterator);
                        functionIndicesMap.put(node.getValue(), funcBlock.getPositionInArray());
                        funcBlock.setParameters((ArrayList) node.getAlternative());
                        funcBlock.setBody((ArrayList) node.getBody());
                        blockList.add(funcBlock);
                        walkTree(funcBlock);
    
                    }

                   

                    break;
            
                
                default:
                    // sonst eig. nur Variablen-Deklaration möglich, kann direkt in den StarterBlock eingefügt werden
                    starterBlock.addNodeToBody(node);
                    break;
            }
        }

        return new BlockLists(blockList, functionIndicesMap);
    }
}
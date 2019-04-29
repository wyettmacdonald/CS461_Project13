/*
 * Wyett MacDonald, Tia Zhang, Kyle Douglas
 * April 11, 2019
 * CS461 Project 17
 * CodeGenVisitor class for MIPS
 */

package proj18DouglasMacDonaldZhang.bantam.codegenmips;

import proj18DouglasMacDonaldZhang.bantam.ast.*;
import proj18DouglasMacDonaldZhang.bantam.util.ClassTreeNode;
import proj18DouglasMacDonaldZhang.bantam.util.ErrorHandler;
import proj18DouglasMacDonaldZhang.bantam.util.Location;
import proj18DouglasMacDonaldZhang.bantam.util.SymbolTable;
import proj18DouglasMacDonaldZhang.bantam.visitor.Visitor;

import java.util.*;

public class CodeGenVisitor extends Visitor {

    private String currentClass; //TODO remove unused fields
    private ErrorHandler errorHandler;
    private SymbolTable currentSymbolTable; //Symbol table of variable Locations
    private List<Instruction> instructionList;
    private MipsSupport mipsSupport;
    Hashtable<String, Integer> idTable;
    Hashtable<String, ClassTreeNode> classMap;
    private Stack<String> labelStack;
    private Map<String, Integer> varMap;
    private Stack<String> usedTRegs;
    private Stack<String> unusedTRegs;
    private Stack<String> usedARegs;
    private Stack<String> unusedARegs;
    private Integer nextOffset;
    private Map<String, List<String>> classListMap;


    public CodeGenVisitor(ErrorHandler errorHandler, Hashtable<String, ClassTreeNode> classMap,
                          MipsSupport mipsSupport, Hashtable<String, Integer> idTable,
                          Map<String, Integer> varMap, Map<String, List<String>> classListMap) {
        this.errorHandler = errorHandler;

//        this.instructionList = instructions;
        this.mipsSupport = mipsSupport;
        this.idTable = idTable;
        this.classMap = classMap;
        this.currentSymbolTable = new SymbolTable();
        this.varMap = varMap;
        this.labelStack = new Stack<>();
        unusedTRegs = new Stack<>();
        usedTRegs = new Stack<>();
        unusedARegs = new Stack<>();
        usedARegs = new Stack<>();
        this.classListMap = classListMap;
    }

    public void createStacks() {
        for(int i = 3; i >=  0; i--) {
            unusedARegs.push("a" + i);
        }
        for(int i = 9; i >= 0; i--) {
            unusedTRegs.push("t" + i);
        }
    }


    public void generateCode(List<Instruction> instrList){
        createStacks();
        this.instructionList = instrList;
        classMap.forEach( (className, classTreeNode) -> {
            if(!classTreeNode.isBuiltIn()) {
                classTreeNode.getASTNode().accept(this);
            }
        });
    }

    /**
     * Generates the next offset
     *
     * @return next offset number
     */
    public int getNextOffset() {
        nextOffset -= 4;
        return nextOffset;
    }
    /**
     * Visit a class node
     *
     * @param node the class node
     * @return null
     */
    public Object visit(Class_ node) {
        // set the currentClass to this class
//        currentClass = currentClass.lookupClass(node.getName());
//        currentSymbolTable = currentClass.getVarSymbolTable();
//        currentClassFieldLevel = currentSymbolTable.getCurrScopeLevel();
        currentClass = node.getName();
        ArrayList<String> classNameList = new ArrayList<>();
        classNameList.add(node.getName() + "_init");
        instructionList.add(new Instruction("", classNameList, ""));
        currentSymbolTable.enterScope();
        node.getMemberList().accept(this);
        return null;
    }

    /**
     * Visit a field node
     *
     * @param node the field node
     * @return null
     */
    public Object visit(Field node) {
//        String reg = getTReg();
        Instruction instruction;
        Integer offset = currentSymbolTable.getCurrScopeLevel()*(-4);
        if(node.getInit() != null) {
            node.getInit().accept(this);
            instruction = new Instruction("sw", null,  "$v0", Integer.toString(offset), "$a0");
        }
        instruction = new Instruction("sw", null, "$a0", Integer.toString(offset));
        instructionList.add(instruction);
//        pushToStack(reg);
        Location loc = new Location("$a0", offset);
        currentSymbolTable.add(node.getName(), loc);
        return null;
    }

    /**
     * Visit a method node
     *
     * @param node the method node
     * @return null
     */
    public Object visit(Method node) {
        // generate prologue code
        // generate code for method body
        // generate epilogue code
        // mips return statement
        // check if the reference is null
        // if not put the reference type in $a0
        // push the parameters on the stack
        // la $t0 Object_dispatch_table
        // add $t0, $t0, 4
        // create a map with a classname and list of methods, then the index of list is the order of method
        // need to
        nextOffset = 0;
        pushRegsOnStack();
        // TODO: What does compute the obj ref and temporarily push it on the stack mean?
        pushReturnAddrAndFP();

        // compute the reference and push on the stack
        // TODO: Do we need this to allocate space on the stack?
        currentSymbolTable.enterScope();
        node.getFormalList().accept(this);
        System.out.println(currentClass + "." + node.getName());
        int numLocalVars = varMap.get(currentClass + "." + node.getName());
        String space = Integer.toString(numLocalVars*4);
        instructionList.add(new Instruction("addi", null, "$sp", "sp", space));
        // initialize sp and fp
        node.getStmtList().accept(this);
        currentSymbolTable.exitScope();

        generateEpilogue();

        return null;
    }

    public void generatePrologue(){

    }

    public void pushRegsOnStack() {
        for (String reg: usedARegs) {
            pushToStack(reg);
        }
        for (String reg: usedTRegs) {
            pushToStack(reg);
        }
    }

    public void generateEpilogue() {
        for (String reg: usedARegs) {
            popFromStack(reg);
        }
        for (String reg: usedTRegs) {
            popFromStack(reg);
        }
    }

    /**
     * Returns the next available T register
     * Adds it to the used stack
     *
     * @return String next available T register
     */
    public String getTReg() {
        String theReg = unusedTRegs.pop();
        usedTRegs.push(theReg);
        return theReg;
    }

    /**
     * Returns the next available A register
     * Adds it to the used stack
     *
     * @return String next available A register
     */
    public String getAReg() {
        String theReg = unusedARegs.pop();
        usedARegs.push(theReg);
        return theReg;
    }

    /**
     * Visit a formal node
     *
     * @param node the formal node
     * @return null
     */
    public Object visit(Formal node) {
//        String reg = getAReg();
//        instructionList.add(new Instruction("li", null, reg, node.getName()));
//        pushToStack(reg);
        // add to symbol table
        // variable name and location (fp)
        // number of parameters * 4 + 4
//        Location loc =
        Location loc = new Location("$fp", getNextOffset());
        currentSymbolTable.add(node.getName(), loc);

        return null;
    }

    /**
     * Visit a declaration statement node
     *
     * @param node the declaration statement node
     * @return null
     */
    public Object visit(DeclStmt node) {
//        Expr initExpr = node.getInit();
//        initExpr.accept(this);
        // declaring variable, won't appear any earlier
        // write code for evaluating expression
        // puts result in $v0
        // add variable to symbol table
        // generate code to put expr in that location
        // sw $v0 -4(fp)
        // need a field to keep track of the offset
        node.getInit().accept(this);
        String reg = getTReg();
        Location loc = new Location(reg, getNextOffset());
        currentSymbolTable.add(node.getName(), loc);
        String offset = Integer.toString(nextOffset);
        instructionList.add(new Instruction("sw", null, "$v0", offset + "($fp)"));
        return null;
    }

    /**
     * Visit an if statement node
     *
     * @param node the if statement node
     * @return null
     */
    public Object visit(IfStmt node) {

        String elseLabel = mipsSupport.getLabel();
        String afterLabel = mipsSupport.getLabel();

        node.getPredExpr().accept(this); //This will store the result of the predicate in $v0
        Instruction branchInstr;

        branchInstr = new Instruction("ble", null, "$v0", "$zero", elseLabel); //If false
        instructionList.add(branchInstr);

        //Generate then code
        currentSymbolTable.enterScope();
        node.getThenStmt().accept(this);
        currentSymbolTable.exitScope();
        //If true, skip over else if there is an else
        instructionList.add(new Instruction("jal", null, afterLabel));


        //If there's an else, generate the else code after the else label
        //Making a no op just because I need something to attach the label to
        ArrayList<String> labList = new ArrayList<String>();
        labList.add(elseLabel);
        instructionList.add(new Instruction("noop", labList));

        if(node.getElseStmt() != null){
            node.getElseStmt().accept(this);
        }

        labList = new ArrayList<String>();
        labList.add(afterLabel);
        instructionList.add(new Instruction("noop", labList));





        return null;
    }

    /**
     * Visit a while statement node
     *
     * @param node the while statement node
     * @return null
     */
    public Object visit(WhileStmt node) {

        // g: gen code for A
        // if $v0 == 0 go to h
        // gen code for C
        // go to g
        // h:

        String whileLabel = mipsSupport.getLabel();
        String afterLabel = mipsSupport.getLabel();
        labelStack.push(afterLabel);
        ArrayList<String> labels = new ArrayList<>();
        labels.add(whileLabel);

        Instruction instruction = new Instruction("", labels, "");
        instructionList.add(instruction);
        currentSymbolTable.enterScope();
        node.getPredExpr().accept(this); // stores result in $v0

        Instruction theInstruction = new Instruction("ble", null, "$v0", "$zero", afterLabel);
        instructionList.add(theInstruction);
        node.getBodyStmt().accept(this);
        Instruction bodyInstruction = new Instruction("j", null, whileLabel);
        instructionList.add(bodyInstruction);
        labels = new ArrayList<>();
        labels.add(afterLabel);

        Instruction afterInstruction = new Instruction("", labels, "");
        instructionList.add(afterInstruction);
        if(labelStack.peek().equals(afterLabel)) {
            labelStack.pop();
        }
        currentSymbolTable.exitScope();

        return null;
    }

    /**
     * Visit a for statement node
     *
     * @param node the for statement node
     * @return null
     */
    public Object visit(ForStmt node) {

        String loopLabel = mipsSupport.getLabel();
        String afterLabel = mipsSupport.getLabel();
        labelStack.push(afterLabel);
        // push afterlabel on the stack

        currentSymbolTable.enterScope();
        node.getInitExpr().accept(this);

        ArrayList<String> labels = new ArrayList<>();
        labels.add(loopLabel);
        instructionList.add(new Instruction("", labels, ""));
        node.getPredExpr().accept(this);

        Instruction branchInstr = new Instruction("bgtz", null, afterLabel);
        instructionList.add(branchInstr);

        //Generate then code
        node.getBodyStmt().accept(this);
        node.getUpdateExpr().accept(this);
        Instruction uncond = new Instruction("j", null, loopLabel);
        instructionList.add(uncond);

        // pop after label off stack
        if(labelStack.peek().equals(afterLabel)) {
            labelStack.pop();
        }

        labels = new ArrayList<>();
        labels.add(afterLabel);
        instructionList.add(new Instruction("", labels, "'"));

        currentSymbolTable.exitScope();
        return null;
    }

    /**
     * Visit a break statement node
     *
     * @param node the break statement node
     * @return null
     */
    public Object visit(BreakStmt node) {
        // jump to the end of loop
        // create a stack of labels and pop the top label off here
        String jumpTo = labelStack.pop();
        Instruction breakInstr = new Instruction("j", null, jumpTo);
        instructionList.add(breakInstr);

        return null;
    }

    // TODO delete function - does not generate any mips code
    /**
     * Visit a block statement node
     *
     * @param node the block statement node
     * @return null
     */
    public Object visit(BlockStmt node) {
        return null;
    }

    /**
     * Visit a return statement node
     *
     * @param node the return statement node
     * @return null
     */
    public Object visit(ReturnStmt node) {
        if(node.getExpr() != null) {
            node.getExpr().accept(this);
        }
        return null;
    }

    /**
     * Visit a dispatch expression node
     *
     * @param node the dispatch expression node
     * @return null
     */
    public Object visit(DispatchExpr node) {
        // <E1>.foo(E2, E3);
        // visit E1, put result in $a0
        // check for E1 is null, if so error
        // visit E2 and push on stack
        // visit E3 and push on stack
        // save any registers $t and $v
        // get the address of vft from the object $a0
        // get address of desired method as offset from start of vft
        // and put it in $t0, for example
        // call jalr $t0
        // check if E1 is null, if so error

        node.getRefExpr().accept(this);


        //loading in type and putting result in a0
//        instructionList.add(new Instruction(node.getRefExpr().getExprType(), null, "$v0"));
//        instructionList.add(new Instruction("li", null, "$a0"));
        // put result of $v0 in $a0
        instructionList.add(new Instruction("move", null, "$a0", "$v0"));
        //check if type is null
        instructionList.add(new Instruction("beq", null, null, "$a0", "null_pointer_error"));
        node.getActualList().accept(this);
        Object loc = currentSymbolTable.lookup(node.getMethodName());
        // this should return location and get the offset

        node.getActualList().accept(this);
        //need to fix
        pushToStack(node.getActualList().toString());
        // getFormalTypesList(node.getActualList().toString());

        //save t and v register
        instructionList.add(new Instruction("sw", null, "$v0", "4($fp"));
        instructionList.add(new Instruction("sw", null, "$t0", "4($fp"));

        //NEED TO DO: find address of vft with a0

        return null;
    }

    /**
     * returns a list of the types of the formal parameters
     *
     * @param method the methods whose formal parameter types are desired
     * @return a List of Strings (the types of the formal parameters)
     */
    private List<String> getFormalTypesList(Method method) {
        List<String> result = new ArrayList<>();
        for (ASTNode formal : method.getFormalList())
            result.add(((Formal) formal).getType());
        return result;
    }

    /**
     * Visit a list node of expressions
     *
     * @param node the expression list node
     * @return null
     */
    public Object visit(ExprList node) {
        List<String> typesList = new ArrayList<>();
        for (ASTNode expr : node) {
            expr.accept(this);
            typesList.add(((Expr) expr).getExprType());
        }
        //return a List<String> of the types of the expressions
        return typesList;
    }

    /**
     * Visit a new expression node
     *
     * @param node the new expression node
     * @return null
     */
    public Object visit(NewExpr node) {

        // TODO Where do you get the object from in Object.clone for the method call? What about general built-ins? - Load the one from the .data section

        //TODO Push all registers in use to the stack

        //The object's pointer is already in $a0
        Instruction cloneInstr = new Instruction("jal", null, "Object.clone");
        instructionList.add(cloneInstr);

        //After cloning, get the pointer to the object from $v0 and move it to $a0 cause that's what Exceptions.s expects
        Instruction moveInstr = new Instruction("move", null, "$a0","$v0");
        instructionList.add(moveInstr);


        //TODO restore registers pushed on the stack


        Instruction initInstr = new Instruction("jal", null, node.getType() + "._init");
        instructionList.add(initInstr);


        return null;
    }


    /*
    * Internal helper function
    * Handles storing the RA and storing the FP on the stack before a function call
    */
    private void pushReturnAddrAndFP(){
        pushToStack("$ra");
        //Instruction storeRA = new Instruction("sw", null, "$ra", "$sp");
        //instructionList.add(storeRA);

        pushToStack("$fp");
        //Instruction storeFP = new Instruction("sw", null, "$fp", "$sp");
       // instructionList.add(storeFP);
    }


    /*
    * Increments stack pointer by a word
    */
    private void incrementSP(){
        //Making this a separate function because you need to push space on the stack for local vars without storing
        instructionList.add(new Instruction("addi", null, "$sp", "sp", "-4"));
    }

    /*
     * Moves the stack pointer up by 1 word and puts the word that was in the given location in the stack
     * @param destination should be a string representing a register
     */
    private void pushToStack(String location){
        incrementSP();
        instructionList.add(new Instruction("sw", null, location, "$sp"));

    }

    /*
     * Moves the stack pointer down by 1 word and puts the word that was at the top in the given destination
     * @param destination should be a string representing a register
     */
    private void popFromStack(String destination){
        Instruction loadInstr = new Instruction("lw", null, destination, "$sp");
        instructionList.add(loadInstr);
        Instruction addInstr = new Instruction("addi", null, "$sp", "sp", "4");
        instructionList.add(addInstr);

    }

    /**
     * Visit an instanceof expression node
     *
     * @param node the instanceof expression node
     * @return null
     */
    public Object visit(InstanceofExpr node) {
        node.getExpr().accept(this);
        //This should put the expr location in $v0
        //Very first word should be its type, so load the first word into $v0
        //instructionList.add(new Instruction("lw", null, "$v0", "$v0"));
        String typeName = node.getType();
        int typeID = idTable.get(typeName);
        instructionList.add(new Instruction("li", null, "$v1", Integer.toString(typeID) ));
        int numDescendants = classMap.get(typeName).getNumDescendants();
        instructionList.add(new Instruction("li", null, "$t0", Integer.toString(numDescendants) ));
        instructionList.add(new Instruction("add", null, "$t0", "$v1", "$t0"));
        //At this point, $v1 will store the class id of the type you're checking against
        // and $t0 will store the id of the last class which could be its descendant
        //$v0 will store the class id of the expression type

        //So check if expression ($v0) is <= the limit ($t0) AND expression ($v0) >= class id ($v1)
        //Dale doesn't care if this is short-circuited
        instructionList.add(new Instruction("sle", null, "$t1", "$v0", "$t0"));
        instructionList.add(new Instruction("sge", null, "$t2", "$v0", "$v1"));
        instructionList.add(new Instruction("and", null, "$v0", "$t2", "$t1"));

        //TODO Make sure that no one expects semi-permanent storage in $v1 or $t0

        return null;
    }

    /**
     * Visit a cast expression node
     *
     * @param node the cast expression node
     * @return null
     */
    public Object visit(CastExpr node) {

        Expr expr = node.getExpr();

        Integer lineNum = node.getLineNum();

        InstanceofExpr instanceofExpr = new InstanceofExpr(lineNum, expr, null);
        Integer instruction = instructionList.indexOf(instanceofExpr.getExpr());

//        instructionArrayList.add(new Instruction("lw", null, "$v0", "$v0"));
//        String typeName = node.getType();
//        int typeID = idTable.get(typeName);
//        instructionArrayList.add(new Instruction("li", null, "$v1", Integer.toString(typeID) ));
//        int numDescendants = classMap.get(typeName).getNumDescendants();
//        instructionArrayList.add(new Instruction("li", null, "$t0", Integer.toString(numDescendants) ));
//        instructionArrayList.add(new Instruction("add", null, "$t0", "$v1", "$t0"));

        if (!node.getUpCast() && instructionList.get(instruction).equals(false)) {
            instructionList.add(new Instruction("li", null , "$v0", "class_cast_exception"));


            // instanceofExpr.setUpCheck(getFormalTypesList());
        }
        return null;
    }

    /**
     * Visit an assignment expression node
     *
     * @param node the assignment expression node
     * @return null
     */
    public Object visit(AssignExpr node) {
        String varType = null;
        String varName = node.getName();
        String refName = node.getRefName();
        Location varLoc = null;
        // typecheck the expr and check compatability
        node.getExpr().accept(this);

        if(refName != null) {

            if (refName.equals("super")) {
                varLoc = (Location) classMap.get(currentClass).getParent().getVarSymbolTable().lookup(varName, 0);
//                varLoc = (Location) currentSymbolTable.lookup(varName, 0);
            }
            else if (refName.equals("this")) {
                varLoc = (Location) currentSymbolTable.lookup(varName, 1);

            }
        }
        else {
            varLoc = (Location) currentSymbolTable.lookup(varName);
        }
        System.out.println("Assignment: " + refName + " " + varName);
        String loc = Integer.toString(varLoc.getOffset());
        instructionList.add(new Instruction("move", null, loc, "$v0"));
        return null;
    }

    /**
     * Visit a variable expression node
     *
     * @param node the variable expression node
     * @return null
     */
    public Object visit(VarExpr node) {

        return null;
    }

    private int getClassFieldLevel(ClassTreeNode node) {
        int level = 1;
        while(node.getParent() != null) {
            level++;
            node = node.getParent();
        }
        return level;
    }

    /**
     * Visit a binary comparison equals expression node
     *
     * @param node the binary comparison equals expression node
     * @return null
     */
    public Object visit(BinaryCompEqExpr node) {
        makeBinaryInstr(node, "seq"); //set equal to
        return null;
    }

    /**
     * Visit a binary comparison not equals expression node
     *
     * @param node the binary comparison not equals expression node
     * @return null
     */
    public Object visit(BinaryCompNeExpr node) {
        makeBinaryInstr(node, "sne"); //set greater than
        return null;
    }

    /**
     * Visit a binary comparison less than expression node
     *
     * @param node the binary comparison less than expression node
     * @return null
     */
    public Object visit(BinaryCompLtExpr node) {
        makeBinaryInstr(node, "slt"); //set less than
        return null;
    }

    private String[] getLeftAndRightTypes(BinaryExpr node) {
        node.getLeftExpr().accept(this);
        node.getRightExpr().accept(this);
        String type1 = node.getLeftExpr().getExprType();
        String type2 = node.getRightExpr().getExprType();
        return new String[]{type1,type2};
    }




    /**
     * Visit a binary comparison less than or equal to expression node
     *
     * @param node the binary comparison less than or equal to expression node
     * @return null
     */
    public Object visit(BinaryCompLeqExpr node) {
        makeBinaryInstr(node, "sle"); //set less than/equal to
        return null;
    }

    /**
     * Visit a binary comparison greater than expression node
     *
     * @param node the binary comparison greater than expression node
     * @return null
     */
    public Object visit(BinaryCompGtExpr node) {
        makeBinaryInstr(node, "sgt"); //set greater than
        return null;
    }

    /**
     * Visit a binary comparison greater than or equal to expression node
     *
     * @param node the binary comparison greater to or equal to expression node
     * @return null
     */
    public Object visit(BinaryCompGeqExpr node) {
        makeBinaryInstr(node, "sge"); //set greater than/ equal to
        return null;
    }




    /**
     * Private internal function only to be used by the visit methods for BinaryNodes
     * Produces the MIPS instructions for BinaryExpr nodes and adds them to the list
     * Handles short circuiting for BinaryLogic and potential divide by 0 error for division as well
     * @param node is a BinaryExpr node that should get instructions generated for it
     * @param instrType is a String representing the MIPS instruction corresponding to the primary function of the node
     * Ex: "slt" (set less than, for BinaryCompLtExpr)
     */
    private void makeBinaryInstr(BinaryExpr node, String instrType){
        node.getLeftExpr().accept(this);

        //Instead of storing in v1, store the first operand in the stack in case
        // this is nested math expression that would overwrite $v1 with inner calculations
        pushToStack("$v0");

        String shortCircLabel = null;
        if ("and".equals(instrType) || "or".equals(instrType) ){
            String condition;
            shortCircLabel = mipsSupport.getLabel();
            //Short-circuiting - branch to skip the right half if $v0 is > 0 (true) for OR and if $v0 <= 0 (false for AND
            if("and".equals(instrType)){
                condition = "ble";
            }
            else{
                condition = "bgt";
            }
            Instruction branchInstr = new Instruction(condition, null, "$zero", "$v0", shortCircLabel);
            instructionList.add(branchInstr);

        }

        node.getRightExpr().accept(this);


        popFromStack("$v1");

        if("div".equals(instrType)){
            Instruction zeroCheckInstr = new Instruction("beq", null, "$zero", "$v1", "divide_zero_error");
            instructionList.add(zeroCheckInstr);
        }

        //Dale's said this format works even for mult/div, and they'll just automatically move it from $lo to $v0
        //Bitwise AND and OR should still work here so long as I guarantee that 1 and 0 are the only values used for booleans
        Instruction mathInstr = new Instruction(instrType, null,"$v0", "$v0", "$v1");
        instructionList.add(mathInstr);

        if(shortCircLabel != null){
            String value;
            //If the label is executed, that means the AND is already false or the OR is already true
            if("and".equals(instrType)){
                value = "0";
            }
            else{
                value = "1";
            }
            ArrayList<String> labels = new ArrayList<>();
            labels.add(shortCircLabel);
            Instruction branchInstr = new Instruction("li", labels, "$v0", value);
            instructionList.add(branchInstr);
        }


    }


    /**
     * Visit a binary arithmetic plus expression node
     *
     * @param node the binary arithmetic plus expression node
     * @return null
     */
    public Object visit(BinaryArithPlusExpr node) {
        makeBinaryInstr(node, "add");
        return null;
    }

    /**
     * Visit a binary arithmetic minus expression node
     *
     * @param node the binary arithmetic minus expression node
     * @return null
     */
    public Object visit(BinaryArithMinusExpr node) {
        makeBinaryInstr(node, "sub");

        return null;
    }


    /**
     * Visit a binary arithmetic times expression node
     *
     * @param node the binary arithmetic times expression node
     * @return null
     */
    public Object visit(BinaryArithTimesExpr node) {
        makeBinaryInstr(node, "mult");
        return null;
    }

    /**
     * Visit a binary arithmetic divide expression node
     *
     * @param node the binary arithmetic divide expression node
     * @return null
     */
    public Object visit(BinaryArithDivideExpr node) {
        makeBinaryInstr(node, "div");
        return null;
    }

    /**
     * Visit a binary arithmetic modulus expression node
     *
     * @param node the binary arithmetic modulus expression node
     * @return null
     */
    public Object visit(BinaryArithModulusExpr node) {
        makeBinaryInstr(node, "div");
        //The remainder is stored in $hi, according to an online reference on U of Idaho's site
        Instruction getRemainInstr = new Instruction("move", null, "$v0", "$hi");
        instructionList.add(getRemainInstr);

        return null;
    }




    /**
     * Visit a binary logical AND expression node
     *
     * @param node the binary logical AND expression node
     * @return null
     */
    public Object visit(BinaryLogicAndExpr node) {
        makeBinaryInstr(node, "and");
        return null;
    }

    /**
     * Visit a binary logical OR expression node
     *
     * @param node the binary logical OR expression node
     * @return null
     */
    public Object visit(BinaryLogicOrExpr node) {
        makeBinaryInstr(node, "or");
        return null;
    }


    /**
     * Helper method. Should only be called by the visit methods for UnaryNodes
     * Handles making MIPs instructions for any of the UnaryNodes
     * This includes handling postfix operations
     * @param node is a UnaryNode for which MIPS should be generated
     * @param instrType is a String representing the MIPS instruction corresponding to the primary function of the node
     * It can be "addi" (for increment), "subi" (for decrement), "not" or "neg"
     */
    private void makeUnaryInstr(UnaryExpr node, String instrType){
        node.getExpr().accept(this);
        //Evaluating the expression should also put the value of the variable in $v0
        if("not".equals(instrType) || "neg".equals(instrType)){
            instructionList.add(new Instruction(instrType,null, "$v0", "$v0"));
        }
        else {
            instructionList.add(new Instruction(instrType, null, "$v0", "1"));
        }

        //Updating the variable's value - casting with Dale's permission
        VarExpr var = (VarExpr) node.getExpr();
        Location varLoc;
        Expr ref;
        if((ref = var.getRef()) == null){
            varLoc = (Location) currentSymbolTable.lookup(var.getName());
        }
        else{
            VarExpr refExpr = (VarExpr) ref;
            if("this".equals( refExpr.getName() ) ){
                varLoc = (Location) currentSymbolTable.lookup(var.getName(), 1);
            }
            else{ //It's "super"
                varLoc = (Location) currentSymbolTable.lookup(var.getName(), 0); //Look it up in the parent
            }
        }

        //Get the variable val into $v0, perform the incr/decr, then write the new val into memory
//        varLoc = Integer.toString(varLoc);
        instructionList.add(new Instruction("lw", null, "$v0", varLoc.getBaseReg()));
        if("addi".equals(instrType)) {
            instructionList.add(new Instruction("addi", null, "$v0", "1"));
        }
        else{
            instructionList.add(new Instruction("subi", null, "$v0", "1"));
        }
        instructionList.add(new Instruction("sw", null, varLoc.getBaseReg(),"$v0"));


        if(node.isPostfix()){
            //If there any other expressions on the line, since they'll assume $v0 has the value, revert $v0 to old value
            if("addi".equals(instrType)) {
                instructionList.add(new Instruction("subi", null, "$v0", "1"));
            }
            else{
                instructionList.add(new Instruction("addi", null, "$v0", "1"));
            }
        }
    }

    /**
     * Visit a unary negation expression node
     *
     * @param node the unary negation expression node
     * @return null
     */
    public Object visit(UnaryNegExpr node) {
        makeUnaryInstr(node, "neg");
        return null;
    }

    /**
     * Visit a unary NOT expression node
     *
     * @param node the unary NOT expression node
     * @return null
     */
    public Object visit(UnaryNotExpr node) {
       makeUnaryInstr(node, "not");
       return null;
    }

    /**
     * Visit a unary increment expression node
     *
     * @param node the unary increment expression node
     * @return null
     */
    public Object visit(UnaryIncrExpr node) {
       makeUnaryInstr(node, "addi");
       return null;
    }



    /**
     * Visit a unary decrement expression node
     *
     * @param node the unary decrement expression node
     * @return null
     */
    public Object visit(UnaryDecrExpr node) {
        makeUnaryInstr(node, "subi");
        return null;
    }

    /**
     * Visit an int constant expression node
     *
     * @param node the int constant expression node
     * @return null
     */
    public Object visit(ConstIntExpr node) {

        Instruction intInstr = new Instruction("li", null, "$v0", node.getConstant());
        instructionList.add(intInstr);
        return null;
    }

    /**
     * Visit a boolean constant expression node
     *
     * @param node the boolean constant expression node
     * @return null
     */
    public Object visit(ConstBooleanExpr node) {
        return null;
    }

    /**
     * Visit a string constant expression node
     *
     * @param node the string constant expression node
     * @return null
     */
    public Object visit(ConstStringExpr node) {
        instructionList.add(new Instruction("lw", null, "$v0", node.getConstant()));
        return null;
    }

}

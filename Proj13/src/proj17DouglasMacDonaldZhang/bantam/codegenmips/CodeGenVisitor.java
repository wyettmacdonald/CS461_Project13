/*
 * Wyett MacDonald, Tia Zhang, Kyle Douglas
 * April 11, 2019
 * CS461 Project 17
 * CodeGenVisitor class for MIPS
 */

package proj17DouglasMacDonaldZhang.bantam.codegenmips;

import proj17DouglasMacDonaldZhang.bantam.ast.*;
import proj17DouglasMacDonaldZhang.bantam.semant.NumLocalVarsVisitor;
import proj17DouglasMacDonaldZhang.bantam.util.ClassTreeNode;
import proj17DouglasMacDonaldZhang.bantam.util.ErrorHandler;
import proj17DouglasMacDonaldZhang.bantam.util.Location;
import proj17DouglasMacDonaldZhang.bantam.util.SymbolTable;
import proj17DouglasMacDonaldZhang.bantam.visitor.Visitor;

import java.lang.reflect.Array;
import java.util.ArrayList;
import java.util.Hashtable;
import java.util.List;
import java.util.Map;
import java.util.Stack;

public class CodeGenVisitor extends Visitor {

    private ClassTreeNode currentClass; //TODO remove unused fields
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


    public CodeGenVisitor(ErrorHandler errorHandler, Hashtable<String, ClassTreeNode> classMap,
                          MipsSupport mipsSupport, Map<String, Integer> numLocalVarsVisitor ) {
        this.errorHandler = errorHandler;

//        this.instructionList = instructions;
        this.mipsSupport = mipsSupport;
        currentSymbolTable = new SymbolTable();
        this.idTable = idTable;
        this.classMap = classMap;
        this.currentSymbolTable = new SymbolTable();
        this.labelStack = new Stack<>();
        this.varMap = numLocalVarsVisitor;
        unusedTRegs = new Stack<>();
        usedTRegs = new Stack<>();
        unusedARegs = new Stack<>();
        usedARegs = new Stack<>();
    }

    public void createStacks() {
        for(int i = 0; i < 4; i++) {
            unusedARegs.push("a" + i);
        }
        for(int i = 0; i < 10; i++) {
            unusedARegs.push("t" + i);
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

    public Object startVisit(ClassTreeNode currentClass) {
        currentClass.getASTNode().accept(this);
        return null;
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
        generatePrologue();
        pushReturnAddrAndFP();
        // compute the reference and push on the stack
        node.getFormalList().accept(this);
//        int numLocalVars = varMap.get(currentClass.getName() + "." + node.getName());
//        String space = Integer.toString(numLocalVars*4);
//        instructionList.add(new Instruction("addi", null, "$sp", "sp", space));
        // initialize sp and fp
        node.getStmtList().accept(this);

        return null;
    }

    public void generatePrologue() {
        for (String reg: usedARegs) {
            pushToStack(reg);
        }
        for (String reg: usedTRegs) {
            pushToStack(reg);
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
        String reg = getAReg();
        instructionList.add(new Instruction("li", null, reg, node.getName()));
        pushToStack(reg);
        return null;
    }

    /**
     * Visit a declaration statement node
     *
     * @param node the declaration statement node
     * @return null
     */
    public Object visit(DeclStmt node) {
        Expr initExpr = node.getInit();
        initExpr.accept(this);
        // declaring variable, won't appear any earlier

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
        instructionList.add(new Instruction("j", null, "$ra"));
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

        node.getRefExpr().accept(this);
        // check if E1 is null, if so error

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

        Instruction cloneInstr = new Instruction("jal", null, "Object.clone");
        instructionList.add(cloneInstr);

        //After cloning, get the pointer to the object from $v0 and move it to $a0 cause that's what Exceptions.s expects
        Instruction moveInstr = new Instruction("move", null, "$a0","$v0");
        instructionList.add(moveInstr);


        //TODO restore registers pushed on the stack


        Instruction initInstr = new Instruction("jal", null, node.getType() + "._init_");
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
     * Visit a new array expression node
     *
     * @param node the new array expression node
     * @return null
     */
    public Object visit(NewArrayExpr node) {
        return null;
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
        instructionList.add(new Instruction("lw", null, "$v0", "$v0"));
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
        instructionList.add(new Instruction("sle", null, "$t1", "$v0", "$t0"));
        instructionList.add(new Instruction("sge", null, "$t2", "$v0", "$v1"));
        instructionList.add(new Instruction("and", null, "$v0", "$t2", "$t1"));

        //TODO Make sure that no one expects semi-permanent storage in $v1 or $t0
        //TODO WHY DOES UPCHECK MATTER?

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
        ClassCastException castException = new ClassCastException();
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

        // typecheck the expr and check compatability
        node.getExpr().accept(this);

        // look up location of variable (say $fp and offset 4)
        // generates sw $v0 4($fp)
        Instruction saveInstruction = new Instruction("sw", null, "$v0", "4($fp");
        instructionList.add(saveInstruction);

        // y.x = 3 + 5
        // Look up type field of reference expression y
        // Look up offset of x in that class
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
        String varLoc;
        Expr ref;
        if((ref = var.getRef()) == null){
            varLoc = (String) currentSymbolTable.lookup(var.getName());
        }
        else{
            VarExpr refExpr = (VarExpr) ref;
            if("this".equals( refExpr.getName() ) ){
                varLoc = (String) currentSymbolTable.lookup(var.getName(), 1);
            }
            else{ //It's "super"
                varLoc = (String) currentSymbolTable.lookup(var.getName(), 0); //Look it up in the parent
            }
        }
        instructionList.add(new Instruction("sw", null, varLoc,"$v0"));


        if(node.isPostfix()){
            //If there any other expressions on the line, since they'll assume $v0 has the value, revert $v0 to old value
            if("addi".equals(instrType)) {
                instructionList.add(new Instruction("addi", null, "$v0", "1"));
            }
            else{
                instructionList.add(new Instruction("subi", null, "$v0", "1"));
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
        node.setExprType("boolean");
        return null;
    }

    /**
     * Visit a string constant expression node
     *
     * @param node the string constant expression node
     * @return null
     */
    public Object visit(ConstStringExpr node) {
        node.setExprType("String");
        return null;
    }

}

/*
 * Wyett MacDonald, Tia Zhang, Kyle Douglas
 * April 11, 2019
 * CS461 Project 17
 * CodeGenVisitor class for MIPS
 */

package proj17DouglasMacDonaldZhang.codegenmips;

import org.reactfx.value.Var;
import proj17DouglasMacDonaldZhang.bantam.ast.*;
import proj17DouglasMacDonaldZhang.bantam.util.ClassTreeNode;
import proj17DouglasMacDonaldZhang.bantam.util.ErrorHandler;
import proj17DouglasMacDonaldZhang.bantam.util.SymbolTable;
import proj17DouglasMacDonaldZhang.bantam.visitor.Visitor;

import java.util.ArrayList;
import java.util.List;

public class CodeGenVisitor extends Visitor {

    private ClassTreeNode currentClass;
    private ErrorHandler errorHandler;
    private SymbolTable currentSymbolTable; //Symbol table of variable Locations
    private ArrayList<Instruction> instructionArrayList;
    private MipsSupport mipsSupport;


    public CodeGenVisitor(ErrorHandler errorHandler, ClassTreeNode root, ArrayList<Instruction> instructions,
                          MipsSupport mipsSupport) {
        this.errorHandler = errorHandler;
        this.currentClass = root;
        this.instructionArrayList = instructions;
        this.mipsSupport = mipsSupport;
        currentSymbolTable = new SymbolTable();
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
        return null;
    }

    /**
     * Visit a formal node
     *
     * @param node the formal node
     * @return null
     */
    public Object visit(Formal node) {
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
        instructionArrayList.add(branchInstr);

        //Generate then code
        node.getThenStmt().accept(this);
        //If true, skip over else if there is an else
        instructionArrayList.add(new Instruction("jal", null, afterLabel));


        //If there's an else, generate the else code after the else label
        //Making a no op just because I need something to attach the label to
        ArrayList<String> labList = new ArrayList<String>();
        labList.add(elseLabel);
        instructionArrayList.add(new Instruction("noop", labList));

        if(node.getElseStmt() != null){
            node.getElseStmt().accept(this);
        }

        labList = new ArrayList<String>();
        labList.add(afterLabel);
        instructionArrayList.add(new Instruction("noop", labList));





        return null;
    }

    /**
     * Visit a while statement node
     *
     * @param node the while statement node
     * @return null
     */
    public Object visit(WhileStmt node) {
        return null;
    }

    /**
     * Visit a for statement node
     *
     * @param node the for statement node
     * @return null
     */
    public Object visit(ForStmt node) {
        return null;
    }

    /**
     * Visit a break statement node
     *
     * @param node the break statement node
     * @return null
     */
    public Object visit(BreakStmt node) {
        // jump to the return register address
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
        return null;
    }

    /**
     * Visit a dispatch expression node
     *
     * @param node the dispatch expression node
     * @return null
     */
    public Object visit(DispatchExpr node) {
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
        instructionArrayList.add(cloneInstr);

        //After cloning, get the pointer to the object from $v0 and move it to $a0 cause that's what Exceptions.s expects
        Instruction moveInstr = new Instruction("move", null, "$a0","$v0");
        instructionArrayList.add(moveInstr);


        //TODO restore registers pushed on the stack


        Instruction initInstr = new Instruction("jal", null, node.getType() + "._init_");
        instructionArrayList.add(initInstr);


        return null;
    }


    /*
    * Internal helper function
    * Handles storing the RA and storing the FP on the stack before a function call
    */
    private void pushReturnAddrAndFP(){
        pushToStack("$ra");
        //Instruction storeRA = new Instruction("sw", null, "$ra", "$sp");
        //instructionArrayList.add(storeRA);

        pushToStack("$fp");
        //Instruction storeFP = new Instruction("sw", null, "$fp", "$sp");
       // instructionArrayList.add(storeFP);
    }


    /*
    * Increments stack pointer by a word
    */
    private void incrementSP(){
        //Making this a separate function because you need to push space on the stack for local vars without storing
        instructionArrayList.add(new Instruction("addi", null, "$sp", "sp", "-4"));
    }

    /*
     * Moves the stack pointer up by 1 word and puts the word that was in the given location in the stack
     * @param destination should be a string representing a register
     */
    private void pushToStack(String location){
        incrementSP();
        instructionArrayList.add(new Instruction("sw", null, location, "$sp"));

    }

    /*
     * Moves the stack pointer down by 1 word and puts the word that was at the top in the given destination
     * @param destination should be a string representing a register
     */
    private void popFromStack(String destination){
        Instruction loadInstr = new Instruction("lw", null, destination, "$sp");
        instructionArrayList.add(loadInstr);
        Instruction addInstr = new Instruction("addi", null, "$sp", "sp", "4");
        instructionArrayList.add(addInstr);

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
        return null;
    }

    /**
     * Visit a cast expression node
     *
     * @param node the cast expression node
     * @return null
     */
    public Object visit(CastExpr node) {
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

        return null;
    }

    /**
     * Visit an array assignment expression node
     *
     * @param node the array assignment expression node
     * @return null
     */
    public Object visit(ArrayAssignExpr node) {
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




    /*
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
            instructionArrayList.add(branchInstr);

        }

        node.getRightExpr().accept(this);


        popFromStack("$v1");

        if("div".equals(instrType)){
            Instruction zeroCheckInstr = new Instruction("beq", null, "$zero", "$v1", "divide_zero_error");
            instructionArrayList.add(zeroCheckInstr);
        }

        //Dale's said this format works even for mult/div, and they'll just automatically move it from $lo to $v0
        //Bitwise AND and OR should still work here so long as I guarantee that 1 and 0 are the only values used for booleans
        Instruction mathInstr = new Instruction(instrType, null,"$v0", "$v0", "$v1");
        instructionArrayList.add(mathInstr);

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
            instructionArrayList.add(branchInstr);
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
        instructionArrayList.add(getRemainInstr);

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
            instructionArrayList.add(new Instruction(instrType,null, "$v0", "$v0"));
        }
        else {
            instructionArrayList.add(new Instruction(instrType, null, "$v0", "1"));
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
                varLoc = (String) currentSymbolTable.lookup(var.getName());
            }
            else{ //It's "super"
                varLoc = (String) currentSymbolTable.lookup(var.getName(), 0); //Look it up in the parent //TODO make sure this works to get the parent
            }

            Instruction updateInstr = new Instruction("sw", null, varLoc,"$v0");

        }


        if(node.isPostfix()){
            //If there any other expressions on the line, since they'll assume $v0 has the value, revert $v0 to old value
            if("addi".equals(instrType)) {
                instructionArrayList.add(new Instruction("subi", null, "$v0", "1"));
            }
            else{
                instructionArrayList.add(new Instruction("addi", null, "$v0", "1"));
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
        instructionArrayList.add(intInstr);
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

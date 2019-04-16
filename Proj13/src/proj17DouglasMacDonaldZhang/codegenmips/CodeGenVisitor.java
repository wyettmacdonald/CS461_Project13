/*
 * Wyett MacDonald, Tia Zhang, Kyle Douglas
 * April 11, 2019
 * CS461 Project 17
 * CodeGenVisitor class for MIPS
 */

package proj17DouglasMacDonaldZhang.codegenmips;

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
     * @return result of the visit
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
     * @return result of the visit
     */
    public Object visit(Field node) {
        return null;
    }

    /**
     * Visit a method node
     *
     * @param node the method node
     * @return result of the visit
     */
    public Object visit(Method node) {
        return null;
    }

    /**
     * Visit a formal node
     *
     * @param node the formal node
     * @return result of the visit
     */
    public Object visit(Formal node) {
        return null;
    }

    /**
     * Visit a declaration statement node
     *
     * @param node the declaration statement node
     * @return result of the visit
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
     * @return result of the visit
     */
    public Object visit(IfStmt node) {


        String afterLabel = mipsSupport.getLabel(); //If there's an else, this is the else label

        node.getPredExpr().accept(this); //This will store the result of the predicate in $v0
        Instruction branchInstr;
        //Since 0 is false, I'm assuming 1 is true TODO ASK DALE - ALSO IS THERE A SMARTER WAY OF DOING THIS

        branchInstr = new Instruction("bgtz", afterLabel);
        instructionArrayList.add(branchInstr);

        //TODO How do we generate a label with the correct instructions following it if Instruction aren't generated till later?
        //Should we count labels as instructions?
        //Stopgap
        instructionArrayList.add(new Instruction(afterLabel + ":", ""));

        //Generate then code
        node.getThenStmt().accept(this);

        //If there's an else, generate the else code after the else label
        if(node.getElseStmt() != null){
            node.getElseStmt().accept(this);
        }



        return null;
    }

    /**
     * Visit a while statement node
     *
     * @param node the while statement node
     * @return result of the visit
     */
    public Object visit(WhileStmt node) {
        return null;
    }

    /**
     * Visit a for statement node
     *
     * @param node the for statement node
     * @return result of the visit
     */
    public Object visit(ForStmt node) {
        return null;
    }

    /**
     * Visit a break statement node
     *
     * @param node the break statement node
     * @return result of the visit
     */
    public Object visit(BreakStmt node) {
        // jump to the return register address
        return null;
    }

    /**
     * Visit a block statement node
     *
     * @param node the block statement node
     * @return result of the visit
     */
    public Object visit(BlockStmt node) {
        return null;
    }

    /**
     * Visit a return statement node
     *
     * @param node the return statement node
     * @return result of the visit
     */
    public Object visit(ReturnStmt node) {
        return null;
    }

    /**
     * Visit a dispatch expression node
     *
     * @param node the dispatch expression node
     * @return the type of the expression
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
     * @return result of the visit
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
     * @return the type of the expression
     */
    public Object visit(NewExpr node) {

        //Push RA to the stack before calling clone method

        //TODO Question for Dale - how do we know what registers to push onto the stack before calling a built-in method?
        //TODO cont - how do I know what registers were in use before a New Expression, for instance?
        //todo does the built in handle the ra itself?
        pushReturnAndFP();



        Instruction cloneInstr = new Instruction("jal", "Object.clone");
        instructionArrayList.add(cloneInstr);

        //After cloning, get the pointer to the object

        //Store locations of fields

        //TODO does this count as a method call?
        Instruction initInstr = new Instruction("jal", node.getType() + "._init_");
        instructionArrayList.add(initInstr);


        return null;
    }


    /*
    * Handles storing the RA and storing the FP on the stack before a function call
    *
    */
    private void pushReturnAndFP(){
        moveSP();
        Instruction storeRA = new Instruction("sw", "$ra");
        storeRA.setOperand2("$sp");
        instructionArrayList.add(storeRA);

        moveSP();
        Instruction storeFP = new Instruction("sw", "$fp");
        storeRA.setOperand2("$sp");
        instructionArrayList.add(storeFP);


    }

    /*
    * Moves the stack pointer
    */
    private void moveSP(){
        Instruction addInstr = new Instruction("addi", "$sp");
        addInstr.setOperand2("$sp");
        addInstr.setOperand3("-4");

        instructionArrayList.add(addInstr);

    }

    /**
     * Visit a new array expression node
     *
     * @param node the new array expression node
     * @return the type of the expression
     */
    public Object visit(NewArrayExpr node) {
        return null;
    }

    /**
     * Visit an instanceof expression node
     *
     * @param node the instanceof expression node
     * @return the type of the expression
     */
    public Object visit(InstanceofExpr node) {
        return null;
    }

    /**
     * Visit a cast expression node
     *
     * @param node the cast expression node
     * @return the type of the expression
     */
    public Object visit(CastExpr node) {
        return null;
    }

    /**
     * Visit an assignment expression node
     *
     * @param node the assignment expression node
     * @return the type of the expression
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
     * @return the type of the expression
     */
    public Object visit(ArrayAssignExpr node) {
        return null;
    }

    /**
     * Visit a variable expression node
     *
     * @param node the variable expression node
     * @return the type of the expression
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
     * @return the type of the expression
     */
    public Object visit(BinaryCompEqExpr node) {
        makeBinaryInstr(node, "seq"); //set equal to
        return null;
    }

    /**
     * Visit a binary comparison not equals expression node
     *
     * @param node the binary comparison not equals expression node
     * @return the type of the expression
     */
    public Object visit(BinaryCompNeExpr node) {
        //TODO is this reference correct - Is this extended mips?
        makeBinaryInstr(node, "sne"); //set greater than
        return null;
    }

    /**
     * Visit a binary comparison less than expression node
     *
     * @param node the binary comparison less than expression node
     * @return the type of the expression
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
     * @return the type of the expression
     */
    public Object visit(BinaryCompLeqExpr node) {
        makeBinaryInstr(node, "sle"); //set less than/equal to
        return null;
    }

    /**
     * Visit a binary comparison greater than expression node
     *
     * @param node the binary comparison greater than expression node
     * @return the type of the expression
     */
    public Object visit(BinaryCompGtExpr node) {
        makeBinaryInstr(node, "sgt"); //set greater than
        return null;
    }

    /**
     * Visit a binary comparison greater than or equal to expression node
     *
     * @param node the binary comparison greater to or equal to expression node
     * @return the type of the expression
     */
    public Object visit(BinaryCompGeqExpr node) {
        makeBinaryInstr(node, "sge"); //set greater than/ equal to
        return null;
    }




    /*
     * Produces the MIPS instructions for BinaryExpr nodes and adds them to the list
     * TODO FILL OUT
     */
    private void makeBinaryInstr(BinaryExpr node, String instrType){
        node.getLeftExpr().accept(this);

        Instruction moveInstr = new Instruction("move", "$v1");
        moveInstr.setOperand2("$v0");
        instructionArrayList.add(moveInstr);

        node.getRightExpr().accept(this);

        if("div".equals(instrType)){
            Instruction zeroCheckInstr = new Instruction("beq", "$zero");
            zeroCheckInstr.setOperand2("$v1");
            zeroCheckInstr.setOperand3("divide_zero_error");
            instructionArrayList.add(zeroCheckInstr);
            //TODO ask Dale if we're supposed to work it out at compile or do it my way - that won't work if it's user input, right?
        }

        //Dale's said this format works even for mult/div, and they'll just automatically move it from $lo to $v0
        Instruction mathInstr = new Instruction(instrType, "$v0");
        mathInstr.setOperand2("$v0");
        mathInstr.setOperand3("$v1");
        instructionArrayList.add(mathInstr);
    }


    /**
     * Visit a binary arithmetic plus expression node
     *
     * @param node the binary arithmetic plus expression node
     * @return the type of the expression
     */
    public Object visit(BinaryArithPlusExpr node) {
        makeBinaryInstr(node, "add");


        return null;
    }

    /**
     * Visit a binary arithmetic minus expression node
     *
     * @param node the binary arithmetic minus expression node
     * @return the type of the expression
     */
    public Object visit(BinaryArithMinusExpr node) {
        makeBinaryInstr(node, "sub");

        return null;
    }


    /**
     * Visit a binary arithmetic times expression node
     *
     * @param node the binary arithmetic times expression node
     * @return the type of the expression
     */
    public Object visit(BinaryArithTimesExpr node) {
        //TODO ASK DALE DO WE CARE ABOUT SIGNED/UNSIGNED MATH
        makeBinaryInstr(node, "mult");
        return null;
    }

    /**
     * Visit a binary arithmetic divide expression node
     *
     * @param node the binary arithmetic divide expression node
     * @return the type of the expression
     */
    public Object visit(BinaryArithDivideExpr node) {
        makeBinaryInstr(node, "div");
        return null;
    }

    /**
     * Visit a binary arithmetic modulus expression node
     *
     * @param node the binary arithmetic modulus expression node
     * @return the type of the expression
     */
    public Object visit(BinaryArithModulusExpr node) {
        makeBinaryInstr(node, "div");
        //The remainder is stored in $hi, according to an online reference on U of Idaho's site
        //TODO ASK DALE ABOUT IF THIS IS CORRECT - I thought we were pretending hi doesn't exist?
        Instruction getRemainInstr = new Instruction("move", "$v0");
        getRemainInstr.setOperand2("$hi");
        instructionArrayList.add(getRemainInstr);

        return null;
    }




    /**
     * Visit a binary logical AND expression node
     *
     * @param node the binary logical AND expression node
     * @return the type of the expression
     */
    public Object visit(BinaryLogicAndExpr node) {
        makeBinaryInstr(node, "and");
        return null;
    }

    /**
     * Visit a binary logical OR expression node
     *
     * @param node the binary logical OR expression node
     * @return the type of the expression
     */
    public Object visit(BinaryLogicOrExpr node) {
        //TODO Are we doing lazy evaluation or not? XOR?

        makeBinaryInstr(node, "or");
        return null;
    }

    /**
     * Visit a unary negation expression node
     *
     * @param node the unary negation expression node
     * @return the type of the expression
     */
    public Object visit(UnaryNegExpr node) {
        node.getExpr().accept(this);
        return null;
    }

    /**
     * Visit a unary NOT expression node
     *
     * @param node the unary NOT expression node
     * @return the type of the expression
     */
    public Object visit(UnaryNotExpr node) {
        node.getExpr().accept(this);
        return null;
    }

    /**
     * Visit a unary increment expression node
     *
     * @param node the unary increment expression node
     * @return the type of the expression
     */
    public Object visit(UnaryIncrExpr node) {
        return null;
    }

    /**
     * Visit a unary decrement expression node
     *
     * @param node the unary decrement expression node
     * @return the type of the expression
     */
    public Object visit(UnaryDecrExpr node) {
        node.getExpr().accept(this);
        return null;
    }

    /**
     * Visit an int constant expression node
     *
     * @param node the int constant expression node
     * @return the type of the expression
     */
    public Object visit(ConstIntExpr node) {

        Instruction intInstr = new Instruction("li", "$v0");
        intInstr.setOperand2(node.getConstant());
        instructionArrayList.add(intInstr);
        return null;
    }

    /**
     * Visit a boolean constant expression node
     *
     * @param node the boolean constant expression node
     * @return the type of the expression
     */
    public Object visit(ConstBooleanExpr node) {
        node.setExprType("boolean");
        return null;
    }

    /**
     * Visit a string constant expression node
     *
     * @param node the string constant expression node
     * @return the type of the expression
     */
    public Object visit(ConstStringExpr node) {
        node.setExprType("String");
        return null;
    }

}

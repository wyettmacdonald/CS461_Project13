/*
 * Wyett MacDonald, Tia Zhang, Kyle Douglas
 * April 29, 2019
 * CS461 Project 18
 * PrintVisitor for printing the AST
 */
package proj18DouglasMacDonaldZhang.bantam.print;

import proj18DouglasMacDonaldZhang.bantam.ast.*;
import proj18DouglasMacDonaldZhang.bantam.semant.SemanticAnalyzer;
import proj18DouglasMacDonaldZhang.bantam.util.ClassTreeNode;
import proj18DouglasMacDonaldZhang.bantam.util.Error;
import proj18DouglasMacDonaldZhang.bantam.util.SymbolTable;
import proj18DouglasMacDonaldZhang.bantam.visitor.Visitor;

import java.util.ArrayList;
import java.util.List;
import java.util.Stack;


public class PrintVisitor extends Visitor {

    private String printString;
    private Program ast;
    private Stack<String> curTab;
    private boolean inForLoop;

    public PrintVisitor(Program ast) {
        this.ast = ast;
    }

    public String startVisit() {
        this.inForLoop = false;
        this.printString = "";
        this.curTab = new Stack<>();
        this.ast.accept(this);
        return printString;
    }

    /**
     * Adds a tab or \t on to the stack
     */
    public void addTab() {
        curTab.push("\t");
    }

    /**
     * Gets the current tab (number of \t's)
     *
     * @return string of \t's
     */
    public String getTab() {
        String tabs = "";
        if(!curTab.empty()) {
            for (String tab : curTab) {
                tabs += tab;
            }
        }
        return tabs;
    }

    /**
     * Visit a class node
     *
     * @param node the class node
     * @return result of the visit
     */
    public Object visit(Class_ node) {
        String parent = node.getParent();
        // set the currentClass to this class
        printString += getTab() + "class " + node.getName();
        if(parent != null) {
            printString += " extends " + parent;
        }
        printString += " {\n";
        addTab();
        node.getMemberList().accept(this);
        curTab.pop();
        printString += getTab() + "}\n";
        return null;
    }

    /**
     * Visit a field node
     *
     * @param node the field node
     * @return result of the visit
     */
    public Object visit(Field node) {
        String type = node.getType();
        String name = node.getName();
        Expr expr = node.getInit();
        printString += getTab() + type + " " + name;
        if (expr != null) {
            printString += " = ";
            expr.accept(this);
        }
        printString += ";\n";
        return null;
    }

    /**
     * Visit a method node
     *
     * @param node the method node
     * @return result of the visit
     */
    public Object visit(Method node) {
        String returnType = node.getReturnType();
        String name = node.getName();
        FormalList list = node.getFormalList();
        printString += getTab() + returnType + " " + name + "(";
//        for (ASTNode formal: list) {
//            formal.accept(this);
//        }
        node.getFormalList().accept(this);
        printString += ") {\n";
        addTab();
        node.getStmtList().accept(this);
        curTab.pop();
        printString += getTab() + "}\n\n";
        return null;
    }

    /**
     * Visit a formal node
     *
     * @param node the formal node
     * @return result of the visit
     */
    public Object visit(Formal node) {
        String type = node.getType();
        String name = node.getName();
        printString += type + " " + name + ",";
        return null;
    }

    /**
     * Visit a declaration statement node
     *
     * @param node the declaration statement node
     * @return result of the visit
     */
    public Object visit(DeclStmt node) {
        String type = node.getType();
        String name = node.getName();
        printString += getTab() + "var " + name;
        Expr initExpr = node.getInit();
        if (initExpr != null) {
            printString += " = ";
            initExpr.accept(this);
        }
        printString += ";\n";
        return null;
    }

    /**
     * Visit an if statement node
     *
     * @param node the if statement node
     * @return result of the visit
     */
    public Object visit(IfStmt node) {
        printString += getTab() + "if(";
        node.getPredExpr().accept(this);
        printString += ") {\n";
        addTab();
        node.getThenStmt().accept(this);
        curTab.pop();
        printString += getTab() + "}\n";
        Stmt stmt = node.getElseStmt();
        if(stmt != null) {
            printString += getTab() + "else {\n";
            addTab();
            node.getElseStmt().accept(this);
            curTab.pop();
            printString += getTab() + "}\n\n";
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
        printString += getTab() + "while(";
        node.getPredExpr().accept(this);
        printString += ") {\n";
        addTab();
        node.getBodyStmt().accept(this);
        curTab.pop();
        printString += getTab() + "}\n\n";
        return null;
    }

    /**
     * Visit a for statement node
     *
     * @param node the for statement node
     * @return result of the visit
     */
    public Object visit(ForStmt node) {
        printString += getTab() + "for(";
        inForLoop = true;
        node.getInitExpr().accept(this);
        printString += "; ";
        node.getPredExpr().accept(this);
        printString += "; ";
        node.getUpdateExpr().accept(this);
        printString += ") {\n";
        inForLoop = false;
        addTab();
        node.getBodyStmt().accept(this);
        curTab.pop();
        printString += getTab() + "}\n\n";
        return null;
    }

    /**
     * Visit a break statement node
     *
     * @param node the break statement node
     * @return result of the visit
     */
    public Object visit(BreakStmt node) {
        printString += getTab() + "break;";
        curTab.pop();
        return null;
    }

    /**
     * Visit a block statement node
     *
     * @param node the block statement node
     * @return result of the visit
     */
    public Object visit(BlockStmt node) {
        node.getStmtList().accept(this);
        return null;
    }

    /**
     * Visit a return statement node
     *
     * @param node the return statement node
     * @return result of the visit
     */
    public Object visit(ReturnStmt node) {
        Expr expr = node.getExpr();
        printString += getTab() + "return";
        if(expr != null) {
            node.getExpr().accept(this);
        }
        printString += ";\n";
        return null;
    }

    /**
     * Visit a dispatch expression node
     *
     * @param node the dispatch expression node
     * @return the type of the expression
     */
    public Object visit(DispatchExpr node) {
        String name = node.getMethodName();
        Expr expr = node.getRefExpr();
        printString += getTab();
        if(expr != null) {
            expr.accept(this);
            printString += ".";
        }
        printString += name + "(";
        node.getActualList().accept(this);
        printString += ");\n";
        return null;
    }

    // TODO: this could be useful for project 18
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
        String type = node.getType();
        printString += "new " + type + "()";
        return null;
    }

    /**
     * Visit an instanceof expression node
     *
     * @param node the instanceof expression node
     * @return the type of the expression
     */
    public Object visit(InstanceofExpr node) {
        String type = node.getType();
        node.getExpr().accept(this);
        printString += " instanceof " + type;
        return null;
    }

    /**
     * Visit a cast expression node
     *
     * @param node the cast expression node
     * @return the type of the expression
     */
    public Object visit(CastExpr node) {
        String type = node.getType();
        printString += "cast(" + type + ", ";
        node.getExpr().accept(this);
        return null;
    }

    /**
     * Visit an assignment expression node
     *
     * @param node the assignment expression node
     * @return the type of the expression
     */
    public Object visit(AssignExpr node) {
        String varName = node.getName();
        String refName = node.getRefName();
        printString += getTab();
        if(refName != null) {
            printString += refName + ".";
        }
        printString += varName + " = ";
        node.getExpr().accept(this);
        printString += ";\n";
        return null;
    }

    /**
     * Visit a variable expression node
     *
     * @param node the variable expression node
     * @return the type of the expression
     */
    public Object visit(VarExpr node) {
        //check that ref.name is legit
        String varName = node.getName();
        Expr expr = node.getRef();
        if(expr != null) {
            node.getRef().accept(this);
        }
        printString += varName;
        return null;
    }

    // TODO: Implement arrays?
    /**
     * Visit a new array expression node
     *
     * @param node the new array expression node
     * @return the type of the expression
     */
    public Object visit(NewArrayExpr node) {
        node.getSize().accept(this);
        return null;
    }

    /**
     * Visit an array expression node
     *
     * @param node the array expression node
     * @return the type of the expression
     */
    public Object visit(ArrayExpr node) {
        node.getRef().accept(this);
        node.getIndex().accept(this);
        return null;
    }

    /**
     * Helper method for generating BinaryExpr
     *
     * @param node BinaryExpr node
     * @param op Operator for the node
     */
    public void visitBinary(BinaryExpr node, String op) {
        node.getLeftExpr().accept(this);
        printString += " " + op + " ";
        node.getRightExpr().accept(this);
    }

    /**
     * Visit a binary comparison equals expression node
     *
     * @param node the binary comparison equals expression node
     * @return the type of the expression
     */
    public Object visit(BinaryCompEqExpr node) {
        visitBinary(node, node.getOpName());
        return null;
    }

    /**
     * Visit a binary comparison not equals expression node
     *
     * @param node the binary comparison not equals expression node
     * @return the type of the expression
     */
    public Object visit(BinaryCompNeExpr node) {
        visitBinary(node, node.getOpName());
        return null;
    }

    /**
     * Visit a binary comparison less than expression node
     *
     * @param node the binary comparison less than expression node
     * @return the type of the expression
     */
    public Object visit(BinaryCompLtExpr node) {
        visitBinary(node, node.getOpName());
        return null;
    }

    /**
     * Visit a binary comparison less than or equal to expression node
     *
     * @param node the binary comparison less than or equal to expression node
     * @return the type of the expression
     */
    public Object visit(BinaryCompLeqExpr node) {
        visitBinary(node, node.getOpName());
        return null;
    }

    /**
     * Visit a binary comparison greater than expression node
     *
     * @param node the binary comparison greater than expression node
     * @return the type of the expression
     */
    public Object visit(BinaryCompGtExpr node) {
        visitBinary(node, node.getOpName());
        return null;
    }

    /**
     * Visit a binary comparison greater than or equal to expression node
     *
     * @param node the binary comparison greater to or equal to expression node
     * @return the type of the expression
     */
    public Object visit(BinaryCompGeqExpr node) {
        visitBinary(node, node.getOpName());
        return null;
    }

    /**
     * Visit a binary arithmetic plus expression node
     *
     * @param node the binary arithmetic plus expression node
     * @return the type of the expression
     */
    public Object visit(BinaryArithPlusExpr node) {
        visitBinary(node, node.getOpName());
        return null;
    }

    /**
     * Visit a binary arithmetic minus expression node
     *
     * @param node the binary arithmetic minus expression node
     * @return the type of the expression
     */
    public Object visit(BinaryArithMinusExpr node) {
        visitBinary(node, node.getOpName());
        return null;
    }

    /**
     * Visit a binary arithmetic times expression node
     *
     * @param node the binary arithmetic times expression node
     * @return the type of the expression
     */
    public Object visit(BinaryArithTimesExpr node) {
        visitBinary(node, node.getOpName());
        return null;
    }

    /**
     * Visit a binary arithmetic divide expression node
     *
     * @param node the binary arithmetic divide expression node
     * @return the type of the expression
     */
    public Object visit(BinaryArithDivideExpr node) {
        visitBinary(node, node.getOpName());
        return null;
    }

    /**
     * Visit a binary arithmetic modulus expression node
     *
     * @param node the binary arithmetic modulus expression node
     * @return the type of the expression
     */
    public Object visit(BinaryArithModulusExpr node) {
        visitBinary(node, node.getOpName());
        return null;
    }

    /**
     * Visit a binary logical AND expression node
     *
     * @param node the binary logical AND expression node
     * @return the type of the expression
     */
    public Object visit(BinaryLogicAndExpr node) {
        visitBinary(node, node.getOpName());
        return null;
    }

    /**
     * Visit a binary logical OR expression node
     *
     * @param node the binary logical OR expression node
     * @return the type of the expression
     */
    public Object visit(BinaryLogicOrExpr node) {
        visitBinary(node, node.getOpName());
        return null;
    }

    /**
     * Visit a unary negation expression node
     *
     * @param node the unary negation expression node
     * @return the type of the expression
     */
    public Object visit(UnaryNegExpr node) {
        String op = node.getOpName();
        printString += op;
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
        String op = node.getOpName();
        printString += op;
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
        String name = node.getOpName();
        if(node.isPostfix()) {
            if(inForLoop) {
                node.getExpr().accept(this);
                printString += name;
            }
            else {
                printString += getTab();
                node.getExpr().accept(this);
                printString += name + ";\n";
            }
        }
        else {
            if(inForLoop) {
                printString += name;
                node.getExpr().accept(this);
            }
            else {
                printString += getTab();
                printString += name;
                node.getExpr().accept(this);
                printString += ";\n";
            }
        }
        return null;
    }

    /**
     * Visit a unary decrement expression node
     *
     * @param node the unary decrement expression node
     * @return the type of the expression
     */
    public Object visit(UnaryDecrExpr node) {
        String name = node.getOpName();
        if(node.isPostfix()) {
            if(inForLoop) {
                node.getExpr().accept(this);
                printString += name;
            }
            else {
                printString += getTab();
                node.getExpr().accept(this);
                printString += name + ";\n";
            }
        }
        else {
            if(inForLoop) {
                printString += name;
                node.getExpr().accept(this);
            }
            else {
                printString += getTab();
                printString += name;
                node.getExpr().accept(this);
                printString += ";\n";
            }
        }
        return null;
    }

    /**
     * Visit an int constant expression node
     *
     * @param node the int constant expression node
     * @return the type of the expression
     */
    public Object visit(ConstIntExpr node) {
        String num = node.getConstant();
        printString += num;
        return null;
    }

    /**
     * Visit a boolean constant expression node
     *
     * @param node the boolean constant expression node
     * @return the type of the expression
     */
    public Object visit(ConstBooleanExpr node) {
        String constBool = node.getConstant();
        printString += constBool;
        return null;
    }

    /**
     * Visit a string constant expression node
     *
     * @param node the string constant expression node
     * @return the type of the expression
     */
    public Object visit(ConstStringExpr node) {
        String constString = node.getConstant();
        printString += "\"" + constString + "\"";
        return null;
    }

}


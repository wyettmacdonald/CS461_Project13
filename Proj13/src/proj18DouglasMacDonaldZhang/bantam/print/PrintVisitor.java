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
        if (!curTab.empty()) {
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
    public Object visit(Program node) {
        printString += indentComments(node.getComments());
        node.getClassList().accept(this);
        printString += node.getEndComments();

        return null;
    }


    /**
     * Visit a class node
     *
     * @param node the class node
     * @return result of the visit
     */
    public Object visit(Class_ node) {
//        String parent = node.getParent();
//        // set the currentClass to this class
//        printString += getTab() + "class " + node.getName();
//        if(parent != null) {
//            printString += " extends " + parent;
//        }
//        printString += " {\n";
//        addTab();
//        node.getMemberList().accept(this);
//        curTab.pop();
//        printString += getTab() + "}\n";
        printString += indentComments(node.getComments());
        printString +=  getTab() + node.toString(); //Tia addition - removing new line because comments should handle it
        //printString += "\n" + getTab() + node.toString();
        addTab();
        node.getMemberList().accept(this);
        curTab.pop();
        printString += "\n" + getTab() + "}";
        return null;
    }

    /**
     * Visit a field node
     *
     * @param node the field node
     * @return result of the visit
     */
    public Object visit(Field node) {
//        String type = node.getType();
//        String name = node.getName();
//        Expr expr = node.getInit();
//        printString += getTab() + type + " " + name;
//        if (expr != null) {
//            printString += " = ";
//            expr.accept(this);
//        }
//        printString += ";\n";
        Expr expr = node.getInit();
        printString += indentComments(node.getComments());
        printString +=  getTab() + node.toString(); //Tia addition - removing new line because comments should handle it
        if (expr != null) {
            expr.accept(this);
            printString += ";";
        }
        return null;
    }

    /**
     * Visit a method node
     *
     * @param node the method node
     * @return result of the visit
     */
    public Object visit(Method node) {
//        String returnType = node.getReturnType();
//        String name = node.getName();
//        FormalList list = node.getFormalList();
//        printString += getTab() + returnType + " " + name + "(";
////        for (ASTNode formal: list) {
////            formal.accept(this);
////        }
//        node.getFormalList().accept(this);
//        printString += ") {\n";
//        addTab();
//        node.getStmtList().accept(this);
//        curTab.pop();
//        printString += getTab() + "}\n\n";
        printString += indentComments(node.getComments());
        printString +=  getTab() + node.toString(); //Tia addition - removing new line because comments should handle it
        node.getFormalList().accept(this);
        printString += ") {\n";
        addTab();
        node.getStmtList().accept(this);
        curTab.pop();
        printString += "\n" + getTab() + "}";
        return null;
    }

    /**
     * Visit a formal node
     *
     * @param node the formal node
     * @return result of the visit
     */
    public Object visit(Formal node) {
//        String type = node.getType();
//        String name = node.getName();
        printString += node.toString();
        return null;
    }

    /**
     * Visit a declaration statement node
     *
     * @param node the declaration statement node
     * @return result of the visit
     */
    public Object visit(DeclStmt node) {
//        String type = node.getType();
//        String name = node.getName();
        printString += indentComments(node.getComments());
        printString +=  getTab() + node.toString(); //Tia addition - removing new line because comments should handle it
        Expr initExpr = node.getInit();
        if (initExpr != null) {
            initExpr.accept(this);
            printString += ";";
        }
        return null;
    }

    /**
     * Visit an if statement node
     *
     * @param node the if statement node
     * @return result of the visit
     */
    public Object visit(IfStmt node) {
        printString += "\n" + getTab() + "if( ";
        node.getPredExpr().accept(this);
        printString += ") {\n";
        addTab();
        node.getThenStmt().accept(this);
        curTab.pop();
        printString += "\n" + getTab() + "}";
        Stmt stmt = node.getElseStmt();
        if (stmt != null) {
            printString += getTab() + "else {\n";
            addTab();
            stmt.accept(this);
            curTab.pop();
            printString += "\n" + getTab() + "}";
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
        printString += "\n" + getTab() + "while(";
        node.getPredExpr().accept(this);
        printString += ") {\n";
        addTab();
        node.getBodyStmt().accept(this);
        curTab.pop();
        printString += "\n" + getTab() + "}";
        return null;
    }

    /**
     * Visit a for statement node
     *
     * @param node the for statement node
     * @return result of the visit
     */
    public Object visit(ForStmt node) {
        printString += "\n" + getTab() + "for(";
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
        printString += "\n" + getTab() + "}";
        return null;
    }

    /**
     * Visit a break statement node
     *
     * @param node the break statement node
     * @return result of the visit
     */
    public Object visit(BreakStmt node) {
        printString += "\n" + getTab() + "break;";
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
        printString += "\n" + getTab() + "return";
        if (expr != null) {
            node.getExpr().accept(this);
        }
        printString += ";";
        return null;
    }

    /**
     * Visit a dispatch expression node
     *
     * @param node the dispatch expression node
     * @return the type of the expression
     */
    public Object visit(DispatchExpr node) {
//        System.out.println(node.getMethodName() + " " + parens);
        String name = node.getMethodName();
        Expr expr = node.getRefExpr();
        printString += "\n" + getTab();
        if (expr != null) {
            if (node.hasParens()) {
                printString += "(";
                expr.accept(this);
                printString += ").";
            } else {
                expr.accept(this);
                printString += ".";
            }
        }
        printString += name + "(";
        node.getActualList().accept(this);
        printString += ");";
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
        if (node.hasParens()) {
            printString += "(new " + type + "())";
        } else {
            printString += "new " + type + "()";
        }
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
        if (node.hasParens()) {
            printString += "( ";
            node.getExpr().accept(this);
            printString += " instanceof" + type + " )";
        } else {
            node.getExpr().accept(this);
            printString += " instanceof " + type;
        }
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
        if (node.hasParens()) {
            printString += "(cast(" + type + ", ";
            node.getExpr().accept(this);
            printString += "))";

        } else {
            printString += "cast(" + type + ", ";
            node.getExpr().accept(this);
            printString += ")";
        }
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
        printString += indentComments(node.getComments());
//        printString += "\n" + getTab() + node.toString();
        printString += "\n" + getTab();
        if (refName != null) {
            printString += refName + ".";
        }
        printString += varName + " = ";
        if (node.hasParens()) {
            printString += "(";
            node.getExpr().accept(this);
            printString += ");";
        } else {
            node.getExpr().accept(this);
            printString += ";";
        }
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
        if (expr != null) {
            if (node.hasParens()) {
                printString += "(";
                node.getRef().accept(this);
                printString += "." + varName + ")";
            } else {
                node.getRef().accept(this);
                printString += "." + varName;
            }
        }
        else {
            if (node.hasParens()) {
                printString += "(" + varName + ")";
            } else {
                printString += varName;
            }
        }

        //Tia addition

//        printString += node.toString();

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
        printString += indentComments(node.getComments());
//        printString += node.toString();
        if (node.hasParens()) {
            printString += " (new " + node.getType() + "[" + node.getSize().toString() + "]";
        } else {
            printString += " new " + node.getType() + "[" + node.getSize().toString() + "]";
        }
        return null;
    }

    /**
     * Visit an array expression node
     *
     * @param node the array expression node
     * @return the type of the expression
     */
    public Object visit(ArrayExpr node) {
        printString += indentComments(node.getComments());
//        printString += node.toString();
        Expr expr = node.getRef();
        if (expr != null) {
            if (node.hasParens()) {
                printString += "(";
                expr.accept(this);
                if (node.getName() != null) {
                    printString += "." + node.getName() + "[";
                    node.getIndex().accept(this);
                    printString += "])";
                }
                else {
                    printString += "[";
                    node.getIndex().accept(this);
                    printString += "])";
                }
            }
            else {
                expr.accept(this);
                if (node.getName() != null) {
                    printString += "." + node.getName() + "[";
                    node.getIndex().accept(this);
                    printString += "]";
                }
                else {
                    printString += "[";
                    node.getIndex().accept(this);
                    printString += "]";
                }
            }
        }
        else {
            if (node.hasParens()) {
                printString += "(" + node.getName() + "[";
                node.getIndex().accept(this);
                printString += "])";
            }
            else {
                printString += node.getName() + "[";
                node.getIndex().accept(this);
                printString += "]";
            }
        }
        return null;
    }

    /**
     * Helper method for generating BinaryExpr
     *
     * @param node BinaryExpr node
     * @param op Operator for the node
     */
    public void visitBinary(BinaryExpr node, String op) {
        if(node.hasParens()) {
            printString += "(";
            node.getLeftExpr().accept(this);
            printString += " " + op + " ";
            node.getRightExpr().accept(this);
            printString += ")";
        }
        else {
            node.getLeftExpr().accept(this);
            printString += " " + op + " ";
            node.getRightExpr().accept(this);
        }
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
//        printString += node.toString();
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
        if(node.hasParens()) {

            printString += "(" + op;
            node.getExpr().accept(this);
            printString += ")";
        }
        else {
            printString += op;
            node.getExpr().accept(this);
        }
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
        if(node.hasParens()) {

            printString += "(" + op;
            node.getExpr().accept(this);
            printString += ")";
        }
        else {
            printString += op;
            node.getExpr().accept(this);
        }
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
                if(node.hasParens()) {
                    printString += "(";
                    node.getExpr().accept(this);
                    printString += name + ")";
                }
                else {
                    node.getExpr().accept(this);
                    printString += name;
                }
            }
            else {
                if(node.hasParens()) {
                    printString += "\n" + getTab() + "(";
                    node.getExpr().accept(this);
                    printString += name + ");";
                }
                else {
                    printString += "\n" + getTab();
                    node.getExpr().accept(this);
                    printString += name + ";";
                }
            }
        }
        else {
            if(inForLoop) {
                if(node.hasParens()) {
                    printString += "(" + name;
                    node.getExpr().accept(this);
                    printString += ")";
                }
                else {
                    printString += name;
                    node.getExpr().accept(this);
                }
            }
            else {
                if(node.hasParens()) {
                    printString += "\n" + getTab() + "(" + name;
                    node.getExpr().accept(this);
                    printString += ";";
                }
                else {
                    printString += "\n" + getTab();
                    printString += name;
                    node.getExpr().accept(this);
                    printString += ";";
                }
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
                if(node.hasParens()) {
                    printString += "(";
                    node.getExpr().accept(this);
                    printString += name + ")";
                }
                else {
                    node.getExpr().accept(this);
                    printString += name;
                }
            }
            else {
                if(node.hasParens()) {
                    printString += "\n" + getTab() + "(";
                    node.getExpr().accept(this);
                    printString += name + ");";
                }
                else {
                    printString += "\n" + getTab();
                    node.getExpr().accept(this);
                    printString += name + ";";
                }
            }
        }
        else {
            if(inForLoop) {
                if(node.hasParens()) {
                    printString += "(" + name;
                    node.getExpr().accept(this);
                    printString += ")";
                }
                else {
                    printString += name;
                    node.getExpr().accept(this);
                }
            }
            else {
                if(node.hasParens()) {
                    printString += "\n" + getTab() + "(" + name;
                    node.getExpr().accept(this);
                    printString += ";";
                }
                else {
                    printString += "\n" + getTab();
                    printString += name;
                    node.getExpr().accept(this);
                    printString += ";";
                }
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
        if(node.hasParens()) {
            printString += "(" + num + ")";
        }
        else {
            printString += num;
        }
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
        if(node.hasParens()) {
            printString += "(" + constBool + ")";
        }
        else {
            printString += constBool;
        }
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
        if(node.hasParens()) {
            printString += "(\"" + constString + "\")";
        }
        else {
            printString += "\"" + constString + "\"";
        }
        return null;
    }


    private String indentComments(String comments){
        String indentedComments = "";
        String[] commentsByLine = comments.split("\n");
        for(int i = 0; i < commentsByLine.length; i++){
            indentedComments += getTab() + commentsByLine[i] + "\n";
        }
        return indentedComments;
    }

}


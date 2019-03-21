/*
 * File: FindDeclarationUsesVisitor.java
 * CS461 Project 13
 * Names: Wyett MacDonald, Kyle Douglas, Tia Zhang
 * Data: 2/28/19
 * This file contains the FindDelcarationUses class, handling the ability to find method, field and var uses.
 */
package proj15DouglasMacDonaldZhang.bantam.semant;

import proj15DouglasMacDonaldZhang.bantam.ast.Program;
import proj15DouglasMacDonaldZhang.bantam.visitor.Visitor;
import proj15DouglasMacDonaldZhang.bantam.ast.*;

import java.util.*;

public class FindDeclarationUsesVisitor extends Visitor {

    /**
     * Root of the AST
     */
    private Program program;

    // find declaration fields
    private String declarationName;
    private Hashtable<Integer, String> usagesFound = new Hashtable<>();

    /**
     * Sets the tab pane.
     *
     * @param declarationName Selected declaration
     */
    public void setJavaTabPane(String declarationName) {
        this.declarationName = declarationName;
    }

    /**
     * If there is a usage, it will be found
     */
    public void handleFindUses(Program program) {
        this.program = program;
        findDeclarationUses();
//        program.accept(this);
//            getSelectedText();
    }

    /**
     * Adds the all usagesFound from usagesFound Hashtable to a String
     *
     * @return theUses a String of all uses
     */
    public String getUses() {
        String theUses = "";
        Set<Integer> lineNumbers = usagesFound.keySet();
        for (Integer key : lineNumbers) {
            theUses += "Line " + key + ", type " + usagesFound.get(key) + "\n";
        }
        return theUses;
    }

    /**
     * Accepts the program
     */
    public void findDeclarationUses() {
        program.accept(this);
    }

    /**
     * Visits ASTNodes
     *
     * @param node the class list node
     * @return null
     */
    public Object visit(ClassList node) {
        for (ASTNode aNode : node) {
            aNode.accept(this);
        }
        return null;
    }

    /**
     * Visits Class node
     *
     * @param node the class node
     * @return null
     */
    public Object visit(Class_ node) {
        node.getMemberList().accept(this);
        return null;
    }

    /**
     * Visits ASTNode from MemberList
     *
     * @param node the member list node
     * @return null
     */
    public Object visit(MemberList node) {
        for (ASTNode child : node) {
            child.accept(this);
        }
        return null;
    }

    /**
     * Visits a Field node
     *
     * @param node the field node
     * @return null
     */
    public Object visit(Field node) {
        if(node.getName().equals(declarationName)) {
            usagesFound.put(node.getLineNum(), node.getType());
        }
        return null;
    }

    /**
     * Visits a Method node
     *
     * @param node the method node
     * @return null
     */
    public Object visit(Method node) {
        node.getFormalList().accept(this);
        node.getStmtList().accept(this);
        return null;
    }

    /**
     * Visits a FormalList node
     *
     * @param node the formal list node
     * @return null
     */
    public Object visit(FormalList node) {
        for (Iterator it = node.iterator(); it.hasNext(); )
            ((Formal) it.next()).accept(this);
        return null;
    }

    /**
     * Visits a Formal node, checks if node is the declaredName
     *
     * @param node the formal node
     * @return null
     */
    public Object visit(Formal node) {
        if(node.getName().equals(declarationName)) {
            usagesFound.put(node.getLineNum(), node.getType());
        }
        return null;
    }

    /**
     * Visits a DeclStmt node
     *
     * @param node the declaration statement node
     * @return node
     */
    public Object visit(DeclStmt node) {
        node.getInit().accept(this);
        super.visit(node);
        return null;
    }

    /**
     * Visit a VarExpr node
     *
     * @param node the variable expression node
     * @return null
     */
    public Object visit(VarExpr node) {
        if(node.getName().equals(declarationName)) {
            usagesFound.put(node.getLineNum(), node.getExprType());
        }
        return null;
    }

    /**
     * Visits a StmtList node
     *
     * @param node the statement list node
     * @return null
     */
    public Object visit(StmtList node) {
        for (Iterator it = node.iterator(); it.hasNext(); ) {
            ((Stmt) it.next()).accept(this);
        }
        return null;
    }

    /**
     * Visit an AssignExpr node
     *
     * @param node the assignment expression node
     * @return null
     */
    public Object visit(AssignExpr node) {
        if(node.getName().equals(declarationName)) {
            usagesFound.put(node.getLineNum(), node.getExprType());
        }
        node.getExpr().accept(this);
        return null;
    }

    /**
     * Visit an ExprStmt node
     *
     * @param node the expression statement node
     * @return null
     */
    public Object visit(ExprStmt node) {
        node.getExpr().accept(this);
        super.visit(node);
        return null;
    }

    /**
     * Visit a ForStmt node
     *
     * @param node the for statement node
     * @return null
     */
    public Object visit(ForStmt node) {
        super.visit(node);
        return null;
    }

    /**
     * Visit a WhileStmt node
     *
     * @param node the while statement node
     * @return null
     */
    public Object visit(WhileStmt node) {
        super.visit(node);
        return null;
    }
}
/*
 * File: GenInnerCode.java
 * Names: Wyett MacDonald
 * Class: CS461
 * Project 16
 * Date: April 7, 2019
 */

package proj16DouglasMacDonaldZhang.codegenmips;

import proj16DouglasMacDonaldZhang.bantam.ast.*;
import proj16DouglasMacDonaldZhang.bantam.util.ClassTreeNode;
import proj16DouglasMacDonaldZhang.bantam.visitor.Visitor;

import java.io.PrintStream;
import java.util.Iterator;

public class GenInnerCode extends Visitor {


    private MipsSupport mipsSupport;

    private PrintStream out;

    private String curClass;

    /**
     * Constructor for GenInnerCode
     *
     * @param mipsSupport mipsSupport object from the MipsCodeGenerator
     * @param out the PrintStream to create the .asm file
     */
    public GenInnerCode(MipsSupport mipsSupport, PrintStream out) {
        this.mipsSupport = mipsSupport;
        this.out = out;
    }

    /**
     * Starts the traversal of the AST
     *
     * @param node ClassTreeNode
     * @return null
     */
    public Object startVisit(ClassTreeNode node) {
        node.getASTNode().accept(this);
        return null;
    }

    /**
     * Visits a Class_ node and stores current class name
     *
     * @param node the class node
     * @return null
     */
    public Object visit(Class_ node) {
        this.curClass = node.getName();
        super.visit(node);
        return null;
    }

    /**
     * Visits a Method node - outputs the method in the form Class.method
     *
     * @param node the method node
     * @return null
     */
    public Object visit(Method node) {
        mipsSupport.genLabel(curClass + "." + node.getName());
        node.getFormalList().accept(this);
        node.getStmtList().accept(this);
        return null;
    }


}

/*
 * File: FindIDsVisitor.java
 * CS461 Project 13
 * Names: Wyett MacDonald, Kyle Douglas, Tia Zhang
 * Data: 3/6/19
 * This file contains the FindIDsVisitor class
 */


package proj13DouglasMacDonaldZhang.bantam.semant;

import proj13DouglasMacDonaldZhang.bantam.ast.*;
import proj13DouglasMacDonaldZhang.bantam.util.ClassTreeNode;
import proj13DouglasMacDonaldZhang.bantam.visitor.Visitor;

import java.util.ArrayList;
import java.util.Hashtable;

public class FindIDsVisitor extends Visitor {


    ArrayList<IdentifierInfo> idsMap;
    Hashtable<String, ClassTreeNode> classMap;
    String currentClass;


    /**
     * Constructor for FindIDsVisitor
     * @param map is the class map from which to get the class nodes which need symbol tables
     */
    public FindIDsVisitor(Hashtable<String, ClassTreeNode> map){
        classMap = map;
        idsMap = new ArrayList<IdentifierInfo>();
    }


    /*
     */
    public ArrayList<IdentifierInfo> collectIdentifiers(Program ast){
        ast.accept(this);
        return idsMap;

    }

    /**
     * Visits Class_ nodes
     * @param node is the Class_ node to be visited
     */
    public Object visit(Class_ node){
        currentClass = node.getName();
        idsMap.add(new IdentifierInfo(currentClass,"Class", node.getLineNum()));
        super.visit(node);
        return null;
    }

    /**
     * Visits Field nodes
     * @param node is the Field node to be visited
     */
    public Object visit(Field node){
        ClassTreeNode treeNode = classMap.get(currentClass);
        String fieldName  = node.getName();
        IdentifierInfo fieldInfo = new IdentifierInfo(node.getName(), "Var", node.getLineNum());
        idsMap.add(fieldInfo);
        return null;
    }


    // TODO figure out how to handle methods of other objects - I don't think that will work until semantic analysis
    /*
     * Visits Method nodes
     * @param node is the Method node to be visited
     */
    public Object visit(Method node){
        ClassTreeNode treeNode = classMap.get(currentClass);
        IdentifierInfo methodInfo = new IdentifierInfo(node.getName(), "Method", node.getLineNum());
        idsMap.add(methodInfo);

        //treeNode.getVarSymbolTable().enterScope();
        super.visit(node);
        return null;
    }

    public Object visit(Formal node){
        ClassTreeNode treeNode = classMap.get(currentClass);
        String paramName  = node.getName();
        IdentifierInfo paramInfo = new IdentifierInfo(node.getName(), "Var", node.getLineNum());
        idsMap.add(paramInfo);
        return null;
    }




    public Object visit(DeclStmt node){
        ClassTreeNode treeNode = classMap.get(currentClass);
        String varName = node.getName();
        IdentifierInfo varInfo = new IdentifierInfo(node.getName(), "Var", node.getLineNum());
        //System.out.println("It's been entered as " + varName + " " + varInfo.getScopeLevel());
        idsMap.add(varInfo);
        super.visit(node);
        return null;
    }













}

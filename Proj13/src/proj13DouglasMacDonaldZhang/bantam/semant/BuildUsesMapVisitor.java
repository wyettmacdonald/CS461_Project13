/*
 * File: FindDeclarationUsesVisitor.java
 * CS461 Project 13
 * Names: Wyett MacDonald, Kyle Douglas, Tia Zhang
 * Data: 2/28/19
 * This file contains the BuildUsesMapVisitor, which builds a map of all the variables and methods in all the classes
 */


package proj13DouglasMacDonaldZhang.bantam.semant;

import proj13DouglasMacDonaldZhang.bantam.ast.*;
import proj13DouglasMacDonaldZhang.bantam.util.ClassTreeNode;
import proj13DouglasMacDonaldZhang.bantam.visitor.Visitor;

import java.util.Hashtable;

public class BuildUsesMapVisitor extends Visitor {


    Hashtable<String, ClassTreeNode> classMap;
    String currentClass;
    Hashtable<ClassTreeNode, Hashtable<String, IdentifierInfo>> classUsesMap;
    Hashtable<String, IdentifierInfo> currentUsesMap;
    int varScopeLevel;

    /*
     * Constructor for BuildUsesMapVisitor
     * @param map is the class map from which to get the class nodes which need to have their identifiers counted
     */
    public BuildUsesMapVisitor(Hashtable<String, ClassTreeNode> map){
        classMap = map;
        classUsesMap = new Hashtable<ClassTreeNode, Hashtable<String, IdentifierInfo>>();
    }


    /*
     */
    public Hashtable<ClassTreeNode, Hashtable<String, IdentifierInfo>> makeMap(Program ast){
        ast.accept(this);
        return classUsesMap;

    }

    /*
     * Visits Class_ nodes
     * @param node is the Class_ node to be visited
     */
    public Object visit(Class_ node){
        currentClass = node.getName();
        ClassTreeNode treeNode = classMap.get(currentClass);
        currentUsesMap = new Hashtable<String, IdentifierInfo>();
        classUsesMap.put(treeNode, currentUsesMap);
        System.out.println("Map for " +currentClass + " was just created and is " + classUsesMap.get(treeNode));

        //treeNode.getMethodSymbolTable().enterScope();
        //treeNode.getVarSymbolTable().enterScope();
        varScopeLevel = 0;

        super.visit(node);
        //treeNode.getMethodSymbolTable().exitScope();
        //treeNode.getVarSymbolTable().exitScope();
        varScopeLevel = 0;
        return null;
    }

    /*
     * Visits Field nodes
     * @param node is the Field node to be visited
     */
    public Object visit(Field node){
        ClassTreeNode treeNode = classMap.get(currentClass);
        String fieldName  = node.getName();
        IdentifierInfo fieldInfo = new IdentifierInfo(node.getName(), "Field", node.getLineNum());
        fieldInfo.setScopeLevel(varScopeLevel);
        fieldInfo.setClassNode(treeNode);
        currentUsesMap.putIfAbsent(fieldName + " " + fieldInfo.getScopeLevel(), fieldInfo );
        return null;
    }


    /*
     * Visits Method nodes
     * @param node is the Method node to be visited
     */
    public Object visit(Method node){
        ClassTreeNode treeNode = classMap.get(currentClass);
        String methodName = node.getName();
        IdentifierInfo methodInfo = new IdentifierInfo(methodName, "Method", node.getLineNum());
        methodInfo.setClassNode(treeNode);
        currentUsesMap.putIfAbsent(methodName, methodInfo);

        //treeNode.getVarSymbolTable().enterScope();
        varScopeLevel ++;
        super.visit(node);
        varScopeLevel --;
        return null;
    }

    public Object visit(Formal node){
        ClassTreeNode treeNode = classMap.get(currentClass);
        String paramName  = node.getName();
        IdentifierInfo paramInfo = new IdentifierInfo(node.getName(), "Var", node.getLineNum());
        paramInfo.setScopeLevel(varScopeLevel);
        //Class tree node is not needed because formals of parents can't be accessed
        currentUsesMap.putIfAbsent(paramName + " " + paramInfo.getScopeLevel(), paramInfo );
        return null;
    }



    public Object visit(ForStmt node){
        //ClassTreeNode treeNode = classMap.get(currentClass);
        //treeNode.getVarSymbolTable().enterScope();
        varScopeLevel++;
        super.visit(node);
        varScopeLevel --;
        return null;
    }

    public Object visit(WhileStmt node){
        //ClassTreeNode treeNode = classMap.get(currentClass);
        //treeNode.getVarSymbolTable().enterScope();
        varScopeLevel ++;
        super.visit(node);
        varScopeLevel --;
        return null;
    }


    public Object visit(DeclStmt node){
        ClassTreeNode treeNode = classMap.get(currentClass);
        String varName = node.getName();
        IdentifierInfo varInfo = new IdentifierInfo(node.getName(),  "Var", node.getLineNum());
        varInfo.setScopeLevel(varScopeLevel);
        //Class tree node is not needed because local vars of parents can't be accessed
        System.out.println("It's been entered as " + varName + " " + varInfo.getScopeLevel());
        currentUsesMap.putIfAbsent(varName + " " + varInfo.getScopeLevel(), varInfo );
        super.visit(node);
        return null;
    }
}

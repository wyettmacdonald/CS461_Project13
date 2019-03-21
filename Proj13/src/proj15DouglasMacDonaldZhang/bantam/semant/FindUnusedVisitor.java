/*
 * File: FindDeclarationUsesVisitor.java
 * CS461 Project 13
 * Names: Wyett MacDonald, Kyle Douglas, Tia Zhang
 * Data: 2/28/19
 * This file contains the FindUnusedVisitor class, which highlights unused variables, fields, and methods
 */


package proj15DouglasMacDonaldZhang.bantam.semant;

import proj15DouglasMacDonaldZhang.bantam.ast.*;
import proj15DouglasMacDonaldZhang.bantam.util.ClassTreeNode;
import proj15DouglasMacDonaldZhang.bantam.visitor.Visitor;

import java.util.Hashtable;

public class FindUnusedVisitor extends Visitor {


    Hashtable<String, ClassTreeNode> classMap;
    String currentClass;
    Hashtable<ClassTreeNode, Hashtable<String, IdentifierInfo>> classUsesMap;
    Hashtable<String, IdentifierInfo> currentUsesMap;
    String unused; //This is a field because it's the only way I've found so far to handle modifying in a for each while using a lambda
    int varScopeLevel;

    /*
     * Constructor for FindUnusedVistor
     * @param map is the class map from which to get the class nodes which need symbol tables
     * @param handler is the error handler which will log any errors found along the way
     */
    public FindUnusedVisitor(Hashtable<String, ClassTreeNode> map,
                             Hashtable<ClassTreeNode, Hashtable<String, IdentifierInfo>> classUsesMap){
        classMap = map;
        this.classUsesMap = classUsesMap;
    }


    /*
     */
    public String checkForUnused(Program ast){
        ast.accept(this);
        unused = "Here are the unused things in this program:\n";
        classUsesMap.forEach( (classNode, usesMap) ->{
                usesMap.forEach( (name, idInfo) ->{
                        if(idInfo.getNumUses() == 0){
                            unused += idInfo.getType() + " " + idInfo.getName() + " on line "
                                    + idInfo.getLineNum() + " was never used\n";
                        }
                    }
                );
            }   //  todo  see if there's a way I can order by line num
        );
        return unused;

    }

    /*
     * Visits Class_ nodes
     * @param node is the Class_ node to be visited
     */
    public Object visit(Class_ node){
        currentClass = node.getName();
        ClassTreeNode treeNode = classMap.get(currentClass);
        //System.out.println("treeNode " + treeNode + " " + currentClass);
        currentUsesMap = classUsesMap.get(treeNode);
        System.out.println("Map for " + currentClass + " is " + currentUsesMap);
        //System.out.println("map " + currentUsesMap + " " + currentClass);
        //if(currentUsesMap == null) return
        //treeNode.getMethodSymbolTable().enterScope();
        //treeNode.getVarSymbolTable().enterScope();
        varScopeLevel = 0;

        super.visit(node);
        //treeNode.getMethodSymbolTable().exitScope();
        //treeNode.getVarSymbolTable().exitScope();
        varScopeLevel = 0;
        return null;
    }




    public Object visit(VarExpr node){
        //System.out.println("Got to a var node");
        ClassTreeNode treeNode = classMap.get(currentClass);
        String varName = node.getName();
        if(node.getRef()!= null){
            System.out.println("Object type " + node.getRef().getExprType() + " for " + varName);
            String objType = node.getRef().getExprType();
            //System.out.println("Class map " + classMap);
            ClassTreeNode objNode = classMap.get(objType); //Since the dummy type is Object, guaranteed to not be null
            Hashtable<String, IdentifierInfo> usesMap = classUsesMap.get(objNode);
            incrementMethodCount(objNode, usesMap, varName);
        }

        else{
            System.out.println("No reference for " + varName + " Map is " + currentUsesMap);
            incrementVarCount(treeNode, currentUsesMap, varName);
        }

        return null;
    }


    public Object visit(DispatchExpr node){
        String methodName = node.getMethodName();
        if(node.getRefExpr()!= null){
            System.out.println("Object type " + node.getRefExpr().getExprType() + " for " + methodName);
            String objType = node.getRefExpr().getExprType();
            ClassTreeNode objNode = classMap.get(objType); //Since the dummy type is Object, guaranteed to not be null
            Hashtable<String, IdentifierInfo> usesMap = classUsesMap.get(objNode);
            incrementMethodCount(objNode, usesMap, methodName);
        }

        else{
            System.out.println("No reference for " + methodName);
            ClassTreeNode treeNode = classMap.get(currentClass);
            incrementMethodCount(treeNode, currentUsesMap, methodName);
        }

        super.visit(node);
        return null;
    }


    private void incrementMethodCount(ClassTreeNode refNode, Hashtable<String, IdentifierInfo> usesMap, String methodName){
        IdentifierInfo methodInfo = usesMap.get(methodName);
        if(methodInfo != null){
            methodInfo.setNumUses(methodInfo.getNumUses() + 1);
        }
        /*else{
            //Check super class
            ClassTreeNode parent = refNode.getParent(); //This will always get at least Object
            Hashtable<String, IdentifierInfo>  parentUsesMap = classUsesMap.get(parent);
            methodInfo = parentUsesMap.get(methodName);
            if(methodInfo != null){
                System.out.println("Incrementing for parent's method " + methodName);
                methodInfo.setNumUses(methodInfo.getNumUses() + 1);
            }
        }*/
        //It's not the job of this visitor to report methods that don't exist
    }

    private void incrementVarCount(ClassTreeNode treeNode, Hashtable<String, IdentifierInfo> map, String varName){
        IdentifierInfo varInfo = null;
        while(varScopeLevel > -1) {
            //System.out.println("Checking " + varName  + " " + varScopeLevel);
            System.out.println("Map " + map);
            varInfo = map.get(varName + " " + varScopeLevel);
            if(varInfo != null){
                System.out.println("Incrementing for " + varName);
                varInfo.setNumUses(varInfo.getNumUses() + 1);
                break;
            }
            varScopeLevel --;

        }
        /*if(varInfo == null){
            ClassTreeNode parent = treeNode.getParent(); //This will always get at least Object
            Hashtable<String, IdentifierInfo>  parentUsesMap = classUsesMap.get(parent);
            varInfo = parentUsesMap.get(varName + " 0"); //Check to see if there's a field
            if(varInfo != null){
                System.out.println("Incrementing for parent's field " + varName);
                varInfo.setNumUses(varInfo.getNumUses() + 1);
            }
        }*/
    }









}

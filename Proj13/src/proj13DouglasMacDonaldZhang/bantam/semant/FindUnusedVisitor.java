/*
 * File: FindUnusedVisitor.java
 * CS461 Project 13
 * Names: Wyett MacDonald, Kyle Douglas, Tia Zhang
 * Data: 2/28/19
 * This file contains the FindUnusedVisitor class, which counts unused variables, fields, and methods
 */


package proj13DouglasMacDonaldZhang.bantam.semant;

import proj13DouglasMacDonaldZhang.bantam.ast.*;
import proj13DouglasMacDonaldZhang.bantam.util.ClassTreeNode;
import proj13DouglasMacDonaldZhang.bantam.visitor.Visitor;

import java.util.ArrayList;
import java.util.Hashtable;

public class FindUnusedVisitor extends Visitor {


    Hashtable<String, ClassTreeNode> classMap;
    String currentClass;
    Hashtable<ClassTreeNode, Hashtable<String, IdentifierInfo>> classUsesMap;
    Hashtable<String, IdentifierInfo> currentUsesMap;
    String unused; //This is a field because it's the only way I've found so far to handle modifying in a for each while using a lambda

    //Shouldn't be needed anymore, not deleting just in case I messed up
    // since I invented and implemented its replacement literally 15 minutes ago -Tia
    //int varScopeLevel;
    ArrayList<String> scopeNameList;

    int varScopeNum;
    ArrayList<String> scopePath;

    /*
     * Constructor for FindUnusedVistor
     * @param map is the class map from which to get the class nodes which need symbol tables
     * @param handler is the error handler which will log any errors found along the way
     */
    public FindUnusedVisitor(Hashtable<String, ClassTreeNode> map,
                             Hashtable<ClassTreeNode, Hashtable<String, IdentifierInfo>> classUsesMap, ArrayList<String> scopeList){
        classMap = map;
        this.classUsesMap = classUsesMap;
        scopeNameList = scopeList;
    }


    /*
    * Collects all the unused methods, variables, and classes
    * @param ast is the Program to be checked for unused things
    * @return a String listing all the unused things. Note that it is not sorted by line num
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

        currentUsesMap = classUsesMap.get(treeNode);
        System.out.println("Map for " + currentClass + " is " + currentUsesMap);

        //varScopeLevel = 0;

        varScopeNum = 0;

        scopePath = new ArrayList<>();

        super.visit(node);

        //varScopeLevel = 0;
        return null;
    }



    /*
     * Visits Method nodes
     * @param node is the Method node to be visited
     */
    public Object visit(Method node){

        varScopeNum ++;
        String scopeName = scopeNameList.get(varScopeNum);
        scopePath.add(scopeName);

        //varScopeLevel ++;
        super.visit(node);
        //varScopeLevel --;

        scopePath.remove(scopeName);

        return null;
    }


    /*
     * Visits ForStmt nodes
     * @param node is the ForStmt node to be visited
     */
    public Object visit(ForStmt node){

        varScopeNum ++;
        String scopeName = scopeNameList.get(varScopeNum);
        scopePath.add(scopeName);


        //varScopeLevel ++;
        super.visit(node);
        //varScopeLevel --;

        scopePath.remove(scopeName);

        return null;
    }

    /*
     * Visits WhileStmt nodes
     * @param node is the WhileStmt node to be visited
     */
    public Object visit(WhileStmt node){

        varScopeNum ++;
        String scopeName = scopeNameList.get(varScopeNum);
        scopePath.add(scopeName);



        //varScopeLevel ++;

        super.visit(node);
        //varScopeLevel--;

        scopePath.remove(scopeName);

        return null;
    }


    /*
     * Visits AssignExpr nodes
     * @param node is the AssignExpr node to be visited
     */
    public Object visit(AssignExpr node){
        String varName = node.getName();
        String refName = node.getRefName();
        Hashtable<String, IdentifierInfo> usesMap;
        ClassTreeNode treeNode;
        if(refName != null){
            if(refName.equals("this")){
                treeNode = classMap.get(currentClass);
            }
            else if (refName.equals("super")){
                treeNode = classMap.get(currentClass).getParent();
            }
            else{
                //This should work to get the left type because the left half has to match the type of the right half
                //And Dale said the extension doesn't have to work if it's semantically incorrect
                treeNode = classMap.get(node.getExpr().getExprType());
            }

        }
        else{
            treeNode = classMap.get(currentClass);
        }

        Hashtable<String, IdentifierInfo> map = classUsesMap.get(treeNode);
        incrementVarCount(map, varName);

        super.visit(node);


        return null;
    }

    /*
     * Visits VarExpr nodes
     * @param node is the VarExpr node to be visited
     */
    public Object visit(VarExpr node){
        System.out.println("Got to var node " + node.getName());
        ClassTreeNode treeNode = classMap.get(currentClass);
        String varName = node.getName();
        if(node.getRef()!= null){
            System.out.println("Object type " + node.getRef().getExprType() + " for " + varName);
            String objType = node.getRef().getExprType();
            //System.out.println("Class map " + classMap);
            ClassTreeNode objNode = classMap.get(objType); //Since the dummy type is Object, guaranteed to not be null
            Hashtable<String, IdentifierInfo> usesMap = classUsesMap.get(objNode);
            incrementVarCount(usesMap, varName);
        }

        else{
            System.out.println("No reference for " + varName + " Map is " + currentUsesMap);
            incrementVarCount(currentUsesMap, varName);
        }

        return null;
    }

    /*
     * Visits DispatchExpr nodes
     * @param node is the DispatchExpr node to be visited
     */
    public Object visit(DispatchExpr node){
        String methodName = node.getMethodName();
        if(node.getRefExpr()!= null){
            System.out.println("Object type " + node.getRefExpr().getExprType() + " for " + methodName);
            String objType = node.getRefExpr().getExprType();
            ClassTreeNode objNode = classMap.get(objType); //Since the dummy type is Object, guaranteed to not be null
            Hashtable<String, IdentifierInfo> usesMap = classUsesMap.get(objNode);
            incrementMethodCount(usesMap, methodName);
        }

        else{
            System.out.println("No reference for " + methodName);
            ClassTreeNode treeNode = classMap.get(currentClass);
            incrementMethodCount(currentUsesMap, methodName);
        }
        super.visit(node);
        return null;
    }


    /*
     * Helper method that increments the usage count for a method.
     * @param usesMap is the map in which the usage count for the method can be found
     * @param methodName is the String identifier to be checked
     */
    private void incrementMethodCount(Hashtable<String, IdentifierInfo> usesMap, String methodName){
        IdentifierInfo methodInfo = usesMap.get(methodName);
        if(methodInfo != null){
            methodInfo.setNumUses(methodInfo.getNumUses() + 1);
        }
    }

    /*
     * Helper method that increments the usage count for a var identifier.
     * @param usesMap is the map in which the usage count for the var can be found
     * @param methodName is the String identifier to be checked
     */
    private void incrementVarCount(Hashtable<String, IdentifierInfo> map, String varName){
        IdentifierInfo varInfo = null;
        int scopeTracker = scopePath.size() - 1;
        System.out.println("Map " + map);
        System.out.println("Checking scope level "+ scopeTracker + " " + varName);
        while(scopeTracker > -1) {
            //System.out.println("Checking " + varName  + " " + varScopeLevel);
            String curScopeName = scopePath.get(scopeTracker);
            System.out.println("Testing " + curScopeName +  " " + varName);
            varInfo = map.get(varName + " " + curScopeName );
            if(varInfo != null){
                System.out.println("Incrementing for " + varName);
                varInfo.setNumUses(varInfo.getNumUses() + 1);
                break;
            }
             scopeTracker--;

        }
    }









}

/*
 * File: BuildUsesMapVisitor.java
 * CS461 Project 13
 * Names: Wyett MacDonald, Kyle Douglas, Tia Zhang
 * Date: 2/28/19
 * This file contains the BuildUsesMapVisitor, which builds a map of all the variables and methods in all the classes
 */


package proj16DouglasMacDonaldZhang.bantam.semant;

import proj16DouglasMacDonaldZhang.bantam.ast.*;
import proj16DouglasMacDonaldZhang.bantam.util.ClassTreeNode;
import proj16DouglasMacDonaldZhang.bantam.visitor.Visitor;

import java.util.ArrayList;
import java.util.Hashtable;


public class BuildUsesMapVisitor extends Visitor {


    Hashtable<String, ClassTreeNode> classMap;
    String currentClass;
    Hashtable<ClassTreeNode, Hashtable<String, IdentifierInfo>> classUsesMap;
    Hashtable<String, IdentifierInfo> currentUsesMap;
    String curScopeName;
    int varScopeNum; //Tracks how many scopes exist total in the program
    int varScopeLevel; //Tracks what scope level the program is at currently
    ArrayList<String> scopeNameList;

    /*
     * Constructor for BuildUsesMapVisitor
     * @param map is the class map from which to get the class nodes which need to have their identifiers counted
     */
    public BuildUsesMapVisitor(Hashtable<String, ClassTreeNode> map){
        classMap = map;
        classUsesMap = new Hashtable<ClassTreeNode, Hashtable<String, IdentifierInfo>>();
        scopeNameList = new ArrayList<>();
    }


    /*
    * Makes maps of the identifiers for all classes in a program, storing them in a bigger map of maps.
    * @param is the Program node that should have maps made
    */
    public Hashtable<ClassTreeNode, Hashtable<String, IdentifierInfo>> makeMap(Program ast){
        ast.accept(this);
        return classUsesMap;

    }

    /*
     * Gets the list of scope names that were found during ast traversal
     * Will return an empty array list if makeMaps() was never called
     * @return ast is the Program node
     */
    public ArrayList<String> getScopeNameList() {
        return scopeNameList;
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

        varScopeNum = 0;
        varScopeLevel = 0;
        curScopeName = "scope no " +  varScopeNum;
        scopeNameList.add(curScopeName);

        super.visit(node);

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
        fieldInfo.setScopeName(curScopeName);
        fieldInfo.setClassNode(treeNode);
        currentUsesMap.putIfAbsent(fieldName + " " + fieldInfo.getScopeName(), fieldInfo );
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
        methodInfo.setClassNode(treeNode); //Collected because I have some ideas for using the parent in the future
        currentUsesMap.putIfAbsent(methodName, methodInfo);

        varScopeNum ++;
        varScopeLevel ++;

        curScopeName = "scope no " +  varScopeNum;
        scopeNameList.add(curScopeName);

        super.visit(node);
        varScopeLevel --;
        curScopeName = scopeNameList.get(varScopeLevel);
        return null;
    }

    /*
     * Visits Formal nodes
     * @param node is the Formal node to be visited
     */
    public Object visit(Formal node){
        ClassTreeNode treeNode = classMap.get(currentClass);
        String paramName  = node.getName();
        IdentifierInfo paramInfo = new IdentifierInfo(node.getName(), "Var", node.getLineNum());
        paramInfo.setScopeName(curScopeName);
        //Class tree node is not needed because formals of parents are always irrelevant
        currentUsesMap.putIfAbsent(paramName + " " + paramInfo.getScopeName(), paramInfo );
        return null;
    }


    /*
     * Visits ForStmt nodes
     * @param node is the ForStmt node to be visited
     */
    public Object visit(ForStmt node){

        varScopeNum++;
        varScopeLevel ++;

        curScopeName = "scope no " +  varScopeNum;
        scopeNameList.add(curScopeName);

        super.visit(node);


        varScopeLevel --;

        curScopeName = scopeNameList.get(varScopeLevel);

        return null;
    }


    /*
     * Visits WhileStmt nodes
     * @param node is the WhileStmt node to be visited
     */
    public Object visit(WhileStmt node){
        varScopeNum ++;
        varScopeLevel ++;

        curScopeName = "scope no " +  varScopeNum;
        scopeNameList.add(curScopeName);

        super.visit(node);
        varScopeLevel--;
        curScopeName = scopeNameList.get(varScopeLevel);
        return null;
    }


    /*
     * Visits DeclStmt nodes
     * @param node is the Method node to be visited
     */
    public Object visit(DeclStmt node){
        ClassTreeNode treeNode = classMap.get(currentClass);
        String varName = node.getName();
        IdentifierInfo varInfo = new IdentifierInfo(node.getName(),  "Var", node.getLineNum());
        varInfo.setScopeName(curScopeName);
        //Class tree node is not needed because local vars of parents are always irrelevant
        System.out.println("It's been entered as " + varName + " " + varInfo.getScopeName());
        currentUsesMap.putIfAbsent(varName + " " + varInfo.getScopeName(), varInfo );
        super.visit(node);
        return null;
    }
}

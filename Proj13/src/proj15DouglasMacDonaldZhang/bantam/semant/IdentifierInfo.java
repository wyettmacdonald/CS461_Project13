/*
 * File: FindDeclarationUsesVisitor.java
 * CS461 Project 13
 * Names: Wyett MacDonald, Kyle Douglas, Tia Zhang
 * Data: 2/28/19
 * This file contains the IdentifierInfo class, which stores information about identifiers
 */



package proj15DouglasMacDonaldZhang.bantam.semant;

import proj15DouglasMacDonaldZhang.bantam.util.ClassTreeNode;

public class IdentifierInfo {
    private String name;
    private ClassTreeNode classNode; //Has no use right now, but I think it could be useful in the future
    private String type; //It should be either Method, Field, Var, or Class
    private int scopeLevel;
    private int numUses;
    private int lineNum;


    public IdentifierInfo(String name, String type, int lineNum){
        this.name = name;
        this.type = type;
        this.numUses = 0;
        this.lineNum = lineNum;
    }


    public String getName() {
        return name;
    }

    public ClassTreeNode getClassNode() {
        return classNode;
    }

    public String getType() {
        return type;
    }

    public int getScopeLevel() {
        return scopeLevel;
    }

    public int getNumUses() {
        return numUses;
    }

    public int getLineNum() {
        return lineNum;
    }

    public void setNumUses(int numUses) {
        this.numUses = numUses;
    }

    public void setClassNode(ClassTreeNode classNode) {
        this.classNode = classNode;
    }

    public void setScopeLevel(int scopeLevel) {
        this.scopeLevel = scopeLevel;
    }
}

/*
 * File: IdentifierInfo.java
 * CS461 Project 13
 * Names: Wyett MacDonald, Kyle Douglas, Tia Zhang
 * Data: 2/28/19
 * This file contains the IdentifierInfo class, which stores information about identifiers
 */



package proj15DouglasMacDonaldZhang.bantam.semant;

import proj15DouglasMacDonaldZhang.bantam.util.ClassTreeNode;

public class IdentifierInfo {
    private String name;
    private ClassTreeNode classNode; //Optional usage. Has no use right now, but I have some idea for future uses -Tia
    private String type; //It should be either Method, Field, Var, or Class. Field is its own cat cause it has fun scoping
    private String scopeName; //Optional usage
    private int numUses; //Optional usage
    private int lineNum;


    /*
    * Constructor for the IdentifierInfo class
    * @param name is the String which is the identifier itself
    * @param type is what kind of identifier it is - Variable, Method, Class, Field
    */
    public IdentifierInfo(String name, String type, int lineNum){
        this.name = name;
        this.type = type;
        this.numUses = 0;
        this.lineNum = lineNum;
    }

    /*
    * @returns the name of an identifier
    */
    public String getName() {
        return name;
    }

    /*
    * @returns the ClassTreeNode where the identifier was located
    * If a class node was never set, this will be null
    */
    public ClassTreeNode getClassNode() {
        return classNode;
    }

    /*
    *@returns a String indicating whether the identifier is a Method, Class, Field, or (other kind of) Variable
    */
    public String getType() {
        return type;
    }

    /*
    * @return the name of the scope. If the scope's name was never set, it will be null.
    */
    public String getScopeName() {
        return scopeName;
    }

    /*
    * @return the number of times the identifier was used.
    * If the identifier info object isn't being used to track usage numbers, it'll return 0
    */
    public int getNumUses() {
        return numUses;
    }


    /*
    * @return the line number that the identifier was located on
    */
    public int getLineNum() {
        return lineNum;
    }

    /*
    * Sets the number of times that the identifier was used.
    * @param numUses is the number that the uses will be set to
    */
    public void setNumUses(int numUses) {
        this.numUses = numUses;
    }

    /*
    * Sets the class tree node of the identifier.
    * @param classNode is the ClassTreeNode it'll be set to
    */
    public void setClassNode(ClassTreeNode classNode) {
        this.classNode = classNode;
    }

    /*
    * Sets the name of the scope of the variable
    * @param scopeName is the name it'll be set to
    */
    public void setScopeName(String scopeName) {
        this.scopeName = scopeName;
    }
}

/*
 * File: FieldCounterVisitor.java
 * Names: Tia Zhang
 * Class: CS 461
 * Project 16
 * Date: April 4, 2019
 */

package proj18DouglasMacDonaldZhang.bantam.codegenmips;

import proj18DouglasMacDonaldZhang.bantam.ast.*;
import proj18DouglasMacDonaldZhang.bantam.util.ClassTreeNode;
import proj18DouglasMacDonaldZhang.bantam.visitor.Visitor;

import java.util.Stack;

/**
* Class which counts the number of fields in a class
* Adapted from the NumLocalVarsVisitor originally written by Zeb Keith-Hardy
*/

public class FieldCounterVisitor extends Visitor {
    private int numFields = 0; //Number of fields in current class


    /**
     * searches a Class_ node for the number of local fields in a class
     * @param classTreeNode ClassTreeNode whose Class_ node is to be searched
     */
    public int getNumFields(ClassTreeNode classTreeNode){
        numFields = 0;
        if(classTreeNode.getName().equals("Object")) {
            return numFields; //Object has no fields;
        }
        else{
            ClassTreeNode curNode;
            Stack<ClassTreeNode> inheritanceStack = new Stack<ClassTreeNode>();
            inheritanceStack.push(classTreeNode);
            //Store the inheritance path of the class.
            while (!(curNode = classTreeNode.getParent()).getName().equals("Object")) {
                inheritanceStack.push(curNode);
                classTreeNode = curNode;
            }

            //Go down the inheritance path and count the fields in each ancestor class and itself
            while (inheritanceStack.size() > 0) {
                curNode = inheritanceStack.pop();
                Class_ classNode = curNode.getASTNode();
                classNode.accept(this);
            }
        }
        return numFields;
    }


    /**
     * Counts and stores the number of fields in a class node
     * @param node the class node
     */
    public Object visit(Class_ node) {
        super.visit(node);
        return null;
    }


    /**
     * count a field and then skip the rest of their tree
     * @param node the field node
     */
    public Object visit(Field node){
        numFields++;
        return null;
    }

}

/*
 * File: FieldCounterVisitor.java
 * Names: Tia Zhang
 * Class: CS 461
 * Project 16
 * Date: April 4, 2019
 */

package proj16DouglasMacDonaldZhang.codegenmips;

import proj16DouglasMacDonaldZhang.bantam.ast.*;
import proj16DouglasMacDonaldZhang.bantam.visitor.Visitor;

import java.util.HashMap;
import java.util.Map;

public class FieldCounterVisitor extends Visitor {
    private int numFields = 0; //Number of fields in current method


    /**
     * searches a Class_ node for the number of local fields in a class
     * @param node Class_ node to be searched
     */
    public int getNumFields(Class_ node){
        numFields = 0;
        node.accept(this);
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

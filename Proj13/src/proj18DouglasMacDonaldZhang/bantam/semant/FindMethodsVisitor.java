package proj18DouglasMacDonaldZhang.bantam.semant;
/*
 * File: StringConstantsVisitor.java
 * Names: Zeb Keith-Hardy, Danqing Zhao, Tia Zhang
 * Class: CS 461
 * Project 11
 * Date: February 13, 2019
 */

import proj18DouglasMacDonaldZhang.bantam.ast.*;
import proj18DouglasMacDonaldZhang.bantam.util.ClassTreeNode;
import proj18DouglasMacDonaldZhang.bantam.visitor.Visitor;

import java.util.Map;
import java.util.HashMap;

//use either lineNum or numMethods to jump back and fourth between methods
//swap second input in map to string and swap input

/**
 * Visitor class that visits the nodes of an AST and puts all of the methods in a map
 */
public class FindMethodsVisitor extends Visitor{
    private String name;
    private int numMethods;
    private int lineNum;
    private Map<String, Integer> MethodsMap; //Map of all methods


    /**
     * Constructor for the StringConstantsVisitor
     */
    public FindMethodsVisitor(String name, int lineNum, int numMethods) {
        this.name = name;
        this.lineNum = lineNum;
        this.numMethods = 0;
    }

    /**
     * Method that maps all the methods in a program
     * The keys in the maps are the method names and the values are an identifier in the form StringConst_[unique number]
     *
     * @param ast is the program whose methods are to be returned. It must be a AST node of type Program
     * @return the map of methods
     */
    public Map<String, Integer> getMethodsMap(Program ast) {
        lineNum = 0;
        //numMethods = 0;
        MethodsMap = new HashMap<>();
        ast.accept(this);
        return MethodsMap;
    }




    /**
     * Method for visiting method nodes
     *
     * @param node is the Method node to be visited
     */
    @Override
    public Object visit(Method node) {
        super.visit(node);
     //   MethodsMap.put(node.getName(), "Method " + numMethods);
        MethodsMap.put(node.getName(), node.getLineNum());

        //It's a terminal node, so there shouldn't be a need to call super.visit()
        numMethods += 1;
        return null;
    }


//    /**
//     * Method for the "visit" to FormalList nodes, which causes it to retreat from the node and skip its children
//     *
//     * @param node is the FormalList node to be visited (and ignored)
//     */
//    @Override
//    public Object visit(FormalList node) {
//        return null;
//    }


}



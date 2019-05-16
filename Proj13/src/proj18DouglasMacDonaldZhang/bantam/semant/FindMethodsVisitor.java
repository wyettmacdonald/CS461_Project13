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

import java.text.Normalizer;
import java.util.ArrayList;
import java.util.Iterator;
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
    private Map<Method, ArrayList<Formal>> docMap;
    private Method currentMethod;
    private ArrayList<Formal> formalList;


    /**
     * Constructor for the StringConstantsVisitor
     */
    public FindMethodsVisitor() {
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
        docMap = new HashMap<>();
        ast.accept(this);
        return MethodsMap;
    }

    public Map<Method, ArrayList<Formal>> getDocMap() {
        return docMap;
    }




    /**
     * Method for visiting method nodes
     *
     * @param node is the Method node to be visited
     */
    @Override
    public Object visit(Method node) {
     //   MethodsMap.put(node.getName(), "Method " + numMethods);
        MethodsMap.put(node.getName(), node.getLineNum());
        currentMethod = node;
        formalList = new ArrayList<>();
        node.getFormalList().accept(this);
        docMap.put(node, formalList);

        //It's a terminal node, so there shouldn't be a need to call super.visit()
        numMethods += 1;
        return null;
    }

    public Object visit(FormalList node) {
        for (Iterator it = node.iterator(); it.hasNext(); )
            ((Formal) it.next()).accept(this);
        return null;
    }

    public Object visit(Formal node) {
        formalList.add(node);
        return null;
    }


}



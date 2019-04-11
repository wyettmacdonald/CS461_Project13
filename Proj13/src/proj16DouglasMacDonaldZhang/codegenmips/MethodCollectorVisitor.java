/*
* Tia Zhang
* CS461 Project 16
* April 4, 2019
* Class for collecting all the methods of a class in correct inheritance order
*/

package proj16DouglasMacDonaldZhang.codegenmips;

import proj16DouglasMacDonaldZhang.bantam.ast.Class_;
import proj16DouglasMacDonaldZhang.bantam.ast.Method;
import proj16DouglasMacDonaldZhang.bantam.util.ClassTreeNode;
import proj16DouglasMacDonaldZhang.bantam.visitor.Visitor;

import java.util.LinkedHashMap;
import java.util.Stack;


/**
* Class which collects all the methods of a class in the correct inheritance order
* from top of the tree going down, with overriden methods taking the ancestor's spot in the order
*/

public class MethodCollectorVisitor extends Visitor {
    private LinkedHashMap<String, String> methodsMap; //Map of method names to the classes they're in
    private String currentClass; //stores the current class in the traversal of the inheritance hierarchy


    /**
    * Collects all the methods of a class in a map
    * @param classTreeNode is the ClassTreeNode of the class whose methods should be mapped
    * @return a LinkedHashMap<String, String> which is the map
    * Map goes from method name to the class the method originally came from
    */
    public LinkedHashMap<String, String> getMethods(ClassTreeNode classTreeNode){
        //Using a LinkedHashMap so I can preserve the insertion order
        methodsMap = new LinkedHashMap();

        methodsMap.put("clone", "Object");
        methodsMap.put("equals", "Object");
        methodsMap.put("toString", "Object");

        if(classTreeNode.getName().equals("Object")) {
            return methodsMap;
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

            //Go down the inheritance path and collect all the methods in each ancestor class and itself
            while (inheritanceStack.size() > 0) {
                curNode = inheritanceStack.pop();
                Class_ classNode = curNode.getASTNode();
                currentClass = classNode.getName();
                classNode.accept(this);
            }
        }

        return methodsMap;
    }

    /**
    * Visits a Method node and adds it to the methods map
    * @param the Method node to be visited
    * @return null
    */
    public Object visit(Method node){
        methodsMap.put(node.getName(), currentClass);
        return null;
    }



}

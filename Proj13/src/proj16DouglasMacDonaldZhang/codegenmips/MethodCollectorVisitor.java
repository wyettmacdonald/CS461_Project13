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

public class MethodCollectorVisitor extends Visitor {
    private LinkedHashMap<String, String> methodsMap;
    private String currentClass;


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
            while (!(curNode = classTreeNode.getParent()).getName().equals("Object")) {
                inheritanceStack.push(curNode);
                classTreeNode = curNode;
            }

            while (inheritanceStack.size() > 0) {
                curNode = inheritanceStack.pop();
                Class_ classNode = curNode.getASTNode();
                currentClass = classNode.getName();
                classNode.accept(this);
            }
        }

        return methodsMap;
    }

    public Object visit(Method node){
        methodsMap.put(node.getName(), currentClass);
        return null;
    }



}

/*
 * Tia Zhang
 * CS461 Project 16
 * April 4, 2019
 * Class for collecting all the classes in a program, including the built-ins.
 */

package proj17DouglasMacDonaldZhang.codegenmips;

import proj17DouglasMacDonaldZhang.bantam.util.ClassTreeNode;

import java.util.List;
import java.util.Vector;

/**
* Class that collects all of the ClassTreeNodes of an inheritance tree in a List
*/

public class ClassCollector {
    private List<ClassTreeNode> classList;

    /**
    * Collects all of the ClassTreeNodes of an inheritance tree
    * @param classList is the list in which it collects the ClassTreeNodes
    * @param objectNode is the root of the class inheritance tree (which is always the Object node)
    */
    public void getAllClasses(List<ClassTreeNode> classList, ClassTreeNode objectNode){
        this.classList = classList;
        classList.add(objectNode);
        addChildren(objectNode);
    }

    /**
    * Helper method which adds all of a node's children to the list and then all the children's children, etc
    * If there are no children, returns
    * @param node is the ClassTreeNode whose children should be added
    */
    private void addChildren(ClassTreeNode node){
        Vector<ClassTreeNode> childrenList = node.getChildrenList();
        if(childrenList.size() == 0){
            return;
        }
        else{
            classList.addAll(childrenList);
            childrenList.forEach(child -> {
                addChildren(child);
            });
        }
    }

}

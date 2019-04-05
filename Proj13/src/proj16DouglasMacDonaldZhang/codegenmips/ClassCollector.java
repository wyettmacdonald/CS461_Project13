/*
 * Tia Zhang
 * CS461 Project 16
 * April 4, 2019
 * Class for collecting all the classes in a program, including the built-ins.
 */

package proj16DouglasMacDonaldZhang.codegenmips;

import proj16DouglasMacDonaldZhang.bantam.util.ClassTreeNode;

import java.util.List;
import java.util.Vector;

public class ClassCollector {
    private List<ClassTreeNode> classList;

    //AbstractList so that Vector can be used too
    public void getAllClasses(List<ClassTreeNode> classList, ClassTreeNode objectNode){
        this.classList = classList;
        getChildren(objectNode);
    }

    private void getChildren(ClassTreeNode node){
        Vector<ClassTreeNode> childrenList = node.getChildrenList();
        if(childrenList.size() == 0){
            return;
        }
        else{
            classList.addAll(childrenList);
            childrenList.forEach(child -> {
                getChildren(child);
            });
        }
    }

}

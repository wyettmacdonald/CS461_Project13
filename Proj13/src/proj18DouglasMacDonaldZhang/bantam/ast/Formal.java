/* Bantam Java Compiler and Language Toolset.

   Copyright (C) 2009 by Marc Corliss (corliss@hws.edu) and 
                         David Furcy (furcyd@uwosh.edu) and
                         E Christopher Lewis (lewis@vmware.com).
   ALL RIGHTS RESERVED.

   The Bantam Java toolset is distributed under the following 
   conditions:

     You may make copies of the toolset for your own use and 
     modify those copies.

     All copies of the toolset must retain the author names and 
     copyright notice.

     You may not sell the toolset or distribute it in 
     conjunction with a commerical product or service without 
     the expressed written consent of the authors.

   THIS SOFTWARE IS PROVIDED ``AS IS'' AND WITHOUT ANY EXPRESS 
   OR IMPLIED WARRANTIES, INCLUDING, WITHOUT LIMITATION, THE 
   IMPLIED WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A 
   PARTICULAR PURPOSE. 
*/

package proj18DouglasMacDonaldZhang.bantam.ast;

import proj18DouglasMacDonaldZhang.bantam.visitor.Visitor;


/**
 * The <tt>Formal</tt> class represents a formal parameter declaration
 * appearing in a method declaration.  It contains a variable type (<tt>type</tt>),
 * and a name (<tt>name</tt>).
 *
 * @see ASTNode
 */
public class Formal extends ASTNode {
    /**
     * The type of the formal parameter
     */
    protected String type;
    /**
     * The name of the formal parameter
     */
    protected String name;

    /**
     * Formal constructor
     *
     * @param lineNum source line number corresponding to this AST node
     * @param type    the type of the formal parameter
     * @param name    the name of the formal parameter
     */
    public Formal(int lineNum, String type, String name, String comments) {
        super(lineNum, comments);
        this.type = type;
        this.name = name;
    }

    /**
     * Get the type of the formal parameter
     *
     * @return type of formal parameter
     */
    public String getType() {
        return type;
    }

    /**
     * Get the name of the formal parameter
     *
     * @return name of formal parameter
     */
    public String getName() {
        return name;
    }

    /**
     * Get the comments associated with this node
     *
     * @return string of comments
     */
    public String getComments() {
        return comments;
    }

    /**
     * toString method to write the node as a string
     *
     * @return a string of bantam java code
     */
    public String toString() {
        return type + " " + name;
    }

    /**
     * Visitor method
     *
     * @param v bantam.visitor object
     * @return result of visiting this node
     * @see proj18DouglasMacDonaldZhang.bantam.visitor.Visitor
     */
    public Object accept(Visitor v) {
        return v.visit(this);
    }
}

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
 * The <tt>NewExpr</tt> class represents a new expression (constructing
 * new objects).  It contains a type name (<tt>type</tt>) to be
 * constructed.
 *
 * @see ASTNode
 * @see Expr
 */
public class NewExpr extends Expr {
    /**
     * The type to be constructed
     */
    protected String type;

    /**
     * NewExpr constructor
     *
     * @param lineNum source line number corresponding to this AST node
     * @param type    the type to be constructed
     */
    public NewExpr(int lineNum, String type,
                   String comments, boolean hasParens) {
        super(lineNum, comments, hasParens);
        this.type = type;
    }

    /**
     * Get the type to be constructed
     *
     * @return type
     */
    public String getType() {
        return type;
    }

    /*
     * Converts into Bantam Java code
     * @return Bantam Java code in String form
     */
    public String toString(){
        return "new " + type + "()"; //Semicolon will come from the statement it's on
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

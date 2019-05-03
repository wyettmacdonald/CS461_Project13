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
 * The <tt>IfStmt</tt> class represents a if-then-else statement
 * appearing in a method declaration.  It contains a predicate expression
 * (<tt>predExpr</tt>), a then statement (<tt>thenStmt</tt>), and an else
 * statement (<tt>elseStmt</tt>).
 *
 * @see ASTNode
 * @see Stmt
 */
public class IfStmt extends Stmt {
    /**
     * The predicate expression
     */
    protected Expr predExpr;

    /**
     * The then statement
     */
    protected Stmt thenStmt;

    /**
     * The else statement
     */
    protected Stmt elseStmt;

    /**
     * IfStmt constructor
     *
     * @param lineNum  source line number corresponding to this AST node
     * @param predExpr the predicate expression
     * @param thenStmt the then statement
     * @param elseStmt the else statement
     */
    public IfStmt(int lineNum, Expr predExpr, Stmt thenStmt, Stmt elseStmt,
                  String comments) {
        super(lineNum, comments);
        this.predExpr = predExpr;
        this.thenStmt = thenStmt;
        this.elseStmt = elseStmt;
    }

    /**
     * Get the predicate expression
     *
     * @return prediate expression
     */
    public Expr getPredExpr() {
        return predExpr;
    }

    /**
     * Get the then statement
     *
     * @return then statement
     */
    public Stmt getThenStmt() {
        return thenStmt;
    }

    /**
     * Set the else statement
     *
     * @param elseStmt the Stmt forming the else part of the if statement.
     */
    public void setElseStmt(Stmt elseStmt) {
        this.elseStmt = elseStmt;
    }

    /**
     * Get the else statement
     *
     * @return else statement
     */
    public Stmt getElseStmt() {
        return elseStmt;
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
     * toString method to write the node as a string. Will not work, leaving it here in case I can figure out a fix in the future -Tia
     *
     * @return a string of bantam java code
     */
    /*public String toString(int numTabs) {
        String tabs = "";
        for(int i = 0; i < numTabs; i++){
            tabs += "\t";
        }
        String ifString = tabs;
        ifString += "if( " + predExpr.toString() +  "){";
        ifString += thenStmt.toString() + "\n}"; //Stmt should begin with a new line character - but that means I can't control the number of tabs. Never mind, ignore this method
        if(elseStmt != null){
            ifString += "\nelse{";
            ifString += elseStmt.toString() + "}";
        }
        return  ifString;
    }*/


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

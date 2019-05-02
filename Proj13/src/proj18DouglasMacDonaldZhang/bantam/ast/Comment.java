/*
 * Tia Zhang
 * CS461 Project 18
 * April 28, 2019
 */


package proj18DouglasMacDonaldZhang.bantam.ast;

import proj18DouglasMacDonaldZhang.bantam.visitor.Visitor;

public class Comment extends ASTNode {
    String commentText;

    public Comment(int lineNum, String commentText) {
        super(lineNum, null, false);
        this.commentText = commentText;
    }

    public Object accept(Visitor v) {
        return v.visit(this);
    }
}


//dvds

///dvfdsf
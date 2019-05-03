/*
 * Parser.java												2.0 1999/08/11
 *
 * Copyright (C) 1999 D.A. Watt and D.F. Brown
 * Dept. of Computing Science, University of Glasgow, Glasgow G12 8QQ Scotland
 * and School of Computer and Math Sciences, The Robert Gordon University,
 * St. Andrew Street, Aberdeen AB25 1HG, Scotland.
 * All rights reserved.
 *
 * This software is provided free for educational use only. It may
 * not be used for commercial purposes without the prior written permission
 * of the authors.
 *
 *
 * Modified by Haoyu Song for a REVISED LL(1) version of Bantam Java
 * The parser is completely recursive descending
 * 		1)Give precedence to operators
 * 		2)Simplify the AST structures
 * 		3)Support more operations and special symbols
 * 		4)Add more keywords to the beginning of expressions and statements and thus make
 * 		the grammar mostly LL(1)
 *
 * Modified by Dale Skrien to clean up the code
 *
 * In the grammar below, the variables are enclosed in angle brackets and
 * "::=" is used instead of "-->" to separate a variable from its rules.
 * The special character "|" is used to separate the rules for each variable.
 * EMPTY indicates a rule with an empty right hand side.
 * All other symbols in the rules are terminals.
 */

package proj18DouglasMacDonaldZhang.bantam.parser;

import proj18DouglasMacDonaldZhang.bantam.ast.*;
import proj18DouglasMacDonaldZhang.bantam.lexer.Scanner;
import proj18DouglasMacDonaldZhang.bantam.lexer.Token;
import proj18DouglasMacDonaldZhang.bantam.util.CompilationException;
import proj18DouglasMacDonaldZhang.bantam.util.Error;
import proj18DouglasMacDonaldZhang.bantam.util.ErrorHandler;

import java.io.Reader;
import java.util.List;

import static proj18DouglasMacDonaldZhang.bantam.lexer.Token.Kind.*;


public class Parser
{
    // instance variables
    private Scanner scanner;
    private Token currentToken;
    private ErrorHandler errorHandler;
    private String comments = "";


    // constructor
    public Parser(ErrorHandler errorHandler) {
        this.errorHandler = errorHandler;
    }

    //----------------------------------
    // checks whether the kind of the current token matches tokenKindExpected.
    // If so, fetches the next token.
    // If not, reports a syntactic bantam.error.

    private void advanceIfMatches(Token.Kind tokenKindExpected) {
        if (currentToken.kind == tokenKindExpected) {
            advance(); // move on to the next token
        }
        else {
            reportSyntacticError(currentToken.position, tokenKindExpected.name(),
                    currentToken.spelling);
        }
    }

    // unconditionally fetch the next token
    //Tia notes: I've currently marked parentheses as impossible for everything
    // until I figure out exactly how the parser would process then
    //CAN HAVE:  constant exprs, new array expressions, super/this, a whole dispatch expression, instanceof, array, math, var on right side
    //CAN'T HAVE: VarExpr on the left side, BlockExpressions, break, DeclStmt, Formal, ExprStmt, anything on left, ReturnStmt
    //  TODO change any nodes that can't have parens outright so the constructor doesn't let you choose

    private void advance() {
        while((currentToken = scanner.scan()).kind == Token.Kind.COMMENT){

            comments += currentToken.spelling;
            //System.out.println(currentToken.spelling);
        }
    }


    //----------------------------------
    //register a SyntaxError and throw a CompilationException to exit from parsing
    private void reportSyntacticError(int position, String expectedToken,
                                      String metToken) {
        String message =
                "At line " + position + ", expected " + expectedToken + ", " + "got" +
                        " " + metToken + " instead.\n ";
        errorHandler.register(Error.Kind.PARSE_ERROR, scanner.getFilename(), position,
                message);
        // exit immediately because the parser can't continue
        throw new CompilationException("Parser error found.");
    }

    /**
     * parse the given file and return the root node of the AST
     *
     * @param filename The name of the Bantam Java file to be parsed
     * @return The Program node forming the root of the AST generated by the parser
     */
    public Program parse(String filename) {

        //set up scanner
        scanner = new Scanner(filename, errorHandler);

        advance();
        return parseProgram();
    }

    // parse the characters in the reader and return the AST
    public Program parse(Reader reader) {

        //set up scanner
        scanner = new Scanner(reader, errorHandler);

        // start scanning and parsing
        advance();
        return parseProgram();
    }


    //------------------------------
    //Begin Parsing

    //<Program> ::= <Class> | <Class> <Program>
    private Program parseProgram() {
        String beginningComments = beginNewComments(); //Comments collected by the parse() method that called parseProgram
        System.out.println("BEGINNING PARSING PROGRAM");
        System.out.println("Begin: " + beginningComments);
        int position = currentToken.position;
        ClassList clist = new ClassList(position);

        while (currentToken.kind != EOF) {
            Class_ aClass = parseClass();
            clist.addElement(aClass);
        }
        // TODO check if you can put parentheses around a Program
        return new Program(position, clist, beginningComments, false, beginNewComments());
    }

    /*
    * Empties out all the currently stored comments
    * @return the stored comments, broken into lines of 80 chars max
    */
    private String beginNewComments(){
        String oldComments = comments;
        if(oldComments.length() > 0) System.out.println("\nToken: + " + currentToken.spelling + " - old comments are " + oldComments + "\n");
        String[] oldCommentsByLine = oldComments.split("\n");
        String reformattedComments = "";
        for(int i = 0; i < oldCommentsByLine.length; i++){
            if(oldCommentsByLine[i].length() > 0){
                reformattedComments += reformat(oldCommentsByLine[i], false) + "\n";
            }
            //TODO figure out a way not to add a new line to an end of file comment
            //if(reformattedComments.length() > 0) System.out.println("Reformed version is " +  reformatComments(oldCommentsByLine[i]) + " a");
        }
        comments = "";
        //if(reformattedComments.length() > 0) System.out.println("\nReformatted comments are " + reformattedComments + "\nDone");
        return reformattedComments;
    }

    /*
    * TODO fill out
    */
    private String reformat(String comments, boolean isString){
        if(comments.length() > 80){
            String reformattedComments = "";
            String shorterLine = "";
            String[] words = comments.split(" ");
            //Split it into words and begin a new line on the word before it reaches 80 chars
            for(int i = 0; i < words.length; i++) {
                //If the word is 80+ chars long, give up on nice splitting and split it every 80 chars
                if(words[i].length() > 80){
                    //Add whatever's previously been stored to the line before handling the stupidly long word
                    if(shorterLine.length() > 0){ //Test to avoid adding a new line char if this is empty
                        reformattedComments = handleCommentOrStringEnding(isString, reformattedComments, shorterLine);
                    }
                    shorterLine = "";

                    String longWord = words[i];
                    while(longWord.length() > 80) {
                        String first80 = longWord.substring(0, 81);
                        reformattedComments = handleCommentOrStringEnding(isString, reformattedComments, first80);
                        longWord = longWord.substring(81);
                    }
                    if(i < words.length -1) {
                        reformattedComments = handleCommentOrStringEnding(isString, reformattedComments, longWord);
                    }
                    else{
                        reformattedComments += longWord;
                    }
                }
                //Else, add words to a line until just before it hits 80 chars per line
                else {
                    if (shorterLine.length() + words[i].length() < 80){
                        shorterLine += words[i];
                        if(i < words.length-1){
                            shorterLine += " ";
                        }
                    }
                    else{
                        reformattedComments = handleCommentOrStringEnding(isString, reformattedComments, shorterLine);
                        shorterLine = words[i]; //Pick up the next line where you left off
                        if(i < words.length-1){
                            shorterLine += " ";
                        }
                    }
                }
            }
            reformattedComments += shorterLine; //Tack on any remnants
            System.out.println("Reformatted string is "+ reformattedComments);
            return reformattedComments;
        }
        else{
            //System.out.println("String is " + string);
            return comments;
        }
    }

    /*
    *TODO fill out
    */
    private String handleCommentOrStringEnding(boolean isString, String curString, String restOfLine){
        if(isString){
            curString += restOfLine + "\" + \n\""; //Close the string, move onto a new line, begin new quote
        }
        else{
            curString += restOfLine + "\n";
        }
        return curString;
    }





    //-----------------------------
    //<Class> ::= CLASS <Identifier> <Extension> { <MemberList> }
    //<Extension> ::= EXTENDS <Identifier> | EMPTY
    //<MemberList> ::= EMPTY | <Member> <MemberList>
    private Class_ parseClass() {
        String beginningComments = beginNewComments();
        Class_ aClass;
        int position = currentToken.position;

        advanceIfMatches(CLASS);
        Token className = currentToken;
        advanceIfMatches(IDENTIFIER);
        String parentName = null;
        if (currentToken.kind == EXTENDS) {
            advance();
            parentName = parseIdentifier();
        }
        else {
            parentName = null; //"Object";
        }

        MemberList memberList = new MemberList(currentToken.position);
        advanceIfMatches(LCURLY);
        while (currentToken.kind != RCURLY && currentToken.kind != EOF) {
            Member member = parseMember();
            memberList.addElement(member);
        }
        advanceIfMatches(RCURLY);

        aClass = new Class_(position, scanner.getFilename(), className.spelling,
                parentName, memberList, beginningComments, false);
        return aClass;
    }


    //-----------------------------------
    //Fields and Methods

    //<Member> ::= <Field> | <Method>
    //<Method> ::= <Type> <Identifier> ( <Parameters> ) <BlockStmt>>
    //<Field> ::= <Type> <Identifier> <OptInitialValue> ;
    //<OptInitialValue> ::= EMPTY | = <Expression>


    private Member parseMember() {
        String beginningComments = beginNewComments();
        Method method;
        String type = parseType();

        String id = parseIdentifier();
        BlockStmt stmt;
        int position = currentToken.position;

        if (currentToken.kind == LPAREN) // it is a method
        {
            advance();
            FormalList parameters = parseParameters();
            advanceIfMatches(RPAREN);
            stmt = (BlockStmt) parseBlock();
            method = new Method(position, type, id, parameters, stmt.getStmtList(), beginningComments, false);
            return method;
        }

        else {
            Expr init = null;

            if (currentToken.kind == ASSIGN) {
                advance();
                init = parseExpression();
            }

            advanceIfMatches(SEMICOLON);

            return new Field(position, type, id, init, beginningComments, false);
        }

    }


    //-----------------------------------
    //<Stmt>::= <IfStmt> | <BlockStmt> | <DeclStmt> | <ReturnStmt>
    //          <ForStmt> | <WhileStmt> | <BreakStmt> | <ExpressionStmt>
    private Stmt parseStatement() {
        Stmt stmt;

        switch (currentToken.kind) {
            case IF:
                stmt = parseIf();
                break;
            case LCURLY:
                stmt = parseBlock();
                break;
            case VAR:
                stmt = parseDeclStmt();
                break;
            case RETURN:
                stmt = parseReturn();
                break;
            case FOR:
                stmt = parseFor();
                break;
            case WHILE:
                stmt = parseWhile();
                break;
            case BREAK:
                stmt = parseBreak();
                break;
            default:
                stmt = parseExpressionStmt();
        }

        return stmt;
    }


    //<WhileStmt>::= WHILE ( <Expression> ) <Stmt>
    private Stmt parseWhile() {
        int position = currentToken.position;
        String beginningComments = beginNewComments();

        advance(); // past "while"
        advanceIfMatches(LPAREN);
        Expr expression = parseExpression();
        advanceIfMatches(RPAREN);
        Stmt execution = parseStatement();

        return new WhileStmt(position, expression, execution, beginningComments, false);
    }


    //<ReturnStmt>::= RETURN <Expression> ; | RETURN ;
    private Stmt parseReturn() {
        String beginningComments = beginNewComments();
        int position = currentToken.position;
        Expr expr = null;

        advance(); // accept the RETURN token

        if (currentToken.kind != SEMICOLON) {
            expr = parseExpression();
        }
        advanceIfMatches(SEMICOLON);

        return new ReturnStmt(position, expr, beginningComments, false);
    }


    //<BreakStmt>::= BREAK ;
    private Stmt parseBreak() {
        String beginningComments = beginNewComments();
        Stmt stmt = new BreakStmt(currentToken.position, beginningComments, false);
        advance();
        advanceIfMatches(SEMICOLON);
        return stmt;
    }


    //<ExpressionStmt>::= <Expression> ;
    private ExprStmt parseExpressionStmt() {
        String beginningComments = beginNewComments();
        int position = currentToken.position;
        Expr expr = parseExpression();
        advanceIfMatches(SEMICOLON);
        return new ExprStmt(position, expr, beginningComments, false);
    }


    //<DeclStmt>::= VAR <Id> = <Expression>;
    //This makes sure that every local variable is initialized
    private Stmt parseDeclStmt() {
        String beginningComments = beginNewComments();

        int position = currentToken.position;
        Stmt stmt;
        advance(); // the keyword var

        String id = parseIdentifier();
        advanceIfMatches(ASSIGN);
        Expr value = parseExpression();

        stmt = new DeclStmt(position, id, value, beginningComments, false);
        advanceIfMatches(SEMICOLON);

        return stmt;
    }


    //<ForStmt>::=FOR ( <Start> ; <Terminate> ; <Increment> ) <STMT>
    //<Start>::= EMPTY | <Expression>
    //<Terminate>::= EMPTY | <Expression>
    //<Increment>::= EMPTY | <Expression>
    private Stmt parseFor() {
        String beginningComments = beginNewComments();

        int position = currentToken.position;
        Expr start = null;
        Expr terminate = null;
        Expr increment = null;
        Stmt execute;
        advance();

        advanceIfMatches(LPAREN);

        //allow the possibility that start,terminate and increment are null
        if (currentToken.kind != SEMICOLON) {
            start = parseExpression();
        }
        advanceIfMatches(SEMICOLON);

        if (currentToken.kind != SEMICOLON) {
            terminate = parseExpression();
        }
        advanceIfMatches(SEMICOLON);

        if (currentToken.kind != RPAREN) {
            increment = parseExpression();
        }
        advanceIfMatches(RPAREN);

        execute = parseStatement();

        return new ForStmt(position, start, terminate, increment, execute, beginningComments, false);
    }


    //<BlockStmt>::=  { <Body> }
    //<Body>::= EMPTY | <Stmt> <Body>
    private Stmt parseBlock() {
        String beginningComments = beginNewComments();

        int position = currentToken.position;
        StmtList stmtList = new StmtList(position);
        advanceIfMatches(LCURLY);

        while (currentToken.kind != RCURLY) {
            stmtList.addElement(parseStatement());
        }
        advanceIfMatches(RCURLY);

        return new BlockStmt(position, stmtList, beginningComments, false);
    }


    //<IfStmt>::= IF (<Expr>) <Stmt> | IF (<Expr>) <Stmt> ELSE <Stmt>
    private Stmt parseIf() {
        String beginningComments = beginNewComments();

        int position = currentToken.position;
        Expr condition;
        Stmt thenStmt;
        Stmt elseStmt = null;

        advance();
        advanceIfMatches(LPAREN);
        condition = parseExpression();
        advanceIfMatches(RPAREN);
        thenStmt = parseStatement();

        if (currentToken.kind == ELSE) {
            advance();
            elseStmt = parseStatement();
        }

        return new IfStmt(position, condition, thenStmt, elseStmt, beginningComments, false);
    }


    //==============================================
    //Expressions
    //Here we introduce the precedence to operations

    //<Expression>::= <LogicalOrExpr> <OptionalAssignment>
    // <OptionalAssignment>::=  = <Expression> | EMPTY
    private Expr parseExpression() {
        Expr result;
        int position = currentToken.position;

        String beginningComments = beginNewComments();

        result = parseOrExpr();
        if (currentToken.kind == ASSIGN && result instanceof VarExpr) {
            advance();
            VarExpr lhs = (VarExpr) result;
            Expr lhsRef = lhs.getRef();
            if(lhsRef != null && (! (lhsRef instanceof VarExpr)
                    || ((VarExpr) lhsRef).getRef() != null))
                reportSyntacticError(position,"a name or a name.name", "expr.name.name");
            Expr right = parseExpression();
            String lhsName = lhs.getName();
            String lhsRefName = (lhs.getRef() == null ? null :
                    ((VarExpr) lhs.getRef()).getName());
            result = new AssignExpr(position, lhsRefName, lhsName, right, beginningComments, false);
        }
        else if (currentToken.kind == ASSIGN && result instanceof ArrayExpr) {
            advance();
            ArrayExpr lhs = (ArrayExpr) result;
            Expr lhsRef = lhs.getRef();
            // lhsRef needs to be a VarExpr whose ref is either null or another
            // VarExpr whose ref is null
            if(! (lhsRef instanceof VarExpr)
                    || ((VarExpr) lhsRef).getRef() != null
                    && ((VarExpr) ((VarExpr) lhsRef).getRef()).getRef() != null)
                reportSyntacticError(position,"a name[expr] or a name.name[expr]",
                        "something else");
            Expr right = parseExpression();
            VarExpr lhsExpr = (VarExpr) lhs.getRef();
            String lhsName = lhsExpr.getName();
            String lhsRefName = (lhsExpr.getRef() == null ? null :
                    ((VarExpr) lhsExpr.getRef()).getName());
            Expr index = lhs.getIndex();
            result = new ArrayAssignExpr(position, lhsRefName, lhsName, index, right, beginningComments, false);
        }

        return result;
    }


    //<LogicalOR>::= <logicalAND> <LogicalORRest>
    //<LogicalORRest>::= || <LogicalAND> <LogicalORRest> | EMPTY
    private Expr parseOrExpr() {
        String beginningComments = beginNewComments();
        int position = currentToken.position;
        Expr left;

        left = parseAndExpr();
        while (currentToken.spelling.equals("||")) {
            advance();
            Expr right = parseAndExpr();
            left = new BinaryLogicOrExpr(position, left, right, beginningComments, false);
        }

        return left;
    }


    //<LogicalAND>::=<ComparisonExpr> <LogicalANDRest>
    //<LogicalANDRest>::= && <ComparisonExpr> <LogicalANDRest> | EMPTY
    private Expr parseAndExpr() {
        String beginningComments = beginNewComments();
        int position = currentToken.position;
        Expr left = parseComparisonExpr();
        while (currentToken.spelling.equals("&&")) {
            advance();
            Expr right = parseComparisonExpr();
            left = new BinaryLogicAndExpr(position, left, right, beginningComments, false);
        }

        return left;
    }


    //<ComparisonExpr>::= <RelationalExpr> <EqualOrNotEqual> <RelationalExpr> |
    //                     <RelationalExpr>
    //<EqualOrNotEqual>::=   == | !=
    private Expr parseComparisonExpr() {
        int position = currentToken.position;
        Expr left = parseRelationalExpr();
        String beginningComments = beginNewComments();

        if (currentToken.spelling.equals("==")) {
            advance();
            Expr right = parseRelationalExpr();
            left = new BinaryCompEqExpr(position, left, right, beginningComments, false);
        }
        else if (currentToken.spelling.equals("!=")) {
            advance();
            Expr right = parseRelationalExpr();
            left = new BinaryCompNeExpr(position, left, right, beginningComments, false);
        }

        return left;
    }


    //<RelationalExpr>::= <AddExpr> | <AddExpr> <ComparisonOp> <AddExpr>
    //<ComparisonOp>::= < | > | <= | >= | INSTANCEOF
    private Expr parseRelationalExpr() {
        int position = currentToken.position;
        Expr left, right;
        String beginningComments = beginNewComments();

        left = parseAddExpr();
        switch (currentToken.spelling) {
            case "<":
                advance();
                right = parseAddExpr();
                return new BinaryCompLtExpr(position, left, right, beginningComments, false);
            case "<=":
                advance();
                right = parseAddExpr();
                return new BinaryCompLeqExpr(position, left, right, beginningComments, false);
            case ">":
                advance();
                right = parseAddExpr();
                return new BinaryCompGtExpr(position, left, right, beginningComments, false);
            case ">=":
                advance();
                right = parseAddExpr();
                return new BinaryCompGeqExpr(position, left, right, beginningComments, false);
            case "instanceof":
                advance();
                String type = parseType();
                return new InstanceofExpr(position, left, type, beginningComments, false);
        }

        return left;
    }


    //<AddExpr>::＝ <MultExpr> <MoreMult>
    //<MoreMult>::= + <MultExpr> <MoreMult> | - <MultiExpr> <MoreMult> | EMPTY
    private Expr parseAddExpr() {
        int position = currentToken.position;
        String beginningComments = beginNewComments();
        Expr left = parseMultExpr();

        while (currentToken.kind == PLUSMINUS) {
            if (currentToken.spelling.equals("+")) {
                advance();
                Expr right = parseMultExpr();
                left = new BinaryArithPlusExpr(position, left, right, beginningComments, false);
            }
            else {
                advance();
                Expr right = parseMultExpr();
                left = new BinaryArithMinusExpr(position, left, right, beginningComments, false);
            }
        }

        return left;
    }


    //<MultiDiv>::= <NewCastOrUnary> <MoreNCU>
    //<MoreNCU>::= * <NewCastOrUnary> <MoreNCU> |
    //             / <NewCastOrUnary> <MoreNCU> |
    //             % <NewCastOrUnary> <MoreNCU> |
    //             EMPTY
    private Expr parseMultExpr() {
        int position = currentToken.position;
        Expr left, right;

        String beginningComments = beginNewComments();

        left = parseNewCastOrUnary();
        while (currentToken.kind == MULDIV) {
            switch (currentToken.spelling) {
                case "/":
                    advance();
                    right = parseNewCastOrUnary();
                    left = new BinaryArithDivideExpr(position, left, right, beginningComments, false);
                    break;
                case "*":
                    advance();
                    right = parseNewCastOrUnary();
                    left = new BinaryArithTimesExpr(position, left, right, beginningComments, false);
                    break;
                case "%":
                    advance();
                    right = parseNewCastOrUnary();
                    left = new BinaryArithModulusExpr(position, left, right, beginningComments, false);
                    break;
            }
        }

        return left;
    }

    //<NewCastOrUnary>::= <NewExpression> | <CastExpression> | <UnaryPrefix>
    private Expr parseNewCastOrUnary() {
        Expr result;

        switch (currentToken.kind) {
            case NEW:
                result = parseNew();
                break;
            case CAST:
                result = parseCast();
                break;
            default:
                result = parseUnaryPrefix();
        }

        return result;
    }


    //<NewExpression>::= NEW <Identifier>() | NEW <Identifier> [ <Expression> ]
    private Expr parseNew() {
        int position = currentToken.position;
        advance();
        String beginningComments = beginNewComments();

        String type = parseIdentifier();
        if (currentToken.kind == LPAREN) {
            advance();
            advanceIfMatches(RPAREN);
            return new NewExpr(position, type, beginningComments, false);
        }
        else {
            advanceIfMatches(LBRACKET);
            Expr sizeExpr = parseExpression();
            advanceIfMatches(RBRACKET);
            return new NewArrayExpr(position, type, sizeExpr, beginningComments, false);
        }
    }


    //<CastExpression>::= <Cast> ( <Type> , <Expression> )
    private Expr parseCast() {
        String beginningComments = beginNewComments();

        Expr castExpression;
        int position = currentToken.position;
        advance();

        advanceIfMatches(LPAREN);
        String type = parseType();
        advanceIfMatches(COMMA);
        Expr expression = parseExpression();
        advanceIfMatches(RPAREN);

        castExpression = new CastExpr(position, type, expression, beginningComments, false);
        return castExpression;
    }


    //<UnaryPrefix>::= <PrefixOp> <UnaryPreFix> | <UnaryPostfix>
    //<PrefixOp>::= - | ! | ++ | --
    private Expr parseUnaryPrefix() {
        int position = currentToken.position;
        Token.Kind kind = currentToken.kind;

        String beginningComments = beginNewComments();

        if (currentToken.spelling.equals("-") || kind == UNARYDECR || kind == UNARYINCR || kind == UNARYNOT) {
            advance();
            Expr expr = parseUnaryPrefix();
            if (kind == PLUSMINUS) {
                return new UnaryNegExpr(position, expr, beginningComments, false);
            }
            else if (kind == UNARYDECR) {
                return new UnaryDecrExpr(position, expr, false, beginningComments, false);
            }
            else if (kind == UNARYINCR) {
                return new UnaryIncrExpr(position, expr, false, beginningComments, false);
            }
            else // kind == UNARYNOT
            {
                return new UnaryNotExpr(position, expr, beginningComments, false);
            }
        }
        else {
            return parseUnaryPostfix();
        }

    }


    //<UnaryPostfix>::= <Primary> <PostfixOp>
    //<PostfixOp>::= ++ | -- | EMPTY
    private Expr parseUnaryPostfix() {

        Expr unary;
        int position = currentToken.position;

        String beginningComments = beginNewComments();

        unary = parsePrimary();
        if (currentToken.kind == UNARYINCR) {
            unary = new UnaryIncrExpr(position, unary, true, beginningComments, false);
            advance();
        }
        else if (currentToken.kind == UNARYDECR) {
            unary = new UnaryDecrExpr(position, unary, true, beginningComments, false);
            advance();
        }

        return unary;
    }


    /*
     * <Primary> ::= ( <Expression> ) <ExprSuffix> | <IntegerConst> | <BooleanConst> |
     *                               <StringConst> <IdSuffix> | <Identifier> <Suffix>
     * <IdSuffix>    ::=  . <Identifier> <Suffix> | EMPTY
     * <IndexSuffix> ::=  [ <Expression> ] <IdSuffix> | EMPTY
     * <DispSuffix>  ::=  ( <Arguments> ) <IdSuffix> | EMPTY
     * <ExprSuffix>  ::=  <IdSuffix> | <IndexSuffix>
     * <Suffix>      ::=  <IdSuffix> | <DispSuffix> | <IndexSuffix>
     */


    /*
     * <Primary> ::= ( <Expression> ) <Suffix> | <IntegerConst> | <BooleanConst> |
     *                               <StringConst> <Suffix> | <Identifier> <Suffix>
     * <Suffix> ::=    . <Identifier> <Suffix>
     *               | [ <Expression> ] <Suffix>
     *               | ( <Arguments> ) <Suffix>
     *               | EMPTY
     */

    /*
     * <Primary> ::= ( <Expression> ) <ExprSuffix> | <IntegerConst> | <BooleanConst> |
     *                               <StringConst> <IdSuffix> | <Identifier> <Suffix>
     * <IdSuffix>    ::=  . <Identifier> <Suffix> | EMPTY
     * <IndexSuffix> ::=  [ <Expression> ] <IdSuffix> | EMPTY
     * <DispSuffix>  ::=  ( <Arguments> ) <IdSuffix> | EMPTY
     * <ExprSuffix>  ::=  <IdSuffix> | <IndexSuffix>
     * <Suffix>      ::=  <IdSuffix> | <DispSuffix> | <IndexSuffix>
     */
    private Expr parsePrimary() {
        Expr primary;

        String beginningComments = beginNewComments();

        switch (currentToken.kind) {
            case INTCONST:
                return parseIntConst();
            case BOOLEAN:
                return parseBoolean();
            case STRCONST:
                primary = parseStringConst();
                break;
            case LPAREN:
                advance();
                primary = parseExpression();
                advanceIfMatches(RPAREN);
                if(currentToken.kind == LPAREN) //cannot have ( expr )( args )
                    reportSyntacticError(currentToken.position,
                            "something other than \"(\"",
                            currentToken.kind.name());
                break;
            default:
                String id = parseIdentifier();
                primary = new VarExpr(currentToken.position, null, id, beginningComments, false);
        }
        // now add the suffixes
        while (    currentToken.kind == DOT
                || currentToken.kind == LPAREN && primary instanceof VarExpr
                || currentToken.kind == LBRACKET) {
            beginningComments = beginNewComments(); //I think technically, only in the first round of looping could this contain anything TODO verify
            if (currentToken.kind == LPAREN) {
                advance();
                ExprList ar = parseArguments();
                advanceIfMatches(RPAREN);
                VarExpr varExpr = (VarExpr) primary;
                primary = new DispatchExpr(primary.getLineNum(), varExpr.getRef(),
                        varExpr.getName(), ar, beginningComments, false);
            }
            else if (currentToken.kind == LBRACKET) {
                advance();
                Expr index = parseExpression();
                advanceIfMatches(RBRACKET);
                primary = new ArrayExpr(primary.getLineNum(), primary, null, index, beginningComments, false);
            }
            else { // currentToken is a DOT
                advance();
                String id = parseIdentifier();
                primary = new VarExpr(currentToken.position, primary, id, beginningComments, false);
            }
        }

        return primary;
    }


    //<Arguments> ::= EMPTY | <Expression> <MoreArgs>
    //<MoreArgs> ::= EMPTY | , <Expression> <MoreArgs>
    private ExprList parseArguments() {
        int position = currentToken.position;

        ExprList ar = new ExprList(position);

        if (currentToken.kind == RPAREN) {
            return ar;
        }
        else {
            ar.addElement(parseExpression());
            while (currentToken.kind != RPAREN) {
                advanceIfMatches(COMMA);
                ar.addElement(parseExpression());
            }
        }

        return ar;
    }


    //<Parameters> ::=  EMPTY | <Formal> <MoreFormals>
    //<MoreFormals> ::= EMPTY | , <Formal> <MoreFormals
    private FormalList parseParameters() {
        int position = currentToken.position;

        FormalList parameters = new FormalList(position);

        if (currentToken.kind == RPAREN) {
            return parameters;
        }
        else {
            parameters.addElement(parseFormal());
            while (currentToken.kind != RPAREN) {
                advanceIfMatches(COMMA);
                parameters.addElement(parseFormal());
            }
        }

        return parameters;
    }


    //<Formal> ::= <Type> <Identifier>
    private Formal parseFormal() {
        String beginningComments = beginNewComments(); //Gotta get comments before the identifiers and such are parsed
        return new Formal(currentToken.position, parseType(), parseIdentifier(), beginningComments, false);
    }


    //<Type> ::= <Identifier> <Brackets>
    //<Brackets> ::= [ ] | EMPTY
    private String parseType() {
        String id = parseIdentifier();

        if (currentToken.kind == LBRACKET) {
            advance();
            advanceIfMatches(RBRACKET);
            id += "[]";
        }

        return id;
    }


    //----------------------------------------
    //Terminals


    private String parseOperator() {
        String op = currentToken.getSpelling();
        advance();
        return op;
    }


    private String parseIdentifier() {
        String name = currentToken.getSpelling();
        advanceIfMatches(IDENTIFIER);
        return name;
    }


    private ConstStringExpr parseStringConst() {
        int position = currentToken.position;
        String spelling = currentToken.spelling;

        String beginningComments = beginNewComments();
        advanceIfMatches(STRCONST);
        return new ConstStringExpr(position, reformat(spelling, true), beginningComments, false);
    }


    private ConstIntExpr parseIntConst() {
        int position = currentToken.position;
        String spelling = currentToken.spelling;

        String beginningComments = beginNewComments();
        advanceIfMatches(INTCONST);
        return new ConstIntExpr(position, spelling, beginningComments, false);
    }


    private ConstBooleanExpr parseBoolean() {
        int position = currentToken.position;
        String spelling = currentToken.spelling;

        String beginningComments = beginNewComments();
        advanceIfMatches(BOOLEAN);
        return new ConstBooleanExpr(position, spelling, beginningComments, false);
    }


    public static void main(String[] args) {
        ErrorHandler errorHandler = new ErrorHandler();
        Parser parser = new Parser(errorHandler);

        args = new String[]{"testsByDale/AAA.txt"};

        for (String inFile : args) {
            System.out.println("\n========== Results for " + inFile + " =============");
            try {
                errorHandler.clear();
                Program program = parser.parse(inFile);
                System.out.println("  Parsing was successful.");
            } catch (CompilationException ex) {
                System.out.println("  There were errors:");
                List<Error> errors = errorHandler.getErrorList();
                for (Error error : errors) {
                    System.out.println("\t" + error.toString());
                }
            }
        }

    }

}


/*
 * @(#)Scanner.java                        2.0 1999/08/11
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
 * Modified by Dale Skrien:
 * 1.  Moved 2 lines of code out of the constructor and into a new
 *     method "setSource()".  This allows the same scanner to be
 *     reused with several source files.
 * 2.  Changed the constructor so that it takes no arguments.
 * 3.  Modified the enableDebugging method to take a boolean parameter.
 * 4.  Added a main method for unit testing purposes.
 * 5.  Added "&& currentChar != '\r' && currentChar != sourceFile.eof"
 *	   to the while loop in scanSeparator().
 * 6.  Changed the "sourceFile.eof" case to "SourceFile.eof".
 *
 *
 * Modifie by Haoyu Song for Bantam Java
 */

package proj18DouglasMacDonaldZhang.bantam.lexer;

import proj18DouglasMacDonaldZhang.bantam.util.CompilationException;
import proj18DouglasMacDonaldZhang.bantam.util.Error;
import proj18DouglasMacDonaldZhang.bantam.util.ErrorHandler;

import java.io.Reader;
import java.math.BigInteger;

import static proj18DouglasMacDonaldZhang.bantam.lexer.Token.Kind.EOF;

public class Scanner
{
    private SourceFile sourceFile;
    private ErrorHandler errorHandler;

    private char currentChar;
    private char prevChar;
    private StringBuffer currentTokenSpelling;
    private boolean currentlyScanningToken;

    public Scanner(ErrorHandler handler) {
        errorHandler = handler;
        sourceFile = null;
        currentChar = ' '; //whitespace
    }

    public Scanner(String filename, ErrorHandler handler) {
        errorHandler = handler;
        currentChar = ' ';
        sourceFile = new SourceFile(filename);
    }

    public Scanner(Reader reader, ErrorHandler handler) {
        errorHandler = handler;
        currentChar = ' ';
        sourceFile = new SourceFile(reader);
    }

    void setSource(SourceFile source) {
        sourceFile = source;
        currentChar = sourceFile.getNextChar();
    }

    public String getFilename() { return sourceFile.getFilename(); }


    private static boolean isLetter(char c) {
        return (c >= 'a' && c <= 'z') || (c >= 'A' && c <= 'Z');
    }

    private static boolean isDigit(char c) {
        return (c >= '0' && c <= '9');
    }

    private static boolean isWhite(char c) {
        return (c == ' ' || c == '\t' || c == '\n' || c == '\r');
    }

    private static boolean legalAfterSlash(char c) {
        return (c == '\\' || c == '\"' || c == 'n' || c == 't' || c == 'f' || c == 'r');
    }

//    private static boolean isOperator(char c) {
//        return (c == '+' || c == '-' || c == '*' || c == '/' || c == '=' || c == '<' || c == '>' || c == '\\' || c == '%' || c == '^' || c == '!' || c == '|' || c == '&');
//    }
//
//    private static boolean isParen(char c) {
//        return (c == '{' || c == '}' || c == '(' || c == ')' || c == '[' || c == ']');
//    }
//
//    private static boolean isPunctuation(char c) {
//        return (c == ',' || c == ';' || c == '.' || c == ':');
//    }


    /*
     * Test whether a character is a legal token i.e, can appear by itself
     */

//    private static boolean isLegalToken(char c) {
//        return (isLetter(c) || isDigit(c) || isWhite(c) || isOperator(c) || isParen(c) || isPunctuation(c) || c == SourceFile.eof);
//    }


    // takeIt appends the current character to the current token, and gets
    // the next character from the source program.
    private void takeIt() {
        if (currentlyScanningToken) {
            currentTokenSpelling.append(currentChar);
        }
        prevChar = currentChar;
        currentChar = sourceFile.getNextChar();
    }


    /*
     * scan a comment
     * Also deal with the bantam.error of unterminated comments
     * Invoked when previous char = '/' and currentChar = '/' or '*'
     */
    private Token.Kind scanComment() {
        if (currentChar == '/') { // line comment
            while (currentChar != '\n' && currentChar != SourceFile.eof)
                takeIt();
            takeIt(); //take the newline or eof char
        }

        else if (currentChar == '*') { // block comment
            while (currentChar != '/' || prevChar != '*') {
                if (currentChar == SourceFile.eof) {
                    int pos = sourceFile.getCurrentLineNumber();
                    errorHandler.register(Error.Kind.LEX_ERROR,
                            sourceFile.getFilename(), pos, "Unterminated block comment");
                    return Token.Kind.ERROR;
                }
                takeIt();
            }
            takeIt();  //TODO determine if this can be removed
            //Tia addition: record all white space after a multiline
            while (isWhite(currentChar)) {
                takeIt();
            }

        }
        return Token.Kind.COMMENT;
    }


    private Token.Kind scanOperator() {
        takeIt();
        String operator = currentTokenSpelling.toString();

        switch (operator) {
            case "=":
                if (currentChar == '=') {
                    takeIt();
                    return Token.Kind.COMPARE;
                }
                else {
                    return Token.Kind.ASSIGN;
                }
            case "!":
                if (currentChar == '=') {
                    takeIt();
                    return Token.Kind.COMPARE;
                }
                else {
                    return Token.Kind.UNARYNOT;
                }
            case "<":
            case ">":
                if (currentChar == '=') {
                    takeIt();
                }
                return Token.Kind.COMPARE;
            case "+":
                if (currentChar == '+') {
                    takeIt();
                    return Token.Kind.UNARYINCR;
                }
                return Token.Kind.PLUSMINUS;
            case "-":
                if (currentChar == '-') {
                    takeIt();
                    return Token.Kind.UNARYDECR;
                }
                return Token.Kind.PLUSMINUS;
            case "*":
            case "/":
            case "%":
                return Token.Kind.MULDIV;
            case "|":
                if (currentChar == '|') {
                    takeIt();
                    return Token.Kind.BINARYLOGIC;
                }
                break;
            case "&":
                if (currentChar == '&') {
                    takeIt();
                    return Token.Kind.BINARYLOGIC;
                }
        }

        // if we haven't returned yet, it is an illegal token
        int pos = sourceFile.getCurrentLineNumber();
        errorHandler.register(Error.Kind.LEX_ERROR, sourceFile.getFilename(), pos,
                "Illegal operator: " + operator);
        return Token.Kind.ERROR;
    }


    /*
     * Scan a string
     * Make sure the string is valid i.e
     *     1) is terminated
     *     2) does NOT span multiple line
     *     3) does NOT have illegal escape character
     *     4) has less than 5000 characters
     */
    private Token.Kind scanString() {
        int pos = sourceFile.getCurrentLineNumber();
        takeIt();

        while (currentChar != '\"' || prevChar == '\\') {

            if (currentChar == SourceFile.eol || currentChar == SourceFile.eof) {
                errorHandler.register(Error.Kind.LEX_ERROR, sourceFile.getFilename(),
                        pos, "String is missing closing " + "quotes on its line");
                return Token.Kind.ERROR;
            }
            else if (prevChar == '\\' && !legalAfterSlash(currentChar)) {
                errorHandler.register(Error.Kind.LEX_ERROR, sourceFile.getFilename(),
                        pos, "Illegal char '" + currentChar + "' after a slash");
                return Token.Kind.ERROR;
            }
            else {
                takeIt();
            }
        }
        takeIt(); // take the closing quotes


        if (currentTokenSpelling.length() > 5002) {
            errorHandler.register(Error.Kind.LEX_ERROR, sourceFile.getFilename(), pos,
                    "String has more than 5000 characters");
            return Token.Kind.ERROR;
        }
        else {
            currentTokenSpelling.deleteCharAt(0);
            currentTokenSpelling.deleteCharAt(currentTokenSpelling.length() - 1);
            return Token.Kind.STRCONST;
        }
    }


    /*
     * Scan an integer
     * Make sure the integer is between 0 and 2^31-1
     */
    private Token.Kind scanInteger() {

        while (isDigit(currentChar)) {
            takeIt();
        }

        BigInteger value = new BigInteger(currentTokenSpelling.toString());
        BigInteger largest = new BigInteger("2147483647");

        if (value.compareTo(largest) > 0) {
            int pos = sourceFile.getCurrentLineNumber();
            errorHandler.register(Error.Kind.LEX_ERROR, sourceFile.getFilename(), pos,
                    "Integer greater than " + "2147483647");
            return Token.Kind.ERROR;
        }
        else {
            return Token.Kind.INTCONST;
        }
    }


    /*
     * scan an identifier
     * make sure the identifier is valid
     * 		1)must start with character
     * 		2)is made of character, integer and underscore
     */
    private Token.Kind scanIdentifier() {
        while ((isLetter(currentChar) || isDigit(currentChar) || currentChar == '_')) {
            takeIt();
        }
        return Token.Kind.IDENTIFIER;
    }


    private Token.Kind scanToken() {
        switch (currentChar) {
            case 'a':
            case 'b':
            case 'c':
            case 'd':
            case 'e':
            case 'f':
            case 'g':
            case 'h':
            case 'i':
            case 'j':
            case 'k':
            case 'l':
            case 'm':
            case 'n':
            case 'o':
            case 'p':
            case 'q':
            case 'r':
            case 's':
            case 't':
            case 'u':
            case 'v':
            case 'w':
            case 'x':
            case 'y':
            case 'z':
            case 'A':
            case 'B':
            case 'C':
            case 'D':
            case 'E':
            case 'F':
            case 'G':
            case 'H':
            case 'I':
            case 'J':
            case 'K':
            case 'L':
            case 'M':
            case 'N':
            case 'O':
            case 'P':
            case 'Q':
            case 'R':
            case 'S':
            case 'T':
            case 'U':
            case 'V':
            case 'W':
            case 'X':
            case 'Y':
            case 'Z':
                return scanIdentifier();
            case '0':
            case '1':
            case '2':
            case '3':
            case '4':
            case '5':
            case '6':
            case '7':
            case '8':
            case '9':
                return scanInteger();
            case '\"':
                return scanString();
            case '/':
                takeIt();
                if (currentChar == '*' || currentChar == '/') {
                    return scanComment();
                }
                else {
                    return Token.Kind.MULDIV;
                }
            case '+':
            case '-':
            case '*':
            case '=':
            case '<':
            case '>':
            case '%':
            case '!':
            case '&':
            case '|':
                return scanOperator();
            case '.':
                takeIt();
                return Token.Kind.DOT;

            case ':':
                takeIt();
                return Token.Kind.COLON;

            case ';':
                takeIt();
                return Token.Kind.SEMICOLON;

            case ',':
                takeIt();
                return Token.Kind.COMMA;
            case '(':
                takeIt();
                return Token.Kind.LPAREN;

            case ')':
                takeIt();
                return Token.Kind.RPAREN;

            case '[':
                takeIt();
                return Token.Kind.LBRACKET;

            case ']':
                takeIt();
                return Token.Kind.RBRACKET;

            case '{':
                takeIt();
                return Token.Kind.LCURLY;

            case '}':
                takeIt();
                return Token.Kind.RCURLY;
            case SourceFile.eof:
                return Token.Kind.EOF;
            case '_': {
                takeIt();
                int pos = sourceFile.getCurrentLineNumber();
                errorHandler.register(Error.Kind.LEX_ERROR, sourceFile.getFilename(),
                        pos, "Identifier starting with _");
                return Token.Kind.ERROR;
            }
            default: {
                int pos = sourceFile.getCurrentLineNumber();
                errorHandler.register(Error.Kind.LEX_ERROR, sourceFile.getFilename(),
                        pos, "Illegal symbol: " + currentChar);
                takeIt();
                return Token.Kind.ERROR;
            }
        }
    }


    public Token scan() {
        // skip whitespace
        while (isWhite(currentChar)) {
            takeIt();
        }

        // scan the token, saving the characters in the instance
        // variable currentTokenSpelling and returning the kind of a token
        int pos = sourceFile.getCurrentLineNumber();
        currentTokenSpelling = new StringBuffer();
        currentlyScanningToken = true;
        Token.Kind kind = scanToken();
        currentlyScanningToken = false;

        //create the Token and spell it
        return new Token(kind, currentTokenSpelling.toString(), pos);
    }

    public static void main(String[] args) {
        ErrorHandler errorHandler = new ErrorHandler();
        Scanner scanner = new Scanner(errorHandler);
        for (String inFile : args) {
            System.out.println("\n========== Results for " + inFile + " =============");
            errorHandler.clear();
            Token currentToken;
            try {
                scanner.setSource(new SourceFile(inFile));
                do { // scan until the end of the file is reached, looking for error tokens
                    currentToken = scanner.scan();
                    System.out.println(currentToken);
                } while (currentToken.kind != EOF);
                if (errorHandler.getErrorList().size() > 0) {
                    System.out.println("  Scanning produced " +
                            errorHandler.getErrorList().size() + " Error tokens");
                }
                else {
                    System.out.println("  Scanning produced 0 Error tokens.");
                }
            } catch (CompilationException ex) {
                System.out.println("  There was error when attempting to read the file" +
                        inFile + ":");
                System.out.println(ex.getMessage());
            }
        }

    }

}

/*
 * File: IDECodeArea.java
 * Names: Zena Abulhab, Paige Hanssen, Kyle Slager, Kevin Zhou
 * Project 5
 * Date: October 12, 2018
 * ---------------------------
 * Edited By: Zeb Keith-Hardy, Michael Li, Iris Lian, Kevin Zhou
 * Project 6/7/9
 * Date: October 26, 2018/ November 3, 2018/ November 20, 2018
 * ---------------------------
 * Edited By: Wyett MacDonald, Tia Zhang
 * Project 15
 * Date: March 22, 2019
 */

package proj16DouglasMacDonaldZhang;

import org.fxmisc.richtext.CodeArea;
import org.fxmisc.richtext.model.StyleSpans;
import org.fxmisc.richtext.model.StyleSpansBuilder;
import org.reactfx.Subscription;

import java.time.Duration;
import java.util.Collection;
import java.util.Collections;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * This class is the controller for all of the toolbar functionality.
 * Specifically the compile, compile and run, and stop buttons
 *
 * @author  Zeb Keith-Hardy, Michael Li, Iris Lian, Kevin Zhou
 * @author  Kevin Ahn, Jackie Hang, Matt Jones, Kevin Zhou
 * @author  Zena Abulhab, Paige Hanssen, Kyle Slager Kevin Zhou
 * @version 2.0
 * @since   10-3-2018
 */
public class IDECodeArea extends CodeArea {

    /**
     * This is the constructor of JavaCodeArea
     *
     * @param type the Style type
     */
    public IDECodeArea(Style type){
        super();
        this.subscribe(type);
    }

    /**
     * Method obtained from the RichTextFX Keywords Demo. Method allows
     * for syntax highlighting after a delay of 500ms after typing has ended.
     * This method was copied from JavaKeyWordsDemo
     * Original Author: Jordan Martinez
     */
    private void subscribe(Style type) {
        // recompute the syntax highlighting 500 ms after user stops editing area
        Subscription codeCheck = this

                // plain changes = ignore style changes that are emitted when syntax highlighting is reapplied
                // multi plain changes = save computation by not rerunning the code multiple times
                //   when making multiple changes (e.g. renaming a method at multiple parts in file)
                .multiPlainChanges()

                // do not emit an event until 500 ms have passed since the last emission of previous stream
                .successionEnds(Duration.ofMillis(500))

                // run the following code block when previous stream emits an event
                .subscribe(ignore -> this.setStyleSpans(0, type.computeHighlighting(this.getText())));
    }
}

    /**
     * source:  https://moodle.colby.edu/pluginfile.php/294745/mod_resource/content/0/JavaKeywordsDemo.java
     * @author  Matt Jones, Kevin Zhou, Kevin Ahn, Jackie Hang
     * @author  Zena Abulhab, Paige Hanssen, Kyle Slager Kevin Zhou
     * @version 3.0
     * @since   09-30-2018
     */
    class JavaStyle implements Style {

        // a list of strings that contain the keywords for the IDE to identify.
        private static final String[] KEYWORDS = new String[]{
                "abstract", "assert", "boolean", "break", "byte",
                "case", "catch", "char", "class", "const",
                "continue", "default", "do", "double", "else",
                "enum", "extends", "final", "finally", "float",
                "for", "goto", "if", "implements", "import",
                "instanceof", "int", "interface", "long", "native",
                "new", "package", "private", "protected", "public",
                "return", "short", "static", "strictfp", "super",
                "switch", "synchronized", "this", "throw", "throws",
                "transient", "try", "void", "volatile", "while", "var"
        };

        // the regex rules for the ide
        private static final String IDENTIFIER_PATTERN = "[a-zA-Z]+[a-zA-Z0-9_]*";
        private static final String FLOAT_PATTERN = "(\\d+\\.\\d+)";
        private static final String INTCONST_PATTERN = "\\d+";
        private static final String KEYWORD_PATTERN = "\\b(" + String.join("|", KEYWORDS) + ")\\b";
        private static final String PAREN_PATTERN = "\\(|\\)";
        private static final String BRACE_PATTERN = "\\{|\\}";
        private static final String BRACKET_PATTERN = "\\[|\\]";
        private static final String SEMICOLON_PATTERN = "\\;";
        private static final String STRING_PATTERN = "\"([^\"\\\\]|\\\\.)*\"";
        private static final String CHAR_PATTERN = "\"([^\'\\\\]|\\\\.)*\'";
        private static final String COMMENT_PATTERN = "//[^\n]*" + "|" + "/\\*(.|\\R)*?\\*/";

        private static final Pattern PATTERN = Pattern.compile(
                "(?<KEYWORD>" + KEYWORD_PATTERN + ")"
                        + "|(?<PAREN>" + PAREN_PATTERN + ")"
                        + "|(?<BRACE>" + BRACE_PATTERN + ")"
                        + "|(?<BRACKET>" + BRACKET_PATTERN + ")"
                        + "|(?<SEMICOLON>" + SEMICOLON_PATTERN + ")"
                        + "|(?<STRING>" + STRING_PATTERN + ")"
                        + "|(?<COMMENT>" + COMMENT_PATTERN + ")"
                        + "|(?<FLOAT>" + FLOAT_PATTERN + ")"
                        + "|(?<INTCONST>" + INTCONST_PATTERN + ")"
                        + "|(?<IDENTIFIER>" + IDENTIFIER_PATTERN + ")"
                        + "|(?<CHARACTER>" + CHAR_PATTERN + ")"

        );

        /**
         * Method to highlight all of the regex rules and keywords.
         * Code obtained from the RichTextFX Demo from GitHub.
         *
         * @param text a string analyzed for proper syntax highlighting
         */
        public StyleSpans<Collection<String>> computeHighlighting(String text) {
            Matcher matcher = PATTERN.matcher(text);
            int lastKwEnd = 0;
            StyleSpansBuilder<Collection<String>> spansBuilder
                    = new StyleSpansBuilder<>();
            while (matcher.find()) {
                String styleClass = matcher.group("KEYWORD") != null ? "keyword" :
                        matcher.group("PAREN") != null ? "paren" :
                                matcher.group("BRACE") != null ? "brace" :
                                        matcher.group("BRACKET") != null ? "bracket" :
                                                matcher.group("SEMICOLON") != null ? "semicolon" :
                                                        matcher.group("STRING") != null ? "string" :
                                                                matcher.group("COMMENT") != null ? "comment" :
                                                                        matcher.group("IDENTIFIER") != null ? "identifier" :
                                                                                matcher.group("INTCONST") != null ? "intconst" :
                                                                                        matcher.group("CHARACTER") != null ? "char" :
                                                                                        null; /* never happens */
                assert styleClass != null;
                spansBuilder.add(Collections.emptyList(), matcher.start() - lastKwEnd);
                spansBuilder.add(Collections.singleton(styleClass), matcher.end() - matcher.start());
                lastKwEnd = matcher.end();
            }
            spansBuilder.add(Collections.emptyList(), text.length() - lastKwEnd);
            return spansBuilder.create();
        }
}

    /**
    * Class to handle the Styling of .asm and .s files
    */
    class MIPSStyle implements Style {

        // a list of strings that contain the keywords for the IDE to identify.
        private static final String[] INSTRUCTIONS = new String[]{
                "move", "abs.d", "abs.s", "add", "add.d", "add.s", "addi",
                "addiu", "addu", "and", "andi", "bc1f", "beq", "bgeq", "bgezal",
                "bgtz", "blez", "bltz", "bltzal", "bne", "break", "c.eq.d",
                "c.eq.s", "c.le.d", "c.le.s", "c.lt.d", "c.lt.s", "ceil.w.d",
                "ceil.w.s", "clo", "clz", "cvt.d.s", "cvt.d.w", "cvt.s.d", "cvt.s.w",
                "cvt.w.d", "cvt.w.s", "div", "div.d", "div.s", "divu", "eret",
                "floor.w.d", "floor.w.s", "j", "jal", "jalr", "jr", "lb", "lbu",
                "ldc1", "lh", "lhu", "ll", "lui", "lw", "lwc1", "lwl", "lwr",
                "madd", "maddu", "mfc0", "mfc1", "mfhi", "mflo", "mov.d", "mov.s",
                "movf", "movf.d", "movf.s", "movn", "movn.d", "movn.s", "movt",
                "movt.d", "movt.s", "movz", "movz.d", "movz.s", "msub", "msubu",
                "mtc0", "mtc1", "mthi", "mtlo", "mul", "mul.d", "mul.s", "mult",
                "multu", "neg.d", "neg.s", "nop", "nor", "or", "ori", "round.w.d",
                "round.w.s", "sb", "sc", "sdc1", "sh", "sll", "sllv", "slt", "slti",
                "sltiu", "sltu", "sqrt.d", "sqrt.s", "sra", "srav", "srl", "srlv",
                "sub", "sub.d", "sub.s", "subu", "sw", "swc1", "swl", "swr", "syscall",
                "teq", "teqi", "tge", "tgei", "tgeiu", "tgeu", "tlt", "tlti", "tltiu",
                "tltu", "tne", "tnei", "trunc.w.d", "trunc.w.s", "xor", "xori", "abs",
                "b", "bge", "bgeu", "bgt", "bgtu", "ble", "bleu", "blt", "bltu", "l.d",
                "l.s", "la", "lb", "lbu", "ld", "ldc1", "li", "mfc1.d", "mtc1.d",
                "mulo", "mulou", "mulu", "neg", "negu", "not", "rem", "remu", "rol",
                "ror", "s.d", "s.s", "sd", "seq", "sge", "sgeu", "sgt", "sgtu", "sle",
                "sleu", "sne", "subi", "ulh", "ulhu", "ulw", "ush", "usw"
        };

        // a list of strings that contain the directives for the IDE to identify
        private static final String[] DIRECTIVES = new String[] {
                "align", "ascii", "asciiz", "byte", "data", "double", "end_macro",
                "eqv", "extern", "float", "globl", "half", "include", "kdata", "ktext",
                "macro", "set", "space", "text", "word"
        };

        // a list of strings that contain the registers for the IDE to identify
        private static final String[] REGISTERS = new String[] {
                "a0", "a1", "0", "zero", "t0", "t1", "t2", "t3", "t4", "t5", "t6", "t7",
                "t8", "t9", "s0", "s1", "s2", "s3", "s4", "s5", "s6", "s7", "a2", "a3",
                "v0", "v1"
        };

        // the regex rules for the ide
        private static final String LABEL_PATTERN = "[a-zA-Z_]+[a-zA-Z0-9_]*:";
        private static final String INSTRUCTION_PATTERN = "\\b(" + String.join("|", INSTRUCTIONS) + ")\\b";
        private static final String DIRECTIVE_PATTERN = "\\.(" + String.join("|", DIRECTIVES) + ")\\b";
        private static final String STRING_PATTERN = "\"([^\"\\\\]|\\\\.)*\"";
        private static final String CHAR_PATTERN = "\"([^\'\\\\]|\\\\.)*\'";
        private static final String COMMENT_PATTERN = "(#)[^\\n\\r]*[\\n\\r]?";
    //                                                    \/\/[^\n\r]+?(?:\*\)|[\n\r])
        private static final String REGISTER_PATTERN = "\\$(" + String.join("|", REGISTERS) + ")\\b";

        private static final Pattern PATTERN = Pattern.compile(
                "(?<INSTRUCTION>" + INSTRUCTION_PATTERN + ")"
                        + "|(?<DIRECTIVE>" + DIRECTIVE_PATTERN + ")"
                        + "|(?<STRING>" + STRING_PATTERN + ")"
                        + "|(?<COMMENT>" + COMMENT_PATTERN + ")"
                        + "|(?<LABEL>" + LABEL_PATTERN + ")"
                        + "|(?<CHARACTER>" + CHAR_PATTERN + ")"
                        + "|(?<REGISTER>" + REGISTER_PATTERN + ")"

        );

        /**
         * Method to highlight all of the regex rules, instructions, directives and registers.
         * Code obtained from the RichTextFX Demo from GitHub.
         *
         * @param text a string analyzed for proper syntax highlighting
         */
        public StyleSpans<Collection<String>> computeHighlighting(String text) {
            Matcher matcher = PATTERN.matcher(text);
            int lastKwEnd = 0;
            StyleSpansBuilder<Collection<String>> spansBuilder
                    = new StyleSpansBuilder<>();
            while (matcher.find()) {
                String styleClass = matcher.group("INSTRUCTION") != null ? "instruction" :
                        matcher.group("DIRECTIVE") != null ? "directive" :
                                matcher.group("REGISTER") != null ? "register" :
                                                            matcher.group("STRING") != null ? "mips_string" :
                                                                    matcher.group("COMMENT") != null ? "mips_comment" :
                                                                            matcher.group("LABEL") != null ? "mips_label" :
                                                                                            matcher.group("CHARACTER") != null ? "char" :
                                                                                                    null; /* never happens */
                assert styleClass != null;
                spansBuilder.add(Collections.emptyList(), matcher.start() - lastKwEnd);
                spansBuilder.add(Collections.singleton(styleClass), matcher.end() - matcher.start());
                lastKwEnd = matcher.end();
            }
            spansBuilder.add(Collections.emptyList(), text.length() - lastKwEnd);
            return spansBuilder.create();
        }
    }
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
package proj17DouglasMacDonaldZhang.bantam.codegenmips;

import proj17DouglasMacDonaldZhang.bantam.ast.Program;
import proj17DouglasMacDonaldZhang.bantam.ast.Class_;
import proj17DouglasMacDonaldZhang.bantam.codegenmips.CodeGenVisitor;
import proj17DouglasMacDonaldZhang.bantam.codegenmips.Instruction;
import proj17DouglasMacDonaldZhang.bantam.parser.Parser;
import proj17DouglasMacDonaldZhang.bantam.semant.NumLocalVarsVisitor;
import proj17DouglasMacDonaldZhang.bantam.semant.SemanticAnalyzer;
import proj17DouglasMacDonaldZhang.bantam.util.ClassTreeNode;
import proj17DouglasMacDonaldZhang.bantam.util.CompilationException;
import proj17DouglasMacDonaldZhang.bantam.util.Error;
import proj17DouglasMacDonaldZhang.bantam.util.ErrorHandler;
import proj17DouglasMacDonaldZhang.bantam.semant.StringConstantsVisitor;

import java.io.FileOutputStream;
import java.io.IOException;
import java.io.PrintStream;
import java.util.*;

/**
 * The <tt>MipsCodeGenerator</tt> class generates mips assembly code
 * targeted for the SPIM or Mars emulators.
 * <p/>
 * This class is incomplete and will need to be implemented by the student.
 */
public class MipsCodeGenerator
{
    /**
     * Root of the class hierarchy tree
     */
    private ClassTreeNode root;

    /**
     * Print stream for output assembly file
     */
    private PrintStream out;

    /**
     * Assembly support object (using Mips assembly support)
     */
    private MipsSupport assemblySupport;

    /**
     * Boolean indicating whether garbage collection is enabled
     */
    private boolean gc = false;

    /**
     * Boolean indicating whether optimization is enabled
     */
    private boolean opt = false;

    /**
     * Boolean indicating whether debugging is enabled
     */
    private boolean debug = false;

    /**
     * for recording any errors that occur.
     */
    private ErrorHandler errorHandler;

    /**
     * for keeping track of the class.methods
     */
    private HashSet<String> classMethodList;

    /**
     * to keep track of where the main method is
     */
    private String theMainMethod;

    /**
     * Map of number of variables
     */
    private Map<String, Integer> varMap;

    /**
     * Map with String as class name and list of fields
     */
    private Map<String, List<String>> classListMap;

    /**
     * Map with String as class name and list of methods
     */
    private Map<String, List<String>> classMethodMap;

    /**
     * MipsCodeGenerator constructor
     *
     * @param errorHandler ErrorHandler to record all errors that occur
     * @param gc      boolean indicating whether garbage collection is enabled
     * @param opt     boolean indicating whether optimization is enabled
     */
    public MipsCodeGenerator(ErrorHandler errorHandler, boolean gc, boolean opt) {
        this.gc = gc;
        this.opt = opt;
        this.errorHandler = errorHandler;
    }

    /**
     * Generate assembly file
     * <p/>
     * In particular, you will need to do the following:
     * 1 - start the data section
     * 2 - generate data for the garbage collector
     * 3 - generate string constants
     * 4 - generate class name table
     * 5 - generate object templates
     * 6 - generate dispatch tables
     * 7 - start the text section
     * 8 - generate initialization subroutines
     * 9 - generate user-defined methods
     * See the lab manual for the details of each of these steps.
     *
     * @param root    root of the class hierarchy tree
     * @param outFile filename of the assembly output file
     */
    public void generate(ClassTreeNode root, Program ast, String outFile) {
        this.root = root;
        classMethodList = new HashSet<>();

        // set up the PrintStream for writing the assembly file.
        try {
            this.out = new PrintStream(new FileOutputStream(outFile));
            this.assemblySupport = new MipsSupport(out);
        } catch (IOException e) {
            // if don't have permission to write to file then throw an exception
            errorHandler.register(Error.Kind.CODEGEN_ERROR, "IOException when writing " +
                    "to file: " + outFile);
            e.printStackTrace();
            throw new CompilationException("Couldn't write to output file.");
        }

        // comment out
        //throw new RuntimeException("MIPS code generator unimplemented");

        this.assemblySupport.genComment("Tia Zhang, Wyett MacDonald, Kyle Douglas");
        this.assemblySupport.genComment("Project 16");
        this.assemblySupport.genComment("April 4, 2019");
        this.assemblySupport.genComment("MIPS auto-generated code");
        this.out.println("\n\n\n");
        this.assemblySupport.genDataStart();
        this.assemblySupport.genLabel("gc_flag");
        this.assemblySupport.genWord("0");


        StringConstantsVisitor stringVisitor = new StringConstantsVisitor();
        Map<String, String> stringMap = stringVisitor.getStringConstants(ast);
        //Add in class names to the String constants


        //Go down the tree and add all the classes to the class list
        //Assuming the root is Object
        //Using a Vector just to be cautious since we have multithreading going on
        List<ClassTreeNode> classList = new Vector<ClassTreeNode>();
        ClassCollector classCollector = new ClassCollector();
        classCollector.getAllClasses(classList, root);


        //Count all the fields for each classes
        classListMap = new HashMap<>();
        FieldCounterVisitor fieldCounter = new FieldCounterVisitor();
        Map<String, Integer> fieldMap = new HashMap<String, Integer>();
        for(int i = 0; i < classList.size(); i ++){
            ClassTreeNode classTreeNode = classList.get(i);
            stringMap.put(classList.get(i).getName(), "ClassName_" + i);
            int numFields = fieldCounter.getNumFields(classTreeNode, classListMap);
            fieldMap.put(classTreeNode.getName(), numFields);
        }
        classListMap = fieldCounter.getClassListMap();


        //Add the filename string to the String map
        Class_ classNode = (Class_) ast.getClassList().get(0);
        String filePath = classNode.getFilename();
        String[] fileParts = filePath.split("\\\\"); //To fix on Windows - \\ to escape in Java and then \\ for regex
        stringMap.put(fileParts[fileParts.length-1], "filename");

        assemblySupport.genComment("String Objects");
        stringMap.forEach( (string, label) -> {
            generateStringObj(string, label);
            out.println();
        });

        assemblySupport.genComment("This is the class name table");
        //Generate class table
        assemblySupport.genLabel("class_name_table");
        for(int i = 0; i < classList.size(); i ++){
            assemblySupport.genWord("ClassName_" + i);
        }

        out.println();

        //In a separate for loop to make Dale happy about the formatting
        for(int i = 0; i < classList.size(); i ++){
            assemblySupport.genGlobal(classList.get(i).getName() + "_template");
        }

        out.println();

        Hashtable<String, Integer> idTable = new Hashtable<>();
        classMethodMap = new HashMap<>();

        //Generate the class templates
        for(int i = 0; i < classList.size(); i ++){
            ClassTreeNode classTreeNode = classList.get(i); //Got a null pointer here once for some reason
            //Retrieving fields count here so that generateClassTemplate can be used independently
            // from fieldMap and FieldCounterVisitor
            int numFields = fieldMap.get(classTreeNode.getName());
            assemblySupport.genComment("Template for " + classList.get(i).getName());
            generateClassTemplate(classTreeNode, i, numFields);
            out.println();
            assemblySupport.genComment("Dispatch table for " + classList.get(i).getName());
            generateDispatchTable(classTreeNode);
            idTable.put(classTreeNode.getName(), i);
            out.println();
        }


        out.println();
        //Generate the globl declarations for the dispatch tables
        for(int j = 0; j < classList.size(); j++){
            assemblySupport.genGlobal(classList.get(j).getName()+"_dispatch_table");
        }

        out.println();

        this.assemblySupport.genTextStart(theMainMethod);
        out.println();

//        for(int i = 0; i < classList.size(); i ++) {
//            out.println(classList.get(i).getName()+"_init:");
//        }

        // Get user defined methods in the from <class_name>.<method_name>

        Iterator it = classMethodList.iterator();
        while (it.hasNext()) {
            out.println(it.next());
        }

        out.println("\tjr $ra");
        out.println();

        this.varMap = new NumLocalVarsVisitor().getNumLocalVars(ast);
        CodeGenVisitor codeGenVisitor = new CodeGenVisitor(errorHandler, root.getClassMap(), assemblySupport, idTable,
                varMap, classListMap, classMethodMap, stringMap);
        ArrayList<Instruction> instrList = new ArrayList<Instruction>();
        codeGenVisitor.generateCode(instrList);
        instrList.forEach(instruction-> {
            out.println(instruction.toString());
        });


        //After code gen is done, exit
        this.assemblySupport.genSyscall(assemblySupport.SYSCALL_EXIT);

        out.flush();
        out.close();

    }

    /**
    * Generates a constant String object for the data section of a MIPS file and writes it to the output stream
    * @param string is the string inside the String object
    * @param label is the String label that should mark the object.
    */
    private void generateStringObj(String string, String label){
        assemblySupport.genLabel(label);
        assemblySupport.genWord("1"); //String is type 1
        //first 4 words are 16 bytes total, 1 byte for each char, 1 byte for null terminator
        int size = 16 + string.length() + 1;
        size = (int) Math.ceil(size/4.0) * 4; //Round up to the nearest 4
        assemblySupport.genWord(Integer.toString(size));
        assemblySupport.genWord("String_dispatch_table");
        assemblySupport.genWord(Integer.toString(string.length()));
        assemblySupport.genAscii(string);
        //assemblySupport.genByte("0");
        //assemblySupport.genAlign();
    }


    /**
     * Generates a class template for the data section of a MIPS file and writes it to the output stream
     * @param classTreeNode is the ClassTreeNode corresponding to the class
     * @param typeNum is an int representing the type of the class.
     * @param numFields is the number of fields in the object
     */
    private void generateClassTemplate(ClassTreeNode classTreeNode, int typeNum, int numFields){
        assemblySupport.genLabel(classTreeNode.getName()+ "_template");
        assemblySupport.genWord(Integer.toString(typeNum));
        //First 3 words are constant and each 4 bytes. Each field is 4 bytes
        int size = 12 +  (4 * numFields);
        size = (int) Math.ceil(size/4.0) * 4; //Round up to the nearest 4
        assemblySupport.genWord(Integer.toString(size));
        assemblySupport.genWord(classTreeNode.getName() + "_dispatch_table");
        for(int i = 0; i < numFields; i++){
            assemblySupport.genWord("0"); //0 is the default value for all fields
        }
    }


    /**
     * Generates class's dispatch template for the data section of a MIPS file and writes it to the output stream
     * @param classTreeNode is the ClassTreeNode corresponding to the class to have its dispatch table generated
     */
    private void generateDispatchTable(ClassTreeNode classTreeNode){
        assemblySupport.genLabel(classTreeNode.getName()+ "_dispatch_table");
        MethodCollectorVisitor methodCollectorVisitor = new MethodCollectorVisitor();
        LinkedHashMap<String, String> methodsList = methodCollectorVisitor.getMethods(classTreeNode);
        ArrayList<String> theList = new ArrayList<>();
        methodsList.forEach((method, className) -> {
            if(!classTreeNode.getClassMap().get(className).isBuiltIn() &&
                    !classMethodList.contains(className + "." + method + ":")) {
                classMethodList.add(className + "." + method + ":");
                String mainString = className + "." + method;
                if(mainString.substring(mainString.length()-5).equals(".main")) {
                    theMainMethod = className + "." + method;
                }
            }
            theList.add(method);
//            theList.add(method);
            assemblySupport.genWord(className + "." + method);
        });
        classMethodMap.put(classTreeNode.getName(), theList);
//        classListMap.put(classTreeNode.getName(), theList);

        //This method can't be generalized that much - it can't be generalized to a HashMap because HashMaps don't
        //have a set order, which is absolutely mandatory for this method to work. I think it's safest just to tie
        //the method to my MethodCollectorVisitor rather than mandate that a param be a LinkedHashMap
        //that already has handled inheritance in insertion order.

    }

    /**
     * Main method
     *
     * @param args
     */
    public static void main(String[] args) {
        args = new String[]{"/Users/wyettmacdonald/Documents/Spring_19/CS461/CS461_Project13/Proj13/src/proj17DouglasMacDonaldZhang/test/GenTest.btm"};
        ErrorHandler errorHandler = new ErrorHandler();
        Parser parser = new Parser(errorHandler);
        SemanticAnalyzer analyzer = new SemanticAnalyzer(errorHandler);
        MipsCodeGenerator mipsCodeGenerator = new MipsCodeGenerator(errorHandler, false, false);
        for (String inFile : args) {
            System.out.println("\n========== MIPS Results for " + inFile + " =============");
            try {
                errorHandler.clear();
                Program program = parser.parse(inFile);
                analyzer.analyze(program);
                ClassTreeNode objectNode = analyzer.getClassMap().get("Object");
                String inFileName = inFile.substring(0, inFile.length()-4);
                mipsCodeGenerator.generate(objectNode, program, inFileName + ".asm");
                System.out.println("  MIPS Generation was successful.");
            } catch (CompilationException ex) {
                System.out.println("  There were errors:");
                List<Error> errors = errorHandler.getErrorList();
                for (Error error : errors) {
                    System.out.println("\t" + error.toString());
                }
            }
        }
        // ... add testing code here ...
    }
}
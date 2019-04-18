/*
* Tia Zhang, Wyett MacDonald, Kyle Douglas
* April 11, 2019
* CS461 Project 17
* Instruction class for MIPS
*/

package proj17DouglasMacDonaldZhang.bantam.codegenmips;

/*
* Instruction class for storing MIPS instructions
*/

import java.util.ArrayList;

public class Instruction {

    private String command; //The action to be performed, named command to help distinguish it from the class
    private String[] operands;
    private ArrayList<String> labels; //A list because there may be empty labels needed //TODO SEE IF THERE IS ANY CASE WHERE THIS IS NEEDED

    /*
    * Constructor for Instruction class
    * @param command is a String representing the instruction to perform (ie move, load, etc)
    * @param op1 is a String that represents the location of the first (possibly only)
    * operand for the instruction or the actual value of the operand
    */
    public Instruction(String command,  ArrayList<String> labels, String... ops) {
        this.command = command;
        operands = ops;
        this.labels = labels;
    }


    /*
    * Prints the full instruction in the form of a string which is valid MIPs code
    */
    public String toString(){
        String mipsInstr = "";
        for(int i = 0; i< labels.size(); i++){
            mipsInstr += labels.get(i) + ":\n";
        }
        mipsInstr += command;
        for(int i = 0; i< operands.length; i++){
            mipsInstr += operands[i];
        }
        return mipsInstr;
    }

}

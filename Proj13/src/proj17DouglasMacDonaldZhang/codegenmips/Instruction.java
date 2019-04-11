/*
* Tia Zhang, Wyett MacDonald, Kyle Douglas
* April 11, 2019
* CS461 Project 17
* Instruction class for MIPS
*/




package proj17DouglasMacDonaldZhang.codegenmips;


/*
* Instruction class for storing MIPS instructions
*/

public class Instruction {

    private String command; //The action to be performed, named command to help distinguish it from the class
    private String operand1;
    private String operand2; //Won't always be used. Jump instructions only have 1 operand
    private String operand3; //Won't always be used.

    /*
    * Constructor for Instruction class
    * @param command is a String representing the instruction to perform (ie move, load, etc)
    * @param op1 is a String that represents the location of the first (possibly only)
    * operand for the instruction or the actual value of the operand
    */
    public Instruction(String command, String op1){
        this.command = command;
        operand1 = op1;
        operand2 = "";
        operand3 = "";
    }

    /*
    * Sets the second operand for the instruction. If not set, it is an empty string.
    * @param op2 is a string that represents the location of the second operand for the instruction
    * or the actual value of the operand
    */
    public void setOperand2(String op2){
        operand2 = op2;
    }


    /*
     * Sets the third operand for the instruction. If not set, it is an empty string.
     * @param op3 is a string that represents the location of the third operand for the instruction
     * or the actual value of the operand
     */
    public void setOperand3(String op3){
        operand3 = op3;
    }


    //TODO delete these getters if not needed

    /*
    * Gets the command to be performed
    * @return a String representing the command
    */
    public String getCommand() {
        return command;
    }

    /*
     * Gets the first operand
     * @return a String representing the first operand for the instruction
     */
    public String getOperand1() {
        return operand1;
    }

    /*
     * Gets the second operand
     * @return a String representing the second operand for the instruction
     * Will be an empty string if never set
     */
    public String getOperand2() {
        return operand2;
    }

    /*
     * Gets the third operand
     * @return a String representing the third operand for the instruction
     * Will be an empty string if never set
     */
    public String getOperand3() {
        return operand3;
    }

    /*
    * Prints the full instruction in the form of a string which is valid MIPs code
    */
    public String toString(){
        return command + " " + operand1 + " " + operand2 + " " + operand3;
    }

}

package mytools;
import java.util.Scanner;

public class Reader {
    
    private static Scanner in = new Scanner(System.in);

    /** Receives input from input stream until appropriate input is found
     *  Only used by GameMain() constructor
     */
    public static String receiveValidInput(int validInputLength, String[] validCharsPerPosition) {
        while (true) {
            String input = in.nextLine();
            if (input.length() == validInputLength 
            &&  isValidString(input, validInputLength, validCharsPerPosition)) {
                return input;
            }
            System.out.print("The input you provided is not valid. Please try again: ");
        }
    }

    /** Checks whether each character of input (of length validInputLength) are in validCharsPerPosition
     *  Only used for receiveValidInput(int, String[])
     */
    private static boolean isValidString(String input, int validInputLength, String[] validCharsPerPosition) {
        for (byte i = 0; i < validInputLength; i++) {
            if (!isValidChar(input.charAt(i), validCharsPerPosition[i])) return false;
        }
        return true;
    }

    /** Checks whether c is a char in validChars
     *  Only used for isValidString(String, int, String[])
     */
    private static boolean isValidChar(char c, String validChars) {
        int len = validChars.length();
        for (byte i = 0; i < len; i++) {
            if (c == validChars.charAt(i)) return true;
        }
        return false;
    }
}
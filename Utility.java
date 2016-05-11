import java.util.Scanner;
/**
 * This class contains the staticmethods needed to transform user input into a format friendly to double arrays 
 * and methods to check whether user input for x and y coordinates are of good format.
 * 
 * @author Christian Edward Rodriguez 
 * @version V2
 */
public class Utility {
    /**
     * Takes char and int input and turns them into a coordiante value in computer (index-accessing) format
     * 
     * @param yAxis y-component of coordinate in the form of a char
     * @param xAxis x-component of coordiante in the form of int (1-10)
     * @return coordinate coordiante in {y, x} format where each element is an Integer (0-9)
     */
    public static Integer[]coordinateMaker(char yAxis, int xAxis) { 
        Integer[]coordinate = new Integer[2];
        coordinate[0] = (int)yAxis - (int)'A';
        coordinate[1] = xAxis - 1;
        return coordinate;
    }
    /**
     * Makes sure that String x is in the form of an appropriate user-viewed x component.
     * If it isn't, the user is asked to type another until a propper value is given
     * 
     * @param x String input of the user for an x component of a point
     * @return value x if it is a propper value
     */
    public static int xChecker(String x){
        int value;
        Scanner in = new Scanner(System.in);
        try { //Check to see if propper
            value = Integer.parseInt(x);
            if (value > Game.LETTER_LIMIT - 'A' + 1 || value < 1){//use recursion if format propper, but out of range
                System.out.print("ERROR\nType a number between 1 and 10 inclusive:  ");
                value = xChecker(in.nextLine());
            }
        }
        catch (NumberFormatException error) {// Used if non-Integer typed
          System.out.print("Error\n".toUpperCase()+"Type an integer value that is less than "+(Game.LETTER_LIMIT-'A'+1) + ":  ");
          value = xChecker(in.nextLine());
        }
        return value;
    }
    /**
     * Makes sure that String y is in the form of an appropriate user-viewed y component.
     * If it isn't, the user is asked to type another until a propper value is given
     * 
     * @param y String input of the user for an y component of a point
     * @return y.charAt(0) if y is appropriate
     */
    public static char yChecker(String y){
        y = y.toUpperCase();//For user convenience
        if (y.length() != 1 || !(y.charAt(0) >= 'A' && y.charAt(0) <= Game.LETTER_LIMIT)){ //Recursion used till all errors corrected
          System.out.print("Error\n".toUpperCase()+"Type a single letter from A to " + Game.LETTER_LIMIT + ": ");
          Scanner in = new Scanner(System.in);
          y = in.nextLine();
          return yChecker(y);
        }
        return y.charAt(0);
    }
}
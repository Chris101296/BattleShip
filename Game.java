import java.util.*;
import java.io.*;
/**
 * This is a Terminal version of the clasic board game, Battle Ship.  It only supports a user vs. cpu mode, since haveing multiple players seems somewhat problematic 
 * without multiple computers, and a website by necessity.  The ships' coordiantes are represented by a letter corresponding to the type of ship occupying the space: A for 
 * Aircraft Carrier, B for Battle Ship, S for Submarine, C for Cruiser, and D for Destoyer.  the y-component of the board is labeled A - J and the x component 1 -10.
 * 
 * The player begins by choosing each ship on a ship-by-ship basis. Each Ship is represented by a subclass of the Ship abstract class. He/she first decides whether the ship's 
 * top-most or left-most coordinate will be typed in, then proceeds to assign that coordinate.  The player must redo this step for each ship when a coordiante on a ship is 
 * either out of the boards or is shared with a previously defined ship.
 * 
 * For each turn the player is shown the cpu's board (with ship locations hidden) and his/her own.  The player chooses what area to fire at and the cpu proceeds by doing the same.  
 * The results of each attack is displayed on the screen both in text and on the next time both boards are displayed.  A * indicates that the coordiante was hit, but nothing was 
 * there while an X indicates that a ship was hit.  The text tells the kind of ship that was hit and the location.
 * 
 * This process repeats itself untill all the ships of either the user or the cpu are destroyed.  The corresponding text file is accessed and displayed onto the screen.  
 * The user is finally asked whether he or she would like to play the game again.
 * 
 * The title, victory, and deffeat texts, in addition to their corresponding ASCII arts, are stored in three seprate plain text files.  I am currently thinking about including a
 * "scoreboard" that is displayed through a Scanner object and editted with a PrintWriter object.
 *
 * @author Christian Edward Rodriguez 
 * @version V2
 */
public class Game
{
    public static final int SHIPS = 5;  //Number of ships each player can have
    public static final char LETTER_LIMIT = 'J'; /*The last letter on the vertical axis of the board (also idirectly)
                                                   also indirectly controls the number of spaces on the horizontal axis
                                                 */
    private static boolean anotherRound = false; //used to decide whether to play another round
    /**
     * Used to tell whether there are any two Ship objects in a List ob such objects that share a common 
     * coordinate in their coordinates private instance variable
     * 
     * @param fleet List of Ship objects in which we are looking for a match in Integer array values
     * @param indexCurrent the index of the latest Ship object added to fleet
     * @return true if there is a match, false if there is not
     */
    public static boolean areThereRepeatedCoordinates(List<Ship> fleet, int indexCurrent)
    {
        for(int i = 0; i<indexCurrent; i++) //Check upto the object being checked for repeats
        {
            List<Integer[]> pointsOnPriorShip = fleet.get(i).getCoordinates();
            for(Integer[] newCoordinates: fleet.get(indexCurrent).getCoordinates()) //Cycle through recent ship coordinates
            {
                for(Integer[]oldCoordiantes: pointsOnPriorShip ) //Cycle through older ship coordinates
                    if(oldCoordiantes[0].equals(newCoordinates[0]) && oldCoordiantes[1].equals(newCoordinates[1]))
                       return true;
            }
        }
        return false;
    }
    /**
     * Overloaded method
     * Generates a List of Integer arrays that represent the coordinates of a particular ship
     * 
     * @param ship the Ship object whose coordinates we are assigning
     * @param input a Scanner that takes in user input, used to assign values to cirtain variables
     * @return shipCoordiantes List of Integer arrays that represent the coordinates of a particular ship [(y,x) format]
     */
    private static List<Integer[]> createShipCoordinates(Ship ship, Scanner input)
    {
       System.out.println("\n" + ship.getClass().getName().toUpperCase() + ":");
       System.out.print("Are you going to type the top (T) most or leftmost (L) point on your ship:  ");
       String orrientation = input.nextLine().toUpperCase();
       while(!(orrientation.equals("T") || orrientation.equals("L"))) //Catches errors in input
       {
          System.out.print("Type either L for left or T for top:  ");
          orrientation = input.nextLine().toUpperCase();
       }
       Integer[]coordinates = createShipCoordinates(input);
       if(orrientation.equals("T") && coordinates[0] + ship.getLength() - 1 > Game.LETTER_LIMIT-'A' || //Check to see given settings don't lead to out of bounds
          orrientation.equals("L") && coordinates[1] + ship.getLength() - 1 > Game.LETTER_LIMIT-'A')
          {
              System.out.print("ERROR\nwith the given settings, your boat is out of the map, try again:  ");
              return createShipCoordinates(ship, input);
          }
       
       //Generate coordiantes after start given orrientation
       List<Integer[]> shipCoordinates = new ArrayList<Integer[]>();
       shipCoordinates.add(coordinates);
       for(int i = 1; i < ship.getLength(); i++)
       {
           if(orrientation.equals("L"))
             shipCoordinates.add(new Integer[]{coordinates[0], coordinates[1]+i});
           else
             shipCoordinates.add(new Integer[]{coordinates[0]+i, coordinates[1]});
       }
      
       return shipCoordinates;
    }
    /**
     * Overloaded method
     * Generates a Integer array that represents a set of (y,x) coordinates
     * A diffrent method of the same name uses this method to generate either the top or left most coordinate
     * This method is also used in the main method to specify the user's desired coordinate to attack
     * 
     * @param userInput a Scanner that takes in user input, used to assign values to cirtain variables
     * @return shipCoordiantes List of Integer arrays that represent the coordinates of a particular ship [(y,x) format]
     */
    private static Integer[] createShipCoordinates(Scanner userInput)
    {
         //Generate y component
         System.out.print("Choose the letter component for the ship's coordinate (a letter from A to " + Game.LETTER_LIMIT +"): ");
         char y = Utility.yChecker(userInput.nextLine().toUpperCase());;
            
         //Generate x component
         System.out.print("Choose the number component for the ship's coordinate (a number from 1 to " + (Game.LETTER_LIMIT - 'A' + 1)+"): ");
         int x = Utility.xChecker(userInput.nextLine().toUpperCase()); //stores number value of ship coordiante
            
         //Translate coordinates to array useage
         return Utility.coordinateMaker(y,x); //check  turns x and y input into propper coordinates for a double array
    }
    /**
     * Displays title onto the screen
     */
    private static void displayTitle() throws IOException
    {
        Scanner title = new Scanner(new File("title.txt")); //title of game
        while (title.hasNextLine())
        {
            System.out.println(title.nextLine());
        } 
        title.close();
        System.out.println("\n\n"); 
    }
    /**
     * Method that allows for the creation of a List of Ship objects that will then be pointed to by
     * instance variables in Board objects 
     * 
     * @return fleet a List of Ship objects to be used in Board objects.
     */
    private static List<Ship> generateFleet()
    {
        System.out.println("CHOOSE YOUR SHIPS' COORDINATES:");
        Scanner userInput = new Scanner(System.in);
        List<Ship> fleet = new ArrayList<Ship>();
           fleet.add(new Aircraft_Carrier());
           fleet.add(new Battle_Ship());
           fleet.add(new Cruiser());
           fleet.add(new Submarine());
           fleet.add(new Destroyer());
        for(int ship = 0; ship < 5; ship++) {
            System.out.println();
            //Make Coordinates for ships
            fleet.get(ship).setUpCoordinates(createShipCoordinates(fleet.get(ship), userInput));
        
            //checks to see if there are repeats if there are multiple ships and acts appropriatel
            boolean repeats = areThereRepeatedCoordinates(fleet, ship);
            while(repeats) {
                System.out.printf("%S%n%S%n","error", "Your ship intersected with a previous ship, please choose another set of coordinates");
                fleet.get(ship).setUpCoordinates(createShipCoordinates(fleet.get(ship), userInput));
                repeats = areThereRepeatedCoordinates(fleet, ship);
            }
        }
        return fleet;
    }
    /**
     * Changes the play variable depending on the user's input
     * 
     * @return true if the user wishes to play another round, false otherwise
     */
    private static boolean toContinueOrNotToContinue()
    {
        Scanner in = new Scanner(System.in);
        String goOn = in.nextLine();
        
        //Correct input errors
        while(!(goOn.equalsIgnoreCase("n") || goOn.equalsIgnoreCase("y")))
        {
            System.out.print("ERROR\nType either N or Y: ");
        }
        
        //Change goOn appropriately
        if(goOn.equalsIgnoreCase("Y"))
        {
            return true;
        }
        return false;
    }
    /**
     * The main method of the Game object
     */
    public static void main (String[]args) throws IOException
    {
       boolean play = true;
        while(play)  { 
          displayTitle();
          System.out.print("\n");
          
          //Generate User Board and Opponent information
          Board userBoard = new Board(generateFleet());
          List<Integer[]> targets = new ArrayList<Integer[]>();
          Opponent cpu = new Opponent();
          
          Scanner chooseTarget = new Scanner(System.in); //For attacking
          while(userBoard.getShipsAlive() > 0 && cpu.getBoard().getShipsAlive() > 0)
          {
              System.out.printf("%n%34s***********%n%34sENEMY BOARD%n%34s***********%n"  , " ", " ", " ");
              //cpu.getBoard().displayEnemyBoard();
              cpu.getBoard().displayEnemyBoard(); //turn to toString() use and get rid of once done testing.
              System.out.println("\n");
              
              System.out.printf("%34s**********\n%34sUSER BOARD\n%34s**********\n"  ," "," ", " ");
              userBoard.displayBoard();
              
              
              System.out.println("\n\nYour turn:\n".toUpperCase() +"Enter coordiantes of target:  ");
              
              Integer[] coordinates = createShipCoordinates(chooseTarget); //Generate data for coordaintes of Opponent's Board user wants to hit
              while(cpu.getBoard().getSpace()[coordinates[0]][coordinates[1]].equals("[*]") ||  //Make sure no previsouly hit point is hit again
                    cpu.getBoard().getSpace()[coordinates[0]][coordinates[1]].equals("[X]"))
              {
                  System.out.println("ERROR!\nENTER A COORDINATE THAT HAS NOT BEEN HIT YET: ");
                  coordinates = createShipCoordinates(chooseTarget);
              }
              System.out.println("\n\n\n\n\n\n\n\n\n\n\n\n\n\n\n\n"); //Make space to have results and Boards on the next display
              
              System.out.print("Your Result:\n\t".toUpperCase()); //Hit opponent useing user's input from line 211 
              cpu.getBoard().hit(coordinates[0], coordinates[1]);
              
              if(cpu.getBoard().getShipsAlive() == 0)
                break;
              //Opponent's turn
              System.out.print("Opponent's Result:\n\t".toUpperCase()); 
              cpu.shoot(userBoard);
          }
          
          //See who won based off of who does not have 0 ships alive and display appropriate message
          if(cpu.getBoard().getShipsAlive() == 0)
          {
              System.out.println("CONGRADULATIONS!!!\nYOU WON!");
              Scanner victoria = new Scanner(new File("victoryBoat.txt"));
              while(victoria.hasNextLine())
              {
                  System.out.println(victoria.nextLine());
              }
              victoria.close();
          }
          else {
              System.out.println("ALL YOUR SHIPS HAVE BEEN SUNKEN!!!");
              Scanner perdicion= new Scanner(new File("explosion.txt"));
              while(perdicion.hasNextLine())
              {
                  System.out.println(perdicion.nextLine());
              }
              perdicion.close();
            }
          
          //Check to see if user wants to play again.
          System.out.print("Do you want to play another round?\nType Y for yes or N for no:  ");
          play = toContinueOrNotToContinue();
       }
       System.out.println("********\nFINISHED\n********");
    }
}
import java.util.*;
/**
 * Serves as both the enemy and the player's board, showing the places that have been hit and the
 * ships remaining.  This class also provies methods for accessing key peices of information, including 
 * the number of ships remaining, and for marking a specific coordiante on the Board as hit.
 * 
 * @author Christian Edward Rodriguez
 * @version V2
 */
public class Board
{
    private String[][] space = new String[Game.LETTER_LIMIT-'A'+1][Game.LETTER_LIMIT-'A'+1]; //Display for User
    private int numberOfShips = Game.SHIPS;      //To see whether player or computer still active
    private List<Ship> fleet;
    /**
     * Constructor for objects of class Board
     * Assigns values to spaces in this.space, first making all spaces equal to
     * "[]" and then replaceing each of these empty spaces with an appropriate emblem if a ship ought
     * to be there depending on a Ship object's hidden type.
     * 
     * @param int[][]coordinates coordinates of ships
     */
    public Board( List<Ship>fleet)
    {
        this.fleet = fleet;
        //Make all elements == "[ ]"
        for(int row = 0; row < this.space.length; row++)
          for(int column = 0; column < this.space[row].length; column++)
            this.space[row][column] = "[ ]";
        //Replace areas with ships in them with propper String value
        
        for(Ship ship: this.fleet)
        {
            for(Integer[]point: ship.getCoordinates())
            {
                this.space[point[0]][point[1]] = "["+ship.getEmblem()+"]";
            }
        }
    }   
    /**
     * Shows the board of the player, allowing him to see where his ships are and 
     * where the enemny has shot, including where ships have been sunken.
     */
    public void displayBoard() {
       System.out.printf("%20s",""); // to add spacing
       for(int i = 0; i < space[0].length; i++) //Put labels for number axis
       {
           System.out.printf("%4d",i+1);
       }
       System.out.println();
       for(int row = 0; row < this.space.length; row++)  { //Put letter labels and appropriate coordinate values
           System.out.print("                    "+ (char)(row+'A') + "|");
           for (String emblem: this.space[row]) //coordinate values
             System.out.print(emblem + " ");
           System.out.println();
       }
    }
    /**
     * Shows the enemy's board to the user, hideing where the enemy ships are
     */
    public void displayEnemyBoard() {
       System.out.printf("%20s","");
        for(int i = 0; i < space[0].length; i++)
       {
           System.out.printf("%4d",i+1);
       }
       System.out.println(); 
       for(int row = 0; row < this.space.length; row++) {
         System.out.print("                    " + (char)(row+'A') + "|");
         for (String emblem: this.space[row]) {
           if (!(emblem.equals("[ ]") || emblem.equals("[X]") || emblem.equals("[*]")))  //Hide the enemy ships
            System.out.print("[ ]"+ " ");
           else
              System.out.print(emblem + " ");
         }   
         System.out.println();
       }
    }    
    /**
     * Gets Coordinates from board 
     * 
     * @return shipCoordinates the coordinates of all the ships for this board
     */
    public List<Ship> getShips()
    {
        return this.fleet;
    }
    /**
     * Returns the number of ships not destroyed
     * 
     * @return numberOfShips the number of ships remaining on the board
     */
    public int getShipsAlive() {
        return this.numberOfShips;
    }
    /**
     * Returns the board spaces
     * 
     * @return space the board spaces
     */
    public String[][] getSpace() {
        return this.space;
    }
    /**
     * Takes a coordinate and sees whether a ship is there.
     * If there is, replace emblem with an X, display appropriate message, and subtract 1 from the number
     * of remaining ships.  Else, only put a * on that area.
     * 
     * @param x x-coordinate of shot area
     * @param y y-component of coordinate of shot area
     * 
     * @return true if ship was hit, false otherwise
     */
    public boolean hit(int y, int x)
    {
        if (this.space[y][x].charAt(1) == 'X' || this.space[y][x].charAt(1) == '*') { //Test to see if space already hit wasn't passed to hit method
           throw new IllegalArgumentException("A space that was already hit can't be hit again".toUpperCase());
        }
        
        if (!(this.space[y][x].equals("[ ]"))) { //Remove point y, x on ship and board, illegal values not looked at (X or *)
           Ship shipHit = null;
           for (int i = 0; i < this.fleet.size(); i++)
           {
               if(this.space[y][x].charAt(1) == this.fleet.get(i).getEmblem()) {
                 shipHit = this.fleet.get(i);
                 break;
                }
           }
            
           if(shipHit.destroyPointOnShip(new int[]{y,x})) //check to see if ship was destroyed and displays appropriate message to user, subtracts from
                                                          //number of ships remaining
           {
               System.out.println((shipHit.getClass().getName().replace('_',' ')+ " was destroyed!!!").toUpperCase());
               this.numberOfShips--;
           }
           else {
            System.out.println((shipHit.getClass().getName().replace('_',' ')+ " was hit at "+(char)(y+'A') + "-" + (x+1)).toUpperCase());
            }
           this.space[y][x] = "[X]";
           return true;
        }
        else { //Mark useless shot on Board
           System.out.println("Nothing was hit at ".toUpperCase()+ (char)(y+'A') + "-" + (x+1) + ".");
           this.space[y][x] = "[*]";
           return false;
        }
    }  
    /**
     * Checks to see whether the target of a particular shot is still alive
     * 
     * @param hitEmblem the emblem at the point in question, hopefuly a ship emblem
     * @return true if the ship hit has been sunken, false otherwise
     */
    public boolean isThisShipSunken(String hitEmblem)
    {
        for(Ship ship: this.fleet)
        {
            if(ship.getEmblem() == hitEmblem.charAt(1)) {
                return ship.isDestroyed();
            }
        }
        
        throw new IllegalStateException("No value is given for isDestoryed"); //Thrown if point that was hit isn't a ship
    }
}
import java.util.*;
/**
 * This serves as the abstract class for five Ship subclasses that represent the diffrent ships in
 * the Battle Ship game, providing all propper methods to access information about each Ship.
 * 
 * @author Christian Edward Rodirugez
 * @version V1
 */
public abstract class Ship
{
    private final int length; //Number of coordiantes oringinally in this ship
    private List<Integer[]>coordinates; //The coordiantes occupied by this ship
    private final char emblem; //Letter associated with this particular ship
    /**
     * Constructor of Boat object
     */
    public Ship(char emblem, int length)
    {
        this.emblem = emblem;
        this.length = length;
    }
    /**
     * Removes correspoinding point from coordinates and returns true if the ship has been sunken and false otherwise
     * 
     * @param point int[] representation of the coordiante that has been hit by the opposing player
     * @return the result of isDestroyed()
     */
    public boolean destroyPointOnShip(int[]point)
    {
       for(int element = 0; element < this.coordinates.size(); element++)
         if(this.coordinates.get(element)[0] == point[0] && 
            this.coordinates.get(element)[1] == point[1])
            {
              this.coordinates.remove(element);
              break;
            }
       return this.isDestroyed();
    }
    /**
     * Getter method for coordiantes
     * 
     * return private instance variable coordinates
     */
    public List<Integer[]> getCoordinates()
    {
        return this.coordinates;
    }
    /**
     * Getter method for the emblem
     * @return private instance variable emblem
     */
    public char getEmblem()
    {
        return this.emblem;
    }
    /**
     * Getter method for the length
     * 
     * @return private instance variable length
     */
    public int getLength()
    {
        return this.length;
    }
    /**
     * Returns true if the ship has been destroyed and false otherwise
     * 
     * @return true if the Ship object has no more Integer arrays in coordiantes and false otherwise
     */
    public boolean isDestroyed()
    {
        return this.coordinates.size() == 0;
    }
    /**
     * Setter method for coordiantes
     * 
     * @param points a List of Integer arrays representing the coordiantes occupied by this Ship object
     */
    public void setUpCoordinates(List<Integer[]>points)
    {
        this.coordinates = points;
    }
}
import java.util.*;
/**
 * Represents the opponent of the single-player mode of this Battle Ship game.  Pseudo-random numbers are used to generate the positions of the boats as well as
 * what coordinates are attacked per turn.  Numerous instance variables are used in order to give the Opponent object information pertaining to what coordinates
 * have been hit, which of those spaces held ships, what those ships were, and the order in which they were hit.
 * 
 * @author Christian Edward Rodriguez
 * @version V2
 */
public class Opponent
{
    private List<Integer[]>accidentalShipsHitCoordinate = new ArrayList<Integer[]>(4); //Ships hit while in stage 1 or 2 that aren't the one hit at stage 0 (coordiantes)
                                                                             //add to it when new ship found and remove from it when ship is moved to firstPointHit 
                                                                             
    private List<String>accidentalShipsHitEmblem = new ArrayList<String>(4);  //Ships hit while in stage 1 or 2 that aren't the one hit at stage 0 (emblems)
                                                                             //add to it when new ship found and remove from it when ship is moved to firstEmblem
    
    private Board board; //Board onto which the Opponent places his ships
    
    private String firstShipEmblem = null; //Emblem of the boat that was hit to change StageOfAttack to 1, used for evaluation of 3
    private Integer[] firstPointHit = null; //Position of the first coordiante of the boat that was hit to change StageOfAttack to 1, used for 3 and 4
    private Integer[] laterPointHit = null; //Position of the other coordiante of the boat that was hit to change StageOfAttack to 1, used for 3 and 4
    private Integer[] otherHalfOfShip = new Integer[]{-1,-1}; // represents the start point of the second half of an unfinished ship (stage2Attack())
    
    private List<Integer[]> possibleStarts = new ArrayList<Integer[]>(); //y, x coordiantes to choose from when at stage 0
    
    private int stageOfAttack = 0; //Indicate what poart of the attack the Opponent is on
                                   /*0 = looking for a hit, use possibleStarts, 
                                     1 = got hit now looking for second poriton, 
                                     2 = found second poriton now hit till first ship is dead, then go back to 0 */
                                     
    /**
     * Creates an Opponent object
     * @param targets the coordinates of the user's ships
     */
    public Opponent() {
        //generate coordiantes for possibleStarts y, x
        for(int y = 0; y <= Game.LETTER_LIMIT - 'A'; y++) 
        {
            int x = 0;
            if(y%2 == 1){ //To keep entire columns from being included in start
                x = 1;
            }
            for(; x <= Game.LETTER_LIMIT - 'A'; x+=2)
            {
                this.possibleStarts.add(new Integer[]{y, x});
            }    
        }
        
        this.board = new Board(this.generateShips());
    }
    /**
     * Helpper method for the constructor of generateShip(), Generates the coordinates for a Ship object in the Opponent Board
     * 
     * @param nave Ship object for which we are generating coordiantes for
     * @return shipCoordinates List<Integer> object that houses the coordiantes of nave
     */
    private List<Integer[]> generateShipCoordinates(Ship nave)
    {
        Random rand = new Random();
        int orrientation = rand.nextInt(2);
        Integer[]firstCoordinates = new Integer[2]; //firstCoordaintes of nave
        //Make its first point so that the boat does not go out of the bounds.
        if(orrientation == 0){ //Left orrinetation
          firstCoordinates[0] = rand.nextInt(Game.LETTER_LIMIT - 'A' + 1);  //y coordiante
          firstCoordinates[1] =  rand.nextInt(Game.LETTER_LIMIT - 'A' + 2 - nave.getLength()); //x coordiante
        }
        else { //Top orrientation
          firstCoordinates[1] = rand.nextInt(Game.LETTER_LIMIT - 'A' + 1);  //x coordiante
          firstCoordinates[0] =  rand.nextInt(Game.LETTER_LIMIT - 'A' + 2 - nave.getLength()); //y coordiante
        }
        
        //Check to see that boat doesn't go out of bounds
        if(orrientation == 0 && firstCoordinates[1] + nave.getLength() - 1 > Game.LETTER_LIMIT-'A' || 
          orrientation == 1 && firstCoordinates[0] + nave.getLength() - 1 > Game.LETTER_LIMIT-'A')
            throw new IllegalStateException("Boat of size " + nave.getLength() + " and starts at "+
                                             firstCoordinates[0] +", " +firstCoordinates[1] + " (y, x) out of bounds");
        
        //Make list of coordinates to give unto the Ship object
        List<Integer[]> shipCoordinates = new ArrayList<Integer[]>();
        shipCoordinates.add(firstCoordinates);
        if(orrientation == 0) {
          for(int i = 1; i < nave.getLength(); i++)
          {       
              shipCoordinates.add(new Integer[]{firstCoordinates[0], firstCoordinates[1]+i});
          }
        }
        else {
          for(int i = 1; i < nave.getLength(); i++)
          {   
             shipCoordinates.add(new Integer[]{firstCoordinates[0]+i, firstCoordinates[1]});
          }
        }
        
        return shipCoordinates;
    }
    /**
     * Helpper method for the constructor of the Opponent object.  Generates the coordinates on which the oppoenent places its ships 
     * 
     * @param fleet a List of Ship objects that shall be passed onto the Board instance variable of the Opponent object
     */
    private List<Ship> generateShips()
    {
        List<Ship> fleet = new ArrayList<Ship>();
           fleet.add(new Aircraft_Carrier());
           fleet.add(new Battle_Ship());
           fleet.add(new Cruiser());
           fleet.add(new Submarine());
           fleet.add(new Destroyer());
           
        for(int ship = 0; ship < 5; ship++) {
            //Tell if ship is horizontal or vertical
            fleet.get(ship).setUpCoordinates(generateShipCoordinates(fleet.get(ship)));
            
            //checks to see if there are repeats if there are multiple ships and regenerates latest ship coordinates
            boolean repeats = Game.areThereRepeatedCoordinates(fleet, ship);
            while(repeats)
            {
                fleet.get(ship).setUpCoordinates(generateShipCoordinates(fleet.get(ship)));
                repeats = Game.areThereRepeatedCoordinates(fleet, ship);
            }
        }
        
        return fleet;
    }
    /**
     * Returns the Board private instance variable that belongs to Opponent object
     * 
     * @return board Board object that belongs to Opponent object
     */
    public Board getBoard()
    {
        return this.board;
    }
    /**
     * Gives the Opponent object 3 modes of attack.  The first chooses a pseudo-ranodm coordinate from the possibleStarts List.  The second mode shoots around
     * a coordinate that was hit and previously had a ship until another coordinate of the same ship is hit.  In this stage, coordinates of ships not hit at
     * stage 1 are stored into a List along with the emblem of the ship that was hit at that point  This information is then used after the last stage when
     * going back to stage 2.  At stage 3, ships of the boat from stage 2 are hit until it is sunk.  Again, hits on boats of a diffrent kind have their 
     * information stored in 2 Lists.  Once stage 2 is complete, the Opponent either reverts to stage 0 if no other ship was accidentally hit or stage 1 
     * otherwise.
     * 
     * @param user Board object representing the ships of the user
     */
    public void shoot(Board user)
    {
        if(this.stageOfAttack == 0) //Choose randomly from possible coordinates to hit and remove point from possible points after hit is made.  
                                    //If a hit is scored, move on to stage 1 and setup propper instance variables for ship that was hit
        {
            Integer[]attackPoint = this.possibleStarts.get((int)(Math.random()*this.possibleStarts.size()));
            this.possibleStarts.remove(attackPoint);
            
            //Changes attackPoint if space was previously hit 
            while(user.getSpace()[attackPoint[0]] [attackPoint[1]].equals("[*]") || 
                  user.getSpace()[attackPoint[0]] [attackPoint[1]].equals("[X]") )
            {
                attackPoint = this.possibleStarts.get((int)(Math.random()*this.possibleStarts.size()));
                this.possibleStarts.remove(attackPoint);
            }
            this.firstShipEmblem = user.getSpace()[attackPoint[0]] [attackPoint[1]];
            
            if( user.hit(attackPoint[0], attackPoint[1]) )
            {
                this.stageOfAttack++; 
                this.firstPointHit = attackPoint;
            }
        }
        else if (this.stageOfAttack == 1) 
        { //Generate List of coordiantes around firstPointHit.  Choose randomly from those points that haven't been hit 
                                           //previously.  If hit is scored on the same ship as before and ship still alive, move onto stage 2, else add the 
                                           //coordainte and its ship emblem to appropriate instance variable Lists if new ship or go to stage 0 if ship is a 
                                           //Destoyer (so it would be sunk since it only has 2 coordiantes).
          
          List<Integer[]> possibleSecondaryPoints = new ArrayList<Integer[]>(4);
              possibleSecondaryPoints.add(new Integer[]{this.firstPointHit[0]-1, this.firstPointHit[1]});
              possibleSecondaryPoints.add(new Integer[]{this.firstPointHit[0]+1, this.firstPointHit[1]});
              possibleSecondaryPoints.add(new Integer[]{this.firstPointHit[0], this.firstPointHit[1]-1});
              possibleSecondaryPoints.add(new Integer[]{this.firstPointHit[0], this.firstPointHit[1]+1});
         
          for(int i = 0; i < possibleSecondaryPoints.size(); i++) //Remove coordinates if outside of board or already have been hit
          {
              Integer[] pointInQuestion = possibleSecondaryPoints.get(i);
              if(pointInQuestion[0] < 0 ||  pointInQuestion[0] > Game.LETTER_LIMIT - 'A' ||  pointInQuestion[1] < 0 ||  pointInQuestion[1] > Game.LETTER_LIMIT - 'A' ||
                 user.getSpace()[pointInQuestion[0]] [pointInQuestion[1]].equals("[X]") || user.getSpace()[pointInQuestion[0]] [pointInQuestion[1]].equals("[*]"))
              {
                   possibleSecondaryPoints.remove(i);
                   i--;  //To make sure that no coordiante is skipped
              }
          }
          
          //Choose random point among possible choices to get hit and store its emblem (done since after hit, there is an X or *)
          Integer[]attackPoint = possibleSecondaryPoints.get((int)(Math.random() * possibleSecondaryPoints.size()));
          String nextEmblem = user.getSpace()[attackPoint[0]] [attackPoint[1]];
          if( user.hit(attackPoint[0], attackPoint[1])) 
          {
              if (nextEmblem.equals(this.firstShipEmblem)) {//Deal with whether the ship hit twice is a Destroyer or not
                if(this.firstShipEmblem.equals("[D]")) { // Either go back to stage 0 if no accidental ships were hit,
                                                         // go to 1 otherwise and use prior ship hit coordinates
                    if(this.accidentalShipsHitCoordinate.size() != 0) {
                        this.firstShipEmblem = this.accidentalShipsHitEmblem.get(0);
                        this.firstPointHit = this.accidentalShipsHitCoordinate.get(0);
                
                        this.accidentalShipsHitCoordinate.remove(0);
                        this.accidentalShipsHitEmblem.remove(0);
                    }
                    else {
                        this.stageOfAttack = 0;
                        this.firstPointHit = null;
                        this.firstShipEmblem = null;
                    }
                 
                }
                else {
                   this.laterPointHit = attackPoint;
                   this.stageOfAttack++; //move to 2
                  }
               }
              else { // Add coordinate to accidentalShiptHitCoordinates, wouldn't be in possible starts, so no need to remove any points.
                 this.accidentalShipsHitCoordinate.add(attackPoint);
                 this.accidentalShipsHitEmblem.add(nextEmblem);
               }
          }
          else { } //Do Nothing if not Ship was hit, but allow the else if statement below to connect to if this.stageOfAttack == 1
         }
       
        else if (this.stageOfAttack == 2)
        {
            this.stage2Attack(user);
        }
    }
    /**
     * Private overload of the shoot method above, meant to test the shoot method above under specific circumstances buy targetting specific locations
     * 
     * @param user Board object representative of the cpu's enemy, the user
     * @param y the y-coordinate of the target that shall be hit
     * @param x the x-coordiante of the target that shall be hit
     */
    private void shoot(Board user, Integer y, Integer x)
    {
        if(this.stageOfAttack == 0) //Choose randomly from possible coordinates to hit and remove point from possible points after hit is made.  
                                    //If a hit is scored, move on to stage 1 and setup propper instance variables for ship that was hit
        {
            Integer[]attackPoint = new Integer[]{y,x};
            this.possibleStarts.remove(attackPoint);
            System.out.println(this.possibleStarts.size()); //Test
            this.firstShipEmblem = user.getSpace()[attackPoint[0]] [attackPoint[1]];
            
            if( user.hit(attackPoint[0], attackPoint[1]) )
            {
                this.stageOfAttack++; 
                this.firstPointHit = attackPoint;
            }
        }
        else if (this.stageOfAttack == 1) 
        { //Hit point y-x and act normally from there
          Integer[]attackPoint = new Integer[]{y,x};
          String nextEmblem = user.getSpace()[attackPoint[0]] [attackPoint[1]];
          if( user.hit(attackPoint[0], attackPoint[1])) 
          {
              if (nextEmblem.equals(this.firstShipEmblem)) {//Deal with whether the ship hit twice is a Destroyer or not
                if(this.firstShipEmblem.equals("[D]")) { // Either go back to stage 0 if no accidental ships were hit,
                                                         // go to 1 otherwise and use prior ship hit coordinates
                    if(this.accidentalShipsHitCoordinate.size() != 0) {
                        this.firstShipEmblem = this.accidentalShipsHitEmblem.get(0);
                        this.firstPointHit = this.accidentalShipsHitCoordinate.get(0);
                
                        this.accidentalShipsHitCoordinate.remove(0);
                        this.accidentalShipsHitEmblem.remove(0);
                    }
                    else {
                        this.stageOfAttack = 0;
                        this.firstPointHit = null;
                        this.firstShipEmblem = null;
                    }
                 
                }
                else {
                   this.laterPointHit = attackPoint;
                   this.stageOfAttack++; //move to 2
                  }
               }
              else { // Add coordinate to accidentalShiptHitCoordinates, wouldn't be in possible starts, so no need to remove any points.
                 this.accidentalShipsHitCoordinate.add(attackPoint);
                 this.accidentalShipsHitEmblem.add(nextEmblem);
               }
           }
          else { } //Do Nothing if not Ship was hit, but allow the else if statement below to connect to if this.stageOfAttack == 1
         }
       
        else if (this.stageOfAttack == 2)
        {
            this.stage2Attack(user);
        }
        //test
        System.out.println(this.stageOfAttack);//test
        for(int element = 0; element < this.accidentalShipsHitCoordinate.size(); element++)
          System.out.println("\t****"+ this.accidentalShipsHitCoordinate.get(element)[0] + ", " + this.accidentalShipsHitCoordinate.get(element)[1] + " " + 
                              this.accidentalShipsHitEmblem.get(element));
    }
    /**
     * Does the third stage of the attack.  See the shoot method for more details of what happens at third stage
     * 
     * @param user Board of the user
     */
    private void stage2Attack(Board user)
    {
        Integer[]hitHere = new Integer[2];  //set to values of laterPointHit to give start that can be used to test current targetting
        hitHere[0] = this.laterPointHit[0];
        hitHere[1] = this.laterPointHit[1];
        if(this.otherHalfOfShip[0] > -1)
        {
            hitHere[0] = this.otherHalfOfShip[0];
            hitHere[1] = this.otherHalfOfShip[1];
            this.otherHalfOfShip[0] = -1; //Resets this.otherShipHit to "default" value so that it is not reused.
        }
       
        //Reverses pattern of shot if out of bounds or if next point was previously hit for all possible directions of shooting
        //while loops employed just incase ship has been hit in 2 adjacent coordinates by 2 previous ship shots
        else {
            if(this.laterPointHit[0] < this.firstPointHit[0]) //later is higher
            {
                if(this.laterPointHit[0] == 0 ||
                    user.getSpace()[this.laterPointHit[0]-1][this.laterPointHit[1]].equals("[*]") || 
                    user.getSpace()[this.laterPointHit[0]-1][this.laterPointHit[1]].equals("[X]"))
                 {
                     while(user.getSpace()[hitHere[0]][hitHere[1]].equals("[X]") || user.getSpace()[hitHere[0]][hitHere[1]].equals("[*]"))
                     {
                       hitHere[0]++;
                     }
                 }
                 else
                 {
                     hitHere[0] = this.laterPointHit[0]-1;
                 }
             }
             else if(this.laterPointHit[0] > this.firstPointHit[0]) //later is lower
             {
                 if(this.laterPointHit[0] == Game.LETTER_LIMIT - 'A'||
                      user.getSpace()[this.laterPointHit[0]+1][this.laterPointHit[1]].equals("[*]") || 
                      user.getSpace()[this.laterPointHit[0]+1][this.laterPointHit[1]].equals("[X]"))
                    {
                     while(user.getSpace()[hitHere[0]][hitHere[1]].equals("[X]") || user.getSpace()[hitHere[0]][hitHere[1]].equals("[*]"))
                     {
                        hitHere[0]--;
                     }
                 }
                 else
                 {
                     hitHere[0] = this.laterPointHit[0]+1;
                 }
             }
             else if(this.laterPointHit[1] > this.firstPointHit[1]) //later is right
              {
                 if(this.laterPointHit[1] == Game.LETTER_LIMIT - 'A'||
                    user.getSpace()[this.laterPointHit[0]][this.laterPointHit[1]+1].equals("[*]") || 
                    user.getSpace()[this.laterPointHit[0]][this.laterPointHit[1]+1].equals("[X]"))
                 {
                    while(user.getSpace()[hitHere[0]][hitHere[1]].equals("[X]") || user.getSpace()[hitHere[0]][hitHere[1]].equals("[*]"))
                    {
                      hitHere[1]--;
                    }
                 }
                 else
                 {
                     hitHere[1] = this.laterPointHit[1]+1;
                 }
             }
             else if(this.laterPointHit[1] < this.firstPointHit[1]) //later is left
             {
                 if(this.laterPointHit[1] == 0 ||
                    user.getSpace()[this.laterPointHit[0]][this.laterPointHit[1]-1].equals("[*]") || 
                     user.getSpace()[this.laterPointHit[0]][this.laterPointHit[1]-1].equals("[X]"))
                 {
                   while(user.getSpace()[hitHere[0]][hitHere[1]].equals("[X]") || user.getSpace()[hitHere[0]][hitHere[1]].equals("[*]"))
                    {
                       hitHere[1]++;
                    }
                 }
                 else
                 {
                     hitHere[1] = this.laterPointHit[1]-1;
                 }
             }
             else //Error if there are no spaces around coordiante to shoot at, should never happen in theory
             {
                 throw new IllegalStateException("the second point is neither top, bottom, left, nor right");
             }
         }
         
        String hitEmblem = user.getSpace() [hitHere[0]] [hitHere[1]]; //emblem of the current targeted coordiante   
        user.hit(hitHere[0], hitHere[1]);//hit target after checking what the emblem originally was
        this.laterPointHit = hitHere;   //for future attack
        
        if(!hitEmblem.equals(this.firstShipEmblem)) //Add a new point to accidental ships hit Lists then change next target point accordingly useing 
                                                    //while loop as precausion
        {
           if(!hitEmblem.equals("[ ]")) {
               this.accidentalShipsHitCoordinate.add(this.laterPointHit);
               this.accidentalShipsHitEmblem.add(hitEmblem);
            }
           
           if(this.laterPointHit[0] < this.firstPointHit[0]){//top
              this.otherHalfOfShip[0] = this.firstPointHit[0] + 1;
              this.otherHalfOfShip[1] = this.firstPointHit[1];
              while(user.getSpace()[this.otherHalfOfShip[0]][this.otherHalfOfShip[1]].equals("[X]") || 
                    user.getSpace()[this.otherHalfOfShip[0]][this.otherHalfOfShip[1]].equals("[*]"))
                    {
                      this.otherHalfOfShip[0]++;
                    }
            }
           else if((this.laterPointHit[0] > this.firstPointHit[0])){//bottom
             this.otherHalfOfShip[0] = this.firstPointHit[0] - 1;
             this.otherHalfOfShip[1] = this.firstPointHit[1];
             while(user.getSpace()[this.otherHalfOfShip[0]][this.otherHalfOfShip[1]].equals("[X]") || 
                    user.getSpace()[this.otherHalfOfShip[0]][this.otherHalfOfShip[1]].equals("[*]"))
                    {
                      this.otherHalfOfShip[0]--;
                    }
            }
           else if(this.laterPointHit[1] > this.firstPointHit[1]){//right
             this.otherHalfOfShip[1] = this.firstPointHit[1] - 1;
             this.otherHalfOfShip[0] = this.firstPointHit[0];
             while(user.getSpace()[this.otherHalfOfShip[0]][this.otherHalfOfShip[1]].equals("[X]") || 
                    user.getSpace()[this.otherHalfOfShip[0]][this.otherHalfOfShip[1]].equals("[*]"))
                    {
                      this.otherHalfOfShip[1]--;
                    }
            }
           else {//left
             this.otherHalfOfShip[1] = this.firstPointHit[1] + 1;
             this.otherHalfOfShip[0] = this.firstPointHit[0];
             while(user.getSpace()[this.otherHalfOfShip[0]][this.otherHalfOfShip[1]].equals("[X]") || 
                    user.getSpace()[this.otherHalfOfShip[0]][this.otherHalfOfShip[1]].equals("[*]"))
                    {
                      this.otherHalfOfShip[1]++;
                    }
            }            
        }
        //Check to see if a ship has been destroyed, if one has been destroyed, go to appropriate stageOfAttack and 
        //make adjustments to instance variables.
        else if(user.isThisShipSunken(hitEmblem)) // set default variable values accordingly
        {
            if(this.accidentalShipsHitCoordinate.size() != 0) //Being attacking first ship on List, and remove from List
            {
                this.firstShipEmblem = this.accidentalShipsHitEmblem.get(0);
                this.firstPointHit = this.accidentalShipsHitCoordinate.get(0);
                
                this.accidentalShipsHitCoordinate.remove(0);
                this.accidentalShipsHitEmblem.remove(0);
                
                //Remove any points that are part of the current ship being targetted
                for(int i = 0; i < this.accidentalShipsHitEmblem.size(); i++)
                {
                    if(this.accidentalShipsHitEmblem.get(0).equals(this.firstShipEmblem)){
                      this.accidentalShipsHitEmblem.remove(i);
                      this.accidentalShipsHitCoordinate.remove(i);
                    }
                }
                
                this.stageOfAttack = 1;
            }
            else
            {
              this.firstShipEmblem = null;
              this.firstPointHit = null;
              this.stageOfAttack = 0;
            }
            
            this.laterPointHit = null;
        }
    }
    /**
     * Tester method to the Opponent class
     */
    public static void main(String args[])
    {
        Scanner wait = new Scanner(System.in);
        
        Opponent cpu = new Opponent();
        
        System.out.println("user Board".toUpperCase());
        List<Ship>b = new ArrayList<Ship>(3);   
        b.add(new Aircraft_Carrier());
        b.add(new Submarine());
        b.add(new Cruiser());
        
        ArrayList<Integer[]>set1 = new ArrayList<Integer[]>(), set2 = new ArrayList<Integer[]>(), set3 = new ArrayList<Integer[]>();
        set1.add(new Integer[]{1,1}); set1.add(new Integer[]{2,1}); set1.add(new Integer[]{3,1});  set1.add(new Integer[]{4,1});  set1.add(new Integer[]{5,1});
        set2.add(new Integer[]{2,2}); set2.add(new Integer[]{2,3}); set2.add(new Integer[]{2,4});
        set3.add(new Integer[]{3,2}); set3.add(new Integer[]{3,3}); set3.add(new Integer[]{3,4});
        
        b.get(0).setUpCoordinates(set1);b.get(1).setUpCoordinates(set2);  b.get(2).setUpCoordinates(set3);
        Board user = new Board(b);
        user.displayBoard();
        //Preset
        cpu.shoot(user,3,3);
        user.displayBoard(); System.out.println("*******");
        cpu.shoot(user, 2, 3);
        user.displayBoard();System.out.println("*******");
        cpu.shoot(user,3,2);
        user.displayBoard(); System.out.println("*******");
        
        int i = 0;
        while(i < 21){
        	
          cpu.shoot(user);
          user.displayBoard(); System.out.println(cpu.firstShipEmblem); System.out.println("*******");
          i++;
        }  
        System.out.println("DONE WITH TEST");
        wait.close();
    }
}
import java.util.*;

public class GSystem implements Positioning
{
	final static int NO_OWNER= -2;
	final static int OWNER_CONFLICTED = -1;
	
	//indicates which player is in control - from the current player's perspective
	int owner_id; //-2 is none, -1 is conflicted, otherwise corresponds to player id's
	int[] player_claims; //the elements of this array keep track of how many claims - planets or ships - each player owns in the system
	
	ArrayList<Satellite> orbiting_objects;
	HashSet<Star> stars;
	
	Object missile_lock = new Object();
	ArrayList<Missile> missiles;
	
	Fleet[] fleets; //indices = player id's
	String name;
	int id;
	
	int x;
	int y;
	int navigability;
	
	int width;
	int height;
	
	//only used in the designer for multi-system drags
	int x_adj;
	int y_adj;
	

	
	public GSystem(int i, int x, int y, String nm, ArrayList<Satellite> orbiting, HashSet<Star> stars, int nav)
	{
		id=i;
		name=nm;
		orbiting_objects=orbiting;
		this.stars=stars;
		this.x=x;
		this.y=y;
		navigability=nav;
		fleets = new Fleet[GalacticStrategyConstants.MAX_PLAYERS];
		missiles = new ArrayList<Missile>();
		owner_id = NO_OWNER;
	}
	
	public void setUpForGame(GameControl GC)
	{
		//set up ownership
		player_claims = new int[GalacticStrategyConstants.MAX_PLAYERS];
		for(int i=0; i<GalacticStrategyConstants.MAX_PLAYERS; i++) //player_claims and fleets both have GalacticStrategyConstants.MAX_PLAYERS as their length
		{
			player_claims[i]=0;
			fleets[i] = new Fleet(this, GC.players[i]);
		}
	}
	
	public void increaseClaim(Player p)
	{
		player_claims[p.getId()]++;
		if(owner_id >= 0 && owner_id != p.getId()) //if there is an owner of the system, and the increaser is not the owner, now conflicted
			owner_id = OWNER_CONFLICTED;
		else if(owner_id == NO_OWNER)
			owner_id = p.getId();
	}
	
	public void decreaseClaim(Player p)
	{
		player_claims[p.getId()]--;
		if(player_claims[p.getId()]==0 && owner_id == OWNER_CONFLICTED)
		{
			int claimers=0;
			int claimer_id=-2;
			for(int i=0; i<player_claims.length; i++)
			{
				if(player_claims[i] > 0)
				{
					claimers++;
					claimer_id=i;
				}
			}
			
			if(claimers == 1)
				owner_id = claimer_id;
			//else - still conflicted, so no change
		}
		else if(player_claims[p.getId()]==0) //decreasing claim in a non-conflicted system to 0 -> no owner
		{
			owner_id = NO_OWNER;
		}
	}
	
	public double massSum()
	{
		double sum=0;
		if(stars instanceof HashSet)
		{
			for(Star st : stars)
				sum += st.getMass();
		}
		return sum;
	}
	
	//methods required for save/load
	public GSystem()
	{
		owner_id = NO_OWNER;
		missiles = new ArrayList<Missile>();
	}
	
	public ArrayList<Satellite> getorbiting_objects(){return orbiting_objects;}
	public void setOrbiting_objects(ArrayList<Satellite> s){orbiting_objects=s;}
	public String getName(){return name;}
	public void setName(String nm){name=nm;}
	public HashSet<Star> getStars(){return stars;}
	public void setStars(HashSet<Star> st){stars=st;}
	public int getX(){return x;}
	public void setX(int x){this.x=x;}
	public int getY(){return y;}
	public void setY(int y){this.y=y;}
	public int getNavigability(){return navigability;}
	public void setNavigability(int nav){navigability=nav;}
	public Fleet[] getFleets(){return fleets;}
	public void setFleets(Fleet[] f){fleets=f;}
	public int getOwner_id(){return owner_id;}
	public void setOwner_id(int id){owner_id=id;}
	
	public int getWidth(){return width;}
	public void setWidth(int w){width=w;}
	public int getHeight(){return height;}
	public void setHeight(int h){height=h;}
	
	public double absoluteCurX(){return ((double)getWidth())/2;}
	public double absoluteCurY(){return ((double)getHeight())/2;}
	public double absoluteInitX(){return ((double)getWidth())/2;}
	public double absoluteInitY(){return ((double)getHeight())/2;}
	public double getAbsVelX(){return 0.0;}
	public double getAbsVelY(){return 0.0;}
	
	public void setId(int i){id=i;}
	public int getId(){return id;}
}
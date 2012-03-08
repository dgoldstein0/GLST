import java.awt.Color;
import java.util.ArrayList;
import java.util.HashSet;

public strictfp class GSystem implements Orbitable<GSystem>
{
	final static int NO_OWNER= -2;
	final static int OWNER_CONFLICTED = -1;
	
	//indicates which player is in control - from the current player's perspective
	int owner_id; //-2 is none, -1 is conflicted, otherwise corresponds to player id's
	int[] player_claims; //the elements of this array keep track of how many claims - planets or ships - each player owns in the system
	
	int current_player_id; //used in OWNER_CONFLICTED state to keep track of the last player whose color was used
	long time_last_color_valid_until; //the time until which the last_color will be used
	Color last_color; //the last return value of currentColor()
	
	ArrayList<Satellite<?>> orbiting;
	HashSet<Star> stars;
	
	//missiles synchronizes on itself
	MissileList missiles;
	
	Fleet[] fleets; //indices = player id's
	String name;
	int id;
	
	int x;
	int y;
	int navigability;
	
	int width;
	int height;
	
	//only used in the designer for multi-system drags.
	//Probably don't belong here...
	int x_adj;
	int y_adj;
	
	public GSystem(int i, int x, int y, String nm, ArrayList<Satellite<?>> orbiting, HashSet<Star> stars, int nav)
	{
		id=i;
		name=nm;
		this.orbiting=orbiting;
		this.stars=stars;
		this.x=x;
		this.y=y;
		navigability=nav;
		fleets = new Fleet[GalacticStrategyConstants.MAX_PLAYERS];
		missiles = new MissileList();
		owner_id = NO_OWNER;
		time_last_color_valid_until=-1;
	}
	
	public Describer<GSystem> describer(){return new GSystemDescriber(this);}
	
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
		if(player_claims != null) //this condition only fails when the player was not created for a game, but rather for the XMLEncoder
		{
			player_claims[p.getId()]++;
			if(owner_id >= 0 && owner_id != p.getId()) //if there is an owner of the system, and the increaser is not the owner, now conflicted
			{
				owner_id = OWNER_CONFLICTED;
				current_player_id = p.getId();
			}
			else if(owner_id == NO_OWNER)
				owner_id = p.getId();
		}
	}
	
	public void decreaseClaim(Player p)
	{
		if(player_claims != null) //this condition only fails when the player was not created for a game, but rather for the XMLEncoder
		{
			player_claims[p.getId()]--;
			if(player_claims[p.getId()]==0 && owner_id == OWNER_CONFLICTED)
			{
				int claimers=0;
				int claimer_id=NO_OWNER;
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
				else if(claimers==0)
					System.err.println("conflicted -> No One in "+ getName() + " system - BUG!");
				//else - still conflicted, so no change
			}
			else if(player_claims[p.getId()]==0) //decreasing claim in a non-conflicted system to 0 -> no owner
			{
				owner_id = NO_OWNER;
			}
		}
	}
	
	public void recalculateClaims()
	{
		for (int i=0; i < player_claims.length; i++)
		{
			player_claims[i] = 0;
			if (fleets[i] != null)
				player_claims[i] += fleets[i].getShips().size();
		}
		
		recalculateSatelliteClaims(orbiting);
		
		int claimers = 0;
		int last_claimer = -2;
		for (int i=0; i < player_claims.length; i++)
		{
			if (player_claims[i] > 0)
			{
				last_claimer = i;
				claimers++;
			}
		}
		
		if (claimers <= 1)
			owner_id = last_claimer;
		else
			owner_id = OWNER_CONFLICTED;
	}
	
	/**
	 * recalculateSatelliteClaims
	 * 
	 * This is meant solely as a helper function for recalculateClaims().  It
	 * works by recursively calling itself on the sets of objects orbiting
	 * the objects in the given list.
	 * 
	 * @param sats an ArrayList of orbiting satellites.  This can be the set of
	 * 		the major satellites in the system, or a set of sub-satellites (Moons),
	 * 		or anything else.
	 */
	private void recalculateSatelliteClaims(ArrayList<Satellite<?>> sats)
	{
		for (Satellite<?> s : sats)
		{
			if (s instanceof OwnableSatellite<?>)
			{
				OwnableSatellite<?> o = (OwnableSatellite<?>)s;
				Player owner = o.getOwner();
				if (owner != null)
					player_claims[owner.getId()]++;
				
				recalculateSatelliteClaims(o.orbiting);
			}
		}
	}
	
	public double massSum()
	{
		double sum=0;
		{
			for(Star st : stars)
				sum += st.getMass();
		}
		return sum;
	}
	
	/**
	 * currentColor
	 * 
	 * This function picks the color that the system should appears as in the
	 * Galaxy view.  It handles conflicts by switching the colors round-robin,
	 * and will color unowned systems white.
	 * 
	 * Note that the last_color and time_last_color_valid_until exist for this
	 * function and essentially form a cache.  This "cache" is only used for
	 * display, so stale data in it is unimportant.
	 * 
	 * @param time the current time.  Used to change the color as a function
	 * 		of time.
	 * @return the color to use.
	 */
	public Color currentColor(long time)
	{
		if(time <= time_last_color_valid_until)
		{
			return last_color;
		}
		else
		{
			//TODO: should 500 be in our constants file?
			time_last_color_valid_until = time+500;
			
			switch(owner_id)
			{
				case NO_OWNER:
					return last_color = Color.white;
				case OWNER_CONFLICTED:
					
					do {
						current_player_id = (current_player_id+1) % player_claims.length;
					} while (player_claims[current_player_id] == 0);
					
					return last_color = GameInterface.GC.players[current_player_id].getColor();
	
				default:
					return last_color = GameInterface.GC.players[owner_id].getColor();
			}
		}
	}
	
	public double getXCoord(long t){return absoluteCurX();}
	public double getYCoord(long t){return absoluteCurY();}
	public double getXVel(long t){return 0.0;}
	public double getYVel(long t){return 0.0;}
	public String imageLoc(){return "";}
	
	//methods required for save/load
	public GSystem()
	{
		owner_id = NO_OWNER;
		missiles = new MissileList();
	}
	
	public ArrayList<Satellite<?>> getOrbiting(){return orbiting;}
	public void setOrbiting(ArrayList<Satellite<?>> s){orbiting=s;}
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
	public MissileList getMissiles(){return missiles;}
	public void setMissiles(MissileList m){missiles=m;}
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

	public void saveOwnablesData() {
		
		for(Satellite<?> sat : orbiting)
		{
			sat.recursiveSaveData();
		}
	}
	
	public void revertOwnables(long t) throws DataSaverControl.DataNotYetSavedException {
		
		for(Satellite<?> sat : orbiting)
		{
			sat.recursiveRevert(t);
		}
	}

	/**
	 * @see OwnableSatellite.compareTo
	 */
	@Override
	public int compareTo(Orbitable<?> o) {
		if (o instanceof GSystem)
		{
			if (id < ((GSystem)o).id)
				return -1;
			else if (id == ((GSystem)o).id)
				return 0;
			else
				return 1;
		}
		else
			return 1; //this makes GSystems the greatest items in the ordering of Orbitables.
	}
	
	@Override
	public boolean equals(Object o)
	{
		if (o instanceof GSystem)
			return (compareTo((GSystem)o) == 0);
		else
			return false;
	}
	
	@Override
	public int hashCode()
	{
		return id;
	}
}
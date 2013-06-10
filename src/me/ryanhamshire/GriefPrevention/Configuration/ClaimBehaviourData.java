package me.ryanhamshire.GriefPrevention.Configuration;

import me.ryanhamshire.GriefPrevention.Claim;
import me.ryanhamshire.GriefPrevention.GriefPrevention;
import me.ryanhamshire.GriefPrevention.Messages;
import me.ryanhamshire.GriefPrevention.PlayerData;
import me.ryanhamshire.GriefPrevention.TextMode;
import org.bukkit.Location;
import org.bukkit.configuration.file.FileConfiguration;
//this enum is used for some of the configuration options.
import org.bukkit.entity.Player;

//holds data pertaining to an option and where it works. 
//used primarily for information on explosions.
public class ClaimBehaviourData {
	public enum ClaimAllowanceConstants {
		Allow_Forced,
		Allow,
		Deny,
		Deny_Forced;
		public boolean Allowed(){ return this==Allow || this==Allow_Forced;}
		public boolean Denied(){ return this==Deny || this==Deny_Forced;}
		
		
		
	}
	public enum ClaimBehaviourMode{
		RequireNone,
		RequireOwner,
		RequireManager,
		RequireAccess,
		RequireContainer,
		RequireBuild,
		Disabled;
		
		
		public static ClaimBehaviourMode parseMode(String name){
			//System.out.println("Looking for " + name);
			for(ClaimBehaviourMode cb:ClaimBehaviourMode.values()){
				//System.out.println("Comparing " + cb.name() + " to " + name);
				if(cb.name().equalsIgnoreCase(name))
					return cb;
			}
			return ClaimBehaviourMode.RequireNone;
			
		}
		public boolean PerformTest(Location testLocation,Player testPlayer,boolean ShowMessages){
			WorldConfig wc = GriefPrevention.instance.getWorldCfg(testLocation.getWorld());
			PlayerData pd = null;
			if(testPlayer==null) return true;
			if(testPlayer!=null) pd = GriefPrevention.instance.dataStore.getPlayerData(testPlayer.getName());
			if((pd!=null)&&pd.ignoreClaims || this==RequireNone) return true;
			String result = null;
			Claim atposition  = GriefPrevention.instance.dataStore.getClaimAt(testLocation, false, null);
			if(atposition==null) return true; //unexpected...
			switch(this){
			case Disabled:
				GriefPrevention.sendMessage(testPlayer, TextMode.Err, Messages.ConfigDisabled);
				return false;
			case RequireNone:
				return true;	
			case RequireOwner:
				if(atposition.ownerName.equalsIgnoreCase(testPlayer.getName())){
					return true;
					
				}else {
					if(ShowMessages)
						GriefPrevention.sendMessage(testPlayer, 
								TextMode.Err, "You need to Own the claim to do that.");
					return false;
				}
			case RequireManager:
				
				if(atposition.isManager(testPlayer.getName())){
					return true; //success
				}
				else {
					//failed! if showmessages is on, show that message.
					if(ShowMessages)
						GriefPrevention.sendMessage(testPlayer, 
								TextMode.Err, "You need to have Manager trust to do that.");
					return false;
				}
			case RequireBuild:
				
				if(null==(result=atposition.allowBuild(testPlayer))){
					return true; //success
				}
				else {
					//failed! if showmessages is on, show that message.
					if(ShowMessages)
						GriefPrevention.sendMessage(testPlayer, 
								TextMode.Err, result);
					return false;
				}
			case RequireAccess:
				
				if(null==(result=atposition.allowAccess(testPlayer))){
					return true; //success
				}
				else {
					//failed! if showmessages is on, show that message.
					if(ShowMessages)
						GriefPrevention.sendMessage(testPlayer, 
								TextMode.Err, result);
					return false;
				}
			case RequireContainer:
				
				if(null==(result=atposition.allowContainers(testPlayer))){
					return true; //success
				}
				else {
					//failed! if showmessages is on, show that message.
					if(ShowMessages)
						GriefPrevention.sendMessage(testPlayer, 
								TextMode.Err,result);
					return false;
				}
			default:
				return false;
			}
			
			
			
			
			
			
		}
	}
	private PlacementRules Wilderness;
	private PlacementRules Claims;
	private ClaimBehaviourMode ClaimBehaviour;
	public ClaimBehaviourMode getBehaviourMode(){ return ClaimBehaviour;}
	public ClaimBehaviourData setBehaviourMode(ClaimBehaviourMode b){
		if(b==null) b = ClaimBehaviourMode.RequireNone;
		ClaimBehaviourData cdc = new ClaimBehaviourData(this);
		cdc.ClaimBehaviour=b;
		return cdc;
	}
	/**
	 * returns whether this Behaviour is allowed at the given location. if the passed player currently has
	 * ignoreclaims on, this will return true no matter what. This delegates to the overload that displays messages
	 * and passes true for the omitted argument.
	 * @param position Position to test.
	 * @param RelevantPlayer Player to test. Can be null for actions or behaviours that do not involve a player.
	 * @return whether this behaviour is Allowed or Denied in this claim.
	 */
	
		
	public ClaimAllowanceConstants Allowed(Location position,Player RelevantPlayer){
		return Allowed(position,RelevantPlayer,true);	
	}
	/**
	 * returns whether this Behaviour is allowed at the given location. if the passed player currently has
	 * ignoreclaims on, this will return true no matter what.
	 * @param position Position to test.
	 * @param RelevantPlayer Player to test. Can be null for actions or behaviours that do not involve a player.
	 * @param ShowMessages Whether a Denied result will display an appropriate message.
	 * @return whether this behaviour is Allowed or Denied in this claim.
	 */
	public ClaimAllowanceConstants Allowed(Location position,Player RelevantPlayer,boolean ShowMessages){
		
		System.out.println("ClaimBehaviour");
		//System.out.println("Testing Allowed," + BehaviourName);
		String result=null;
		PlayerData pd = null;
		boolean ignoringclaims = false;
		if(RelevantPlayer!=null) pd = GriefPrevention.instance.dataStore.getPlayerData(RelevantPlayer.getName());
		if(pd!=null) ignoringclaims = pd.ignoreClaims;
		if(ignoringclaims) return ClaimAllowanceConstants.Allow;
		Claim testclaim = GriefPrevention.instance.dataStore.getClaimAt(position, true, null);
		if(testclaim!=null){
			
			if(!this.ClaimBehaviour.PerformTest(position, RelevantPlayer, ShowMessages))
				return ClaimAllowanceConstants.Deny;
			
			
			boolean varresult =  this.Claims.Allow(position, RelevantPlayer, ShowMessages);
			
			
			
			return varresult?ClaimAllowanceConstants.Allow:ClaimAllowanceConstants.Deny;
		}

		
		//retrieve the appropriate Sea Level for this world.
		/*int sealevel = GriefPrevention.instance.getWorldCfg(position.getWorld()).seaLevelOverride();
		int yposition = position.getBlockY();
		boolean abovesealevel = yposition > sealevel;*/
		else if(testclaim==null){
			//we aren't inside a claim.
			//System.out.println(BehaviourName + "Wilderness test...");
			ClaimAllowanceConstants wildernessresult = Wilderness.Allow(position,RelevantPlayer,false)?ClaimAllowanceConstants.Allow:ClaimAllowanceConstants.Deny;
			if(wildernessresult.Denied() && ShowMessages){
				GriefPrevention.sendMessage(RelevantPlayer, TextMode.Err, Messages.ConfigDisabled,this.BehaviourName);
			}
			return wildernessresult;
			
		}
		
		
		return ClaimAllowanceConstants.Allow;
		
	}
	/**
	 * retrieves the placement rules for this Behaviour outside claims (in the 'wilderness')
	 * @return PlacementRules instance encapsulating applicable placement rules.
	 */
	public PlacementRules getWildernessRules() { return Wilderness;}
	/**
	 * retrieves the placement rules for this Behaviour inside claims.
	 * @return PlacementRules instance encapsulating applicable placement rules.
	 */
	public PlacementRules getClaimsRules(){ return Claims;}
	
	private String BehaviourName;
	/**
	 * retrieves the name for this Behaviour. This will be used in any applicable messages.
	 * @return Name for this behaviour.
	 */
	public String getBehaviourName(){ return BehaviourName;}
	@Override
	public String toString(){
		return BehaviourName + " in the wilderness " + getWildernessRules().toString() + " and in claims " + getClaimsRules().toString();
		
	}
	public Object clone(){
		return new ClaimBehaviourData(this);
	}
	public ClaimBehaviourData(ClaimBehaviourData Source){
		
		this.BehaviourName = Source.BehaviourName;
		this.Claims= (PlacementRules) Source.Claims.clone();
		this.Wilderness = (PlacementRules)Source.Wilderness.clone();
		this.ClaimBehaviour = Source.ClaimBehaviour;
		
		
		
	}
	public ClaimBehaviourData(String pName,FileConfiguration Source,FileConfiguration outConfig,String NodePath,ClaimBehaviourData Defaults){
		
		BehaviourName = pName;
		//we want to read NodePath.BelowSeaLevelWilderness and whatnot.
		//bases Defaults off another ClaimBehaviourData instance.
		Wilderness = new PlacementRules(Source,outConfig,NodePath + ".Wilderness",Defaults.getWildernessRules());
		Claims = new PlacementRules (Source,outConfig,NodePath + ".Claims",Defaults.getClaimsRules());
		String strmode = Source.getString(NodePath + ".Claims.ClaimControl",Defaults.getBehaviourMode().name());
		
		
		ClaimBehaviour = ClaimBehaviourMode.parseMode(strmode);
		
		outConfig.set(NodePath +".Claims.ClaimControl",ClaimBehaviour.name());
		
		
	}
	public ClaimBehaviourData(String pName,PlacementRules pWilderness,PlacementRules pClaims,ClaimBehaviourMode behaviourmode){
		Wilderness = pWilderness;
		Claims = pClaims;
		this.ClaimBehaviour=behaviourmode;
		BehaviourName=pName;
		
	}
	
	
	public static ClaimBehaviourData getOutsideClaims(String pName) { return new ClaimBehaviourData(pName,PlacementRules.Both,PlacementRules.Neither,ClaimBehaviourMode.RequireNone);}
	public static ClaimBehaviourData getInsideClaims(String pName) {return new ClaimBehaviourData(pName,PlacementRules.Neither,PlacementRules.Neither,ClaimBehaviourMode.RequireAccess);}
	public static ClaimBehaviourData getAboveSeaLevel(String pName){return new ClaimBehaviourData(pName,PlacementRules.AboveOnly,PlacementRules.AboveOnly,ClaimBehaviourMode.RequireNone);};
	public static ClaimBehaviourData getBelowSeaLevel(String pName){return new ClaimBehaviourData(pName,PlacementRules.BelowOnly,PlacementRules.BelowOnly,ClaimBehaviourMode.RequireNone);};
	public static ClaimBehaviourData getNone(String pName){ return new ClaimBehaviourData(pName,PlacementRules.Neither,PlacementRules.Neither,ClaimBehaviourMode.RequireNone);}
	public static ClaimBehaviourData getAll(String pName){ return new ClaimBehaviourData(pName,PlacementRules.Both,PlacementRules.Both,ClaimBehaviourMode.RequireNone);}
	
}
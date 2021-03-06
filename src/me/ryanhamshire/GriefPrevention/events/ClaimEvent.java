package me.ryanhamshire.GriefPrevention.events;

import me.ryanhamshire.GriefPrevention.Claim;

import org.bukkit.event.Event;
import org.bukkit.event.HandlerList;

public abstract class ClaimEvent extends Event {
	// Custom Event Requirements
	private static final HandlerList handlers = new HandlerList();

	public static HandlerList getHandlerList() {
		return handlers;

	}

	protected Claim claim;

	protected ClaimEvent(Claim c) {
		claim = c;
	}

	/**
	 * the claim being affected
	 * 
	 * @return
	 */
	public Claim getClaim() {
		return claim;

	}

	@Override
	public HandlerList getHandlers() {
		return handlers;
	}
}

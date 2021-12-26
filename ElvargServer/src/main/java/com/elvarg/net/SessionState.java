package com.elvarg.net;

/**
 * Represents a player's current session state.
 *
 * @author Professor Oak
 */
public enum SessionState {

    /**
     * The client is currently decoding the login protocol.
     */
    LOGGING_IN,

    /**
     * The client is now a player, and is logged in.
     */
    LOGGED_IN,

    /**
     * The player requested a logout using the logout button.
     */
    REQUESTED_LOG_OUT,
    
    ;
	
}
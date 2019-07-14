package com.alessiodp.core.common.user;

public interface User extends OfflineUser {
	@Override
	default boolean isOnline() {
		return true;
	}
	
	/**
	 * Does the user have the permission?
	 *
	 * @param permission the permission to check
	 * @return true if the user have the given permission
	 */
	boolean hasPermission(String permission);
	
	/**
	 * Is a player? (Check if is a player or a console user)
	 *
	 * @return true if is a player
	 */
	boolean isPlayer();
	
	/**
	 * Send a message to the user
	 *
	 * @param message the message to send
	 * @param colorTranslation toggle color translation
	 */
	void sendMessage(String message, boolean colorTranslation);
	
	/**
	 * Perform a chat message by the user
	 *
	 * @param message the message to send
	 */
	void chat(String message);
	
	/**
	 * Play a sound
	 *
	 * @param sound the name of the sound
	 * @param volume the volume of the sound
	 * @param pitch the pitch of the sound
	 */
	void playSound(String sound, float volume, float pitch);
}

package blocksworld;

import java.util.prefs.Preferences;

/**
 * Static object to store settings
 * 
 * @author W.Pasman 14jan2014
 * 
 */

public class BlocksWorldSettings {

	/**
	 * Use the global preference store for this user to store the settings.
	 */
	static public Preferences prefs = Preferences
			.userNodeForPackage(BlocksWorldSettings.class);

	/**
	 * get preferred x position of top left corner of the window.
	 * 
	 * @return preferred x pos of top left corner set by user, or 0 by default
	 */
	public static int getX() {
		return BlocksWorldSettings.prefs.getInt("x", 0);
	}

	/**
	 * get preferred y position of top left corner of the window.
	 * 
	 * @return preferred y pos of top left corner set by user, or 0 by default
	 */
	public static int getY() {
		return BlocksWorldSettings.prefs.getInt("y", 0);
	}

	/**
	 * save the window settings
	 * 
	 * @param x
	 *            :x pos of top left corner
	 * @param y
	 *            :y pos of top left corner
	 * @param width
	 *            :width of the window
	 * @param height
	 *            :height of the window
	 */
	public static void setWindowParams(int x, int y, int width, int height) {
		BlocksWorldSettings.prefs.putInt("x", x);
		BlocksWorldSettings.prefs.putInt("y", y);
		BlocksWorldSettings.prefs.putInt("width", width);
		BlocksWorldSettings.prefs.putInt("height", height);

	}

	/**
	 * get the window width. Defaults to 800
	 * 
	 * @return the window width. Defaults to 800. Minimum 400.
	 */
	public static int getWidth() {
		return Math.max(400, BlocksWorldSettings.prefs.getInt("width", 800));
	}

	/**
	 * get the window height. Defaults to 600
	 * 
	 * @return the window height. Defaults to 600. Minumum 300
	 */

	public static int getHeight() {
		return Math.max(300, BlocksWorldSettings.prefs.getInt("height", 600));
	}

}
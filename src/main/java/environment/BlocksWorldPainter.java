package environment;

/** BlocksWorld.java, version 1.11, December 9, 1998.
 Applet for interactive blocks world.
 Copyright 1998 by Rick Wagner, all rights reserved.
 Downloaded from http://rjwagner49.com/Science/ComputerScience/CS480/java/blocks/blocks.htm
 * Java source code is for educational purposes only. Viewing or downloading the
 * source implies your consent to obey the restrictions:
 * <p>
 * <ol>
 * <li>Use the source code for educational purposes only.
 * <li>Give appropriate attribution in all executables and listings.
 * <li>Reproduce these restrictions and conditions.
 * </ol>
 * @author Rick Wagner 1998
 * @author W.Pasman cleanup and modifications separating model from renderer
 */

import java.awt.Color;
import java.awt.Dimension;
import java.awt.Event;
import java.awt.Frame;
import java.awt.Graphics;
import java.awt.Image;
import java.awt.Point;
import java.awt.event.ComponentAdapter;
import java.awt.event.ComponentEvent;
import java.util.ArrayList;
import java.util.List;

import javax.swing.SwingUtilities;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;

import eisinterface.BWEnvironment;

/**
 * This is an educational example of object oriented design for a 3D graphics
 * applet. Use of this source code is authorized for educational purposes only.
 * No use without proper attribution to Rick Wagner (wagner@pollux.usc.edu) at
 * the University of Southern California.
 * <p>
 * Compiled with the Sun JDK 1.1 and written for the JDK 1.0 so it will run in
 * all browsers.
 * <p>
 * The prefix naming convention used here is a modified Hungarian notation. "s"
 * is string, "sf" is single precision floating point, "i" is integer, "b" is
 * boolean, "d" is dimension, and "hm" is homogeneous matrix. Other objects
 * defined here have no prefix. One-based indexing of arrays is used (the
 * zeroeth element is generally reserved for swap space).
 * <p>
 * 
 * <ul>
 * <li>"Spacebar" to reset the blocks.
 * <li>"n" to move nearer the blocks.
 * <li>"f" to move farther from the blocks.
 * <li>"i" to increment the number of blocks and reset.
 * <li>"d" to decrement the number of blocks and reset.
 * <li>"s" for a shorter focal length (zoom out).
 * <li>"l" for a longer focal length (zoom in).
 * <li>"x" to translate to the left.
 * <li>"X" to translate to the right.
 * <li>"y" to translate down.
 * <li>"Y" to translate up.
 * <li>"Left arrow" to pan left.
 * <li>"Right arrow" to pan right.
 * <li>"Down arrow" to pan down.
 * <li>"Up arrow" to pan up.
 * 
 * </ul>
 * Java source code is for educational purposes only. Viewing or downloading the
 * source implies your consent to obey the restrictions:
 * <p>
 * <ol>
 * <li>Use the source code for educational purposes only.
 * <li>Give appropriate attribution in all executables and listings.
 * <li>Reproduce these restrictions and conditions.
 * </ol>
 */
@SuppressWarnings("serial")
public class BlocksWorldPainter extends Frame implements ChangeListener {
	private static final float MAX_CLICK_DISTANCE = 500;
	// Applet instance variables:
	private String sVerNum = "1.11";
	private HMatrix3D hmPerspXform; // Perspective transform
	// private float sfFocalLength; // For perspective projection

	/**
	 * Multipliccation factor to determine focal length from the width of the
	 * screen. 1 means focal length= width of screen.
	 */
	private float zoomFactor = 1;
	private Point3D ViewPoint; // The viewer's location in world space
	private boolean bSelectedDest = false; // So mousedown() knows what
											// selection mode
	// private int iSourceBlockIndex = 0;
	// For remembering the source block in stacking
	private Cube3D iSourceBlock = null;
	// private int iDestBlockIndex = 0;
	// For remembering the destination block in stacking
	private Cube3D iDestBlock = null;
	private float sfPositionFactor; // Viewpoint positioning
	private float sfXPosition; // Viewpoint positioning
	private float sfYPosition; // Viewpoint positioning
	private float sfPanX; // Viewpoint orienting
	private float sfPanY; // Viewpoint orienting
	private Image imOffScreen = null; // Offscreen image for double buffering
	private Graphics grOffScreen = null; // Offscreen graphics for double
											// buffering
	private BlocksWorldModel world;

	/**
	 * To allow browsers to get information about the applet:
	 * 
	 * @return applet info string.
	 */
	public String getAppletInfo() {
		return "BlocksWorld applet, version " + sVerNum
				+ ", by Rick Wagner, copyright 1998,\nall rights reserved.\n\n"
				+ "This is an educational example of object oriented design.\n"
				+ "Compiled December 9, 1998. Source code use authorized for\n"
				+ "educational purposes only. No use without attribution.";
	}

	public BlocksWorldPainter(BlocksWorldModel model) {
		world = model;
		setMinimumSize(new Dimension(320, 200));
		setSize(new Dimension(BlocksWorldSettings.getWidth(),
				BlocksWorldSettings.getHeight()));
		setLocation(BlocksWorldSettings.getX(), BlocksWorldSettings.getY());
		init();
		// pack(); no pack, as we don't have a canvas yet.
		setVisible(true);
		start();
		addWindowListeners();
	}

	private void addWindowListeners() {
		addComponentListener(new ComponentAdapter() {
			@Override
			public void componentMoved(ComponentEvent e) {
				saveWindowDimensions();
			}

			@Override
			public void componentResized(ComponentEvent e) {
				saveWindowDimensions();
			}
		});
	}

	private void saveWindowDimensions() {
		BlocksWorldSettings.setWindowParams(getX(), getY(), getWidth(),
				getHeight());

	}

	/**
	 * get the current focal length
	 * 
	 * @return zoomFactor * current screen width.
	 */
	private float getFocalLength() {
		return zoomFactor * getWidth();
	}

	/** Initialize the blocks world */
	public void init() {
		world.addListener(this);
		this.setBackground(Color.lightGray);

		bSelectedDest = false;

		sfPositionFactor = 1;
		sfXPosition = 0;
		sfYPosition = 0;
		sfPanX = 0;
		sfPanY = 0;
		SetupPerspXform(); // Uses the starting view point (0, 1000, -1732)
	}

	public void showStatus(String msg) {
		System.out.println(msg);
	}

	// Execute this code after initialization
	public void start() {
		this.requestFocus(); // So we can get keyboard input
		// System.out.println("\n" + this.getAppletInfo());
		this.showStatus("Welcome to the blocks world. Click blocks to stack. Spacebar to reset.");
	}

	/**
	 * The frame painting function
	 */
	public void paint(Graphics g) {
		// Code for displaying images or drawing in the applet frame
		int i;
		int j;
		float sfDSQi;
		float sfDSQj;
		Point3D vp;
		int width = getWidth();
		int height = getHeight();
		// Make a copy of the blocks array for painting
		List<Cube3D> blockCopy = new ArrayList<Cube3D>(world.getBlocks());

		g.clearRect(0, 0, width, height);

		vp = new Point3D(-ViewPoint.getX(), -ViewPoint.getY(),
				-ViewPoint.getZ()); // Opposite view point

		/**
		 * Painter's algorithm. Works with BlocksWorld where all the faces are
		 * the same size. Not generally correct. Z-buffer algorithm is used with
		 * most low level 3D graphics libraries. Sort the copies of the blocks
		 * on distance from the view point:
		 */
		// FIXME There is built-in support for sorting in Java.
		// FIXME cleanup this using straight iterator?
		for (i = 1; i <= blockCopy.size() - 1; i++) // This fixed loop bubble
													// sort is
		// good enough
		{ // for blocks world. More general applications
			for (j = i + 1; j <= blockCopy.size(); j++) // will use a faster
														// sort
			// algorithm.
			{
				// Put most distant first:
				sfDSQi = blockCopy.get(i - 1).getDSquared(vp); // Distance from
																// the
																// block to the
																// viewpoint
				sfDSQj = blockCopy.get(j - 1).getDSquared(vp);
				if (sfDSQj > sfDSQi) {
					// swap i and j
					Cube3D swap = blockCopy.get(i - 1);
					blockCopy.set(i - 1, blockCopy.get(j - 1));
					blockCopy.set(j - 1, swap);
				}
			}
		}

		/*
		 * Have the copies of the blocks paint themselves.
		 */
		for (Cube3D block : blockCopy) {
			block.paint(g, hmPerspXform, vp, getFocalLength(), width, height);
		}

		// Draw a recessed frame around the applet border. Designed for
		// gray-on-gray browser background.
		g.setColor(Color.black);
		g.drawLine(0, 0, width - 1, 0);
		g.drawLine(0, 0, 0, height - 1);
		g.setColor(Color.white);
		g.drawLine(0, height - 1, width - 1, height - 1);
		g.drawLine(width - 1, 1, width - 1, height - 1);

	}

	// Implements double buffering
	public void update(Graphics g) {
		int width = getWidth();
		int height = getHeight();
		if (imOffScreen == null || imOffScreen.getWidth(null) != width
				|| imOffScreen.getHeight(null) != height) {

			// System.out.println("rescale of window detected. Resizing");
			// Make sure the offscreen and graphics exist
			imOffScreen = this.createImage(width, height);
			grOffScreen = imOffScreen.getGraphics();
			grOffScreen.clearRect(0, 0, width, height);
		}
		this.paint(grOffScreen);
		g.drawImage(imOffScreen, 0, 0, null);
	}

	/**
	 * Select a block for source or destination: bSelectedDest is initially
	 * false and toggles with each mouse click on the applet frame. The cube
	 * nearest (in 2D screen space) to the mouse click point gets selected. This
	 * is really a 2D estimation and therefore it is part of the GUI.
	 */
	public boolean mouseDown(Event e, int x, int y) {

		Point ptBCP = new Point(0, 0); // Block center point
		Cube3D iClosestBlock = world.get(0);
		float sfDSquared = 0;
		float sfAdjust = 0;
		float sfMin = 100000000; // 2d distance to block closest to click.

		// Find the closest block (a very simple solution to the selection
		// interaction task (seems to work fine)):
		for (Cube3D block : world.getBlocks()) {
			// Compute the distance from the cube centerpoint to the mouse
			// point:
			Point3D TempPoint = block.getCenterPoint();
			TempPoint.transform(hmPerspXform);
			sfAdjust = getFocalLength() / TempPoint.getZ(); // Adjust x and y
															// for
															// perspective view
			ptBCP.x = getWidth() / 2 + ((int) (TempPoint.getX() * sfAdjust));
			ptBCP.y = getHeight() / 2 - ((int) (TempPoint.getY() * sfAdjust));
			sfDSquared = (ptBCP.x - x) * (ptBCP.x - x) + (ptBCP.y - y)
					* (ptBCP.y - y);
			if (sfDSquared < sfMin) {
				sfMin = sfDSquared;
				iClosestBlock = block;
			}
		}
		if (sfMin > MAX_CLICK_DISTANCE) {
			if (bSelectedDest) {
				showStatus("move " + BWEnvironment.blockName(iSourceBlock)
						+ " to floor");
				bSelectedDest = false; // Toggle the selection state
				iSourceBlock.setSelected(false);
				world.move(iSourceBlock, null);
				return true;
			} else {
				showStatus("nothing selected");
			}
			return false;
		}

		if (iClosestBlock.topBlock()) {
			if (bSelectedDest && iClosestBlock == iSourceBlock) {
				this.showStatus("Block "
						+ BWEnvironment.blockName(iClosestBlock)
						+ " cannot be stacked on top of itself.");
			} else {
				if (bSelectedDest) {
					// We are selecting the destination block for stacking the
					// previously selected block
					iDestBlock = iClosestBlock;
					bSelectedDest = false; // Toggle the selection state
					iSourceBlock.setSelected(false);
					world.move(iSourceBlock, iDestBlock);

					this.showStatus("Block "
							+ BWEnvironment.blockName(iSourceBlock)
							+ " stacked on block "
							+ BWEnvironment.blockName(iDestBlock) + ".");
				} else {
					// We are selecting the source block for stacking on the
					// next block selected
					this.showStatus("Block "
							+ BWEnvironment.blockName(iClosestBlock)
							+ " selected.");

					// Change the selected block color to red and redisplay:
					iClosestBlock.setSelected(true);
					repaint(); // pure gui change, need to ask for repaint.

					iSourceBlock = iClosestBlock;
					bSelectedDest = true; // Toggle the selection state
				}
			}
		} else {
			this.showStatus("Block " + BWEnvironment.blockName(iClosestBlock)
					+ " has a block above it.");
		}
		return true;
	}

	/**
	 * user presses key
	 */
	public boolean keyDown(Event e, int k) {
		switch (k) {
		case 32: // Space bar
		{
			world.allBlocksToTable(); // or reset(some number)?
			bSelectedDest = false;
			repaint();
			this.showStatus("Blocks world reset.");
			break;
		}
		case 88: // X (translate right)
		{
			sfXPosition += (float) 10;
			SetupPerspXform(); // Translates right 10
			repaint();
			this.showStatus("Moved right by 10 to "
					+ Integer.toString((int) sfXPosition) + ".");
			break;
		}
		case 89: // Y (translate up)
		{
			sfYPosition += (float) 10;
			SetupPerspXform(); // Translates up 10
			repaint();
			this.showStatus("Moved up by 10 to "
					+ Integer.toString((int) sfYPosition) + ".");
			break;
		}
		case 102: // f (farther)
		{
			sfPositionFactor *= (float) 1.1;
			SetupPerspXform(); // Move outward 10%
			repaint();
			this.showStatus("Moved farther away by 10 percent.");
			break;
		}
		case 108: // l (longer focal length)
		{
			zoomFactor *= (float) 1.1;
			SetupPerspXform(); // Zooms in 10%
			repaint();
			this.showStatus("Zoomed in by 10 percent.");
			break;
		}
		case 110: // n (nearer)
		{
			if (sfPositionFactor > .5) {
				sfPositionFactor *= (float) 0.9;
				SetupPerspXform(); // Move inward 10%
				repaint();
				this.showStatus("Moved nearer by 10 percent.");
			} else {
				this.showStatus("Can't move any nearer.");
			}
			break;
		}
		case 115: // s (shorter focal length)
		{
			zoomFactor *= (float) 0.9;
			SetupPerspXform(); // Zooms out 10%
			repaint();
			this.showStatus("Zoomed out by 10 percent.");
			break;
		}
		case 120: // x (translate left)
		{
			sfXPosition -= (float) 10;
			SetupPerspXform(); // Translates left 10
			repaint();
			this.showStatus("Moved left by 10 to "
					+ Integer.toString((int) sfXPosition) + ".");
			break;
		}
		case 121: // y (translate down)
		{
			sfYPosition -= (float) 10;
			SetupPerspXform(); // Translates down 10
			repaint();
			this.showStatus("Moved down by 10 to "
					+ Integer.toString((int) sfYPosition) + ".");
			break;
		}
		case 1004: // Up arrow (pan up)
		{
			sfPanX += (float) 1;
			SetupPerspXform();
			repaint();
			this.showStatus("Panned up one degree.");
			break;
		}
		case 1005: // Down arrow (pan down)
		{
			sfPanX -= (float) 1;
			SetupPerspXform();
			repaint();
			this.showStatus("Panned down one degree.");
			break;
		}
		case 1006: // Left arrow (pan left)
		{
			sfPanY += (float) 1;
			SetupPerspXform();
			repaint();
			this.showStatus("Panned left one degree.");
			break;
		}
		case 1007: // Right arrow (pan right)
		{
			sfPanY -= (float) 1;
			SetupPerspXform();
			repaint();
			this.showStatus("Panned left one degree.");
			break;
		}
		}
		return true;
	}

	/**
	 * initialize the perspective transformation
	 */
	public void SetupPerspXform() {
		HMatrix3D hmXform; // Transform matrix
		RHMatrix3DX hmRMX; // Rotation transform matrices
		RHMatrix3DY hmRMY;
		RHMatrix3DZ hmRMZ;
		HMatrix3D hmXformR; // Compound rotation matrix

		float vx = sfXPosition;
		float vy = 1000 * sfPositionFactor + sfYPosition;
		float vz = -1732 * sfPositionFactor;
		ViewPoint = new Point3D(vx, vy, vz); // The point in world space the
												// viewer is seeing from

		// Set up the perspective transform:
		hmPerspXform = new HMatrix3D();
		hmPerspXform.setElement(4, 3, 1 / getFocalLength());
		hmPerspXform.setElement(4, 4, 0);

		// Rotate the view direction:
		hmRMX = new RHMatrix3DX(-30 + sfPanX); // Rotation of viewpoint about X
												// in degrees
		hmRMY = new RHMatrix3DY(sfPanY); // Rotation of viewpoint about Y in
											// degrees
		hmRMZ = new RHMatrix3DZ(0); // Rotation of viewpoint about Z in degrees
		hmXformR = hmRMX.multiply(hmRMZ, hmRMX); // Compound rotation
		hmXformR = hmRMY.multiply(hmXformR, hmRMY); // Compound rotation

		// Transform the view:
		hmXform = new THMatrix3D(ViewPoint); // Translation matrix constructed
												// from the view point
		hmXform = hmXform.multiply(hmXformR, hmXform); // Multiply the rot.
														// matrix by the trans.
														// matrix

		hmPerspXform = hmXform.multiply(hmPerspXform, hmXform); // Postmultiply
																// the
																// perspective
																// matrix by the
																// view
																// transformation
	} // End of SetupPerspXform()

	/**
	 * IMPLEMENTS CHANGELISTENER
	 */
	public void stateChanged(ChangeEvent e) {
		SwingUtilities.invokeLater(new Runnable() {
			public void run() {
				repaint();
			}
		});
	}

	/**
	 * Close the GUI.
	 */
	public void close() {
		world.removeListener(this);
		SwingUtilities.invokeLater(new Runnable() {
			public void run() {
				saveWindowDimensions();
				setVisible(false);
				dispose();
			}
		});
	}

}

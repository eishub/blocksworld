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
import java.awt.Frame;
import java.awt.Graphics;
import java.awt.Image;
import java.awt.Point;
import java.awt.event.ComponentAdapter;
import java.awt.event.ComponentEvent;
import java.awt.event.KeyAdapter;
import java.awt.event.KeyEvent;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
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
	private final String sVerNum = "1.11";
	// Perspective transform
	private HMatrix3D hmPerspXform;
	/**
	 * Multiplication factor to determine focal length from the width of the screen.
	 * 1 means focal length= width of screen.
	 */
	private float zoomFactor = 1;
	// The viewer's location in world space
	private Point3D ViewPoint;
	// So mousedown() knows what selection mode
	private boolean bSelectedDest = false;
	// For remembering the source block in stacking
	private Cube3D iSourceBlock = null;
	// private int iDestBlockIndex = 0;
	// For remembering the destination block in stacking
	private Cube3D iDestBlock = null;
	// Viewpoint positioning
	private float sfPositionFactor;
	private float sfXPosition;
	private float sfYPosition;
	// Viewpoint orienting
	private float sfPanX;
	private float sfPanY;
	// Offscreen image for double buffering
	private Image imOffScreen = null;
	// Offscreen graphics for double buffering
	private Graphics grOffScreen = null;
	private final BlocksWorldModel world;

	/**
	 * To allow browsers to get information about the applet:
	 *
	 * @return applet info string.
	 */
	public String getAppletInfo() {
		return "BlocksWorld applet, version " + this.sVerNum
				+ ", by Rick Wagner, copyright 1998,\nall rights reserved.\n\n"
				+ "This is an educational example of object oriented design.\n"
				+ "Compiled December 9, 1998. Source code use authorized for\n"
				+ "educational purposes only. No use without attribution.";
	}

	public BlocksWorldPainter(final BlocksWorldModel model) {
		this.world = model;
		setMinimumSize(new Dimension(320, 200));
		setSize(new Dimension(BlocksWorldSettings.getWidth(), BlocksWorldSettings.getHeight()));
		setLocation(BlocksWorldSettings.getX(), BlocksWorldSettings.getY());
		init();
		setVisible(true);
		start();
		addWindowListeners();
	}

	private void addWindowListeners() {
		addComponentListener(new ComponentAdapter() {
			@Override
			public void componentMoved(final ComponentEvent e) {
				saveWindowDimensions();
			}

			@Override
			public void componentResized(final ComponentEvent e) {
				saveWindowDimensions();
			}
		});

		addMouseListener(new MouseAdapter() {
			@Override
			public void mouseClicked(final MouseEvent e) {
				handleMousePressed(e, e.getX(), e.getY());
			}
		});

		addKeyListener(new KeyAdapter() {
			@Override
			public void keyPressed(final KeyEvent e) {
				final char c = e.getKeyChar();
				// returns UNICODE char. But arrows have no unicode char...
				if (c == KeyEvent.CHAR_UNDEFINED) {
					handleKeyCodePressed(e.getKeyCode());
				} else {
					handleKeyPressed(c);
				}
			}
		});
	}

	private void saveWindowDimensions() {
		BlocksWorldSettings.setWindowParams(getX(), getY(), getWidth(), getHeight());
	}

	/**
	 * get the current focal length
	 *
	 * @return zoomFactor * current screen width.
	 */
	private float getFocalLength() {
		return this.zoomFactor * getWidth();
	}

	/** Initialize the blocks world */
	public void init() {
		this.world.addListener(this);
		setBackground(Color.lightGray);

		this.bSelectedDest = false;

		this.sfPositionFactor = 1;
		this.sfXPosition = 0;
		this.sfYPosition = 0;
		this.sfPanX = 0;
		this.sfPanY = 0;
		SetupPerspXform(); // Uses the starting view point (0, 1000, -1732)
	}

	public void showStatus(final String msg) {
		System.out.println(msg);
	}

	// Execute this code after initialization
	public void start() {
		requestFocus(); // So we can get keyboard input
		showStatus("Welcome to the blocks world. Click blocks to stack. Spacebar to reset.");
	}

	/**
	 * The frame painting function
	 */
	@Override
	public void paint(final Graphics g) {
		// Code for displaying images or drawing in the applet frame
		int i;
		int j;
		float sfDSQi;
		float sfDSQj;
		Point3D vp;
		final int width = getWidth();
		final int height = getHeight();
		// Make a copy of the blocks array for painting
		final List<Cube3D> blockCopy = new ArrayList<>(this.world.getBlocks());

		g.clearRect(0, 0, width, height);

		// Opposite viewpoint
		vp = new Point3D(-this.ViewPoint.getX(), -this.ViewPoint.getY(), -this.ViewPoint.getZ());

		/**
		 * Painter's algorithm. Works with BlocksWorld where all the faces are the same
		 * size. Not generally correct. Z-buffer algorithm is used with most low level
		 * 3D graphics libraries. Sort the copies of the blocks on distance from the
		 * view point:
		 */
		// FIXME There is built-in support for sorting in Java.
		// FIXME cleanup this using straight iterator?
		// This fixed loop bubble sort is good enough for blocks world.
		for (i = 1; i <= blockCopy.size() - 1; i++) {
			for (j = i + 1; j <= blockCopy.size(); j++) {
				// Put most distant first:
				// Distance from the block to the viewpoint
				sfDSQi = blockCopy.get(i - 1).getDSquared(vp);
				sfDSQj = blockCopy.get(j - 1).getDSquared(vp);
				if (sfDSQj > sfDSQi) {
					// swap i and j
					final Cube3D swap = blockCopy.get(i - 1);
					blockCopy.set(i - 1, blockCopy.get(j - 1));
					blockCopy.set(j - 1, swap);
				}
			}
		}

		/*
		 * Have the copies of the blocks paint themselves.
		 */
		for (final Cube3D block : blockCopy) {
			block.paint(g, this.hmPerspXform, vp, getFocalLength(), width, height);
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

	@Override
	public void update(final Graphics g) { // Implements double buffering
		final int width = getWidth();
		final int height = getHeight();
		if (this.imOffScreen == null || this.imOffScreen.getWidth(null) != width
				|| this.imOffScreen.getHeight(null) != height) {
			// Make sure the offscreen and graphics exist
			this.imOffScreen = this.createImage(width, height);
			this.grOffScreen = this.imOffScreen.getGraphics();
			this.grOffScreen.clearRect(0, 0, width, height);
		}
		paint(this.grOffScreen);
		g.drawImage(this.imOffScreen, 0, 0, null);
	}

	/**
	 * Select a block for source or destination: bSelectedDest is initially false
	 * and toggles with each mouse click on the applet frame. The cube nearest (in
	 * 2D screen space) to the mouse click point gets selected. This is really a 2D
	 * estimation and therefore it is part of the GUI.
	 */
	private boolean handleMousePressed(final MouseEvent e, final int x, final int y) {
		final Point ptBCP = new Point(0, 0); // Block center point
		Cube3D iClosestBlock = this.world.get(0);
		float sfDSquared = 0;
		float sfAdjust = 0;
		float sfMin = 100000000; // 2d distance to block closest to click.

		// Find the closest block (a very simple solution to the selection
		// interaction task (seems to work fine)):
		for (final Cube3D block : this.world.getBlocks()) {
			// Compute the distance from the cube centerpoint to the mouse
			// point:
			final Point3D TempPoint = block.getCenterPoint();
			TempPoint.transform(this.hmPerspXform);
			// Adjust x and y for perspective view
			sfAdjust = getFocalLength() / TempPoint.getZ();
			ptBCP.x = getWidth() / 2 + ((int) (TempPoint.getX() * sfAdjust));
			ptBCP.y = getHeight() / 2 - ((int) (TempPoint.getY() * sfAdjust));
			sfDSquared = (ptBCP.x - x) * (ptBCP.x - x) + (ptBCP.y - y) * (ptBCP.y - y);
			if (sfDSquared < sfMin) {
				sfMin = sfDSquared;
				iClosestBlock = block;
			}
		}
		if (sfMin > MAX_CLICK_DISTANCE) {
			if (this.bSelectedDest) {
				showStatus("move " + BWEnvironment.blockName(this.iSourceBlock) + " to floor");
				this.bSelectedDest = false; // Toggle the selection state
				this.iSourceBlock.setSelected(false);
				this.world.move(this.iSourceBlock, null);
				return true;
			} else {
				showStatus("nothing selected");
			}
			return false;
		}

		if (iClosestBlock.topBlock()) {
			if (this.bSelectedDest && iClosestBlock == this.iSourceBlock) {
				showStatus("Block " + BWEnvironment.blockName(iClosestBlock) + " cannot be stacked on top of itself.");
			} else {
				if (this.bSelectedDest) {
					// We are selecting the destination block for stacking the
					// previously selected block
					this.iDestBlock = iClosestBlock;
					this.bSelectedDest = false; // Toggle the selection state
					this.iSourceBlock.setSelected(false);
					this.world.move(this.iSourceBlock, this.iDestBlock);

					showStatus("Block " + BWEnvironment.blockName(this.iSourceBlock) + " stacked on block "
							+ BWEnvironment.blockName(this.iDestBlock) + ".");
				} else {
					// We are selecting the source block for stacking on the
					// next block selected
					showStatus("Block " + BWEnvironment.blockName(iClosestBlock) + " selected.");

					// Change the selected block color to red and redisplay:
					iClosestBlock.setSelected(true);
					repaint(); // pure gui change, need to ask for repaint.

					this.iSourceBlock = iClosestBlock;
					this.bSelectedDest = true; // Toggle the selection state
				}
			}
		} else {
			showStatus("Block " + BWEnvironment.blockName(iClosestBlock) + " has a block above it.");
		}
		return true;
	}

	/**
	 * An arrow key was pressed. We can only handle this through the key's code.
	 *
	 * @param code the key code.
	 */
	public void handleKeyCodePressed(final int code) {
		switch (code) {
		case KeyEvent.VK_UP:
			this.sfPanX += 1;
			SetupPerspXform();
			repaint();
			showStatus("Panned up one degree.");
			break;
		case KeyEvent.VK_DOWN:
			this.sfPanX -= 1;
			SetupPerspXform();
			repaint();
			showStatus("Panned down one degree.");
			break;
		case KeyEvent.VK_LEFT:
			this.sfPanY += 1;
			SetupPerspXform();
			repaint();
			showStatus("Panned left one degree.");
			break;
		case KeyEvent.VK_RIGHT:
			this.sfPanY -= 1;
			SetupPerspXform();
			repaint();
			showStatus("Panned left one degree.");
			break;
		}
	}

	/**
	 * user presses key k.
	 *
	 * @param k the key ascii character code.
	 */
	private boolean handleKeyPressed(final char k) {
		switch (k) {
		case 32: // Space bar
			this.world.allBlocksToTable(); // or reset(some number)?
			this.bSelectedDest = false;
			repaint();
			showStatus("Blocks world reset.");
			break;
		case 88: // X (translate right)
			this.sfXPosition += 10;
			SetupPerspXform(); // Translates right 10
			repaint();
			showStatus("Moved right by 10 to " + Integer.toString((int) this.sfXPosition) + ".");
			break;
		case 89: // Y (translate up)
			this.sfYPosition += 10;
			SetupPerspXform(); // Translates up 10
			repaint();
			showStatus("Moved up by 10 to " + Integer.toString((int) this.sfYPosition) + ".");
			break;
		case 102: // f (farther)
			this.sfPositionFactor *= (float) 1.1;
			SetupPerspXform(); // Move outward 10%
			repaint();
			showStatus("Moved farther away by 10 percent.");
			break;
		case 108: // l (longer focal length)
			this.zoomFactor *= (float) 1.1;
			SetupPerspXform(); // Zooms in 10%
			repaint();
			showStatus("Zoomed in by 10 percent.");
			break;
		case 110: // n (nearer)
			if (this.sfPositionFactor > .5) {
				this.sfPositionFactor *= (float) 0.9;
				SetupPerspXform(); // Move inward 10%
				repaint();
				showStatus("Moved nearer by 10 percent.");
			} else {
				showStatus("Can't move any nearer.");
			}
			break;
		case 115: // s (shorter focal length)
			this.zoomFactor *= (float) 0.9;
			SetupPerspXform(); // Zooms out 10%
			repaint();
			showStatus("Zoomed out by 10 percent.");
			break;
		case 120: // x (translate left)
			this.sfXPosition -= 10;
			SetupPerspXform(); // Translates left 10
			repaint();
			showStatus("Moved left by 10 to " + Integer.toString((int) this.sfXPosition) + ".");
			break;
		case 121: // y (translate down)
			this.sfYPosition -= 10;
			SetupPerspXform(); // Translates down 10
			repaint();
			showStatus("Moved down by 10 to " + Integer.toString((int) this.sfYPosition) + ".");
			break;
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

		final float vx = this.sfXPosition;
		final float vy = 1000 * this.sfPositionFactor + this.sfYPosition;
		final float vz = -1732 * this.sfPositionFactor;
		// The point in world space the viewer is seeing from
		this.ViewPoint = new Point3D(vx, vy, vz);

		// Set up the perspective transform:
		this.hmPerspXform = new HMatrix3D();
		this.hmPerspXform.setElement(4, 3, 1 / getFocalLength());
		this.hmPerspXform.setElement(4, 4, 0);

		// Rotate the view direction:
		// Rotation of viewpoint about X in degrees
		hmRMX = new RHMatrix3DX(-30 + this.sfPanX);
		// Rotation of viewpoint about Y in degrees
		hmRMY = new RHMatrix3DY(this.sfPanY);
		// Rotation of viewpoint about Z in degrees
		hmRMZ = new RHMatrix3DZ(0);
		// Compound rotation
		hmXformR = hmRMX.multiply(hmRMZ, hmRMX);
		// Compound rotation
		hmXformR = hmRMY.multiply(hmXformR, hmRMY);

		// Transform the view:
		// Translation matrix constructed from the view point
		hmXform = new THMatrix3D(this.ViewPoint);
		// Multiply the rot. matrix by the trans. matrix
		hmXform = hmXform.multiply(hmXformR, hmXform);

		// Postmultiply the perspective matrix by the view transformation
		this.hmPerspXform = hmXform.multiply(this.hmPerspXform, hmXform);
	} // End of SetupPerspXform()

	/**
	 * IMPLEMENTS CHANGELISTENER
	 */
	@Override
	public void stateChanged(final ChangeEvent e) {
		SwingUtilities.invokeLater(this::repaint);
	}

	/**
	 * Close the GUI.
	 */
	public void close() {
		this.world.removeListener(this);
		SwingUtilities.invokeLater(() -> {
			saveWindowDimensions();
			setVisible(false);
			dispose();
		});
	}
}

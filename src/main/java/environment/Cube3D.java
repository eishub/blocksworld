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
import java.awt.Font;
import java.awt.FontMetrics;
import java.awt.Graphics;
import java.awt.Polygon;

/**
 * The cube class is built of 6 squares (a surface model), by default with its
 * bottom face on the X-Z plane. The cube has a paint() method for painting
 * itself.
 */
public class Cube3D {
	// Face array
	private Square3D F[];
	// For distance calculations
	private Point3D centerPoint;
	// The block knows its own color
	private Color BlockColor = Color.getHSBColor((float) Math.random(), (float) Math.random(), 1f);
	// Not any blocks on top of it, true by default
	private boolean bTopBlock;
	// Index of the block this one rests on, null by default
	private Cube3D iOnBlock;
	private boolean selected;
	private int blockNumber;
	private Point3D frontFaceCenter;

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + this.blockNumber;
		return result;
	}

	/**
	 * Blocks are equal if their BLOCK NUMBER is equal. The rest is irrelevant.
	 */
	@Override
	public boolean equals(final Object obj) {
		if (this == obj) {
			return true;
		} else if (obj == null) {
			return false;
		} else if (getClass() != obj.getClass()) {
			return false;
		}

		final Cube3D other = (Cube3D) obj;
		if (this.blockNumber != other.blockNumber) {
			return false;
		}

		return true;
	}

	/*
	 *
	 * Construct block having number i.
	 */
	public Cube3D(final int i) {
		if (i <= 0) {
			throw new IllegalArgumentException("illegal block number " + i);
		}
		this.blockNumber = i;
		reset();
	}

	/**
	 * put point to initial position (in origin, 50 below table). Erase all
	 * properties, so block is blank and on table after reset.
	 */
	public synchronized void reset() {
		this.bTopBlock = true;
		this.iOnBlock = null;

		Point3D V[];
		// 50 units above origin, -Y is up
		this.centerPoint = new Point3D(0, -50, 0);
		// center of front face.
		this.frontFaceCenter = new Point3D(0, -50, 50);

		V = new Point3D[5];
		this.F = new Square3D[7];

		V[1] = new Point3D(50, -100, 50); // Front face
		V[2] = new Point3D(-50, -100, 50);
		V[3] = new Point3D(-50, 0, 50);
		V[4] = new Point3D(50, 0, 50);
		this.F[1] = new Square3D(V);

		V[1] = new Point3D(50, -100, -50); // Right face
		V[2] = new Point3D(50, -100, 50);
		V[3] = new Point3D(50, 0, 50);
		V[4] = new Point3D(50, 0, -50);
		this.F[2] = new Square3D(V);

		V[1] = new Point3D(-50, -100, -50); // Back face
		V[2] = new Point3D(50, -100, -50);
		V[3] = new Point3D(50, 0, -50);
		V[4] = new Point3D(-50, 0, -50);
		this.F[3] = new Square3D(V);

		V[1] = new Point3D(-50, -100, 50); // Left face
		V[2] = new Point3D(-50, -100, -50);
		V[3] = new Point3D(-50, 0, -50);
		V[4] = new Point3D(-50, 0, 50);
		this.F[4] = new Square3D(V);

		V[1] = new Point3D(50, -100, -50); // Top face
		V[2] = new Point3D(-50, -100, -50);
		V[3] = new Point3D(-50, -100, 50);
		V[4] = new Point3D(50, -100, 50);
		this.F[5] = new Square3D(V);

		V[1] = new Point3D(50, 0, -50); // Top face
		V[2] = new Point3D(50, 0, 50);
		V[3] = new Point3D(-50, 0, 50);
		V[4] = new Point3D(-50, 0, -50);
		this.F[6] = new Square3D(V);
	}

	public Cube3D(final Cube3D c) {
		int i;
		this.bTopBlock = c.bTopBlock;
		this.iOnBlock = c.iOnBlock;
		this.BlockColor = c.BlockColor;
		this.centerPoint = new Point3D(c.centerPoint);
		this.frontFaceCenter = new Point3D(c.frontFaceCenter);
		this.F = new Square3D[7];
		for (i = 1; i <= 6; i++) {
			this.F[i] = new Square3D(c.F[i]);
		}
	}

	public synchronized void transform(final HMatrix3D m) {
		this.centerPoint.transform(m);
		this.frontFaceCenter.transform(m);
		int i = 0;
		for (i = 1; i <= 6; i++) {
			this.F[i].transform(m);
		}
	}

	public float getDSquared(final Point3D p) {
		// Get the square of the distance from the center of the cube to some point:
		return (this.centerPoint.getX() - p.getX()) * (this.centerPoint.getX() - p.getX())
				+ (this.centerPoint.getY() - p.getY()) * (this.centerPoint.getY() - p.getY())
				+ (this.centerPoint.getZ() - p.getZ()) * (this.centerPoint.getZ() - p.getZ());
	}

	public Point3D getCenterPoint() {
		return new Point3D(this.centerPoint); // Return a copy of the center point of the cube
	}

	// The cube paints itself:
	public synchronized void paint(final Graphics g, final HMatrix3D pxf, final Point3D vp, final float sfFocalLength,
			final int width, final int height) {
		int i;
		int j;
		Polygon Pgon;
		int x[]; // For passing x coordinates to the polygon
		int y[]; // For passing y coordinates to the polygon
		x = new int[5];
		y = new int[5];
		Point3D TempPoint; // Temporary point
		float sfAdjust;
		float sfDSQi;
		float sfDSQj;

		// Sort the faces on distance from the viewpoint:
		// This fixed loop bubble sort is good enough for a simple cube.
		for (i = 1; i <= 5; i++) {
			for (j = i + 1; j <= 6; j++) {
				// Put most distant first:
				// Distance from the face to the viewpoint
				sfDSQi = this.F[i].getDSquared(vp);
				sfDSQj = this.F[j].getDSquared(vp);
				if (sfDSQj > sfDSQi) {
					this.F[0] = this.F[j]; // Use the zeroeth face for swap space
					this.F[j] = this.F[i];
					this.F[i] = this.F[0];
				}
			}
		}
		// Render the faces to the applet panel:
		for (i = 1; i <= 6; i++) // For each face
		{
			// Establish a front clipping plane so we don't render faces behind us:
			TempPoint = this.F[i].getPoint(0); // Center point of the face
			TempPoint.transform(pxf);
			if (TempPoint.getZ() < -100) {
				for (j = 1; j <= 4; j++) { // For each point of the face
					TempPoint = this.F[i].getPoint(j);
					TempPoint.transform(pxf);
					// Adjust x and y for perspective view
					sfAdjust = sfFocalLength / TempPoint.getZ();
					x[j - 1] = width / 2 + ((int) (TempPoint.getX() * sfAdjust));
					y[j - 1] = height / 2 - ((int) (TempPoint.getY() * sfAdjust));
				}
				Pgon = new Polygon(x, y, 4);
				g.setColor(getBlockColor());
				g.fillPolygon(Pgon);
				g.setColor(Color.black);
				g.drawPolygon(Pgon);
			}
		}

		TempPoint = new Point3D(this.frontFaceCenter);
		TempPoint.transform(pxf);
		sfAdjust = sfFocalLength / TempPoint.getZ();
		// Adjust x and y for perspective view
		int xs = width / 2 + ((int) (TempPoint.getX() * sfAdjust));
		int ys = height / 2 - ((int) (TempPoint.getY() * sfAdjust));
		// and center
		final String text = "" + this.blockNumber;
		final FontMetrics fm = g.getFontMetrics();
		xs -= fm.stringWidth(text) / 2;
		ys += fm.getAscent() / 2;
		g.setFont(new Font("default", Font.BOLD, 12));
		g.drawString(text, xs, ys);
	}

	public Color getBlockColor() {
		if (this.selected) {
			return this.BlockColor.darker();
		} else {
			return this.BlockColor;
		}

	}

	public boolean topBlock() {
		return this.bTopBlock;
	}

	public void setTopBlock(final boolean v) {
		this.bTopBlock = v;
	}

	/**
	 * get the block that this block rests on.
	 *
	 * @return
	 */
	public Cube3D getOnBlock() {
		return this.iOnBlock;
	}

	public void setOnBlock(final Cube3D i) {
		this.iOnBlock = i;
	}

	/**
	 * set the block to selected or deselected. This changes the color to a bit
	 * darker shade.
	 *
	 * @param b true to select, false to deselect.
	 */
	public void setSelected(final boolean b) {
		this.selected = b;

	}

	/**
	 * get the block number
	 *
	 * @return block number
	 */
	public int getNumber() {
		return this.blockNumber;
	}
}

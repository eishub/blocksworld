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
	private Square3D F[]; // Face array
	private Point3D centerPoint; // For distance calculations
	private Color BlockColor = Color.getHSBColor((float) Math.random(),
			(float) Math.random(), 1f); // The block knows its own color
	private boolean bTopBlock; // Not any blocks on top of it, true by
								// default
	private Cube3D iOnBlock; // Index of the block this one rests on, null by
								// default
	private boolean selected;
	private int blockNumber;
	private Point3D frontFaceCenter;

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + blockNumber;
		return result;
	}

	/**
	 * Blocks are equal if their BLOCK NUMBER is equal. The rest is irrelevant.
	 */
	@Override
	public boolean equals(Object obj) {
		if (this == obj)
			return true;
		if (obj == null)
			return false;
		if (getClass() != obj.getClass())
			return false;
		Cube3D other = (Cube3D) obj;
		if (blockNumber != other.blockNumber)
			return false;
		return true;
	}

	/*
	 * 
	 * Construct block having number i.
	 */
	public Cube3D(int i) {
		if (i <= 0) {
			throw new IllegalArgumentException("illegal block number " + i);
		}
		blockNumber = i;
		reset();
	}

	/**
	 * put point to initial position (in origin, 50 below table). Erase all
	 * properties, so block is blank and on table after reset.
	 */
	public synchronized void reset() {
		bTopBlock = true;
		iOnBlock = null;

		Point3D V[];
		centerPoint = new Point3D(0, -50, 0); // 50 units above origin, -Y
												// is up

		frontFaceCenter = new Point3D(0, -50, 50); // center of front face.

		V = new Point3D[5];
		F = new Square3D[7];

		V[1] = new Point3D(50, -100, 50); // Front face
		V[2] = new Point3D(-50, -100, 50);
		V[3] = new Point3D(-50, 0, 50);
		V[4] = new Point3D(50, 0, 50);
		F[1] = new Square3D(V);

		V[1] = new Point3D(50, -100, -50); // Right face
		V[2] = new Point3D(50, -100, 50);
		V[3] = new Point3D(50, 0, 50);
		V[4] = new Point3D(50, 0, -50);
		F[2] = new Square3D(V);

		V[1] = new Point3D(-50, -100, -50); // Back face
		V[2] = new Point3D(50, -100, -50);
		V[3] = new Point3D(50, 0, -50);
		V[4] = new Point3D(-50, 0, -50);
		F[3] = new Square3D(V);

		V[1] = new Point3D(-50, -100, 50); // Left face
		V[2] = new Point3D(-50, -100, -50);
		V[3] = new Point3D(-50, 0, -50);
		V[4] = new Point3D(-50, 0, 50);
		F[4] = new Square3D(V);

		V[1] = new Point3D(50, -100, -50); // Top face
		V[2] = new Point3D(-50, -100, -50);
		V[3] = new Point3D(-50, -100, 50);
		V[4] = new Point3D(50, -100, 50);
		F[5] = new Square3D(V);

		V[1] = new Point3D(50, 0, -50); // Top face
		V[2] = new Point3D(50, 0, 50);
		V[3] = new Point3D(-50, 0, 50);
		V[4] = new Point3D(-50, 0, -50);
		F[6] = new Square3D(V);
	}

	public Cube3D(Cube3D c) // Copyconstructor
	{
		int i;
		bTopBlock = c.bTopBlock;
		iOnBlock = c.iOnBlock;
		BlockColor = c.BlockColor;
		centerPoint = new Point3D(c.centerPoint);
		frontFaceCenter = new Point3D(c.frontFaceCenter);
		F = new Square3D[7];
		for (i = 1; i <= 6; i++) {
			F[i] = new Square3D(c.F[i]);
		}
	}

	public synchronized void transform(HMatrix3D m) // transform the cube
	{
		centerPoint.transform(m);
		frontFaceCenter.transform(m);
		int i = 0;
		for (i = 1; i <= 6; i++) {
			F[i].transform(m);
		}
	}

	// Get the square of the distance from the center of the cube to some
	// point:
	public float getDSquared(Point3D p) {
		float sfDSquared;
		sfDSquared = (centerPoint.getX() - p.getX())
				* (centerPoint.getX() - p.getX())
				+ (centerPoint.getY() - p.getY())
				* (centerPoint.getY() - p.getY())
				+ (centerPoint.getZ() - p.getZ())
				* (centerPoint.getZ() - p.getZ());
		return sfDSquared; // Distance squared from the cube to the point
	}

	public Point3D getCenterPoint() {
		return new Point3D(centerPoint); // Return a copy of the center
											// point of the cube
	}

	// The cube paints itself:
	public synchronized void paint(Graphics g, HMatrix3D pxf, Point3D vp,
			float sfFocalLength, int width, int height) {
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
		for (i = 1; i <= 5; i++) // This fixed loop bubble sort is good
									// enough
		{ // for a simple cube. More general applications
			for (j = i + 1; j <= 6; j++) // will use a faster sort
											// algorithm.
			{
				// Put most distant first:
				sfDSQi = F[i].getDSquared(vp); // Distance from the face to
												// the viewpoint
				sfDSQj = F[j].getDSquared(vp);
				if (sfDSQj > sfDSQi) {
					F[0] = F[j]; // Use the zeroeth face for swap space
					F[j] = F[i];
					F[i] = F[0];
				}
			}
		}
		// Render the faces to the applet panel:
		for (i = 1; i <= 6; i++) // For each face
		{
			// Establish a front clipping plane so we don't render faces
			// behind us:
			TempPoint = F[i].getPoint(0); // Center point of the face
			TempPoint.transform(pxf);
			if (TempPoint.getZ() < -100) {
				for (j = 1; j <= 4; j++) // For each point of the face
				{
					TempPoint = F[i].getPoint(j);
					TempPoint.transform(pxf);
					sfAdjust = sfFocalLength / TempPoint.getZ(); // Adjust x
																	// and y
																	// for
																	// perspective
																	// view
					x[j - 1] = width / 2
							+ ((int) (TempPoint.getX() * sfAdjust));
					y[j - 1] = height / 2
							- ((int) (TempPoint.getY() * sfAdjust));
				}
				Pgon = new Polygon(x, y, 4);
				g.setColor(getBlockColor());
				g.fillPolygon(Pgon);
				g.setColor(Color.black);
				g.drawPolygon(Pgon);
			}
		}

		TempPoint = new Point3D(frontFaceCenter);
		TempPoint.transform(pxf);
		sfAdjust = sfFocalLength / TempPoint.getZ();
		/*
		 * Adjust x and y for perspective view
		 */
		int xs = width / 2 + ((int) (TempPoint.getX() * sfAdjust));
		int ys = height / 2 - ((int) (TempPoint.getY() * sfAdjust));
		// and center
		String text = "" + blockNumber;
		FontMetrics fm = g.getFontMetrics();
		xs -= fm.stringWidth(text) / 2;
		ys += fm.getAscent() / 2;
		g.setFont(new Font("default", Font.BOLD, 12));
		g.drawString(text, xs, ys);

	} // End of Cube3D paint()

	public Color getBlockColor() // Accessor
	{
		if (selected) {
			return BlockColor.darker();
		}
		return BlockColor;

	}

	public boolean topBlock() // Accessor
	{
		return bTopBlock;
	}

	public void setTopBlock(boolean v) // Mutator
	{
		bTopBlock = v;
	}

	/**
	 * get the block that this block rests on.
	 * 
	 * @return
	 */
	public Cube3D getOnBlock() // Accessor
	{
		return iOnBlock;
	}

	public void setOnBlock(Cube3D i) // Mutator
	{
		iOnBlock = i;
	}

	/**
	 * set the block to selected or deselected. This changes the color to a bit
	 * darker shade.
	 * 
	 * @param b
	 *            true to select, false to deselect.
	 */
	public void setSelected(boolean b) {
		selected = b;

	}

	/**
	 * get the block number
	 * 
	 * @return block number
	 */
	public int getNumber() {
		return blockNumber;
	}

}

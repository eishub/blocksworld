package blocksworld;

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
/**
 * A square in 3D is built up out of four corner points going around
 * counterclockwise as you look at the square from the outside of a solid it
 * might be a face of. A square can have any position and orientation in
 * 3-space. The center point of the square is used to compute average distance
 * for the perspective rendering painter's algorithm and is itself computed as
 * the average of its corner points.
 */
public class Square3D {
	private Point3D V[]; // Vertex array, zeroeth element is the average

	public Square3D() // Default constructor
	{
		V = new Point3D[5];
		V[0] = new Point3D(0, 0, 0); // Centered at the origin
		V[1] = new Point3D(50, 50, 0);
		V[2] = new Point3D(-50, 50, 0);
		V[3] = new Point3D(-50, -50, 0);
		V[4] = new Point3D(50, -50, 0);
	}

	public Square3D(Point3D a, Point3D b, Point3D c, Point3D d) // 4 point
																// constructor
	{
		float x = (a.getX() + b.getX() + c.getX() + d.getX()) / 4; // Average
																	// x
		float y = (a.getY() + b.getY() + c.getY() + d.getY()) / 4; // Average
																	// y
		float z = (a.getZ() + b.getZ() + c.getZ() + d.getZ()) / 4; // Average
																	// z
		V = new Point3D[5];
		V[0] = new Point3D(x, y, z);
		V[1] = new Point3D(a);
		V[2] = new Point3D(b);
		V[3] = new Point3D(c);
		V[4] = new Point3D(d);
	}

	public Square3D(Point3D a[]) // Point array constructor
	{
		float x = (a[1].getX() + a[2].getX() + a[3].getX() + a[4].getX()) / 4; // Average
																				// x
		float y = (a[1].getY() + a[2].getY() + a[3].getY() + a[4].getY()) / 4; // Average
																				// y
		float z = (a[1].getZ() + a[2].getZ() + a[3].getZ() + a[4].getZ()) / 4; // Average
																				// z
		V = new Point3D[5];
		V[0] = new Point3D(x, y, z);
		V[1] = new Point3D(a[1]);
		V[2] = new Point3D(a[2]);
		V[3] = new Point3D(a[3]);
		V[4] = new Point3D(a[4]);
	}

	public Square3D(Square3D s) // Copy constructor
	{
		V = new Point3D[5];
		V[0] = new Point3D(s.V[0]);
		V[1] = new Point3D(s.V[1]);
		V[2] = new Point3D(s.V[2]);
		V[3] = new Point3D(s.V[3]);
		V[4] = new Point3D(s.V[4]);
	}

	public Point3D getPoint(int i) {
		return new Point3D(V[i]); // Makes a copy of the point
	}

	public void transform(HMatrix3D m) // Transform the square
	{
		int i = 0;
		for (i = 0; i <= 4; i++) {
			V[i].transform(m); // Transform all the points in the square
		}
	}

	// Get the distance squared to some point:
	public float getDSquared(Point3D p) {
		float sfDSquared;
		sfDSquared = (V[0].getX() - p.getX()) * (V[0].getX() - p.getX())
				+ (V[0].getY() - p.getY()) * (V[0].getY() - p.getY())
				+ (V[0].getZ() - p.getZ()) * (V[0].getZ() - p.getZ());
		return sfDSquared; // Distance squared from the square to the point
	}

} // End of class Square3D
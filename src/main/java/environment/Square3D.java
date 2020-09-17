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
/**
 * A square in 3D is built up out of four corner points going around
 * counterclockwise as you look at the square from the outside of a solid it
 * might be a face of. A square can have any position and orientation in
 * 3-space. The center point of the square is used to compute average distance
 * for the perspective rendering painter's algorithm and is itself computed as
 * the average of its corner points.
 */
public class Square3D {
	private final Point3D V[]; // Vertex array, zeroeth element is the average

	public Square3D() {
		this.V = new Point3D[5];
		this.V[0] = new Point3D(0, 0, 0); // Centered at the origin
		this.V[1] = new Point3D(50, 50, 0);
		this.V[2] = new Point3D(-50, 50, 0);
		this.V[3] = new Point3D(-50, -50, 0);
		this.V[4] = new Point3D(50, -50, 0);
	}

	public Square3D(final Point3D a, final Point3D b, final Point3D c, final Point3D d) {
		// Average x
		final float x = (a.getX() + b.getX() + c.getX() + d.getX()) / 4;
		// Average y
		final float y = (a.getY() + b.getY() + c.getY() + d.getY()) / 4;
		// Average z
		final float z = (a.getZ() + b.getZ() + c.getZ() + d.getZ()) / 4;

		this.V = new Point3D[5];
		this.V[0] = new Point3D(x, y, z);
		this.V[1] = new Point3D(a);
		this.V[2] = new Point3D(b);
		this.V[3] = new Point3D(c);
		this.V[4] = new Point3D(d);
	}

	public Square3D(final Point3D a[]) {
		// Average x
		final float x = (a[1].getX() + a[2].getX() + a[3].getX() + a[4].getX()) / 4;
		// Average y
		final float y = (a[1].getY() + a[2].getY() + a[3].getY() + a[4].getY()) / 4;
		// Average z
		final float z = (a[1].getZ() + a[2].getZ() + a[3].getZ() + a[4].getZ()) / 4;

		this.V = new Point3D[5];
		this.V[0] = new Point3D(x, y, z);
		this.V[1] = new Point3D(a[1]);
		this.V[2] = new Point3D(a[2]);
		this.V[3] = new Point3D(a[3]);
		this.V[4] = new Point3D(a[4]);
	}

	public Square3D(final Square3D s) {
		this.V = new Point3D[5];
		this.V[0] = new Point3D(s.V[0]);
		this.V[1] = new Point3D(s.V[1]);
		this.V[2] = new Point3D(s.V[2]);
		this.V[3] = new Point3D(s.V[3]);
		this.V[4] = new Point3D(s.V[4]);
	}

	public Point3D getPoint(final int i) {
		return new Point3D(this.V[i]); // Makes a copy of the point
	}

	public void transform(final HMatrix3D m) {
		for (int i = 0; i <= 4; i++) {
			this.V[i].transform(m); // Transform all the points in the square
		}
	}

	public float getDSquared(final Point3D p) {
		return (this.V[0].getX() - p.getX()) * (this.V[0].getX() - p.getX())
				+ (this.V[0].getY() - p.getY()) * (this.V[0].getY() - p.getY())
				+ (this.V[0].getZ() - p.getZ()) * (this.V[0].getZ() - p.getZ());
	}
}
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
 * This is the atomic geometric object, a 3D point in single precision floating
 * point coordinates:
 */
public class Point3D {
	private float x;
	private float y;
	private float z;

	public Point3D() {
		this.x = 0;
		this.y = 0;
		this.z = 0;
	}

	public Point3D(final Point3D p) {
		this.x = p.x;
		this.y = p.y;
		this.z = p.z;
	}

	public Point3D(final float a, final float b, final float c) {
		this.x = a;
		this.y = b;
		this.z = c;
	}

	public void setX(final float a) {
		this.x = a;
	}

	public void setY(final float a) {
		this.y = a;
	}

	public void setZ(final float a) {
		this.z = a;
	}

	public float getX() {
		return this.x;
	}

	public float getY() {
		return this.y;
	}

	public float getZ() {
		return this.z;
	}

	public void transform(final HMatrix3D m) {
		// Make copies for correct computation
		final float a = this.x;
		final float b = this.y;
		final float c = this.z;
		final float d = 1;

		// Multiply the point vector by the transformation matrix:
		this.x = m.getElement(1, 1) * a + m.getElement(1, 2) * b + m.getElement(1, 3) * c + m.getElement(1, 4) * d;
		this.y = m.getElement(2, 1) * a + m.getElement(2, 2) * b + m.getElement(2, 3) * c + m.getElement(2, 4) * d;
		this.z = m.getElement(3, 1) * a + m.getElement(3, 2) * b + m.getElement(3, 3) * c + m.getElement(3, 4);
	}
}
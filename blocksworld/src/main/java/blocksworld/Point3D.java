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
 * This is the atomic geometric object, a 3D point in single precision floating
 * point coordinates:
 */
public class Point3D {
	private float x;
	private float y;
	private float z;

	public Point3D() // Default constructor
	{
		x = 0;
		y = 0;
		z = 0;
	}

	public Point3D(Point3D p) // Copy constructor
	{
		x = p.x;
		y = p.y;
		z = p.z;
	}

	public Point3D(float a, float b, float c) // General constructor
	{
		x = a;
		y = b;
		z = c;
	}

	public void setX(float a) // Mutator
	{
		x = a;
	}

	public void setY(float a) // Mutator
	{
		y = a;
	}

	public void setZ(float a) // Mutator
	{
		z = a;
	}

	public float getX() // Accessor
	{
		return x;
	}

	public float getY() // Accessor
	{
		return y;
	}

	public float getZ() // Accessor
	{
		return z;
	}

	public void transform(HMatrix3D m) // Transform the point
	{
		float a = x; // Make copies for correct computation
		float b = y;
		float c = z;
		float d = 1;

		// Multiply the point vector by the transformation matrix:
		x = m.getElement(1, 1) * a + m.getElement(1, 2) * b
				+ m.getElement(1, 3) * c + m.getElement(1, 4) * d;
		y = m.getElement(2, 1) * a + m.getElement(2, 2) * b
				+ m.getElement(2, 3) * c + m.getElement(2, 4) * d;
		z = m.getElement(3, 1) * a + m.getElement(3, 2) * b
				+ m.getElement(3, 3) * c + m.getElement(3, 4);
	}

} // End of class Point3D
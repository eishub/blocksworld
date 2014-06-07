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
/** Translation matrix class */
public class THMatrix3D extends HMatrix3D {
	public THMatrix3D(float x, float y, float z) // Three-parameter
													// constructor
	{
		super();
		super.m[1][4] = (x);
		super.m[2][4] = (y);
		super.m[3][4] = (z);
	}

	public THMatrix3D(Point3D p) // Point constructor
	{
		super();
		super.m[1][4] = (p.getX());
		super.m[2][4] = (p.getY());
		super.m[3][4] = (p.getZ());
	}
}

class RHMatrix3DX extends HMatrix3D // Rotation matrix class
{
	public RHMatrix3DX(float Theta) // Constructor, Theta in degrees
	{
		super();
		super.m[2][2] = (float) Math.cos(Theta * Math.PI / 180);
		super.m[2][3] = (float) -Math.sin(Theta * Math.PI / 180);
		super.m[3][2] = (float) Math.sin(Theta * Math.PI / 180);
		super.m[3][3] = (float) Math.cos(Theta * Math.PI / 180);
	}
}

class RHMatrix3DY extends HMatrix3D // Rotation matrix class
{
	public RHMatrix3DY(float Theta) // Constructor, Theta in degrees
	{
		super();
		super.m[1][1] = (float) Math.cos(Theta * Math.PI / 180);
		super.m[1][3] = (float) Math.sin(Theta * Math.PI / 180);
		super.m[3][1] = (float) -Math.sin(Theta * Math.PI / 180);
		super.m[3][3] = (float) Math.cos(Theta * Math.PI / 180);
	}
}

class RHMatrix3DZ extends HMatrix3D // Rotation matrix class
{
	public RHMatrix3DZ(float Theta) // Constructor, Theta in degrees
	{
		super();
		super.m[1][1] = (float) Math.cos(Theta * Math.PI / 180);
		super.m[1][2] = (float) -Math.sin(Theta * Math.PI / 180);
		super.m[2][1] = (float) Math.sin(Theta * Math.PI / 180);
		super.m[2][2] = (float) Math.cos(Theta * Math.PI / 180);
	}
}

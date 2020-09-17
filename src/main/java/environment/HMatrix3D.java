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
 * Homogeneous matrix class. Having a 3D homogeneous matrix object is very
 * convenient. The matrix class can also multiply two matrices.
 */
public class HMatrix3D {
	protected float m[][]; // Matrix elements

	public HMatrix3D() {
		int i = 0;
		int j = 0;
		this.m = new float[5][5]; // 1-based indexing
		for (i = 1; i <= 4; i++) {// rows
			for (j = 1; j <= 4; j++) {// columns
				if (i == j) {
					this.m[i][j] = 1;
				} else {
					this.m[i][j] = 0; // Identity matrix by default
				}
			}
		}
	}

	public void setElement(final int i, final int j, final float a) {
		this.m[i][j] = a;
	}

	public float getElement(final int i, final int j) {
		return this.m[i][j];
	}

	public HMatrix3D multiply(final HMatrix3D A, final HMatrix3D B) { // Post-multiply by another matrix
		final HMatrix3D C = new HMatrix3D(); // New matrix to return
		int i = 0;
		int j = 0;
		int k = 0;
		for (i = 1; i <= 4; i++) { // rows
			for (j = 1; j <= 4; j++) { // columns
				C.m[i][j] = 0;
				for (k = 1; k <= 4; k++) {
					C.m[i][j] += A.m[i][k] * B.m[k][j];
				}
			}
		}
		return C;
	}
}
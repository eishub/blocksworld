package environment;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Set;

import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;

/**
 * the blocks world model containing the abstract info on what is where in the
 * world, and functions to change the world.
 * <p>
 * Original world was using an <x,z> area of [-500,500]x[-500,500] to place the
 * blocks. Y is up
 * <p>
 */
public class BlocksWorldModel {
	private final static int X0 = -500;
	private final static int Z0 = 500;
	private final static int D = 150;
	private final static int PER_ROW = 8;

	/**
	 * All blocks, indexed by their number.
	 */
	private Map<Integer, Cube3D> blocks;
	private final Set<ChangeListener> listeners = new HashSet<>();

	/**
	 * make world with n blocks. * We have a virtual grid on which the stacks are
	 * placed, starting at {@value #X0}, {@value #Z0} with grid spacing {@value #D}.
	 * This grid replaces the old random scattering method. THe grid positions are
	 * numbered 0,1,2,... from front left to rear right.
	 * <p>
	 * The X and Z coordinates of the front left point. D is the distance between
	 * blocks (in the grid). Each row contains {@value #PER_ROW} blocks.
	 *
	 * @param startlist is initial config. see {@link #reset(List)}.
	 * @throws IllegalArgumentException if parameter is negative or 0.
	 */
	public BlocksWorldModel(final List<Integer> startlist) {
		reset(startlist);
	}

	/**
	 * reset the model. New number of blocks. All blocks are made new and placed on
	 * table. Observers are notified.
	 *
	 * @param n
	 */
	private void reset(final int n) {
		if (n <= 0) {
			throw new IllegalArgumentException("size must be positive but got " + n);
		}
		this.blocks = new HashMap<>();
		for (int i = 1; i <= n; i++) {
			this.blocks.put(i, new Cube3D(i));
		}
		allBlocksToTable();
		notifyChange();
	}

	/**
	 *
	 * The input list is in the form of a list of integers. The blocks are to be
	 * thought of as numbered from 1 to N, with the table as number 0. Each state is
	 * specified by giving each block in sequence the number of the block it is on.
	 * A notional size of zero terminates the output. For example, the state of 6
	 * blocks<br>
	 *
	 * 2 <br>
	 * 5 3 <br>
	 * 4 1 6<br>
	 * ------<br>
	 * <br>
	 * is represented by the integers<br>
	 * 0 5 1 0 4 0<br>
	 * meaning there are 6 blocks; 1 is on the table, 2 on 5, 3 on 1, 4 on the
	 * table, 5 on 4 and 6 on the table. <br>
	 * This format matches the format used by Slaney.
	 *
	 * @param initialpositions
	 */
	public void reset(final List<Integer> initialpositions) {
		// note, both reset and all move() calls will notify renderer..
		checkList(initialpositions);
		reset(initialpositions.size());
		/**
		 * list with on-top-of numbers. Copy of initialpositions.
		 * <p>
		 * A well placed block is marked in the list by setting its value to 0.
		 */
		final List<Integer> placedBlocks = new ArrayList<>(initialpositions);

		/*
		 * we iterate through the blocks and try to place them, until they are all
		 * placed. after at most size+1 iterations all blocks must have been placed. We
		 * keep track of progress to catch cycle problems which would result in infinite
		 * loop if not detected.
		 */
		boolean blocksRemaining = true;
		while (blocksRemaining) {
			blocksRemaining = false;
			boolean blocksMoved = false;
			// try place block n
			for (int n = 1; n <= placedBlocks.size(); n++) {
				final int on = placedBlocks.get(n - 1);
				if (on >= 1) { // on another block?
					blocksRemaining = true;
					if (placedBlocks.get(on - 1) == 0) {
						// the block it has to be on is in position, go ahead
						move(this.blocks.get(n), this.blocks.get(on));
						placedBlocks.set(n - 1, 0);
						blocksMoved = true;
					}
				}
			}
			if (blocksRemaining && !blocksMoved) {
				throw new IllegalArgumentException("Failed to place blocks in initial position, there must be a loop");
			}
		}
	}

	/**
	 * Check that all numbers in the list are betweeen 0 and size(list). We do not
	 * check for loops.
	 *
	 * @param initialpositions
	 * @throws IllegalArgumentException if some block is numbered incorrectly
	 */
	private void checkList(final List<Integer> initialpositions) {
		final int size = initialpositions.size();
		for (int n = 0; n < size; n++) {
			final int pos = initialpositions.get(n);
			if (pos < 0) {
				throw new IllegalArgumentException("block " + (n + 1) + " is supposed to be on negative block " + pos);
			}
			if (pos > size) {
				throw new IllegalArgumentException(
						"block " + (n + 1) + " is supposed to be on nonexistent block " + pos);
			}
		}
	}

	/**
	 * get block n
	 *
	 * @param n
	 * @return
	 */
	public Cube3D get(final int n) {
		return this.blocks.get(n);
	}

	/**
	 * get all blocks in the world.
	 *
	 * @return all blocks in the world
	 */
	public Collection<Cube3D> getBlocks() {
		return this.blocks.values();
	}

	/**
	 * Put all blocks to the table.
	 */
	public void allBlocksToTable() {
		for (final Cube3D block : this.blocks.values()) { // Create the random blocks
			block.reset();
			final float sfTheta = (float) Math.random() * 10;
			final RHMatrix3DY hmRMY = new RHMatrix3DY(sfTheta);
			block.transform(hmRMY); // Rotate the block about its center
									// vertical axis
			block.transform(new THMatrix3D(getTablePosition(block.getNumber())));
		}
		notifyChange();
	}

	/**
	 * Move src block on top of dest block or to table.
	 *
	 * @param source
	 * @param dest   cube. Use null to move block to table.
	 */
	public void move(final Cube3D iSourceBlock, final Cube3D iDestBlock) {
		Point3D TempPoint;
		THMatrix3D TM = null; // Translation matrix for making the block jump

		if (!iSourceBlock.topBlock()) {
			throw new IllegalArgumentException("block is not on top");
		}

		if (iDestBlock == null) {
			// move to floor
			final Point3D freetablepos = getTablePosition(getFreeStackNumber());
			if (iSourceBlock.getOnBlock() != null) {
				// The block it was sitting on is now free to move:
				iSourceBlock.getOnBlock().setTopBlock(true);
			}
			iSourceBlock.reset();
			iSourceBlock.transform(new THMatrix3D(freetablepos));
		} else {
			if (!iDestBlock.topBlock()) {
				throw new IllegalArgumentException("target is not on top");
			}
			// move on top of another block. This part is original code....
			// Compute the translation transform for stacking the block:
			// -Y is up
			TempPoint = new Point3D(iDestBlock.getCenterPoint().getX() - iSourceBlock.getCenterPoint().getX(),
					iDestBlock.getCenterPoint().getY() - iSourceBlock.getCenterPoint().getY() - 100,
					iDestBlock.getCenterPoint().getZ() - iSourceBlock.getCenterPoint().getZ());
			TM = new THMatrix3D(TempPoint);
			iSourceBlock.transform(TM);
			// The block it moves to now can't move
			iDestBlock.setTopBlock(false);

			if (iSourceBlock.getOnBlock() != null) {
				// The block it was sitting on is now free to move:
				iSourceBlock.getOnBlock().setTopBlock(true);
			}

			// Block knows what block it sits on so it can free it later if it moves
			iSourceBlock.setOnBlock(iDestBlock);
		}
		notifyChange();
	}

	/**
	 * notify all listeners that the model has changed.
	 */
	private void notifyChange() {
		for (final ChangeListener l : this.listeners) {
			l.stateChanged(new ChangeEvent(this));
		}

	}

	/**
	 * This function gets you the stack number for a cube.
	 *
	 * @param cube
	 * @return stack number for given cube. Stack 0 is front left.
	 */
	public int getStackNumber(final Cube3D cube) {
		final Point3D center = cube.getCenterPoint();
		final int nx = (int) (((center.getX() - X0) + 1) / D); // +1 for rounding
		final int nz = (int) ((Z0 - center.getZ() + 1) / D);
		if (nx < 0 || nz < 0) {
			throw new IllegalArgumentException("cube " + cube + " is outside the grid at (" + nx + "," + nz + ")");
		}
		return nz * PER_ROW + nx;
	}

	/**
	 * Get the table position for stack number n.
	 *
	 * @param n the stack number. 0=first position.
	 * @return 3d position of table position n.
	 */
	public Point3D getTablePosition(final int n) {
		final int nx = n % PER_ROW;
		final int nz = n / PER_ROW;
		return new Point3D(X0 + nx * D, 0, Z0 - nz * D);
	}

	/**
	 * find a free stack position on the table. Needed if blocks have to be moved to
	 * the table
	 *
	 * @return stack number
	 */
	public int getFreeStackNumber() {
		final List<Boolean> free = new LinkedList<>();

		/*
		 * all places are free until we find a stack there. we have a list one longer
		 * than the number of blocks, because occasionally (see last line of this
		 * function) we return the overflow position.
		 */
		for (int n = 0; n <= this.blocks.size(); n++) {
			free.add(true);
		}

		// check all stacks
		for (final Cube3D block : this.blocks.values()) {
			free.set(getStackNumber(block), false);
		}

		// finally, find first free stack
		for (int n = 0; n < this.blocks.size(); n++) {
			if (free.get(n)) {
				return n;
			}
		}
		// there is always a place after the last block, if we always
		// use the first free position if there is one.
		return this.blocks.size();
	}

	/**
	 * add listener. Listener will immediately be called with stateChanged event.
	 *
	 * @param l is listener to add.
	 */
	public void addListener(final ChangeListener l) {
		this.listeners.add(l);
		l.stateChanged(new ChangeEvent(this));
	}

	public void removeListener(final ChangeListener l) {
		this.listeners.remove(l);
	}
}
package eisinterface;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import eis.eis2java.annotation.AsAction;
import eis.eis2java.annotation.AsPercept;
import eis.exceptions.ActException;
import environment.BlocksWorldModel;
import environment.Cube3D;

/**
 * this is the EIS entity that is plugged into the EIS2Java environment.
 */
public class Gripper {
	private final BlocksWorldModel world;

	public Gripper(final BlocksWorldModel model) {
		this.world = model;
	}

	/**
	 * Returns the on/2 percepts.
	 *
	 * @return The percepts that represent which block is on top of another object.
	 */
	@AsPercept(name = "on", multiplePercepts = true, multipleArguments = true)
	public List<List<String>> on() {
		final Collection<Cube3D> blocks = this.world.getBlocks();
		final List<List<String>> onlist = new ArrayList<>(blocks.size());
		for (final Cube3D block : blocks) {
			final List<String> params = new ArrayList<>(2);
			params.add(BWEnvironment.blockName(block));
			params.add(BWEnvironment.blockName(block.getOnBlock()));
			onlist.add(params);
		}

		return onlist;
	}

	/**
	 * Get Cube that has given block name.
	 *
	 * @param name "table" -> null, "bN" -> block N
	 * @return the block or null
	 */
	private Cube3D blockWithNumber(final String name) {
		if (name.equals("table")) {
			return null;
		}
		if (!name.startsWith("b")) {
			throw new IllegalArgumentException("Unknown block " + name);
		}

		final Integer blocknr = Integer.parseInt(name.substring(1));
		final Cube3D block = this.world.get(blocknr);
		if (block == null) {
			throw new IllegalArgumentException("Unknown block " + name);
		}

		return block;
	}

	/**
	 * move block on top of target block. Block 1 is the first block. use
	 * targetblock=0 to move to table.
	 *
	 * @param block       name of block to move.
	 * @param targetblock name of another block to move the block on, or table.
	 */
	@AsAction(name = "move")
	public void move(final String blockname, final String targetblockname) throws ActException {
		final Cube3D block = blockWithNumber(blockname);
		final Cube3D targetblock = blockWithNumber(targetblockname);

		if (block == null) {
			throw new IllegalArgumentException("table can not be moved");
		}

		this.world.move(block, targetblock);
	}
}
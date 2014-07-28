package eisinterface;

import java.util.ArrayList;
import java.util.List;

import eis.eis2java.annotation.AsAction;
import eis.eis2java.annotation.AsPercept;
import eis.exceptions.ActException;
import eisinterface.BWEnvironment;
import environment.BlocksWorldModel;
import environment.Cube3D;

/**
 * this is the EIS entity that is plugged into the EIS2Java environment.
 * 
 * @author W.Pasman modified 22jan14 using "table" and "bX" where X is number
 *         instead of numbers 0..<nblocks>.
 * */

public class Gripper {
	private BlocksWorldModel world;

	public Gripper(BlocksWorldModel model) {
		world = model;
	}

	/**
	 * Returns the on/2 percepts.
	 * 
	 * @return The percepts that represent which block is on top of another object.
	 */
	@AsPercept(name = "on", multiplePercepts = true, multipleArguments = true)
	public List<List<String>> on() {

		List<List<String>> onlist = new ArrayList<List<String>>();
		for (Cube3D block : world.getBlocks()) {
			List<String> params = new ArrayList<String>();
			params.add(BWEnvironment.blockName(block));
			params.add(BWEnvironment.blockName(block.getOnBlock()));
			onlist.add(params);
		}
		return onlist;
	}

	/**
	 * Get Cube that has given block name.
	 * 
	 * @param name
	 *            "table" -> null, "bN" -> block N
	 * @return the block or null
	 */
	private Cube3D blockWithNumber(String name) {
		if (name.equals("table")) {
			return null;
		}
		if (!name.startsWith("b")) {
			throw new IllegalArgumentException("Unknown block " + name);
		}
		Integer blocknr = new Integer(name.substring(1));
		Cube3D block = world.get(blocknr);
		if (block == null) {
			throw new IllegalArgumentException("Unknown block " + name);
		}
		return block;
	}

	/**
	 * move block on top of target block. Block 1 is the first block. use
	 * targetblock=0 to move to table.
	 * 
	 * @param block
	 *            name of block to move.
	 * @param targetblock
	 *            name of another block to move the block on, or table.
	 */
	@AsAction(name = "move")
	public void move(String blockname, String targetblockname)
			throws ActException {

		Cube3D block = blockWithNumber(blockname);
		Cube3D targetblock = blockWithNumber(targetblockname);

		if (block.equals("table")) {
			throw new IllegalArgumentException("table can not be moved");
		}

		world.move(block, targetblock);
	}
}
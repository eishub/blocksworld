package eisinterface;

import java.io.File;
import java.io.FileNotFoundException;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Scanner;

import eis.eis2java.environment.AbstractEnvironment;
import eis.exceptions.EntityException;
import eis.exceptions.ManagementException;
import eis.iilang.Action;
import eis.iilang.EnvironmentState;
import eis.iilang.Identifier;
import eis.iilang.Numeral;
import eis.iilang.Parameter;
import eis.iilang.ParameterList;
import environment.BlocksWorldModel;
import environment.BlocksWorldPainter;
import environment.Cube3D;

/**
 * EIS2Java layer for the 3D Blocks World environment.
 *
 * Creates the AbstractEnvironemnt and hooks in the entities.
 */
@SuppressWarnings("serial")
public class BWEnvironment extends AbstractEnvironment {
	private BlocksWorldModel model;
	private BlocksWorldPainter gui;

	public BWEnvironment() {
	}

	@Override
	protected boolean isSupportedByEnvironment(final Action action) {
		return (action.getName().equals("move") && action.getParameters().size() == 2);
	}

	@Override
	protected boolean isSupportedByType(final Action action, final String type) {
		return true;
	}

	@Override
	public void init(final Map<String, Parameter> parameters) throws ManagementException {
		reset(parameters); // create the model.
		final Parameter usegui = parameters.get("gui");
		if (usegui == null || (usegui instanceof Identifier && ((Identifier) usegui).getValue().equals("true"))) {
			this.gui = new BlocksWorldPainter(this.model);
		}

		// Try creating and registering an entity called gripper.
		try {
			registerEntity("gripper", new Gripper(this.model));
		} catch (final EntityException e) {
			throw new ManagementException("Could not create a gripper", e);
		}
	}

	/**
	 * Creates a new model if not already there. If there is already a model, it
	 * resets the model to the given new size. Resets the environment(-interface)
	 * with a set of key-value-pairs.
	 *
	 * @param parameters
	 * @throws ManagementException is thrown either when the initializing is not
	 *                             supported or the parameters are wrong.
	 */
	@Override
	public void reset(final Map<String, Parameter> parameters) throws ManagementException {
		// get the start config and check its type
		ParameterList start;
		final Parameter start1 = parameters.get("start");
		if (start1 != null) {
			if (start1 instanceof ParameterList) {
				start = (ParameterList) start1;
			} else if (start1 instanceof Identifier) {
				try {
					start = readNumbersFromFile(((Identifier) start1).getValue());
				} catch (final FileNotFoundException e) {
					throw new ManagementException("failed to read initial configuration from file " + start1, e);
				}
			} else {
				throw new IllegalArgumentException(
						"expected a list or a filename for initializing the blocks configuration but found " + start1);
			}
		} else {
			// no start info, use default world with 8 blocks on table.
			start = new ParameterList();
			for (int n = 0; n < 8; n++) {
				start.add(new Numeral(0));
			}
		}

		// convert to list of integers.
		final List<Integer> startlist = new ArrayList<>();
		for (final Parameter n : start) {
			if (!(n instanceof Numeral)) {
				throw new IllegalArgumentException("List should contain only numbers but found " + n);
			}
			startlist.add(((Numeral) n).getValue().intValue());
		}

		if (this.model == null) {
			this.model = new BlocksWorldModel(startlist);
		} else {
			this.model.reset(startlist);
		}

		setState(EnvironmentState.PAUSED);
	}

	/**
	 * Read a (Slaney-style) list of numbers from file.
	 *
	 * @param filename
	 * @return parameterlist with the numbers in the file.
	 * @throws FileNotFoundException
	 */
	private ParameterList readNumbersFromFile(final String filename) throws FileNotFoundException {
		final ParameterList list = new ParameterList();
		// first try if filename is absolute path.
		File file = new File(filename);

		if (!file.exists()) {
			// failed, try relative path, relative to folder where environment
			// interface jar can be found
			final File envfile = new File(getClass().getProtectionDomain().getCodeSource().getLocation().getFile());
			file = new File(envfile.getParentFile(), filename);
		}
		final Scanner sc = new Scanner(file);
		while (sc.hasNextInt()) {
			list.add(new Numeral(sc.nextInt()));
		}
		sc.close();

		return list;
	}

	@Override
	public void kill() throws ManagementException {
		if (this.gui != null) {
			this.gui.close();
			this.gui = null;
		}
		this.model = null;
		setState(EnvironmentState.KILLED);
	}

	/**
	 * Get name for block. null="table", block n="b"<n>
	 *
	 * @param block or null for table.
	 * @return block name or "table"
	 */
	public static String blockName(final Cube3D block) {
		if (block == null) {
			return "table";
		} else {
			return "b" + block.getNumber();
		}
	}
}
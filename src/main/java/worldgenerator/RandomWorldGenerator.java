package worldgenerator;

import java.util.HashMap;
import java.util.Random;
import java.util.Vector;

/**
 * Generates random configurations for the Blocks World.
 * 
 * This generator has been adapted from the C program written by John Slaney at
 * http://users.cecs.anu.edu.au/~jks/bwstates.html.
 * 
 * @author D.Singh
 * @modified W.Pasman 31jan13 to match Slaney's output format and to generate
 *           init files for learning.
 * 
 */
public class RandomWorldGenerator {

	private Random rand;
	private long seed;
	private Sigma sigma;
	private Vector<Double> ratio;

	private HashMap<String, Integer> stats;

	/**
	 * Random configuration generator.
	 * 
	 * @param N
	 * 		number of blocks
	 * @param seed
	 * 		random seed
	 * @param buildStatistics
	 */
	public RandomWorldGenerator(int N, long seed, boolean buildStatistics) {
		if (buildStatistics) {
			this.stats = new HashMap<String, Integer>();
		}

		this.seed = seed;
		rand = new Random(this.seed);
		sigma = new Sigma(N);
		ratio = new Vector<Double>();
		make_ratio(sigma.N, ratio);
	}

	/**
	 * Generate configurations from command line.
	 * 
	 * Usage: <numberr of blocks> <seed number> <number of iterations>
	 * 
	 * Note that n>6 with i>100000 will already take a long time to run.
	 * The reason is that the bookkeeping search/updates takes longer.
	 * For this reason, by default, statistics has been disabled.
	 *
	 */
	public static void main(String[] args) {
		int n, seed, loop;
		
		try {
			n = Integer.parseInt(args[0]);
			seed = Integer.parseInt(args[1]);
			loop = Integer.parseInt(args[2]);
			
			RandomWorldGenerator r = new RandomWorldGenerator(n, seed, false);
			for (int i = 0; i < loop; i++) {
				System.out.println(r.nextState());
			}
		} catch (Exception e) {
			System.err.println("usage: <nr of blocks> <seed nr> <nr of iterations>");
			System.exit(0);
		}
	}

	/**
	 * 
	 * @return
	 */
	public String nextState() {
		make_state(sigma, ratio);
		String state = print_state(sigma);
		if (stats != null) {
			Integer count = stats.containsKey(state) ? stats.get(state)
					: new Integer(0);
			stats.put(state, new Integer(count.intValue() + 1));
		}
		return state;
	}

	/**
	 * This function is called during initialization.
	 * 
	 * The function g is easily defined recursively:<br>
	 * g(0,k) = 1 <br>
	 * g(n+1,k) = g(n,k)(n + k) + g(n,k+1)<br>
	 * 
	 * This determines the required ratio. <br>
	 * Let g(n-1,k) = a. Let g(n-1,k+1)/a = R. Let g(n-1,k+2)/g(n-1,k+1) = S.
	 * Then we have:<br>
	 * g(n,k+1) / g(n,k) <br>=
	 * (Ra(n+k) + SRa) / (a(n+k-1) + Ra) <br>=
	 * R(n+k+Sa) / (n+k-1+R) <br>=
	 * (n+k+Sa) / ((n+k-1)/R + 1) <br>
	 * Either of the last two expressions may be used conveniently to calculate
	 * the ratio for (n,k) given those for (n-1,k) and (n-1,k+1).
	 */
	private void make_ratio(int N, Vector<Double> ratio) {
		int n, k;
		Vector<Double> temp = new Vector<Double>();

		for (k = 0; k <= N; k++) {
			temp.add(k, new Double(1.0));
		}

		for (n = 0; n <= N; n++)
			for (k = 0; k + n <= N; k++) {
				if (n == 0) {
					ratio.add(pos(N, n, k), new Double(1.0));
				} else {
					temp.set(k, (temp.get(k).doubleValue() * (temp.get(k + 1)
							.doubleValue() + n + k))
							/ (temp.get(k).doubleValue() + n + k - 1.0));
					if ((n % 2) == 0) {
						ratio.add(pos(N, n / 2, k), temp.get(k));
					}
				}
			}
	}

	/**
	 * The 2-dimensional array of ratios is represented in one dimension, so
	 * here is an index function such that ratio[pos(x,y)] is essentially
	 * ratio[2x][y].
	 */
	private int pos(int N, int x, int y) {
		return ((x * (N + 2 - x)) + y);
	}

	/**
	 * To make the state, begin by regarding the blocks as short floating
	 * towers, and repeatedly take the last one and put ikt on something. It may
	 * go on the table, in which case the array of grounded or rooted towers is
	 * extended by one, or it may go on another (floating or rooted) tower. All
	 * destinations except for the table have equal probability.
	 */
	private void make_state(Sigma sigma, Vector<Double> ratio) {
		int x;
		float r; /* The randomly generated number */
		float rat; /* The relevant ratio from the array */
		float p; /* The probability that the block goes on the table */
		int choice; /* Abbreviates (n + k) */
		int b; /* The destination block */

		for (x = 0; x < sigma.N; x++) {
			sigma.rooted.add(x, new Tower(-1, -1));
			sigma.floating.add(x, new Tower(x, x));
			sigma.S.add(x, new Integer(-1));
		} /* Initially, each block is a floating tower */
		sigma.nrt = 0;
		sigma.nft = sigma.N;
		while (sigma.nft-- != 0) {
			r = rand.nextFloat();
			choice = sigma.nft + sigma.nrt;
			rat = Ratio(ratio, sigma.N, sigma.nft, sigma.nrt);
			p = rat / (rat + choice);

			if (r <= p) { /* Put the next block on the table */
				sigma.rooted.get(sigma.nrt).top = sigma.floating.get(sigma.nft).top;
				sigma.rooted.get(sigma.nrt).bottom = sigma.floating
						.get(sigma.nft).bottom;
				sigma.nrt++;
			} else { /* Put the next block on some b */
				b = (int) Math
						.round(Math.floor((r - p) / ((1.0 - p) / choice)));
				if (b < sigma.nrt) { /* Destination is a rooted tower */
					sigma.S.set(sigma.floating.get(sigma.nft).bottom,
							sigma.rooted.get(b).top);
					sigma.rooted.get(b).top = sigma.floating.get(sigma.nft).top;
				} else { /* Destination is a floating tower */
					b -= sigma.nrt;
					sigma.S.set(sigma.floating.get(sigma.nft).bottom,
							sigma.floating.get(b).top);
					sigma.floating.get(b).top = sigma.floating.get(sigma.nft).top;
				}
			}
		}
	}

	/**
	 * Let g(n,k) be the number of states that extend a part-state with k towers
	 * already on the table and n floating towers not yet on anything. We work
	 * with Ratio(...n,k) which is g(n,k+1)/g(n,k). <br>
	 * The ratio is stored in the case of an even-numbered row, and calculated
	 * in the case of an odd-numbered row. This is simply to halve the space
	 * required to store ratios. Note that N is the number of blocks.
	 */
	private float Ratio(Vector<Double> ratio, int N, int x, int y) {
		int z;
		z = pos(N, x / 2, y);
		if (x % 2 != 0) {
			return (ratio.get(z + 1).floatValue() + x + y)
					/ (((1 / ratio.get(z).floatValue()) * (x + y - 1)) + 1);
		} else {
			return ratio.get(z).floatValue();
		}
	}

	/**
	 * Convert generated state to a comma-separated string.
	 * This matches Slaney's original output format.
	 * 
	 * @param sigma
	 * @return Slaney-style block configuration list.
	 */
	private String print_state(Sigma sigma) {
		String out = "";

		for (int x = 0; x < sigma.N; x++) {
			out += sigma.S.get(x) + 1;
			if (x < sigma.N - 1)
				out += ",";
		}
		
		return out;
	}

	/**
	 * A print_state function that print towers instead of a block position
	 * list.
	 * 
	 * @param sigma
	 * @return string containing generated towers.
	 */
	private String print_state_towers(Sigma sigma) {
		String out = "";
		for (int x = 0; x < sigma.N; x++) {
			/* Find a block on the table */
			if (sigma.S.get(x) + 1 == 0) {
				int done = 0;
				int thisBlock = x + 1;
				out += String.format("b%d", thisBlock);
				while (done == 0) {
					int y;
					int found = 0;
					for (y = 0; y < sigma.N; y++) {
						if (sigma.S.get(y) + 1 == thisBlock) {
							thisBlock = y + 1;
							out += String.format(",b%d", thisBlock);
							found = 1;
							break;
						}
					}
					if (found == 0) {
						done = 1;
					}
				}
				out += String.format("\n");
			}
		}
		return out;
	}

	/**
	 * Function to dump some statistics about the states generated. The raw
	 * output is useful for verifying that states are being generated uniformly,
	 * using MATLAB for instance.
	 * 
	 * @author D.Singh
	 */
	public String print_statistics() {
		if (stats == null) {
			return "No statistics are available. Must be enabled at startup.";
		}
		String raw = "";
		int total = 0;
		double min = 1;
		double max = 0;
		int rawlimit = 100;
		for (Integer i : stats.values()) {
			raw += (rawlimit > 0) ? i.intValue() + " " : "";
			total += i.intValue();
			rawlimit--;
		}
		for (Integer i : stats.values()) {
			double ratio = (i.doubleValue() / total);
			if (ratio < min) {
				min = ratio;
			}
			if (ratio >= max) {
				max = ratio;
			}
		}
		String str = "states=" + String.format("%05d", stats.size());
		str += ", expectancy=" + String.format("%6.4f", (1.0 / stats.size()));
		str += ", actual range=[" + String.format("%6.4f", min) + ","
				+ String.format("%6.4f", max) + "]";
		str += ", raw100=[ " + raw + "]";
		return str;
	}

	/**
	 *
	 */
	class Tower {
		int top;
		int bottom;

		public Tower(int t, int b) {
			top = t;
			bottom = b;
		}
	}

	/**
	 * 
	 */
	class Sigma {
		int N;
		Vector<Integer> S;
		int nrt;
		int nft;
		Vector<Tower> rooted;
		Vector<Tower> floating;

		public Sigma(int N) {
			this.N = N;
			S = new Vector<Integer>();
			rooted = new Vector<Tower>();
			floating = new Vector<Tower>();
		}
	}

}

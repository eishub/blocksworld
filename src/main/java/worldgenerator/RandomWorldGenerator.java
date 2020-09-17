package worldgenerator;

import java.util.HashMap;
import java.util.Map;
import java.util.Random;
import java.util.Vector;

/**
 * Generates random configurations for the Blocks World.
 *
 * This generator has been adapted from the C program written by John Slaney at
 * http://users.cecs.anu.edu.au/~jks/bwstates.html.
 */
public class RandomWorldGenerator {
	private final Random rand;
	private final long seed;
	private final Sigma sigma;
	private final Vector<Double> ratio;
	private Map<String, Integer> stats;

	/**
	 * Random configuration generator.
	 *
	 * @param N               number of blocks
	 * @param seed            random seed
	 * @param buildStatistics
	 */
	public RandomWorldGenerator(final int N, final long seed, final boolean buildStatistics) {
		if (buildStatistics) {
			this.stats = new HashMap<>();
		}

		this.seed = seed;
		this.rand = new Random(this.seed);
		this.sigma = new Sigma(N);
		this.ratio = new Vector<>();
		make_ratio(this.sigma.N, this.ratio);
	}

	/**
	 * Generate configurations from command line.
	 *
	 * Usage: <numberr of blocks> <seed number> <number of iterations>
	 *
	 * Note that n>6 with i>100000 will already take a long time to run. The reason
	 * is that the bookkeeping search/updates takes longer. For this reason, by
	 * default, statistics has been disabled.
	 *
	 */
	public static void main(final String[] args) {
		int n, seed, loop;
		try {
			n = Integer.parseInt(args[0]);
			seed = Integer.parseInt(args[1]);
			loop = Integer.parseInt(args[2]);

			final RandomWorldGenerator r = new RandomWorldGenerator(n, seed, false);
			for (int i = 0; i < loop; i++) {
				System.out.println(r.nextState());
			}
		} catch (final Exception e) {
			System.err.println("usage: <nr of blocks> <seed nr> <nr of iterations>");
			System.exit(0);
		}
	}

	/**
	 *
	 * @return
	 */
	public String nextState() {
		make_state(this.sigma, this.ratio);
		final String state = print_state(this.sigma);
		if (this.stats != null) {
			final Integer count = this.stats.containsKey(state) ? this.stats.get(state) : 0;
			this.stats.put(state, count.intValue() + 1);
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
	 * Let g(n-1,k) = a. Let g(n-1,k+1)/a = R. Let g(n-1,k+2)/g(n-1,k+1) = S. Then
	 * we have:<br>
	 * g(n,k+1) / g(n,k) <br>
	 * = (Ra(n+k) + SRa) / (a(n+k-1) + Ra) <br>
	 * = R(n+k+Sa) / (n+k-1+R) <br>
	 * = (n+k+Sa) / ((n+k-1)/R + 1) <br>
	 * Either of the last two expressions may be used conveniently to calculate the
	 * ratio for (n,k) given those for (n-1,k) and (n-1,k+1).
	 */
	private void make_ratio(final int N, final Vector<Double> ratio) {
		int n, k;
		final Vector<Double> temp = new Vector<>();
		for (k = 0; k <= N; k++) {
			temp.add(k, 1.0);
		}
		for (n = 0; n <= N; n++) {
			for (k = 0; k + n <= N; k++) {
				if (n == 0) {
					ratio.add(pos(N, n, k), 1.0);
				} else {
					temp.set(k, (temp.get(k).doubleValue() * (temp.get(k + 1).doubleValue() + n + k))
							/ (temp.get(k).doubleValue() + n + k - 1.0));
					if ((n % 2) == 0) {
						ratio.add(pos(N, n / 2, k), temp.get(k));
					}
				}
			}
		}
	}

	/**
	 * The 2-dimensional array of ratios is represented in one dimension, so here is
	 * an index function such that ratio[pos(x,y)] is essentially ratio[2x][y].
	 */
	private int pos(final int N, final int x, final int y) {
		return ((x * (N + 2 - x)) + y);
	}

	/**
	 * To make the state, begin by regarding the blocks as short floating towers,
	 * and repeatedly take the last one and put ikt on something. It may go on the
	 * table, in which case the array of grounded or rooted towers is extended by
	 * one, or it may go on another (floating or rooted) tower. All destinations
	 * except for the table have equal probability.
	 */
	private void make_state(final Sigma sigma, final Vector<Double> ratio) {
		int x;
		float r; /* The randomly generated number */
		float rat; /* The relevant ratio from the array */
		float p; /* The probability that the block goes on the table */
		int choice; /* Abbreviates (n + k) */
		int b; /* The destination block */

		for (x = 0; x < sigma.N; x++) {
			sigma.rooted.add(x, new Tower(-1, -1));
			sigma.floating.add(x, new Tower(x, x));
			sigma.S.add(x, -1);
		} /* Initially, each block is a floating tower */
		sigma.nrt = 0;
		sigma.nft = sigma.N;
		while (sigma.nft-- != 0) {
			r = this.rand.nextFloat();
			choice = sigma.nft + sigma.nrt;
			rat = Ratio(ratio, sigma.N, sigma.nft, sigma.nrt);
			p = rat / (rat + choice);
			if (r <= p) { /* Put the next block on the table */
				sigma.rooted.get(sigma.nrt).top = sigma.floating.get(sigma.nft).top;
				sigma.rooted.get(sigma.nrt).bottom = sigma.floating.get(sigma.nft).bottom;
				sigma.nrt++;
			} else { /* Put the next block on some b */
				b = (int) Math.round(Math.floor((r - p) / ((1.0 - p) / choice)));
				if (b < sigma.nrt) { /* Destination is a rooted tower */
					sigma.S.set(sigma.floating.get(sigma.nft).bottom, sigma.rooted.get(b).top);
					sigma.rooted.get(b).top = sigma.floating.get(sigma.nft).top;
				} else { /* Destination is a floating tower */
					b -= sigma.nrt;
					sigma.S.set(sigma.floating.get(sigma.nft).bottom, sigma.floating.get(b).top);
					sigma.floating.get(b).top = sigma.floating.get(sigma.nft).top;
				}
			}
		}
	}

	/**
	 * Let g(n,k) be the number of states that extend a part-state with k towers
	 * already on the table and n floating towers not yet on anything. We work with
	 * Ratio(...n,k) which is g(n,k+1)/g(n,k). <br>
	 * The ratio is stored in the case of an even-numbered row, and calculated in
	 * the case of an odd-numbered row. This is simply to halve the space required
	 * to store ratios. Note that N is the number of blocks.
	 */
	private float Ratio(final Vector<Double> ratio, final int N, final int x, final int y) {
		final int z = pos(N, x / 2, y);
		if (x % 2 != 0) {
			return (ratio.get(z + 1).floatValue() + x + y) / (((1 / ratio.get(z).floatValue()) * (x + y - 1)) + 1);
		} else {
			return ratio.get(z).floatValue();
		}
	}

	/**
	 * Convert generated state to a comma-separated string. This matches Slaney's
	 * original output format.
	 *
	 * @param sigma
	 * @return Slaney-style block configuration list.
	 */
	private String print_state(final Sigma sigma) {
		String out = "";
		for (int x = 0; x < sigma.N; x++) {
			out += sigma.S.get(x) + 1;
			if (x < sigma.N - 1) {
				out += ",";
			}
		}

		return out;
	}

	/**
	 * Function to dump some statistics about the states generated. The raw output
	 * is useful for verifying that states are being generated uniformly, using
	 * MATLAB for instance.
	 *
	 * @author D.Singh
	 */
	public String print_statistics() {
		if (this.stats == null) {
			return "No statistics are available. Must be enabled at startup.";
		}
		String raw = "";
		int total = 0;
		double min = 1;
		double max = 0;
		int rawlimit = 100;
		for (final Integer i : this.stats.values()) {
			raw += (rawlimit > 0) ? i.intValue() + " " : "";
			total += i;
			rawlimit--;
		}
		for (final Integer i : this.stats.values()) {
			final double ratio = (i.doubleValue() / total);
			if (ratio < min) {
				min = ratio;
			}
			if (ratio >= max) {
				max = ratio;
			}
		}

		String str = "states=" + String.format("%05d", this.stats.size());
		str += ", expectancy=" + String.format("%6.4f", (1.0 / this.stats.size()));
		str += ", actual range=[" + String.format("%6.4f", min) + "," + String.format("%6.4f", max) + "]";
		str += ", raw100=[ " + raw + "]";

		return str;
	}

	private final class Tower {
		int top;
		int bottom;

		public Tower(final int t, final int b) {
			this.top = t;
			this.bottom = b;
		}
	}

	private final class Sigma {
		int N;
		Vector<Integer> S;
		int nrt;
		int nft;
		Vector<Tower> rooted;
		Vector<Tower> floating;

		public Sigma(final int N) {
			this.N = N;
			this.S = new Vector<>();
			this.rooted = new Vector<>();
			this.floating = new Vector<>();
		}
	}
}

package organisms.ui;

import java.io.IOException;
import java.io.OutputStream;
import java.io.PrintStream;
import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class TournamentRunner {
	public static void main(String[] __args) {
		try {
			Instant start = Instant.now();

			String[] classNames = {
					"organisms.g0.RandomPlayer",
					"organisms.g0.RandomPlayer",
					"organisms.g0.RandomPlayer"
//					"organisms.g3.Chameleon",
////					"organisms.g3.LanternFly2",
//					"organisms.g4.Memory",
//					"organisms.g5.GodsFavorite",
//					"organisms.g6.SmartOrganism",
			};

			Class[] playerClasses = new Class[classNames.length];
			for (int i = 0; i < classNames.length; i++) {
				try {
					playerClasses[i] = Class.forName(classNames[i]);
				} catch (ClassNotFoundException e) {
					e.printStackTrace();
				}
			}

			PrintStream consoleOut = System.out;
			PrintStream emptyStream = new PrintStream(new OutputStream() {
				@Override
				public void write(int b) throws IOException {

				}
			});

			// silence standard and error prints
			System.setOut(emptyStream);
//			System.setErr(emptyStream);

			String[][] configurations = Util.readCSV("tournament.csv");
			printOneOff("Running " + configurations.length +
				" configurations", consoleOut, emptyStream);

			TournamentResultLogger.setUp("all-players-400.txt");
			List<String> outputs = new ArrayList<>(configurations.length);

			Arrays.stream(configurations).parallel().forEach(c -> {
				String configName = c[c.length - 1];
				outputs.add(
					OrganismsGame.runTournament(c, playerClasses));
				printOneOff("Finished configuration " + configName +
						" (" + secondsRound3(start) + " sec)",
					consoleOut, emptyStream);
			});

			TournamentResultLogger.print(String.join("\n", outputs));

			System.setOut(consoleOut);
			System.out.println("Tournament done");
			System.out.println("Runtime: " + secondsRound3(start) + " seconds");
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	private static synchronized void printOneOff(String toPrint, PrintStream actualOut,
									PrintStream emptyStream) {
		System.setOut(actualOut);
		System.out.println(toPrint);
		System.setOut(emptyStream);
	}

	private static double secondsRound3(Instant start, Instant end) {
		return start.until(end, ChronoUnit.MILLIS) / 1000.0;
	}

	private static double secondsRound3(Instant start) {
		return secondsRound3(start, Instant.now());
	}
}

import Data.*;

import java.nio.file.Path;
import java.util.Scanner;

public class MainClass {
	PathManager mgr;

	MainClass() {
		Scanner cin     = new Scanner(System.in);
		String input    = "";
		String toId;
		String fromId;

		//System.out.println("Enter CSV filename to load data (enter for default): ");
		//input = cin.nextLine();

//		if (input.isEmpty())
//			input = "metadata.csv";

		System.out.println("Loading point data and computing connections...");
		try {
			//mgr = new PathManager(input, " <-> ", true);
			mgr = new PathManager(" <-> ", true);
		} catch (Exception e) {
			System.err.println(e.getMessage());
			return;
		}

		// debug
//		mgr.printPoints();
//
//		System.out.println("========================================================");
//		mgr.printConnections();

		// user input
//		System.out.println("Enter starting location: ");
//		toId = cin.nextLine();
//
//		System.out.println("Enter destination: ");
//		fromId = cin.nextLine();

		// override for testing
		toId    = "JsDDhoV5LiTIKJwuSxly9w";
		fromId  = "QOIUeoy2pgWTdrK5gNObRw";
		System.out.println("Calculating path from " + fromId + " to " + toId + "...");
		System.out.println(mgr.getPath(toId, fromId));
	}

	public static void main(String[] args) {
		MainClass mc = new MainClass();
	}
}
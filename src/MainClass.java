import Data.*;

import java.util.Scanner;

public class MainClass {
	Manager mgr;

	MainClass() {
		Scanner cin     = new Scanner(System.in);
		String input    = "";
		String toId;
		String fromId;

		System.out.println("Enter CSV filename to load data (enter for default): ");
		//input = cin.nextLine();

		if (input.isEmpty())
			input = "metadata.csv";

		mgr = new Manager(input);

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
		mgr.printPath(toId, fromId);
	}

	public static void main(String[] args) {
		MainClass mc = new MainClass();
	}
}
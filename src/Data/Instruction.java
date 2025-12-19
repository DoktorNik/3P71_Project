package Data;

enum Maneuver {
	START, STRAIGHT, LEFT, RIGHT, ARRIVE
}

final class Instruction {
	final Maneuver maneuver;
	final String street;
	double meters;

	Instruction(Maneuver maneuver, String street, double meters) {
		this.maneuver = maneuver;
		this.street = street;
		this.meters = meters;
	}
}

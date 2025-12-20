/**
 * Nikolas Fraser
 * 5538939
 * Dec 19, 2025
 * COSC 3P71
 */

package Geo;

/**
 * wachu gonna do?
 */
enum Maneuver {
	START, STRAIGHT, LEFT, RIGHT, ARRIVE
}

/**
 * whee supermodular code
 */
final class Instruction {
	final Maneuver maneuver;
	final String street;
	double meters;

	/**
	 * OBJECTS FOR EVERYONE
	 * @param maneuver
	 * @param street
	 * @param meters
	 */
	Instruction(Maneuver maneuver, String street, double meters) {
		this.maneuver = maneuver;
		this.street = street;
		this.meters = meters;
	}
}

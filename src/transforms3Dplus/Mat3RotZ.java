package transforms3Dplus;

public class Mat3RotZ extends Mat3Identity {

	/**
	 * Vytvari transformacni matici 3x3 pro rotaci kolem osy Z ve 3D
	 * 
	 * @param alpha
	 *            uhel rotace v radianech
	 */
	public Mat3RotZ(double alpha) {
		mat[0][0] = (double) Math.cos(alpha);
		mat[1][1] = (double) Math.cos(alpha);
		mat[1][0] = (double) -Math.sin(alpha);
		mat[0][1] = (double) Math.sin(alpha);
	}
}
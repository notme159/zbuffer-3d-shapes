package transforms3Dplus;

/**
 * trida pro praci s body ve 3D (homogeni souradnice)
 */

public class Point3D {
	public double x, y, z, w;

	/**
	 * Vytvari 3D bod v homogenich souradnicich lezici v pocatku souradnic
	 */
	public Point3D() {
		x = y = z = 0.0f;
		w = 1.0f;
	}

	/**
	 * Vytvari 3D bod v homogenich souradnicich
	 * 
	 * @param ax
	 *            souradnice x
	 * @param ay
	 *            souradnice y
	 * @param az
	 *            souradnice z
	 */
	public Point3D(double ax, double ay, double az) {
		x = ax;
		y = ay;
		z = az;
		w = 1.0f;
	}

	/**
	 * Vytvari 3D bod v homogenich souradnicich
	 * 
	 * @param ax
	 *            souradnice x
	 * @param ay
	 *            souradnice y
	 * @param az
	 *            souradnice z
	 * @param aw
	 *            souradnice w
	 */
	public Point3D(double ax, double ay, double az, double aw) {
		x = ax;
		y = ay;
		z = az;
		w = aw;
	}

	/**
	 * Vytvari 3D bod v homogenich souradnicich
	 * 
	 * @param v
	 *            souradnice (x,y,z)
	 */
	public Point3D(Vec3D v) {
		x = v.x;
		y = v.y;
		z = v.z;
		w = 1.0f;
	}

	/**
	 * Vytvari 3D bod v homogenich souradnicich
	 * 
	 * @param p
	 *            souradnice (x,y,z,w)
	 */
	public Point3D(Point3D p) {
		x = p.x;
		y = p.y;
		z = p.z;
		w = p.w;
	}

	/**
	 * Nasobeni matici zprava
	 * 
	 * @param mat
	 *            matice 4x4
	 * @return nova instance Point3D
	 */
	public Point3D mul(Mat4 mat) {
		Point3D res = new Point3D();
		res.x = mat.mat[0][0] * x + mat.mat[1][0] * y + mat.mat[2][0] * z
				+ mat.mat[3][0] * w;
		res.y = mat.mat[0][1] * x + mat.mat[1][1] * y + mat.mat[2][1] * z
				+ mat.mat[3][1] * w;
		res.z = mat.mat[0][2] * x + mat.mat[1][2] * y + mat.mat[2][2] * z
				+ mat.mat[3][2] * w;
		res.w = mat.mat[0][3] * x + mat.mat[1][3] * y + mat.mat[2][3] * z
				+ mat.mat[3][3] * w;
		return res;
	}
	
	/*
	 * Transformace 3D bodu kvaternionem
	 * 
	 * @param q
	 *            kvaternion
	 * @return nova instance Point3D
	 */
	public Point3D mul(Quat q) {
		Point3D oPoint = new Point3D(this.dehomog().mul(q));
		return oPoint;
	}

	/**
	 * Pricteni vektoru
	 * 
	 * @param rhs
	 *            vektor (x,y,z,w)
	 * @return nova instance Point3D
	 */
	public Point3D add(Point3D p) {
		return new Point3D(x + p.x, y + p.y, z + p.z, w + p.w);
	}

	/**
	 * Nasobeni skalarem
	 * 
	 * @param f
	 *            skalar
	 * @return nova instance Point3D
	 */
	public Point3D mul(double f) {
		return new Point3D(x * f, y * f, z * f, w * f);
	}

	/**
	 * Dehmogenizace vektoru
	 * 
	 * @return nova instance Vec3D
	 */
	public Vec3D dehomog() {
		if (w == 0.0f)
			return new Vec3D(0,0,0);
		return new Vec3D(x / w, y / w, z / w);
	}

	/**
	 * Prevod na vektor (x,y,z), zanedbani w
	 * 
	 * @return nova instance Vec3D
	 */
	public Vec3D ignoreW() {
		return new Vec3D(x, y, z);
	}

}
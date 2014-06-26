package togetherfornow;

import java.awt.Color;
import java.awt.Graphics;
import java.awt.image.BufferedImage;
import java.util.List;

import transforms3Dplus.*;

public class GPU {
	private BufferedImage img;
	private int w;
	private int h;
	private double[][] depth;
	private double wmin = 0.001;
	private boolean fill = false;
	private Mat4 fMat;
	private List<Point3D> vertices;
	private List<Integer> indices;
	private Graphics g;

	public GPU(BufferedImage img) {
		this.img = img;
		w = img.getWidth();
		h = img.getHeight();
		depth = new double[w][h];
		g = img.getGraphics();
	}

	public void transform() {
		clear();
		// transform axes
		for (int i = 0; i < 6; i += 2) {
			Point3D sta = vertices.get(indices.get(i)).mul(fMat);
			Point3D end = vertices.get(indices.get(i + 1)).mul(fMat);

			double x1Axes = ((0.5 * (sta.x / sta.w + 1)) * (w - 1));
			double x2Axes = ((0.5 * (end.x / end.w + 1)) * (w - 1));
			double y1Axes = ((0.5 * (1 - sta.y / sta.w)) * (h - 1));
			double y2Axes = ((0.5 * (1 - end.y / end.w)) * (h - 1));

			g.setColor(new Color((int) (66666 * Math.random() + 100)));
			g.drawLine((int) x1Axes, (int) y1Axes, (int) x2Axes, (int) y2Axes);
		}

		// transform solids
		for (int i = 6; i < 30; i += 3) {
			Point3D prv = vertices.get(indices.get(i)).mul(fMat);
			Point3D drh = vertices.get(indices.get(i + 1)).mul(fMat);
			Point3D tre = vertices.get(indices.get(i + 2)).mul(fMat);
			int col = (int) (66666 * Math.random() + 100);

			clipW(prv, drh, tre, col);
		}
	}

	public void clipW(Point3D pA, Point3D pB, Point3D pC, int color) {
		if (pA.w > pB.w) {
			Point3D p = pA;
			pA = pB;
			pB = p;
		}
		if (pB.w > pC.w) {
			Point3D p = pB;
			pB = pC;
			pC = p;
		}
		if (pA.w > pB.w) {
			Point3D p = pA;
			pA = pB;
			pB = p;
		}

		if (pC.w < wmin) {
			return;
		}

		if (pB.w < wmin) {
			double t = (wmin - pC.w) / (pA.w - pC.w);
			Point3D nv1 = pA.mul(t).add(pC.mul(1.0 - t));
			t = (wmin - pC.w) / (pB.w - pC.w);
			Point3D nv2 = pB.mul(t).add(pC.mul(1.0 - t));
			dehomog(nv1, nv2, pC, color, fill);
			return;
		}

		if (pA.w < wmin) {
			double t = (wmin - pB.w) / (pA.w - pB.w);
			Point3D nva = pA.mul(t).add(pB.mul(1.0 - t));
			t = (wmin - pC.w) / (pA.w - pC.w);
			Point3D nvb = pA.mul(t).add(pC.mul(1.0 - t));
			dehomog(nva, nvb, pC, color, fill);
			dehomog(nva, pB, pC, color, fill);
			return;
		}

		dehomog(pA, pB, pC, color, fill);
	}

	private void dehomog(Point3D pA, Point3D pB, Point3D pC, int color, boolean fill) {
		Vec3D vA = pA.dehomog();
		Vec3D vB = pB.dehomog();
		Vec3D vC = pC.dehomog();

		cropScene(vA, vB, vC, color, fill);
	}

	private void cropScene(Vec3D vA, Vec3D vB, Vec3D vC, int color, boolean fill) {
		if ((Math.min(Math.min(vA.x, vB.x), vC.x) > 1.0) || (Math.max(Math.max(vA.x, vB.x), vC.x) < -1.0) ||
				(Math.min(Math.min(vA.y, vB.y), vC.y) > 1.0) || (Math.max(Math.max(vA.y, vB.y), vC.y) < -1.0) ||
				(Math.min(Math.min(vA.z, vB.z), vC.z) > 1.0) || (Math.max(Math.max(vA.z, vB.z), vC.z) < 0.0)) {
			return;
		}

		vA = vA.mul(new Vec3D(1.0, -1.0, 1.0)).add(new Vec3D(1.0, 1.0, 0.0)).mul(new Vec3D((w - 1) / 2, (h - 1) / 2, 1.0));
		vB = vB.mul(new Vec3D(1.0, -1.0, 1.0)).add(new Vec3D(1.0, 1.0, 0.0)).mul(new Vec3D((w - 1) / 2, (h - 1) / 2, 1.0));
		vC = vC.mul(new Vec3D(1.0, -1.0, 1.0)).add(new Vec3D(1.0, 1.0, 0.0)).mul(new Vec3D((w - 1) / 2, (h - 1) / 2, 1.0));

		scanLine(vA, vB, vC, color);
	}
	
	private void scanLine(Vec3D vA, Vec3D vB, Vec3D vC, int color) {
		if (fill) {
			if (vA.y > vB.y) {
				Vec3D p = vA;
				vA = vB;
				vB = p;
			}
			if (vB.y > vC.y) {
				Vec3D p = vB;
				vB = vC;
				vC = p;
			}
			if (vA.y > vB.y) {
				Vec3D p = vA;
				vA = vB;
				vB = p;
			}

			for (int y = Math.max((int) vA.y + 1, 0); y <= Math.min(vB.y, h - 1); y++) {
				double s1 = (y - vA.y) / (vB.y - vA.y);
				double s2 = (y - vA.y) / (vC.y - vA.y);
				double x1 = vA.x * (1.0 - s1) + vB.x * s1;
				double x2 = vA.x * (1.0 - s2) + vC.x * s2;
				double z1 = vA.z * (1.0 - s1) + vB.z * s1;
				double z2 = vA.z * (1.0 - s2) + vC.z * s2;
				if (x1 > x2) {
					double t = x1;
					x1 = x2;
					x2 = t;
					t = z1;
					z1 = z2;
					z2 = t;
				}
				for (int x = Math.max((int) x1 + 1, 0); x <= Math.min(x2, w - 1); x++) {
					double t = (x - x1) / (x2 - x1);
					double z = z1 * (1.0 - t) + z2 * t;
					if ((depth[x][y] > z) && (z > 0.0)) {
						depth[x][y] = z;
						img.setRGB(x, y, new Color(color).getRGB());
					}
				}
			}

			for (int y = Math.max((int) vB.y + 1, 0); y <= Math.min(vC.y, h - 1); y++) {
				double s1 = (y - vB.y) / (vC.y - vB.y);
				double s2 = (y - vA.y) / (vC.y - vA.y);
				double x1 = vB.x * (1.0 - s1) + vC.x * s1;
				double x2 = vA.x * (1.0 - s2) + vC.x * s2;
				double z1 = vB.z * (1.0 - s1) + vC.z * s1;
				double z2 = vA.z * (1.0 - s2) + vC.z * s2;
				if (x1 > x2) {
					double t = x1;
					x1 = x2;
					x2 = t;
					t = z1;
					z1 = z2;
					z2 = t;
				}
				for (int x = Math.max((int) x1 + 1, 0); x <= Math.min(x2, w - 1); x++) {
					double t = (x - x1) / (x2 - x1);
					double z = z1 * (1.0 - t) + z2 * t;
					if ((depth[x][y] > z) && (z > 0.0)) {
						depth[x][y] = z;
						img.setRGB(x, y, new Color(color).getRGB());
					}
				}
			}
		} else {
			g.setColor(new Color(color));
			g.drawLine((int) vA.x, (int) vA.y, (int) vB.x, (int) vB.y);
			g.drawLine((int) vA.x, (int) vA.y, (int) vC.x, (int) vC.y);
			g.drawLine((int) vC.x, (int) vC.y, (int) vB.x, (int) vB.y);
		}
	}

	public void clear() {
		for (int x = 0; x < w; x++)
			for (int y = 0; y < h; y++)
				depth[x][y] = 1.0;
	}

	public void setFill(boolean fill) {
		this.fill = fill;
	}

	public void setVertices(List<Point3D> vertices) {
		this.vertices = vertices;
	}

	public void setIndices(List<Integer> indices) {
		this.indices = indices;
	}

	public void setfMat(Mat4 fMat) {
		this.fMat = fMat;
	}
}
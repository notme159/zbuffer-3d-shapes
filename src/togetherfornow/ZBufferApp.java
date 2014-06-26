package togetherfornow;

import java.awt.Color;
import java.awt.Graphics;
import java.awt.event.KeyAdapter;
import java.awt.event.KeyEvent;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.event.MouseMotionAdapter;
import java.awt.image.BufferedImage;
import java.util.ArrayList;
import javax.swing.JFrame;
import javax.swing.SwingUtilities;
import transforms3Dplus.*;

@SuppressWarnings("serial")
public class ZBufferApp extends JFrame {
	private BufferedImage img;
	private GPU renderZbuffer;
	private ArrayList<Point3D> vertices = new ArrayList<>();
	private ArrayList<Integer> indices = new ArrayList<>();
	private Mat4 viewTrans;
	private Mat4 projTrans;
	private Mat4 rotTrans = new Mat4Identity();
	private Mat4 finTrans;
	private Camera cam = new Camera();
	private double azimuth = 0.0;
	private double zenith = 0.0;
	private boolean mouseDown = false;
	private boolean fill = true;
	private boolean persp = true;
	private int oldX;
	private int oldY;
	private Graphics g;
	private float speed = 0.1f;

	public static void main(String[] args) {
		SwingUtilities.invokeLater(new Runnable() {
			@Override
			public void run() {
				new ZBufferApp().setVisible(true);
			}
		});
	}

	public ZBufferApp() {
		// jframe size
		setSize(800, 600);
		// center the jframe
		setLocationRelativeTo(null);
		// set title
		setTitle("====================================DISCO ZBUFFER");
		// close jframe if [x] clicked
		setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		// nastavi a implementuje eventy
		regListeners();
		// new img obj from jframe width, height, argb type?
		img = new BufferedImage(getWidth(), getHeight(), 2);
		// zbuffer obj from img
		renderZbuffer = new GPU(img);
		initialize();
		startRender();
	}

	private void regListeners() {
		addMouseListener(new MouseAdapter() {
			public void mousePressed(MouseEvent e) {
				if (e.getButton() == 1) {
					mouseDown = true;
					oldX = e.getX();
					oldY = e.getY();
				}
			};

			public void mouseReleased(MouseEvent e) {
				if (e.getButton() == 1) {
					mouseDown = false;
				}
			};
		});
		addMouseMotionListener(new MouseMotionAdapter() {
			@Override
			public void mouseDragged(MouseEvent e) {
				super.mouseDragged(e);
				if (mouseDown) {
					cam.addAzimuth(-(Math.PI * (oldX - e.getX()) / getWidth()));
					cam.addZenith(Math.PI * (e.getY() - oldY) / getHeight());
					oldX = e.getX();
					oldY = e.getY();
					startRender();
					repaint();
				}
			}
		});
		addKeyListener(new KeyAdapter() {
			@Override
			public void keyPressed(KeyEvent e) {
				super.keyPressed(e);
				switch (e.getKeyCode()) {
				case KeyEvent.VK_UP:
					cam.down(speed);
					startRender();
					repaint();
					break;
				case KeyEvent.VK_DOWN:
					cam.up(speed);
					startRender();
					repaint();
					break;
				case KeyEvent.VK_LEFT:
					cam.right(speed);
					startRender();
					repaint();
					break;
				case KeyEvent.VK_RIGHT:
					cam.left(speed);
					startRender();
					repaint();
					break;
				case KeyEvent.VK_A:
					cam.backward(speed);
					startRender();
					repaint();
					break;
				case KeyEvent.VK_Q:
					cam.forward(speed);
					startRender();
					repaint();
					break;
				case KeyEvent.VK_W:
					fill = (!fill);
					startRender();
					repaint();
					break;
				case KeyEvent.VK_E:
					persp = (!persp);
					startRender();
					repaint();
				}
			}
		});
	}

	public void initialize() {
		g = img.getGraphics();
		
		cam.setAzimuth(azimuth);
		cam.setZenith(zenith);
		cam.setPosition(new Vec3D(-22.0, 0.0, 0.0));

		vertices.clear();

		// axes
		vertices.add(new Point3D(0.0, 0.0, 0.0));
		vertices.add(new Point3D(15.0, 0.0, 0.0));
		vertices.add(new Point3D(0.0, 15.0, 0.0));
		vertices.add(new Point3D(0.0, 0.0, 15.0));

		// solid1
		vertices.add(new Point3D(5.0, 5.0, 5.0D));
		vertices.add(new Point3D(-5.0, -5.0, 5.0));
		vertices.add(new Point3D(-5.0, 5.0, -5.0));
		vertices.add(new Point3D(5.0, -5.0, -5.0));

		// solid2
		vertices.add(new Point3D(-4.0, -4.0, -4.0));
		vertices.add(new Point3D(4.0, 4.0, -4.0));
		vertices.add(new Point3D(4.0, -4.0, 4.0));
		vertices.add(new Point3D(-4.0, 4.0, 4.0));

		indices.clear();

		indices.add(0); indices.add(1);

		indices.add(0); indices.add(2);

		indices.add(0); indices.add(3);

		indices.add(4); indices.add(5); indices.add(6);

		indices.add(5); indices.add(6); indices.add(7);

		indices.add(4); indices.add(5); indices.add(7);

		indices.add(4); indices.add(6); indices.add(7);

		indices.add(8); indices.add(9); indices.add(10);

		indices.add(9); indices.add(10); indices.add(11);

		indices.add(8); indices.add(9); indices.add(11);

		indices.add(8); indices.add(10); indices.add(11);
	}

	public void startRender() {
		// set bg color, draw bg
		g.setColor(Color.BLACK);
		g.fillRect(0, 0, img.getWidth(), img.getHeight());
		
		// draw legend
		g.setColor(Color.WHITE);
		g.drawString("mys/sipky pohyb, Q zvetsit, A zmensit, W drat, E projekce", 10, 40);

		// get view matrix from cam
		viewTrans = cam.getViewMatrix();

		// check if persp or ortho
		if (persp)
			projTrans = new Mat4PerspRH(Math.PI / 4, 1, 0.01, 50);
		else {
			projTrans = new Mat4OrthoRH(img.getWidth() / 10, img.getHeight() / 10, -10.0, 100.0);
		}

		// set MatRot, make finalMat
		rotTrans = rotTrans.mul(new Mat4RotXYZ(0.04, 0.04, 0.04));
		finTrans = rotTrans.mul(viewTrans).mul(projTrans);

		// set render
		renderZbuffer.setfMat(finTrans);
		renderZbuffer.setVertices(vertices);
		renderZbuffer.setIndices(indices);
		
		// fill/nofill option
		renderZbuffer.setFill(fill);

		// render vertices
		renderZbuffer.transform();
	}

	public void paint(Graphics g) {
		g.drawImage(img, 0, 0, this);
	}
}
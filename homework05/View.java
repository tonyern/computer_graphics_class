//******************************************************************************
// Copyright (C) 2016-2019 University of Oklahoma Board of Trustees.
//******************************************************************************
// Last modified: Thu Apr 18 19:37:22 2019 by Chris Weaver
//******************************************************************************
// Major Modification History:
//
// 20160209 [weaver]:	Original file.
// 20190203 [weaver]:	Updated to JOGL 2.3.2 and cleaned up.
// 20190227 [weaver]:	Updated to use model and asynchronous event handling.
// 20190318 [weaver]:	Modified for homework04.
//
//******************************************************************************
// Notes:
//
//******************************************************************************

package edu.ou.cs.cg.assignment.homework05;

//import java.lang.*;
import java.awt.*;
import java.awt.geom.Point2D;
import java.awt.geom.Point2D.Double;
import java.text.DecimalFormat;
import java.util.*;

import com.jogamp.opengl.*;
import com.jogamp.opengl.awt.GLJPanel;
import com.jogamp.opengl.glu.*;
import com.jogamp.opengl.util.FPSAnimator;
import com.jogamp.opengl.util.awt.TextRenderer;
import edu.ou.cs.cg.utilities.*;

//******************************************************************************

/**
 * The <CODE>View</CODE> class.<P>
 *
 * @author  Chris Weaver
 * @version %I%, %G%
 */
public final class View
	implements GLEventListener
{
	//**********************************************************************
	// Private Class Members
	//**********************************************************************

	private static final int			DEFAULT_FRAMES_PER_SECOND = 60;
	@SuppressWarnings("unused")
	private static final DecimalFormat	FORMAT = new DecimalFormat("0.000");

	//**********************************************************************
	// Private Members
	//**********************************************************************

	// State (internal) variables
	private final GLJPanel			canvas;
	private int						w;			// Canvas width
	private int						h;			// Canvas height

	private TextRenderer			renderer;

	private final FPSAnimator		animator;
	private int						counter;	// Frame counter

	private final Model				model;

	@SuppressWarnings("unused")
	private final KeyHandler		keyHandler;
	@SuppressWarnings("unused")
	private final MouseHandler		mouseHandler;

	// TODO: YOUR ADDITIONAL MEMBERS HERE (AS NEEDED)
	private final ArrayList<Deque<Point2D.Double>> nodePolygon;
	private final Deque<Point2D.Double>	hull;
	
	//**********************************************************************
	// Constructors and Finalize
	//**********************************************************************

	public View(GLJPanel canvas)
	{
		this.canvas = canvas;

		// Initialize rendering
		counter = 0;
		canvas.addGLEventListener(this);

		// Initialize model (scene data and parameter manager)
		model = new Model(this);

		// TODO: INITIALIZE YOUR ADDITIONAL MEMBERS HERE (AS NEEDED)
		nodePolygon = new ArrayList<Deque<Point2D.Double>>();
		hull = new ArrayDeque<Point2D.Double>();
		
		// Initialize controller (interaction handlers)
		keyHandler = new KeyHandler(this, model);
		mouseHandler = new MouseHandler(this, model);

		// Initialize animation
		animator = new FPSAnimator(canvas, DEFAULT_FRAMES_PER_SECOND);
		animator.start();
	}

	//**********************************************************************
	// Getters and Setters
	//**********************************************************************

	public GLJPanel	getCanvas()
	{
		return canvas;
	}

	public int	getWidth()
	{
		return w;
	}

	public int	getHeight()
	{
		return h;
	}
	
	public int getCounter()
	{
		return counter;
	}

	//**********************************************************************
	// Override Methods (GLEventListener)
	//**********************************************************************

	public void	init(GLAutoDrawable drawable)
	{
		w = drawable.getSurfaceWidth();
		h = drawable.getSurfaceHeight();

		renderer = new TextRenderer(new Font("Monospaced", Font.PLAIN, 12),
									true, true);

		initPipeline(drawable);
	}

	public void	dispose(GLAutoDrawable drawable)
	{
		renderer = null;
	}

	public void	display(GLAutoDrawable drawable)
	{
		updatePipeline(drawable);

		update(drawable);
		render(drawable);
	}

	public void	reshape(GLAutoDrawable drawable, int x, int y, int w, int h)
	{
		this.w = w;
		this.h = h;
	}

	//**********************************************************************
	// Private Methods (Rendering)
	//**********************************************************************

	private void	update(GLAutoDrawable drawable)
	{
		counter++;								// Advance animation counter
	}

	// Drawing only happens when render is called.
	private void	render(GLAutoDrawable drawable)
	{
		GL2	gl = drawable.getGL().getGL2();

		gl.glClear(GL.GL_COLOR_BUFFER_BIT);		// Clear the depth buffer
		// Draw the scene
		drawMain(gl);							// Draw main content
		drawMode(drawable);						// Draw mode text

		gl.glFlush();							// Finish and display
	}

	//**********************************************************************
	// Private Methods (Pipeline)
	//**********************************************************************

	private void	initPipeline(GLAutoDrawable drawable)
	{
		GL2	gl = drawable.getGL().getGL2();

		gl.glClearColor(0.0f, 0.0f, 0.0f, 0.0f);	// Black background
	}

	private void	updatePipeline(GLAutoDrawable drawable)
	{
		GL2	gl = drawable.getGL().getGL2();
		GLU	glu = GLU.createGLU();

		gl.glMatrixMode(GL2.GL_PROJECTION);			// Prepare for matrix x-form
		gl.glLoadIdentity();						// Set to identity matrix
		glu.gluOrtho2D(-1.5, 1.5, -1.5, 1.5);		// 2D translate and scale
	}

	//**********************************************************************
	// Private Methods (Scene)
	//**********************************************************************

	private void	drawMode(GLAutoDrawable drawable)
	{
		renderer.beginRendering(w, h);

		renderer.setColor(0.75f, 0.75f, 0.75f, 1.0f);		// Light gray
		renderer.draw("Welcome to Blobster", 2, h - 12);	// Upper left corner

		// All names not in the node appearing in the lower left-hand corner.
		renderer.setColor(1.0f, 1.0f, 0.0f, 1.0f);
		
		// Making sure array names has elements in it. If not, then display "Empty List."
		if (!model.getIndividualNames().isEmpty())
		{
			// Checking edge cases.
			if (model.getNameCycle() == model.getIndividualNames().size())
			{
				if (!(model.getIndividualNames().get(model.getNameCycle() - 1) == null))
				{
					renderer.draw(model.getIndividualNames().get(model.getNameCycle() - 1), 2, 30);
					model.decreaseNameCycle();
				}
			}
			else if (!(model.getIndividualNames().get(model.getNameCycle()) == null))
			{
				renderer.draw(model.getIndividualNames().get(model.getNameCycle()), 2, 30);
			}
		}
		else
		{
			renderer.draw("Empty List", 2, 30);
		}

		renderer.endRendering();
	}

	private void	drawMain(GL2 gl)
	{
		drawGrid(gl);					// X and Y grid lines
		drawAxes(gl);					// X and Y axes
		drawHull(gl);					// Convex hull around...
		drawNodes(gl);					// ...all inserted/visible nodes
		//drawSelectedNode(gl);			// Current node selected
		drawCursor(gl);					// Cursor around the mouse point
	}

	// Draw horizontal (y==0) and vertical (x==0) axes
	private void	drawAxes(GL2 gl)
	{
		gl.glColor3f(0.50f, 0.50f, 0.50f);			// Medium gray

		gl.glBegin(GL.GL_LINES);

		gl.glVertex2d(-10.0, 0.0);
		gl.glVertex2d(10.0, 0.0);

		gl.glVertex2d(0.0, -10.0);
		gl.glVertex2d(0.0, 10.0);

		gl.glEnd();
	}

	// If the cursor point is not null, draw something helpful around it.
	private void	drawCursor(GL2 gl)
	{
		Point2D.Double	cursor = model.getCursor();

		if (cursor == null)
			return;

		// Draw a circle of radius 0.025

		gl.glColor3f(0.50f, 0.50f, 0.50f);			// Medium gray

		gl.glBegin(GL.GL_LINE_LOOP);

		for (int i=0; i<32; i++)
		{
			double	theta = (2.0 * Math.PI) * (i / 32.0);

			gl.glVertex2d(cursor.x + 0.025 * Math.cos(theta),
						  cursor.y + 0.025 * Math.sin(theta));
		}

		gl.glEnd();
	}

	// Draws a grid in the XY plane around the origin.
	private void	drawGrid(GL2 gl)
	{
		gl.glPushMatrix();

		drawGridLines(gl);					// Draw vertical lines
		gl.glRotated(90.0, 0.0, 0.0, 1.0);	// Rotate +90 degrees about Z
		drawGridLines(gl);					// Draw horizontal lines

		gl.glPopMatrix();
	}

	// Draws 21 parallel horizontal lines, with 10 on each side of the origin.
	private void	drawGridLines(GL2 gl)
	{
		gl.glColor3f(0.15f, 0.15f, 0.15f);			// Very dark gray

		gl.glPushMatrix();

		gl.glTranslated(-1.0, 0.0, 0.0);			// Start on the left

		for (int i=-10; i<=10; i++)				// Draw 21 lines
		{
			gl.glBegin(GL.GL_LINES);
			gl.glVertex2d(0.0, -1.0);
			gl.glVertex2d(0.0, 1.0);
			gl.glEnd();

			gl.glTranslated(0.1, 0.0, 0.0);		// Inch to the right
		}

		gl.glPopMatrix();
	}

	// Draw the convex hull around the currently inserted/visible nodes.
	private void	drawHull(GL2 gl)
	{
		// If hull is empty, it becomes just the insert point.
		if (hull.isEmpty())
		{
			// TODO: Figure out insert point.
		}
		
	}

	// Draw the social network nodes that are currently inserted/visible.
	private void	drawNodes(GL2 gl)
	{
		String nodeToAdd;
		
		// Draw all visible nodes in the visible nodes list.
		if (!model.getNodes().isEmpty())
		{
			for (int i = 0; i < model.getNodes().size(); i++)
			{
				nodeToAdd = model.getNodes().get(i);
				
				int red = Network.getColor(nodeToAdd).getRed();
				int green = Network.getColor(nodeToAdd).getGreen();
				int blue = Network.getColor(nodeToAdd).getBlue();
				
				if (i == model.getNodeCycle())
				{
					gl.glPushMatrix();
					
					// Node movements.
					gl.glTranslated(model.getSelectedX(), model.getSelectedY(), 0.0);
						
					// Node Rotation.
					gl.glRotated(model.getNodeRotation(), 0.0, 0.0, 1.0);
					
					setColor(gl, red, green, blue, 255);
					fillPolygon(gl, createPolygon(Network.getSides(nodeToAdd), 
							model.getNodeRadius()));
					setColor(gl, 255, 255, 255, 255);
					edgePolygon(gl, createPolygon(Network.getSides(nodeToAdd), 
							model.getNodeRadius()));
					
					gl.glPopMatrix();
				}
				else
				{
					setColor(gl, red, green, blue, 255);
					fillPolygon(gl, createPolygon(Network.getSides(nodeToAdd), 
							0.050));
					edgePolygon(gl, createPolygon(Network.getSides(nodeToAdd), 
							0.050));
				}
			}
		}
		else
		{
			return;
		}
	}
	
	/*// User can cycle between visible nodes. Once a node is currently selected,
	// its edges color white to show selection.
	private void drawSelectedNode(GL2 gl)
	{
		if (!model.getSelectedNode().isEmpty())
		{
			String nodeToAdd = model.getSelectedNode().get(0);
				
			// Remove from visible nodes list as there was a problem of duplicate
			// nodes appearing.
			if (model.getNodes().contains(nodeToAdd))
			{
				model.getNodes().remove(nodeToAdd);
			}
			
			int red = Network.getColor(nodeToAdd).getRed();
			int green = Network.getColor(nodeToAdd).getGreen();
			int blue = Network.getColor(nodeToAdd).getBlue();
				
			gl.glPushMatrix();
				
			// Node movements.
			gl.glTranslated(model.getSelectedX(), model.getSelectedY(), 0.0);
				
			// Node Rotation.
			gl.glRotated(model.getNodeRotation(), 0.0, 0.0, 1.0);
				
			setColor(gl, red, green, blue, 255);
			fillPolygon(gl, createPolygon(Network.getSides(nodeToAdd), 
					model.getNodeRadius()));
			setColor(gl, 255, 255, 255, 255);
			edgePolygon(gl, createPolygon(Network.getSides(nodeToAdd), 
					model.getNodeRadius()));
				
			gl.glPopMatrix();
		}
		else
		{
			return;
		}
	}*/

	/*// An example to give you an idea about coding with transformations...
	private void	drawAnExampleThingWithAffineTransformations(GL2 gl)
	{
		// Calculate translation, scaling, and rotation values for your object
		double	dx = -0.005 * counter;
		double	dy = 0.3;
		double	sx = 0.5;
		double	sy = 0.3;
		double	theta = counter * 0.05;			// In degrees

		gl.glPushMatrix();						// Begin using internal xforms

		// Causes polygon to move.
		gl.glTranslated(1.0+dx, dy, 0.0);		// Translate (in 2D: dz = 0.0)
		
		// IDK what this does!
		gl.glScaled(sx, sy, 1.0);				// Scale (in 2D: sz = 1.0)
		
		// Causes polygon to rotate.
		gl.glRotated(counter, 0.0, 0.0, 1.0);	// Rotate (in 2D: about Z axis)

		fillPolygon(gl, createPolygon(5));		// Fill a pentagon
		edgePolygon(gl, createPolygon(32));		// Edge a "circle"

		gl.glPopMatrix();						// End using internal xforms
	}*/

	//**********************************************************************
	// Private Methods (Network and Hull)
	//**********************************************************************

	// TODO: YOUR METHODS TO MANAGE THE SOCIAL NETWORK AND CONVEX HULL HERE	

	//**********************************************************************
	// Private Methods (Polygons)
	//**********************************************************************

	// Set color for a polygon.
	private void	setColor(GL2 gl, int r, int g, int b, int a)
	{
		gl.glColor4f(r / 255.0f, g / 255.0f, b / 255.0f, a / 255.0f);
	}
	
	// Creates a regular N-gon with points stored in counterclockwise order.
	// The polygon is centered at the origin with first vertex at (1.0, 0.0).
	private Deque<Point2D.Double>	createPolygon(int sides, double rad)
	{	
		Deque<Point2D.Double>	polygon = new ArrayDeque<Point2D.Double>(sides);
			
		// Initializing variables needed to get coordinates of Regular Polygons.
		double angle = (2 * Math.PI) / sides;
		double x = 0.000;
		double y = 0.000;
		double radius = rad;
			
		// Loop through sides and calculate vertices of the polygon.
		for (int i = 0; i < sides; i++)
		{
			// Centered at origin with radius being 1.
			x = radius * Math.cos(i * angle);
			y = radius * Math.sin(i * angle);
			polygon.add(new Point2D.Double(x, y));
		}
	
		// TODO: Test adding to array list.
		nodePolygon.add(polygon);
		
		return polygon;
	}

	// Draws the sides of the specified polygon.
	private void	edgePolygon(GL2 gl, Deque<Point2D.Double> polygon)
	{
		gl.glBegin(GL.GL_LINE_LOOP);

		for (Point2D.Double p : polygon)
			gl.glVertex2d(p.x, p.y);

		gl.glEnd();
	}

	// Draws the interior of the specified polygon.
	private void	fillPolygon(GL2 gl, Deque<Point2D.Double> polygon)
	{
		gl.glBegin(GL2.GL_POLYGON);

		for (Point2D.Double p : polygon)
			gl.glVertex2d(p.x, p.y);

		gl.glEnd();
	}

	//**********************************************************************
	// Private Methods (Vectors)
	//**********************************************************************

	// This might be a method to calculate a dot product. Sure seems like it.
	private double		dot(double vx, double vy, double vz,
							double wx, double wy, double wz)
	{
		return (vx * wx + vy * wy + vz * wz);
	}

	// Determines if point q is to the left of line p1->p2. If strict is false,
	// points exactly on the line are considered to be left of it.
	private boolean	isLeft(Point2D.Double p1, Point2D.Double p2,
							   Point2D.Double q, boolean strict)
	{
		// Calculate the CCW (inward-pointing) perp vector for the side.
		double		vdx = p2.x - p1.x;		// Calc side vector
		double		vdy = p2.y - p1.y;		// from p1 to p2
		double		ndx = -vdy;			// Calc perp vector:
		double		ndy = vdx;				// negate y and swap

		// See the dot product on the slide on "Testing Containment in 2D".
		double		wdx = q.x - p1.x;		// Calculate test vector
		double		wdy = q.y - p1.y;		// from p1 to q
		double		dot = dot(wdx, wdy, 0.0, ndx, ndy, 0.0);

		// If strict, disallow cases of points exactly along the side's line.
		return (strict ? (dot > 0.0) : (dot >= 0.0));
	}

	// Determines if point q is inside a polygon. The polygon must be convex
	// with points stored in counterclockwise order. Points exactly on any side
	// of the polygon are considered to be outside of it.
	private boolean	contains(Deque<Point2D.Double> polygon,
								 Point2D.Double q)
	{
		int			sides = polygon.size();
		boolean		contains = true;
		Point2D.Double	p1 = polygon.peekLast();

		for (int i=0; i<sides; i++)
		{
			Point2D.Double	p2 = polygon.peekFirst();

			if (!isLeft(p1, p2, q, true))
				contains = false;

			polygon.offerLast(polygon.pollFirst());	// Cycle around polygon
			p1 = p2;
		}

		return contains;
	}
}

//******************************************************************************

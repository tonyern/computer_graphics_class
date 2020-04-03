//******************************************************************************
// Copyright (C) 2019 University of Oklahoma Board of Trustees.
//******************************************************************************
// Last modified: Fri Mar 27 19:01:58 2020 by Chris Weaver
//******************************************************************************
// Major Modification History:
//
// 20190227 [weaver]:	Original file.
// 20190318 [weaver]:	Modified for homework04.
//
//******************************************************************************
//
// The model manages all of the user-adjustable variables utilized in the scene.
// (You can store non-user-adjustable scene data here too, if you want.)
//
// For each variable that you want to make interactive:
//
//   1. Add a member of the right type
//   2. Initialize it to a reasonable default value in the constructor.
//   3. Add a method to access (getFoo) a copy of the variable's current value.
//   4. Add a method to modify (setFoo) the variable *asynchronously*.
//
// Concurrency management is important because the JOGL and the Java AWT run on
// different threads. The modify methods use the GLAutoDrawable.invoke() method
// so that all changes to variables take place on the JOGL thread. Because this
// happens at the END of GLEventListener.display(), all changes will be visible
// to the View.update() and render() methods in the next animation cycle.
//
//******************************************************************************

package edu.ou.cs.cg.assignment.homework05;

//import java.lang.*;
import java.awt.Point;
import java.awt.geom.Point2D;
import java.util.*;
import com.jogamp.opengl.*;
import edu.ou.cs.cg.utilities.*;

//******************************************************************************

/**
 * The <CODE>Model</CODE> class.
 *
 * @author  Chris Weaver
 * @version %I%, %G%
 */
public final class Model
{
	//**********************************************************************
	// Private Members
	//**********************************************************************

	// State (internal) variables
	private final View					view;

	// Model variables
	private Point2D.Double				cursor;	// Current cursor coordinates

	// TODO: YOUR ADDITIONAL MEMBERS HERE (AS NEEDED)
	
	// XY coordinates of a node that if selected, user can manipulate.
	double								dx;
	double								dy;
	
	// For user to cycle through name.
	private int							nameCycle;
	// For user to cycle through nodes.
	private int							nodeCycle;
	// For user to scale radius of selected node.
	private double						nodeRadius;
	// For user to rotate the selected node.
	private int							nodeRotation;
	// For user to scale hull radius.
	private double						hullRadius;
	
	// Managing names.
	private final String[] names;
	private final ArrayList<String> individualNames;
	
	// Managing nodes.
	private final ArrayList<String>	nodes;

	//**********************************************************************
	// Constructors and Finalize
	//**********************************************************************

	public Model(View view)
	{
		this.view = view;

		// Initialize user-adjustable variables (with reasonable default values)
		cursor = null;

		// TODO: INITIALIZE YOUR ADDITIONAL MEMBERS HERE (AS NEEDED)
		nameCycle = 0;
		nodeCycle = 0;
		
		dx = 0.0;
		dy = 0.0;
		nodeRadius = 0.05;
		
		// List of all names.
		names = Network.getAllNames();
		
		individualNames = new ArrayList<String>();
		for (int i = 0; i < names.length; i++)
		{
			individualNames.add(names[i]);
		}
		
		nodes = new ArrayList<String>();
	}

	//**********************************************************************
	// Public Methods (Access Variables)
	//**********************************************************************

	public Point2D.Double	getCursor()
	{
		if (cursor == null)
		{
			return null;
		}
		else
		{
			return new Point2D.Double(cursor.x, cursor.y);
		}
	}

	public int	getNameCycle()
	{
		return nameCycle;
	}
	
	public int getNodeCycle()
	{
		return nodeCycle;
	}
	
	public double getX()
	{
		return dx;
	}
	
	public double getY()
	{
		return dy;
	}
	
	public double getNodeRadius()
	{
		return nodeRadius;
	}
	
	public int getNodeRotation()
	{
		return nodeRotation;
	}
	
	public double getHullRadius()
	{
		return hullRadius;
	}
	
	public ArrayList<String> getIndividualNames()
	{
		return individualNames;
	}

	//**********************************************************************
	// Public Methods (Modify Variables)
	//**********************************************************************

	public void	setCursorInViewCoordinates(Point q)
	{	// TODO: ADD ACCESS METHODS FOR YOUR ADDITIONAL MEMBERS HERE (AS NEEDED)
		if (q == null)
		{
			view.getCanvas().invoke(false, new BasicUpdater() {
					public void	update(GL2 gl) {
						cursor = null;
					}
				});;
		}
		else
		{
			view.getCanvas().invoke(false, new ViewPointUpdater(q) {
					public void	update(double[] p) {
						cursor = new Point2D.Double(p[0], p[1]);
					}
				});;
		}
	}

	// TODO: ADD MODIFY METHODS FOR YOUR ADDITIONAL MEMBERS HERE (AS NEEDED)
	
	public void increaseNameCycle()
	{
		view.getCanvas().invoke(false, new BasicUpdater() {
			public void update(GL2 gl) {
				nameCycle = nameCycle + 1;
				
				// Bounds check.
				//if (nameCycle > view.getNameListSize() - 1)
				if (nameCycle > names.length - 1)
				{
					nameCycle = nameCycle - 1;
				}
			}
		});;
	}
	
	public void decreaseNameCycle()
	{
		view.getCanvas().invoke(false, new BasicUpdater() {
			public void update(GL2 gl) {
				nameCycle = nameCycle - 1;
				
				// Bounds check.
				if (nameCycle < 0)
				{
					nameCycle = nameCycle + 1;
				}
			}
		});;
	}
	
	// TODO: Implement adding currently selected name to node.
	public void addNode()
	{
		System.out.println("Adding selected name to node.");
	}
	
	// TODO: Implement node deletion.
	public void deleteNode()
	{
		System.out.println("Deleting selected node.");
	}
	
	public void increaseNodeCycle()
	{
		view.getCanvas().invoke(false, new BasicUpdater() {
			public void update(GL2 gl) {
				nodeCycle = nodeCycle + 1;
				
				// Bounds check.
				if (nodeCycle > Network.getAllNames().length - 1)
				{
					nodeCycle = nodeCycle - 1;
				}
			}
		});;
	}
	
	public void decreaseNodeCycle()
	{
		view.getCanvas().invoke(false, new BasicUpdater() {
			public void update(GL2 gl) {
				nodeCycle = nodeCycle - 1;
				
				// Bounds check.
				if (nodeCycle < 0)
				{
					nodeCycle = nodeCycle + 1;
				}
			}
		});;
	}
	
	// Scale nodeRadius by 0.8.
	public void scaleNodeRadius08()
	{
		view.getCanvas().invoke(false, new BasicUpdater() {
			public void update(GL2 gl) {
				nodeRadius *= 0.8;
			}
		});;
	}
		
	// Scale nodeRadius by 1.2.
	public void scaleNodeRadius12()
	{
		view.getCanvas().invoke(false, new BasicUpdater() {
			public void update(GL2 gl) {
				nodeRadius *= 1.2;
			}
		});;
	}
	
	// Decrease node rotation by 15.
	public void rotateNodeByNeg15()
	{
		view.getCanvas().invoke(false, new BasicUpdater() {
			public void update(GL2 gl) {
				nodeRotation -= 15;
			}
		});;
	}
	
	// Increase node rotation by 15.
	public void rotateNodeByPos15()
	{
		view.getCanvas().invoke(false, new BasicUpdater() {
			public void update(GL2 gl) {
				nodeRotation += 15;
			}
		});;
	}
	
	public void decreaseHullRadius()
	{
		view.getCanvas().invoke(false, new BasicUpdater() {
			public void update(GL2 gl) {
				hullRadius -= 0.1;
				
				// Ensuring hullRadius is never negative.
				if (hullRadius < 0)
				{
					hullRadius += 0.1;
				}
			}
		});;
	}
	
	public void increaseHullRadius()
	{
		view.getCanvas().invoke(false, new BasicUpdater() {
			public void update(GL2 gl) {
				hullRadius += 0.1;
			}
		});;
	}
	
	/** Below are the methods to translate XY coordinates of selected nodes. */
	public void moveLeft()
	{
		view.getCanvas().invoke(false, new BasicUpdater() {
			public void update(GL2 gl) {
				dx -= 5.0;
			}
		});;
	}
	
	public void moveRight()
	{
		view.getCanvas().invoke(false, new BasicUpdater() {
			public void update(GL2 gl) {
				dx += 5.0;
			}
		});;
	}
	
	public void moveDown()
	{
		view.getCanvas().invoke(false, new BasicUpdater() {
			public void update(GL2 gl) {
				dy -= 5.0;
			}
		});;
	}
	
	public void moveUp()
	{
		view.getCanvas().invoke(false, new BasicUpdater() {
			public void update(GL2 gl) {
				dy += 5.0;
			}
		});;
	}

	//**********************************************************************
	// Public Methods (Special)
	//**********************************************************************

	public void	selectNodeInViewCoordinates(Point q)
	{
		view.getCanvas().invoke(false, new ViewPointUpdater(q) {
			public void	update(double[] p) {
				Point2D.Double	r = new Point2D.Double(p[0], p[1]);

				view.selectNodeInSceneCoordinates(r);
			}
		});;
	}

	//**********************************************************************
	// Inner Classes
	//**********************************************************************

	// Convenience class to simplify the implementation of most updater.
	private abstract class BasicUpdater implements GLRunnable
	{
		public final boolean	run(GLAutoDrawable drawable)
		{
			GL2	gl = drawable.getGL().getGL2();

			update(gl);

			return true;	// Let animator take care of updating the display
		}

		public abstract void	update(GL2 gl);
	}

	// Convenience class to simplify updates in cases in which the input is a
	// single point in view coordinates (integers/pixels).
	private abstract class ViewPointUpdater extends BasicUpdater
	{
		private final Point	q;

		public ViewPointUpdater(Point q)
		{
			this.q = q;
		}

		public final void	update(GL2 gl)
		{
			int		h = view.getHeight();
			double[]	p = Utilities.mapViewToScene(gl, q.x, h - q.y, 0.0);

			update(p);
		}

		public abstract void	update(double[] p);
	}
}

//******************************************************************************

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
	
	// Size of x and y changes with user interaction.
	double								movementStep;
	
	// For user to cycle through name.
	private int							nameCycle;
	// For user to cycle through nodes.
	private int							nodeCycle;
	// For user to scale hull radius.
	private double						hullRadius;
	
	// Array list of transformations that track user interaction with nodes.
	private final ArrayList<Double> dx;
	private final ArrayList<Double> dy;
	private final ArrayList<Double> radius;
	private final ArrayList<Integer> rotation;
	
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
		
		// List of all names.
		names = Network.getAllNames();
		individualNames = new ArrayList<String>();
		for (int i = 0; i < names.length; i++)
		{
			individualNames.add(names[i]);
		}
		
		// Initializing nodes.
		nodes = new ArrayList<String>();
		dx = new ArrayList<Double>();
		dy = new ArrayList<Double>();
		radius = new ArrayList<Double>();
		rotation = new ArrayList<Integer>();
		
		// Size of stepping pressing the arrow keys.
		movementStep = 0.05;
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
	
	public double getHullRadius()
	{
		return hullRadius;
	}
	
	public ArrayList<String> getIndividualNames()
	{
		return individualNames;
	}
	
	public ArrayList<String> getNodes()
	{
		return nodes;
	}
	
	public double getDx(int index)
	{
		return dx.get(index);
	}
	
	public double getDy(int index)
	{
		return dy.get(index);
	}
	
	public double getRadius(int index)
	{
		return radius.get(index);
	}
	
	public int getRotation(int index)
	{
		return rotation.get(index);
	}
	
	//**********************************************************************
	// Public Methods (Modify Variables)
	//**********************************************************************

	public void	setCursorInViewCoordinates(Point q)
	{
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
	
	// Adding currently selected name to node.
	public void addNode()
	{
		view.getCanvas().invoke(false, new BasicUpdater() {
			public void update(GL2 gl) {
				if (!individualNames.isEmpty())
				{
					// Add name to visible nodes.
					nodes.add(individualNames.get(nameCycle));
					
					// Adding default radius.
					radius.add(0.050);
					// Adding default rotation.
					rotation.add(1);
					// Adding default XY coordinates at origin.
					dx.add(0.0);
					dy.add(0.0);
					
					// Since name is visible, remove from names list.
					individualNames.remove(nameCycle);
				}
				else
				{
					return;
				}
			}
		});;
	}
	
	// Delete currently selected node.
	public void deleteNode()
	{
		view.getCanvas().invoke(false, new BasicUpdater() {
			public void update(GL2 gl) {
				// If statements to find bound problems.
				if (nodeCycle > 0)
				{
					nodeCycle = 0;
				}
				else if (nodeCycle >= 26)
				{
					nodeCycle = 25;
				}
				
				// Removing name from visible node and adding it back to names list.
				if (!nodes.isEmpty())
				{
					individualNames.add(nodes.remove(nodeCycle));
					
					// Removing transformations.
					radius.remove(nodeCycle);
					rotation.remove(nodeCycle);
					dx.remove(nodeCycle);
					dy.remove(nodeCycle);
				}
			}
		});;
	}
	
	public void increaseNodeCycle()
	{
		view.getCanvas().invoke(false, new BasicUpdater() {
			public void update(GL2 gl) {
				nodeCycle = nodeCycle + 1;
				
				// Bounds check.
				if (nodeCycle > nodes.size() - 1)
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
				radius.set(nodeCycle, radius.get(nodeCycle) * 0.8);
			}
		});;
	}
		
	// Scale nodeRadius by 1.2.
	public void scaleNodeRadius12()
	{
		view.getCanvas().invoke(false, new BasicUpdater() {
			public void update(GL2 gl) {
				radius.set(nodeCycle, radius.get(nodeCycle) * 1.2);
			}
		});;
	}
	
	// Decrease node rotation by 15.
	public void rotateNodeByNeg15()
	{
		view.getCanvas().invoke(false, new BasicUpdater() {
			public void update(GL2 gl) {
				rotation.set(nodeCycle, rotation.get(nodeCycle) - 15);
			}
		});;
	}
	
	// Increase node rotation by 15.
	public void rotateNodeByPos15()
	{
		view.getCanvas().invoke(false, new BasicUpdater() {
			public void update(GL2 gl) {
				rotation.set(nodeCycle, rotation.get(nodeCycle) + 15);
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
				dx.set(nodeCycle, dx.get(nodeCycle) - movementStep);
			}
		});;
	}
	
	public void moveRight()
	{
		view.getCanvas().invoke(false, new BasicUpdater() {
			public void update(GL2 gl) {
				dx.set(nodeCycle, dx.get(nodeCycle) + movementStep);
			}
		});;
	}
	
	public void moveDown()
	{
		view.getCanvas().invoke(false, new BasicUpdater() {
			public void update(GL2 gl) {
				dy.set(nodeCycle, dy.get(nodeCycle) - movementStep);
			}
		});;
	}
	
	public void moveUp()
	{
		view.getCanvas().invoke(false, new BasicUpdater() {
			public void update(GL2 gl) {
				dy.set(nodeCycle, dy.get(nodeCycle) + movementStep);
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

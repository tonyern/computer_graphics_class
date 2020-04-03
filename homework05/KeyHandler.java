//******************************************************************************
// Copyright (C) 2016-2019 University of Oklahoma Board of Trustees.
//******************************************************************************
// Last modified: Wed Apr 17 23:02:13 2019 by Chris Weaver
//******************************************************************************
// Major Modification History:
//
// 20160225 [weaver]:	Original file.
// 20190227 [weaver]:	Updated to use model and asynchronous event handling.
// 20190318 [weaver]:	Modified for homework04.
//
//******************************************************************************
// Notes:
//
//******************************************************************************

package edu.ou.cs.cg.assignment.homework05;

//import java.lang.*;
import java.awt.Component;
import java.awt.event.*;
import edu.ou.cs.cg.utilities.Utilities;

//******************************************************************************

/**
 * The <CODE>KeyHandler</CODE> class.<P>
 *
 * @author  Chris Weaver
 * @version %I%, %G%
 */
public final class KeyHandler extends KeyAdapter
{
	//**********************************************************************
	// Private Members
	//**********************************************************************

	// State (internal) variables
	@SuppressWarnings("unused")
	private final View	view;
	private final Model	model;

	//**********************************************************************
	// Constructors and Finalize
	//**********************************************************************

	public KeyHandler(View view, Model model)
	{
		this.view = view;
		this.model = model;

		Component	component = view.getCanvas();

		component.addKeyListener(this);
	}

	//**********************************************************************
	// Override Methods (KeyListener)
	//**********************************************************************

	public void	keyPressed(KeyEvent e)
	{
		boolean	b = Utilities.isShiftDown(e);

		switch (e.getKeyCode())
		{
			// Cycles through node one at a time.
			case KeyEvent.VK_COMMA:
				if (!b)
				{
					model.decreaseNodeCycle();
				}
				else
				{
					model.decreaseNameCycle();
				}
				return;
			case KeyEvent.VK_PERIOD:
				if (!b)
				{
					model.increaseNodeCycle();
				}
				else
				{
					model.increaseNameCycle();
				}
				return;
				
			// Enter key adds a node for the currently selected node.
			case KeyEvent.VK_ENTER:
				model.addNode();
				return;
		
			// Delete and D buttons both removes selected node and put it to the back.
			case KeyEvent.VK_DELETE:
				model.deleteNode();
				return;
			case KeyEvent.VK_D:
				model.deleteNode();
				return;
			
			// Move coordinates of selected node.
			case KeyEvent.VK_LEFT:
				model.moveLeft();
				return;
			case KeyEvent.VK_RIGHT:
				model.moveRight();
				return;
			case KeyEvent.VK_UP:
				model.moveUp();
				return;
			case KeyEvent.VK_DOWN:
				model.moveDown();
				return;
			
			// Manipulates the nodes through radius or rotation.
			case KeyEvent.VK_OPEN_BRACKET:
				if (!b)
				{
					model.scaleNodeRadius08();
				}
				else
				{
					model.rotateNodeByNeg15();
				}
				return;
			case KeyEvent.VK_CLOSE_BRACKET:
				if (!b)
				{
					model.scaleNodeRadius12();
				}
				else
				{
					model.rotateNodeByPos15();
				}
				return;
			case KeyEvent.VK_Q:
				model.decreaseHullRadius();
				return;
			case KeyEvent.VK_W:
				model.increaseHullRadius();
				return;
		}
	}
}

//******************************************************************************

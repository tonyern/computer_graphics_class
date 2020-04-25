//******************************************************************************
// Copyright (C) 2016 University of Oklahoma Board of Trustees.
//******************************************************************************
// Last modified: Wed Apr 24 13:54:51 2019 by Chris Weaver
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

package edu.ou.cs.cg.assignment.homework06;

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
	// Constructors and Finalizer
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
			case KeyEvent.VK_UP:
			case KeyEvent.VK_KP_UP:
				// If up key is not down.
				if (!b)
				{
					model.setFocalPointHeight(2.0);
				}
				else
				{
					model.setFocalPointHeight(1.1);
				}
				return;
				
			case KeyEvent.VK_DOWN:
			case KeyEvent.VK_KP_DOWN:
				// If up key is not down.
				if (!b)
				{
					model.setFocalPointHeight(0.5);
				}
				else
				{
					model.setFocalPointHeight(0.9);
				}
				return;
			
			case KeyEvent.VK_LEFT:
			case KeyEvent.VK_KP_LEFT:
				// If left key is not down.
				if (!b)
				{
					model.setSceneRotationAmount(0.1);
				}
				else
				{
					model.setSceneRotationAmount(0.01);
				}
				return;
			
			case KeyEvent.VK_RIGHT:
			case KeyEvent.VK_KP_RIGHT:
				// If right key is not down.
				if (!b)
				{
					model.setSceneRotationAmount(-0.1);
				}
				else
				{
					model.setSceneRotationAmount(-0.01);
				}
				return;
		
			case KeyEvent.VK_PERIOD:
				// If shift is not down.
				if (!b)
				{
					model.setCameraDistance(2.0);
				}
				else
				{
					model.setCameraDistance(1.1);
				}
				return;
			
			case KeyEvent.VK_COMMA:
				// If shift is not down.
				if (!b)
				{
					model.setCameraDistance(0.5);
				}
				else
				{
					model.setCameraDistance(0.9);
				}
				return;
		}
	}
}

//******************************************************************************

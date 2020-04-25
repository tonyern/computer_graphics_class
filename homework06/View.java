//******************************************************************************
// Copyright (C) 2016-2020 University of Oklahoma Board of Trustees.
//******************************************************************************
// Last modified: Thu Apr 16 20:04:56 2020 by Chris Weaver
//******************************************************************************
// Major Modification History:
//
// 20160425 [weaver]:	Original file.
// 20190129 [weaver]:	Updated to JOGL 2.3.2 and cleaned up.
// 20190203 [weaver]:	Additional cleanup and more extensive comments.
// 20200416 [weaver]:	Extensive modifications and additions for HW06.
//
//******************************************************************************
// Notes:
//
// Warning! This code uses depricated features of OpenGL, including immediate
// mode vertex attribute specification, for sake of easier classroom learning.
// See www.khronos.org/opengl/wiki/Legacy_OpenGL
//
//******************************************************************************

package edu.ou.cs.cg.assignment.homework06;

//import java.lang.*;
import java.awt.Font;
//import java.awt.event.*;
//import java.awt.geom.*;
import java.net.URL;
import java.text.DecimalFormat;
import java.util.*;
//import javax.swing.*;
import com.jogamp.opengl.*;
import com.jogamp.opengl.awt.*;
import com.jogamp.opengl.glu.*;
//import com.jogamp.opengl.math.Quaternion;
import com.jogamp.opengl.util.FPSAnimator;
import com.jogamp.opengl.util.awt.TextRenderer;
import com.jogamp.opengl.util.gl2.GLUT;
import com.jogamp.opengl.util.texture.*;
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
	private static final DecimalFormat	FORMAT = new DecimalFormat("0.00");

	public static final GLUT			MYGLUT = new GLUT();
	public static final Random			RANDOM = new Random();

	// Add texture files to images/ then edit the filenames below.
	private static final String		RSRC = "images/";
	private static final String[]		FILENAMES =
	{
		"log-side.jpg",	// Image #0 used to texture the side of the logs
		"grass2.jpg",	// Image #1 used to texture the floor of the sky box.
		"log-ends.jpg", // Image #2 used to texture the ends of the logs.
		"sky3.jpg",		// Image #3 used to texture the sky of the sky box.
		"scary-forest4.jpg", // Image #4 used to texture the background wall as a scary forest.
		"marshmallow.jpg", // Image #5 used to texture the marshmallow.
		"stick-wood.jpg", // Image #6 used to texture the stick that holds the marshmallows.
		"metal-pole.jpeg", // Image #7 used to texture the flag pole.
		"viva-mexico.jpeg" // Image #8 used to texture the wind sock.
	};

	//**********************************************************************
	// Private Members
	//**********************************************************************

	// State (internal) variables
	private final GLJPanel				canvas;
	private int						w;			// Canvas width
	private int						h;			// Canvas height

	private TextRenderer				renderer;

	private final FPSAnimator			animator;
	private int						k;			// Animation counter

	private final Model				model;

	@SuppressWarnings("unused")
	private final KeyHandler			keyHandler;
	@SuppressWarnings("unused")
	private final MouseHandler			mouseHandler;

	private Texture[]					textures;	// Loaded from FILENAMES
	private Node						root;		// Root node of scene graph

	// TODO: Add members to represent your particle systems

	//**********************************************************************
	// Constructors and Finalizer
	//**********************************************************************

	public View(GLJPanel canvas)
	{
		this.canvas = canvas;

		// Initialize rendering
		k = 0;
		canvas.addGLEventListener(this);

		// Initialize model (scene data and parameter manager)
		model = new Model(this);

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
		return k;
	}

	//**********************************************************************
	// Override Methods (GLEventListener)
	//**********************************************************************

	// Called immediately after the GLContext of the GLCanvas is initialized.
	public void	init(GLAutoDrawable drawable)
	{
		w = drawable.getSurfaceWidth();
		h = drawable.getSurfaceHeight();

		renderer = new TextRenderer(new Font("Monospaced", Font.PLAIN, 12),
									true, true);

		initPipeline(drawable);
		initTextures(drawable);

		root = new Node();

		initScene(drawable);
	}

	// Notification to release resources for the GLContext.
	public void	dispose(GLAutoDrawable drawable)
	{
		renderer = null;

		disposeScene(drawable);
	}

	// Called to initiate rendering of each frame into the GLCanvas.
	public void	display(GLAutoDrawable drawable)
	{
		updatePipeline(drawable);

		update(drawable);
		render(drawable);

		GL2	gl = drawable.getGL().getGL2();

		gl.glFlush();							// Finish and display
	}

	// Called during the first repaint after a resize of the GLCanvas.
	public void	reshape(GLAutoDrawable drawable, int x, int y, int w, int h)
	{
		this.w = w;
		this.h = h;
	}

	//**********************************************************************
	// Private Methods (Pipeline)
	//**********************************************************************

	// www.khronos.org/registry/OpenGL-Refpages/gl2.1/xhtml/glLightModel.xml
	// www.khronos.org/registry/OpenGL-Refpages/gl2.1/xhtml/glShadeModel.xml
	// www.khronos.org/registry/OpenGL-Refpages/es2.0/xhtml/glBlendFunc.xml
	private void	initPipeline(GLAutoDrawable drawable)
	{
		GL2	gl = drawable.getGL().getGL2();

		// See com.jogamp.opengl.GL
		gl.glEnable(GL.GL_DEPTH_TEST);		// Turn on depth buffer updates
		gl.glEnable(GL2.GL_LINE_SMOOTH);	// Turn on line anti-aliasing

		// See com.jogamp.opengl.fixedfunc.GLLightingFunc
		gl.glEnable(GL2.GL_LIGHTING);		// Turn on lighting
		gl.glEnable(GL2.GL_NORMALIZE);		// Normalize normals before lighting
		gl.glShadeModel(GL2.GL_SMOOTH);	// Use smooth (Gouraud) shading

		// Adjust global ambient light for a campfire under a starlit sky.
		float[]	ambient = new float[] { 0.4f, 0.4f, 0.05f, 1.0f };

		gl.glLightModelfv(GL2.GL_LIGHT_MODEL_AMBIENT, ambient, 0);
	}

	// Load image files as textures into instances of JOGL's Texture class.
	// www.khronos.org/registry/OpenGL-Refpages/es2.0/xhtml/glTexParameter.xml
	private void	initTextures(GLAutoDrawable drawable)
	{
		GL2	gl = drawable.getGL().getGL2();

		textures = new Texture[FILENAMES.length];

		for (int i=0; i<FILENAMES.length; i++)
		{
			try
			{
				URL	url = View.class.getResource(RSRC + FILENAMES[i]);

				if (url != null)
				{
					// Create the texture from the JPEG file at the URL.
					textures[i] = TextureIO.newTexture(url, false,
													   TextureIO.JPG);

					textures[i].setTexParameteri(gl, GL2.GL_TEXTURE_MIN_FILTER,
												 GL2.GL_LINEAR);
					textures[i].setTexParameteri(gl, GL2.GL_TEXTURE_MAG_FILTER,
												 GL2.GL_LINEAR);
					textures[i].setTexParameteri(gl, GL2.GL_TEXTURE_WRAP_S,
												 GL2.GL_CLAMP_TO_EDGE);
					textures[i].setTexParameteri(gl, GL2.GL_TEXTURE_WRAP_T,
												 GL2.GL_CLAMP_TO_EDGE);
				}
			}
			catch (Exception ex)
			{
				ex.printStackTrace();
				System.exit(1);		// Bail if any image file is missing!
			}
		}
	}

	// www.khronos.org/registry/OpenGL-Refpages/gl2.1/xhtml/glMaterial.xml
	// www.khronos.org/registry/OpenGL-Refpages/gl2.1/xhtml/glMatrixMode.xml
	private void	updatePipeline(GLAutoDrawable drawable)
	{
		GL2		gl = drawable.getGL().getGL2();
		GLU		glu = GLU.createGLU();

		gl.glClearColor(0.0f, 0.0f, 0.0f, 0.0f);	// Black background
		gl.glClear(GL.GL_COLOR_BUFFER_BIT |		// Clear the color buffer
				   GL.GL_DEPTH_BUFFER_BIT);		// Clear the depth buffer

		// ****************************************
		// Zeroth step: Determine which part of the screen/window to draw into
		// ****************************************

		// Let JOGL take care of this (usually)
		//gl.glViewport(0, 0, w, h);					// Full window

		// ****************************************
		// First step: Position and orient the default camera
		// ****************************************

		// Use to adjust the frustum (clipped volume) relative to viewport
		float	aspect = (float) w / (float) h;		// Aspect ratio of viewport

		// Set up a typical perspective projection with 45 degree field-of-view
		// The closest z shown is 0.1, the farthest z shown is 50.0
		gl.glMatrixMode(GL2.GL_PROJECTION);
		gl.glLoadIdentity();
		glu.gluPerspective(45.0f, aspect, 0.1f, 50.0f);

		// ****************************************
		// Second step: Position and orient the actual camera
		// www.opengl.org/archives/resources/faq/technical/viewing.htm
		// ****************************************

		gl.glMatrixMode(GL2.GL_MODELVIEW);
		gl.glLoadIdentity();

		// Place the camera to account for the position of the user and
		// where they're looking along the y axis (above the fire).
		glu.gluLookAt(model.getCameraDistance(), model.getCameraDistance(), 1.0 + model.getCameraDistance(), // Camera/eyes x, y, z
					  model.getFocalPointHeight(), 0.5, model.getFocalPointHeight(), // Focal point x, y, z (origin)
					  0.0, 1.0, 0.0);		// Above the head/"up"

		// ****************************************
		// Third step: position and orient the scene
		// ****************************************

		// TODO: Update the current viewing angle around the campfire based on
		// the interactively set angular rate of change.

		// TODO: Apply the current viewing angles the the rotation amount for
		// the entire scene. Currently rotates the scene by a fixed 30 degrees.
		gl.glRotated(30.0, 0.0, 1.0, 0.0);
		
		// Update scene rotation amount based on the current rotation rate.
	}

	//**********************************************************************
	// Private Methods (Building)
	//**********************************************************************

	public void	initScene(GLAutoDrawable drawable)
	{
		// Set the basic stage for the scene (don't touch these!)
		root.add(new Skycan(textures));				// Add a skycan
		root.add(new Spotlight());					// Add a spotlight

		// Build your scene graph (permanent objects only here)

		float[]	emit = new float[] { 0.8f, 0.6f, 0.0f, 1.0f };
		Log		log1 = new Log(textures, 7, emit);	// Create a log
		log1.setTransform(0.1f, 0.1f, 0.1f,
						  0.1f, 0.1f, 0.4f,
						  1.0f, 0.0f, 0.0f,
						  90.0f);
		
		Log		log2 = new Log(textures, 7, emit);	// Create another log
		log2.setTransform(0.7f, 0.1f, 0.1f,
						  0.1f, 0.1f, 0.4f,
						  1.0f, 0.0f, 0.0f,
						  90.0f);

		Log		log3 = new Log(textures, 7, emit);	// Create another log
		log3.setTransform(0.4f, 0.25f, 0.4f,
						  0.4f, 0.1f, 0.1f,
						  0.0f, 0.0f, 1.0f,
						  90.0f);
		
		Log		log4 = new Log(textures, 7, emit);	// Create another log
		log4.setTransform(0.4f, 0.25f, -0.2f,
						  0.4f, 0.1f, 0.1f,
						  0.0f, 0.0f, 1.0f,
						  90.0f);
		
		Log		log5 = new Log(textures, 7, emit);	// Create another log
		log5.setTransform(0.1f, 0.40f, 0.1f,
						  0.1f, 0.1f, 0.4f,
						  1.0f, 0.0f, 0.0f,
						  90.0f);
		
		Log		log6 = new Log(textures, 7, emit);	// Create another log
		log6.setTransform(0.7f, 0.40f, 0.1f,
						  0.1f, 0.1f, 0.4f,
						  1.0f, 0.0f, 0.0f,
						  90.0f);
		
		// Leg for the flat wood bench.
		Log		seatLeg1 = new Log(textures, 5, emit);
		seatLeg1.setTransform(-0.9f, 0.1f, 0.3f,
				 			0.1f, 0.1f, 0.1f,
				 			0.0f, 0.0f, 1.0f,
				 			90.0f);
		Log		seatLeg2 = new Log(textures, 5, emit);
		seatLeg2.setTransform(-0.9f, 0.1f, -0.3f,
				 			0.1f, 0.1f, 0.1f,
				 			0.0f, 0.0f, 1.0f,
				 			90.0f);
		// Add flat half cylinder on top of the two seat legs.
		Bench	bench1 = new Bench(textures, 360, emit);
		bench1.setTransform(-0.9f, 0.23f, 0.0f,
	 					   0.1f, 0.1f, 0.5f,
	 					   1.0f, 0.0f, 0.0f,
	 					   90.0f);
		
		// Add seating that is an upward log.
		Log		seat3 = new Log(textures, 6, emit);
		seat3.setTransform(-0.5f, 0.1f, -1.2f,
				 			0.1f, 0.1f, 0.1f,
				 			0.0f, 1.0f, 0.0f,
				 			90.0f);
		
		Log		seat4 = new Log(textures, 6, emit);
		seat4.setTransform(0.3f, 0.1f, -1.5f,
				 		   0.1f, 0.1f, 0.1f,
				 		   0.0f, 1.0f, 0.0f,
				 		   90.0f);
		
		Log		seat5 = new Log(textures, 6, emit);
		seat5.setTransform(1.4f, 0.1f, -1.3f,
				 		   0.1f, 0.1f, 0.1f,
				 		   0.0f, 1.0f, 0.0f,
				 		   90.0f);
		
		// Leg for the flat wood bench.
		Log		seatLeg3 = new Log(textures, 5, emit);
		seatLeg3.setTransform(1.5f, 0.1f, -0.3f,
	 						  0.1f, 0.1f, 0.1f,
	 						  0.0f, 0.0f, 1.0f,
	 						  90.0f);
		Log		seatLeg4 = new Log(textures, 5, emit);
		seatLeg4.setTransform(1.5f, 0.1f, 0.3f,
	 						  0.1f, 0.1f, 0.1f,
	 						  0.0f, 0.0f, 1.0f,
	 						  90.0f);
		// Add flat half cylinder on top of the two seat legs.
		Bench	bench2 = new Bench(textures, 360, emit);
		bench2.setTransform(1.5f, 0.23f, 0.0f,
	 					    0.1f, 0.1f, 0.5f,
	 					    1.0f, 0.0f, 0.0f,
	 					    90.0f);
		
		Log		seat8 = new Log(textures, 6, emit);
		seat8.setTransform(0.9f, 0.1f, 1.2f,
				 		   0.1f, 0.1f, 0.1f,
				 		   0.0f, 1.0f, 0.0f,
				 		   90.0f);
		
		Log		seat9 = new Log(textures, 6, emit);
		seat9.setTransform(0.0f, 0.1f, 1.3f,
				 		   0.1f, 0.1f, 0.1f,
				 		   0.0f, 1.0f, 0.0f,
				 		   90.0f);
		
		Thing	thing = new Thing();				// Make example light

		thing.setLight(GL2.GL_LIGHT1);
		thing.setTransform(0.0f, 0.5f, 0.0f,
						   0.05f, 0.05f, 0.05f,
						   0.0f, 1.0f, 0.0f,
						   0.0f);
		
		// Add two marshmallows.
		Marshmallow marshmallow1 = new Marshmallow(textures, 360, emit);
		marshmallow1.setTransform(0.1f, 0.8f, 0.1f,
				 				  0.02f, 0.02f, 0.02f,
				 				  0.0f, 1.0f, 0.0f,
				 				  90.0f);
		
		Marshmallow marshmallow2 = new Marshmallow(textures, 360, emit);
		marshmallow2.setTransform(0.1f, 0.87f, 0.1f,
				 				  0.02f, 0.02f, 0.02f,
				 				  0.0f, 1.0f, 0.0f,
				 				  90.0f);
		
		// Add stick to put marshmallows on.
		Stick stick = new Stick(textures, 360, emit);
		stick.setTransform(0.1f, 0.65f, 0.1f,
				  		   0.001f, 0.3f, 0.001f,
				  		   0.0f, 1.0f, 0.0f,
				  		   90.0f);
		
		// Add flag pole with wind sock.
		Pole flagPole = new Pole(textures, 360, emit);
		flagPole.setTransform(1.0f, 0.1f, -2.0f,
				  		   	  0.02f, 1.5f, 0.02f,
				  		   	  0.0f, 3.0f, 0.0f,
				  		   	  90.0f);
		WindSock windSock = new WindSock(textures, 360, emit);
		windSock.setTransform(1.25f, 1.5f, -2.0f,
		  		   			  0.2f, 0.1f, 0.2f,
		  		   			  0.0f, 0.0f, 4.0f,
		  		   			  90.0f);

		// Add them to the scene graph
		root.add(log1);
		root.add(log2);
		root.add(log3);
		root.add(log4);
		root.add(log5);
		root.add(log6);
		root.add(seatLeg1);
		root.add(seatLeg2);
		root.add(bench1);
		root.add(seat3);
		root.add(seat4);
		root.add(seat5);
		root.add(seatLeg3);
		root.add(seatLeg4);
		root.add(bench2);
		root.add(seat8);
		root.add(seat9);
		root.add(marshmallow1);
		root.add(marshmallow2);
		root.add(stick);
		root.add(flagPole);
		root.add(windSock);
		root.add(thing);
	}

	public void	disposeScene(GLAutoDrawable drawable)
	{
	}

	//**********************************************************************
	// Private Methods (Rendering)
	//**********************************************************************

	// Update the scene model for the current animation frame.
	private void	update(GLAutoDrawable drawable)
	{
		k++;								// Advance animation counter

		GL2	gl = drawable.getGL().getGL2();

		root.update(gl);

		// TODO: Update the object/nodes in your particle systems
	}

	// Render the scene model and display the current animation frame.
	private void	render(GLAutoDrawable drawable)
	{
		GL2	gl = drawable.getGL().getGL2();

		root.enable(gl);				// Turn on lights in scene graph

		// TODO: Enable any light sources in your particle systems

		root.render(gl);				// Draw the scene graph

		// TODO: Draw the object/nodes in your particle systems

		root.disable(gl);				// Turn off lights in scene graph

		// TODO: Disable any light sources in your particle systems

		drawMode(drawable);				// Draw mode text (do this last)
	}

	// Draw text for the various interactively controlled model parameters
	// along the left-hand side of the canvas.
	private void	drawMode(GLAutoDrawable drawable)
	{
		renderer.beginRendering(w, h);

		// Draw all text in white
		renderer.setColor(1.0f, 1.0f, 1.0f, 1.0f);

		// Draw the primary keyboard options from the top of the window down.
		// Each line (including empty ones for spacing) get 12 pixels of space.
		String	sfoo = FORMAT.format(new Double(model.getFoo()));

		renderer.draw("Foo = " + sfoo, 2, h - 12);
		renderer.draw("Camera Distance = " + model.getCameraDistance(), 2, h - 24);
		renderer.draw("Focal Point Height = " + model.getFocalPointHeight(), 2, h - 36);
		renderer.draw("Scene Rotation Amount = " + model.getSceneRotationAmount(), 2, h - 48);
		renderer.draw("Scene Rotation Rate = " + model.getCameraDistance(), 2, h - 60);

		renderer.endRendering();
	}

	//**********************************************************************
	// Private Methods (Skycan)
	//**********************************************************************

	public static final class Skycan extends Node
	{
		private final float[]	ambi = new float[] { 0.7f, 0.7f, 0.7f, 1.0f };
		private final float[]	diff = new float[] { 0.6f, 0.6f, 0.6f, 1.0f };
		private final float[]	spec = new float[] { 1.0f, 1.0f, 1.0f, 1.0f };

		private final Cylinder	skycan;	// Cylinder + ground + sky

		public Skycan(Texture[] textures)
		{
			super(textures,
				  0.0f, 0.0f, 0.0f,
				  4.0f, 1.0f, 4.0f,
				  0.0f, 1.0f, 0.0f,
				  0.0f);

			skycan = new Cylinder(32, 0.0f, 2.0f);
		}

		public void	depict(GL2 gl)
		{
			Lighting.setMaterial(gl, ambi, diff, spec, null, null);
			
			skycan.fill(gl, textures[4]);		// Around the cylinder
			skycan.fillFoot(gl, textures[1]);	// Bottom end of the cylinder
			skycan.fillHead(gl, textures[3]);	// Top end of the cylinder
		}
	}

	//**********************************************************************
	// Private Methods (Spotlight)
	//**********************************************************************

	public static final class Spotlight extends Node
	{
		private float	sdx;		// Running direction x amount
		private float	sdz;		// Running direction y amount
		private float	sdr;		// Running red intensity
		private float	sdg;		// Running green intensity

		public void Spot()
		{
			sdx = 0.0f;
			sdz = 0.0f;
			sdr = 0.85f;
			sdg = 0.75f;
		}

		protected void	change(GL2 gl)
		{
			sdx = cutGaussian(sdx, 0.02f, -0.3f, 0.03f);
			sdz = cutGaussian(sdz, 0.02f, -0.3f, 0.03f);
			sdr = cutGaussian(sdr, 0.01f,  0.7f, 1.00f);
			sdg = cutGaussian(sdg, 0.01f,  0.5f, 1.00f);
		}

		protected void	enableLighting(GL2 gl)
		{
			gl.glEnable(GL2.GL_LIGHT0);

			// Add variable ruddy yellow spotlight pointing up from origin
			// Position, Intensity, Direction, Cutoff, Exponent
			float[]	lp1 = new float[] { 0.0f, 0.0f, 0.0f, 1.0f };
			float[]	li1 = new float[] {  sdr,  sdg, 0.0f, 0.5f };
			float[]	ld1 = new float[] {  sdx, 1.0f,  sdz, 1.0f };
			float[]	lc1 = new float[] { 15.0f };
			float[]	le1 = new float[] { 2f };

			Lighting.setLight(gl, GL2.GL_LIGHT0, lp1, li1, ld1, lc1, le1);
		}

		protected void	disableLighting(GL2 gl)
		{
			gl.glDisable(GL2.GL_LIGHT0);
		}
	}

	//**********************************************************************
	// Public Methods (Helpful Math for Dynamics Calculations)
	//**********************************************************************

	// Generates a random Gaussian, scales it, cuts it in a range.
	public static float	cutGaussian(float v, float s, float lo, float hi)
	{
		float	d = (float)(s * RANDOM.nextGaussian());

		return Math.max(lo, Math.min(v + d, hi));
	}

	// Repeatedly generates random numbers between 0.0 and 1.0 until a number
	// is below the threshold. Returns the count of numbers generated. Useful
	// for simulating natural phenomena that involve cascade effects.
	public static int	randomCascade(double threshold)
	{
		int	n = 0;

		while (RANDOM.nextDouble() > threshold)
			n++;

		return n;
	}

	//**********************************************************************
	// Private Methods (Particles)
	//**********************************************************************
	
	// Wind sock object.
	public static final class WindSock extends Node
	{
		private final int		sides;			// Allow logs to look jagged
		private final float[]	emit;			// Allow logs to glow dimly
		private final Cylinder	cylinder;		// Geometry for the log

		public WindSock(Texture[] textures, int sides, float[] emit)
		{
			super (textures);

			this.sides = sides;
			this.emit = emit;

			cylinder = new Cylinder(sides, -1.0f, 1.0f);
		}

		protected void	change(GL2 gl)
		{
			// The wind sock just sits there, unchanging. Nothing to do...yet?
		}

		public void	depict(GL2 gl)
		{
			Lighting.setMaterial(gl, null, null, null, null, emit);

			cylinder.fill(gl, textures[8]);		// Around sides
			cylinder.fillFoot(gl, textures[8]);	// Bottom end
			cylinder.fillHead(gl, textures[8]);	// Top end
		}
		
		public int getSides()
		{
			return sides;
		}
	}
	
	// Pole object.
	public static final class Pole extends Node
	{
		private final int		sides;			// Allow logs to look jagged
		private final float[]	emit;			// Allow logs to glow dimly

		private final Cylinder	cylinder;		// Geometry for the log

		public Pole(Texture[] textures, int sides, float[] emit)
		{
			super (textures);

			this.sides = sides;
			this.emit = emit;

			cylinder = new Cylinder(sides, -1.0f, 1.0f);
		}

		protected void	change(GL2 gl)
		{
			// The pole just sits there, unchanging. Nothing to do...yet?
		}

		public void	depict(GL2 gl)
		{
			Lighting.setMaterial(gl, null, null, null, null, emit);

			cylinder.fill(gl, textures[7]);		// Around sides
			cylinder.fillFoot(gl, textures[7]);	// Bottom end
			cylinder.fillHead(gl, textures[7]);	// Top end
		}
		
		public int getSides()
		{
			return sides;
		}
	}
	
	// Stick object.
	public static final class Stick extends Node
	{
		private final int		sides;			// Allow logs to look jagged
		private final float[]	emit;			// Allow logs to glow dimly

		private final Cylinder	cylinder;		// Geometry for the log

		public Stick(Texture[] textures, int sides, float[] emit)
		{
			super (textures);

			this.sides = sides;
			this.emit = emit;

			cylinder = new Cylinder(sides, -1.0f, 1.0f);
		}

		protected void	change(GL2 gl)
		{
			// The stick just sits there, unchanging. Nothing to do...yet?
		}

		public void	depict(GL2 gl)
		{
			Lighting.setMaterial(gl, null, null, null, null, emit);

			cylinder.fill(gl, textures[6]);		// Around sides
			cylinder.fillFoot(gl, textures[6]);	// Bottom end
			cylinder.fillHead(gl, textures[6]);	// Top end
		}
		
		public int getSides()
		{
			return sides;
		}
	}
	
	// Marshmallow object.
	public static final class Marshmallow extends Node
	{
		private final int		sides;			// Allow logs to look jagged
		private final float[]	emit;			// Allow logs to glow dimly

		private final Cylinder	cylinder;		// Geometry for the log

		public Marshmallow(Texture[] textures, int sides, float[] emit)
		{
			super (textures);

			this.sides = sides;
			this.emit = emit;

			cylinder = new Cylinder(sides, -1.0f, 1.0f);
		}

		protected void	change(GL2 gl)
		{
			// The marshmallow just sits there, unchanging. Nothing to do...yet?
		}

		public void	depict(GL2 gl)
		{
			Lighting.setMaterial(gl, null, null, null, null, emit);

			cylinder.fill(gl, textures[5]);		// Around sides
			cylinder.fillFoot(gl, textures[5]);	// Bottom end
			cylinder.fillHead(gl, textures[5]);	// Top end
		}
		
		public int getSides()
		{
			return sides;
		}
	}

	public static final class Log extends Node
	{
		private final int		sides;			// Allow logs to look jagged
		private final float[]	emit;			// Allow logs to glow dimly

		private final Cylinder	cylinder;		// Geometry for the log

		public Log(Texture[] textures, int sides, float[] emit)
		{
			super (textures);

			this.sides = sides;
			this.emit = emit;

			cylinder = new Cylinder(sides, -1.0f, 1.0f);
		}

		protected void	change(GL2 gl)
		{
			// The log just sits there, unchanging. Nothing to do...yet?
		}

		public void	depict(GL2 gl)
		{
			Lighting.setMaterial(gl, null, null, null, null, emit);

			cylinder.fill(gl, textures[0]);		// Around sides
			cylinder.fillFoot(gl, textures[2]);	// Bottom end
			cylinder.fillHead(gl, textures[2]);	// Top end
		}
		
		public int getSides()
		{
			return sides;
		}
	}

	public static final class Bench extends Node
	{
		private final int		sides;			// Allow logs to look jagged
		private final float[]	emit;			// Allow logs to glow dimly

		private final Half_Cylinder	cylinder;		// Geometry for the log

		public Bench(Texture[] textures, int sides, float[] emit)
		{
			super (textures);

			this.sides = sides;
			this.emit = emit;

			cylinder = new Half_Cylinder(sides, -1.0f, 1.0f);
		}

		protected void	change(GL2 gl)
		{
			// The bench just sits there, unchanging. Nothing to do...yet?
		}

		public void	depict(GL2 gl)
		{
			Lighting.setMaterial(gl, null, null, null, null, emit);

			cylinder.fill(gl, textures[0]);		// Around sides
			cylinder.fillFoot(gl, textures[2]);	// Bottom end
			cylinder.fillHead(gl, textures[2]);	// Top end
		}
		
		public int getSides()
		{
			return sides;
		}
	}

	// An example of some ascending non-textured spherical thing that is also
	// a glowing light source. Lots of random changes happening each frame!
	public static final class Thing extends Node
	{
		public float		b;	// Emission and diffuse light brightness scalar

		public Thing()
		{
			super(0.4f * (RANDOM.nextFloat() - 0.5f),
				  0.1f * (RANDOM.nextFloat() - 0.5f),
				  0.4f * (RANDOM.nextFloat() - 0.5f),
				  1.0f);
		}

		protected void	change(GL2 gl)
		{
			float	dx = 0.003f * (RANDOM.nextFloat() - 0.5f);
			float	dy = 0.002f + 0.001f * RANDOM.nextFloat();
			float	dz = 0.003f * (RANDOM.nextFloat() - 0.5f);
			float	sf = Math.max(0.0f, s.getX()-0.00005f * RANDOM.nextFloat());

			// Randomly drifts translation and scaling amounts
			d.set(d.getX() + dx, d.getY() + dy, d.getZ() + dz, d.getW());
			s.set(sf, sf, sf, s.getW());

			// Randomly generates new brightness scalar
			b = 0.5f + 0.05f * RANDOM.nextFloat();

			// Teleport back to fire when it gets too high. Stays small, though
			if (d.getY() > 2.0f)
				d.setY(0.0f);
		}

		public void	depict(GL2 gl)
		{
			float[]	emit =
				new float[] { 1.0f * b, 0.8f * b, 0.3f * b, 1.0f };

			Lighting.setMaterial(gl, null, null, null, null, emit);
			MYGLUT.glutSolidSphere(1.0, 8, 8);
		}

		protected void	enableLighting(GL2 gl)
		{
			float[]	lightColor =
				new float[] { 1.0f * b, 0.5f * b, 0.2f * b, 1.0f };

			enableLightDiffuse(gl, lightColor);
		}

		protected void	disableLighting(GL2 gl)
		{
			disableLightDiffuse(gl);
		}
	}
}

//******************************************************************************

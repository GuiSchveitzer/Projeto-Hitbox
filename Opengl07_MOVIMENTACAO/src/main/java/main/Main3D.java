package main;

import org.lwjgl.*;
import org.lwjgl.glfw.*;
import org.lwjgl.opengl.*;
import org.lwjgl.system.*;
import org.lwjgl.system.MemoryUtil;
import org.lwjgl.util.vector.Matrix4f;
import org.lwjgl.util.vector.Vector3f;

import Model.VboCube;
import Model.VboQuad;
import obj.Cubo3D;
import obj.Explosao;
import obj.ObjModel;
import obj.Object3D;
import obj.Personagem;
import obj.Projetil;
import shaders.StaticShader;
import util.GifLoader;
import util.TextureLoader;

import java.awt.image.BufferedImage;

//import com.sun.org.apache.xerces.internal.dom.DeepNodeListImpl;

import java.nio.*;
import java.util.ArrayList;
import java.util.Random;

import static org.lwjgl.glfw.Callbacks.*;
import static org.lwjgl.glfw.GLFW.*;
import static org.lwjgl.opengl.GL11.*;
import static org.lwjgl.opengl.GL13.GL_TEXTURE0;
import static org.lwjgl.opengl.GL13.glActiveTexture;
import static org.lwjgl.opengl.GL20.glGetUniformLocation;
import static org.lwjgl.opengl.GL20.glUniform1i;
import static org.lwjgl.opengl.GL20.glUniform4f;
import static org.lwjgl.opengl.GL20.glUniform1f;
import static org.lwjgl.opengl.GL20.glUniformMatrix4fv;
import static org.lwjgl.system.MemoryStack.*;
import static org.lwjgl.system.MemoryUtil.*;

public class Main3D {

	// The window handle
	private long window;
	
	float viewAngX = 0;
	float viewAngY = 0;
	float scale = 1.0f;
	
	public Random rnd = new Random();
	
	VboCube vboc;
	StaticShader shader;
	
	public static ArrayList<Object3D> listaObjetos = new ArrayList<>();
	public static ArrayList<Object3D> listaProjeteis = new ArrayList<>();
	public static Personagem personagem;
	public static VboCube vbocube = null;
	
	public static int ttverde = -1;

	// explosoes (quando um tiro acerta um cubo)
	public static ArrayList<Explosao> listaExplosoes = new ArrayList<>();
	public static VboQuad vboquad = null;
	public static int[] texExplosao = new int[0];


	public void run() {
		System.out.println("Hello LWJGL " + Version.getVersion() + "!");

		init();
		loop();

		// Free the window callbacks and destroy the window
		glfwFreeCallbacks(window);
		glfwDestroyWindow(window);

		// Terminate GLFW and free the error callback
		glfwTerminate();
		glfwSetErrorCallback(null).free();
	}

	private void init() {
		// Setup an error callback. The default implementation
		// will print the error message in System.err.
		GLFWErrorCallback.createPrint(System.err).set();

		// Initialize GLFW. Most GLFW functions will not work before doing this.
		if (!glfwInit())
			throw new IllegalStateException("Unable to initialize GLFW");

		// Configure GLFW
		glfwDefaultWindowHints(); // optional, the current window hints are already the default
		glfwWindowHint(GLFW_VISIBLE, GLFW_FALSE); // the window will stay hidden after creation
		glfwWindowHint(GLFW_RESIZABLE, GLFW_TRUE); // the window will be resizable

		// Create the window
		window = glfwCreateWindow(1500, 1000, "Hello World!", NULL, NULL);
		if (window == NULL)
			throw new RuntimeException("Failed to create the GLFW window");

		// Setup a key callback. It will be called every time a key is pressed, repeated
		// or released.
		
		glfwSetKeyCallback(window, (window, key, scancode, action, mods) -> {
			if (key == GLFW_KEY_ESCAPE && action == GLFW_RELEASE)
				glfwSetWindowShouldClose(window, true); // We will detect this in the rendering loop
		
			if(action == GLFW_PRESS) {
				if ( key == GLFW_KEY_W) {
					//viewAngX+=5;
					personagem.UP = true;
				}
				if ( key == GLFW_KEY_S) {
					//viewAngX-=5;
					personagem.DOWN = true;
				}
				if ( key == GLFW_KEY_A) {
					//viewAngY+=5;
					personagem.LEFT = true;
				}
				if ( key == GLFW_KEY_D) {
					//viewAngY-=5;
					personagem.RIGHT = true;
				}
				if ( key == GLFW_KEY_SPACE) {
					//viewAngY-=5;
					personagem.SHOOT = true;
				}
				
				if ( key == GLFW_KEY_Z) {
					scale = scale*1.1f;
				}
				if ( key == GLFW_KEY_X) {
					scale = scale*0.9f;
				}
			}
			
			if(action == GLFW_RELEASE) {
				if ( key == GLFW_KEY_W) {
					//viewAngX+=5;
					personagem.UP = false;
					//System.out.println("personagem.UP"+personagem.UP);
				}
				if ( key == GLFW_KEY_S) {
					//viewAngX-=5;
					personagem.DOWN = false;
				}
				if ( key == GLFW_KEY_A) {
					//viewAngY+=5;
					personagem.LEFT = false;
				}
				if ( key == GLFW_KEY_D) {
					//viewAngY-=5;
					personagem.RIGHT = false;
				}
				
				if ( key == GLFW_KEY_Z) {
					scale = scale*1.1f;
				}
				if ( key == GLFW_KEY_X) {
					scale = scale*0.9f;
				}
				
				if ( key == GLFW_KEY_SPACE) {
					personagem.SHOOT = false;
				}
			}
		
		});

		// Get the thread stack and push a new frame
		try (MemoryStack stack = stackPush()) {
			IntBuffer pWidth = stack.mallocInt(1); // int*
			IntBuffer pHeight = stack.mallocInt(1); // int*

			// Get the window size passed to glfwCreateWindow
			glfwGetWindowSize(window, pWidth, pHeight);

			// Get the resolution of the primary monitor
			GLFWVidMode vidmode = glfwGetVideoMode(glfwGetPrimaryMonitor());

			// Center the window
			glfwSetWindowPos(window, (vidmode.width() - pWidth.get(0)) / 2, (vidmode.height() - pHeight.get(0)) / 2);
		} // the stack frame is popped automatically

		// Make the OpenGL context current
		glfwMakeContextCurrent(window);
		// Enable v-sync
		glfwSwapInterval(1);

		// Make the window visible
		glfwShowWindow(window);
	}

	private void loop() {
		// This line is critical for LWJGL's interoperation with GLFW's
		// OpenGL context, or any context that is managed externally.
		// LWJGL detects the context that is current in the current thread,
		// creates the GLCapabilities instance and makes the OpenGL
		// bindings available for use.

		GL.createCapabilities();

		

		vboc = new VboCube();
		vboc.load();
		shader = new StaticShader();
		
		//Cubo3D cubo = new Cubo3D(0.0f, 0.0f, -1.0f, 0.2f);
		//cubo.vbocube = vboc;
		
		//ObjModel x35 = new ObjModel();
		//x35.loadObj("x-35_obj.obj");
		//x35.load();
		
		System.out.println("f104starfighter.obj");
		ObjModel f101 = new ObjModel();
		f101.loadObj("x-35_obj.obj"); // arquivo esta na pasta do projeto (antes: ..\\objs\\)
		f101.load();
		System.out.println("Faces: "+f101.f.size());

		// SR71.obj nao existe neste projeto e nao era usado
		//ObjModel sr71 = new ObjModel();
		//sr71.loadObj("..\\objs\\SR71.obj");
		//sr71.load();

		vbocube = new VboCube();
		vbocube.load();

		vboquad = new VboQuad();
		vboquad.load();
		
		for(int i = 0; i < 1000; i++) {
			//Cubo3D cubo = new Cubo3D(rnd.nextFloat()*2-1,rnd.nextFloat()*2-1, rnd.nextFloat()*2-1, rnd.nextFloat()*0.005f+0.0001f);
			//cubo.model = x35;
			Cubo3D cubo = new Cubo3D(rnd.nextFloat()*20-10,rnd.nextFloat()*20-10, rnd.nextFloat()*20-10, 0.1f);
			cubo.model = vbocube;
			cubo.vx = 0;
			cubo.vy = 0;
			cubo.vz = 0;
			cubo.rotvel = rnd.nextFloat()*9;
			listaObjetos.add(cubo);
		}
		
		personagem = new Personagem(0, 0, 0, 0.01f);
		personagem.model = f101;
		
		
		BufferedImage imggato = TextureLoader.loadImage("texturaGato.jpeg");
		BufferedImage gatorgba = new BufferedImage(imggato.getWidth(), imggato.getHeight(), BufferedImage.TYPE_INT_ARGB);
		gatorgba.getGraphics().drawImage(imggato, 0, 0, null);
		int tgato = TextureLoader.loadTexture(imggato);
		System.out.println("tgato "+tgato);
		BufferedImage imgcube = TextureLoader.loadImage("texturecube.png");
		int tcube = TextureLoader.loadTexture(imgcube);
		
		BufferedImage imgtverde = TextureLoader.loadImage("tiro_verde.png");
		ttverde = TextureLoader.loadTexture(imgtverde);

		// cada frame do GIF vira uma textura.
		// O explode.gif comeca no meio da explosao: o "flash" inicial e o frame 8,
		// entao tocamos 8..16 e depois 0..7.
		BufferedImage[] framesExplosao = GifLoader.loadFrames("explode.gif");
		int inicio = framesExplosao.length > 8 ? 8 : 0;
		texExplosao = new int[framesExplosao.length];
		for (int i = 0; i < framesExplosao.length; i++) {
			texExplosao[i] = TextureLoader.loadTexture(framesExplosao[(inicio + i) % framesExplosao.length]);
		}
		System.out.println("Frames da explosao: " + texExplosao.length);

		
		// Run the rendering loop until the user has attempted to close
		// the window or has pressed the ESCAPE key.

		int frame = 0;
		long lasttime = System.currentTimeMillis();

		float angle = 0;
		
		glMatrixMode(GL_PROJECTION);
		glLoadIdentity();
		gluPerspective(45, 600f / 800f, 0.5f, 100);
		glMatrixMode(GL_MODELVIEW);
		glLoadIdentity();
		
		FloatBuffer matrixBuffer = MemoryUtil.memAllocFloat(16);
		long ultimoTempo = System.currentTimeMillis();
		while (!glfwWindowShouldClose(window)) {
			
			long diftime = System.currentTimeMillis()-ultimoTempo;
			ultimoTempo = System.currentTimeMillis();
			
			for(int i = 0; i < listaProjeteis.size();i++) {
				Projetil proj = (Projetil)listaProjeteis.get(i);
				proj.SimulaSe(diftime);
				if(proj.vivo==false) {
					listaProjeteis.remove(i);
					i--;
				}
				
			}
			
			for(int i = 0; i < listaObjetos.size();i++) {
				listaObjetos.get(i).SimulaSe(diftime);
			}
			personagem.SimulaSe(diftime);

			// Colisao tiro x cubo: se acertou, remove os dois e cria uma explosao no lugar do cubo
			for(int i = 0; i < listaProjeteis.size();i++) {
				Object3D proj = listaProjeteis.get(i);
				for(int j = 0; j < listaObjetos.size();j++) {
					Object3D cubo = listaObjetos.get(j);
					if(proj.colide(cubo)) {
						listaExplosoes.add(new Explosao(cubo.x, cubo.y, cubo.z, 0.4f));
						listaObjetos.remove(j);
						listaProjeteis.remove(i);
						i--;
						break; // este tiro ja foi usado
					}
				}
			}

			for(int i = 0; i < listaExplosoes.size();i++) {
				Explosao exp = listaExplosoes.get(i);
				exp.SimulaSe(diftime);
				if(exp.vivo==false) {
					listaExplosoes.remove(i);
					i--;
				}
			}

			angle+=0.1;
			
			glClear(GL_COLOR_BUFFER_BIT | GL_DEPTH_BUFFER_BIT); // clear the framebuffer

			glEnable(GL_LIGHTING);
			glShadeModel(GL_SMOOTH);

			//float senoang = (float) Math.sin(Math.toRadians(angle));
			//float cosang = (float) Math.cos(Math.toRadians(angle));
			glLoadIdentity();

			float[] lightAmbient = { 0.1f, 0.1f, 0.1f, 0.5f };
			float[] lightDiffuse = { 0.5f, 0.5f, 0.5f, 0.5f };
			float[] lightPosition = { 0.0f, 0.0f, 0.0f, 1.0f };
			//float[] lightPosition = { 0.0f, senoang * 600f, cosang * 600f, 1.0f };

			glLightfv(GL_LIGHT0, GL_AMBIENT, lightAmbient);
			glLightfv(GL_LIGHT0, GL_DIFFUSE, lightDiffuse);
			glLightfv(GL_LIGHT0, GL_POSITION, lightPosition);

			float[] lightAmbient2 = { 0.0f, 0.0f, 0.0f, 1.0f };
			float[] lightDiffuse2 = { 1.0f, 1.0f, 1.0f, 1.0f };
			float[] lightPosition2 = { -5f, -5f, 0f, 1.0f };

			shader.start();
			int modellocation = glGetUniformLocation(shader.programID, "model");
			
			glEnable(GL_DEPTH_TEST);
			

	        
	        int projectionloc = glGetUniformLocation(shader.programID, "projection");
	        //System.out.println("projectionloc "+projectionloc);
	        
	        float fov    = (float) Math.toRadians(60.0f); // vertical FOV, in radians
	        float aspect = (float) 1500 / 1000;
	        float zNear  = 0.01f;
	        float zFar   = 1000.0f;

	        Matrix4f projection = setFrustum(-0.11f,0.11f,-0.11f,0.11f,0.1f,100.0f);
	        projection.storeTranspose(matrixBuffer);
			matrixBuffer.flip();
	        glUniformMatrix4fv(projectionloc, false, matrixBuffer);

	        
	        int loctexture = glGetUniformLocation(shader.programID, "tex");
	        glUniform1i(loctexture, 0);
			
			
			Matrix4f view = new Matrix4f();
			view.setIdentity();
			
			
			view.scale(new Vector3f(scale,scale,scale));
			view.rotate(viewAngX*0.0174532f, new Vector3f(1,0,0));
			view.rotate(viewAngY*0.0174532f, new Vector3f(0,1,0));
			view.translate(new Vector3f(-personagem.x,-personagem.y-0.5f,-personagem.z-1.5f));
			
			
			int viewlocation = glGetUniformLocation(shader.programID, "view");
			int corgeralloc = glGetUniformLocation(shader.programID, "corgeral");
			int angpos = glGetUniformLocation(shader.programID, "ang");
			
			view.storeTranspose(matrixBuffer);
			matrixBuffer.flip();
			glUniformMatrix4fv(viewlocation, false, matrixBuffer);
			
			glUniform4f(corgeralloc, .5f, .5f, .5f, 1.0f);
			glUniform1f(angpos, angle);
			
			Matrix4f modelmS = new Matrix4f();
			modelmS.setIdentity();
			
			for(int i = 0; i < listaProjeteis.size();i++) {
				listaProjeteis.get(i).DesenhaSe(shader);
			}
	        
			glActiveTexture(GL_TEXTURE0);
	        //glBindTexture(GL_TEXTURE_2D, tgato);
	        glBindTexture(GL_TEXTURE_2D, tcube);
	        
			for(int i = 0; i < listaObjetos.size();i++) {
				listaObjetos.get(i).DesenhaSe(shader);
			}
			
	        glActiveTexture(GL_TEXTURE0);
	        glBindTexture(GL_TEXTURE_2D, tgato);
			
			personagem.DesenhaSe(shader);

			// Explosoes por ultimo, com transparencia (alpha do GIF).
			// glDepthMask(false): o fundo transparente do sprite nao esconde o que esta atras.
			glEnable(GL_BLEND);
			glBlendFunc(GL_SRC_ALPHA, GL_ONE_MINUS_SRC_ALPHA);
			glDepthMask(false);
			for(int i = 0; i < listaExplosoes.size();i++) {
				listaExplosoes.get(i).DesenhaSe(shader);
			}
			glDepthMask(true);
			glDisable(GL_BLEND);

			/*
			Matrix4f modelm = new Matrix4f();
			modelm.setIdentity();
			
			//System.out.println(""+x+" "+y+" "+z);
			
			modelm.translate(new Vector3f(0,-0.5f,-1.2f));
			modelm.scale(new Vector3f(0.01f,0.01f,0.01f));
			modelm.rotate((float)Math.PI,new Vector3f(0,1,0));
			
			//modelm.rotate(ang,new Vector3f(0.0f,1.0f,0.0f));
			
			/*
			modelm.storeTranspose(matrixBuffer);
			matrixBuffer.flip();
			glUniformMatrix4fv(modellocation, false, matrixBuffer);
			
			*/
			
			
			//System.out.println("corgeralloc "+corgeralloc);
			//glUniform4f(corgeralloc, .5f, 0.8f, 0.5f, 1.0f);
			
	        //glActiveTexture(GL_TEXTURE0);
	        //glBindTexture(GL_TEXTURE_2D, tgato);
			
			//f101.draw();
			
			
			
			shader.stop();
			
			glfwSwapBuffers(window); // swap the color buffers

			// Poll for window events. The key callback above will only be
			// invoked during this call.
			glfwPollEvents();
			frame++;
			long actualTime = System.currentTimeMillis();
			if ((lasttime / 1000) != (actualTime / 1000)) {
				System.out.println("FPS " + frame);
				frame = 0;
				lasttime = actualTime;
			}

		}
	}


	public static void main(String[] args) {
		new Main3D().run();
	}

	public static void gluPerspective(float fovy, float aspect, float near, float far) {
		float bottom = -near * (float) Math.tan(fovy / 2);
		float top = -bottom;
		float left = aspect * bottom;
		float right = -left;
		glFrustum(left, right, bottom, top, near, far);
	}
	
	public static Matrix4f setFrustum(float l, float r, float b, float t, float n, float f)
	{
		Matrix4f m = new Matrix4f();
		m.m00 = 2 * n / (r - l);
		m.m01 = 0.0f;
		m.m02 = 0.0f;
		m.m03 = 0.0f;
		
		m.m10 = 0.0f;
		m.m11 = 2 * n / (t - b);
		m.m12 = 0.0f;
		m.m13 = 0.0f;
		
		m.m20 = (r + l) / (r - l);
		m.m21 = (t + b) / (t - b);
		m.m22 = -(f + n) / (f - n);
		m.m23 = -1;
		
		m.m30 = 0.0f;
		m.m31 = 0.0f;
		m.m32 = -(2 * f * n) / (f - n);
		m.m33 = 0;

		return m;
	}
	

}

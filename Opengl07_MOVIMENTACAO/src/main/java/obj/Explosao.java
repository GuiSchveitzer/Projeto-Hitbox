package obj;

import static org.lwjgl.opengl.GL11.*;
import static org.lwjgl.opengl.GL13.GL_TEXTURE0;
import static org.lwjgl.opengl.GL13.glActiveTexture;
import static org.lwjgl.opengl.GL20.glGetUniformLocation;
import static org.lwjgl.opengl.GL20.glUniformMatrix4fv;

import java.nio.FloatBuffer;

import org.lwjgl.system.MemoryUtil;
import org.lwjgl.util.vector.Matrix4f;
import org.lwjgl.util.vector.Vector3f;

import Model.Model;
import main.Main3D;
import shaders.ShaderProgram;

// Explosao animada: um quadrado virado para a camera que troca de textura
// a cada TEMPO_FRAME ms (um frame do explode.gif por vez).
public class Explosao extends Object3D {
	public static final int TEMPO_FRAME = 50; // ms por frame

	public Model model = null;
	public int[] texturas;
	public boolean vivo = true;

	FloatBuffer matrixBuffer = MemoryUtil.memAllocFloat(16);
	long timer = 0;
	int frame = 0;

	public Explosao(float x, float y, float z, float r) {
		super(x, y, z);
		raio = r;
		model = Main3D.vboquad;
		texturas = Main3D.texExplosao;
	}

	@Override
	public void SimulaSe(long diftime) {
		timer += diftime;
		frame = (int) (timer / TEMPO_FRAME);
		if (frame >= texturas.length) {
			vivo = false; // acabou a animacao
		}
	}

	@Override
	public void DesenhaSe(ShaderProgram shader) {
		if (!vivo || texturas.length == 0) {
			return;
		}

		glActiveTexture(GL_TEXTURE0);
		glBindTexture(GL_TEXTURE_2D, texturas[frame]);

		Matrix4f modelm = new Matrix4f();
		modelm.setIdentity();
		modelm.translate(new Vector3f(x, y, z));
		modelm.scale(new Vector3f(raio, raio, raio));

		int modellocation = glGetUniformLocation(shader.programID, "model");
		modelm.storeTranspose(matrixBuffer);
		matrixBuffer.flip();
		glUniformMatrix4fv(modellocation, false, matrixBuffer);

		model.draw();
	}
}

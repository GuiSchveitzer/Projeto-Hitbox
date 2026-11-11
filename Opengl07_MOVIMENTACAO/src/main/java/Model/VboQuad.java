package Model;

import static org.lwjgl.opengl.GL20.*;

import java.nio.FloatBuffer;

import org.lwjgl.BufferUtils;

// Quadrado no plano XY (de -1 a 1), virado para a camera.
// Usado para desenhar a explosao (sprite do GIF).
public class VboQuad extends Model {
	int vbo_vertex_handle;
	int vbo_color_handle;
	int vbo_texture_handle;

	int vertices = 6;

	@Override
	public void load() {
		// 2 triangulos: inferior-esquerdo, inferior-direito, superior-direito / superior-direito, superior-esquerdo, inferior-esquerdo
		float[] pos = {
			-1f, -1f, 0f,   1f, -1f, 0f,   1f,  1f, 0f,
			 1f,  1f, 0f,  -1f,  1f, 0f,  -1f, -1f, 0f,
		};
		float[] cor = new float[vertices * 3];
		java.util.Arrays.fill(cor, 1f);

		// O vertex shader faz uv = (textuv.y, 1 - textuv.x),
		// entao para a imagem aparecer em pe usamos textuv = ((y+1)/2, (x+1)/2)
		float[] uv = new float[vertices * 2];
		for (int i = 0; i < vertices; i++) {
			float x = pos[i * 3];
			float y = pos[i * 3 + 1];
			uv[i * 2] = (y + 1) / 2;
			uv[i * 2 + 1] = (x + 1) / 2;
		}

		vbo_vertex_handle = criaVbo(pos);
		vbo_color_handle = criaVbo(cor);
		vbo_texture_handle = criaVbo(uv);
	}

	private int criaVbo(float[] dados) {
		FloatBuffer buffer = BufferUtils.createFloatBuffer(dados.length);
		buffer.put(dados).flip();
		int handle = glGenBuffers();
		glBindBuffer(GL_ARRAY_BUFFER, handle);
		glBufferData(GL_ARRAY_BUFFER, buffer, GL_STATIC_DRAW);
		glBindBuffer(GL_ARRAY_BUFFER, 0);
		return handle;
	}

	@Override
	public void draw() {
		glBindBuffer(GL_ARRAY_BUFFER, vbo_vertex_handle);
		glEnableVertexAttribArray(0);
		glVertexAttribPointer(0, 3, GL_FLOAT, false, 0, 0);

		glBindBuffer(GL_ARRAY_BUFFER, vbo_color_handle);
		glEnableVertexAttribArray(1);
		glVertexAttribPointer(1, 3, GL_FLOAT, false, 0, 0);

		glBindBuffer(GL_ARRAY_BUFFER, vbo_texture_handle);
		glEnableVertexAttribArray(3);
		glVertexAttribPointer(3, 2, GL_FLOAT, false, 0, 0);

		glDrawArrays(GL_TRIANGLES, 0, vertices);
	}
}

package obj;

import shaders.ShaderProgram;

public class Object3D {
	public float x;
	public float y;
	public float z;

	public float vx = 0;
	public float vy = 0;
	public float vz = 0;

	float raio = 0;

	// raio da esfera usada como hitbox (ver colide())
	public float raioColisao = 0;

	public Object3D(float x, float y, float z) {
		super();
		this.x = x;
		this.y = y;
		this.z = z;
	}

	public void DesenhaSe(ShaderProgram shader) {

	};

	public void SimulaSe(long diftime) {

	};

	// Colisao esfera x esfera:
	// d = sqrt((x1-x2)^2 + (y1-y2)^2 + (z1-z2)^2)
	// colide quando d < r1 + r2
	public boolean colide(Object3D outro) {
		float dx = x - outro.x;
		float dy = y - outro.y;
		float dz = z - outro.z;
		float d = (float) Math.sqrt(dx*dx + dy*dy + dz*dz);
		return d < raioColisao + outro.raioColisao;
	}

}

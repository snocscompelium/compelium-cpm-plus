package com.compelium.cpmplus.effects;

/**
 * Small 4x4 matrix helpers. Matrices are stored column by column, like OpenGL and JOML:
 * index 12, 13 and 14 hold the position.
 */
final class Mat {
	private Mat() {
	}

	static double[] fromColumnMajor(float[] m) {
		double[] out = new double[16];
		for (int i = 0; i < 16; i++) out[i] = m[i];
		return out;
	}

	/** CPM stores its matrices row by row. */
	static double[] fromRowMajor(float[] m) {
		double[] out = new double[16];
		for (int r = 0; r < 4; r++) {
			for (int c = 0; c < 4; c++) {
				out[c * 4 + r] = m[r * 4 + c];
			}
		}
		return out;
	}

	static double[] mul(double[] a, double[] b) {
		double[] out = new double[16];
		for (int c = 0; c < 4; c++) {
			for (int r = 0; r < 4; r++) {
				double s = 0;
				for (int k = 0; k < 4; k++) s += a[k * 4 + r] * b[c * 4 + k];
				out[c * 4 + r] = s;
			}
		}
		return out;
	}

	static float[] toFloat(double[] m) {
		float[] out = new float[m.length];
		for (int i = 0; i < m.length; i++) out[i] = (float) m[i];
		return out;
	}

	static boolean isIdentity(float[] m, double eps) {
		for (int i = 0; i < 16; i++) {
			double want = (i % 5 == 0) ? 1 : 0;
			if (Math.abs(m[i] - want) > eps) return false;
		}
		return true;
	}

	/** The inverse of a 4x4 matrix, or null when it has none. */
	static double[] invert(double[] m) {
		double[] inv = new double[16];
		inv[0] = m[5] * m[10] * m[15] - m[5] * m[11] * m[14] - m[9] * m[6] * m[15] + m[9] * m[7] * m[14] + m[13] * m[6] * m[11] - m[13] * m[7] * m[10];
		inv[4] = -m[4] * m[10] * m[15] + m[4] * m[11] * m[14] + m[8] * m[6] * m[15] - m[8] * m[7] * m[14] - m[12] * m[6] * m[11] + m[12] * m[7] * m[10];
		inv[8] = m[4] * m[9] * m[15] - m[4] * m[11] * m[13] - m[8] * m[5] * m[15] + m[8] * m[7] * m[13] + m[12] * m[5] * m[11] - m[12] * m[7] * m[9];
		inv[12] = -m[4] * m[9] * m[14] + m[4] * m[10] * m[13] + m[8] * m[5] * m[14] - m[8] * m[6] * m[13] - m[12] * m[5] * m[10] + m[12] * m[6] * m[9];
		inv[1] = -m[1] * m[10] * m[15] + m[1] * m[11] * m[14] + m[9] * m[2] * m[15] - m[9] * m[3] * m[14] - m[13] * m[2] * m[11] + m[13] * m[3] * m[10];
		inv[5] = m[0] * m[10] * m[15] - m[0] * m[11] * m[14] - m[8] * m[2] * m[15] + m[8] * m[3] * m[14] + m[12] * m[2] * m[11] - m[12] * m[3] * m[10];
		inv[9] = -m[0] * m[9] * m[15] + m[0] * m[11] * m[13] + m[8] * m[1] * m[15] - m[8] * m[3] * m[13] - m[12] * m[1] * m[11] + m[12] * m[3] * m[9];
		inv[13] = m[0] * m[9] * m[14] - m[0] * m[10] * m[13] - m[8] * m[1] * m[14] + m[8] * m[2] * m[13] + m[12] * m[1] * m[10] - m[12] * m[2] * m[9];
		inv[2] = m[1] * m[6] * m[15] - m[1] * m[7] * m[14] - m[5] * m[2] * m[15] + m[5] * m[3] * m[14] + m[13] * m[2] * m[7] - m[13] * m[3] * m[6];
		inv[6] = -m[0] * m[6] * m[15] + m[0] * m[7] * m[14] + m[4] * m[2] * m[15] - m[4] * m[3] * m[14] - m[12] * m[2] * m[7] + m[12] * m[3] * m[6];
		inv[10] = m[0] * m[5] * m[15] - m[0] * m[7] * m[13] - m[4] * m[1] * m[15] + m[4] * m[3] * m[13] + m[12] * m[1] * m[7] - m[12] * m[3] * m[5];
		inv[14] = -m[0] * m[5] * m[14] + m[0] * m[6] * m[13] + m[4] * m[1] * m[14] - m[4] * m[2] * m[13] - m[12] * m[1] * m[6] + m[12] * m[2] * m[5];
		inv[3] = -m[1] * m[6] * m[11] + m[1] * m[7] * m[10] + m[5] * m[2] * m[11] - m[5] * m[3] * m[10] - m[9] * m[2] * m[7] + m[9] * m[3] * m[6];
		inv[7] = m[0] * m[6] * m[11] - m[0] * m[7] * m[10] - m[4] * m[2] * m[11] + m[4] * m[3] * m[10] + m[8] * m[2] * m[7] - m[8] * m[3] * m[6];
		inv[11] = -m[0] * m[5] * m[11] + m[0] * m[7] * m[9] + m[4] * m[1] * m[11] - m[4] * m[3] * m[9] - m[8] * m[1] * m[7] + m[8] * m[3] * m[5];
		inv[15] = m[0] * m[5] * m[10] - m[0] * m[6] * m[9] - m[4] * m[1] * m[10] + m[4] * m[2] * m[9] + m[8] * m[1] * m[6] - m[8] * m[2] * m[5];
		double det = m[0] * inv[0] + m[1] * inv[4] + m[2] * inv[8] + m[3] * inv[12];
		if (Math.abs(det) < 1e-12 || Double.isNaN(det)) return null;
		double d = 1.0 / det;
		for (int i = 0; i < 16; i++) inv[i] *= d;
		return inv;
	}

	static double[] normalize(double x, double y, double z) {
		double l = Math.sqrt(x * x + y * y + z * z);
		if (l < 1e-9) return new double[] {0, 1, 0};
		return new double[] {x / l, y / l, z / l};
	}

	static double[] normalize(double[] v) {
		return normalize(v[0], v[1], v[2]);
	}

	static double[] cross(double[] a, double[] b) {
		return new double[] {
				a[1] * b[2] - a[2] * b[1],
				a[2] * b[0] - a[0] * b[2],
				a[0] * b[1] - a[1] * b[0]
		};
	}
}

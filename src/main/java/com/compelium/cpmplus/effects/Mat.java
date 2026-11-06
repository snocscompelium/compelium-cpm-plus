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

package com.compelium.cpmplus.bends;

/**
 * Small quaternion helper (x, y, z, w), matching CPM's rotation convention:
 * a {@code Rotation(x, y, z)} means R = Rz(z) * Ry(y) * Rx(x) (same as vanilla model parts).
 */
public final class Quat {
	public double x, y, z, w;

	public Quat(double x, double y, double z, double w) {
		this.x = x;
		this.y = y;
		this.z = z;
		this.w = w;
	}

	public static Quat identity() {
		return new Quat(0, 0, 0, 1);
	}

	/** Same formula as CPM's {@code Quaternion(x, y, z, RotationOrder.ZYX, false)}. */
	public static Quat fromEulerZYX(double rx, double ry, double rz) {
		double l = Math.cos(0.5 * rx), u = Math.sin(0.5 * rx);
		double c = Math.cos(0.5 * ry), d = Math.sin(0.5 * ry);
		double h = Math.cos(0.5 * rz), p = Math.sin(0.5 * rz);
		return new Quat(
				u * c * h - l * d * p,
				l * d * h + u * c * p,
				l * c * p - u * d * h,
				l * c * h + u * d * p);
	}

	/** Rotation by {@code angle} radians around the (unit) axis, right-handed. */
	public static Quat axisAngle(double ax, double ay, double az, double angle) {
		double len = Math.sqrt(ax * ax + ay * ay + az * az);
		if (len < 1e-9) return identity();
		double s = Math.sin(angle / 2) / len;
		return new Quat(ax * s, ay * s, az * s, Math.cos(angle / 2));
	}

	/** this * o (apply o first, then this). */
	public Quat mul(Quat o) {
		return new Quat(
				w * o.x + x * o.w + y * o.z - z * o.y,
				w * o.y - x * o.z + y * o.w + z * o.x,
				w * o.z + x * o.y - y * o.x + z * o.w,
				w * o.w - x * o.x - y * o.y - z * o.z);
	}

	public Quat conj() {
		return new Quat(-x, -y, -z, w);
	}

	/** Rotates the vector v by this quaternion. */
	public double[] rotate(double vx, double vy, double vz) {
		// t = 2 * cross(q.xyz, v); v' = v + w * t + cross(q.xyz, t)
		double tx = 2 * (y * vz - z * vy);
		double ty = 2 * (z * vx - x * vz);
		double tz = 2 * (x * vy - y * vx);
		return new double[] {
				vx + w * tx + (y * tz - z * ty),
				vy + w * ty + (z * tx - x * tz),
				vz + w * tz + (x * ty - y * tx)
		};
	}

	/** Euler angles (x, y, z) such that Rz(z) * Ry(y) * Rx(x) equals this rotation. */
	public double[] toEulerZYX() {
		double n = Math.sqrt(x * x + y * y + z * z + w * w);
		double qx = x / n, qy = y / n, qz = z / n, qw = w / n;
		double r00 = 1 - 2 * (qy * qy + qz * qz);
		double r01 = 2 * (qx * qy - qz * qw);
		double r10 = 2 * (qx * qy + qz * qw);
		double r11 = 1 - 2 * (qx * qx + qz * qz);
		double r20 = 2 * (qx * qz - qy * qw);
		double r21 = 2 * (qy * qz + qx * qw);
		double r22 = 1 - 2 * (qx * qx + qy * qy);
		double sy = -r20;
		if (sy > 1) sy = 1;
		if (sy < -1) sy = -1;
		double ry = Math.asin(sy);
		double rx, rz;
		if (Math.abs(sy) < 0.999999) {
			rx = Math.atan2(r21, r22);
			rz = Math.atan2(r10, r00);
		} else {
			rx = 0;
			rz = Math.atan2(-r01, r11);
		}
		return new double[] {rx, ry, rz};
	}
}

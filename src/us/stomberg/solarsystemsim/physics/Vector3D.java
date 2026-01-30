package us.stomberg.solarsystemsim.physics;

import us.stomberg.solarsystemsim.graphics.FormatText;

import java.text.DecimalFormat;

/**
 * The Vector3D class represents a three-dimensional vector with x, y, and z components.
 * <p>
 * This immutable data structure serves as a fundamental building block for:
 * <ul>
 *   <li>Representing positions in 3D space</li>
 *   <li>Storing velocities and accelerations of bodies</li>
 *   <li>Calculating gravitational forces and trajectories</li>
 *   <li>Performing vector mathematics (addition, scaling, normalization, etc.)</li>
 * </ul>
 * <p>
 * The class provides both immutable operations (which return new Vector3D instances) and
 * mutable operations (with "InPlace" suffix, which modify the current instance). The mutable
 * operations support method chaining for composing complex vector operations efficiently.
 * <p>
 * Vector3D implements essential vector mathematics including addition, subtraction,
 * scaling, normalization, dot product, and various utility methods for physics simulations.
 */
public class Vector3D {

    /**
     * <b>x</b> component of the vector.
     */
    private double x;
    /**
     * <b>y</b> component of the vector.
     */
    private double y;
    /**
     * <b>z</b> component of the vector.
     */
    private double z;

    /**
     * Class constructor specifying initial values.
     *
     * @param x initial <b>x</b> value
     * @param y initial <b>y</b> value
     * @param z initial <b>z</b> value
     */
    public Vector3D(double x, double y, double z) {
        this.x = x;
        this.y = y;
        this.z = z;
    }

    /**
     * Class constructor. Sets values to (0.0, 0.0, 0.0) as a default.
     */
    public Vector3D() {
        this.x = 0;
        this.y = 0;
        this.z = 0;
    }

    /**
     * Getter method for the <b>x</b> component.
     *
     * @return Returns the <b>x</b> component.
     */
    public double getX() {
        return x;
    }

    /**
     * Getter method for the <b>y</b> component.
     *
     * @return Returns the <b>y</b> component.
     */
    public double getY() {
        return y;
    }

    /**
     * Getter method for the <b>z</b> component.
     *
     * @return Returns the <b>z</b> component.
     */
    public double getZ() {
        return z;
    }

    /**
     * Sets all components of this vector to match those of another vector.
     * <p>
     * This is a mutable operation that modifies the current vector instance.
     * It provides an efficient way to update a vector's values without creating
     * a new object.
     *
     * @param vector The source vector whose components will be copied
     * @return This vector instance, allowing for method chaining
     * @throws NullPointerException If the input vector is null
     */
    public Vector3D copyFrom(Vector3D vector) {
        x = vector.x;
        y = vector.y;
        z = vector.z;
        return this;
    }

    /**
     * Sums two vectors. Adds the corresponding components separately. This method returns a new vector rather than
     * setting the values of this vector.
     *
     * @param vector The second vector.
     * @return The sum of both vectors.
     */
    public Vector3D add(Vector3D vector) {
        return new Vector3D(x + vector.x, y + vector.y, z + vector.z);
    }

    /**
     * Adds another vector to this vector, modifying this vector in place.
     * <p>
     * This method performs elementwise addition of the components of the two vectors.
     * It modifies the current vector instance rather than creating a new one,
     * making it more efficient for cumulative operations.
     *
     * @param vector The vector to add to this vector
     * @return This vector instance, allowing for method chaining
     * @throws NullPointerException If the input vector is null
     */
    public Vector3D addInPlace(Vector3D vector) {
        x += vector.x;
        y += vector.y;
        z += vector.z;
        return this;
    }

    /**
     * Multiplies a vector with another number. This value multiplies each component. This function is used to
     * scale a vector.
     *
     * @param factor The factor by which to scale the vector.
     * @return The scaled vector.
     */
    public Vector3D scale(double factor) {
        return new Vector3D(x * factor, y * factor, z * factor);
    }

    /**
     * Multiplies a vector with another number. This value multiplies each component. This function is used to
     * scale a vector.
     *
     * @param factor The factor by which to scale the vector.
     * @return The scaled vector.
     */
    public Vector3D scaleInPlace(double factor) {
        x *= factor;
        y *= factor;
        z *= factor;
        return this;
    }

    /**
     * Adds a scaled version of another vector to this vector.
     * <p>
     * This method performs the operation: this += vector * factor
     * <p>
     * This combined operation is particularly useful in numerical integration
     * algorithms where a scaled vector needs to be added to an accumulator.
     * It is more efficient than separate scale and add operations as it
     * requires only one pass through the vector components.
     *
     * @param vector The vector to be scaled and added
     * @param factor The scaling factor to apply to the vector
     * @return This vector instance, allowing for method chaining
     * @throws NullPointerException If the input vector is null
     */
    public Vector3D addAndScaleInPlace(Vector3D vector, double factor) {
        x += vector.x * factor;
        y += vector.y * factor;
        z += vector.z * factor;
        return this;
    }

    /**
     * Sets all components of this vector to zero.
     * <p>
     * This method resets the vector to the origin point (0,0,0) in 3D space.
     * It is more efficient than creating a new zero vector or setting each
     * component individually.
     *
     * @return This vector instance, allowing for method chaining
     */
    public Vector3D setToZero() {
        x = 0;
        y = 0;
        z = 0;
        return this;
    }

    /**
     * Creates a new vector with all components negated.
     * <p>
     * This method returns a new vector pointing in the opposite direction
     * with the same magnitude as the original vector.
     *
     * @return A new vector with all components negated
     */
    public Vector3D negate() {
        return new Vector3D(-x, -y, -z);
    }

    /**
     * Negates all components of this vector in place.
     * <p>
     * This method reverses the direction of the vector while maintaining
     * its magnitude. It modifies the current vector instance rather than
     * creating a new one.
     *
     * @return This vector instance, allowing for method chaining
     */
    public Vector3D negateInPlace() {
        x = -x;
        y = -y;
        z = -z;
        return this;
    }

    /**
     * Gets the differences between the corresponding components of two vectors. Subtracts the second vector from the
     * first vector. Returned as an array of values.
     *
     * @param vector The second vector.
     * @return Returns the <b>x</b>, <b>y</b>, and <b>z</b> difference.
     */
    public Vector3D subtract(Vector3D vector) {
        return new Vector3D(x - vector.x, y - vector.y, z - vector.z);
    }

    /**
     * Gets the differences between the corresponding components of two vectors. Subtracts the second vector from the
     * first vector. Returned as an array of values.
     *
     * @param vector The second vector.
     * @return Returns the <b>x</b>, <b>y</b>, and <b>z</b> difference.
     */
    public Vector3D subtractInPlace(Vector3D vector) {
        x -= vector.x;
        y -= vector.y;
        z -= vector.z;
        return this;
    }

    /**
     * Calculates the Euclidean distance between this vector and another vector in 3D space.
     * <p>
     * This method computes the straight-line distance between two points represented
     * by the vectors, using the Pythagorean formula:
     * <p>
     * distance = √((x₂-x₁)² + (y₂-y₁)² + (z₂-z₁)²)
     * <p>
     * This is a fundamental operation in physics simulations for determining
     * the separation between bodies.
     *
     * @param vector The second vector to calculate distance to
     * @return The Euclidean distance between the two vectors
     * @throws NullPointerException If the input vector is null
     */
    public double distance(Vector3D vector) {
        double dx = x - vector.x;
        double dy = y - vector.y;
        double dz = z - vector.z;
        return Math.sqrt(dx * dx + dy * dy + dz * dz);
    }

    /**
     * Returns the vector's 3D distance to (0, 0, 0). Is equal to the vector's magnitude.
     *
     * @return Returns the vector's magnitude.
     */
    public double magnitude() {
        return Math.sqrt(x * x + y * y + z * z);
    }

    /**
     * Calculates a normalized direction vector pointing from this vector to another.
     * <p>
     * This method creates a unit vector (magnitude of 1) in the direction from
     * this vector to the specified target vector. It is computed as:
     * <p>
     * (target - this).normalize()
     * <p>
     * This is useful for determining the direction of gravitational forces,
     * movement trajectories, or any calculation requiring only directional information.
     *
     * @param vector The target vector to find direction toward
     * @return A normalized vector pointing from this vector to the target
     * @throws NullPointerException If the input vector is null
     */
    public Vector3D angleTo(Vector3D vector) {
        return vector.subtract(this).normalizeInPlace();
    }

    /**
     * Gets a vector representing only the vector's direction, its angle to (0, 0, 0).
     *
     * @return Returns a vector on a unit scale to represent angle.
     */
    public Vector3D normalize() {
        double magnitude = magnitude();
        if (magnitude == 0) {
            return new Vector3D(0, 0, 0);
        }
        return scale(1 / magnitude());
    }

    /**
     * Gets a vector representing only the vector's direction, its angle to (0, 0, 0).
     *
     * @return Returns a vector on a unit scale to represent angle.
     */
    public Vector3D normalizeInPlace() {
        double magnitude = magnitude();
        if (magnitude == 0) {
            return this;
        }
        return scaleInPlace(1 / magnitude);
    }

    /**
     * Creates a new vector by linearly interpolating between this vector and another.
     * <p>
     * The interpolation parameter p controls the blend:
     * <ul>
     *   <li>When p = 0, the result equals this vector</li>
     *   <li>When p = 1, the result equals the other vector</li>
     *   <li>When 0 &lt; p &lt; 1, the result is a proportional blend</li>
     * </ul>
     * <p>
     * This method can be used to smoothly transition between two positions,
     * velocities, or other vector quantities.
     *
     * @param other The target vector to interpolate toward
     * @param p The interpolation parameter (typically between 0 and 1)
     * @return A new vector representing the interpolated result
     * @throws NullPointerException If the other vector is null
     */
    public Vector3D interpolate(Vector3D other, double p) {
        return new Vector3D(x + (other.x - x) * p, y + (other.y - y) * p, z + (other.z - z) * p);
    }

    /**
     * Linearly interpolates this vector toward another vector in place.
     * <p>
     * This method modifies the current vector instance by moving it
     * partially toward the target vector according to parameter p:
     * <ul>
     *   <li>When p = 0, this vector remains unchanged</li>
     *   <li>When p = 1, this vector becomes equal to the other vector</li>
     *   <li>When 0 &lt; p &lt; 1, this vector moves proportionally toward the other</li>
     * </ul>
     * <p>
     * This operation is commonly used in collision physics to calculate
     * the new position of a merged body based on the mass-weighted average
     * of the colliding bodies.
     *
     * @param other The target vector to interpolate toward
     * @param p The interpolation parameter (typically between 0 and 1)
     * @return This vector instance, allowing for method chaining
     * @throws NullPointerException If the other vector is null
     */
    public Vector3D interpolateInPlace(Vector3D other, double p) {
        x += (other.x - x) * p;
        y += (other.y - y) * p;
        z += (other.z - z) * p;
        return this;
    }

    /**
     * Calculates the dot product between this vector and another vector.
     *
     * @param vector The other vector to calculate dot product with
     * @return the scalar dot product of the two vectors
     */
    public double dotProduct(Vector3D vector) {
        return x * vector.x + y * vector.y + z * vector.z;
    }

    /**
     * Create a new <code>Vector3D</code> with identical values.
     *
     * @return Returns the copied vector.
     */
    public Vector3D copy() {
        return new Vector3D(x, y, z);
    }

    /**
     * Formats the vector into a string representation with specified units.
     * <p>
     * This method generates a human-readable string representation of the vector
     * with properly formatted values and unit labels. For large values, it will
     * automatically convert to appropriate unit scales (e.g., kilometers).
     * <p>
     * Example output: <code>"Vector3D(x=149.6 Mkm, y=0.00 km, z=0.00 km)"</code>
     *
     * @param unit The base unit to display (e.g., "m", "km")
     * @return A formatted string with components and appropriate units
     */
    public String toString(String unit) {
        String string = "Vector3D(x=" + FormatText.formatValue(x, unit, "k" + unit);
        string += ", y=" + FormatText.formatValue(y, unit, "k" + unit);
        string += ", z=" + FormatText.formatValue(z, unit, "k" + unit);
        return string + ")";
    }

    /**
     * Formats the vector into a string representation without units.
     * <p>
     * This method generates a human-readable string representation of the vector
     * with properly formatted values.
     * <p>
     * Example output: <code>"Vector3D(x=149.60, y=0.00, z=0.00)"</code>
     *
     * @return Returns a string expressing the vector.
     */
    @Override
    public String toString() {
        DecimalFormat df = new DecimalFormat("#,##0.00");
        String string = "Vector3D(x=" + df.format(x);
        string += ", y=" + df.format(y);
        string += ", z=" + df.format(z);
        return string + ")";
    }

}

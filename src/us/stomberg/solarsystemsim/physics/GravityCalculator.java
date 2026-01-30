package us.stomberg.solarsystemsim.physics;

import us.stomberg.solarsystemsim.Setup;

import java.util.ArrayList;
import java.util.List;

/**
 * The GravityCalculator class calculates gravitational forces between celestial bodies
 * in the simulation using Newton's law of universal gravitation.
 * <p>
 * This class efficiently computes the N-body gravitational interactions by:
 * <ul>
 *   <li>Pre-allocating force vectors to minimize garbage collection</li>
 *   <li>Using a single reusable vector for intermediate calculations</li>
 *   <li>Applying the principle of action and reaction (Newton's third law) to reduce computations</li>
 *   <li>Calculating forces only for unique pairs of bodies (i,j where i&lt;j)</li>
 * </ul>
 * <p>
 * Newton's law of universal gravitation states that the force between two bodies is:
 * <pre>F = G * (m₁ * m₂) / r²</pre>
 * where:
 * <ul>
 *   <li>F is the gravitational force between the bodies</li>
 *   <li>G is the gravitational constant</li>
 *   <li>m₁ and m₂ are the masses of the bodies</li>
 *   <li>r is the distance between the centers of the bodies</li>
 * </ul>
 * <p>
 * The direction of the force is along the line connecting the bodies, with
 * equal and opposite forces applied to each body (Newton's third law).
 */
public class GravityCalculator {

    /**
     * Pre-allocated array of force vectors for each body in the simulation.
     * <p>
     * This array stores the accumulated gravitational forces acting on each body.
     * Using pre-allocated vectors avoids creating new Vector3D objects during
     * each force calculation, reducing garbage collection overhead.
     */
    private final ArrayList<Vector3D> gravitationalForces = new ArrayList<>();
    /**
     * Reusable vector for intermediate calculations.
     * <p>
     * This single vector is reused for all directional and magnitude calculations
     * in the force computation process, minimizing object creation.
     */
    private final Vector3D force = new Vector3D();

    /**
     * Constructs a GravityCalculator for the given set of celestial bodies.
     * <p>
     * Initializes the pre-allocated force vectors for each body in the simulation.
     * Each body will have its own dedicated Vector3D to store the accumulated
     * gravitational forces acting upon it.
     *
     * @param bodies The list of celestial bodies in the simulation
     */
    public GravityCalculator(List<Body> bodies) {
        for (int i = 0; i < bodies.size(); i++) {
            gravitationalForces.add(new Vector3D());
        }
    }

    // TODO: Parallelize for sufficiently large n
    /**
     * Calculates the gravitational forces between all pairs of bodies in the simulation.
     * <p>
     * This method implements an optimized N-body force calculation algorithm:
     * <ol>
     *   <li>First, it resets all force vectors to zero</li>
     *   <li>Then, for each unique pair of bodies (i,j where i&lt;j), it:
     *     <ul>
     *       <li>Calculates the gravitational force between them</li>
     *       <li>Adds the force vector to the first body</li>
     *       <li>Subtracts the same force vector from the second body (applying Newton's third law)</li>
     *     </ul>
     *   </li>
     * </ol>
     * <p>
     * This approach computes each interaction only once, that is, N(N-1)/2 tests rather than N² tests.
     *
     * @param bodies The list of celestial bodies to calculate forces for
     * @return An ArrayList of Vector3D objects representing the gravitational force acting on each body
     */
    public ArrayList<Vector3D> updateForces(List<Body> bodies) {
        // Reset all force vectors to zero
        for (int i = 0; i < bodies.size(); i++) {
            gravitationalForces.get(i).setToZero();
        }

        // Calculate forces between each unique pair of bodies
        for (int i = 0; i < bodies.size(); i++) {
            Body body = bodies.get(i);
            for (int j = i + 1; j < bodies.size(); j++) {
                Body other = bodies.get(j);
                // Calculate the force between this pair of bodies
                calculateForce(body, other);
                // Apply the force to the first body (positive direction)
                gravitationalForces.get(i).addInPlace(force);
                // Apply the equal and opposite force to the second body (Newton's third law)
                gravitationalForces.get(j).subtractInPlace(force);
            }
        }

        // Return the calculated forces for all bodies
        return gravitationalForces;
    }

    /**
     * Calculates the gravitational force between two celestial bodies.
     * <p>
     * This method implements Newton's law of universal gravitation:
     * <pre>F = G * (m₁ * m₂) / r²</pre>
     * <p>
     * The force vector is calculated by:
     * <ol>
     *   <li>Finding the displacement vector from the first body to the second body</li>
     *   <li>Computing the distance (magnitude of displacement)</li>
     *   <li>Scaling the displacement vector by the gravitational force magnitude:
     *     <pre>G * (m₁ * m₂) / r³</pre>
     *     (dividing by r³ instead of r² automatically normalizes the direction vector)
     *   </li>
     * </ol>
     * <p>
     * The resulting vector represents the gravitational force acting on the first body.
     * The force on the second body is equal and opposite (Newton's third law).
     *
     * @param body The first celestial body
     * @param other The second celestial body
     */
    private void calculateForce(Body body, Body other) {
        // Get the positions of both bodies
        BodyHistory stateFirst = body.getState();
        BodyHistory stateSecond = other.getState();

        // Calculate the displacement vector from the first body to the second
        force.copyFrom(stateSecond.getPosition())
             .subtractInPlace(stateFirst.getPosition());

        // Calculate the distance between the bodies
        double dist = force.magnitude();

        // Scale the displacement vector to get the force vector
        // Force = G * (m1 * m2) / (r^2) * (direction vector)
        // Since the displacement vector is already in the correct direction,
        // we can divide by r^3 instead of normalizing and then multiplying by 1/r^2
        force.scaleInPlace(Setup.getGravity() *
                         ((body.getMass() * other.getMass()) /
                         (dist * dist * dist)));
    }
}

package us.stomberg.solarsystemsim.physics;

import java.util.ArrayList;
import java.util.List;
import java.util.logging.Logger;

// TODO: Parallelize for sufficiently large n
/**
 * The CollisionDetector class is responsible for detecting collisions between celestial bodies
 * in the physics simulation.
 * <p>
 * This class implements an efficient collision detection algorithm that:
 * <ul>
 *   <li>Uses a bounding box check as an initial fast rejection test</li>
 *   <li>Computes the time of closest approach between object pairs with linear motion approximation</li>
 *   <li>Returns a list of collision events</li>
 * </ul>
 * <p>
 * The collision detection occurs in two phases:
 * <ol>
 *   <li>Broad phase: Bounding box checks eliminate obviously non-colliding pairs</li>
 *   <li>Narrow phase: Linear trajectory analysis computes the precise collision time</li>
 * </ol>
 * <p>
 * This approach efficiently handles N-body simulations by only testing each unique pair
 * of bodies once (N(N-1)/2 tests rather than N² tests) and using mathematical
 * optimizations to minimize computational overhead.
 * <p>
 * Detected collisions are returned as {@link CollisionEvent} objects that contain
 * information about the colliding bodies and the precise time of collision.
 */
public class CollisionDetector {

    /**
     * Logger instance for recording collision detection events and debugging information.
     * <p>
     * Used to log information about detected collisions for simulation monitoring
     * and analysis.
     */
    private static final Logger logger = Logger.getLogger(CollisionDetector.class.getName());
    /**
     * Reusable list for storing detected collision events.
     * <p>
     * This list is cleared and repopulated during each collision detection pass
     * to minimize object creation and garbage collection overhead.
     */
    private final ArrayList<CollisionEvent> collisions = new ArrayList<>();
    /**
     * Reusable vector for calculating relative positions between bodies.
     * <p>
     * Reusing this vector minimizes object creation during collision detection.
     */
    private static final Vector3D relPos = new Vector3D();
    /**
     * Reusable vector for calculating relative velocities between bodies.
     * <p>
     * Reusing this vector minimizes object creation during collision detection.
     */
    private static final Vector3D relVel = new Vector3D();

    /**
     * Detects all potential collisions between celestial bodies within the current time step.
     * <p>
     * This method examines each unique pair of bodies to determine if they will collide
     * before the end of the current time step. For each potential collision, it:
     * <ol>
     *   <li>Computes the relative position and velocity of the bodies</li>
     *   <li>Performs a fast bounding box check to eliminate obviously non-colliding pairs</li>
     *   <li>If the bounding box check passes, calculates the precise collision time using
     *       a linear approximation of the bodies' trajectories</li>
     *   <li>Verifies that the collision occurs within the current time step</li>
     *   <li>Creates a {@link CollisionEvent} for valid collisions</li>
     * </ol>
     * <p>
     * The algorithm is optimized to test each pair of bodies only once and uses
     * mathematical shortcuts to minimize computational complexity.
     *
     * @param bodies The list of celestial bodies to check for collisions
     * @param timeStep The current simulation time step containing timing information
     * @return A list of collision events that occur within the current time step
     */
    public List<CollisionEvent> detectCollisions(List<Body> bodies, TimeStep timeStep) {
        // Clear the collision list of any previous collisions
        collisions.clear();
        // For each body, determine whether there has been a collision with another body
        for (int i = 0; i < bodies.size(); i++) {
            Body body = bodies.get(i);
            for (int j = i + 1; j < bodies.size(); j++) {
                Body other = bodies.get(j);
                relPos.copyFrom(other.getState().getHistory().position()
                                     .subtract(body.getState().getHistory().position()));
                relVel.copyFrom(other.getState().findLinearVelocity(timeStep)
                                     .subtract(body.getState().findLinearVelocity(timeStep)));

                if (!bboxCheck(body, other, timeStep)) {
                    continue;
                }

                // Find the collision time
                double t = findEarliestLinearApproach(body, other);
                // NaN if the bodies do not intersect
                if (Double.isNaN(t) || t < -timeStep.getElapsed() || t >= timeStep.getRemaining()) {
                    continue;
                }
                logger.info("Collision detected between " + body + " and " + other);
                collisions.add(new CollisionEvent(body, other, t));
            }
        }
        return collisions;
    }

    /**
     * Performs an axis-aligned bounding box check as a fast rejection test.
     * <p>
     * This method checks whether two bodies might collide by examining their
     * positions and velocities along each coordinate axis. It quickly eliminates
     * pairs that:
     * <ul>
     *   <li>Are moving away from each other</li>
     *   <li>Are too far apart to collide within the current time step</li>
     *   <li>Are moving too slowly to close the distance between them</li>
     * </ul>
     * <p>
     * The bounding box check is a conservative approximation - it may return true
     * for objects that won't actually collide (false positives), but it will never
     * return false for objects that will collide (no false negatives).
     *
     * @param a The first celestial body
     * @param b The second celestial body
     * @param timeStep The current simulation time step containing timing information
     * @return {@code true} if the bodies might collide, {@code false} if they definitely won't
     */
    private boolean bboxCheck(Body a, Body b, TimeStep timeStep) {
        double dt = timeStep.getRemaining();
        double threshold = a.getRadius() + b.getRadius();

        // Check that not intersecting on X axis
        if (Math.abs(relPos.getX()) > threshold) {
            if (relPos.getX() * relVel.getX() >= 0) {
                return false; // Not approaching
            }
            if (Math.abs(relPos.getX()) - threshold > Math.abs(relVel.getX()) * dt) {
                return false;  // Not moving fast enough to collide
            }
        }

        // Check that not intersecting on Y axis
        if (Math.abs(relPos.getY()) > threshold) {
            if (relPos.getY() * relVel.getY() >= 0) {
                return false; // Not approaching
            }
            if (Math.abs(relPos.getY()) - threshold > Math.abs(relVel.getY()) * dt) {
                return false; // Not moving fast enough to collide
            }
        }

        // Check that not intersecting on Z axis
        if (Math.abs(relPos.getZ()) > threshold) {
            if (relPos.getZ() * relVel.getZ() >= 0) {
                return false; // Not approaching
            }
            if (Math.abs(relPos.getZ()) - threshold > Math.abs(relVel.getZ()) * dt) {
                return false;  // Not moving fast enough to collide
            }
        }
        return true;
    }

    /**
     * Calculates the earliest time of collision between two bodies assuming linear motion.
     * <p>
     * This method solves the quadratic equation that determines when the distance between
     * two linearly moving bodies equals the sum of their radii. The derivation is as follows:
     * <p>
     * Given:
     * <ul>
     *   <li>r = relative position vector between bodies</li>
     *   <li>v = relative velocity vector between bodies</li>
     *   <li>R = sum of the radii of the two bodies</li>
     * </ul>
     * <p>
     * The squared distance between the bodies at time t is:
     * <pre>d²(t) = |r + vt|² = (r + vt)·(r + vt) = r·r + 2(r·v)t + (v·v)t²</pre>
     * <p>
     * Collision occurs when d²(t) = R², which yields the quadratic equation:
     * <pre>(v·v)t² + 2(r·v)t + (r·r - R²) = 0</pre>
     * <p>
     * The method solves this equation and returns the earliest positive solution,
     * which represents the time of first contact between the bodies.
     *
     * @param a The first celestial body
     * @param b The second celestial body
     * @return The time until collision, or Double.NaN if no collision will occur
     */
    private double findEarliestLinearApproach(Body a, Body b) {
        // Suppose for a body p(t) = r_p_0 + v_p_0 * t.
        // Then if r = r_p2_0 - r_p1_0, v = v_p2_0 - v_p1_0,
        // the distance between the bodies will be r(t) = || r + v * t || at time t.
        // To find the instant of collision, we must solve for r(t) = r_1 + r_2, the radii of the bodies.
        // Squaring both sides, the equation becomes
        // r^2 + 2 * r * v * t + v^2 * t^2 = (r_1 + r_2)^2

        // Now we can solve this as a standard quadratic with
        // a = v^2
        // b = 2 * r * v
        // c = r^2 - r_1 - r_2
        double threshold = a.getRadius() + b.getRadius();
        double qa = relVel.dotProduct(relVel);
        double qb = 2 * relPos.dotProduct(relVel);
        double qc = relPos.dotProduct(relPos) - threshold * threshold;

        // Find the discriminant and check for real roots
        double discriminant = qb * qb - 4 * qa * qc;
        if (discriminant < 0) {
            return Double.NaN;
        } else if (discriminant == 0) {
            // Don't bother computing a square root if not necessary
            return (-qb) / (2 * qa);
        } else {
            // We want the earliest intersection
            return (-qb - Math.sqrt(discriminant)) / (2 * qa);
        }

    }

}

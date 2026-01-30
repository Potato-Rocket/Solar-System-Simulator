package us.stomberg.solarsystemsim.physics;

import java.util.List;
import java.util.logging.Logger;

/**
 * The CollisionEvent class represents a collision between two celestial bodies in the simulation.
 * <p>
 * This class stores information about:
 * <ul>
 *   <li>The two bodies involved in the collision</li>
 *   <li>The time at which the collision occurs</li>
 * </ul>
 * <p>
 * CollisionEvent implements Comparable to allow sorting events chronologically in a priority queue,
 * ensuring that collisions are processed in the correct temporal order during simulation.
 * <p>
 * When a collision occurs, the smaller body is merged into the larger one following conservation
 * of mass and momentum principles. This models the physical process of celestial bodies combining
 * after impact.
 */
public class CollisionEvent implements Comparable<CollisionEvent> {

    /**
     * Logger for recording collision information and debugging details.
     */
    private static final Logger logger = Logger.getLogger(CollisionEvent.class.getName());
    /**
     * First body involved in the collision.
     */
    private final Body a;
    /**
     * Second body involved in the collision.
     */
    private final Body b;
    /**
     * Time until the collision occurs, relative to the current simulation time step.
     */
    private final double t;

    /**
     * Creates a new collision event between two bodies at a specific time.
     * <p>
     * The time parameter represents the relative time from the current simulation
     * step when the collision will occur.
     *
     * @param a The first body involved in the collision
     * @param b The second body involved in the collision
     * @param t The relative time until the collision occurs
     */
    public CollisionEvent(Body a, Body b, double t) {
        this.a = a;
        this.b = b;
        this.t = t;
    }

    /**
     * Processes a collision between two celestial bodies.
     * <p>
     * This method implements the physical effects of a collision by:
     * <ul>
     *   <li>Determining which body is more massive (to be preserved)</li>
     *   <li>Rolling back all bodies to the exact moment of collision</li>
     *   <li>Calculating the new position through mass-weighted interpolation</li>
     *   <li>Calculating the new velocity through conservation of momentum</li>
     *   <li>Merging mass, density, and radius properties</li>
     *   <li>Removing the consumed body from the simulation</li>
     * </ul>
     * <p>
     * The collision follows conservation of mass and momentum principles, where
     * the smaller body is consumed by the larger body.
     *
     * @param bodies The list of all bodies in the simulation
     * @param timeStep The current simulation time step
     * @param integrator The numerical integrator used for rolling back positions
     * @throws NullPointerException If any of the parameters are null
     */
    public void processCollision(List<Body> bodies, TimeStep timeStep, Integrator integrator) {
        Body destroy;
        Body keep;
        // Keep the more massive body
        if (a.getMass() < b.getMass()) {
            destroy = a;
            keep = b;
        } else {
            destroy = b;
            keep = a;
        }
        BodyHistory keepState = keep.getState();
        BodyHistory destroyState = destroy.getState();

        logger.info("Collision processed between " + a + " and " + b);
        logger.info(destroy + " will be consumed by " + keep + " at time " + (timeStep.getElapsed() + t));

        // Rolls back each body's position and velocity estimate to the time of collision.
        double rollBack = timeStep.getRemaining() - t;
        for (Body body : bodies) {
            integrator.rollBack(body, rollBack);
        }

        double totalMass = keep.getMass() + destroy.getMass();
        // Average the position of the bodies, weighted by the relative masses
        keepState.getPosition()
                 .interpolateInPlace(destroyState.getPosition(), destroy.getMass() / totalMass);

        // Find the resultant velocity by conservation of momentum
        keepState.getVelocity()
                 .scaleInPlace(keep.getMass() / totalMass)
                 .addInPlace(destroyState.getVelocity()
                                         .scale(destroy.getMass() / totalMass));

        // Update the history of the body now to avoid overwriting the velocity
        BodyHistory.State prev = keepState.getHistory();
        keep.getState().updateHistory((prev == null) ? new Vector3D() : prev.acceleration());

        // Combine mass, density, and radius
        keep.merge(destroy);
        bodies.remove(destroy);
    }

    /**
     * Returns the time until the collision occurs.
     * <p>
     * This value represents the time interval from the current simulation
     * step until the projected collision will take place.
     *
     * @return The relative time until collision occurs
     */
    public double getT() {
        return t;
    }

    /**
     * Determines whether this collision event is equal to another object.
     * <p>
     * Two collision events are considered equal if they involve the same two bodies,
     * regardless of their order. This implementation supports the proper functioning
     * of hash-based collections.
     * <p>
     * The collision time is not considered for equality since multiple potential
     * collisions between the same bodies should be treated as equivalent events.
     *
     * @param o The object to compare with this collision event
     * @return <code>true</code> if the objects are equal, <code>false</code> otherwise
     */
    @Override
    public boolean equals(Object o) {
        if (o == null || getClass() != o.getClass()) {
            return false;
        }
        CollisionEvent that = (CollisionEvent) o;
        return (that.a.equals(a) && that.b.equals(b)) || (that.a.equals(b) && that.b.equals(a));
    }

    /**
     * Compares this collision event with another based on their occurrence times.
     * <p>
     * This method allows collision events to be sorted chronologically in priority queues
     * or other ordered collections, ensuring that events are processed in the correct
     * temporal sequence during simulation.
     * <p>
     * Events with earlier occurrence times are considered "less than" those with
     * later times.
     *
     * @param other The collision event to compare with this one
     * @return A negative integer if this event occurs before the other,
     *         zero if they occur simultaneously, or a positive integer if this
     *         event occurs after the other
     */
    @Override
    public int compareTo(CollisionEvent other) {
        return Double.compare(t, other.t);
    }

}

package us.stomberg.solarsystemsim.physics;

import us.stomberg.solarsystemsim.Setup;
import us.stomberg.solarsystemsim.TimeManager;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * The Physics class manages all physical interactions between celestial bodies in the simulation.
 * It handles gravitational forces, collisions, numerical integration of motion equations,
 * and maintains the state of all bodies in the system.
 * <p>
 * This class is thread-safe with synchronization on the lock object when accessing shared state.
 */
public class Physics {

    public final Object lock = new Object();
    /**
     * The initial max distance of any body from the origin.
     */
    private double initBounds = 0;
    /**
     * The array of all bodies.
     */
    private final List<Body> bodyArray;
    /**
     * The total kinetic energy of all bodies at system initialization.
     * Used as a reference point for energy conservation checks.
     */
    private final double initialKineticEnergy;
    /**
     * The numerical integration algorithm used to update body positions and velocities.
     */
    private final Integrator integrator;
    /**
     * Manages the simulation time step intervals and subdivision for collision handling.
     */
    private final TimeStep timeStep;
    /**
     * Calculates gravitational forces between all bodies in the system.
     */
    private final GravityCalculator gravityCalculator;
    /**
     * Detects potential collisions between bodies in the system.
     */
    private final CollisionDetector collisionDetector = new CollisionDetector();

    /**
     * Constructor for the Physics class.
     * Populates the array of bodies based on the generation data and initializes the physics system.
     *
     * @param integrator The numerical integrator to use for updating body positions and velocities.
     *                   Different integrators provide varying levels of accuracy and performance
     */
    public Physics(Integrator integrator) {
        timeStep = new TimeStep(Setup.getTimeStep());
        this.integrator = integrator;

        synchronized (lock) {
            // Import the bodies from the generation data
            // TODO: Copy rather than reference, for better encapsulation and thread safety
            bodyArray = Setup.getGenerationData();
            gravityCalculator = new GravityCalculator(bodyArray);

            // Calculate the kinetic energy of the system at initialization
            initialKineticEnergy = getKineticEnergy();
            // Make sure each body starts with the proper history and initial state
            ArrayList<Vector3D> forces = gravityCalculator.updateForces(bodyArray);
            for (int i = 0; i < bodyArray.size(); i++) {
                Body body = bodyArray.get(i);
                body.getState().updateHistory(forces.get(i).scaleInPlace(1.0 / body.getMass()));
            }
            // Calculate the max initial distance of any one body from the origin
            for (Body body : bodyArray) {
                double dist = body.getState().getPosition().magnitude();
                if (dist > initBounds) {
                    initBounds = dist;
                }
            }
        }
    }

    /**
     * Updates the state of all bodies in the simulation for the current time step.
     * <p>
     * This method performs the following operations:
     * <ol>
     *     <li>Makes position predictions for all bodies using the integrator
     *     <li>Detects potential collisions between bodies (if collision checking is enabled)
     *     <li>Handles any collisions by adjusting positions, velocities, and possibly merging bodies
     *     <li>Calculates gravitational forces between all bodies
     *     <li>Finalizes the position and velocity updates using the numerical integrator
     *     <li>Updates visualization data like body trails
     * </ol>
     * The physics updates are performed within the timeStep interval, which may be subdivided
     * if collisions are detected, to ensure accurate collision handling at the precise
     * moment of collision.
     * <p>
     * This method is synchronized on the lock object to ensure thread safety.
     */
    public void updateBodies() {
        timeStep.reset();
        synchronized (lock) {
            // Process the physics updates until the full timestep is completed
            while (!timeStep.isFinished()) {
                // Make initial position predictions for each body using the integrator
                for (Body body : bodyArray) {
                    integrator.setPrediction(body, timeStep.getRemaining());
                }
                // Start with the full remaining time step
                double t = timeStep.getRemaining();

                // Check for collisions if enabled in the simulation settings
                if (Setup.shouldCheckCollisions()) {
                    // Get all potential collisions within the current time step
                    List<CollisionEvent> collisions = collisionDetector.detectCollisions(bodyArray, timeStep);

                    // If collisions are detected, handle the earliest one first
                    if (!collisions.isEmpty()) {
                        // Find the earliest collision by comparing timestamps
                        CollisionEvent collision = Collections.min(collisions);

                        // Process the collision, which may merge bodies or modify velocities
                        collision.processCollision(bodyArray, timeStep, integrator);

                        // Adjust the time step to process only up to the collision point
                        t = collision.getT();
                    }
                }
                // Update the time step counter with the time actually processed
                timeStep.update(t);

                // Increment the global simulation duration
                TimeManager.incrementDuration(t);

                // Calculate gravitational forces between all bodies
                ArrayList<Vector3D> forces = gravityCalculator.updateForces(bodyArray);

                // Apply the calculated forces to update each body's position and velocity
                for (int i = 0; i < bodyArray.size(); i++) {
                    integrator.update(bodyArray.get(i), forces.get(i), t);
                }
            }

            // Increment the global simulation duration
            TimeManager.incrementDuration(timeStep.getDt());

            // Update visualization data after all physics calculations are complete
            for (Body body : bodyArray) {
                body.updateTrail();
            }
        }
    }

    /**
     * Returns the list of all celestial bodies in the simulation.
     * <p>
     * Note: While this returns a direct reference to the internal body list,
     * operations on the list should be synchronized using the lock object
     * to maintain thread safety.
     *
     * @return The list of all bodies in the simulation
     */
    public List<Body> getBodyArray() {
        return bodyArray;
    }

    /**
     * Gets the initial boundaries for the simulation view. Equal to the furthest distance from the origin any one
     * body starts at.
     *
     * @return The initial boundaries for the view
     */
    public double getInitBounds() {
        return initBounds;
    }

    /**
     * Calculates and returns the current total kinetic energy of the system.
     * <p>
     * This method sums the kinetic energy of all bodies in the simulation.
     * The calculation is performed in a thread-safe manner by synchronizing
     * on the lock object.
     *
     * @return The total kinetic energy of the system in the simulation's energy units
     */
    public double getKineticEnergy() {
        double sum = 0;
        synchronized (lock) {
            for (Body body : bodyArray) {
                sum += body.getKineticEnergy();
            }
        }
        return sum;
    }

    /**
     * Returns the total kinetic energy of the system at initialization.
     * <p>
     * This value can be used as a reference to check energy conservation
     * throughout the simulation.
     *
     * @return The initial kinetic energy value in the simulation's energy units
     */
    public double getInitialKineticEnergy() {
        synchronized (lock) {
            return initialKineticEnergy;
        }
    }
}

package us.stomberg.solarsystemsim.physics;

/**
 * The Integrator interface defines the numerical integration methods used to simulate
 * the motion of celestial bodies in a physics simulation.
 * <p>
 * This interface provides a standard set of operations that different numerical integration
 * techniques must implement to advance the simulation state over time. The typical usage
 * pattern involves three phases:
 * <ol>
 *   <li>Making position and velocity predictions based on current state</li>
 *   <li>Rolling back predictions if collisions or other events occur</li>
 *   <li>Finalizing the state update with calculated forces</li>
 * </ol>
 * <p>
 * The choice of integrator significantly affects simulation stability, accuracy,
 * and performance, especially for long-running simulations.
 */
public interface Integrator {

    /**
     * Creates a prediction of a body's future position and velocity.
     * <p>
     * This method advances the body's state by the specified time step to create
     * a tentative prediction of where the body will be and how it will be moving.
     * These predictions are used for collision detection and visualization before
     * the final state update.
     * <p>
     * The implementation should use the body's current position, velocity, and acceleration
     * to generate the prediction according to the specific integration algorithm.
     *
     * @param body The celestial body whose state should be predicted
     * @param dt The time step duration in simulation time units
     */
    void setPrediction(Body body, double dt);

    /**
     * Rolls back a previous prediction to restore the body to its original state.
     * <p>
     * This method undoes the effects of a previous {@code setPrediction} call,
     * restoring the body to its state before the prediction was made. This is typically
     * used when a collision is detected, and the simulation needs to be advanced
     * only up to the point of collision.
     * <p>
     * The implementation should ensure that the rollback operation is the exact
     * mathematical inverse of the prediction operation to maintain simulation accuracy.
     *
     * @param body The celestial body whose state should be rolled back
     * @param dt The time step duration that was used for the prediction
     */
    void rollBack(Body body, double dt);

    /**
     * Updates a body's state based on applied forces over a time interval.
     * <p>
     * This method finalizes the update of a body's position, velocity, and acceleration
     * using the calculated force vector and time step.
     * It represents the core of the numerical integration process that advances the simulation state.
     * <p>
     * The implementation should:
     * <ul>
     *   <li>Update the body's velocity based on the applied force, if necessary</li>
     *   <li>Update the body's position based on the new velocity, if necessary</li>
     *   <li>Store the new acceleration derived from the force for future predictions</li>
     *   <li>Store the body's current state in its history</li>
     * </ul>
     *
     * @param body The celestial body whose state should be updated
     * @param f The force vector currently applied to the body
     * @param dt The time step duration in simulation time units
     */
    void update(Body body, Vector3D f, double dt);

}

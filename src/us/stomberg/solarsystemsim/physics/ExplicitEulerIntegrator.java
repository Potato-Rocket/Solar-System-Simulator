package us.stomberg.solarsystemsim.physics;

/**
 * Implementation of the Integrator interface using the Explicit Euler method.
 * <p>
 * The Explicit Euler method is a first-order numerical integration technique
 * that updates positions and velocities using the following formulas:
 * <ul>
 *   <li>x(t+dt) = x(t) + v(t)*dt</li>
 *   <li>v(t+dt) = v(t) + a(t)*dt</li>
 * </ul>
 * <p>
 * This is one of the simplest integration methods, with the following characteristics:
 * <ul>
 *   <li><b>Simplicity</b>: Easy to implement and computationally efficient</li>
 *   <li><b>Accuracy</b>: First-order accuracy (error proportional to dt)</li>
 *   <li><b>Stability</b>: Conditionally stable - may become unstable with large time steps</li>
 *   <li><b>Energy Conservation</b>: Does not conserve energy over long simulations</li>
 * </ul>
 * <p>
 * Due to its simplicity, the Explicit Euler method is suitable for educational purposes
 * and simulations where computational speed is prioritized over accuracy. For long-term
 * simulations or those requiring energy conservation, more sophisticated integrators
 * like Verlet or Symplectic should be considered.
 * <p>
 * More info can be found at <a href=https://en.wikipedia.org/wiki/Euler_method>Wikipedia - Euler method</a>.
 */
public class ExplicitEulerIntegrator implements Integrator {

    /**
     * Predicts a body's future position and velocity using the Explicit Euler method.
     * <p>
     * This implementation follows the standard Explicit Euler formulation:
     * <ul>
     *   <li>Position update: x_{n+1} = x_n + v_n * dt</li>
     *   <li>Velocity update: v_{n+1} = v_n + a_n * dt</li>
     * </ul>
     * <p>
     * The method updates the position first using the current velocity, then updates
     * the velocity using the current acceleration. This order is important in the
     * Explicit Euler method and affects the accuracy of the simulation.
     *
     * @param body The celestial body whose state should be predicted
     * @param dt The time step duration in simulation time units
     */
    @Override
    public void setPrediction(Body body, double dt) {
        BodyHistory state = body.getState();
        // Predict x_{n+1} = x_n + v_n * dt
        state.getPosition()
             .addAndScaleInPlace(state.getVelocity(), dt);
        // Predict v_{n+1} = v_n + a_n * dt
        state.getVelocity()
             .addAndScaleInPlace(state.getHistory().acceleration(), dt);
    }

    /**
     * Rolls back a prediction to restore the body to its original state.
     * <p>
     * This method provides the mathematical inverse of the {@code setPrediction} operation,
     * effectively undoing the Explicit Euler step. The rollback process:
     * <ul>
     *   <li>Reverses the velocity update: v_n = v_{n+1} - a_n * dt</li>
     *   <li>Reverses the position update: x_n = x_{n+1} - v_n * dt</li>
     * </ul>
     * <p>
     * Note that the negation of the time step (dt) in the calculations reverses
     * the effect of the original prediction. The order of operations is also reversed
     * compared to {@code setPrediction}.
     *
     * @param body The celestial body whose state should be rolled back
     * @param dt The time step duration that was used for the prediction
     */
    @Override
    public void rollBack(Body body, double dt) {
        BodyHistory state = body.getState();
        // Predict v_n = v_{n+1} - a_n * dt
        state.getVelocity()
             .addAndScaleInPlace(state.getHistory().acceleration(), -dt);
        // Predict x_n = x_{n+1} - v_n * dt
        state.getPosition()
             .addAndScaleInPlace(state.getVelocity(), -dt);
    }

    /**
     * Updates a body's state based on applied forces over a time interval.
     * <p>
     * This method finalizes the physics update by:
     * <ol>
     *   <li>Converting the applied force to acceleration (a = F/m)</li>
     *   <li>Storing the current position, velocity, and the new acceleration in the history buffer</li>
     * </ol>
     * <p>
     * In the Explicit Euler method, the position and velocity updates are already
     * handled by the {@code setPrediction} method. This method primarily ensures
     * that the new acceleration is properly calculated and stored for the next
     * integration step.
     *
     * @param body The celestial body whose state should be updated
     * @param f The force vector currently applied to the body
     * @param dt The time step duration in simulation time units
     */
    @Override
    public void update(Body body, Vector3D f, double dt) {
        BodyHistory state = body.getState();
        // Set a_{n+1} = f(x_{n+1}) / m
        // Now that all x, v, a have been updated, we can move the state to history
        state.updateHistory(f.scaleInPlace(1.0 / body.getMass()));
    }

}

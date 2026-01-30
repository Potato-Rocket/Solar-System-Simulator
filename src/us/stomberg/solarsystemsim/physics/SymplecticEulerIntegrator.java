package us.stomberg.solarsystemsim.physics;

/**
 * Implementation of the Integrator interface using the Symplectic Euler method,
 * also known as Semi-implicit Euler method.
 * <p>
 * The Symplectic Euler method is a first-order numerical integration technique
 * that updates velocities and positions in a specific sequence:
 * <ul>
 *   <li>First, velocity is updated: v(t+dt) = v(t) + a(t)*dt</li>
 *   <li>Then, position is updated using the new velocity: x(t+dt) = x(t) + v(t+dt)*dt</li>
 * </ul>
 * <p>
 * This sequence is crucial - unlike the Explicit Euler method that uses v(t) to update
 * position, the Symplectic Euler method uses the newly calculated v(t+dt), which
 * provides important benefits:
 * <ul>
 *   <li><b>Energy Conservation</b>: Preserves the Hamiltonian structure of mechanical
 *       systems, meaning energy is bounded over long simulations</li>
 *   <li><b>Stability</b>: More stable than the standard Euler method for orbital mechanics</li>
 *   <li><b>Symplecticity</b>: Preserves the phase space volume, a key property for
 *       accurate long-term simulations of conservative systems</li>
 *   <li><b>Efficiency</b>: Offers improved stability and energy conservation at minimal
 *       additional computational cost compared to Explicit Euler</li>
 * </ul>
 * <p>
 * This integrator is particularly well-suited for gravitational simulations and
 * celestial mechanics, where long-term energy conservation is critical for accurate
 * trajectory prediction.
 * <p>
 * More info can be found at <a href="https://en.wikipedia.org/wiki/Semi-implicit_Euler_method">Wikipedia - Semi-implicit Euler method</a>.
 */
public class SymplecticEulerIntegrator implements Integrator {

    /**
     * Predicts a body's future position and velocity using the Symplectic Euler method.
     * <p>
     * This implementation follows the key sequence that defines Symplectic Euler:
     * <ul>
     *   <li>First velocity update: v_{n+1} = v_n + a_n * dt</li>
     *   <li>Then position update using new velocity: x_{n+1} = x_n + v_{n+1} * dt</li>
     * </ul>
     * <p>
     * This ordering is what makes the method "symplectic" - by using the updated velocity
     * to advance the position. The algorithm preserves certain geometric properties of
     * the physical system, particularly for Hamiltonian systems like gravitational motion.
     * <p>
     * Though still a first-order method (like Explicit Euler), this approach provides
     * better stability for oscillatory systems and conserves energy over long simulations.
     *
     * @param body The celestial body whose state should be predicted
     * @param dt The time step duration in simulation time units
     */
    @Override
    public void setPrediction(Body body, double dt) {
        BodyHistory state = body.getState();
        // Predict v_{n+1} = v_n + a_n * dt
        state.getVelocity()
             .addAndScaleInPlace(state.getHistory().acceleration(), dt);
        // Predict x_{n+1} = x_n + v_{n+1} * dt
        state.getPosition()
             .addAndScaleInPlace(state.getVelocity(), dt);
    }

    /**
     * Rolls back a prediction to restore the body to its original state.
     * <p>
     * This method provides the mathematical inverse of the {@code setPrediction} operation,
     * effectively undoing the Symplectic Euler step. The rollback process follows the
     * reverse sequence:
     * <ul>
     *   <li>First reverse position update: x_n = x_{n+1} - v_{n+1} * dt</li>
     *   <li>Then reverse velocity update: v_n = v_{n+1} - a_n * dt</li>
     * </ul>
     * <p>
     * Note that the order of operations is reversed compared to {@code setPrediction}.
     * This ensures the correct mathematical inverse of the prediction operation.
     *
     * @param body The celestial body whose state should be rolled back
     * @param dt The time step duration that was used for the prediction
     */
    @Override
    public void rollBack(Body body, double dt) {
        BodyHistory state = body.getState();
        // Predict x_n = x_{n+1} - v_{n+1} * dt
        state.getPosition()
             .addAndScaleInPlace(state.getVelocity(), -dt);
        // Predict v_n = v_{n+1} - a_n * dt
        state.getVelocity()
             .addAndScaleInPlace(state.getHistory().acceleration(), -dt);
    }

    /**
     * Updates a body's state based on applied forces over a time interval.
     * <p>
     * This method finalizes the physics update by calculating the new acceleration
     * based on the applied force and storing the current state in the history buffer.
     * <p>
     * In the Symplectic Euler method, the primary integration work (updating position
     * and velocity) is already handled by the {@code setPrediction} method. This method
     * simply:
     * <ul>
     *   <li>Calculates the new acceleration from the applied force: a_{n+1} = f/m</li>
     *   <li>Stores the current state (position, velocity, new acceleration) for the next step</li>
     * </ul>
     * <p>
     * The calculated acceleration will be used in the next integration step to
     * update the velocity.
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

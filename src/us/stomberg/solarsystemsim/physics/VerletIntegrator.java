package us.stomberg.solarsystemsim.physics;

/**
 * Implementation of the Integrator interface using the Velocity Verlet algorithm.
 * <p>
 * The Velocity Verlet method is a second-order numerical integration technique
 * commonly used in molecular dynamics simulations and astronomical calculations.
 * It updates positions and velocities using the following formulas:
 * <ul>
 *   <li>x(t+dt) = x(t) + v(t)*dt + 0.5*a(t)*dt²</li>
 *   <li>v(t+dt) = v(t) + 0.5*[a(t) + a(t+dt)]*dt</li>
 * </ul>
 * <p>
 * Key characteristics of the Velocity Verlet method include:
 * <ul>
 *   <li><b>Accuracy</b>: Second-order accuracy (error proportional to dt²)</li>
 *   <li><b>Stability</b>: More stable than Euler methods for oscillatory systems</li>
 *   <li><b>Symplecticity</b>: Approximately preserves energy in conservative systems</li>
 *   <li><b>Time-Reversibility</b>: The algorithm is time-reversible, improving long-term stability</li>
 * </ul>
 * <p>
 * This implementation is particularly well-suited for gravitational simulations
 * where energy conservation and long-term stability are important. The method
 * handles oscillatory motion (like orbits) much more accurately than first-order
 * methods such as Explicit Euler.
 * <p>
 * More info can be found at <a href="https://en.wikipedia.org/wiki/Verlet_integration">Wikipedia - Verlet Integration</a>.
 */
public class VerletIntegrator implements Integrator {

    /**
     * Predicts a body's future position and velocity using the Velocity Verlet method.
     * <p>
     * This implementation follows the first part of the Velocity Verlet algorithm:
     * <ul>
     *   <li>Position update: x_{n+1} = x_n + v_n*dt + 0.5*a_n*dt²</li>
     *   <li>Initial velocity estimate: v_{n+1} = v_n + a_n*dt</li>
     * </ul>
     * <p>
     * The position update includes both the velocity component and half of the
     * acceleration component over the squared time step. This provides a more
     * accurate trajectory prediction than first-order methods, especially for
     * orbital motion.
     * <p>
     * The velocity estimate is only preliminary and will be corrected in the
     * {@code update} method once new forces are calculated.
     *
     * @param body The celestial body whose state should be predicted
     * @param dt The time step duration in simulation time units
     */
    @Override
    public void setPrediction(Body body, double dt) {
        BodyHistory state = body.getState();
        // Predict x_{n+1} = x_n + v_n * dt + 1/2 * a_n * dt^2
        state.getPosition()
             .addAndScaleInPlace(state.getVelocity(), dt)
             .addAndScaleInPlace(state.getHistory().acceleration(), (dt * dt) * 0.5);
        // Predict v_{n+1} = v_n + a_n * dt
        state.getVelocity()
             .addAndScaleInPlace(state.getHistory().acceleration(), dt);
    }

    /**
     * Rolls back a prediction to restore the body to its original state.
     * <p>
     * This method provides the mathematical inverse of the {@code setPrediction} operation,
     * effectively undoing the Velocity Verlet step. The rollback process follows these steps:
     * <ul>
     *   <li>Reverse velocity update: v_n = v_{n+1} - a_n*dt</li>
     *   <li>Reverse position update: x_n = x_{n+1} - v_n*dt - 0.5*a_n*dt²</li>
     * </ul>
     * <p>
     * Note that the order of operations is reversed compared to {@code setPrediction},
     * and the signs of the time step terms are negated to reverse the prediction.
     * This preserves the time-reversibility property of the Verlet algorithm.
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
        // Predict x_n = x_{n+1} - v_n * dt - 1/2 * a_n * dt^2
        state.getPosition()
             .addAndScaleInPlace(state.getVelocity(), -dt)
             .addAndScaleInPlace(state.getHistory().acceleration(), (dt * dt) * -0.5);
    }

    /**
     * Updates a body's state based on applied forces over a time interval.
     * <p>
     * This method implements the second part of the Velocity Verlet algorithm,
     * which corrects the velocity estimate using the newly calculated forces:
     * <ul>
     *   <li>Calculate new acceleration: a_{n+1} = f(x_{n+1})/m</li>
     *   <li>Update velocity: v_{n+1} = v_n + 0.5*(a_n + a_{n+1})*dt</li>
     * </ul>
     * <p>
     * The velocity correction uses the average of the old and new accelerations,
     * which is a key feature of the VelocityVerlet method that improves its
     * accuracy and stability compared to simpler methods. This averaging approach
     * is what gives the method its symplectic properties.
     * <p>
     * After updating the velocity, the method stores the current state in the
     * history buffer for use in the next integration step.
     *
     * @param body The celestial body whose state should be updated
     * @param f The force vector currently applied to the body
     * @param dt The time step duration in simulation time units
     */
    @Override
    public void update(Body body, Vector3D f, double dt) {
        BodyHistory state = body.getState();
        // Find a_{n+1} = f(x_{n+1}) / m
        // Set v_{n+1} = v_n + (a_{n+1} + a_n) / 2 * dt
        state.getVelocity().copyFrom(state.getHistory().velocity())
             .addInPlace(f.scaleInPlace(1.0 / body.getMass())
                          .addInPlace(state.getHistory().acceleration())
                          .scaleInPlace(0.5 * dt));
        // Now that all x, v, a have been updated, we can move the state to history
        state.updateHistory(f);
    }

}

package us.stomberg.solarsystemsim.physics;

/**
 * The TimeStep class manages the temporal progression of the physics simulation.
 * <p>
 * This class provides mechanisms to:
 * <ul>
 *   <li>Track the total simulation time step</li>
 *   <li>Subdivide time steps when handling collisions</li>
 *   <li>Track elapsed and remaining time within each step</li>
 *   <li>Provide a clean reset mechanism between simulation iterations</li>
 * </ul>
 * <p>
 * TimeStep plays a crucial role in collision handling, allowing the simulation to
 * advance precisely to the moment of collision rather than using the full time step,
 * which ensures accurate physical interactions between bodies.
 * <p>
 * The class maintains three key time values:
 * <ul>
 *   <li>dt: The total time step duration</li>
 *   <li>elapsed: How much of the current time step has been processed</li>
 *   <li>remaining: How much of the current time step is left to process</li>
 * </ul>
 */
public class TimeStep {

    /**
     * The total time step duration in simulation time units.
     * <p>
     * This value represents the basic time quantum for the simulation and
     * determines how far the simulation advances in each update cycle.
     */
    private final double dt;
    /**
     * The time that has elapsed within the current time step.
     * <p>
     * This tracks how much of the current time step has been processed,
     * which is particularly important when handling collisions that
     * require subdivision of the time step.
     */
    private double elapsed;
    /**
     * The time remaining to be processed in the current time step.
     * <p>
     * This value decreases as the simulation processes portions of the
     * time step, especially when collisions are detected and handled.
     */
    private double remaining;

    /**
     * Creates a new TimeStep with the specified duration.
     * <p>
     * Initializes a time step with zero elapsed time and the full
     * time step remaining to be processed.
     *
     * @param dt The total duration of the time step in simulation time units
     * @throws IllegalArgumentException If dt is negative or zero
     */
    public TimeStep(double dt) {
        this.dt = dt;
        this.elapsed = 0;
        this.remaining = dt;
    }

    /**
     * Updates the time step by advancing time by the specified amount.
     * <p>
     * This method increases the elapsed time and decreases the remaining time
     * by the specified amount. It's typically called after processing a portion
     * of the simulation, especially when handling collisions that require
     * stopping at specific points in time.
     *
     * @param dt The amount of time to advance, in simulation time units
     * @throws IllegalArgumentException If dt is negative or greater than remaining time
     */
    public void update(double dt) {
        this.elapsed += dt;
        this.remaining -= dt;
    }

    /**
     * Resets the time step to its initial state.
     * <p>
     * This method clears the elapsed time and restores the remaining time
     * to the full time step duration. It's typically called at the beginning
     * of each simulation cycle to prepare for a new round of calculations.
     */
    public void reset() {
        this.elapsed = 0;
        this.remaining = dt;
    }

    /**
     * Returns the total duration of the time step.
     * <p>
     * This value represents the full simulation time quantum and
     * remains constant throughout the simulation.
     *
     * @return The total time step duration in simulation time units
     */
    public double getDt() {
        return dt;
    }

    /**
     * Returns the time that has elapsed within the current time step.
     * <p>
     * This value increases as portions of the time step are processed,
     * and is reset to zero at the beginning of each new time step cycle.
     *
     * @return The elapsed time in simulation time units
     */
    public double getElapsed() {
        return elapsed;
    }

    /**
     * Returns the time remaining to be processed in the current time step.
     * <p>
     * This value decreases as portions of the time step are processed
     * and is restored to the full time step duration when reset is called.
     *
     * @return The remaining time in simulation time units
     */
    public double getRemaining() {
        return remaining;
    }

    /**
     * Determines whether the current time step has been fully processed.
     * <p>
     * This method checks if the remaining time is effectively zero, using
     * a small epsilon value (1.0e-12) to account for floating-point precision issues.
     * When this method returns true, the simulation should advance to the next
     * time step cycle.
     *
     * @return <code>true</code> if the time step is complete, <code>false</code> otherwise
     */
    public boolean isFinished() {
        return remaining <= 1.0e-12;
    }

}

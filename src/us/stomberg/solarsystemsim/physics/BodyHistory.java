package us.stomberg.solarsystemsim.physics;

/**
 * The BodyHistory class provides a mechanism for tracking the position, velocity,
 * and acceleration history of a physics body over a fixed number of time steps.
 * <p>
 * This class implements an efficient circular buffer that maintains historical state
 * information, enabling calculations such as linear velocity based on position changes
 * over time. It is particularly useful for simulation stability and analysis.
 * <p>
 * The history storage is pre-allocated with reusable state objects to minimize
 * garbage collection overhead during simulation.
 */
public class BodyHistory {

    /**
     * Immutable record representing a complete state snapshot of a body at a point in time.
     * <p>
     * Contains the position, velocity, and acceleration vectors that fully describe
     * the physical state of a body.
     *
     * @param position The position vector of the body
     * @param velocity The velocity vector of the body
     * @param acceleration The acceleration vector of the body
     */
    public record State(Vector3D position, Vector3D velocity, Vector3D acceleration) {}
    /**
     * The maximum number of historical states to maintain in the buffer.
     * <p>
     * This defines the size of the circular buffer used for history tracking.
     */
    public static final int historyLength = 2;
    /**
     * The current position vector of the body.
     * <p>
     * This vector is updated externally and represents the most recent position.
     */
    private final Vector3D position;
    /**
     * The current velocity vector of the body.
     * <p>
     * This vector is updated externally and represents the most recent velocity.
     */
    private final Vector3D velocity;
    /**
     * Circular buffer of historical states.
     * <p>
     * Uses pre-allocated <code>State</code> objects to avoid creating unnecessary objects
     * during simulation.
     */
    private final State[] history = new State[historyLength];
    /**
     * Index of the most recent state in the history array.
     * <p>
     * Updated cyclically as new states are added.
     */
    private int headIndex = 0;
    /**
     * The number of valid states in the history buffer.
     * <p>
     * Increases up to <code>historyLength</code> as states are added.
     */
    private int size = 0;

    /**
     * Creates a new history tracker for a body with the given position and velocity.
     * <p>
     * Initializes the history buffer with empty states that will be populated
     * as the simulation progresses.
     *
     * @param position The initial position vector of the body
     * @param velocity The initial velocity vector of the body
     */
    public BodyHistory(Vector3D position, Vector3D velocity) {
        this.position = position;
        this.velocity = velocity;

        // Pre-allocate all State objects
        for (int i = 0; i < historyLength; i++) {
            history[i] = new State(new Vector3D(), new Vector3D(), new Vector3D());
        }
    }

    /**
     * Gets the current position vector of the body.
     * <p>
     * This returns a reference to the internal position vector, not a copy.
     *
     * @return The current position vector
     */
    public Vector3D getPosition() {
        return position;
    }

    /**
     * Gets the current velocity vector of the body.
     * <p>
     * This returns a reference to the internal velocity vector, not a copy.
     *
     * @return The current velocity vector
     */
    public Vector3D getVelocity() {
        return velocity;
    }

    /**
     * Calculates the linear velocity based on position change over time.
     * <p>
     * This method computes the velocity by finding the difference between the current
     * position and the position from the most recent history entry, then dividing
     * by the remaining time in the step. If no history is available, it returns
     * a copy of the current velocity.
     * <p>
     * This calculation is useful for stable velocity estimates in numerical simulations.
     *
     * @param step The time step containing timing information
     * @return A vector representing the calculated linear velocity
     */
    public Vector3D findLinearVelocity(TimeStep step) {
        if (size == 0) {
            return velocity.copy();
        }
        State prev = history[headIndex];
        return position.subtract(prev.position()).scaleInPlace(1.0 / step.getRemaining());
    }

    /**
     * Gets the most recent historical state.
     * <p>
     * This method returns the state at the current head position in the circular buffer,
     * or <code>null</code> if no historical states have been recorded yet.
     *
     * @return The most recent historical state, or <code>null</code> if none exists
     */
    public State getHistory() {
        return size > 0 ? history[headIndex] : null;
    }

    /**
     * Gets a historical state from a specified number of steps in the past.
     * <p>
     * Retrieves a state from the circular buffer at the specified number of steps
     * before the current head position. If the requested state is outside the
     * buffer boundaries, returns <code>null</code>.
     *
     * @param steps Number of steps back in history to retrieve (0 is most-recent)
     * @return The historical state at the specified position, or <code>null</code> if invalid
     */
    public State getHistory(int steps) {
        if (steps >= historyLength || steps < 0) {
            return null;
        }
        int index = (headIndex - steps + historyLength) % historyLength;
        return history[index];
    }

    /**
     * Records the current state in the history buffer.
     * <p>
     * This method updates the circular buffer with the current position, velocity,
     * and the provided acceleration.
     * It reuses existing <code>State</code> objects
     * to avoid creating unnecessary objects during the simulation.
     * <p>
     * The history buffer maintains up to <code>historyLength</code> states, with
     * older states being overwritten as new ones are added.
     *
     * @param acceleration The current acceleration vector to store with this state
     */
    public void updateHistory(Vector3D acceleration) {
        if (size < historyLength) {
            size++;
        }

        // Move head to the next position (circular)
        headIndex = (headIndex + 1) % historyLength;

        // Reuse the existing State object instead of creating a new one
        State state = history[headIndex];
        state.position().copyFrom(position);
        state.velocity().copyFrom(velocity);
        state.acceleration().copyFrom(acceleration);
    }

}

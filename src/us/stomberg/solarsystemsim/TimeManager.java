package us.stomberg.solarsystemsim;

/**
 * The TimeManager class manages time-related aspects of the simulation.
 * It handles simulation time tracking, timescale adjustments, and frame rate calculations.
 * <p>
 * This class is thread-safe with synchronization on the lock object when accessing shared state.
 * <p>
 * TODO: Make FPS a concern of the main loop, make this an object of Physics, perhaps integrated with timestep
 */
public class TimeManager {

    private static final Object lock = new Object();

    /**
     * The length of the simulation in seconds.
     */
    private static double duration = 0;
    /**
     * Current time scale target.
     */
    private static double timescaleCap = 1.0;
    /**
     * An epsilon value for comparing floats.
     */
    private static final double EPSILON = 0.01;
    /**
     * The time of the previously rendered frame.
     */
    private static long prevFrame = -1;
    /**
     * The calculated current FPS.
     */
    private static double currentFPS = 0;
    /**
     * The length of the simulation since the timescale was last changed.
     */
    private static double timescaleDuration = 0;
    /**
     * The time of the previous physics timestep.
     */
    private static long timescaleStart = -1;
    /**
     * The calculated actual current timescale.
     */
    private static double currentTimeScale = 1.0;

    /**
     * Represents the types of possible changes to the timescale cap in the simulation.
     * <p>
     * This enum is used to indicate whether the timescale should be increased
     * or decreased when modifying the simulation speed.
     */
    public enum TimeScaleChangeType {
        INCREMENT,
        DECREMENT
    }

    /**
     * Increments the simulation time by one time step.
     * <p>
     * This method updates both the total simulation duration and the duration
     * since the timescale was last changed. It also recalculates the current
     * effective timescale based on the elapsed real time.
     *
     * @param dt The time increment in simulation seconds
     */
    public static void incrementDuration(double dt) {
        synchronized (lock) {
            duration += dt;
            timescaleDuration += dt;
            currentTimeScale = timescaleDuration / getTimescaleSeconds();
        }
    }

    /**
     * Retrieves the total duration of the simulation in seconds.
     *
     * @return the duration of the simulation in seconds
     */
    public static double getDuration() {
        synchronized (lock) {
            return duration;
        }
    }

    /**
     * Determines whether the physics system should execute another timestep.
     * <p>
     * This decision is based on comparing the target timescale cap with the actual
     * timescale that has been achieved. This helps maintain synchronization between
     * simulation time and real time according to the desired timescale.
     *
     * @return true if the physics system should update, false otherwise
     */
    public static boolean shouldUpdatePhysics() {
        synchronized (lock) {
            return timescaleDuration < getTimescaleSeconds() * timescaleCap;
        }
    }

    /**
     * Calculates the elapsed time in seconds since the timescale was last updated.
     * <p>
     * This method handles the initialization of the timescale tracking if it hasn't
     * been initialized yet. It provides a reference point for calculating the
     * actual achieved timescale.
     *
     * @return The elapsed time in seconds since the timescale was last updated
     */
    private static double getTimescaleSeconds() {
        synchronized (lock) {
            if (timescaleStart == -1) {
                timescaleStart = System.nanoTime();
            }
            return (double)(System.nanoTime() - timescaleStart) / 1.0E9;
        }
    }

    /**
     * Determines whether the next frame should be rendered.
     * <p>
     * This decision is based on the time elapsed since the previous frame
     * and the frame rate limit specified in the setup configuration.
     * It helps maintain a consistent frame rate without exceeding
     * the specified limit.
     *
     * @return true if enough time has passed to render the next frame, false otherwise
     */
    public static boolean shouldRenderFrame() {
        return System.nanoTime() - prevFrame >= (long) (1.0E9 / Setup.getFrameLimit());
    }

    /**
     * Calculates the current actual frames per second.
     * <p>
     * This method measures the real time between successive frame renders
     * and updates the currentFPS value accordingly. It should be called
     * once per frame to maintain accurate FPS tracking.
     */
    public static void updateFPS() {
        long time = System.nanoTime();
        if (prevFrame != -1) {
            currentFPS = 1.0E9 / (time - prevFrame);
        }
        prevFrame = time;
    }

    /**
     * Increases or decreases the timescale based on the specified change type.
     * <p>
     * This method is bound to a keymap in the <code>Keymaps</code> class and
     * adjusts the simulation speed relative to real time. The timescale
     * is constrained to powers of ten multiplied by 1, 2, or 5 to provide
     * intuitive speed increments.
     * <p>
     * When the timescale is modified, the timescale tracking is reset to
     * ensure smooth transitions between different speeds.
     *
     * @param change The type of change to apply (increment or decrement)
     */
    public static void modifyTimescaleCap(TimeScaleChangeType change) {
        synchronized (lock) {
            int decade = (int) Math.floor(Math.log10(timescaleCap));
            double decadeFactor = Math.pow(10, decade);

            double factor = timescaleCap / decadeFactor;
            if (Math.abs(factor - 1) < EPSILON) {
                switch (change) {
                    case INCREMENT:
                        timescaleCap = decadeFactor * 2;
                        break;
                    case DECREMENT:
                        timescaleCap = decadeFactor * 0.5;
                        break;
                }
            } else if (Math.abs(factor - 2) < EPSILON) {
                switch (change) {
                    case INCREMENT:
                        timescaleCap = decadeFactor * 5;
                        break;
                    case DECREMENT:
                        timescaleCap = decadeFactor;
                        break;
                }
            } else if (Math.abs(factor - 5) < EPSILON) {
                switch (change) {
                    case INCREMENT:
                        timescaleCap = decadeFactor * 10;
                        break;
                    case DECREMENT:
                        timescaleCap = decadeFactor * 2;
                        break;
                }
            } else {
                timescaleCap = decadeFactor;
            }

            timescaleStart = System.nanoTime();
            timescaleDuration = 0;

        }
    }

    /**
     * Gets the current target timescale.
     * <p>
     * This value represents the desired ratio of simulation time to real time,
     * expressed in simulation seconds per real second.
     *
     * @return The current target timescale
     */
    public static double getTimescaleCap() {
        synchronized (lock) {
            return timescaleCap;
        }
    }

    /**
     * Gets the current calculated actual timescale.
     * <p>
     * This value represents the actual achieved ratio of simulation time to real time,
     * which may differ from the target timescale due to processing limitations.
     * It is expressed in simulation seconds per real second.
     *
     * @return The current actual timescale
     */
    public static double getCurrentTimeScale() {
        synchronized (lock) {
            return currentTimeScale;
        }
    }

    /**
     * Gets the current frames per second measurement.
     * <p>
     * This value represents the actual rendering frame rate achieved by the
     * application. The value is calculated by averaging frame durations
     * over approximately one second of real time.
     *
     * @return The current FPS value
     */
    public static double getCurrentFPS() {
        return currentFPS;
    }

}

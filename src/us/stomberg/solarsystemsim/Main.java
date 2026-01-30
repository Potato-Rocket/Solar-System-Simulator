package us.stomberg.solarsystemsim;

import us.stomberg.solarsystemsim.graphics.DrawSpace;
import us.stomberg.solarsystemsim.physics.Physics;
import us.stomberg.solarsystemsim.physics.VerletIntegrator;

/**
 * The Main class serves as the top-level runner for the solar system simulation.
 * It coordinates the physics engine, graphics rendering, and manages the simulation lifecycle.
 * <p>
 * This class sets up the simulation environment, creates the physics system,
 * and maintains separate threads for rendering and physics calculations to ensure
 * smooth performance.
 */
public class Main {

    /**
     * Controls whether the simulation should continue running.
     * <p>
     * This flag is accessed by multiple threads and marked volatile to ensure
     * visibility of changes across threads without needing explicit synchronization.
     */
    private static volatile boolean running = true;
    /**
     * The physics engine instance that manages all physical interactions in the simulation.
     * <p>
     * This instance handles gravitational calculations, body movements, and collisions
     * between celestial bodies in the simulated solar system.
     */
    private static Physics physics;

    /**
     * The entry point for the solar system simulation application.
     * <p>
     * This method initializes the simulation environment by:
     * <ol>
     *     <li>Loading configuration parameters from the setup file
     *     <li>Creating the physics engine with the selected integration algorithm
     *     <li>Initializing the graphical display window
     *     <li>Starting a separate thread for physics calculations
     *     <li>Managing the rendering loop in the main thread
     * </ol>
     * <p>
     * The main thread focuses on rendering at the specified frame rate,
     * while the physics thread handles body position and velocity updates
     * according to the specified timescale.
     *
     * @param args Command line arguments (not used in the current implementation)
     * @throws InterruptedException If the thread is interrupted while waiting for the physics thread to complete
     */
    public static void main(String[] args) throws InterruptedException {
        // Initialize simulation configuration and components
        Setup.read();
        physics = new Physics(new VerletIntegrator());
        // TODO: Set up graphics window first, and show a loading indicator until everything is loaded
        DrawSpace drawSpace = new DrawSpace();

        // Start the physics simulation in a separate thread
        Thread simulation = new Thread(new Simulation());
        simulation.start();

        // The Main rendering loop continues until the program is explicitly terminated
        while (running) {
            if (TimeManager.shouldRenderFrame()) {
                drawSpace.repaint();
                TimeManager.updateFPS();
            }
        }

        // Ensure a clean shutdown by waiting for the physics thread to complete
        simulation.join();
        System.exit(0);
    }

    /**
     * Inner class that handles the physics update loop in a separate thread.
     * <p>
     * This separation allows the physics calculations to run independent of
     * the rendering loop, enabling better performance and more consistent
     * simulation behavior regardless of rendering frame rate.
     */
    private static class Simulation implements Runnable {

        /**
         * Executes the physics simulation loop.
         * <p>
         * This method continuously checks if physics updates should be performed
         * based on the current timescale and triggers body updates accordingly.
         * The TimeManager ensures that physics calculations maintain the correct
         * pace relative to real time and the selected timescale.
         */
        @Override
        public void run() {
            while (running) {
                if (TimeManager.shouldUpdatePhysics()) {
                    physics.updateBodies();
                }
            }
        }
    }

    /**
     * Provides access to the physics engine instance.
     * <p>
     * This method allows other components of the application to access
     * the physics system for retrieving body information, calculating
     * physical properties, or performing other physics-related operations.
     *
     * @return The physics engine instance used by the simulation
     */
    public static Physics getPhysics() {
        return physics;
    }

    /**
     * Signals the simulation to stop execution.
     * <p>
     * This method sets the running flag to false, which causes both the main
     * rendering loop and the physics simulation thread to terminate gracefully.
     * It can be called from event handlers or other components to initiate
     * a clean shutdown of the application.
     */
    public static void stopRunning() {
        running = false;
    }

}

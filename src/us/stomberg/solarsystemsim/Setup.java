package us.stomberg.solarsystemsim;

import java.awt.*;
import java.io.*;
import java.util.ArrayList;
import java.util.Objects;
import java.util.Properties;
import java.util.function.Function;
import java.util.logging.Logger;

import us.stomberg.solarsystemsim.physics.Body;
import us.stomberg.solarsystemsim.physics.BodyBuilder;
import us.stomberg.solarsystemsim.physics.BodyFactory;
import us.stomberg.solarsystemsim.physics.Vector3D;

import javax.naming.ConfigurationException;

/**
 * The Setup class manages configuration and initialization for the simulation.
 * It handles loading settings from properties files, parsing configuration values,
 * and providing default configurations when necessary.
 * <p>
 * This class is responsible for reading graphics settings, physics parameters,
 * and celestial body definitions. It provides access to these settings through
 * various getter methods.
 */
public class Setup {

    /**
     * Stores parameters for controlling the visual aspects of the simulation.
     * <p>
     * This record encapsulates settings related to rendering performance,
     * visual elements like body trails, and user interaction preferences
     * for camera movement and zooming.
     *
     * @param frameLimit      The maximum number of frames to render per second
     * @param shouldDrawTrail Whether to draw trajectory trails behind bodies
     * @param trailHasAlpha   Whether the trails should have transparency (fade out)
     * @param collisions      Whether to enable collision detection between bodies
     * @param trailLength     The length of trails in degrees of revolution
     * @param trailResolution The spacing between trail points in degrees of revolution
     * @param rotatePrecision The sensitivity factor for camera rotation
     * @param scalePrecision  The zoom factor applied when scaling the view
     */
    private record GraphicsConfig(
            double frameLimit,
            boolean shouldDrawTrail,
            boolean trailHasAlpha,
            boolean collisions,
            double trailLength,
            double trailResolution,
            double rotatePrecision,
            double scalePrecision
    ) {}
    /**
     * Stores parameters for the physical simulation engine.
     * <p>
     * This record encapsulates fundamental physics settings that control
     * how the simulation calculates gravitational forces and how time
     * advances during each update cycle.
     *
     * @param gravity  The universal gravitational constant value for the simulation
     * @param timeStep The size of each physics update step in simulation seconds
     */
    private record SimulationConfig(double gravity, double timeStep) {}

    /**
     * The logger instance for recording configuration events and errors.
     * <p>
     * This logger captures information about configuration loading,
     * parsing errors, and fallback to default settings.
     */
    private static final Logger logger = Logger.getLogger(Setup.class.getName());
    /**
     * The path to the application's configuration directory.
     * <p>
     * This directory is located in the user's home folder and contains
     * the properties files for simulation settings and celestial body definitions.
     */
    private static final String CONFIG_DIR = System.getProperty("user.home") + "/.config/solarsystemsim/";
    /**
     * Default graphics configuration used when settings cannot be loaded.
     * <p>
     * These values provide a reasonable starting point for visual settings
     * and ensure the application can run even without a configuration file.
     */
    private static final GraphicsConfig defaultGraphicsConfig = new GraphicsConfig(
            60,
            true,
            false,
            false,
            180,
            1.0,
            1.0,
            1.1
    );
    /**
     * Default physics configuration used when settings cannot be loaded.
     * <p>
     * These values provide stable simulation parameters that work well
     * with the default celestial body configurations.
     */
    private static final SimulationConfig defaultSimulationConfig = new SimulationConfig(1.0, 0.004);
    /**
     * The collection of celestial bodies defined for the simulation.
     * <p>
     * This list contains all the bodies (stars, planets, etc.) that will be
     * created in the physics engine when the simulation starts.
     */
    private static final ArrayList<Body> generationData = new ArrayList<>();
    /**
     * The currently active graphics configuration.
     * <p>
     * This configuration is loaded from the setup file or set to default values
     * if the file cannot be read or contains invalid settings.
     */
    private static GraphicsConfig graphicsConfig;
    /**
     * The currently active physics simulation configuration.
     * <p>
     * This configuration controls the physical behavior of the simulation
     * and is loaded from the system setup file.
     */
    private static SimulationConfig simulationConfig;

    /**
     * Represents an exception that occurs during the processing of properties.
     * This exception serves as a general exception for property-related issues and
     * provides a base class for more specific property-related exceptions.
     */
    public static class PropertiesException extends ConfigurationException {
        public PropertiesException(String msg) {
            super(msg);
        }
    }

    /**
     * Represents an exception that occurs when a property is not found in a properties file.
     */
    public static class PropertyNotFoundException extends PropertiesException {
        public PropertyNotFoundException(String msg) {
            super(msg);
        }
    }

    /**
     * Represents an exception that occurs when a property is not formatted correctly.
     */
    public static class PropertyFormatException extends PropertiesException {
        public PropertyFormatException(String msg) {
            super(msg);
        }
    }

    /**
     * Represents an exception that occurs when a property file is not found.
     */
    public static class PropertyFileNotFoundException extends PropertiesException {
        public PropertyFileNotFoundException(String msg) {
            super(msg);
        }
    }

    // TODO: Make the integration method configurable
    /**
     * Loads all simulation configuration from setup files.
     * <p>
     * This method attempts to read the main configuration file and the
     * celestial body definitions file. If either file is missing or contains
     * invalid data, the method falls back to appropriate default values.
     * <p>
     * All exceptions are caught and handled internally, ensuring that the
     * simulation can always start even with configuration issues. Warning
     * messages are logged to help diagnose configuration problems.
     */
    public static void read() {
        // Sets up a .properties file using InputStream.
        try (InputStream input = new FileInputStream(CONFIG_DIR + "setup.properties")) {
            Properties setup = new Properties();
            setup.load(input);
            //Gets the values for how to display the simulation.
            graphicsConfig = new GraphicsConfig(
                    validateProperty(setup, "frameLimit", Double::parseDouble, defaultGraphicsConfig.frameLimit()),
                    validateProperty(setup, "drawTrail", Boolean::parseBoolean, defaultGraphicsConfig.shouldDrawTrail()),
                    validateProperty(setup, "trailAlpha", Boolean::parseBoolean, defaultGraphicsConfig.trailHasAlpha()),
                    validateProperty(setup, "collisions", Boolean::parseBoolean, defaultGraphicsConfig.collisions()),
                    validateProperty(setup, "trailLength", Double::parseDouble, defaultGraphicsConfig.trailLength()),
                    validateProperty(setup, "trailResolution", Double::parseDouble, defaultGraphicsConfig.trailResolution()),
                    validateProperty(setup, "rotatePrecision", Double::parseDouble, defaultGraphicsConfig.rotatePrecision()),
                    validateProperty(setup, "scalePrecision", Double::parseDouble, defaultGraphicsConfig.scalePrecision()));
            logger.info("Setup file read successfully.");
            //Reads the system setup file.
            readGeneration(setup.getProperty("generationFile"));
        } catch (IOException e) {
            Exception e2 = new PropertyFileNotFoundException("Invalid main setup file: " + e.getMessage());
            logger.warning(e2.toString());
            graphicsConfig = defaultGraphicsConfig;
            setDefaultSystem();
        } catch (PropertiesException e) {
            logger.warning(e.toString());
            setDefaultSystem();
        }
    }

    /**
     * Gets the maximum frames per second for rendering.
     * <p>
     * This value limits the rendering rate to prevent excessive
     * CPU usage when rendering at very high frame rates.
     *
     * @return The maximum frames per second setting
     */
    public static double getFrameLimit() {
        return Objects.requireNonNullElse(graphicsConfig, defaultGraphicsConfig).frameLimit();
    }

    /**
     * Checks whether body trajectory trails should be displayed.
     * <p>
     * When enabled, each celestial body will display a trail showing
     * its recent path through the simulation space.
     *
     * @return True if trails should be drawn, false otherwise
     */
    public static boolean isDrawingTrail() {
        return Objects.requireNonNullElse(graphicsConfig, defaultGraphicsConfig).shouldDrawTrail();
    }

    /**
     * Checks whether body trails should use transparency effects.
     * <p>
     * When enabled, the trails will fade out gradually rather than
     * having uniform color throughout their length.
     *
     * @return True if trails should use transparency, false otherwise
     */
    public static boolean trailHasAlpha() {
        return Objects.requireNonNullElse(graphicsConfig, defaultGraphicsConfig).trailHasAlpha();
    }

    /**
     * Checks whether collision detection is enabled.
     * <p>
     * When enabled, the simulation will detect and handle collisions
     * between celestial bodies, which may result in merged bodies or
     * altered trajectories.
     *
     * @return True if collision detection is enabled, false otherwise
     */
    public static boolean shouldCheckCollisions() {
        return Objects.requireNonNullElse(graphicsConfig, defaultGraphicsConfig).collisions();
    }

    /**
     * Gets the length of body trajectory trails.
     * <p>
     * This value determines how far back in time the trails will
     * extend, measured in degrees of revolution.
     *
     * @return The trail length in degrees
     */
    public static double getTrailLength() {
        return Objects.requireNonNullElse(graphicsConfig, defaultGraphicsConfig).trailLength();
    }

    /**
     * Gets the resolution of body trajectory trails.
     * <p>
     * This value determines the spacing between points in the trail,
     * measured in degrees of revolution. Lower values create smoother
     * trails but may impact performance.
     *
     * @return The trail resolution in degrees
     */
    public static double getTrailResolution() {
        return Objects.requireNonNullElse(graphicsConfig, defaultGraphicsConfig).trailResolution();
    }

    /**
     * Gets the sensitivity factor for camera rotation.
     * <p>
     * This value controls how quickly the camera rotates in response
     * to mouse movements. Higher values result in faster rotation.
     *
     * @return The rotation sensitivity factor
     */
    public static double getRotatePrecision() {
        return Objects.requireNonNullElse(graphicsConfig, defaultGraphicsConfig).rotatePrecision();
    }

    /**
     * Gets the zoom factor for camera scaling.
     * <p>
     * This value determines how quickly the view zooms in or out
     * in response to scroll-wheel movements. Values are typically
     * slightly above 1.0, with higher values causing faster zooming.
     *
     * @return The zoom factor for each step of scaling
     */
    public static double getScalePrecision() {
        return Objects.requireNonNullElse(graphicsConfig, defaultGraphicsConfig).scalePrecision();
    }

    /**
     * Gets the universal gravitational constant for the simulation.
     * <p>
     * This value determines the strength of gravitational attraction
     * between celestial bodies. It is a fundamental parameter that
     * affects all gravitational calculations in the physics engine.
     *
     * @return The gravitational constant value
     */
    public static double getGravity() {
        return Objects.requireNonNullElse(simulationConfig, defaultSimulationConfig).gravity();
    }

    /**
     * Gets the simulation time step interval.
     * <p>
     * This value determines how much simulation time passes during each
     * physics update cycle. Smaller values provide more accurate simulation
     * but require more computational resources.
     *
     * @return The time step in simulation seconds
     */
    public static double getTimeStep() {
        return Objects.requireNonNullElse(simulationConfig, defaultSimulationConfig).timeStep();
    }

    /**
     * Gets the collection of celestial bodies for the simulation.
     * <p>
     * This method provides access to the list of bodies that have been
     * loaded from the system setup file or created as default bodies.
     * The physics engine uses this collection to initialize the simulation.
     *
     * @return The list of celestial bodies
     */
    public static ArrayList<Body> getGenerationData() {
        return generationData;
    }

    /**
     * Reads the celestial body definitions from a system setup file.
     * <p>
     * This method parses the specified properties file to extract definitions
     * for all celestial bodies in the simulation. It creates Body objects with
     * the specified properties and adds them to the generationData collection.
     * <p>
     * Unlike the main read method, this method enforces strict validation of
     * properties and will throw exceptions for missing or invalid configuration.
     *
     * @param fileName Name of the system setup file to read
     * @throws PropertyFormatException If any property has an invalid format
     * @throws PropertyNotFoundException If a required property is missing
     * @throws PropertyFileNotFoundException If the system setup file is missing or cannot be read
     */
    private static void readGeneration(String fileName)
            throws PropertyFormatException, PropertyNotFoundException, PropertyFileNotFoundException {
        if (fileName == null) {
            throw new PropertyFileNotFoundException("No system setup file specified");
        }
        try (InputStream input = new FileInputStream(CONFIG_DIR + fileName)) {
            // Sets up the .properties file using InputStream.
            Properties generation = new Properties();
            generation.load(input);
            // Sets the gravitational constant and gets the keys for the different bodies.
            simulationConfig = new SimulationConfig(
                    parseProperty(generation, "gravity", Double::parseDouble),
                    parseProperty(generation, "timeStep", Double::parseDouble)
            );
            String bodiesStr = generation.getProperty("bodies");
            if (bodiesStr == null) {
                throw new PropertyNotFoundException("No bodies specified in system setup file");
            }
            String[] keys = bodiesStr.split(" ");
            // Reads the body data for each body key.
            generationData.clear();
            for (String key : keys) {
                Body newBody = new BodyBuilder().position(parseVector(generation, key + ".position"))
                                                .velocity(parseVector(generation, key + ".velocity"))
                                                .mass(parseProperty(generation, key + ".mass", Double::parseDouble))
                                                .density(parseProperty(generation, key + ".density", Double::parseDouble))
                                                .name(generation.getProperty(key + ".name"))
                                                .color(parseColor(generation, key + ".color")).build();
                generationData.add(newBody);
            }
            logger.info("System setup file read successfully.");
        } catch (IOException e) {
            throw new PropertyFileNotFoundException("Invalid system setup file: " + e.getMessage());
        }
    }

    /**
     * Creates a basic default solar system when configuration files cannot be loaded.
     * <p>
     * This method is called when the system setup file is missing or contains
     * critical errors. It creates a simple system with one star and two planets,
     * ensuring that the simulation always has something to display even when
     * configuration is unavailable.
     * <p>
     * It also sets physics parameters to their default values to ensure
     * appropriate simulation behavior for the default bodies.
     */
    private static void setDefaultSystem() {
        logger.info("Using default system.");
        generationData.clear();
        generationData.add(BodyFactory.createDefaultStar());
        generationData.add(BodyFactory.createDefaultInnerPlanet());
        generationData.add(BodyFactory.createDefaultOuterPlanet());
        simulationConfig = defaultSimulationConfig;
    }

    /**
     * Parses a string representation of a 3D vector into a Vector3D object.
     * <p>
     * This method extracts a vector from a properties file where the vector
     * is stored in the format "x,y,z" with x, y, and z being numeric values.
     * It performs strict validation of the property's existence
     * and the format of the vector representation.
     *
     * @param setup The properties file containing the vector data
     * @param key   The key identifying the vector property
     * @return A Vector3D object containing the parsed vector components
     * @throws PropertyNotFoundException If the specified key is not found in the properties file
     * @throws PropertyFormatException If the vector format is invalid (wrong number of elements or non-numeric values)
     */
    private static Vector3D parseVector(Properties setup, String key)
        throws PropertyNotFoundException, PropertyFormatException {
        String in = setup.getProperty(key);
        if (in == null) {
            throw new PropertyNotFoundException("Key " + key + " not found in properties file");
        }

        String[] split = in.split(",");
        if (split.length != 3) {
            throw new PropertyFormatException("Invalid vector length for key " + key +
                                                      ": Found " + split.length + ", expected 3");
        }

        try {
            double x = Double.parseDouble(split[0]);
            double y = Double.parseDouble(split[1]);
            double z = Double.parseDouble(split[2]);
            return new Vector3D(x, y, z);
        } catch (NumberFormatException e) {
            throw new PropertyFormatException("Invalid vector values for key " + key + ": " + e.getMessage());
        }
    }

    /**
     * Parses a string representation of RGB color values into a Color object.
     * <p>
     * This method extracts color information from a properties file where colors
     * are stored in the format "r,g,b" with r, g, and b being integer values
     * between 0 and 255. If values are outside this range, they are automatically
     * clamped to ensure valid color representation.
     * <p>
     * It performs strict validation of the property's existence
     * and the format of the vector representation.
     *
     * @param setup The properties file containing the color data
     * @param key   The key identifying the color property
     * @return A Color object containing the parsed RGB values (clamped to valid range)
     * @throws PropertyNotFoundException If the specified key is not found in the properties file
     * @throws PropertyFormatException If the color format is invalid (wrong number of elements or non-numeric values)
     */
    private static Color parseColor(Properties setup, String key)
        throws PropertyFormatException, PropertyNotFoundException {
        String in = setup.getProperty(key);
        if (in == null) {
            throw new PropertyNotFoundException("Key " + key + " not found in properties file");
        }

        String[] split = in.split(",");
        if (split.length != 3) {
            throw new PropertyFormatException("Invalid number of elements for key " + key +
                                                      ": Found " + split.length + ", expected 3");
        }

        try {
            int r = Math.clamp(Integer.parseInt(split[0]), 0, 255);
            int g = Math.clamp(Integer.parseInt(split[1]), 0, 255);
            int b = Math.clamp(Integer.parseInt(split[2]), 0, 255);
            return new Color(r, g, b);
        } catch (NumberFormatException e) {
            throw new PropertyFormatException("Invalid color values for key " + key + ": " + e.getMessage());
        }
    }

    /**
     * Safely extracts and converts a property value with fallback to a default.
     * <p>
     * This method attempts to read a property value and convert it to the desired type.
     * If the property is missing or cannot be converted, it falls back to the provided
     * default value. This approach is used for non-critical configuration properties
     * where using a default is acceptable.
     * <p>
     * The method logs warnings when falling back to defaults to help diagnose
     * configuration issues.
     *
     * @param <T>          The target type for the converted property
     * @param setup        The properties file to read from
     * @param key          The key identifying the property
     * @param converter    The function to convert from string to target type
     * @param defaultValue The default value to use if conversion fails
     * @return The converted property value or the default value if unavailable/invalid
     */
    private static <T> T validateProperty(Properties setup, String key, Function<String, T> converter, T defaultValue) {
        String in = setup.getProperty(key);
        if (in == null) {
            logger.warning("No value found for key " + key + ", using default value " + defaultValue + ".");
            return defaultValue;
        }
        try {
            return converter.apply(in);
        } catch (NumberFormatException e) {
            logger.warning("Invalid value for key " + key + ": " + e.getMessage() +
                                   ", using default value " + defaultValue + ".");
            return defaultValue;
        }
    }

    /**
     * Extracts and converts a required property value with strict validation.
     * <p>
     * This method attempts to read a property value and convert it to the desired type.
     * Unlike validateProperty, this method throws exceptions when properties are missing
     * or invalid rather than falling back to defaults. It is used for critical
     * configuration properties where defaults are not acceptable.
     * <p>
     * The method performs strict validation of both the existence of the property
     * and the format of its value.
     *
     * @param <T>       The target type for the converted property
     * @param setup     The properties file to read from
     * @param key       The key identifying the property
     * @param converter The function to convert from string to target type
     * @return The converted property value
     * @throws PropertyNotFoundException If the specified key is not found in the properties file
     * @throws PropertyFormatException If the property value cannot be converted using the provided function
     */
    private static <T> T parseProperty(Properties setup, String key, Function<String, T> converter)
            throws PropertyFormatException, PropertyNotFoundException {
        String in = setup.getProperty(key);
        if (in == null) {
            throw new PropertyNotFoundException("Key " + key + " not found in properties file");
        }
        try {
            return converter.apply(in);
        } catch (NumberFormatException e) {
            throw new PropertyFormatException("Invalid value for key " + key + ": " + e.getMessage());
        }
    }

}

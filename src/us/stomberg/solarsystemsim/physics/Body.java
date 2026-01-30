package us.stomberg.solarsystemsim.physics;

import us.stomberg.solarsystemsim.Main;
import us.stomberg.solarsystemsim.Setup;
import us.stomberg.solarsystemsim.graphics.FormatText;

import java.awt.*;
import java.util.ArrayList;
import java.util.LinkedList;

/**
 * Stores and updates physical information about a body. Updates information such as position and velocity as time
 * passes and as forces are applied to the body.
 */
public class Body {

    /**
     * Stores the distance between points one degree apart on a unit circle.
     */
    private static final double ONE_DEGREE = Math.PI * 2 / 360;
    /**
     * The number of bodies that have been generated.
     */
    private static int count = 0;
    /**
     * Stores the position vectors to render the trail.
     */
    private final LinkedList<Vector3D> trail = new LinkedList<>();
    /**
     * Stores the direction at the previous trail point.
     */
    private Vector3D prevTrail;
    /**
     * Stores the current and historical body state.
     */
    private final BodyHistory state;
    /**
     * Stores the color to use when drawing this body.
     */
    private final Color color;
    /**
     * The body's mass.
     */
    private double mass;
    /**
     * The body's radius.
     */
    private double density;
    /**
     * The body's radius.
     */
    private double radius;
    /**
     * The body's name.
     */
    private final String name;
    /**
     * The body's unique identification number.
     */
    private final int id;

    /**
     * Class constructor with all values specified by the user. Assigns specified values to their corresponding fields
     * and generates an id and initializes the trail.
     *
     * @param pos     Initial position of the body
     * @param vel     Initial velocity of the body
     * @param mass    Initial mass of the body
     * @param density Density of the body used to calculate radius
     * @param name    Name of the body
     * @param c       Color used for rendering the body
     */
    public Body(Vector3D pos, Vector3D vel, double mass, double density, String name, Color c) {
        this.mass = mass;
        this.density = density;
        this.name = name;
        this.color = c;

        state = new BodyHistory(pos, vel);

        id = count;
        count++;
        updateRadius();

        prevTrail = vel.normalize();
        trail.add(pos.copy());
    }

    /**
     * Updates the radius of the body based on its mass and density.
     * <p>
     * This method calculates the radius using the formula derived from the
     * volume of a sphere, where the volume is determined by the mass and
     * density of the body.
     */
    private void updateRadius() {
        radius = Math.cbrt((3 * (mass / density)) / (4 * Math.PI));
    }

    /**
     * Merges the specified body into this body by summing the masses, averaging the densities, and updating the radius.
     *
     * @param other The other body to merge into this body
     */
    public void merge(Body other) {
        // Average the densities of the bodies, weighted by the relative masses
        density = ((mass * density) + (other.mass * other.density)) / (mass + other.mass);
        // Add the masses together
        mass += other.mass;
        // Update the radius now that the mass and density have changed
        updateRadius();
    }

    // TODO: Make a trail graphical element class, and link it to a body - destruction of a body should not destroy the trail
    /**
     * Updates the body's trail if:
     * <ul>
     *     <li>The direction of the body's velocity has changed by at least the trail resolution
     *     <li>The body has traversed at least one-tenth of the initial simulation bounds
     * </ul>
     * Adds the body's current position to the list of trail vectors and removes the oldest trail element if
     * the trail is at its max length.
     */
    public void updateTrail() {
        // Find the change in direction
        Vector3D direction = state.getVelocity().normalize();
        // If the direction has changed more than the resolution
        // Or the body has traveled more than a tenth of the initial bounds
        if (direction.distance(prevTrail) > ONE_DEGREE * Setup.getTrailResolution() ||
                state.getPosition().distance(trail.getFirst()) > Main.getPhysics().getInitBounds() / 10) {
            // Insert a new point into the trail
            trail.addFirst(state.getPosition().copy());
            // If the trail is too long, remove the last point
            if (trail.size() > Setup.getTrailLength() / Setup.getTrailResolution()) {
                trail.removeLast();
            }
            prevTrail = direction;
        }
    }

    /**
     * Calculates the kinetic energy of the body using the formula:
     * KE = 0.5 * mass * velocity^2.
     *
     * @return The kinetic energy of the body as a double
     */
    public double getKineticEnergy() {
        return Math.pow(getState().getVelocity().magnitude(), 2) * mass * 0.5;
    }

    /**
     * Getter method for the body's trail data.
     *
     * @return The <code>ArrayList</code> of trail points
     */
    public LinkedList<Vector3D> getTrail() {
        return trail;
    }

    /**
     * Retrieves the current state of the body represented by a <code>BodyHistory</code> object.
     *
     * @return The current state of the body as a <code>BodyHistory</code> instance
     */
    public BodyHistory getState() {
        return state;
    }

    /**
     * Getter method for the body's mass.
     *
     * @return The body's mass
     */
    public double getMass() {
        return mass;
    }

    /**
     * Getter method for the body's radius.
     *
     * @return The body's radius
     */
    public double getRadius() {
        return radius;
    }

    /**
     * Getter method for the body's unique identification number.
     *
     * @return The body's id
     */
    public int getId() {
        return id;
    }

    /**
     * Getter method for the body's name.
     *
     * @return The body's name
     */
    public String getName() {
        return name;
    }

    /**
     * Getter method for the body's drawing color.
     *
     * @return The body's color
     */
    public Color getColor() {
        return color;
    }

    /**
     * Formats the body's attributes into a string list. Each array item is a line.
     *
     * @return A string array representing the body's attributes
     */
    public ArrayList<String> toStringArray() {
        synchronized (Main.getPhysics().lock) {
            ArrayList<String> string = new ArrayList<>();
            string.add("Body " + (id + 1) + ": " + name);
            string.add("Mass = " + FormatText.formatValue(mass, "kg", "t"));
            string.add("Radius = " + FormatText.formatValue(radius, "m", "km"));
            string.add("Position = " + state.getPosition().toString("m"));
            string.add("Velocity = " + state.getVelocity().toString("m/s"));
            string.add("KE = " + FormatText.formatValue(getKineticEnergy(), "J", "kJ"));
            return string;
        }
    }

    /**
     * Formats the body's name with its id in parentheses next to it.
     *
     * @return A representative string
     */
    @Override
    public String toString() {
        return name + " (" + id + ")";
    }

    /**
     * Two bodies are identical if they share the same unique id.
     *
     * @param o The other object to compare
     * @return Whether the other object is equal to this one
     */
    @Override
    public boolean equals(Object o) {
        if (o instanceof Body) {
            return ((Body) o).id == id;
        }
        return false;
    }

}

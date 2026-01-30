package us.stomberg.solarsystemsim.physics;

import java.awt.*;

/**
 * A builder class for creating instances of the Body class. This class uses the builder design pattern to allow for a
 * step-by-step configuration of the Body's attributes before creating the object.
 */
public class BodyBuilder {

    /**
     * The initial position vector of the body being built. Default is a zero vector (0,0,0).
     */
    private Vector3D pos = new Vector3D();
    /**
     * The initial velocity vector of the body being built. Default is a zero vector (0,0,0).
     */
    private Vector3D vel = new Vector3D();
    /**
     * The mass of the body being built. Default is 1.0.
     */
    private double mass = 1.0;
    /**
     * The density of the body being built. Default is 1.0.
     */
    private double density = 1.0;
    /**
     * The name of the body being built. Default is "Unnamed".
     */
    private String name = "Unnamed";
    /**
     * The color used for rendering the body. Default is white.
     */
    private Color c = Color.WHITE;
    /**
     * Tracks if all properties are still at default values.
     */
    private boolean isDefault = true;
    /**
     * Prevents the builder from being reused after building a body.
     */
    private boolean isBuilt = false;

    /**
     * Sets the position vector for the body.
     *
     * @param pos The position vector to use; if null, the default position is retained
     * @return This builder instance for method chaining
     */
    public BodyBuilder position(Vector3D pos) {
        if (pos != null) {
            isDefault = false;
            this.pos = pos;
        }
        return this;
    }

    /**
     * Sets the velocity vector for the body.
     *
     * @param vel The velocity vector to use; if null, the default velocity is retained
     * @return This builder instance for method chaining
     */
    public BodyBuilder velocity(Vector3D vel) {
        if (vel != null) {
            isDefault = false;
            this.vel = vel;
        }
        return this;
    }

    /**
     * Sets the mass for the body. Mass must be positive.
     *
     * @param mass The mass to use in kg; if null or non-positive, the default mass is retained
     * @return This builder instance for method chaining
     */
    public BodyBuilder mass(Double mass) {
        if (mass != null && mass > 0) {
            isDefault = false;
            this.mass = mass;
        }
        return this;
    }

    /**
     * Sets the density for the body. Density must be positive. Density is used to calculate the body's radius based on
     * its mass.
     *
     * @param density The density to use in kg/m³; if null or non-positive, the default density is retained
     * @return This builder instance for method chaining
     */
    public BodyBuilder density(Double density) {
        if (density != null && density > 0) {
            isDefault = false;
            this.density = density;
        }
        return this;
    }

    /**
     * Sets the name for the body.
     *
     * @param name The name to use; if null, a the default name is retained
     * @return This builder instance for method chaining
     */
    public BodyBuilder name(String name) {
        if (name != null) {
            isDefault = false;
            this.name = name;
        }
        return this;
    }

    /**
     * Sets the color for rendering the body.
     *
     * @param c The color to use; if null, the default color (white) is retained
     * @return This builder instance for method chaining
     */
    public BodyBuilder color(Color c) {
        if (c != null) {
            isDefault = false;
            this.c = c;
        }
        return this;
    }

    /**
     * Builds a new Body instance with the configured properties.
     * If no parameters were specified, null is returned.
     * This builder can only be used once; subsequent calls will return null.
     *
     * @return A new Body instance, or null if the builder was already used or no parameters were specified
     */
    public Body build() {
        if (isBuilt || isDefault) {
            return null;
        }
        isBuilt = true;
        return new Body(pos, vel, mass, density, name, c);
    }

}

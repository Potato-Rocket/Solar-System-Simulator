package us.stomberg.solarsystemsim.physics;

import java.awt.*;

/**
 * Factory class that provides default celestial bodies with realistic properties.
 */
public class BodyFactory {

    /**
     * Creates a default star with Sun-like properties.
     *
     * @return A new Body instance with Sun-like properties
     */
    public static Body createDefaultStar() {
        return new BodyBuilder()
                .position(new Vector3D(0, 0, 0))
                .velocity(new Vector3D(0, 0, 0))
                .mass(1000000.0)
                .density(1.4)
                .name("Sun")
                .color(new Color(255, 220, 0))
                .build();
    }

    /**
     * Creates a default inner planet with Mercury-like properties.
     *
     * @return A new Body instance with Mercury-like properties
     */
    public static Body createDefaultInnerPlanet() {
        return new BodyBuilder()
                .position(new Vector3D(800, 0, 50))
                .velocity(new Vector3D(0, 35, 0))
                .mass(1000.0)
                .density(5.4)
                .name("Inner Planet")
                .color(new Color(180, 180, 180))
                .build();
    }

    /**
     * Creates a default outer planet with Earth-like properties.
     *
     * @return A new Body instance with Earth-like properties
     */
    public static Body createDefaultOuterPlanet() {
        return new BodyBuilder()
                .position(new Vector3D(1500, 0, 0))
                .velocity(new Vector3D(0, 25, -5))
                .mass(5000.0)
                .density(5.5)
                .name("Outer Planet")
                .color(new Color(0, 100, 255))
                .build();
    }

}

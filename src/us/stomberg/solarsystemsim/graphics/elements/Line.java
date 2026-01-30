package us.stomberg.solarsystemsim.graphics.elements;

import us.stomberg.solarsystemsim.graphics.Draw;
import us.stomberg.solarsystemsim.graphics.Graphics3D;
import us.stomberg.solarsystemsim.physics.Vector3D;

import java.awt.*;

/**
 * Stores information about a line to draw.
 */
public class Line {

    /**
     * Stores the position of the line's first endpoint.
     */
    private final Vector3D converted1;

    /**
     * Stores the position of the line's second endpoint transformed to the 3D view.
     */
    private final Vector3D converted2;

    /**
     * Stores the color of the line to draw.
     */
    private final Color color;

    /**
     * Class constructor. Takes the position of both endpoints as an input as well as the color of the line, and uses
     * <code>Graphics3D</code> to transform them to the viewing angle.
     *
     * @param point1 first endpoint
     * @param point2 second endpoint
     * @param focus  center point of the view
     * @param c      color of the line
     */
    public Line(Vector3D point1, Vector3D point2, Vector3D focus, Color c) {
        converted1 = Graphics3D.convertPoint(point1, focus).scale(Draw.getScale());
        converted2 = Graphics3D.convertPoint(point2, focus).scale(Draw.getScale());
        color = c;
    }

    /**
     * Draws the line on the screen. Takes the current <code>Graphics2D</code> object as an input.
     *
     * @param g current <code>Graphics2D</code> object
     */
    public void draw(Graphics2D g) {
        g.setColor(color);
        g.drawLine((int) converted1.getY(), (int) converted1.getZ(), (int) converted2.getY(), (int) converted2.getZ());
    }

    /**
     * Gets the line's average depth relative to the view.
     *
     * @return Returns the point's average distance along the axis perpendicular to the viewing angle.
     */
    public double getDepth() {
        return (converted1.getX() + converted2.getX()) / 2;
    }

}

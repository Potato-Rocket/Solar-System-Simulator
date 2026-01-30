package us.stomberg.solarsystemsim.interaction;

import us.stomberg.solarsystemsim.Setup;
import us.stomberg.solarsystemsim.graphics.Graphics3D;

import java.awt.event.MouseEvent;
import java.awt.event.MouseMotionListener;

/**
 * Class to handle user interaction through mouse movement and dragging.
 */
public class MouseRotation implements MouseMotionListener {

    /**
     * Stores the coordinates of the mouse in the frame.
     */
    private static int[] mousePos = new int[2];

    /**
     * Setter method for the mouse position.
     *
     * @param pos mouse coordinates
     */
    public static void setMousePos(int[] pos) {
        mousePos = pos;
    }

    /**
     * Executed when the mouse is moved while depressed. Modifies the 3D yaw and tilt based on the sensitivity and
     * magnitude of the mouse movement.
     *
     * @param e mouse event
     */
    @Override
    public void mouseDragged(MouseEvent e) {
        if (mousePos[0] != 0 && mousePos[1] != 0) {
            double[] difference = {e.getX() - mousePos[0], mousePos[1] - e.getY()};
            Graphics3D.changeYaw(difference[0] * (Setup.getRotatePrecision() * Math.PI / 180));
            Graphics3D.changeTilt(difference[1] * (Setup.getRotatePrecision() * Math.PI / 180));
        }
        mousePos = new int[]{e.getX(), e.getY()};
    }

    /**
     * Executed when the mouse is moved. Does not contain any code.
     *
     * @param e mouse event
     */
    @Override
    public void mouseMoved(MouseEvent e) {
    }

}

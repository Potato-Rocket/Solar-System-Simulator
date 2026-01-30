package us.stomberg.solarsystemsim.interaction;

import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;

/**
 * Class to handle user interaction through clicking the mouse, relating to rotation movements.
 */
public class MouseRotationListener implements MouseListener {

    /**
     * Executes when the mouse is pressed, then released. Does not contain any code.
     *
     * @param e mouse event
     */
    @Override
    public void mouseClicked(MouseEvent e) {
    }

    /**
     * Executes when the mouse is pressed. Does not contain any code.
     *
     * @param e mouse event
     */
    @Override
    public void mousePressed(MouseEvent e) {
    }

    /**
     * Executes when the mouse is released. Currently resets the <code>MouseRotation</code> class' stored mouse position to
     * allow the view to be dragged and released indefinitely..
     *
     * @param e mouse event
     */
    @Override
    public void mouseReleased(MouseEvent e) {
        MouseRotation.setMousePos(new int[2]);
    }

    /**
     * Executes when the mouse enters the frame. Does not contain any code.
     *
     * @param e mouse event
     */
    @Override
    public void mouseEntered(MouseEvent e) {
    }

    /**
     * Executes when the mouse exits the frame. Does not contain any code.
     *
     * @param e mouse event
     */
    @Override
    public void mouseExited(MouseEvent e) {
    }

}

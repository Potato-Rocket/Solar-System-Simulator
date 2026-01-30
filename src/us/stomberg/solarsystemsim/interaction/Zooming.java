package us.stomberg.solarsystemsim.interaction;

import us.stomberg.solarsystemsim.graphics.Draw;

import java.awt.event.MouseWheelEvent;
import java.awt.event.MouseWheelListener;

/**
 * Class to handle user interaction through mouse wheel rotation.
 */
public class Zooming implements MouseWheelListener {

    /**
     * Executed when the mouse wheel is spun a notch in either direction. Updates the
     * <code>Draw</code> class' zoom scale.
     *
     * @param e mouse event
     */
    @Override
    public void mouseWheelMoved(MouseWheelEvent e) {
        Draw.modifyBounds(e.getWheelRotation());
    }

}

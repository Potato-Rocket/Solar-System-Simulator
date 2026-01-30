package us.stomberg.solarsystemsim.interaction;

import us.stomberg.solarsystemsim.graphics.Draw;
import us.stomberg.solarsystemsim.graphics.Graphics3D;
import us.stomberg.solarsystemsim.TimeManager;

import javax.swing.*;
import java.awt.event.ActionEvent;

import us.stomberg.solarsystemsim.Main;

// TODO: Resolve local final field warnings
/**
 * Class to create and manage keymaps and their corresponding actions.
 */
public class Keymaps {

    /**
     * Tilts the view up.
     */
    private final Action tiltUp = new AbstractAction() {
        @Override
        public void actionPerformed(ActionEvent e) {
            Graphics3D.changeTilt(Math.PI / -36);
        }
    };

    /**
     * Tilts the view down.
     */
    private final Action tiltDown = new AbstractAction() {
        @Override
        public void actionPerformed(ActionEvent e) {
            Graphics3D.changeTilt(Math.PI / 36);
        }
    };

    /**
     * Spins the view left.
     */
    private final Action yawLeft = new AbstractAction() {
        @Override
        public void actionPerformed(ActionEvent e) {
            Graphics3D.changeYaw(Math.PI / -36);
        }
    };

    /**
     * Spins the view right.
     */
    private final Action yawRight = new AbstractAction() {
        @Override
        public void actionPerformed(ActionEvent e) {
            Graphics3D.changeYaw(Math.PI / 36);
        }
    };

    /**
     * Speeds up the timescale.
     */
    private final Action speedUp = new AbstractAction() {
        @Override
        public void actionPerformed(ActionEvent e) {
            TimeManager.modifyTimescaleCap(TimeManager.TimeScaleChangeType.INCREMENT);
        }
    };

    /**
     * Slows down the timescale.
     */
    private final Action slowDown = new AbstractAction() {
        @Override
        public void actionPerformed(ActionEvent e) {
            TimeManager.modifyTimescaleCap(TimeManager.TimeScaleChangeType.DECREMENT);
        }
    };

    /**
     * Zooms in the view.
     */
    private final Action zoomIn = new AbstractAction() {
        @Override
        public void actionPerformed(ActionEvent e) {
            for (int i = 0; i < 10; i++) {
                Draw.modifyBounds(-1);
            }
        }
    };

    /**
     * Zooms out the view.
     */
    private final Action zoomOut = new AbstractAction() {
        @Override
        public void actionPerformed(ActionEvent e) {
            for (int i = 0; i < 10; i++) {
                Draw.modifyBounds(1);
            }
        }
    };

    /**
     * Toggles the focus to the previous body.
     */
    private final Action prevFocus = new AbstractAction() {
        @Override
        public void actionPerformed(ActionEvent e) {
            Draw.modifyFocus(-1);
        }
    };

    /**
     * Toggles the focus to the next body.
     */
    private final Action nextFocus = new AbstractAction() {
        @Override
        public void actionPerformed(ActionEvent e) {
            Draw.modifyFocus(1);
        }
    };

    /**
     * Resets the view scaling and rotation.
     */
    private final Action resetView = new AbstractAction() {
        @Override
        public void actionPerformed(ActionEvent e) {
            Draw.resetBounds();
            Graphics3D.resetView();
        }
    };

    /**
     * Toggles whether to draw planet size to scale or in a lgo scale for visibility.
     */
    private final Action toggleLog = new AbstractAction() {
        @Override
        public void actionPerformed(ActionEvent e) {
            Draw.toggleLogScale();
        }
    };

    /**
     * Toggles whether to draw planet size to scale or in a lgo scale for visibility.
     */
    private final Action toggleVerbose = new AbstractAction() {
        @Override
        public void actionPerformed(ActionEvent e) {
            Draw.toggleVerboseOut();
        }
    };

    /**
     * Exits the program when Escape key is pressed.
     */
    private final Action exitProgram = new AbstractAction() {
        @Override
        public void actionPerformed(ActionEvent e) {
            Main.stopRunning();
        }
    };

    /**
     * Class constructor. Creates an <code>inputMap</code> and an <code>actionMap</code> for each
     * <code>Action</code>.
     *
     * @param component <code>JComponent</code> in which to create the input and action maps
     */
    public Keymaps(JComponent component) {
        // Creates the inputMap and actionMap.
        InputMap inputMap = component.getInputMap(JComponent.WHEN_IN_FOCUSED_WINDOW);
        ActionMap actionMap = component.getActionMap();
        // Function key maps.
        inputMap.put(KeyStroke.getKeyStroke("released F1"), "RESET_VIEW");
        actionMap.put("RESET_VIEW", resetView);
        inputMap.put(KeyStroke.getKeyStroke("released F2"), "TOGGLE_LOG");
        actionMap.put("TOGGLE_LOG", toggleLog);
        inputMap.put(KeyStroke.getKeyStroke("released F3"), "TOGGLE_VERBOSE");
        actionMap.put("TOGGLE_VERBOSE", toggleVerbose);
        // Q&E keys to change the timescale.
        inputMap.put(KeyStroke.getKeyStroke("released Q"), "SLOW_DOWN");
        actionMap.put("SLOW_DOWN", slowDown);
        inputMap.put(KeyStroke.getKeyStroke("released E"), "SPEED_UP");
        actionMap.put("SPEED_UP", speedUp);
        // Vim keys to control zooming and toggling the focused body.
        inputMap.put(KeyStroke.getKeyStroke("J"), "ZOOM_IN");
        actionMap.put("ZOOM_IN", zoomIn);
        inputMap.put(KeyStroke.getKeyStroke("K"), "ZOOM_OUT");
        actionMap.put("ZOOM_OUT", zoomOut);
        inputMap.put(KeyStroke.getKeyStroke("released H"), "PREV_FOCUS");
        actionMap.put("PREV_FOCUS", prevFocus);
        inputMap.put(KeyStroke.getKeyStroke("released L"), "NEXT_FOCUS");
        actionMap.put("NEXT_FOCUS", nextFocus);
        // WASD keys to rotate the view.
        inputMap.put(KeyStroke.getKeyStroke("W"), "TILT_UP");
        actionMap.put("TILT_UP", tiltUp);
        inputMap.put(KeyStroke.getKeyStroke("S"), "TILT_DOWN");
        actionMap.put("TILT_DOWN", tiltDown);
        inputMap.put(KeyStroke.getKeyStroke("A"), "YAW_LEFT");
        actionMap.put("YAW_LEFT", yawLeft);
        inputMap.put(KeyStroke.getKeyStroke("D"), "YAW_RIGHT");
        actionMap.put("YAW_RIGHT", yawRight);
        // Exit program with Escape key
        inputMap.put(KeyStroke.getKeyStroke("ESCAPE"), "EXIT_PROGRAM");
        actionMap.put("EXIT_PROGRAM", exitProgram);
    }

}

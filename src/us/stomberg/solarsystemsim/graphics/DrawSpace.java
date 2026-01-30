package us.stomberg.solarsystemsim.graphics;

import us.stomberg.solarsystemsim.interaction.MouseRotationListener;
import us.stomberg.solarsystemsim.interaction.Keymaps;
import us.stomberg.solarsystemsim.interaction.MouseRotation;
import us.stomberg.solarsystemsim.interaction.Zooming;

import javax.swing.*;
import java.awt.*;

/**
 * Class to handle the <code>JFrame</code> and <code>JPanel</code>. Deals with the highest level of graphical
 * operations.
 */
public class DrawSpace extends JPanel {

    /**
     * Sets up a new <code>JFrame</code> and sets the required settings. Adds this JPanel and various listeners required
     * for user interaction.
     */
    public DrawSpace() {
        JFrame frame = new JFrame();
        frame.add(this);
        frame.setTitle("Solar System Simulator");
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        Dimension dim = Toolkit.getDefaultToolkit().getScreenSize();
        frame.setSize(dim.width / 2, dim.height / 2);
        frame.setLocation(dim.width / 4, dim.height / 4);
        frame.setExtendedState(Frame.MAXIMIZED_BOTH);
        frame.setVisible(true);
        addMouseListener(new MouseRotationListener());
        addMouseMotionListener(new MouseRotation());
        addMouseWheelListener(new Zooming());
        new Keymaps(this);
    }

    /**
     * Updates the graphics in the frame. Creates a new <code>Draw</code> object and runs the
     * <code>drawAll</code> method. Finally, it revalidates to ensure smoother animations.
     *
     * @param g current <code>Graphics</code> object
     */
    public void paint(Graphics g) {
        // Cast to Graphics2D to access advanced rendering hints
        Graphics2D g2d = (Graphics2D) g;

        // Enable antialiasing
        g2d.setRenderingHint(RenderingHints.KEY_ANTIALIASING,
                             RenderingHints.VALUE_ANTIALIAS_ON);

        g.setFont(new Font("Roboto Mono Thin", Font.PLAIN, 12));
        g.fillRect(0, 0, getWidth(), getHeight());
        Draw draw = new Draw(g, getWidth(), getHeight());
        draw.drawAll();
        revalidate();
    }

}

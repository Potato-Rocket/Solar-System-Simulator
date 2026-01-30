package us.stomberg.solarsystemsim.graphics;

import us.stomberg.solarsystemsim.Main;
import us.stomberg.solarsystemsim.Setup;
import us.stomberg.solarsystemsim.graphics.elements.Line;
import us.stomberg.solarsystemsim.graphics.elements.Point;
import us.stomberg.solarsystemsim.TimeManager;
import us.stomberg.solarsystemsim.physics.Body;
import us.stomberg.solarsystemsim.physics.Vector3D;

import java.awt.*;
import java.text.DecimalFormat;
import java.time.temporal.ChronoUnit;
import java.util.ArrayList;
import java.util.LinkedList;

// TODO: Make this class non-static, and better segmented
// TODO: Allow for graphical elements to persist and be tied to the objects which they represent
/**
 * Class to create all the drawings and graphics. Runs the 3D transformation code and organises the 3D elements before
 * drawing them.
 */
public class Draw {

    /**
     * Stores element objects to draw.
     */
    private static final ArrayList<Object> elements = new ArrayList<>();

    /**
     * Simulated units to fit on the screen
     */
    private static double minBounds;

    /**
     * Whether to display bodies at real scale or a log scale.
     */
    private static boolean logScale = false;

    /**
     * Whether to show extra info on screen.
     */
    private static boolean verbose = false;

    /**
     * Stores the scale factor between the virtual units and the pixels on screen.
     */
    private static double scale = 0;

    /**
     * Index + 1 of the body the view is currently centered on.
     */
    private static Body focus;

    /**
     * Stores the <code>Graphics2D</code> object used to create all graphics.
     */
    private final Graphics2D g2d;

    /**
     * Point in 3D space the view is centered on.
     */
    private final Vector3D centerPoint;

    /**
     * Smallest and largest body sizes after being put on a log scale.
     */
    private final double[] logMinMax;

    /**
     * Stores the frame width.
     */
    private int w;

    /**
     * Stores the frame height.
     */
    private int h;

    /**
     * Class constructor. Takes the <code>Graphics</code> object provided by the <code>paint</code> method in the
     * <code>drawSpace</code> class as well as the frame's width and height.
     *
     * @param graphics current <code>Graphics</code> object
     * @param width    width of the frame
     * @param height   height of the frame
     */
    public Draw(Graphics graphics, int width, int height) {
        g2d = (Graphics2D) graphics;
        w = width;
        h = height;
        synchronized (Main.getPhysics().lock) {
            checkFocus();
            centerPoint = focus.getState().getPosition();
            if (minBounds == 0) {
                minBounds = Main.getPhysics().getInitBounds() * Setup.getScalePrecision();
                if (minBounds == 0) {
                    minBounds = 1;
                }
            }
            logMinMax = new double[]{100, 0};
            for (Body body : Main.getPhysics().getBodyArray()) {
                double log = Math.log(body.getRadius());
                if (log < logMinMax[0]) {
                    logMinMax[0] = log;
                }
                if (log > logMinMax[1]) {
                    logMinMax[1] = log;
                }
            }
        }
    }

    /**
     * Modifies the scale of the view by the scale factor.
     *
     * @param direction Which direction the zooming is going in.
     */
    public static void modifyBounds(int direction) {
        if (direction == 1) {
            minBounds *= Setup.getScalePrecision();
        } else {
            minBounds /= Setup.getScalePrecision();
        }
    }

    /**
     * Resets the view scale to the initial view scale.
     */
    public static void resetBounds() {
        minBounds = Main.getPhysics().getInitBounds();
    }

    /**
     * Getter method for the current drawing scale factor.
     *
     * @return Returns the drawing scale.
     */
    public static double getScale() {
        return scale;
    }

    /**
     * Toggles through the array of bodies. Alters the <code>focus</code> field, which stores the index of the focused
     * body in the bodyArray.
     *
     * @param change whether to increase or decrease the index
     */
    public static void modifyFocus(int change) {
        synchronized (Main.getPhysics().lock) {
            if (checkFocus()) {
                int index = Main.getPhysics().getBodyArray().indexOf(focus);
                index += change;
                if (index < 0) {
                    index = Main.getPhysics().getBodyArray().size() - 1;
                } else if (index >= Main.getPhysics().getBodyArray().size()) {
                    index = 0;
                }
                focus = Main.getPhysics().getBodyArray().get(index);
            }
        }
    }

    private static boolean checkFocus() {
        synchronized (Main.getPhysics().lock) {
            if (focus == null || !Main.getPhysics().getBodyArray().contains(focus)) {
                focus = Main.getPhysics().getBodyArray().getFirst();
                return false;
            }
            return true;
        }
    }

    public static void toggleLogScale() {
        logScale = !logScale;
    }

    public static void toggleVerboseOut() {
        verbose = !verbose;
    }

    /**
     * Draws everything. Operates in the following order:
     * <ol>
     *   <li>Scales and transforms the <code>Graphics</code> object's coordinate system as required
     *   by the defined scale of the view and the 3D origin point.</li>
     *   <li>3D axes to aid with orientation.</li>
     *   <li>Draws the physical bodies and their trails. Uses the <code>Graphics3D</code> class
     *   to get the <code>Line</code> and <code>Point</code> objects describing the elements to
     *   be rendered. These objects are added to an array which sorts the elements by their
     *   apparent depth before finally rendering them on the screen.</li>
     *   <li>Adds a 2D crosshair at the center of the screen, as well as tick marks to indicate
     *   scale.</li>
     * </ol>
     */
    public void drawAll() {
        //Transforms the coordinate grid
        g2d.translate(w / 2, h / 2);
        int min = h;
        if (h > w) {
            min = w;
        }
        w /= 2;
        h /= 2;
        scale = min / (minBounds * 2);
        elements.clear();
        drawAxes();
        synchronized (Main.getPhysics().lock) {
            for (Body body : Main.getPhysics().getBodyArray()) {
                drawBody(body);
            }
        }
        render();
        drawGuides();
        g2d.setColor(new Color(0, 0, 0, 127));
        if (verbose) {
            g2d.fillRect(-w, -h, 450, 18 * 16 + 9);
        } else {
            g2d.fillRect(-w, -h, 300, 18 * 6 + 9);
        }
        g2d.setColor(Color.WHITE);
        ArrayList<String> bodyInfo = new ArrayList<>();

        DecimalFormat fps = new DecimalFormat("0.00 fps");
        DecimalFormat fpsc = new DecimalFormat("0.## fps");
        bodyInfo.add("FPS: " + fps.format(TimeManager.getCurrentFPS()) + " (" + fpsc.format(Setup.getFrameLimit()) + ")");
        bodyInfo.add("Duration: " + FormatText.formatDuration((long) TimeManager.getDuration(), ChronoUnit.SECONDS));
        bodyInfo.add("Time step: " + FormatText.formatScale(Setup.getTimeStep(), true) + " s");
        bodyInfo.add("Time scale: " + FormatText.formatScale(TimeManager.getCurrentTimeScale(), false) +
                             "x (" + FormatText.formatScale(TimeManager.getTimescaleCap(), true) + "x)");
        double initialKE = Main.getPhysics().getInitialKineticEnergy();
        double currentKE = Main.getPhysics().getKineticEnergy();
        bodyInfo.add("");
        bodyInfo.add("System Kinetic Energy:");
        bodyInfo.add("Initial = " + FormatText.formatValue(initialKE, "J", "kJ"));
        bodyInfo.add("Current = " + FormatText.formatValue(currentKE, "J", "kJ"));
        DecimalFormat percent = new DecimalFormat("0.00%");
        bodyInfo.add("Δ = " + FormatText.formatValue(Math.abs(initialKE - currentKE), "J", "kJ")
        + " (" + percent.format(Math.abs(initialKE - currentKE) / initialKE) + ")");
        bodyInfo.add("");
        if (logScale) {
            bodyInfo.add("Planet scale: Log");
        } else {
            bodyInfo.add("Planet scale: Realistic");
        }
        bodyInfo.add("Body count: " + Main.getPhysics().getBodyArray().size());
        if (verbose) {
            bodyInfo.add("");
            bodyInfo.add("Viewing angle:");
            bodyInfo.add("Rotation = " + (int) Math.round(Math.toDegrees(Graphics3D.getYaw())) + "°");
            bodyInfo.add("Tilt = " + (int) Math.round(Math.toDegrees(-Graphics3D.getTilt())) + "°");
            bodyInfo.add("");
            bodyInfo.addAll(focus.toStringArray());
        } else {
            synchronized (Main.getPhysics().lock) {
                bodyInfo.add("Focused body: " + focus.getName() + " (" + (focus.getId() + 1) + ")");
            }
        }
        FormatText.drawText(g2d, bodyInfo, -w + 10, -h + 10, 1.5);
    }

    /**
     * Draws the 3D axes. The X-axis is red, the Y-axis is green, and the Z-axis is blue. The axes' positive sections
     * appear brighter than their negative sections. Translates it to the current focus point.
     */
    private void drawAxes() {
        Vector3D v1 = new Vector3D(minBounds / 2, 0, 0);
        Vector3D v2 = new Vector3D(minBounds / -2, 0, 0);
        Vector3D v3 = new Vector3D(0, minBounds / 2, 0);
        Vector3D v4 = new Vector3D(0, minBounds / -2, 0);
        Vector3D v5 = new Vector3D(0, 0, minBounds / 2);
        Vector3D v6 = new Vector3D(0, 0, minBounds / -2);

        elements.add(new Line(v1.add(centerPoint), centerPoint, centerPoint,
                              new Color(255, 0, 0)));
        elements.add(new Line(v2.add(centerPoint), centerPoint, centerPoint,
                              new Color(63, 0, 0)));
        elements.add(new Line(v3.add(centerPoint), centerPoint, centerPoint,
                              new Color(0, 255, 0)));
        elements.add(new Line(v4.add(centerPoint), centerPoint, centerPoint,
                              new Color(0, 63, 0)));
        elements.add(new Line(v5.add(centerPoint), centerPoint, centerPoint,
                              new Color(0, 0, 255)));
        elements.add(new Line(v6.add(centerPoint), centerPoint, centerPoint,
                              new Color(0, 0, 127)));
    }

    /**
     * Draws tick marks to represent the scale. Maintains the distance between the tick marks at a factor of 10 and
     * makes every 10th tick mark longer. Prints the virtual distance between each tick mark in the bottom left corner.
     */
    private void drawGuides() {
        g2d.setColor(Color.WHITE);
        long tickExp = 1;
        long tickDist = 10;
        while (h / (tickDist * scale) > 2) {
            tickExp++;
            tickDist = (long) Math.pow(10, tickExp);
        }
        tickExp--;
        tickDist = (long) (Math.pow(10, tickExp) * scale);
        for (int x = (int) ((int) (-w / tickDist) * tickDist); x < w; x += (int) tickDist) {
            int len = 10;
            if (Math.round(x / (double) tickDist) % 10 == 0) {
                len = 20;
            }
            g2d.drawLine(x, h, x, h - len);
        }
        for (int y = (int) ((int) (-h / tickDist) * tickDist); y < h; y += (int) tickDist) {
            int len = 10;
            if (Math.round(y / (double) tickDist) % 10 == 0) {
                len = 20;
            }
            g2d.drawLine(w, y, w - len, y);
        }
        g2d.setColor(new Color(0, 0, 0, 127));
        g2d.fillRect(-w, h - 50, 200, 30);
        g2d.setColor(Color.WHITE);
        FormatText.drawText(g2d, "One tick = " + FormatText.formatValue(Math.pow(10, tickExp), "m", "km"), 10 - w,
                            h - 40);
    }

    /**
     * Sorts the elements array by depth before running the <code>draw</code> function on each element.
     */
    private void render() {
        Object[] elementArray = elements.toArray();
        quickSort(elementArray, 0, elementArray.length - 1);
        for (Object e : elementArray) {
            if (e instanceof Point) {
                ((Point) e).draw(g2d);
            } else if (e instanceof Line) {
                ((Line) e).draw(g2d);
            }
        }
    }

    /**
     * Adds a <code>Point</code> object to the elements array. Uses the given body's position and size to generate the
     * new object using <code>Graphics3D</code>.
     *
     * @param body body to draw
     */
    private void drawBody(Body body) {
        if (Setup.isDrawingTrail()) {
            drawTrail(body);
        }
        double size = body.getRadius();
        if (logScale) {
            size = Math.log(size);
            size -= logMinMax[0];
            size++;
            size *= (minBounds * scale) / 100;
        } else {
            size *= scale;
            if (size < 2) {
                size = 2;
            }
        }
        elements.add(new Point(body.getState().getPosition(), centerPoint, body.getColor(), (int) size));
    }

    /**
     * Adds <code>Line</code> objects to the elements array. Uses the given body's trail data to generate a line for
     * each trail segment using <code>Graphics3D</code>.
     *
     * @param body body to draw the trail for
     */
    private void drawTrail(Body body) {
        LinkedList<Vector3D> trail = body.getTrail();
        if (!trail.isEmpty()) {
            elements.add(new Line(body.getState().getPosition(), trail.getFirst(), centerPoint, new Color(255, 255, 0)));
        }
        for (int i = 0; i < trail.size() - 1; i++) {
            Color c;
            if (Setup.trailHasAlpha()) {
                double fade = 1.0 - (1.0 / trail.size() * i);
                c = new Color(255, 255, 0, (int) (255 * fade));
            } else {
                c = new Color(255, 255, 0);
            }
            elements.add(new Line(trail.get(i), trail.get(i + 1), centerPoint, c));
        }
    }

    /**
     * Runs the quicksort sorting algorithm on an array of elements. This sorts by depth and is a recursive algorithm.
     * <p>
     * Information on the quicksort algorithm can be found here on Wikipedia:
     * <a href="https://en.wikipedia.org/wiki/Quicksort">Quicksort</a>
     *
     * @param arr   array to sort
     * @param begin beginning index of the range to compare
     * @param end   ending index of the range to compare
     */
    private void quickSort(Object[] arr, int begin, int end) {
        if (begin < end) {
            double pivot = getDepth(arr[end]);
            int i = (begin - 1);
            for (int j = begin; j < end; j++) {
                if (getDepth(arr[j]) <= pivot) {
                    i++;
                    Object swapTemp = arr[i];
                    arr[i] = arr[j];
                    arr[j] = swapTemp;
                }
            }
            Object swapTemp = arr[i + 1];
            arr[i + 1] = arr[end];
            arr[end] = swapTemp;
            int partitionIndex = i + 1;
            quickSort(arr, begin, partitionIndex - 1);
            quickSort(arr, partitionIndex + 1, end);
        }
    }

    /**
     * Gets the relative depth of any 3D element. Functions by determining  the element type, then calling its
     * <code>getDepth</code> method.
     * <p>
     * Functions for:
     * <ul>
     *   <li>Any <code>Point</code> object.</li>
     *   <li>Any <code>Line</code> object.</li>
     * </ul>
     *
     * @param e object to get the depth of
     * @return Returns the relative depth of the object.
     */
    private double getDepth(Object e) {
        if (e instanceof Point) {
            return ((Point) e).getDepth();
        } else if (e instanceof Line) {
            return ((Line) e).getDepth();
        }
        return 0;
    }

}

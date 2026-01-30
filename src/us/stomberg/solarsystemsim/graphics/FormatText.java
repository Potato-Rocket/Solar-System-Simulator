package us.stomberg.solarsystemsim.graphics;

import java.awt.*;
import java.text.DecimalFormat;
import java.time.Duration;
import java.time.temporal.TemporalUnit;
import java.util.ArrayList;

/**
 * Class to store methods dealing with text and strings to be displayed on screen.
 */
public class FormatText {

    /**
     * Prints an array of lines of text onto the screen. Each <code>String</code> in the array is treated as a line, and
     * the positioning is determined by the line spacing and base <b>x</b> and <b>y</b> positions.
     *
     * @param g           current <code>Graphics2D</code> object
     * @param text        array of lines of text to print
     * @param x           <b>x</b> position of upper-left corner
     * @param y           <b>y</b> position of upper-left corner
     * @param lineSpacing distance between lines measured in character heights
     */
    public static void drawText(Graphics2D g, ArrayList<String> text, int x, int y, double lineSpacing) {
        for (int l = 0; l < text.size(); l++) {
            g.drawString(text.get(l), x, y + 9 + (int) (lineSpacing * 12 * l));
        }
    }

    public static void drawText(Graphics2D g, String text, int x, int y) {
        g.drawString(text, x, y + 9);
    }

    /**
     * Formats a time duration into a readable string. Formats milliseconds into years, days, hours, minutes, seconds,
     * and the remainder milliseconds.
     *
     * @return Returns the formatted string.
     */
    public static String formatDuration(long amount, TemporalUnit unit) {
        Duration duration = Duration.of(amount, unit);
        long years = duration.toDays() / 365;
        long days = duration.toDays() % 365;
        long hours = duration.toHours() % 24;
        long minutes = duration.toMinutes() % 60;
        long seconds = duration.getSeconds() % 60;
        return String.format("%dy:%03dd:%02dh:%02dm:%02ds", years, days, hours, minutes, seconds);
    }

    /**
     * Formats a large number to be more readable. Groups the zeroes if possible, and appends the correct unit. If value
     * is over 10000, will use the kilo unit and divide the value by 1000. (eg. 10000000 -> 10,000km)
     *
     * @param num      number to format
     * @param unit     base unit
     * @param kiloUnit unit 1000x base unit
     * @return Returns the formatted string.
     */
    public static String formatValue(double num, String unit, String kiloUnit) {
        String suffix = unit;
        if (Math.abs(num) >= 10000) {
            num /= 1000;
            suffix = kiloUnit;
        }
        DecimalFormat df;
        if (Math.abs(num) >= 100000) {
            df = new DecimalFormat("0.000E0 " + suffix);
        } else {
            df = new DecimalFormat("#,##0.00 " + suffix);
        }
        return df.format(num);
    }

    /**
     * Formats a large number to be more readable. Groups the zeroes if possible, and appends the correct unit. If value
     * is over 10000, will use the kilo unit and divide the value by 1000. (eg. 10000000 -> 10,000km)
     *
     * @param num      number to format
     * @return Returns the formatted string.
     */
    public static String formatScale(Double num, boolean round) {
        StringBuilder format = new StringBuilder("#,##0");

        int decade = (int) Math.floor(Math.log10(num));
        int digits = -decade + 2;
        if (decade >= 0) {
            digits += 1;
        }
        if (digits > 0) {
            format.append(".");
            if (round) {
                format.append("#".repeat(digits));
            } else {
                format.append("0".repeat(digits));
            }
        }

        DecimalFormat df = new DecimalFormat(format.toString());
        return df.format(num);
    }

}

import java.awt.*;

/**
 * The ColorDrift algorithm is the second algorithm for generating a color palette.
 *
 * The algorithm begins with all 5 colors on Gray values, and only lets them move
 * in small steps at a time. This will create a smooth transition over time for
 * a better experience during live-play.
 *
 * @author Aaron Weiss
 * @version 1.0
 * @since 2018-11-21
 */

public class ColorDrift implements PaletteAlgorithm {

    private Color[] palette;
    private int currentColor;
    private int lastVelocity;

    public ColorDrift() {
        palette = new Color[5];
        for (int i = 0; i < 5; i++) {
            palette[i] = new Color(128, 128, 128);
        }

        currentColor = 0;
        lastVelocity = 64;
    }

    /**
     * Implements the add method from the PaletteAlgorithm interface.
     * With each new note, update the current color we are on and moved to the next.
     * @param note The note played, from 0 to 11.
     * @param velocity The velocity of note played, from 0 to 127.
     * @param octave The octave of note played, from 0 to 8(?).
     */

    public void add(int note, int velocity, int octave) {

        boolean increase = velocity > lastVelocity;

        int step = 1;

        if (!increase)
            step = -step;

        int red = palette[currentColor].getRed();
        int green = palette[currentColor].getGreen();
        int blue = palette[currentColor].getBlue();

        if (note % 3 == 0)
            red += step;
        else if (note % 3 == 1)
            green += step;
        else if (note % 3 == 2)
            blue += step;

        red = constrainToRange(red, 0, 255);
        green = constrainToRange(green, 0, 255);
        blue = constrainToRange(blue, 0, 255);

        palette[currentColor] = new Color(red, green, blue);

        currentColor = ++currentColor % palette.length;
        lastVelocity = velocity;
    }

    /**
     * Implements PaletteAlgorithm method. Returns colors.
     * @return palette Array of size 5 of Color objects.
     */

    public Color[] getColors() {
        return palette;
    }

    /**
     * A helper method to keep RGB values in their proper range.
     * @param val The tested value.
     * @param min The minimum value.
     * @param max The maximum value.
     * @return
     */

    private int constrainToRange(int val, int min, int max) {
        return Math.max(Math.min(val, max), min);
    }

}

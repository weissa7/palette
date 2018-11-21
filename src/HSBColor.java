import java.awt.*;

/**
 * The HSBColor class holds color information using the Hue-Saturation-Brightness
 * color space. This color system is ideal for the NoteDistance algorithm.
 */

public class HSBColor {

    // All values should be floats from 0.0f - 1.0f
    private float hue;
    private float saturation;
    private float brightness;

    public HSBColor() {
        hue = 0.0f;
        saturation = 0.0f;
        brightness = 1.0f;
    }

    /**
     * Convert this color system to the standard RGB Color.
     * @return Color object of the equivalent color.
     */

    public Color getColor() {
        return Color.getHSBColor(hue, saturation, brightness);
    }

    /**
     * Set the hue of the color.
     * @param hue The hue.
     */

    public void setHue(float hue) {
        this.hue = hue;
    }

    /**
     * Set the saturation of the color
     * @param saturation The saturation.
     */

    public void setSaturation(float saturation) {
        this.saturation = saturation;
    }

    /**
     * Set the brightness of the color
     * @param brightness The brightness.
     */

    public void setBrightness(float brightness) {
        this.brightness = brightness;
    }

    /**
     * Print information about the color, for debugging purposes
     * @return Color information in string format.
     */

    public String getString() {
        return "H: " + hue*360 + " S: " + saturation*100 + " B: " + brightness*100;
    }
}

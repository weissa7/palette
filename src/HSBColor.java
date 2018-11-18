import java.awt.*;

public class HSBColor {

    //All values should be floats from 0.0 - 1.0
    private float hue;
    private float saturation;
    private float brightness;

    public HSBColor() {
        hue = 0.0f;
        saturation = 0.0f;
        brightness = 1.0f;
    }

    public Color getColor() {
        return Color.getHSBColor(hue, saturation, brightness);
    }

    public void setHue(float hue) {
        this.hue = hue;
    }

    public void setSaturation(float saturation) {
        this.saturation = saturation;
    }

    public void setBrightness(float brightness) {
        this.brightness = brightness;
    }

    public String getString() {
        return "H: " + hue*360 + " S: " + saturation*100 + " B: " + brightness*100;
    }
}

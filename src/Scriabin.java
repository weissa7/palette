import java.awt.*;

/**
 * The Scribian algorithm uses the Scribian color wheel to assign colors.
 *
 * The algorithm moves the previously played notes to the right in the palette and adds a new note.
 * Basically, this algorithm shows the last 5 notes played.
 *
 * This reflects the synesthesia Alexander Scribian experienced.
 *
 */

public class Scriabin implements PaletteAlgorithm {

    private Color[] palette;
    private Color[] wheel;

    public Scriabin() {
        palette = new Color[5];
        for (int i = 0; i < 5; i++) {
            palette[i] = new Color(0, 0, 0);
        }

        wheel = new Color[12];

        // [ 0  1   2  3   4  5  6   7  8   9  10  11 ]
        // [ C, C#, D, D#, E, F, F#, G, G#, A, A#,  B ]

        // assign all Scribian colors to the wheel
        wheel[0] = new Color(255, 0, 0);
        wheel[1] = new Color(144, 0, 255);
        wheel[2] = new Color(255, 255, 0);
        wheel[3] = new Color(183, 70, 139);
        wheel[4] = new Color(195, 242, 255);
        wheel[5] = new Color(171, 0, 52);
        wheel[6] = new Color(127, 139, 253);
        wheel[7] = new Color(255, 127, 0);
        wheel[8] = new Color(187, 117, 252);
        wheel[9] = new Color(51, 204, 51);
        wheel[10] = new Color(169, 103, 124);
        wheel[11] = new Color(142, 201, 255);

    }

    public void add(int note, int velocity, int octave) {

        // assign Scribian colors
        Color newColor = wheel[note];

        // shift all colors to the right
        palette[4] = palette[3];
        palette[3] = palette[2];
        palette[2] = palette[1];
        palette[1] = palette[0];
        palette[0] = newColor;

    }

    public Color[] getColors() {
        return palette;
    }
}

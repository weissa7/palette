import java.awt.*;

/**
 * PaletteAlgorithm
 * The PaletteAlgorithm interface provides a series of methods any color-generating
 * algorithm should implement.
 */

interface PaletteAlgorithm {

    /**
     * The add method is used to add new note information to the data structures
     * used by the PaletteAlgorithm.
     * @param note The note played in integer format, from 0 to 11. The notes begin
     *             with C - (C, C#, D, D#, E, F, F#, G, G#, A, A#, B).
     * @param velocity The velocity of the note played. This number marks the intensity
     *                 and ranges from 0 to 127.
     * @param octave The octave of the note played in integer format, from 0 to 8(?).
     */
    void add(int note, int velocity, int octave);

    /**
     * The getColors method is used to retrieve the color palette the algorithm creates.
     * @return Color[5] an array of 5 colors of the generated color palette.
     */
    Color[] getColors();
}

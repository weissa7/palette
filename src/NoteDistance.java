import java.awt.*;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Random;

/**
 * The NoteDistance class is the first implemented algorithm for generating a color
 * palette from MIDI input.
 *
 * The algorithm uses a random seed based on the most played note. From this 'base
 * note', the algorithm determines the 'color distance' of the next-most popular
 * note. It then repeats for up to 4 notes, generating 5 colors.
 *
 * The algorithm reliably creates a color palette for any MIDI sequence, however,
 * does not work well in real-time, due to the base note changing frequently.
 *
 * @author Aaron Weiss, Alex Cretella
 * @version 1.0
 * @since 2018-11-18
 */

public class NoteDistance implements PaletteAlgorithm {

    // Store a history of notes played
    // [ 0  1   2  3   4  5  6   7  8   9  10  11 ]
    // [ C, C#, D, D#, E, F, F#, G, G#, A, A#,  B ]
    private int[] noteCounts;
    private ArrayList<Integer>[] velocityHistory;
    private HSBColor[] palette;
    private static final int MAX_VELOCITY = 127;

    /**
     * Constructor for the NoteDistance class. Prepare storage for notes played,
     * velocity history, and initialize color palette.
     */

    public NoteDistance() {
        noteCounts = new int[12];
        velocityHistory = (ArrayList<Integer>[]) new ArrayList[12];
        for (int i = 0; i < 12; i++) {
            velocityHistory[i] = new ArrayList<>();
        }
        palette = new HSBColor[5];
        for (int i = 0; i < 5; i++) {
            palette[i] = new HSBColor();
        }
    }

    /**
     * Implements PaletteAlgorithm add method. This algorithm does not make use of
     * the octave parameter. The color palette is updated on note add.
     * @param note The note played in integer format, from 0 to 11. The notes begin
     *             with C - (C, C#, D, D#, E, F, F#, G, G#, A, A#, B).
     * @param velocity The velocity of the note played. This number marks the intensity
     *                 and ranges from 0 to 127.
     * @param octave The octave of the note played in integer format, from 0 to 8(?).
     */

    public void add(int note, int velocity, int octave) {
        noteCounts[note]++;
        velocityHistory[note].add(velocity);
        updatePalette();
    }

    /**
     * Implements PaletteAlgorithm getColor method. Uses the HSBColor getColor
     * method to retrieve Color objects of each color.
     * @return colors An array of 5 Color objects.
     */

    public Color[] getColors() {
        Color[] colors = new Color[5];
        for(int i = 0; i < colors.length; i++) {
            colors[i] = palette[i].getColor();
        }
        return colors;
    }

    /**
     * The updatePalette method is used to calculate the color palette, using the
     * current count of notes, and velocity history.
     */

    private void updatePalette() {

        // sort notes to prepare for algorithm
        int[] sortedCounts = noteCounts.clone();

        // keep track of what notes are what after sorting
        int[] noteTracker = new int[12];
        for(int i = 0; i < noteTracker.length; i++)
            noteTracker[i] = i;

        // put the average velocities into the same order as our sorted notes
        float[] sortedVel = new float[12];
        for(int i = 0; i < sortedVel.length; i++)
            sortedVel[i] = (float)getAverageVelocity(i);

        // perform sorting
        int n = sortedCounts.length;
        for (int i = 0; i < n-1; i++) {
            for (int j = 0; j < n-i-1; j++) {
                if (sortedCounts[j] < sortedCounts[j+1]) {
                    // swap temp and arr[i]
                    int tempCount = sortedCounts[j];
                    sortedCounts[j] = sortedCounts[j + 1];
                    sortedCounts[j + 1] = tempCount;

                    int tempTracker = noteTracker[j];
                    noteTracker[j] = noteTracker[j + 1];
                    noteTracker[j + 1] = tempTracker;

                    float tempVel = sortedVel[j];
                    sortedVel[j] = sortedVel[j + 1];
                    sortedVel[j + 1] = tempVel;
                }
            }
        }

        // count the number of unique notes played
        int uniqueNotes = 0;
        for (int i = 0; i < sortedCounts.length; i++) {
            if (sortedCounts[i] > 0)
                uniqueNotes++;
        }

        // get base color based on most played note
        Random rand = new Random(sortedCounts[0]);
        // returns a number between 0.0f and 1.0f, for our base hue value
        float baseHue = rand.nextFloat();

        palette[0].setHue(baseHue);

        float[] normalizedVelocity = normalizeVector(sortedVel);

        palette[0].setSaturation(normalizedVelocity[0] * ((sortedVel[0] * 100) / MAX_VELOCITY));
        palette[0].setBrightness(normalizedVelocity[0] * ((sortedVel[0] * 100) / MAX_VELOCITY));

        // perform algorithm 0-4 times, depending on number of unique notes
        for (int i = 1; i < 5 && i < uniqueNotes; i++) {
            int distance = getNoteDistance(noteTracker[0], noteTracker[i]);

            float normalizedOffset = (float)sortedCounts[i] / (float)sortedCounts[0];

            float newHueDistance = ((float)1 / (float)noteCounts.length) * (float)distance * normalizedOffset;

            float newHue = (baseHue + newHueDistance) % 1.0f;

            palette[i].setHue(newHue);
            palette[i].setSaturation(normalizedVelocity[i] * ((sortedVel[i] * 100) / MAX_VELOCITY) );

            palette[i].setBrightness(normalizedVelocity[i] * ((sortedVel[i] * 100) / MAX_VELOCITY) );
        }
    }

    /**
     * The normalizeVector method is used to normalize values of an array according
     * to the highest value stored in that array.
     * @return array The normalized array.
     */

    private float[] normalizeVector(float array[]) {
        // clone our array
        array = array.clone();

        float maxValue = 0.0f;
        for (int i = 0; i < array.length; i++) {
            if (array[i] > maxValue)
                maxValue = array[i];
        }

        for (int i = 0; i < array.length; i++) {
            array[i] /= maxValue;
        }

        return array;
    }

    /**
     * Retreive the note distance between two notes.
     * @param base The base note (0 to 11)
     * @param offset The offset note (0 to 11)
     * @return The distance between the two notes
     */

    private int getNoteDistance(int base, int offset) {
        return offset - base;
    }

    /**
     * Return the average velocity of a note.
     * @param note The index of the desired note (0 to 11)
     * @return The average velocity.
     */

    private double getAverageVelocity(int note) {
        return velocityHistory[note].stream().mapToInt(val -> val).average().orElse(0.0);
    }

    /**
     * The printMe method is used to print all values for debugging purposes.
     */

    public void printMe() {
        System.out.println(Arrays.toString(noteCounts));
        System.out.print("[");
        for (int i = 0; i < velocityHistory.length; i++) {
            Double avg = getAverageVelocity(i);
            if (i < velocityHistory.length - 1)
                System.out.print(avg +", ");
            else
                System.out.print(avg);
        }
        System.out.println("]");
        for(int i = 0; i < 5; i++)
            System.out.print("{" + palette[i].getString() + "} ");
    }


}

import java.awt.*;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Random;

public class MidiToPalette {

    //Store a history of notes played
    //[0  1   2  3  4   5  6   7  8   9  10  11]
    //[C, C#, D, D#, E, F, F#, G, G#, A, A#, B,]
    private int[] noteCounts;
    private ArrayList<Integer>[] velocityHistory;
    private HSBColor[] palette;
    private static final int MAX_VELOCITY = 127;

    public MidiToPalette() {
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
            System.out.println("=================================");
            int distance = getNoteDistance(noteTracker[0], noteTracker[i]);
            System.out.println("Base: " + noteTracker[0] + " offset: " + noteTracker[i]);
            System.out.println("Dist: " + distance);

            float normalizedOffset = (float)sortedCounts[i] / (float)sortedCounts[0];
            System.out.println("SC0: " + sortedCounts[0] + " SCi: " + sortedCounts[i]);

            System.out.println("NO: " + normalizedOffset);

            float newHueDistance = ((float)1 / (float)noteCounts.length) * (float)distance * normalizedOffset;

            System.out.println("BaseHue: " + baseHue);
            System.out.println("NHD: " + newHueDistance);

            float newHue = (baseHue + newHueDistance) % 1.0f;

            System.out.println("NewHue: " + newHue);

            palette[i].setHue(newHue);
            palette[i].setSaturation(normalizedVelocity[i] * ((sortedVel[i] * 100) / MAX_VELOCITY) );

            System.out.println("SortedVel " + sortedVel[i]);
            palette[i].setBrightness(normalizedVelocity[i] * ((sortedVel[i] * 100) / MAX_VELOCITY) );
        }
    }

    private float[] normalizeVector(float array[]) {
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

    private int getNoteDistance(int base, int offset) {
        return offset - base;
    }

    public void add(int note, int velocity) {
        noteCounts[note]++;
        velocityHistory[note].add(velocity);
        updatePalette();
    }

    private double getAverageVelocity(int i) {
        return velocityHistory[i].stream().mapToInt(val -> val).average().orElse(0.0);
    }

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

    public Color[] getColors() {
        Color[] colors = new Color[5];
        for(int i = 0; i < colors.length; i++) {
            colors[i] = palette[i].getColor();
        }
        return colors;
    }
}

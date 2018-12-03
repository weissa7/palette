import javax.sound.midi.*;
import javax.swing.*;
import java.awt.*;
import java.awt.datatransfer.Clipboard;
import java.awt.datatransfer.StringSelection;
import java.awt.event.*;
import java.io.*;

import java.util.*; //this includes Scanner

import javax.sound.midi.MidiDevice.Info;

public class PaletteForm {

    private JPanel main;
    private JButton recordButton;
    private JButton stopButton;
    private JLabel colorPaletteLabel;
    private JButton uploadButton;
    private JPanel color0;
    private JPanel color1;
    private JPanel color2;
    private JPanel color3;
    private JPanel color4;
    private JLabel color0Label;
    private JLabel color1Label;
    private JLabel color2Label;
    private JLabel color3Label;
    private JLabel color4Label;
    private JLabel chordLabel;
    private JComboBox algComboBox;

    private static String inputName  = "MIDIIN3";
    private static String outputName = "Gervill";
    private MidiDevice input;
    private MidiDevice output;
    private String midiFile = "MyTestMidiFile.mid";

    private Sequencer sequencer;
    private Transmitter transmitter;
    private Receiver receiver;
    private MyMidiDevice myDevice;
    private ArrayList<String> chord;
    private ArrayList<String> chord2;

    private PaletteAlgorithm algorithm[];
    private int selectedAlgorithm = 0;
    private static int ALG_COUNT = 2;


    private void initializeAlgorithms() {
        algorithm = new PaletteAlgorithm[ALG_COUNT];
        algorithm[0] = new NoteDistance();
        algorithm[1] = new ColorDrift();
    }

    private PaletteForm() {

        initializeAlgorithms();

        chord = new ArrayList<>();
        chord2 = new ArrayList<>();

        recordButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                try {
                    init();

                    sequencer = MidiSystem.getSequencer(); // default MIDI Sequencer
                    sequencer.open();

                    // Create a custom device that can listen to MidiEvents
                    myDevice = new MyMidiDevice();

                    // input is instantiated in the init() method
                    input.open();

                    transmitter = input.getTransmitter();

                    // Route all info from midi through our custom device
                    transmitter.setReceiver(myDevice);

                    // Get the receiver class from sequencer
                    receiver = sequencer.getReceiver();

                    // Output info fed through custom device to proper receiver
                    myDevice.setReceiver(receiver);

                    // Create a new sequence
                    Sequence seq = new Sequence(Sequence.PPQ, 24);
                    // And of course a track to record the input on
                    Track currentTrack = seq.createTrack();
                    // Do some sequencer settings
                    sequencer.setSequence(seq);
                    sequencer.setTickPosition(0);
                    sequencer.recordEnable(currentTrack, -1);
                    // And start recording
                    sequencer.startRecording();

                    colorPaletteLabel.setText("Awaiting MIDI input...");

                    initializeAlgorithms();
                    updateVisuals();

                    recordButton.setEnabled(false);
                    stopButton.setEnabled(true);
                } catch (InvalidMidiDataException imde) {
                    System.out.println("Invalid Midi exception.");
                } catch (MidiUnavailableException mue) {
                    System.out.println("Midi Unavailable exception.");
                } catch (Exception ex){
                    System.out.println("Exception - MIDI Keyboard not found?");
                    colorPaletteLabel.setText("MIDI Keyboard not found.");
                }

            }
        });
        stopButton.addActionListener(e -> {
            try {
                sequencer.stopRecording(); //stop recording

                transmitter.setReceiver(receiver);

                //save the sequence and stick it in a file
                Sequence tmp = sequencer.getSequence();

                FileDialog saveFile = new FileDialog(new JFrame(), "Save", FileDialog.SAVE);
                saveFile.setVisible(true);
                String dir;
                dir = saveFile.getDirectory();
                midiFile = saveFile.getDirectory() + saveFile.getFile();
//                System.out.println(dir);
//                System.out.println(midiFile);

                MidiSystem.write(tmp, 0, new File(midiFile));
                colorPaletteLabel.setText("Session saved: " + saveFile.getFile());

                recordButton.setEnabled(true);
                stopButton.setEnabled(false);
            } catch (IOException i){
                System.out.println("End Exception");
            } catch (NullPointerException npe) {
                System.out.println("Null Pointer Exception");
            }

        });

        //Allows user to open and then play a file through the selected output device

        uploadButton.addActionListener(e -> {

            init();

            try{
                FileDialog fd = new FileDialog(new JFrame());
                fd.setFile("*.mid");
                fd.setVisible(true);
                File[] f = fd.getFiles();
                String filePath = null;
                if (f.length > 0) {
                    filePath = fd.getFiles()[0].getAbsolutePath();
                }

                File playFile = new File(filePath);
                InputStream ios = new BufferedInputStream(new FileInputStream(playFile));


                initializeAlgorithms();

                Sequence sequence = MidiSystem.getSequence(ios);

                for (Track track :  sequence.getTracks()) {
                    for (int i=0; i < track.size(); i++) {
                        MidiEvent event = track.get(i);
                        MidiMessage message = event.getMessage();
                        interpretMidi(message);
                    }
                }

                colorPaletteLabel.setText("Color Palette of '" + fd.getFiles()[0].getName() + "'.");


            } catch (NullPointerException npe) {
                System.out.println("No File Chosen");
                colorPaletteLabel.setText("No file uploaded.  Please press 'Record' to start a session or upload a file.");
            } catch (InvalidMidiDataException imde) {
                System.out.println("Invalid Midi Data Exception");
            } catch (IOException ioe) {
                System.out.println("End Exception");
            }

        });
        color0.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseClicked(MouseEvent e) {
                super.mouseClicked(e);
                Clipboard clipboard = Toolkit.getDefaultToolkit().getSystemClipboard();
                clipboard.setContents(new StringSelection(color0Label.getText()), null);
                colorPaletteLabel.setText("Color Palette - Copied " + color0Label.getText() +" to clipboard");
            }
        });
        color1.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseClicked(MouseEvent e) {
                super.mouseClicked(e);
                Clipboard clipboard = Toolkit.getDefaultToolkit().getSystemClipboard();
                clipboard.setContents(new StringSelection(color1Label.getText()), null);
                colorPaletteLabel.setText("Color Palette - Copied " + color1Label.getText() +" to clipboard");
            }
        });
        color2.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseClicked(MouseEvent e) {
                super.mouseClicked(e);
                Clipboard clipboard = Toolkit.getDefaultToolkit().getSystemClipboard();
                clipboard.setContents(new StringSelection(color2Label.getText()), null);
                colorPaletteLabel.setText("Color Palette - Copied " + color2Label.getText() +" to clipboard");
            }
        });
        color3.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseClicked(MouseEvent e) {
                super.mouseClicked(e);
                Clipboard clipboard = Toolkit.getDefaultToolkit().getSystemClipboard();
                clipboard.setContents(new StringSelection(color3Label.getText()), null);
                colorPaletteLabel.setText("Color Palette - Copied " + color3Label.getText() +" to clipboard");
            }
        });
        color4.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseClicked(MouseEvent e) {
                super.mouseClicked(e);
                Clipboard clipboard = Toolkit.getDefaultToolkit().getSystemClipboard();
                clipboard.setContents(new StringSelection(color4Label.getText()), null);
                colorPaletteLabel.setText("Color Palette - Copied " + color4Label.getText() +" to clipboard");
            }
        });
        algComboBox.addActionListener(e -> {
            JComboBox cb = (JComboBox)e.getSource();
            selectedAlgorithm = cb.getSelectedIndex();
            updateVisuals();
        });
    }

    public static void main(String[] args) {
        try {
            UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
        } catch(Exception ignored){}

        JFrame frame = new JFrame("Palette");
        frame.setContentPane(new PaletteForm().main);
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        frame.pack();
        frame.setVisible(true);
    }

    // Kind of spaghetti, relies on the MIDI keyboard having the name 'Keyboard' in it
    // Maybe let the user choose MIDI input?
    private void init(){
        boolean outputFound = false, inputFound = false;
        try {
            Info[] info = MidiSystem.getMidiDeviceInfo();

            for (Info inf : info) {
                String name = inf.getName().replace(" ", "");
                System.out.println("\"NAME: " + name + "\"");
                if (name.contains(inputName) && !inputFound) {
                    input = MidiSystem.getMidiDevice(inf);
                    inputFound = true;
                }
                if (name.contains(outputName) && !outputFound) {
                    output = MidiSystem.getMidiDevice(inf);
                    outputFound = true;
                }
            }

        }
        catch (Exception e) {
            e.printStackTrace();
        }
    }


    private class MyMidiDevice implements Transmitter, Receiver
    {

        private Receiver receiver;

        @Override
        public Receiver getReceiver()
        {
            return this.receiver;
        }

        @Override
        public void setReceiver(Receiver receiver)
        {
            this.receiver = receiver;
        }

        @Override
        public void close() { }

        // Perform real-time computations on MidiMessages we receive
        @Override
        public void send(MidiMessage message, long timeStamp)
        {
            interpretMidi(message);
            this.getReceiver().send(message, timeStamp);
        }
    }

    private void interpretMidi(MidiMessage message) {
        int NOTE_ON = 0x90;
        int NOTE_OFF = 0x80;
        String[] NOTE_NAMES = {"C", "C#", "D", "D#", "E", "F", "F#", "G", "G#", "A", "A#", "B"};

        if (message instanceof ShortMessage) {
            ShortMessage sm = (ShortMessage) message;

            if (sm.getCommand() == NOTE_ON) {
                int key = sm.getData1();
                int octave = (key / 12) - 1;
                int note = key % 12;
                String noteName = NOTE_NAMES[note];
                int velocity = sm.getData2();

                // When velocity is above zero, the key was pressed
                // Add to chord
                if (velocity > 0) {

                    // Add notes to chord data structures
                    if(!chord.contains(noteName + octave)) {
                        chord.add(noteName + octave);
                        chord2.add(octave + noteName);
                    }

                    // Update all algorithms
                    for (int j = 0; j < ALG_COUNT; j++) {
                        algorithm[j].add(note, velocity, octave);
                    }
                }

                // When velocity is zero, the key was released
                // Remove from chord
                if (velocity == 0) {
                    chord.remove(noteName + octave);
                    chord2.remove(octave + noteName);
                }

                // Display chord
                colorPaletteLabel.setText(chord.toString());
                chordLabel.setText(isMajorMinor(chord2));

                updateVisuals();
            }
            // Some keyboards make use of NOTE_OFF events instead of velocity = 0
            else if (sm.getCommand() == NOTE_OFF) {
                int key = sm.getData1();
                int octave = (key / 12) - 1;
                int note = key % 12;
                String noteName = NOTE_NAMES[note];

                chord.remove(noteName + octave);
            }
        }

    }

    private void updateVisuals() {
        Color[] colors = algorithm[selectedAlgorithm].getColors();

        color0.setBackground(colors[0]);
        color1.setBackground(colors[1]);
        color2.setBackground(colors[2]);
        color3.setBackground(colors[3]);
        color4.setBackground(colors[4]);
        color0Label.setText(String.format("#%02x%02x%02x", colors[0].getRed(), colors[0].getGreen(), colors[0].getBlue()));
        color1Label.setText(String.format("#%02x%02x%02x", colors[1].getRed(), colors[1].getGreen(), colors[1].getBlue()));
        color2Label.setText(String.format("#%02x%02x%02x", colors[2].getRed(), colors[2].getGreen(), colors[2].getBlue()));
        color3Label.setText(String.format("#%02x%02x%02x", colors[3].getRed(), colors[3].getGreen(), colors[3].getBlue()));
        color4Label.setText(String.format("#%02x%02x%02x", colors[4].getRed(), colors[4].getGreen(), colors[4].getBlue()));
    }

    private String isMajorMinor(ArrayList<String> notes) {
        int chordSize = notes.size();
        if (chordSize < 3){
            return "";
        }

        String[] arr1 = {"A", "A#", "B", "C", "C#","D", "D#", "E", "F", "F#","G", "G#"};
        ArrayList<String> notesMaster = new ArrayList<>(Arrays.asList(arr1));

        String[] arr2 = {"C", "C#","D", "D#", "E", "F", "F#","G", "G#", "A", "A#", "B"}; //notes in keyboard order
        ArrayList<String> notesMaster2 = new ArrayList<>(Arrays.asList(arr2));


        //sort based on order on the keyboard instead of order played
        notes.sort((o1, o2) -> {
            if (o1.substring(0, 1).equals(o2.substring(0, 1))) //in the same octave, use order specified above
                return Integer.compare(notesMaster2.indexOf(o1.substring(1)), notesMaster2.indexOf(o2.substring(1)));
            else
                return o1.substring(0, 1).compareTo(o2.substring(0, 1));
        });


        ArrayList<Integer> intervals = new ArrayList<>();
        int noteLength = notes.get(0).length();
        //System.out.println(notes.toString());
        int indexA = notesMaster.indexOf(notes.get(0).substring(1));
        int interval, indexB;



        for(int i = 1; i < chordSize; i++){
            noteLength = notes.get(i).length();
            //System.out.println("CHORDSIZE: " + chordSize + " i = " + i);

            //System.out.print(notes.get(i).substring(1));
            // System.out.println(" - " + notes.get(i-1).substring(1));


            //get the index of the note, use substring to exclude the octave
            indexB = notesMaster.indexOf(notes.get(i).substring(1));
            //System.out.println ( indexB + " - " + indexA + " = " + (indexB-indexA));
            interval = indexB-indexA;

            if (interval <0){
                interval +=12;
            }
            intervals.add(interval);

            indexA = indexB;

        }
        //System.out.println(intervals.toString());


        //interval of 5/4/3 == Major
        //intervals of 3/4/5 == Minor

        if (intervals.contains(3) && intervals.contains(4)){
            if (intervals.indexOf(3) < intervals.indexOf(4)){
                return notes.get(0).substring(1) + " MINOR ROOT\n";
            }
            return  notes.get(0).substring(1) + " MAJOR ROOT\n";

        }
        else if (intervals.contains(5) && intervals.contains(4)){
            if (intervals.indexOf(4) < intervals.indexOf(5)){
                return notes.get(1).substring(1) + " MINOR, 2ND INVERSION\n";
            }
            return notes.get(1).substring(1) + " MAJOR, 2ND INVERSION\n";

        }
        else if (intervals.contains(3) && intervals.contains(5)) {
            if (intervals.indexOf(3) > intervals.indexOf(5)) {
                return notes.get(2).substring(1) + " MINOR  1ST INVERSION\n";
            }
            return notes.get(2).substring(1) + " MAJOR, 1ST INVERSION\n";

        }
        return "";
    }
}

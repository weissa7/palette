import javax.sound.midi.*;
import javax.swing.*;
import java.awt.*;
import java.awt.event.*;
import java.io.*;

import java.util.*; //this includes Scanner
import java.util.concurrent.TimeUnit;

import javax.sound.midi.MidiDevice;
import javax.sound.midi.MidiDevice.Info;
import javax.sound.midi.MidiSystem;
import javax.sound.midi.Receiver;
import javax.sound.midi.Sequencer;

public class PaletteForm {

    private JPanel main;
    private JButton recordButton;
    private JButton stopButton;
    private JLabel noteLabel;
    private JButton uploadButton;
    private JPanel colorPanel;
    private JPanel color0;
    private JPanel color1;
    private JPanel color2;
    private JPanel color3;
    private JPanel color4;

    private static String inputName  = "Keyboard";
    private static String outputName = "Gervill";
    private MidiDevice input;
    private MidiDevice output;
    private String midiFile = "MyTestMidiFile.mid";

    private Sequencer sequencer;
    private Transmitter transmitter;
    private Receiver receiver;
    private MyMidiDevice myDevice;
    private ArrayList<String> chord;

    public PaletteForm() {

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

                    noteLabel.setText("Awaiting MIDI input...");

                    chord = new ArrayList<String>();

                    recordButton.setEnabled(false);
                    stopButton.setEnabled(true);
                } catch (InvalidMidiDataException imde) {
                    System.out.println("Invalid Midi exception.");
                } catch (MidiUnavailableException mue) {
                    System.out.println("Midi Unavailable exception.");
                } catch (Exception ex){
                    System.out.println("Exception - MIDI Keyboard not found?");
                    noteLabel.setText("MIDI Keyboard not found.");
                }

            }
        });
        stopButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                try {
                    sequencer.stopRecording(); //stop recording

                    transmitter.setReceiver(receiver);

                    noteLabel.setText("Session saved: MyTestMidiFile.mid");

                    //save the sequence and stick it in a file
                    Sequence tmp = sequencer.getSequence();
                    MidiSystem.write(tmp, 0, new File(midiFile));
                    recordButton.setEnabled(true);
                    stopButton.setEnabled(false);
                } catch (IOException i) {
                    System.out.println("End exception.");
                } catch (NullPointerException npe) {
                    System.out.println("Null Pointer Exception");
                }

            }
        });

        //Allows user to open and then play a file through the selected output device

        uploadButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {

                init();

                try{
                    FileDialog fd = new FileDialog(new JFrame());
                    fd.setVisible(true);
                    File[] f = fd.getFiles();
                    String filePath = null;
                    if (f.length > 0) {
                        filePath = fd.getFiles()[0].getAbsolutePath();
                    }

                    File playFile = new File(filePath);
                    InputStream ios = new BufferedInputStream(new FileInputStream(playFile));
                    MidiToPalette runner = new MidiToPalette();
                    /*sequencer = MidiSystem.getSequencer(false);
                    sequencer.setSequence(ios);
                    sequencer.open();
                    output.open();
                    //Sequence seq = sequencer.getSequence();
                    receiver = output.getReceiver();
                    sequencer.getTransmitter().setReceiver(receiver);
                    sequencer.start();*/

                    Sequence sequence = MidiSystem.getSequence(ios);

                    int NOTE_ON = 0x90;
                    int NOTE_OFF = 0x80;
                    String[] NOTE_NAMES = {"C", "C#", "D", "D#", "E", "F", "F#", "G", "G#", "A", "A#", "B"};


                    int trackNumber = 0;
                    for (Track track :  sequence.getTracks()) {
                        trackNumber++;
                        System.out.println("Track " + trackNumber + ": size = " + track.size());
                        System.out.println();
                        for (int i=0; i < track.size(); i++) {
                            MidiEvent event = track.get(i);
                            System.out.print("@" + event.getTick() + " ");
                            MidiMessage message = event.getMessage();
                            if (message instanceof ShortMessage) {
                                ShortMessage sm = (ShortMessage) message;
                                System.out.print("Channel: " + sm.getChannel() + " ");
                                if (sm.getCommand() == NOTE_ON) {
                                    int key = sm.getData1();
                                    int octave = (key / 12)-1;
                                    int note = key % 12;
                                    String noteName = NOTE_NAMES[note];
                                    int velocity = sm.getData2();
                                    System.out.println("Note on, " + noteName + octave + " key=" + key + " velocity: " + velocity);
                                    runner.add(note, velocity);

                                } else if (sm.getCommand() == NOTE_OFF) {
                                    int key = sm.getData1();
                                    int octave = (key / 12)-1;
                                    int note = key % 12;
                                    String noteName = NOTE_NAMES[note];
                                    int velocity = sm.getData2();
                                    System.out.println("Note off, " + noteName + octave + " key=" + key + " velocity: " + velocity);
                                } else {
                                    System.out.println("Command:" + sm.getCommand());
                                }
                            } else {
                                System.out.println("Other message: " + message.getClass());
                            }
                        }

                        System.out.println();
                    }

                    /*for (Track track : seq.getTracks()) {
                        for(int i = 0; i < track.size(); i++) {
                            MidiEvent event = track.get(i);
                            MidiMessage message = event.getMessage();
                            int NOTE_ON = 0x90;
                            int NOTE_OFF = 0x80;
                            System.out.println("Test");

                            if (message instanceof ShortMessage) {
                                ShortMessage sm = (ShortMessage) message;
                                System.out.println("Sm");
                                if (sm.getCommand() == NOTE_ON) {
                                    int key = sm.getData1();
                                    int octave = (key / 12)-1;
                                    int note = key % 12;
                                    //String noteName = NOTE_NAMES[note];
                                    int velocity = sm.getData2();
                                    System.out.println(event.getTick() + " " + sm.getChannel() + " " + key + " " + octave + " " + note + " " + velocity);
                                    // When velocity is above zero, the key was pressed
                                    // Add to chord
                                    if (velocity > 0) {
                                        System.out.println("Adding note");
                                        runner.add(note, velocity);
                                    }

                                } else if (sm.getCommand() == NOTE_OFF) {
                                    int key = sm.getData1();
                                    int octave = (key / 12)-1;
                                    int note = key % 12;
                                    //String noteName = NOTE_NAMES[note];
                                    int velocity = sm.getData2();
                                    System.out.println("Note off, " + octave + " key=" + key + " velocity: " + velocity);
                                }

                            }
                        }
                    }*/

                    runner.printMe();

                    Color[] colors = runner.getColors();

                    System.out.println(colors[0]);

                    color0.setBackground(colors[0]);
                    color1.setBackground(colors[1]);
                    color2.setBackground(colors[2]);
                    color3.setBackground(colors[3]);
                    color4.setBackground(colors[4]);




                } catch (NullPointerException npe) {
                    System.out.println("No File Chosen");
                    noteLabel.setText("No file uploaded.  Please press 'Record' to start a session or upload a file.");
                } catch (InvalidMidiDataException imde) {
                    System.out.println("Invalid Midi Data Exception");
                } catch (IOException ioe) {
                    System.out.println("End Exception");
                } /*catch (MidiUnavailableException mue){
                    System.out.println("Midi Unavailable Exception");
                }*/


            }

        });
    }

    public static void main(String[] args) {
        try {
            UIManager.setLookAndFeel("com.sun.java.swing.plaf.nimbus.NimbusLookAndFeel");
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
                System.out.println("\"NAME:" + name + "\"");
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
        public void close()
        {
        }

        // Perform real-time computations on MidiMessages we receive
        @Override
        public void send(MidiMessage message, long timeStamp)
        {
            int NOTE_ON = 0x90;
            int NOTE_OFF = 0x80;
            String[] NOTE_NAMES = {"C", "C#", "D", "D#", "E", "F", "F#", "G", "G#", "A", "A#", "B"};

            if (message instanceof ShortMessage) {
                ShortMessage sm = (ShortMessage) message;

                if (sm.getCommand() == NOTE_ON) {
                    int key = sm.getData1();
                    int octave = (key / 12)-1;
                    int note = key % 12;
                    String noteName = NOTE_NAMES[note];
                    int velocity = sm.getData2();

                    // When velocity is above zero, the key was pressed
                    // Add to chord
                    if (velocity > 0) {
                        chord.add(noteName + octave);
                    }

                    // When velocity is zero, the key was released
                    // Remove from chord
                    if (velocity == 0 && chord.contains(noteName + octave)) {
                        chord.remove(noteName + octave);
                    }

                    // Display chord
                    noteLabel.setText(chord.toString());


                }
                // Never used, should probably be removed
                /*else if (sm.getCommand() == NOTE_OFF) {
                    int key = sm.getData1();
                    int octave = (key / 12)-1;
                    int note = key % 12;
                    String noteName = NOTE_NAMES[note];
                    int velocity = sm.getData2();
                    System.out.println("Note off, " + noteName + octave + " key=" + key + " velocity: " + velocity);
                } else {
                    //System.out.println("Command:" + sm.getCommand());
                } */
            } else {
                System.out.println("Other message: " + message.getClass());
            }
            this.getReceiver().send(message, timeStamp);
        }
    }
}

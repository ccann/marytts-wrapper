/**
 * MaryTTSComponent implementation class for the TTS server.
 *
 * @author: Cody Canning; cody.canning@tufts.edu
 *
 */

package com.tts;

import ade.ADEComponentImpl;
import java.io.BufferedReader;
import java.io.DataInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.rmi.RemoteException;
import java.util.Locale;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.sound.sampled.*;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamResult;
import marytts.datatypes.MaryData;
import marytts.datatypes.MaryDataType;
import marytts.modules.synthesis.Voice;
import marytts.server.Mary;
import marytts.server.Request;
import marytts.util.data.audio.AudioPlayer;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;

public final class MaryTTSComponentImpl extends ADEComponentImpl implements MaryTTSComponent {
    boolean initialized = false;
    private MaryDataType inputType;//.TEXT or .RAWMARYXML
    private MaryDataType outputType = MaryDataType.AUDIO;
    private Voice voice = Voice.getVoice("cmu-slt-hsmm");
    private String defaultEffects;
    private String defaultStyle;
    private Locale locale;
    private int id;
    private String emphasis = "silent";
    private AudioFileFormat audioFileFormat;
    private AudioInputStream ais;
    private AudioPlayer ap;
    private Request request;
    private MaryData maryData;
    private BufferedReader br;

    private static enum Emotion {STRESS, CONFUSION, ANGER, CUSTOM1, NONE;}
    private Emotion e = Emotion.NONE;

    // boolean markers set by args flags
    private static boolean saveToWav = false;
    private boolean useEmotion = false;

    // if saving to a .wav file, save at this location
    private final String wavFilename = "/tmp/adeplay.wav";
    private boolean speaking = false;


    /** MaryTTSComponentImpl constructor. Initializes TTS component.
     *
     * @throws IOException
     * @throws UnsupportedAudioFileException
     * @throws InterruptedException
     */
    public MaryTTSComponentImpl() throws IOException, UnsupportedAudioFileException, InterruptedException {
        super();
        init();

        try {
            if (Mary.currentState() == Mary.STATE_OFF) {
                Mary.startup();
            }
        } catch (Exception ex) {
            Logger.getLogger(MaryTTSComponentImpl.class.getName()).log(Level.SEVERE, null, ex);
        }
    }

    /**
     * extension of constructor.
     */
    private void init() {
        if (initialized) {
            System.out.println(" already initialized");
            return;
        }
        locale = new Locale("en-US");
        defaultEffects = null;
        defaultStyle = null;
        id = 0;

        audioFileFormat = new AudioFileFormat(AudioFileFormat.Type.WAVE, Voice.AF16000, AudioSystem.NOT_SPECIFIED);
        //audioFileFormat = new AudioFileFormat(AudioFileFormat.Type.WAVE, Voice.AF22050, AudioSystem.NOT_SPECIFIED);

        if (this.e != Emotion.NONE) {
            this.useEmotion = true;
            System.out.println("Using " + this.e.toString());
        }

        initialized = true;
    }

    /**
     * Checks if speech is being produced.
     *
     * @return <tt>true</tt> if speech is being produced, <tt>false</tt> otherwise
     * @throws RemoteException if an error occurs
     */
    @Override
    public boolean isSpeaking() throws RemoteException {
        return speaking;
    }

    /**
     * Stops an ongoing utterance.
     *
     * @return <tt>true</tt> if speech is interrupted, <tt>false</tt> otherwise.
     * @throws RemoteException if an error occurs
     */
    @Override
    public boolean stopUtterance() throws RemoteException {
        ap.interrupt();
        if (ap.isInterrupted()) {
            return true;
        }
        else {
            return false;
        }
    }

    /**
     * sets prosody to STRESS ANGER or CONFUSION
     *
     * @param newEmo the new prosody to be used
     */
    @Override
    public void setEmotion(String newEmo) {
        for (Emotion emo : Emotion.values()) {
            if (emo.toString().equalsIgnoreCase(newEmo)) {
                this.e = emo;
            }
        }
        if (this.e == Emotion.NONE){
            this.useEmotion = false;
        }
        else {
            this.useEmotion = true;
            System.out.println("Applying " + this.e.toString());

        }
    }

    /**
     * returns the current vocal emotion
     */
    @Override
    public String getEmotion() {
        return this.e.toString();
    }

    @Override
    public boolean sayText(String text) throws RemoteException {
        sayText(text, true);
        return true;
    }

    /**
     * Speaks appropriate text
     *
     * @param text the text to be spoken
     * @param wait whether or not to block until speaking call returns
     *
     * @return true if the text was generated as speech, false otherwise
     */
    @Override
    public boolean sayText(String text, boolean wait) throws RemoteException {
        speaking = true;

        // GB: add punctuation if none-exists, default to '.'
        if (!(text.endsWith(".") || text.endsWith(",") || text.endsWith("?") || text.endsWith("!"))) {
            text += ".";
        }

        try {
            if (useEmotion) {
                applyEmotion(text);
            }
            else {
                inputType = MaryDataType.TEXT;
                maryData = new MaryData(inputType, Locale.US);
                maryData.setData(text);
            }
            request = new Request(inputType,
                                  outputType,
                                  locale,
                                  voice,
                                  defaultEffects,
                                  defaultStyle,
                                  id,
                                  audioFileFormat);

            if (useEmotion) {
                request.readInputData(br);
            } else {
                request.setInputData(maryData);
            }

            request.process();
            ais = request.getOutputData().getAudio();

            if (saveToWav) {
                File f = new File(wavFilename);
                AudioSystem.write(ais, audioFileFormat.getType(), f);
            } else {
                ap = new AudioPlayer(ais);
                ap.start();
                if (wait) {
                    ap.join();
                }
            }

            speaking = false;
            return true;

        } catch (Exception ex) {
            Logger.getLogger(MaryTTSComponentImpl.class.getName()).log(Level.SEVERE, null, ex);
            speaking = false;
            return false;
        }
    }
    /**
     * applies Emotion e to input s by wrapping the utterance in Mary XML
     *
     * @param s the string we're applying emotion to
     */
    private void applyEmotion(String s) {
        inputType = MaryDataType.RAWMARYXML;
        try {

            // build maryxml document
            DocumentBuilderFactory dbf = DocumentBuilderFactory.newInstance();
            DocumentBuilder db = dbf.newDocumentBuilder();
            Document doc = db.newDocument();

            // create elements
            Element maryEl = doc.createElement("maryxml");
            maryEl.setAttribute("version", "0.4");
            maryEl.setAttribute("xmlns:xsi", "http://www.w3.org/2001/XMLSchema-instance");
            maryEl.setAttribute("xmlns", "http://mary.dfki.de/2002/MaryXML");
            maryEl.setAttribute("xml:lang", "en-US");
            doc.appendChild(maryEl);
            Element pEl = doc.createElement("p");
            maryEl.appendChild(pEl);
            Element prosodyEl = doc.createElement("prosody");

            // generate emotive markup
            switch (this.e) {
                case STRESS: // nervous, stressed, fearful
                    prosodyEl.setAttribute("contour", "(0%,+3st)(10%,+3st)(20%,+3st)(30%,+10st)(40%,+4st)(50%,+4st)(60%,+4st)(70%,+9st)(80%,+7st)(90%,+10st)(100%,+11st)");
                    prosodyEl.setAttribute("rate", "1.15");
                    break;
                case ANGER: // angry, frustrated
                    prosodyEl.setAttribute("contour", "(0%,-2st)(10%,-2st)(20%,-2st)(30%,-2st)(40%,-2st)(50%,-2st)(60%,-3st)(70%,-3st)(80%,-3st)(90%,-4st)(100%,-4st)");
                    prosodyEl.setAttribute("rate", "0.82");
                    break;
                case CONFUSION: // confused, puzzled
                    prosodyEl.setAttribute("contour", "(0%,-1st)(10%,-1st)(20%,-1st)(30%,-1st)(40%,-1st)(50%,-1st)(60%,-2st)(70%,+3st)(80%,+3st)(90%,+10st)(100%,+6st)");
                    prosodyEl.setAttribute("rate", "0.85");
                    prosodyEl.setAttribute("volume","0.0");
                    break;
                case CUSTOM1:

                default:
            }

            // add text with prosody markup to XML file 
            String utterance = s;
            Node utt = doc.createTextNode(utterance);
            prosodyEl.appendChild(utt);
            pEl.appendChild(prosodyEl);

            //appendTextWithEmphasis(doc, s, prosodyEl);
            //pEl.appendChild(prosodyEl);

            // XML transform
            TransformerFactory transformerFactory = TransformerFactory.newInstance();
            Transformer transformer = transformerFactory.newTransformer();
            DOMSource source = new DOMSource(doc);
            StreamResult result = new StreamResult(new File("utterance.xml"));
            transformer.transform(source, result);
            System.out.println("Mary XML file generated!");

            // delegate datastream
            FileInputStream fstream = new FileInputStream("utterance.xml");
            DataInputStream in = new DataInputStream(fstream);
            br= new BufferedReader(new InputStreamReader(in));

        } catch (Exception ex) {
            Logger.getLogger(MaryTTSComponentImpl.class.getName()).log(Level.SEVERE, null, ex);
        }
    }


    /**
     * adds emphasis to uppercase words in String s
     *
     * @param s
     * @return the updated String including emphasis
     */
    private void appendTextWithEmphasis(Document doc, String s, Element e) {
        Element prosEl = doc.createElement("prosody");
        prosEl.setAttribute("volume", emphasis);
        String[] words = getWords(s);
        String w = "";
        for (int i = 0; i < words.length; i++) {
            if (isAllUppercase(words[i])) {
                e.appendChild(doc.createTextNode(w));
                prosEl.appendChild(doc.createTextNode(words[i]));
                e.appendChild(prosEl);
                w = " ";
            } else {
                w = w + words[i] + " ";
            }
        }
        e.appendChild(doc.createTextNode(w));
    }

    /**
     * parses String s and returns the list of words contained in it
     *
     * @param s
     * @return list of strings
     */
    private String[] getWords(String s) {
        String[] words = new String[getNumWhitespaces(s) + 1];
        String word = "";
        int x = 0;
        for (int i = 0; i < s.length(); i++) {
            if (!Character.isWhitespace(s.charAt(i))) {
                word = word.concat(Character.toString(s.charAt(i)));
            } else {
                words[x] = (word);
                word = "";
                x++;
            }
        }
        words[x] = (word);
        return words;
    }

    /**
     * gets the number of white spaces in String s
     *
     * @param s
     * @return int; the number of white spaces
     */
    private int getNumWhitespaces(String s) {
        int spaces = 0;
        for (int i = 0; i < s.length(); i++) {
            if (Character.isWhitespace(s.charAt(i))) {
                spaces++;
            }
        }
        return spaces;
    }

    /**
     * *
     * Returns true if string s is all uppercase, false otherwise
     *
     * @param s
     * @return true or false
     */
    private boolean isAllUppercase(String s) {
        boolean allUpper = true;
        for (int i = 0; i < s.length(); i++) {
            if (Character.isLowerCase(s.charAt(i))) {
                allUpper = false;
            }
        }
        return allUpper;
    }

    @Override
    protected void updateComponent() {
    }

    @Override
    protected boolean localServicesReady() {
        return initialized;
    }

    @Override
    public String getGuiHelp() throws RemoteException {
	return "sayText: enter any string you want to say\n"
		+ "sayText (with boolean): enter any string you want to say, with blocking true/false\n" 
		+ "isSpeaking: is component currently speaking\n"
		+ "stopUtterance: cancel utterance from being said\n"
		+ "setEmotion: set either \"stress\", \"anger\", or \"confusion\""
		+ " (without quotes).\n"
		+ "getEmotion: get currently set emotion";
    }

    @Override
    protected String additionalUsageInfo() {
        // GB: added flag to enable save-to-file behavior
        // CC: added flags to enable prosody, emoting
        return "-wav\t\t save to wav play instead of sending directly to audioout\n"
            + "-stress\t\t apply stressed prosody to an utterance\n"
            + "-anger\t\t apply angry, frustrated prosody to an utterance\n"
            + "-confusion\t\t apply confused, perplexed prosody to an utterance\n";
    }

    @Override
    protected boolean parseadditionalargs(String[] args) {
        init(); // added 
        for (int i = 0; i < args.length; i++) {
            if (args[i].equalsIgnoreCase("-wav")) {
                System.out.println("found WAV, saving to file");
                saveToWav = true;
            } else if (args[i].equalsIgnoreCase("-stress")) {
                System.out.println("found STRESS");
                this.e = Emotion.STRESS;
            } else if (args[i].equalsIgnoreCase("-anger")) {
                System.out.println("found ANGER");
                this.e = Emotion.ANGER;
            } else if (args[i].equalsIgnoreCase("-confusion")) {
                System.out.println("found CONFUSION");
                this.e = Emotion.CONFUSION;
            }
        }

        return true;
        // should return false if it gets an unsupported argument
    }

    // empty Overrides ... silly that we need these

    @Override
    protected void localshutdown() { /*
                                      * do nothing
                                      */ }

    @Override
    protected void updateFromLog(String logEntry) { /*
                                                     * do nothing
                                                     */ }

    @Override
    protected void clientConnectReact(String user) { /*
                                                      * do nothing
                                                      */ }

    @Override
    protected boolean clientDownReact(String user) {
        return false;
    }

    @Override
    protected void componentDownReact(String serverkey, String[][] constraints) {
    }

    @Override
    protected void componentConnectReact(String serverkey, Object ref, String[][] constraints) {
    }

    @Override
    protected boolean localrequestShutdown(Object credentials) {
        return false;
    }
}

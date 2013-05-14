/**
 * MaryTTSComponent implementation class for the TTS server.
 *
 * @author: Cody Canning; cody.canning@tufts.edu
 *
 */

package com.tts;

import ade.ADEComponentImpl;
import java.io.*;
import org.apache.commons.io.IOUtils;
import java.rmi.RemoteException;
import java.util.Locale;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.sound.sampled.*;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamResult;
<<<<<<< HEAD
=======
import marytts.datatypes.MaryData;
>>>>>>> cb4f4a619cc94926cfd4f6c2c5f429b72153220d
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
<<<<<<< HEAD
    private static Voice voice;
=======
    private Voice voice = Voice.getVoice("cmu-slt-hsmm");
>>>>>>> cb4f4a619cc94926cfd4f6c2c5f429b72153220d
    private String defaultEffects;
    private String defaultStyle;
    private Locale locale;
    private int id;
<<<<<<< HEAD
=======
    private String emphasis = "silent";
>>>>>>> cb4f4a619cc94926cfd4f6c2c5f429b72153220d
    private AudioFileFormat audioFileFormat;
    private AudioInputStream ais;
    private AudioPlayer ap;
    private Request request;
<<<<<<< HEAD
=======
    private MaryData maryData;
>>>>>>> cb4f4a619cc94926cfd4f6c2c5f429b72153220d
    private BufferedReader br;
    Document doc;
    DocumentBuilder db;
    Transformer tr;
<<<<<<< HEAD
    //private static String voiceStr = "cmu-rms=hsmm"; // default to male voice
    private static String voiceStr = "cmu-slt-hsmm";    // default to female voice

    private static enum Emotion {STRESS, CONFUSION, ANGER, CUSTOM1, NONE}
    private static Emotion e = Emotion.NONE;

    // boolean markers set by args flags
    private static boolean saveToWav = false;
=======

    private static enum Emotion {STRESS, CONFUSION, ANGER, CUSTOM1, NONE;}
    private Emotion e = Emotion.NONE;

    // boolean markers set by args flags
    private static boolean saveToWav = false;
    private boolean useEmotion = false;
>>>>>>> cb4f4a619cc94926cfd4f6c2c5f429b72153220d
    private String markup = "SSML";

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
<<<<<<< HEAD

        setVoice(voiceStr);
        //this.sayText("Hi My name is andy");

=======
>>>>>>> cb4f4a619cc94926cfd4f6c2c5f429b72153220d
    }

    /**
     * extension of constructor.
     */
    private void init() {
        if (initialized) {
<<<<<<< HEAD
            System.out.println("already initialized");
=======
            System.out.println(" already initialized");
>>>>>>> cb4f4a619cc94926cfd4f6c2c5f429b72153220d
            return;
        }
        locale = new Locale("en-US");
        defaultEffects = null;
        defaultStyle = null;
        id = 0;

        audioFileFormat = new AudioFileFormat(AudioFileFormat.Type.WAVE, Voice.AF16000, AudioSystem.NOT_SPECIFIED);
        //audioFileFormat = new AudioFileFormat(AudioFileFormat.Type.WAVE, Voice.AF22050, AudioSystem.NOT_SPECIFIED);

<<<<<<< HEAD
=======
        if (this.e != Emotion.NONE) {
            this.useEmotion = true;
            System.out.println("Using " + this.e.toString());
        }
>>>>>>> cb4f4a619cc94926cfd4f6c2c5f429b72153220d

        // initialize components necessary for building and transforming XML doc later
        try {
            DocumentBuilderFactory dbf = DocumentBuilderFactory.newInstance();
            db = dbf.newDocumentBuilder();
            TransformerFactory transformerFactory = TransformerFactory.newInstance();
            this.tr = transformerFactory.newTransformer();
        }

        catch (Exception ex){
            Logger.getLogger(MaryTTSComponentImpl.class.getName()).log(Level.SEVERE, null, ex);
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
<<<<<<< HEAD
        return ap.isInterrupted();
=======
        if (ap.isInterrupted()) {
            return true;
        }
        else {
            return false;
        }
>>>>>>> cb4f4a619cc94926cfd4f6c2c5f429b72153220d
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
<<<<<<< HEAD
        System.out.println("Applying " + this.e.toString());

=======
        if (this.e == Emotion.NONE){
            this.useEmotion = false;
        }
        else {
            this.useEmotion = true;
            System.out.println("Applying " + this.e.toString());
        }
>>>>>>> cb4f4a619cc94926cfd4f6c2c5f429b72153220d
    }

    /**
     * returns the current vocal emotion
     */
    @Override
    public String getEmotion() {
        return this.e.toString();
    }

<<<<<<< HEAD
    /**
     * @return  the current voice used for TTS
     */
    @Override
    public String getVoice(){
        return this.voice.toString();
    }

    /**
     * @param v  name of the voice or male or female
     */
    @Override
    public void setVoice(String v){
        String vc = v;

        if (v.equalsIgnoreCase("male")){
            vc = "cmu-rms-hsmm";
        }
        else if (v.equalsIgnoreCase("female")){
            vc = "cmu-slt-hsmm";
        }

        voice = Voice.getVoice(vc);
    }

=======
>>>>>>> cb4f4a619cc94926cfd4f6c2c5f429b72153220d
    @Override
    public boolean sayText(String text) throws RemoteException {
        sayText(text, true);
        return true;
    }

    /**
     * @return the markup to be used (MARYXML or SSML)
     */
//    public String getMarkup(){
//        return this.markup;
//    }

    /**
     *  @param m  the markup to use (MARYXML or SSML)
     */
//    public void setMarkup(String m){
//        this.markup = m;
//    }

    /**
     *
     * @return the available sentence-wide emotions
     */
//    public String getAvailableEmotions(){
//        String emos = "";
//        for (Emotion emo : Emotion.values()) {
//            emos = emos + ", " + emo.toString();
//        }
//        return emos;
//    }

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
<<<<<<< HEAD

=======
>>>>>>> cb4f4a619cc94926cfd4f6c2c5f429b72153220d
        speaking = true;
        doc = db.newDocument();

        // GB: add punctuation if none-exists, default to '.'
        if (!(text.endsWith(".") || text.endsWith(",") || text.endsWith("?") || text.endsWith("!"))) {
            text += ".";
        }

        try {
<<<<<<< HEAD

            text = demoHack(text);
=======
>>>>>>> cb4f4a619cc94926cfd4f6c2c5f429b72153220d
            // search for words in ALLCAPS and wrap them with emphasis tags
            String emphUtt = addEmphasis(text);
            // apply appropriate emotion (including NONE)
            applyEmotion(emphUtt);

            request = new Request(inputType,
                                  outputType,
                                  locale,
                                  voice,
                                  defaultEffects,
                                  defaultStyle,
                                  id,
                                  audioFileFormat);

            request.readInputData(br);
            request.process();
            ais = request.getOutputData().getAudio();

            // either save the audio to .wav or output it as audio
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
     * kind of a hack... When adding emphasis tags to utterance they aren't escaped by the XML transformer.
     * this function fixes the opening and closing XML brackets.
     *
     * @param fname  file name to fix XML tags of
     * @throws IOException
     */
    private void fixXML(String fname) throws IOException {
        FileInputStream fis = new FileInputStream(fname);
        String content = IOUtils.toString(fis);
        content = content.replace("&lt;", "<");
        content = content.replace("&gt;", ">");
        fis.close();
        FileOutputStream fos = new FileOutputStream(fname);
        IOUtils.write(content, fos);
        fos.close();
    }

    /**
     * generate utterance with RAWMARYXML markup. populates doc with MARYXML header and paragraph tag
     *
     * @return the child element
     * @throws ParserConfigurationException
     */
    private Element createRAWMARYXMLDoc() throws ParserConfigurationException {
        inputType = MaryDataType.RAWMARYXML;

        // create elements
        Element maryEl = doc.createElement("maryxml");
        maryEl.setAttribute("version", "0.4");
        maryEl.setAttribute("xmlns:xsi", "http://www.w3.org/2001/XMLSchema-instance");
        maryEl.setAttribute("xmlns", "http://mary.dfki.de/2002/MaryXML");
        maryEl.setAttribute("xml:lang", "en-US");
        doc.appendChild(maryEl);
        Element pEl = doc.createElement("p");
        maryEl.appendChild(pEl);
        return pEl;
    }
    /**
     * generate utterance with SSML markup. populates doc with SSML header and paragraph tag
     *
     * @return the child element
     * @throws ParserConfigurationException
     */
    private Element createSSMLDoc() throws ParserConfigurationException {
        inputType = MaryDataType.SSML;

        // create elements
        Element maryEl = doc.createElement("speak");
        maryEl.setAttribute("version", "1.0");
        maryEl.setAttribute("xmlns", "http://www.w3.org/2001/10/synthesis");
        maryEl.setAttribute("xmlns:xsi", "http://www.w3.org/2001/XMLSchema-instance");
        maryEl.setAttribute("xsi:schemaLocation","http://www.w3.org/2001/10/synthesis " +
                                                 "http://www.w3.org/TR/speech-synthesis/synthesis.xsd");
        maryEl.setAttribute("xml:lang", "en-US");
        doc.appendChild(maryEl);
        Element pEl = doc.createElement("p");
        maryEl.appendChild(pEl);
        return pEl;
    }

    /**
     * applies Emotion e to input s by wrapping the utterance in Mary XML
     *
     * @param s the string we're applying emotion to
     */
    private void applyEmotion(String s) {
        try {
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
                    break;
                default:
                    break;
            }

            // append the utterance with prosody markup to doc.
<<<<<<< HEAD
            Node utt = doc.createTextNode(s);
=======
            String utterance = s;
            Node utt = doc.createTextNode(utterance);
>>>>>>> cb4f4a619cc94926cfd4f6c2c5f429b72153220d

            System.out.println(utt);

            prosodyEl.appendChild(utt);
            if (this.markup.equalsIgnoreCase("SSML")) {
                createSSMLDoc().appendChild(prosodyEl);
            }
            else {
                createRAWMARYXMLDoc().appendChild(prosodyEl);
            }

            // transform the document to XML
            DOMSource source = new DOMSource(doc);
            StreamResult result = new StreamResult(new File("utterance.xml"));
            this.tr.transform(source, result);

            fixXML("utterance.xml");

            // delegate datastream
            FileInputStream fstream = new FileInputStream("utterance.xml");
            DataInputStream in = new DataInputStream(fstream);
            this.br= new BufferedReader(new InputStreamReader(in));


        } catch (Exception ex) {
            Logger.getLogger(MaryTTSComponentImpl.class.getName()).log(Level.SEVERE, null, ex);
        }
    }

    /**
     * adds emphasis to uppercase words in String s
     *
     * @param utterance  the utterance text we are searching for ALLCAPS words and emphasizing
     * @return the updated String including emphasis
     */
    private String addEmphasis(String utterance) {
        String[] words = utterance.split("\\s+");
        String newUtterance = "";
        for (int i = 0; i < words.length; i++) {
            if (isAllUppercase(words[i])) {
                words[i] = "<emphasis level=\"strong\">" + words[i] + "</emphasis>";
            }
            newUtterance = newUtterance + " " + words[i];
        }
        return newUtterance;
    }

<<<<<<< HEAD
    private String demoHack(String s){
        if (s.equalsIgnoreCase("I dont know how to do that") || (s.equalsIgnoreCase("I don't know how to do that")) ||
           (s.equalsIgnoreCase("I dont know how to do that.")) || (s.equalsIgnoreCase("I don't know how to do that."))) {
            return "i dont know how to <prosody pitch=\"+40%\" rate=\"x-slow\"><emphasis level=\"strong\">do</emphasis></prosody>that!";
        }
        else {return s;}
    }
=======

>>>>>>> cb4f4a619cc94926cfd4f6c2c5f429b72153220d

    /**
     * *
     * Returns true if string s is all uppercase, false otherwise
     *
<<<<<<< HEAD
     * @param s  the string to check
=======
     * @param s
>>>>>>> cb4f4a619cc94926cfd4f6c2c5f429b72153220d
     * @return true or false
     */
    private boolean isAllUppercase(String s) {
        boolean allUpper = true;
        for (int i = 0; i < s.length(); i++) {
            if (Character.isLowerCase(s.charAt(i))) {
                allUpper = false;
                break;
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
<<<<<<< HEAD
		+ "getEmotion: get currently set emotion\n"
        + "setVoice: specify voice to use by name. Or by gender (\"female\" or \"male\").\n"
        + "getVoice: get the current voice being used";
=======
		+ "getEmotion: get currently set emotion";
>>>>>>> cb4f4a619cc94926cfd4f6c2c5f429b72153220d
    }

    @Override
    protected String additionalUsageInfo() {
        // GB: added flag to enable save-to-file behavior
        return "-wav\t\t save to wav play instead of sending directly to audioout\n"
            + "-stress\t\t apply stressed prosody to an utterance\n"
            + "-anger\t\t apply angry, frustrated prosody to an utterance\n"
<<<<<<< HEAD
            + "-confusion\t\t apply confused, perplexed prosody to an utterance\n"
            + "-male\t\t use male voice\n"
            + "-female\t\t use female voice\n";
=======
            + "-confusion\t\t apply confused, perplexed prosody to an utterance\n";
>>>>>>> cb4f4a619cc94926cfd4f6c2c5f429b72153220d
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
<<<<<<< HEAD
            } else if(args[i].equalsIgnoreCase("-male")){
                System.out.println("found MALE");
                voiceStr = "male";
            } else if(args[i].equalsIgnoreCase("-female")){
                System.out.println("found FEMALE");
                voiceStr = "female";
=======
>>>>>>> cb4f4a619cc94926cfd4f6c2c5f429b72153220d
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

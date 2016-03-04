package libs;

import java.io.IOException;
import java.net.URL;

import javax.sound.midi.InvalidMidiDataException;
import javax.sound.midi.MetaEventListener;
import javax.sound.midi.MetaMessage;
import javax.sound.midi.MidiSystem;
import javax.sound.midi.MidiUnavailableException;
import javax.sound.midi.Receiver;
import javax.sound.midi.Sequence;
import javax.sound.midi.Sequencer;
import javax.sound.midi.ShortMessage;
import javax.sound.midi.Synthesizer;

/**
 * Midi clip class for playing sounds.
 * 
 * @author williamhooper
 */
public class AudioMidi implements MetaEventListener
{
    /**
     * Audio Midi states
     * 
     * @author williamhooper
     * 
     */
    public enum AudioMidiState
    {
        PLAYING, LOOPING, STOPPED, DONE, CLOSED
    };

    public static final int MAX_VOLUME = 11;
    public static final int LOOP_CONTINUOUSLY = Sequencer.LOOP_CONTINUOUSLY;

    private static final int VOLUME_CONTROLLER = 7;
    private static final int PAN_CONTROLLER = 10;
    private static final int BALANCE_CONTROLLER = 8;

    private URL audioURL;
    private Sequence sequence;
    private Sequencer sequencer;
    private Synthesizer synthesizer;
    private AudioMidiState audioState;

    /**
     * Constructor
     * 
     * @param obj
     * @param filename
     * @throws InvalidMidiDataException
     * @throws IOException
     * @throws MidiUnavailableException
     */
    public AudioMidi( Object obj, String filename ) throws InvalidMidiDataException, IOException, MidiUnavailableException
    {
        java.net.URL audioURL = obj.getClass().getResource( filename );
        if ( audioURL != null )
        {
            this.audioURL = audioURL;
            load();
            audioState = AudioMidiState.DONE;
        }
        else
        {
            throw new IOException( "Unable to load file: " + filename );
        }
    }

    /**
     * Constructor
     * 
     * @param audioURL
     * @throws InvalidMidiDataException
     * @throws IOException
     * @throws MidiUnavailableException
     */
    public AudioMidi( URL audioURL ) throws InvalidMidiDataException, IOException, MidiUnavailableException
    {
        this.audioURL = audioURL;
        load();
        audioState = AudioMidiState.DONE;
    }

    /**
     * Return the state of the audio clip
     * 
     * @return AudioMidiState
     */
    public synchronized AudioMidiState getState()
    {
        return audioState;
    }

    /**
     * Get the length of the audio clip
     * 
     * @return long
     */
    public synchronized long getTime()
    {
        return ( sequence == null ) ? 0L : sequence.getMicrosecondLength();
    }

    /**
     * Loop the audio clip
     * 
     * @param loop
     */
    public synchronized void loop( int loop )
    {
        if ( sequence == null )
        {
            return;
        }

        /**
         * rewind
         */
        sequencer.setTickPosition( 0L );

        /**
         * Start looping
         */
        sequencer.setLoopCount( loop );
        sequencer.start();
        audioState = AudioMidiState.LOOPING;
    }

    /**
     * Rewind the audio clip and play
     * 
     */
    public synchronized void play()
    {
        if ( sequence == null )
        {
            return;
        }

        /**
         * rewind
         */
        sequencer.setTickPosition( 0L );

        /**
         * Start playing
         */
        sequencer.start();
        audioState = AudioMidiState.PLAYING;
    }

    /**
     * Set the balance from -1.0 for left, 0.0 for center and 1.0 for right
     * 
     * @param range
     * @throws MidiUnavailableException
     * @throws InvalidMidiDataException
     */
    public synchronized void setBalance( double range ) throws MidiUnavailableException, InvalidMidiDataException
    {
        int index;
        int midiBalance = ( int ) ( int ) ( ( range + 1.0 ) * 127.0 / 2.0 );

        if ( sequencer != null && sequencer instanceof Synthesizer )
        {
            synthesizer = ( Synthesizer ) sequencer;
            javax.sound.midi.MidiChannel[] channels = synthesizer.getChannels();
            for ( index = 0; channels != null && index < channels.length; index++ )
            {
                channels[ index ].controlChange( BALANCE_CONTROLLER, midiBalance );
            }
        }
        else if ( synthesizer != null )
        {
            javax.sound.midi.MidiChannel[] channels = synthesizer.getChannels();
            for ( index = 0; channels != null && index < channels.length; index++ )
            {
                channels[ index ].controlChange( BALANCE_CONTROLLER, midiBalance );
            }
        }
        else
        {
            Receiver receiver = MidiSystem.getReceiver();
            ShortMessage volumeMessage = new ShortMessage();
            for ( index = 0; index < 16; index++ )
            {
                volumeMessage.setMessage( ShortMessage.CONTROL_CHANGE, index, BALANCE_CONTROLLER, midiBalance );
                receiver.send( volumeMessage, -1 );
            }
        }
    }

    /**
     * Set the pan from -1.0 for left, 0.0 for center and 1.0 for right
     * 
     * @param range
     * @throws MidiUnavailableException
     * @throws InvalidMidiDataException
     */
    public synchronized void setPan( double range ) throws MidiUnavailableException, InvalidMidiDataException
    {
        int index;
        int midiPan = ( int ) ( int ) ( ( range + 1.0 ) * 127.0 / 2.0 );

        if ( sequencer != null && sequencer instanceof Synthesizer )
        {
            synthesizer = ( Synthesizer ) sequencer;
            javax.sound.midi.MidiChannel[] channels = synthesizer.getChannels();
            for ( index = 0; channels != null && index < channels.length; index++ )
            {
                channels[ index ].controlChange( PAN_CONTROLLER, midiPan );
            }
        }
        else if ( synthesizer != null )
        {
            javax.sound.midi.MidiChannel[] channels = synthesizer.getChannels();
            for ( index = 0; channels != null && index < channels.length; index++ )
            {
                channels[ index ].controlChange( PAN_CONTROLLER, midiPan );
            }
        }
        else
        {
            Receiver receiver = MidiSystem.getReceiver();
            ShortMessage volumeMessage = new ShortMessage();
            for ( index = 0; index < 16; index++ )
            {
                volumeMessage.setMessage( ShortMessage.CONTROL_CHANGE, index, PAN_CONTROLLER, midiPan );
                receiver.send( volumeMessage, -1 );
            }
        }
    }

    /**
     * Set playback volume 0 (muted) to 11 (loud)
     * 
     * @throws MidiUnavailableException
     * @throws InvalidMidiDataException
     */
    public void setVolume( double volume ) throws MidiUnavailableException, InvalidMidiDataException
    {
        int index;
        int midiVolume = ( int ) ( ( volume / ( double ) 11.0 ) * 127 );

        if ( sequencer != null && sequencer instanceof Synthesizer )
        {
            synthesizer = ( Synthesizer ) sequencer;
            javax.sound.midi.MidiChannel[] channels = synthesizer.getChannels();
            for ( index = 0; channels != null && index < channels.length; index++ )
            {
                channels[ index ].controlChange( VOLUME_CONTROLLER, midiVolume );
            }
        }
        else if ( synthesizer != null )
        {
            javax.sound.midi.MidiChannel[] channels = synthesizer.getChannels();
            for ( index = 0; channels != null && index < channels.length; index++ )
            {
                channels[ index ].controlChange( VOLUME_CONTROLLER, midiVolume );
            }
        }
        else
        {
            Receiver receiver = MidiSystem.getReceiver();
            ShortMessage volumeMessage = new ShortMessage();
            for ( index = 0; index < 16; index++ )
            {
                volumeMessage.setMessage( ShortMessage.CONTROL_CHANGE, index, VOLUME_CONTROLLER, midiVolume );
                receiver.send( volumeMessage, -1 );
            }
        }
    }

    /**
     * Start playing the audio clip from the current position
     * 
     */
    public synchronized void start()
    {
        if ( sequence == null )
        {
            return;
        }

        /**
         * Start playing
         */
        sequencer.start();
        audioState = AudioMidiState.PLAYING;
    }

    /**
     * Stop playing the audio clip
     * 
     */
    public synchronized void stop()
    {
        if ( sequence == null )
        {
            return;
        }

        /**
         * Stop playing
         */
        sequencer.stop();
        audioState = AudioMidiState.STOPPED;
    }

    /**
     * Private method to load audio file
     * 
     * @throws IOException
     * @throws InvalidMidiDataException
     * @throws MidiUnavailableException
     * 
     */
    private void load() throws InvalidMidiDataException, IOException, MidiUnavailableException
    {
        /**
         * Read the sequence from the file
         */
        sequence = MidiSystem.getSequence( audioURL );

        /**
         * Get a Sequencer to play sequences of MIDI events
         */
        sequencer = MidiSystem.getSequencer( false );
        sequencer.open();
        sequencer.setSequence( sequence );

        sequencer.addMetaEventListener( this );

        /**
         * Get a Synthesizer for the Sequencer to send notes to
         */
        synthesizer = MidiSystem.getSynthesizer();
        synthesizer.open();

        synthesizer.getChannels();

        /**
         * you need to do a check here to see if the java sound bank was loaded
         */
        if ( synthesizer.getDefaultSoundbank() == null )
        {
            /**
             * then you know that java sound is using the hardware soundbank
             */
            sequencer.getTransmitter().setReceiver( MidiSystem.getReceiver() );
        }
        else
        {
            /**
             * link the sequencer to the synthesizer
             */
            sequencer.getTransmitter().setReceiver( synthesizer.getReceiver() );
        }
    }

    /**
     * Close the audio clip.
     * 
     */
    public void close()
    {
        if ( sequence == null )
        {
            return;
        }

        sequencer.stop();
        audioState = AudioMidiState.CLOSED;
        sequencer.close();

        synthesizer.close();

        sequence = null;
    }

    @Override
    public void meta( MetaMessage event )
    {
        if ( event.getType() == 47 )
        {
            sequencer.stop();
            audioState = AudioMidiState.DONE;
        }
    }

}

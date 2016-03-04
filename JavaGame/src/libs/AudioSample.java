package libs;

import java.io.IOException;
import java.net.URL;

import javax.sound.sampled.AudioFormat;
import javax.sound.sampled.AudioInputStream;
import javax.sound.sampled.AudioSystem;
import javax.sound.sampled.Clip;
import javax.sound.sampled.DataLine;
import javax.sound.sampled.FloatControl;
import javax.sound.sampled.LineEvent;
import javax.sound.sampled.LineListener;
import javax.sound.sampled.LineUnavailableException;
import javax.sound.sampled.UnsupportedAudioFileException;

/**
 * Audio sample class for playing sounds.
 * 
 * @author williamhooper
 */
public class AudioSample implements LineListener
{

    /**
     * Audio Sample states
     * 
     * @author williamhooper
     * 
     */
    public enum AudioSampleState
    {
        PLAYING, LOOPING, STOPPED, DONE, CLOSED
    };

    public static final int MAX_VOLUME = 11;
    public static final int LOOP_CONTINUOUSLY = Clip.LOOP_CONTINUOUSLY;

    private URL audioURL;
    private Clip audioClip;
    private AudioSampleState audioState;
    private AudioInputStream audioStream;

    /**
     * Constructor, create an audio sample from an existing audio file
     * 
     * @param obj
     * @param filename
     * @throws IOException
     * @throws UnsupportedAudioFileException
     * @throws LineUnavailableException
     */
    public AudioSample( Object obj, String filename ) throws IOException, UnsupportedAudioFileException,
            LineUnavailableException
    {
        java.net.URL audioURL = obj.getClass().getResource( filename );
        if ( audioURL != null )
        {
            this.audioURL = audioURL;
            load();
            audioState = AudioSampleState.DONE;
        }
        else
        {
            throw new IOException( "Could not find file: " + filename );
        }
    }

    /**
     * Constructor, create an audio sample from an existing audio URL
     * 
     * @param audioURL
     * @throws LineUnavailableException
     * @throws IOException
     * @throws UnsupportedAudioFileException
     */
    public AudioSample( URL audioURL ) throws UnsupportedAudioFileException, IOException, LineUnavailableException
    {
        this.audioURL = audioURL;
        load();
        audioState = AudioSampleState.DONE;
    }

    /**
     * Close the audio clip.
     * 
     */
    public synchronized void close()
    {
        if ( audioClip == null )
        {
            return;
        }

        if ( audioClip.isRunning() )
        {
            audioClip.stop();
        }
        audioClip.removeLineListener( this );
        audioClip.close();

        audioState = AudioSampleState.CLOSED;
        audioClip = null;
    }

    /**
     * Return the state of the audio clip
     * 
     * @return AudioSampleState
     */
    public synchronized AudioSampleState getState()
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
        if ( audioClip == null )
        {
            return 0;
        }

        return audioClip.getMicrosecondLength();
    }

    /**
     * Loop the audio clip
     * 
     * @param loop
     */
    public synchronized void loop( int loop )
    {
        if ( audioClip == null )
        {
            return;
        }

        /**
         * rewind
         */
        audioClip.setFramePosition( 0 );

        /**
         * Start looping
         */
        audioClip.loop( loop );
        audioState = AudioSampleState.LOOPING;
    }

    /**
     * Rewind the audio clip and play
     * 
     */
    public synchronized void play()
    {
        if ( audioClip == null )
        {
            return;
        }

        /**
         * rewind
         */
        audioClip.setFramePosition( 0 );

        /**
         * Start playing
         */
        audioClip.start();
        audioState = AudioSampleState.PLAYING;
    }

    /**
     * Set the balance from -1.0 for left, 0.0 for center and 1.0 for right
     * 
     * @param range
     */
    public synchronized void setBalance( float range )
    {
        if ( audioClip == null )
        {
            return;
        }

        FloatControl gainControl = ( FloatControl ) audioClip.getControl( FloatControl.Type.BALANCE );
        gainControl.setValue( range );
    }

    /**
     * Set the pan from -1.0 for left, 0.0 for center and 1.0 for right
     * 
     * @param range
     */
    public synchronized void setPan( float range )
    {
        if ( audioClip == null )
        {
            return;
        }

        FloatControl gainControl = ( FloatControl ) audioClip.getControl( FloatControl.Type.PAN );
        gainControl.setValue( range );
    }

    /**
     * Set the volume from 0 (muted) to 11 (loud)
     * 
     * @param volume
     */
    public synchronized void setVolume( float volume )
    {
        if ( audioClip == null )
        {
            return;
        }

        FloatControl gainControl = ( FloatControl ) audioClip.getControl( FloatControl.Type.MASTER_GAIN );
        float gain = ( float ) ( volume / 11.0 );
        float dB = ( float ) ( Math.log( gain ) / Math.log( 10.0 ) * 20.0 );
        gainControl.setValue( dB );
    }

    /**
     * Start playing the audio clip from the current position
     * 
     */
    public synchronized void start()
    {
        if ( audioClip == null )
        {
            return;
        }

        /**
         * Start playing
         */
        audioClip.start();
        audioState = AudioSampleState.PLAYING;
    }

    /**
     * Stop playing the audio clip
     * 
     */
    public synchronized void stop()
    {
        if ( audioClip == null )
        {
            return;
        }

        /**
         * Stop playing
         */
        audioClip.stop();
        audioState = AudioSampleState.STOPPED;
    }

    @Override
    public void update( LineEvent evt )
    {
        if ( evt.getType() == LineEvent.Type.STOP )
        {
            audioState = AudioSampleState.DONE;
        }
    }

    /**
     * Private method to load audio file
     * 
     * @throws LineUnavailableException
     * @throws UnsupportedAudioFileException
     * @throws IOException
     */
    private void load() throws UnsupportedAudioFileException, IOException, LineUnavailableException
    {
        audioStream = AudioSystem.getAudioInputStream( ( URL ) audioURL );

        /**
         * we can't yet open the device for ALAW/ULAW playback, convert
         * ALAW/ULAW to PCM
         */
        AudioFormat audioFormat = audioStream.getFormat();

        if ( ( audioFormat.getEncoding() == AudioFormat.Encoding.ULAW )
                || ( audioFormat.getEncoding() == AudioFormat.Encoding.ALAW ) )
        {
            AudioFormat tmp = new AudioFormat( AudioFormat.Encoding.PCM_SIGNED, audioFormat.getSampleRate(),
                    audioFormat.getSampleSizeInBits() * 2, audioFormat.getChannels(), audioFormat.getFrameSize() * 2,
                    audioFormat.getFrameRate(), true );
            audioStream = AudioSystem.getAudioInputStream( tmp, audioStream );
            audioFormat = tmp;
        }

        DataLine.Info info = new DataLine.Info( Clip.class, audioFormat,
                ( ( int ) audioStream.getFrameLength() * audioFormat.getFrameSize() ) );
        audioClip = ( Clip ) AudioSystem.getLine( info );

        /**
         * Add a listener for line events
         */
        audioClip.addLineListener( this );

        /**
         * This method does not return until the audio file is completely loaded
         */
        audioClip.open( audioStream );
    }

}

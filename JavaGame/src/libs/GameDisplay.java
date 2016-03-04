package libs;

import java.awt.AWTException;
import java.awt.BufferCapabilities;
import java.awt.BufferCapabilities.FlipContents;
import java.awt.Cursor;
import java.awt.Dimension;
import java.awt.DisplayMode;
import java.awt.Frame;
import java.awt.Graphics;
import java.awt.GraphicsConfiguration;
import java.awt.GraphicsDevice;
import java.awt.GraphicsEnvironment;
import java.awt.Image;
import java.awt.Point;
import java.awt.Rectangle;
import java.awt.Robot;
import java.awt.Toolkit;
import java.awt.event.KeyAdapter;
import java.awt.event.KeyEvent;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.event.MouseMotionAdapter;
import java.awt.image.BufferStrategy;
import java.util.ArrayList;

/**
 * Game display
 * 
 * @author williamhooper
 */
public class GameDisplay
{
    private static BufferStrategy bufferStrategy;
    private static boolean captureCursor;
    private static Graphics currentGraphics;
    private static Frame frameInstance = null;
    private static ArrayList< KeyboardEventListener > keyListeners = new ArrayList< KeyboardEventListener >();
    private static MouseEvent lastMouseEvent;
    private static ArrayList< MouseEventListener > mouseListeners = new ArrayList< MouseEventListener >();
    private static Robot robot;

    /**
     * Add a keyboard listener
     * 
     * @param kel
     */
    public static synchronized void addKeyboardListener( KeyboardEventListener kel )
    {
        keyListeners.add( kel );
    }

    /**
     * Add a mouse listener
     * 
     * @param mel
     */
    public static synchronized void addMouseListener( MouseEventListener mel )
    {
        mouseListeners.add( mel );
    }

    /**
     * Capture the mouse cursor so that it does not leave the display frame.
     * Holding the shift key will allow the cursor to escape.
     * 
     * @param value
     */
    public static void captureCursor( boolean value )
    {
        captureCursor = value;
    }

    /**
     * Create a game display
     * 
     * @param width
     * @param height
     * @throws AWTException
     */
    public static void create( int width, int height )
    {
        if ( frameInstance == null )
        {
            GraphicsEnvironment env = GraphicsEnvironment.getLocalGraphicsEnvironment();
            GraphicsDevice screenDevice = env.getDefaultScreenDevice();
            GraphicsConfiguration gc = screenDevice.getDefaultConfiguration();
            frameInstance = new Frame( gc );

            /**
             * Set the capture cursor state to false
             */
            captureCursor = false;

            /**
             * Turn off auto repaint and decorations
             */
            frameInstance.setIgnoreRepaint( true );
            frameInstance.setUndecorated( true );

            /**
             * Get focus so events come to here
             */
            frameInstance.setFocusable( true );
            frameInstance.requestFocus();

            /**
             * Set the graphics frame width and height
             */
            frameInstance.setSize( new Dimension( width, height ) );

            /**
             * Center frame on screen
             */
            frameInstance.setLocationRelativeTo( null );

            /**
             * Create a robot to manage our mouse
             */
            try
            {
                robot = new Robot();
                robot.mouseMove( frameInstance.getX() + frameInstance.getWidth() / 2,
                        frameInstance.getY() + frameInstance.getHeight() / 2 );
            }
            catch ( AWTException e )
            {
                throw new RuntimeException( e.getMessage() );
            }

            /**
             * Add a mouse motion listener so we can get mouse motion. The AWT
             * graphics library for Java has two mouse listeners, one for mouse
             * movement and one for the mouse buttons. The GameDisplay class
             * consolidates these two listeners and manages its own mouse
             * listeners to reduce the amount of required methods.
             */
            frameInstance.addMouseMotionListener( new MouseMotionAdapter()
            {
                @Override
                public void mouseDragged( MouseEvent me )
                {
                    /**
                     * If the mouse is dragged (moved with a button pressed)
                     * outside the frame, move it back
                     */
                    if ( !frameInstance.getBounds().contains( me.getXOnScreen(), me.getYOnScreen() ) )
                    {
                        processMouseEvent( me );
                    }
                    else
                    {
                        synchronized ( mouseListeners )
                        {
                            for ( MouseEventListener listener : mouseListeners )
                            {
                                listener.mouseEvent( me );
                            }
                            lastMouseEvent = me;
                        }
                    }
                }

                @Override
                public void mouseMoved( MouseEvent me )
                {
                    /**
                     * If the mouse is moved outside the frame, move it back
                     */
                    if ( !frameInstance.getBounds().contains( me.getXOnScreen(), me.getYOnScreen() ) )
                    {
                        processMouseEvent( me );
                    }
                    else
                    {
                        synchronized ( mouseListeners )
                        {
                            for ( MouseEventListener listener : mouseListeners )
                            {
                                listener.mouseEvent( me );
                            }
                            lastMouseEvent = me;
                        }
                    }
                }
            } );

            /**
             * Add a mouse listener so we can get mouse clicks
             */
            frameInstance.addMouseListener( new MouseAdapter()
            {
                @Override
                public void mousePressed( MouseEvent me )
                {
                    synchronized ( mouseListeners )
                    {
                        for ( MouseEventListener listener : mouseListeners )
                        {
                            listener.mouseEvent( me );
                        }

                    }
                    lastMouseEvent = me;
                }

                @Override
                public void mouseReleased( MouseEvent me )
                {
                    synchronized ( mouseListeners )
                    {
                        for ( MouseEventListener listener : mouseListeners )
                        {
                            listener.mouseEvent( me );
                        }

                    }
                    lastMouseEvent = me;
                }

                @Override
                public void mouseClicked( MouseEvent me )
                {
                    synchronized ( mouseListeners )
                    {
                        for ( MouseEventListener listener : mouseListeners )
                        {
                            listener.mouseEvent( me );
                        }

                    }
                    lastMouseEvent = me;
                }

                @Override
                public void mouseExited( MouseEvent me )
                {
                    processMouseEvent( me );
                }
            } );

            /**
             * Add a keyboard listener so we can get key presses and releases
             */
            frameInstance.addKeyListener( new KeyAdapter()
            {
                @Override
                public void keyPressed( KeyEvent ke )
                {
                    for ( KeyboardEventListener listener : keyListeners )
                    {
                        listener.keyboardEvent( ke );
                    }
                }

                @Override
                public void keyReleased( KeyEvent ke )
                {
                    for ( KeyboardEventListener listener : keyListeners )
                    {
                        listener.keyboardEvent( ke );
                    }
                }
            } );

            /**
             * Make the frame visible. Wait until the frame is really visible to
             * avoid an exception when creating the buffer strategy
             */
            frameInstance.setVisible( true );
            do
            {} while ( !frameInstance.isVisible() );

            /**
             * Create a double buffer strategy
             */
            frameInstance.createBufferStrategy( 2 );
            bufferStrategy = frameInstance.getBufferStrategy();
        }
    }

    /**
     * Display the buffer capabilities of the default graphics environment. This
     * method is available for debugging the display.
     * 
     */
    public static void displaygetBufferCapabilities()
    {
        GraphicsEnvironment env = GraphicsEnvironment.getLocalGraphicsEnvironment();
        GraphicsDevice screenDevice = env.getDefaultScreenDevice();
        GraphicsConfiguration gc = screenDevice.getDefaultConfiguration();

        BufferCapabilities bc = gc.getBufferCapabilities();
        System.out.println( "isFullScreenRequired = " + bc.isFullScreenRequired() );
        System.out.println( "isMultiBufferAvailable = " + bc.isMultiBufferAvailable() );
        System.out.println( "isPageFlipping = " + bc.isPageFlipping() );

        FlipContents fc = bc.getFlipContents();
        System.out.println( "getFlipContents = " + fc.toString() );
    }

    /**
     * Dispose of the game display
     * 
     */
    public static void dispose()
    {
        if ( frameInstance != null )
        {
            /**
             * Dispose of the frame instance and set to null
             */
            frameInstance.dispose();
            frameInstance = null;
        }
        else
        {
            throw new RuntimeException( "Game display not created" );
        }
    }

    /**
     * Return the bounds of the game display as a rectangle
     * 
     * @return Rectangle
     */
    public static Rectangle getBounds()
    {
        if ( frameInstance != null )
        {
            return new Rectangle( 0, 0, frameInstance.getWidth(), frameInstance.getHeight() );
        }
        else
        {
            throw new RuntimeException( "Game display not created" );
        }
    }

    /**
     * Get the current graphics context
     * 
     * @return Graphics
     */
    public static Graphics getContext()
    {
        if ( frameInstance != null )
        {
            currentGraphics = bufferStrategy.getDrawGraphics();
            return currentGraphics;
        }
        else
        {
            throw new RuntimeException( "Game display not created" );
        }
    }

    /**
     * Hide the cursor
     * 
     */
    public static void hideCursor()
    {
        if ( frameInstance != null )
        {
            /**
             * Hide the mouse cursor
             */
            Image cursorImage = Toolkit.getDefaultToolkit().getImage( "xparent.gif" );
            Cursor blankCursor = Toolkit.getDefaultToolkit().createCustomCursor( cursorImage, new Point( 0, 0 ), "" );
            frameInstance.setCursor( blankCursor );
        }
        else
        {
            throw new RuntimeException( "Game display not created" );
        }
    }

    /**
     * Return the state of the captured cursor
     * 
     * @return boolean
     */
    public static boolean isCapturedCursor()
    {
        return captureCursor;
    }

    /**
     * Set to full screen
     * 
     */
    public static void setFullScreen()
    {
        if ( frameInstance != null )
        {
            int width = frameInstance.getWidth();
            int height = frameInstance.getHeight();

            GraphicsEnvironment env = GraphicsEnvironment.getLocalGraphicsEnvironment();
            GraphicsDevice screenDevice = env.getDefaultScreenDevice();

            /**
             * If full screen is supported then switch to full screen mode and
             * find the best display mode that matches the frame instance
             */
            if ( screenDevice.isFullScreenSupported() )
            {
                screenDevice.setFullScreenWindow( frameInstance );
                if ( screenDevice.isDisplayChangeSupported() )
                {
                    setDisplayMode( screenDevice, width, height );
                }
                else
                {
                    throw new RuntimeException( "display unable to change to requested resolution" );
                }
            }
        }
        else
        {
            throw new RuntimeException( "Game display not created" );
        }
    }

    /**
     * Update the current graphics screen
     * 
     */
    public static void update()
    {
        if ( frameInstance != null )
        {
            /**
             * If the buffer got lost (graphics memory reallocated before we
             * were done with it) then don't show.
             */
            if ( !bufferStrategy.contentsLost() )
            {
                bufferStrategy.show();
            }

            /**
             * Dispose of the graphics like we were asked to do
             */
            if ( currentGraphics != null )
            {
                currentGraphics.dispose();
            }

            /**
             * Make sure the context buffer is flushed to the screen
             */
            Toolkit.getDefaultToolkit().sync();
        }
        else
        {
            throw new RuntimeException( "Game display not created" );
        }
    }

    /**
     * Process the mouse event to keep the mouse movement within the game
     * display. Allow the mouse to escape the frame when the shift key is held
     * down.
     * 
     * @param me
     * @return
     */
    private static void processMouseEvent( MouseEvent me )
    {
        /**
         * If the mouse has left the building then force the mouse back onto the
         * frame. Since the mouse has left make sure to adjust the coordinates
         * relative to the frame
         */
        if ( captureCursor && !me.isShiftDown() )
        {
            robot.mouseMove( frameInstance.getX() + lastMouseEvent.getX(), frameInstance.getY() + lastMouseEvent.getY() );
        }
    }

    /**
     * Set the best display mode for the given width and height
     * 
     * @param device
     */
    private static void setDisplayMode( GraphicsDevice device, int width, int height )
    {
        int maxBitDepth = -1;
        DisplayMode bestMode;
        DisplayMode [ ] modes = device.getDisplayModes();

        bestMode = null;
        for ( int i = 0; i < modes.length; i++ )
        {
            if ( modes[ i ].getWidth() == width && modes[ i ].getHeight() == height && modes[ i ].getBitDepth() > maxBitDepth )
            {
                bestMode = modes[ i ];
            }
        }
        if ( bestMode != null )
        {
            device.setDisplayMode( bestMode );
        }
        else
        {
            throw new RuntimeException( "Unable to find matching display mode" );
        }
    }

    /**
     * Private constructor
     * 
     */
    private GameDisplay( )
    {
        /**
         * no code required
         */
    }

    @Override
    public Object clone() throws CloneNotSupportedException
    {
        throw new CloneNotSupportedException();
    }
}

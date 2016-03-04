package libs;

import java.util.LinkedList;

/**
 * Threaded game engine, with support for game events and collisions
 * 
 * @author williamhooper
 */

public class GameEngine implements Runnable, GameEventListener
{
    /**
     * Game engine instance
     */
    private static GameEngine gameEngineInstance = null;

    /**
     * game thread object
     */
    private static Thread gameThread;

    /**
     * Return true if the game engine is running
     * 
     * @return boolean
     */
    public static boolean isRunning()
    {
        return ( gameEngineInstance != null ) ? gameEngineInstance.running : false;
    }

    /**
     * Get the animation thread going. Calls start() once the thread is created
     * and running.
     * 
     * @param game
     */
    public static void start( Game game )
    {
        if ( gameEngineInstance == null )
        {
            /**
             * Create an instance of the game engine, initialize variables and
             * register the game event listener
             */
            gameEngineInstance = new GameEngine();
            gameEngineInstance.game = game;
            gameEngineInstance.gameEventList = new LinkedList< GameEvent >();
            GameEventDispatcher.addGameEventListener( gameEngineInstance );

            /**
             * Gentlemen, start your engines
             */
            gameThread = new Thread( gameEngineInstance );
            gameThread.start();

            /**
             * Go off and manage events
             */
            gameEngineInstance.manageGameEvents();
        }
    }

    /**
     * Stop the game engine. Will not immediately stop the game engine. The game
     * engine will stop at the beginning of the next frame update.
     * 
     */
    public static void stop()
    {
        if ( gameEngineInstance != null )
        {
            gameEngineInstance.running = false;
        }
    }

    /**
     * Number of frames per second
     */
    private final int DEFAULT_FPS = 60;

    /**
     * Desired time per rendered frame
     */
    private final long framePeriod = ( 1000 / DEFAULT_FPS ) * 1000000L;

    /**
     * Game object
     */
    private Game game;

    /**
     * Game event list for managing game events
     */
    private LinkedList< GameEvent > gameEventList = new LinkedList< GameEvent >();

    /**
     * Number of frames that can be skipped in any one animation loop
     */
    private final int MAX_FRAME_SKIPS = 2;

    /**
     * Number of frames with a delay of 0 ms before the animation thread yields
     * to other running threads.
     */
    private final int NO_DELAYS_PER_YIELD = 16;

    /**
     * Thread is running flag
     */
    private boolean running;

    /**
     * Private constructor
     * 
     */
    private GameEngine( )
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

    @Override
    public void gameEvent( GameEvent ge )
    {
        synchronized ( gameEventList )
        {
            gameEventList.add( ge );
            gameEventList.notify();
        }
    }

    /**
     * The run method is part of the Runnable interface and needs to be
     * implemented. It goes through an update-render-draw loop to drive the game
     * engine.
     * 
     */
    public void run()
    {
        long beforeTime, afterTime, timeDiff, sleepTime;
        long overSleepTime = 0L;
        int noDelays = 0;
        long excess = 0L;

        running = true;
        while ( running )
        {
            beforeTime = System.nanoTime();

            /**
             * Check for collisions among the game objects
             */
            game.collisions();

            /**
             * Update all the game objects
             */
            game.update();

            /**
             * Render all the game objects onto the offscreen buffer
             */
            game.render();

            /**
             * Draw the offscreen buffer to the screen
             */
            game.draw();

            /**
             * Calculate how long we should sleep *EVERYTHING AFTER HERE MANAGES FRAMERATE*
             */
            afterTime = System.nanoTime();
            timeDiff = afterTime - beforeTime;
            sleepTime = ( framePeriod - timeDiff ) - overSleepTime;

            if ( sleepTime > 0 )
            {
                /**
                 * some time left in this cycle
                 */
                try
                {
                    Thread.sleep( sleepTime / 1000000L ); // nano -> ms
                    noDelays = 0; // reset noDelays when sleep occurs
                }
                catch ( InterruptedException ex )
                {}
                overSleepTime = ( System.nanoTime() - afterTime ) - sleepTime;
            }
            else
            {
                /**
                 * If sleepTime <= 0 then the frame took longer than the period,
                 * so store the excess time value
                 */
                excess -= sleepTime;
                overSleepTime = 0L;

                if ( ++noDelays >= NO_DELAYS_PER_YIELD )
                {
                    /**
                     * give another thread a chance to run
                     */
                    Thread.yield();
                    noDelays = 0;
                }
            }

            /**
             * If frame animation is taking too long, update the game state
             * without rendering it, to get the updates/sec nearer to the
             * required FPS.
             */
            int skips = 0;
            while ( ( excess > framePeriod ) && ( skips < MAX_FRAME_SKIPS ) )
            {
                excess -= framePeriod;

                /**
                 * Check for collisions among the game objects
                 */
                game.collisions();

                /**
                 * Update all the game objects
                 */
                game.update();
                skips++;
            }

        }

        /**
         * If we get to this point, it's because running was set to false. In
         * that case, notify the game event manager to quit and exit the thread.
         */
        synchronized ( gameEventList )
        {
            gameEventList.notify();
        }
        return;
    }

    /**
     * Manage events in the event message queue
     */
    private void manageGameEvents()
    {
        GameEvent gameEvent;

        while ( true )
        {
            synchronized ( gameEventList )
            {
                /**
                 * If the game event list is empty then wait for an event to
                 * show up
                 */
                if ( gameEventList.isEmpty() )
                {
                    try
                    {
                        gameEventList.wait();
                    }
                    catch ( InterruptedException exception )
                    {}
                }

                /**
                 * Check to see if the game engine thread has stopped running.
                 * If so then we return regardless of the state of the game
                 * event list
                 */
                if ( !running )
                {
                    break;
                }

                /**
                 * Fetch the game event
                 */
                gameEvent = gameEventList.removeFirst();
            }

            /**
             * Deal with the game event
             */
            game.manageGameEvent( gameEvent );
        }
    }
}

package libs;

import java.util.ArrayList;

/**
 * Game Event Dispatcher. For the game engine library there will typically only
 * be one game event listener. The ability to maintain multiple game event
 * listeners is included more as an example of how multiple listeners would be
 * implemented.
 * 
 * @author williamhooper
 * 
 */

public class GameEventDispatcher
{
    /**
     * List of game listeners
     */
    private static ArrayList< GameEventListener > gameEventListeners = new ArrayList< GameEventListener >();

    /**
     * Add a game listener
     * 
     * @param gel
     */
    public static synchronized void addGameEventListener( GameEventListener gel )
    {
        gameEventListeners.add( gel );
    }

    /**
     * Dispatch a game event
     * 
     * @param event
     */
    public static synchronized void dispatchEvent( GameEvent event )
    {
        for ( GameEventListener listener : gameEventListeners )
        {
            listener.gameEvent( event );
        }
    }

    /**
     * Private constructor
     * 
     */
    private GameEventDispatcher( )
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

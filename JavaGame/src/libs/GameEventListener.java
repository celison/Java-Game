package libs;

/**
 * Game event listener interface.
 * 
 * @author williamhooper
 */

public interface GameEventListener
{
    /**
     * Receive a game event
     * 
     * @param ge
     */
    public void gameEvent( GameEvent ge );
}

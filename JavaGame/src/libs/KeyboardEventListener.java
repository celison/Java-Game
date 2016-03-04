package libs;

import java.awt.event.KeyEvent;

/**
 * Keyboard event listener. For the game library the keyboard events are
 * collected by the GameDisplay class and dispatched to listeners. The
 * GameDisplay class will pass on only the key pressed and ket released events.
 * 
 * @author williamhooper
 */

public interface KeyboardEventListener
{
    /**
     * Receive a keyboard event
     * 
     * @param ke
     */
    public void keyboardEvent( KeyEvent ke );
}

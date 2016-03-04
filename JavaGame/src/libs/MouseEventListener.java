package libs;

import java.awt.event.MouseEvent;

/**
 * Mouse event listener. For the game library the Mouse events are collected by
 * the GameDisplay class and dispatched to listeners. The GameDisplay class will
 * pass on only the mouse dragged, mouse moved, and mouse clicked events.
 * 
 * @author williamhooper
 */

public interface MouseEventListener
{
    /**
     * Receive a mouse event
     * 
     * @param me
     */
    public void mouseEvent( MouseEvent me );
}

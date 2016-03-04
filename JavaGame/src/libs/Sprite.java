package libs;

import java.awt.Graphics2D;
import java.awt.Rectangle;
import java.awt.event.KeyEvent;
import java.awt.event.MouseEvent;

/**
 * Sprite class
 * 
 * @author williamhooper
 */

public interface Sprite
{
    /**
     * Determine if the passed Sprite object collided with this object.
     * 
     * @param obj
     */
    public abstract void checkCollision( Sprite obj );

    /**
     * Draw method
     * 
     * @param g
     */
    public abstract void draw( Graphics2D g );

    /**
     * Return the bounding box for this sprite
     * 
     */
    public abstract Rectangle getBounds();

    /**
     * Update the sprite's state.
     * 
     */
    public abstract void update();

    /**
     * Receive a keyboard event
     * 
     * @param ke
     */
    public void keyboardEvent( KeyEvent ke );

    /**
     * Receive a mouse event
     * 
     * @param me
     */
    public void mouseEvent( MouseEvent me );

}

package libs;

import java.awt.Graphics2D;
import java.awt.Point;
import java.awt.Rectangle;
import java.awt.event.KeyEvent;
import java.awt.event.MouseEvent;

/**
 * Playing field class to manage a virtual graphics display that is larger than
 * the physical display. This class would typically manage a scrolling
 * background.
 * 
 * @author williamhooper
 */
public interface PlayingField
{
    /**
     * Draw the playing field onto the graphics context
     * 
     * @param g2d
     */
    public abstract void draw( Graphics2D g2d );

    /**
     * Return the bounds of the playing field as a rectangle
     * 
     * @return Rectangle
     */
    public abstract Rectangle getBounds();

    /**
     * Get the position of the game display on the playing field as a point
     * 
     * @return Point
     */

    public abstract Point getGameDisplayPosition();

    /**
     * Check to see it there is a collision with anything on the playing field
     * at the x,y coordinates
     * 
     * @param x
     * @param y
     * @return boolean
     */
    public boolean isCollision( double x, double y );

    /**
     * Receive a keyboard event.
     * 
     * @param ke
     */
    public abstract void keyboardEvent( KeyEvent ke );

    /**
     * Receive a mouse event.
     * 
     * @param me
     */
    public abstract void mouseEvent( MouseEvent me );

    /**
     * Convert x,y playing field coordinates to a point on the game display
     * 
     * @param x
     * @param y
     * @return Point
     */
    public abstract Point toGameDisplay( double x, double y );

    /**
     * Update the position of the game display on the playing field
     * 
     * @param x
     * @param y
     */
    public abstract void update( double x, double y );
}

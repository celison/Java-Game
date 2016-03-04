package missionToTitan;

import java.awt.Graphics2D;
import java.awt.Point;
import java.awt.Rectangle;
import java.awt.event.KeyEvent;
import java.awt.event.MouseEvent;
import java.awt.geom.AffineTransform;
import java.io.IOException;

import libs.GameDisplay;
import libs.GameEvent;
import libs.GameEvent.GameEventType;
import libs.GameEventDispatcher;
import libs.Sprite;
import libs.TileSheet;

public class AsteroidSprite implements Sprite
{
    private double xPos, yPos;
    private double xVel, yVel;
    private Rectangle asteroidShape, displayBounds;
    private long nextTimeUpdate;
    private int row = 0, column = 0;
    private Size size;
    private TileSheet asteroidTileSheet;

    public enum Size
    {
        LARGE, MEDIUM, SMALL
    }

    /*
     * Constructor, make new asteroid
     */
    public AsteroidSprite( double x, double y, double xv, double yv, Size s )
    {
        xPos = x;
        yPos = y;
        xVel = xv;
        yVel = yv;
        size = s;
        try
        {
            switch ( s )
            {
                case LARGE:
                    asteroidTileSheet = new TileSheet( this, "img/asteroidLarge.png", 174, 174 );
                    break;
                case MEDIUM:
                    asteroidTileSheet = new TileSheet( this, "img/asteroidMedium.png", 64, 64 );
                    break;
                case SMALL:
                    asteroidTileSheet = new TileSheet( this, "img/asteroidSmall.png", 32, 32 );
                    break;
            }
        }
        catch ( IOException ioe )
        {

        }
        asteroidShape = new Rectangle( ( int ) xPos, ( int ) yPos, asteroidTileSheet.getTileWidth(),
                asteroidTileSheet.getTileHeight() );
        nextTimeUpdate = System.currentTimeMillis();
        row = 0;
        column = 0;
        displayBounds = GameDisplay.getBounds();
    }

    @Override
    public void checkCollision( Sprite obj )
    {
        if ( obj instanceof PlayerSprite )
        {
            if ( asteroidShape.intersects( obj.getBounds() ) )
            {
                /**
                 * Dispatch an event to remove the asteroid
                 */
                GameEventDispatcher.dispatchEvent( new GameEvent( this, GameEventType.Remove, this ) );
            }
        }

    }

    @Override
    public void draw( Graphics2D g )
    {
        asteroidShape.x = ( int ) xPos;
        asteroidShape.y = ( int ) yPos;
        AffineTransform transform = AffineTransform.getTranslateInstance( xPos, yPos );
        g.drawImage( asteroidTileSheet.getTile( row, column ), transform, null );

    }

    @Override
    public Rectangle getBounds()
    {
        return asteroidShape;
    }

    @Override
    public void update()
    {
        if ( xVel * xVel + yVel * yVel < 1 )
        {
            xVel += Math.random() * 2 - 1;
            yVel += Math.random() * 2 - 1;
        }
        xPos += xVel;
        yPos += yVel;

        if ( xPos > displayBounds.width + asteroidShape.width )
            xPos = 0 - asteroidShape.width;

        else if ( xPos < 0 - asteroidShape.width )
            xPos = displayBounds.width + asteroidShape.width;

        // wrap around in Y axis
        if ( yPos < 0 - asteroidShape.height )
            yPos = displayBounds.height + asteroidShape.height;

        else if ( yPos > displayBounds.height + asteroidShape.height )
            yPos = 0 - asteroidShape.height;

        if ( nextTimeUpdate < System.currentTimeMillis() )
        {
            column++;
            if ( column == 4 )
            {
                row++;
                column = 0;
            }
            row %= 4;
            nextTimeUpdate = System.currentTimeMillis() + 50;
        }
    }

    @Override
    public void keyboardEvent( KeyEvent ke )
    {
        // Nothing

    }

    @Override
    public void mouseEvent( MouseEvent me )
    {
        // Nothing

    }

    public Size getSize()
    {
        return size;
    }

    public Point getVel()
    {
        return new Point( ( int ) xVel, ( int ) yVel );
    }

    public Point getPos()
    {
        return new Point( ( int ) xPos /* + asteroidShape.x / 2 */, ( int ) yPos /*
                                                                                  * +
                                                                                  * asteroidShape
                                                                                  * .
                                                                                  * y
                                                                                  * /
                                                                                  * 2
                                                                                  */);
    }
}

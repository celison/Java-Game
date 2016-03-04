package missionToTitan;

import java.awt.Graphics2D;
import java.awt.Rectangle;
import java.awt.event.KeyEvent;
import java.awt.event.MouseEvent;
import java.awt.geom.AffineTransform;
import java.io.IOException;

import javax.sound.sampled.LineUnavailableException;
import javax.sound.sampled.UnsupportedAudioFileException;

import libs.AudioSample;
import libs.GameDisplay;
import libs.GameEngine;
import libs.GameEvent;
import libs.GameEvent.GameEventType;
import libs.GameEventDispatcher;
import libs.Sprite;
import libs.TileSheet;

/**
 * Player missile sprite. This sprite is created by the player sprite
 * 
 * @author williamhooper
 * 
 */
public class PlayerMissile implements Sprite
{

    private double xPos;
    private double yPos;
    private double lastX;
    private double lastY;
    private double width;
    private double heigth;
    private double yVel;
    private double xVel;
    private long updateTime;
    private int row, column;
    private Rectangle missileShape;
    private Rectangle display;
    private static TileSheet playerMissileSprite;
    private static TileSheet explosion;
    private static AudioSample playerHit;
    private State state;

    private enum State
    {
        MOVING, TARGET_HIT;
    }

    /**
     * Constructor
     * 
     */
    public PlayerMissile( double x, double y, double xv, double yv )
    {
        xPos = x;
        yPos = y;

        xVel = xv;
        yVel = yv;

        row = 0;
        column = 0;
        state = State.MOVING;

        if ( playerMissileSprite == null )
        {
            loadBufferedImage( this );
        }

        if ( playerHit == null )
        {
            loadAudioSample( this );
        }
        display = GameDisplay.getBounds();
        width = playerMissileSprite.getTileWidth();
        heigth = playerMissileSprite.getTileHeight();
        missileShape = new Rectangle( ( int ) xPos, ( int ) yPos, ( int ) width, ( int ) heigth );
        updateTime = System.currentTimeMillis();
    }

    @Override
    public void checkCollision( Sprite obj )
    {
        /**
         * Check to see if we hit an enemy
         */
        if ( state == State.MOVING )
            if ( obj instanceof EnemySprite || obj instanceof AsteroidSprite )
            {
                if ( missileShape.intersects( obj.getBounds() ) )
                {
                    playerHit.play();

                    /**
                     * Dispatch an event to remove the enemy
                     */
                    if ( obj instanceof AsteroidSprite )
                        GameEventDispatcher.dispatchEvent( new GameEvent( this, GameEventType.Remove, obj ) );
                    else if ( obj instanceof EnemySprite )
                        ( ( EnemySprite ) obj ).takeDamage( 50 );

                    /**
                     * Done with the player missile
                     */
                    row = 0;
                    column = 0;
                    state = State.TARGET_HIT;
                    xPos = xPos + playerMissileSprite.getTileWidth() / 2 - explosion.getTileWidth() / 2;
                    yPos = yPos + playerMissileSprite.getTileHeight() / 2 - explosion.getTileHeight() / 2;
                    /**
                     * Dispatch an event to update the score
                     */
                    GameEventDispatcher.dispatchEvent( new GameEvent( this, GameEventType.Score, new Integer( 10 ) ) );
                }
            }
    }

    @Override
    public void draw( Graphics2D g )
    {
        missileShape.x = ( int ) xPos;
        missileShape.y = ( int ) yPos;
        if ( state == State.MOVING )
        {
            double angle = Math.atan2( yPos - lastY, xPos - lastX );
            AffineTransform transform = AffineTransform.getTranslateInstance( xPos, yPos );
            transform.concatenate( AffineTransform.getRotateInstance( angle - Math.PI / 2, ( missileShape.getWidth() / 2 ),
                    ( missileShape.getHeight() / 2 ) ) );
            g.drawImage( playerMissileSprite.getTile( row, column ), transform, null );
        }
        else if ( state == State.TARGET_HIT )
            try
            {
                g.drawImage( explosion.getTile( row, column ), null, ( int ) xPos, ( int ) yPos );
            }
            catch ( Exception e )
            {
                e.printStackTrace();
            }
    }

    @Override
    public void update()
    {
        if ( yPos + heigth <= 0 || yPos >= display.height || xPos + width <= 0 || xPos >= display.width )
        {
            /**
             * The missile went off the screen so remove it
             */

            GameEventDispatcher.dispatchEvent( new GameEvent( this, GameEventType.Remove, this ) );
        }
        if ( state == State.MOVING )
        {
            lastY = yPos;
            yPos += yVel;
            lastX = xPos;
            xPos += xVel;
            if ( updateTime < System.currentTimeMillis() )
            {
                updateTime += 1000 / 30;

                column++;
                if ( column == playerMissileSprite.getNumberColumns() )
                {
                    column = 0;
                    row++;
                }
                row %= playerMissileSprite.getNumberRows();
            }
        }
        else if ( state == State.TARGET_HIT && updateTime < System.currentTimeMillis() )
        {
            updateTime += 125;
            if ( ++column == explosion.getNumberColumns() )
            {
                column = 0;
                row++;
            }
            if ( row == explosion.getNumberRows() )
                GameEventDispatcher.dispatchEvent( new GameEvent( this, GameEventType.Remove, this ) );
        }
    }

    /**
     * @param xPos
     *            the xPos to set
     */
    public void setxPos( double xPos )
    {
        this.xPos = xPos;
    }

    /**
     * @param yPos
     *            the yPos to set
     */
    public void setyPos( double yPos )
    {
        this.yPos = yPos;
    }

    @Override
    public Rectangle getBounds()
    {
        return missileShape.getBounds();
    }

    @Override
    public void keyboardEvent( KeyEvent ke )
    {
        // TODO Auto-generated method stub

    }

    @Override
    public void mouseEvent( MouseEvent me )
    {
        // TODO Auto-generated method stub

    }

    private static void loadBufferedImage( PlayerMissile pm )
    {
        try
        {
            playerMissileSprite = new TileSheet( pm, "img/playerMissile.png", 64, 64 );
            explosion = new TileSheet( pm, "img/explosion.png", 256, 256 );
        }
        catch ( IOException ioe )
        {
            GameEngine.stop();
        }
    }

    private static void loadAudioSample( PlayerMissile pm )
    {
        try
        {
            playerHit = new AudioSample( pm, "sound/playerHit.wav" );
        }
        catch ( IOException ioe )
        {
            GameEngine.stop();
        }
        catch ( UnsupportedAudioFileException uafe )
        {
            GameEngine.stop();
        }
        catch ( LineUnavailableException lue )
        {
            GameEngine.stop();
        }
    }
}

package missionToTitan;

import java.awt.Color;
import java.awt.Font;
import java.awt.Graphics2D;
import java.awt.Point;
import java.awt.Rectangle;
import java.awt.event.KeyEvent;
import java.awt.event.MouseEvent;
import java.awt.geom.AffineTransform;
import java.awt.image.BufferedImage;
import java.io.IOException;

import javax.sound.sampled.LineUnavailableException;
import javax.sound.sampled.UnsupportedAudioFileException;

import libs.AudioSample;
import libs.AudioSample.AudioSampleState;
import libs.GameDisplay;
import libs.GameEngine;
import libs.GameEvent;
import libs.GameEvent.GameEventType;
import libs.GameEventDispatcher;
import libs.ImageUtil;
import libs.Sprite;

/**
 * The player sprite. This is the sprite the player controlls
 * 
 * @author williamhooper
 * 
 */
public class PlayerSprite implements Sprite
{
    private double xPos;
    private double yPos;
    private double width;
    private double heigth;

    private double heading;
    private double omega; // angular velocity in radians per frame ( 30 fps )

    private double xVelocity;
    private double yVelocity;// velocity in pixels / frame ( 30 fps )

    private int fuel, maxFuel;
    private int shield, maxShield;
    private Rectangle playerShape;
    private Rectangle displayBounds;
    private long missileTime;

    private static BufferedImage playerBufferedImage;
    private static AudioSample playerMissileFire;
    private static AudioSample playerHit;

    /**
     * Constructor
     * 
     */
    public PlayerSprite( )
    {
        displayBounds = GameDisplay.getBounds();

        if ( playerBufferedImage == null )
        {
            try
            {
                playerBufferedImage = ImageUtil.loadBufferedImage( this, "img/player.png" );
            }
            catch ( IOException ioe )
            {

            }
        }
        if ( playerMissileFire == null )
        {
            loadAudioSample( this );
        }

        xPos = displayBounds.getWidth() / 2;
        yPos = displayBounds.getHeight() - 500;

        shield = 100;
        maxShield = 100;

        fuel = 100;
        maxFuel = 100;

        width = playerBufferedImage.getWidth();
        heigth = playerBufferedImage.getHeight();

        xVelocity = 0;
        yVelocity = 0;

        omega = 0;
        heading = 0;

        playerShape = new Rectangle( ( int ) xPos, ( int ) yPos, ( int ) width, ( int ) heigth );

        missileTime = System.currentTimeMillis() + 250;
    }

    @Override
    public void checkCollision( Sprite obj )
    {
        if ( playerShape.intersects( obj.getBounds() ) )
        {
            if ( obj instanceof EnemySprite )
            {
                /**
                 * Dispatch an event to remove the enemy
                 */
                GameEventDispatcher.dispatchEvent( new GameEvent( this, GameEventType.Remove, obj ) );
                takeDamage( 50 );
            }
            else if ( obj instanceof AsteroidSprite )
            {
                switch ( ( ( AsteroidSprite ) obj ).getSize() )
                {
                    case LARGE:
                        takeDamage( 30 );
                        break;
                    case MEDIUM:
                        takeDamage( 20 );
                        break;
                    case SMALL:
                        takeDamage( 10 );
                        break;
                }
            }
        }

    }

    @Override
    public void draw( Graphics2D g )
    {
        Font f = new Font( "Times New Roman", Font.BOLD, 24 );

        playerShape.x = ( int ) xPos;
        playerShape.y = ( int ) yPos;
        g.setFont( f );
        g.setColor( Color.green );
        g.drawString( "Fuel Level:", displayBounds.width - 300, 20 );
        g.drawRect( displayBounds.width - 150, 10, 100, 10 );
        g.fillRect( displayBounds.width - 150, 10, ( int ) ( 100 * fuel / maxFuel ), 10 );
        g.setColor( Color.blue );
        g.drawString( "Shield Level:", displayBounds.width - 600, 20 );
        g.drawRect( displayBounds.width - 450, 10, 100, 10 );
        g.fillRect( displayBounds.width - 450, 10, ( int ) ( 100 * shield / maxShield ), 10 );
        AffineTransform transform = AffineTransform.getTranslateInstance( xPos, yPos );
        transform.concatenate( AffineTransform.getRotateInstance( heading, ( playerShape.getWidth() / 2 ),
                ( playerShape.getHeight() / 2 ) ) );
        g.drawImage( playerBufferedImage, transform, null );

    }

    @Override
    public void mouseEvent( MouseEvent me )
    {

    }

    @Override
    public void update()
    {
        xPos += xVelocity;
        yPos += yVelocity;
        heading = ( heading + omega );

        // wrap around in X axis
        if ( xPos > displayBounds.width + playerShape.width )
            xPos -= displayBounds.width + playerShape.width;

        else if ( xPos < 0 - playerShape.width )
            xPos += displayBounds.width + playerShape.width;

        // wrap around in Y axis
        if ( yPos < 0 - playerShape.height )
            yPos += displayBounds.height + playerShape.height;

        else if ( yPos > displayBounds.height + playerShape.height )
            yPos -= displayBounds.height + playerShape.height;
    }

    @Override
    public Rectangle getBounds()
    {
        return playerShape.getBounds();
    }

    @Override
    public void keyboardEvent( KeyEvent ke )
    {
        if ( ke.getID() == KeyEvent.KEY_PRESSED )
        {
            switch ( ke.getKeyCode() )
            {
                case KeyEvent.VK_A:
                    omega -= .01;
                    break;

                case KeyEvent.VK_D:
                    omega += .01;
                    break;

                case KeyEvent.VK_W:
                    addVelocity();
                    break;

                case KeyEvent.VK_SPACE:
                    if ( missileTime < System.currentTimeMillis() )
                    {
                        PlayerMissile missile = new PlayerMissile( xPos + playerShape.width / 2, yPos + playerShape.height / 2,
                                xVelocity + ( 5 * Math.sin( heading ) ), yVelocity - ( 5 * Math.cos( heading ) ) );
                        GameEventDispatcher.dispatchEvent( new GameEvent( this, GameEventType.AddLast, missile ) );
                        if ( playerMissileFire.getState() == AudioSampleState.DONE )
                            playerMissileFire.play();
                        missileTime = System.currentTimeMillis() + 250;

                    }
                    break;
            }
        }

    }

    /*
     * private static void loadTileSheet( PlayerSprite ps ) { try {
     * playerTileSheet = new TileSheet( ps, "img/player.png", 29, 64 ); } catch
     * ( IOException ioe ) { GameEngine.stop(); } }
     */

    public Point getPos()
    {
        return new Point( ( int ) xPos, ( int ) yPos );
    }

    private static void loadAudioSample( PlayerSprite ps )
    {
        try
        {
            playerMissileFire = new AudioSample( ps, "sound/playerFire.wav" );
            playerHit = new AudioSample( ps, "sound/playerHit.wav");
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

    private void addVelocity()
    {
        if ( fuel <= 0 )
            return;
        xVelocity += Math.sin( heading );
        yVelocity -= Math.cos( heading );
        fuel -= 1;
    }

    private void takeDamage( int damage )
    {
        playerHit.play();
        shield -= damage;
        if ( shield < 0 ) // if shield is 0, destroy sprite
        {
            shield = 0;
            GameEventDispatcher.dispatchEvent( new GameEvent( this, GameEventType.Remove, this ) );
        }
    }

    public void reset()
    {
        xVelocity = 0;
        yVelocity = 0;
        omega = 0;
        fuel = maxFuel;
        shield = maxShield;
        xPos = displayBounds.getWidth() / 2;
        yPos = displayBounds.getHeight() - 500;
    }
}

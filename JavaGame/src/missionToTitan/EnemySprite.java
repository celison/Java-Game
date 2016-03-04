package missionToTitan;

import java.awt.Graphics2D;
import java.awt.Point;
import java.awt.Rectangle;
import java.awt.event.KeyEvent;
import java.awt.event.MouseEvent;
import java.awt.geom.AffineTransform;
import java.awt.image.BufferedImage;
import java.io.IOException;

import libs.GameEngine;
import libs.GameEvent;
import libs.GameEvent.GameEventType;
import libs.GameEventDispatcher;
import libs.ImageUtil;
import libs.Sprite;

/**
 * Enemy sprite
 * 
 * This sprite follows path provided in the constuctor. When the sprite has
 * completed the path it removes itself from the game
 * 
 * @author williamhooper $Id: EnemySprite.java,v 1.4 2011/08/29 00:22:43
 *         williamhooper Exp $
 */
public class EnemySprite implements Sprite
{
    private double xPos;
    private double yPos;
    private double lastX;

    private double maxVel;
    private double xVel;
    private double yVel;
    private long nextUpdate;
    private Rectangle enemyShape;
    private int shield;
    private double angle;
    private PlayerSprite player;
    private Point playerPos;
    private static BufferedImage enemySprite;

    /**
     * Constructor
     * 
     * @param ps
     */
    public EnemySprite( PlayerSprite ps, int x, int y, int vel )
    {
        if ( enemySprite == null )
        {
            loadBufferedImage( this );
        }
        xPos = x;
        yPos = y;
        maxVel = vel;

        enemyShape = new Rectangle( ( int ) xPos, ( int ) yPos, enemySprite.getWidth(), enemySprite.getHeight() );
        player = ps;
        angle = 0;
        shield = 100;
        nextUpdate = System.currentTimeMillis();
    }

    @Override
    public void draw( Graphics2D g )
    {
        enemyShape.x = ( int ) xPos;
        enemyShape.y = ( int ) yPos;
        AffineTransform transform = AffineTransform.getTranslateInstance( xPos, yPos );
        transform.concatenate( AffineTransform.getRotateInstance( angle + Math.PI / 2, ( enemyShape.getWidth() / 2 ),
                ( enemyShape.getHeight() / 2 ) ) );
        if ( xPos < lastX )
        {
            transform.concatenate( AffineTransform.getRotateInstance( Math.PI, ( enemyShape.getWidth() / 2 ),
                    ( enemyShape.getHeight() / 2 ) ) );
        }
        g.drawImage( enemySprite, transform, null );
    }

    @Override
    public void update()
    {
        playerPos = player.getPos();
        lastX = xPos;
        xPos += xVel;
        yPos += yVel;
        angle = Math.tan( ( yPos - playerPos.y ) / ( xPos - playerPos.x ) );
        if ( nextUpdate < System.currentTimeMillis() )
        {
            if ( playerPos.x == xPos )
                xVel = 0;
            else if ( playerPos.x > xPos )
                xVel = maxVel;
            else if ( playerPos.x < xPos )
                xVel = 0 - maxVel;

            if ( playerPos.y == yPos )
                yVel = 0;
            else if ( playerPos.y > yPos )
                yVel = maxVel;
            else if ( playerPos.y < yPos )
                yVel = 0 - maxVel;
        }

    }

    @Override
    public void checkCollision( Sprite obj )
    {

    }

    @Override
    public Rectangle getBounds()
    {
        return enemyShape.getBounds();
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

    public void takeDamage( int damage )
    {
        shield -= damage;
        if ( shield < 0 ) // if shield is 0, destroy sprite
        {
            shield = 0;
            GameEventDispatcher.dispatchEvent( new GameEvent( this, GameEventType.Remove, this ) );
        }
    }

    private static void loadBufferedImage( EnemySprite es )
    {
        try
        {
            enemySprite = ImageUtil.loadBufferedImage( es, "img/enemy.png" );
        }
        catch ( IOException ioe )
        {
            GameEngine.stop();
        }
    }

}
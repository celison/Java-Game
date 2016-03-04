package missionToTitan;

import java.awt.Graphics2D;
import java.awt.Rectangle;
import java.awt.event.KeyEvent;
import java.awt.event.MouseEvent;
import java.awt.image.BufferedImage;
import java.io.IOException;

import libs.GameEngine;
import libs.ImageUtil;
import libs.Sprite;

public class SplashSprite implements Sprite
{
    private BufferedImage splashImage;
    
    public SplashSprite( String filename)
    {
        try
        {
            splashImage = ImageUtil.loadBufferedImage( this, filename );
        }
        catch ( IOException ioe )
        {
            GameEngine.stop();
        }
    }

    @Override
    public void checkCollision( Sprite obj )
    {
        // TODO Auto-generated method stub

    }

    @Override
    public void draw( Graphics2D g )
    {
        g.drawImage( splashImage, null, 0, 0 );

    }

    @Override
    public Rectangle getBounds()
    {
        // TODO Auto-generated method stub
        return null;
    }

    @Override
    public void update()
    {
        // TODO Auto-generated method stub

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

}

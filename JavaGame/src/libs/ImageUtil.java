package libs;

import java.awt.Graphics2D;
import java.awt.GraphicsConfiguration;
import java.awt.GraphicsEnvironment;
import java.awt.image.BufferedImage;
import java.awt.image.VolatileImage;
import java.io.IOException;

import javax.imageio.ImageIO;

/**
 * Class to load an image from the file system
 * 
 * @author williamhooper
 */

public class ImageUtil
{

    /**
     * Create a buffered image compatible with the current display. The buffered
     * image will be empty.
     * 
     * @param width
     * @param height
     * @param transparancy
     * @return BufferedImage
     */
    public static BufferedImage createBufferedImage( int width, int height, int transparancy )
    {
        GraphicsEnvironment ge = GraphicsEnvironment.getLocalGraphicsEnvironment();
        GraphicsConfiguration gc = ge.getDefaultScreenDevice().getDefaultConfiguration();
        return gc.createCompatibleImage( width, height, transparancy );
    }

    /**
     * Create a volatile image compatible with the current display. The volatile
     * image will be empty.
     * 
     * @param width
     * @param height
     * @param transparency
     * @return VolatileImage
     */
    public static VolatileImage createVolatileImage( int width, int height, int transparency )
    {
        VolatileImage image = null;

        GraphicsEnvironment ge = GraphicsEnvironment.getLocalGraphicsEnvironment();
        GraphicsConfiguration gc = ge.getDefaultScreenDevice().getDefaultConfiguration();

        image = gc.createCompatibleVolatileImage( width, height, transparency );

        if ( image.validate( gc ) == VolatileImage.IMAGE_INCOMPATIBLE )
        {
            throw new RuntimeException( "Volatile images not supported" );
        }

        return image;
    }

    /**
     * Return true if the image has an alpha channel
     * 
     * @param image
     * @return boolean
     */
    public static boolean hasAlpha( BufferedImage image )
    {
        return image.getColorModel().hasAlpha();
    }

    /**
     * Load a buffered image from an existing image file
     * 
     * @param obj
     * @param path
     * @return Image
     * @throws IOException
     */
    public static BufferedImage loadBufferedImage( Object obj, String path ) throws IOException
    {
        java.net.URL imgURL = obj.getClass().getResource( path );
        if ( imgURL != null )
        {
            return ImageIO.read( imgURL );
        }
        else
        {
            throw new IOException( "Unable to find file: " + path );
        }
    }

    /**
     * Convert a buffered image into a volatile image
     * 
     * @param image
     * @return VolatileImage
     */
    public static VolatileImage toVolatileImage( BufferedImage image )
    {
        VolatileImage vimage = createVolatileImage( image.getWidth(), image.getHeight(), image.getTransparency() );
        Graphics2D g = vimage.createGraphics();
        g.drawImage( image, 0, 0, null );
        g.dispose();
        return vimage;
    }
}

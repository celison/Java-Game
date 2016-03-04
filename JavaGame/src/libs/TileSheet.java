package libs;

import java.awt.image.BufferedImage;
import java.io.IOException;

/**
 * Class to manage tile sheets
 * 
 * @author williamhooper
 */
public class TileSheet
{
    private BufferedImage image;

    private int tileHeight;
    private int tileWidth;

    /**
     * Constructor, create a tile sheet from an existing buffered image
     * 
     * @param image
     * @param tileWidth
     * @param tileHeight
     */
    public TileSheet( BufferedImage image, int tileWidth, int tileHeight )
    {
        this.tileWidth = tileWidth;
        this.tileHeight = tileHeight;

        this.image = image;
        if ( ( image.getHeight() % tileHeight ) != 0 || ( image.getWidth() % tileWidth ) != 0 )
        {
            throw new RuntimeException( "Image dimensions do not match the tile width and height" );
        }
    }

    /**
     * Constructor, create a tile sheet from an existing image file
     * 
     * @param obj
     * @param file
     * @param tileWidth
     * @param tileHeight
     * @throws IOException
     */
    public TileSheet( Object obj, String file, int tileWidth, int tileHeight ) throws IOException
    {
        this.tileWidth = tileWidth;
        this.tileHeight = tileHeight;

        image = ImageUtil.loadBufferedImage( obj, file );
        if ( ( image.getHeight() % tileHeight ) != 0 || ( image.getWidth() % tileWidth ) != 0 )
        {
            throw new RuntimeException( "Image dimensions do not match the tile width and height" );
        }
    }

    /**
     * Return the number columns of tiles
     * 
     * @return int
     */
    public int getNumberColumns()
    {
        return ( image.getWidth() / tileWidth );
    }

    /**
     * Return the number rows of tiles
     * 
     * @return int
     */
    public int getNumberRows()
    {
        return ( image.getHeight() / tileHeight );
    }

    /**
     * Get a tile image
     * 
     * @param row
     * @param column
     * @return BufferedImage
     */
    public BufferedImage getTile( int row, int column )
    {
        return image.getSubimage( ( column * tileWidth ), ( row * tileHeight ), tileWidth, tileHeight );
    }

    /**
     * Return the height of a tile
     * 
     * @return the tileHeight
     */
    public int getTileHeight()
    {
        return tileHeight;
    }

    /**
     * Return the width of a tile
     * 
     * @return the tileWidth
     */
    public int getTileWidth()
    {
        return tileWidth;
    }
}

package libs;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.URI;
import java.net.URISyntaxException;

/**
 * File utilities for reading and writing a game map. The game map is in the
 * format of...
 * 
 * 
 * @author williamhooper
 */
public class MapFile
{
    /**
     * Read the game map into a byte array. This method works on a local game
     * map file and a game map file in a JAR
     * 
     * @param obj
     * @param fileName
     * @return byte [ ][ ]
     */
    public static short [ ][ ] readMap( Object obj, String fileName )
    {
        int rows;
        int columns;
        String line;
        String [ ] pieces;

        InputStream in = obj.getClass().getResourceAsStream( fileName );
        if ( in == null )
        {
            return null;
        }

        try
        {
            BufferedReader dataInput = new BufferedReader( new InputStreamReader( in ) );

            /**
             * First line of the file contains the number of rows and columns
             */
            line = dataInput.readLine();
            pieces = line.split( "," );
            rows = Integer.parseInt( pieces[ 0 ].trim() );
            columns = Integer.parseInt( pieces[ 1 ].trim() );

            short map[][] = new short [ rows ] [ columns ];

            for ( int row = 0; row < map.length; row++ )
            {
                line = dataInput.readLine();
                pieces = line.split( "," );
                for ( int index = 0; index < columns; index++ )
                {
                    map[ row ][ index ] = Short.parseShort( pieces[ index ].trim() );
                }
            }

            return map;
        }
        catch ( IOException e )
        {
            System.out.println( e.getMessage() );
            return null;
        }
    }

    /**
     * Write the game map to a file. This method will not write to a file inside
     * a jar
     * 
     * @param obj
     * @param fileName
     * @param map
     */
    public static void writeMap( Object obj, String fileName, short map[][] )
    {
        try
        {
            URI fileURI = obj.getClass().getResource( fileName ).toURI();
            try
            {
                BufferedWriter out = new BufferedWriter( new FileWriter( fileURI.getPath() ) );

                out.write( map.length + "," + map[ 0 ].length + "\n" );
                for ( int row = 0; row < map.length; row++ )
                {
                    for ( int index = 0; index < map[ 0 ].length; index++ )
                    {
                        out.write( Short.toString( map[ row ][ index ] ) );
                        if ( index < ( map[ 0 ].length - 1 ) )
                        {
                            out.write( "," );
                        }
                    }
                    out.write( "\n" );
                }
                out.close();
            }
            catch ( IOException e )
            {
                // TODO Auto-generated catch block
                e.printStackTrace();
            }
        }
        catch ( URISyntaxException e1 )
        {
            // TODO Auto-generated catch block
            e1.printStackTrace();
        }

        return;
    }
}
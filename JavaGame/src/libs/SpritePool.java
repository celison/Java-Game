package libs;

import java.util.LinkedList;

/**
 * Sprite pool class for managing sprite object pools
 * 
 * @author williamhooper $Id: SpritePool.java,v 1.3 2011/07/07 05:57:09
 *         williamhooper Exp $
 * 
 *         Copyright 2008 William Hooper
 * 
 *         This library is free software: you can redistribute it and/or modify
 *         it under the terms of the GNU General Public License as published by
 *         the Free Software Foundation, either version 3 of the License, or (at
 *         your option) any later version.
 * 
 *         This library is distributed in the hope that it will be useful, but
 *         WITHOUT ANY WARRANTY; without even the implied warranty of
 *         MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU
 *         General Public License for more details.
 * 
 *         You should have received a copy of the GNU General Public License. If
 *         not, see <http://www.gnu.org/licenses/>.
 */
public abstract class SpritePool
{
    private int poolSize;
    private boolean poolWait;
    private LinkedList< SpritePoolObject > spritePoolInList;
    private LinkedList< SpritePoolObject > spritePoolOutList;

    /**
     * Constructor
     * 
     */
    public SpritePool( )
    {
        spritePoolOutList = new LinkedList< SpritePoolObject >();
        spritePoolInList = new LinkedList< SpritePoolObject >();

        poolSize = 5;
        poolWait = false;
    }

    /**
     * Constructor
     * 
     */
    public SpritePool( int size )
    {
        spritePoolOutList = new LinkedList< SpritePoolObject >();
        spritePoolInList = new LinkedList< SpritePoolObject >();

        poolSize = size;
        poolWait = false;
    }

    /**
     * Check an object back into the pool
     * 
     * @param obj
     */
    public void checkIn( SpritePoolObject obj )
    {
        spritePoolOutList.remove( obj );
        spritePoolInList.add( obj );
        if ( poolWait )
        {
            spritePoolInList.notify();
        }
    }

    /**
     * Check an object from out of the pool
     * 
     * @return SpritePoolObject
     */
    public SpritePoolObject checkOut()
    {
        if ( ( spritePoolOutList.size() + spritePoolInList.size() ) == poolSize )
        {
            if ( spritePoolInList.size() != 0 )
            {
                /**
                 * Move a pool object from the in list to the out list and
                 * return that object
                 */
                SpritePoolObject obj = spritePoolInList.remove();
                spritePoolOutList.add( obj );
                obj.setParentPool( this );
                return obj;
            }
            else if ( poolWait )
            {
                /**
                 * Wait for a pool object to show up
                 */
                synchronized ( spritePoolInList )
                {
                    try
                    {
                        spritePoolInList.wait();
                    }
                    catch ( InterruptedException exception )
                    {}
                    SpritePoolObject obj = spritePoolInList.remove();
                    spritePoolOutList.add( obj );
                    obj.setParentPool( this );
                    return obj;
                }
            }
            else
            {
                /**
                 * Since we do not have any available sprite objects and we are
                 * not waiting return null
                 */
                return null;
            }
        }
        else if ( ( spritePoolOutList.size() + spritePoolInList.size() ) < poolSize )
        {
            /**
             * Create a sprite pool object, set the parent pool and return
             */
            SpritePoolObject obj = create();
            spritePoolOutList.add( obj );
            obj.setParentPool( this );
            System.out.println( "created missile " + poolSize + " in " + spritePoolInList.size() + " out "
                    + spritePoolOutList.size() );
            return obj;
        }
        return null;
    }

    /**
     * @return the poolSize
     */
    public int getPoolSize()
    {
        return poolSize;
    }

    /**
     * @return the poolWait
     */
    public boolean isPoolWait()
    {
        return poolWait;
    }

    /**
     * @param poolSize
     *            the poolSize to set
     */
    public void setPoolSize( int poolSize )
    {
        this.poolSize = poolSize;
    }

    /**
     * @param poolWait
     *            the poolWait to set
     */
    public void setPoolWait( boolean poolWait )
    {
        this.poolWait = poolWait;
    }

    /**
     * Abstract class to create a pool object
     * 
     * @return
     */
    protected abstract SpritePoolObject create();

}

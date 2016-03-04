package libs;

/**
 * Abstract class for a sprite pool object
 * 
 * @author williamhooper
 * 
 */
public abstract class SpritePoolObject
{
    private SpritePool parentPool = null;

    /**
     * Check this object back into it's parent pool
     * 
     */
    final public void checkIn()
    {
        if ( parentPool != null )
        {
            parentPool.checkIn( this );
        }
        else
        {
            throw new RuntimeException( "parent pool not initialized" );
        }
    }

    /**
     * @return the parentPool
     */
    final public SpritePool getParentPool()
    {
        return parentPool;
    }

    /**
     * @param parentPool
     *            the parentPool to set
     */
    final public void setParentPool( SpritePool parentPool )
    {
        this.parentPool = parentPool;
    }
}

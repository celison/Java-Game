package libs;

/**
 * Game event class. This class is used to pass async messages back to the main
 * thread.
 * 
 * @author williamhooper
 */

public class GameEvent
{
    /**
     * Game event types
     * 
     * @author williamhooper
     * 
     */
    public enum GameEventType
    {
        AddFirst, AddLast, End, Help, Life, Menu, NextLevel, Quit, Remove, Score, Start, Pause
    };

    private Object attachment;
    private Object source;
    private GameEventType type;

    /**
     * Constructor
     * 
     * @param source
     * @param type
     * @param attachment
     */
    public GameEvent( Object source, GameEventType type, Object attachment )
    {
        this.source = source;
        this.type = type;
        this.attachment = attachment;
    }

    /**
     * Get the event attachment
     * 
     * @return Object
     */
    public Object getAttachment()
    {
        return attachment;
    }

    /**
     * Get the source of the game event
     * 
     * @return Object
     */
    public Object getSource()
    {
        return source;
    }

    /**
     * Get the event type
     * 
     * @return GameEventType
     */
    public GameEventType getType()
    {
        return type;
    }
}

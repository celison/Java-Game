package missionToTitan;

import java.awt.Graphics2D;
import java.awt.Point;
import java.awt.Rectangle;
import java.awt.event.KeyEvent;
import java.awt.event.MouseEvent;
import java.awt.image.BufferedImage;
import java.io.IOException;
import java.util.LinkedList;

import javax.sound.midi.InvalidMidiDataException;
import javax.sound.midi.MidiUnavailableException;

import libs.AudioMidi;
import libs.Game;
import libs.GameDisplay;
import libs.GameEngine;
import libs.GameEvent;
import libs.GameEvent.GameEventType;
import libs.GameEventDispatcher;
import libs.ImageUtil;
import libs.KeyboardEventListener;
import libs.MouseEventListener;
import libs.Sprite;
import missionToTitan.AsteroidSprite.Size;

/**
 * Mission to Titan
 * 
 * @author Connor Elison
 */
public class MissionToTitan implements Game, MouseEventListener, KeyboardEventListener
{
    enum GameState
    {
        SPLASH, PLAYING, PAUSED, GAME_OVER, END_OF_LEVEL, HELP;
    }

    /**
     * Our list of sprites
     */
    private LinkedList< Sprite > spriteList;

    /**
     * Paths for the enemys
     */

    private long nextTime;
    private long enemyCount;
    private int level;
    private ScoreSprite scoreSprite;
    private SplashSprite splashSprite;
    private SplashSprite gameOverSprite;
    private SplashSprite nextLevelSprite;
    private SplashSprite winSprite;
    private SplashSprite helpSprite;

    private Rectangle display;

    private BufferedImage background;
    private AudioMidi backgroundMusic;

    private GameState currentGameState;

    private PlayerSprite playerSprite;
    private long maxEnemyCount;
    private long NextEnemyTime;

    private static final int FINAL_LEVEL = 3;

    /**
     * Main
     * 
     * @param args
     */
    public static void main( String [ ] args )
    {
        MissionToTitan game = new MissionToTitan();

        GameEngine.start( game );

        /**
         * If we are here we have stopped the game engine
         */
        GameDisplay.dispose();
    }

    /**
     * Constructor
     */
    public MissionToTitan( )
    {
        /**
         * Create our game display
         */
        GameDisplay.create( 1280, 720 );

        /**
         * Create our list of sprites
         */
        spriteList = new LinkedList< Sprite >();

        currentGameState = GameState.SPLASH;

        /**
         * Add a mouse listener so we can get mouse events
         */
        GameDisplay.addMouseListener( this );

        /**
         * Add a keyboard listener so we can get key presses
         */
        GameDisplay.addKeyboardListener( this );
        GameDisplay.captureCursor( false );

        /**
         * Setup some variables for managing the enemies
         */
        nextTime = System.currentTimeMillis() + NextEnemyTime;

        /**
         * Set up initial sprites.
         */

        scoreSprite = new ScoreSprite();
        splashSprite = new SplashSprite( "img/splash.png" ); // add help button
        gameOverSprite = new SplashSprite( "img/restart.png" );
        nextLevelSprite = new SplashSprite( "img/levelComplete.png" );
        winSprite = new SplashSprite( "img/missionComplete.png" ); // makeme
        helpSprite = new SplashSprite( "img/help.png" ); // makeme

        playerSprite = new PlayerSprite();
        nextTime = System.currentTimeMillis();
        spriteList.add( splashSprite );
        display = GameDisplay.getBounds();
        level = 0;
        currentGameState = GameState.SPLASH;
    }

    @Override
    public void collisions()
    {
        /**
         * Check collisions on the Sprite objects
         */

        synchronized ( spriteList )
        {
            for ( Sprite spriteObj : spriteList )
            {
                for ( Sprite otherSprite : spriteList )
                {
                    if ( !otherSprite.equals( spriteObj ) )
                    {
                        spriteObj.checkCollision( otherSprite );
                    }
                }
            }
        }

    }

    @Override
    public void draw()
    {
        /**
         * Update the graphics we drew on
         */
        GameDisplay.update();
    }

    @Override
    public void render()
    {
        /**
         * Get the current graphics
         */
        Graphics2D offscreenGraphics = ( Graphics2D ) GameDisplay.getContext();
        offscreenGraphics.drawImage( background, null, 0, 0 );
        /**
         * Draw the Sprite objects
         */
        synchronized ( spriteList )
        {
            for ( Sprite spriteObj : spriteList )
            {
                spriteObj.draw( offscreenGraphics );
            }
        }
        if ( currentGameState == GameState.END_OF_LEVEL )
            nextLevelSprite.draw( offscreenGraphics );
    }

    @Override
    public void update()
    {
        if ( currentGameState != GameState.PAUSED )
        {
            if ( currentGameState == GameState.PLAYING )
                if ( nextTime < System.currentTimeMillis() && enemyCount < maxEnemyCount )
                {
                    synchronized ( spriteList )
                    {
                        releaseEnemy();
                    }
                }

            /**
             * Update the Sprite objects
             */
            synchronized ( spriteList )
            {
                boolean enemiesLeft = false;
                for ( Sprite spriteObj : spriteList )
                {
                    if ( currentGameState == GameState.PLAYING )
                        enemiesLeft = enemiesLeft || ( spriteObj instanceof AsteroidSprite || spriteObj instanceof EnemySprite );
                    else
                        enemiesLeft = true;
                    spriteObj.update();
                }
                if ( !enemiesLeft && enemyCount == maxEnemyCount )
                    currentGameState = GameState.END_OF_LEVEL;
            }
        }
    }

    @Override
    public void keyboardEvent( KeyEvent ke )
    {
        if ( ke.getID() == KeyEvent.KEY_PRESSED )
        {
            switch ( ke.getKeyCode() )
            {
                case KeyEvent.VK_ESCAPE:
                    /**
                     * Exit the application
                     */
                    GameEventDispatcher.dispatchEvent( new GameEvent( this, GameEventType.Quit, this ) );
                    break;

                case KeyEvent.VK_S: // start the game from the splash screen
                    GameEventDispatcher.dispatchEvent( new GameEvent( this, GameEventType.Start, this ) );
                    break;

                case KeyEvent.VK_P: // pause and unpause the game from
                                    // playing/paused state
                    GameEventDispatcher.dispatchEvent( new GameEvent( this, GameEventType.Pause, this ) );
                    break;

                case KeyEvent.VK_R: // restart the game after a game over
                    GameEventDispatcher.dispatchEvent( new GameEvent( this, GameEventType.Start, this ) );
                    break;

                case KeyEvent.VK_N: // move to next level
                    GameEventDispatcher.dispatchEvent( new GameEvent( this, GameEventType.NextLevel, this ) );
                    break;

                case KeyEvent.VK_H: // show help screen
                    GameEventDispatcher.dispatchEvent( new GameEvent( this, GameEventType.Help, this ) );
                    break;
            }
        }

        /**
         * Send the keyboard event to each sprite
         */
        if ( currentGameState != GameState.PAUSED )
        {
            synchronized ( spriteList )
            {
                for ( Sprite spriteObj : spriteList )
                {
                    spriteObj.keyboardEvent( ke );
                }
            }
        }
    }

    @Override
    public void mouseEvent( MouseEvent me )
    {
        /**
         * Send the mouse event to each sprite, unless game is paused
         */
        if ( currentGameState != GameState.PAUSED )
        {
            synchronized ( spriteList )
            {
                for ( Sprite spriteObj : spriteList )
                {
                    spriteObj.mouseEvent( me );
                }
            }
        }
    }

    @Override
    public void manageGameEvent( GameEvent ge )
    {

        switch ( ge.getType() )
        {
            case AddFirst:
                synchronized ( spriteList )
                {
                    spriteList.addFirst( ( Sprite ) ge.getAttachment() );
                }
                break;

            case AddLast:
                synchronized ( spriteList )
                {
                    spriteList.addLast( ( Sprite ) ge.getAttachment() );
                }
                break;

            case Remove:
                synchronized ( spriteList )
                {
                    Sprite sprite = ( Sprite ) ge.getAttachment();
                    spriteList.remove( sprite );
                    if ( sprite instanceof PlayerSprite )
                    {
                        GameEventDispatcher.dispatchEvent( new GameEvent( this, GameEventType.End, this ) );
                    }
                    if ( sprite instanceof AsteroidSprite )
                    {
                        AsteroidSprite as = ( AsteroidSprite ) ge.getAttachment();
                        Point pos = as.getPos();
                        Point vel = as.getVel();
                        switch ( as.getSize() )
                        {
                            case SMALL:
                                break;
                            case MEDIUM:
                                spriteList.add( new AsteroidSprite( pos.x, pos.y, vel.x * Math.cos( ( 2 * Math.PI + 1 ) / 3 ),
                                        vel.y * Math.sin( ( 2 * Math.PI + 1 ) / 3 ), Size.SMALL ) );
                                spriteList.add( new AsteroidSprite( pos.x, pos.y, vel.x * Math.cos( ( 4 * Math.PI + 1 ) / 3 ),
                                        vel.y * Math.sin( ( 4 * Math.PI + 1 ) / 3 ), Size.SMALL ) );
                                spriteList.add( new AsteroidSprite( pos.x, pos.y, vel.x * Math.cos( ( 6 * Math.PI + 1 ) / 3 ),
                                        vel.y * Math.sin( ( 6 * Math.PI + 1 ) / 3 ), Size.SMALL ) );
                                break;
                            case LARGE:
                                spriteList.add( new AsteroidSprite( pos.x, pos.y, vel.x * Math.cos( ( 2 * Math.PI + 1 ) / 3 ),
                                        vel.y * Math.sin( ( 2 * Math.PI + 1 ) / 3 ), Size.MEDIUM ) );
                                spriteList.add( new AsteroidSprite( pos.x, pos.y, vel.x * Math.cos( ( 4 * Math.PI + 1 ) / 3 ),
                                        vel.y * Math.sin( ( 4 * Math.PI + 1 ) / 3 ), Size.MEDIUM ) );
                                spriteList.add( new AsteroidSprite( pos.x, pos.y, vel.x * Math.cos( ( 6 * Math.PI + 1 ) / 3 ),
                                        vel.y * Math.sin( ( 6 * Math.PI + 1 ) / 3 ), Size.MEDIUM ) );
                                break;
                        }
                    }
                }
                break;

            case Score:
                int score = ( ( Integer ) ge.getAttachment() ).intValue();
                scoreSprite.add( score );
                break;

            case Start:
                if ( currentGameState == GameState.SPLASH )
                {
                    GameDisplay.captureCursor( true );
                    setLevel( level );
                    currentGameState = GameState.PLAYING;
                }
                else if ( currentGameState == GameState.GAME_OVER )
                {
                    GameDisplay.captureCursor( true );
                    enemyCount = 0;
                    level = 0;
                    currentGameState = GameState.SPLASH;
                    scoreSprite = new ScoreSprite();
                    playerSprite.reset();
                    synchronized ( spriteList )
                    {
                        spriteList.clear();
                        spriteList.add( splashSprite );
                    }
                }
                break;

            case Pause:
                if ( currentGameState == GameState.PLAYING )
                {
                    currentGameState = GameState.PAUSED;
                    GameDisplay.captureCursor( false );
                }
                else if ( currentGameState == GameState.PAUSED )
                {
                    currentGameState = GameState.PLAYING;
                    GameDisplay.captureCursor( true );
                }
                break;

            case End:
                GameDisplay.captureCursor( false );
                currentGameState = GameState.GAME_OVER;
                synchronized ( spriteList )
                {
                    spriteList.addFirst( gameOverSprite );
                }
                break;

            case Quit:
                synchronized ( spriteList )
                {
                    spriteList.clear();
                }
                if ( backgroundMusic != null )
                    backgroundMusic.close();
                GameEngine.stop();
                break;
            case NextLevel:
                nextTime = System.currentTimeMillis() + 500;
                synchronized ( spriteList )
                {
                    if ( currentGameState == GameState.END_OF_LEVEL )
                    {
                        spriteList.clear();
                        setLevel( ++level );
                    }
                }
                break;
            case Help:
                
                if ( currentGameState == GameState.SPLASH )
                {
                    currentGameState = GameState.HELP;
                    synchronized ( spriteList )
                    {
                        spriteList.remove( splashSprite );
                        spriteList.addLast( helpSprite );
                    }
                }
                else if ( currentGameState == GameState.HELP )
                {
                    currentGameState = GameState.SPLASH;
                    synchronized ( spriteList )
                    {
                        spriteList.remove( helpSprite );
                        spriteList.addLast( splashSprite );
                    }
                }
            default:
                break;

        }
    }

    /**
     * Return the current game state
     * 
     * @return GameState
     */
    public GameState getGameState()
    {
        return currentGameState;
    }

    private void releaseEnemy()
    {
        nextTime += NextEnemyTime;
        double cos = Math.acos( Math.random() );
        double sin = Math.PI / 2 - cos;
        switch ( level )
        {
            case 0:
                if ( enemyCount == 0 )
                {
                    spriteList.add( new AsteroidSprite( 0, 0, 3 * Math.cos( cos ), 3 * Math.sin( sin ), Size.MEDIUM ) );
                }
                else
                {
                    spriteList.add( new AsteroidSprite( 0, 0, 4 * Math.cos( cos ), 4 * Math.sin( sin ), Size.LARGE ) );
                }
                break;
            case 1:
                switch ( ( int ) enemyCount % 4 )
                {
                    case 0:
                        spriteList.add( new EnemySprite( playerSprite, 0, -100, 1 ) );
                        break;
                    case 1:
                        spriteList.add( new EnemySprite( playerSprite, display.width, 0, 1 ) );
                        break;
                    case 2:
                        spriteList.add( new EnemySprite( playerSprite, 0, display.height, 1 ) );
                        break;
                    case 3:
                        spriteList.add( new EnemySprite( playerSprite, display.width, display.height, 1 ) );
                        break;
                }
                break;
            case 2:
                if ( enemyCount % 5 == 0 )
                    spriteList.add( new EnemySprite( playerSprite, display.width, display.height, 2 ) );
                else
                    spriteList.add( new AsteroidSprite( 0, 0, 5 * Math.cos( cos ), 5 * Math.sin( sin ), Size.LARGE ) );
                break;
        }
        enemyCount++;
    }

    private void setLevel( int inLevel )
    {
        try
        {
            synchronized ( spriteList )
            {
                spriteList.clear();
                if ( inLevel == FINAL_LEVEL )
                {
                    spriteList.addLast( winSprite );
                    currentGameState = GameState.GAME_OVER;
                    return;
                }
                    
                else
                {
                    spriteList.addFirst( playerSprite );
                    spriteList.addFirst( scoreSprite );
                }
            }
            switch ( inLevel )
            {
                case 0:
                    // set enemy values
                    enemyCount = 0;
                    NextEnemyTime = 5000;
                    maxEnemyCount = 3;

                    // set background
                    background = ImageUtil.loadBufferedImage( this, "img/level0.png" );
                    // http://opengameart.org/content/space-background-2

                    // set music
                    if ( backgroundMusic != null )
                        backgroundMusic.close();
                    backgroundMusic = new AudioMidi( this, "sound/level1.mid" );
                    backgroundMusic.loop( AudioMidi.LOOP_CONTINUOUSLY );
                    break;
                case 1:
                    enemyCount = 0;
                    maxEnemyCount = 4;
                    NextEnemyTime = 4000;

                    background = ImageUtil.loadBufferedImage( this, "img/level1.png" );

                    backgroundMusic.close();
                    backgroundMusic = new AudioMidi( this, "sound/level2.mid" );
                    backgroundMusic.loop( AudioMidi.LOOP_CONTINUOUSLY );
                    break;
                case 2:
                    enemyCount = 0;
                    maxEnemyCount = 5;
                    NextEnemyTime = 3000;

                    background = ImageUtil.loadBufferedImage( this, "img/level2.png" );

                    backgroundMusic.close();
                    backgroundMusic = new AudioMidi( this, "sound/level3.mid" );
                    backgroundMusic.loop( AudioMidi.LOOP_CONTINUOUSLY );

                    break;
                case 3:
                    break;
            }
        }
        catch ( IOException ioe )
        {
            // TODO: EXCEPTION
        }
        catch ( InvalidMidiDataException | MidiUnavailableException e )
        {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }
        currentGameState = GameState.PLAYING;
    }
}
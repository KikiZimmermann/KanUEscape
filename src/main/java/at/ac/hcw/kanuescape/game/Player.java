package at.ac.hcw.kanuescape.game;

// Speed in tiles/sec
// Direction of sight (UP, DOWN, LEFT, RIGHT) + animation
// Provides current frame-row/column from sprite sheet

public class Player {

    public enum Direction {DOWN, LEFT, RIGHT, UP}

    // Sprite sheet layout
    public static final int SPRITE_COLS = 3; // animation phases
    public static final int SPRITE_ROWS = 4; // directions

    // Column -> animation phases
    public static final int COL_MOVE1 = 0;
    public static final int COL_IDLE = 1;
    public static final int COL_MOVE2 = 2;

    // Rows -> directions
    public static final int ROW_UP = 0;
    public static final int ROW_RIGHT = 1;
    public static final int ROW_DOWN = 2;
    public static final int ROW_LEFT = 3;

    // Position in tile coordinates
    public double tileX;
    public double tileY;

    // Mvm speed in tiles/sec
    private double speedTilesPerSecond = 1.0;

    // Direction of view
    private Direction direction = Direction.DOWN;

    // Animation state (column)
    // Sequence: MOVE1 -> IDLE -> MOVE2 -> IDLE -> ...
    private final int[] moveSequence = {COL_MOVE1, COL_IDLE, COL_MOVE2, COL_IDLE};
    private int moveSeqIndex = 1; // Start in idle
    private int frameCol = COL_IDLE;

    // Animation timing
    private long frameDurationNS = 120_000_000; // 120ns per step
    private long lastFrameTime = 0;

    public Player(double startTileX, double startTileY) {
        this.tileX = startTileX;
        this.tileY = startTileY;
    }

    // Update per "tick": calculate position and direction of view from input
    // dt = time delta in sec since last "tick" - check if neccessary
    public void update(double dt, boolean up, boolean down, boolean left, boolean right) {
        double dx = 0.0;
        double dy = 0.0;

        // With diagonal walking
        //   if (up) dy -= 1.0;
        //   if (down) dy += 1.0;
        //   if (left) dx -= 1.0;
        //   if (right) dx += 1.0;

        // Without diagonal walking
        if (up) {
            dy = -1.0;
            direction = Direction.UP;
        } else if (down) {
            dy =  1.0;
            direction = Direction.DOWN;
        } else if (left) {
            dx = -1.0;
            direction = Direction.LEFT;
        } else if (right) {
            dx =  1.0;
            direction = Direction.RIGHT;
        }

        // no direction -> IDLE in last direction of view
        tileX += dx * speedTilesPerSecond * dt;
        tileY += dy * speedTilesPerSecond * dt;
    }

    // Animation update
    // now = current time in Ns
    // boolean moving = if direction Taste pressed
    public void animate(long now, boolean moving) {
        if (!moving) {
            // idle -> middle column
            frameCol = COL_IDLE;
            moveSeqIndex = 1; // -> back to idle
            return;
        }

        // Movement: cyclic trough MOVE1 -> IDLE -> MOVE2 -> IDLE -> ...
        if (now - lastFrameTime > frameDurationNS) {
            lastFrameTime = now;
            moveSeqIndex = (moveSeqIndex + 1) % moveSequence.length;
            frameCol = moveSequence[moveSeqIndex];
        }
    }

    // Getter for rendering
    public double getTileX() {
        return tileX;
    }
    public double getTileY() {
        return tileY;
    }
    public int getFrameCol() {
        return frameCol;
    }

    public int getFrameRow() {
        return switch (direction) {
            case UP -> ROW_UP;
            case DOWN -> ROW_DOWN;
            case LEFT -> ROW_LEFT;
            case RIGHT -> ROW_RIGHT;
        };
    }

    // Set speed in tiles/sec
    public void setSpeedTilesPerSecond(double speedTilesPerSecond) {
        this.speedTilesPerSecond = speedTilesPerSecond;
    }

    // Set animation speed in ms/frame
    public void setFrameDurationMs(long ms) {
        this.frameDurationNS = ms * 1_000_000;
    }
}

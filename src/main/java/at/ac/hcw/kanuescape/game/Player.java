package at.ac.hcw.kanuescape.game;

// GRID BASED MOVEMENT
// Logical grid position (int; gridX / gridY)
// Render tile position (double; tileX / tileY)
// 1 tile per click OR continuous running while pressing down; stopping in grid

public class Player {

    public enum Direction {UP, RIGHT, DOWN, LEFT}

    private Direction queuedDir = null;

    // Sprite sheet layout
    public static final int SPRITE_COLS = 3; // MOVE1; IDLE; MOVE2
    public static final int SPRITE_ROWS = 4; // 0 = UP; 1 = RIGHT; 2 = DOWN; 3 = LEFT

    // Columns
    public static final int COL_MOVE1 = 0;
    public static final int COL_IDLE = 1;
    public static final int COL_MOVE2 = 2;

    // Rows
    public static final int ROW_UP = 0;
    public static final int ROW_RIGHT = 1;
    public static final int ROW_DOWN = 2;
    public static final int ROW_LEFT = 3;

    // Logical grid-position (int)
    private int gridX;
    private int gridY;

    // Render tile-position (double)
    private double tileX;
    private double tileY;

    // Step status
    private boolean moving = false;
    private int startX, startY; // start tile of current step
    private int targetX, targetY; // target tile of current step
    private double progress = 0.0; // 0-1; on 1, the target tile has been reached

    // Tiles per second (1 step = 1 tile)
    private double speedTilesPerSecond = 4.0;

    // Start direction of view
    private Direction direction = Direction.DOWN;

    // Animation (row sequence)
    private final int[] moveSequence = {COL_MOVE1, COL_IDLE, COL_MOVE2, COL_IDLE};
    private int moveSeqIndex = 1; // start in idle
    private int frameCol = COL_IDLE; // current row
    private long frameDurationNs = 120_000_000; // equals 120 ms
    private long lastFrameTimeNs = 0;

    public Player(int startGridX, int startGridY) {
        this.gridX = startGridX;
        this.gridY = startGridY;
        this.tileX = startGridX;
        this.tileY = startGridY;
    }

    public void update(double dt, boolean up, boolean down, boolean left, boolean right) {

        Direction wanted = null;
        if (up) wanted = Direction.UP;
        else if (down) wanted = Direction.DOWN;
        else if (left) wanted = Direction.LEFT;
        else if (right) wanted = Direction.RIGHT;

        if (wanted != null) {
            direction = wanted;
            if (moving) queuedDir = wanted;
        }

        if (!moving) {
            if (up) {
                direction = Direction.UP;
                startMove(gridX, gridY - 1);
            } else if (down) {
                direction = Direction.DOWN;
                startMove(gridX, gridY + 1);
            } else if (left) {
                direction = Direction.LEFT;
                startMove(gridX - 1, gridY);
            } else if (right) {
                direction = Direction.RIGHT;
                startMove(gridX + 1, gridY);
            }
        }

        if (moving) { // Progress from 0 -> 1; grows with v * dt
            progress += speedTilesPerSecond * dt;
            if (progress >= 1.0) { // step completed -> logical and render position to target tile
                gridX = targetX;
                gridY = targetY;
                tileX = gridX;
                tileY = gridY;
                moving = false;
                progress = 0.0;
            } else {
                tileX = lerp(startX, targetX, progress);
                tileY = lerp(startY, targetY, progress);
            }
            } else { // standing still - render position unchanged
                tileX = gridX;
                tileY = gridY;
            }
        }

        public void animate(long now, boolean movingFlag) {
        if(!movingFlag) {
            frameCol = COL_IDLE;
            moveSeqIndex = 1;
            return;
        }
        if(now - lastFrameTimeNs >= frameDurationNs) {
            lastFrameTimeNs = now;
            moveSeqIndex = (moveSeqIndex + 1) % moveSequence.length;
            frameCol = moveSequence[moveSeqIndex];
        }
        }

        private void startMove ( int nextX, int nextY){
            startX = gridX;
            startY = gridY;
            targetX = nextX;
            targetY = nextY;
            progress = 0.0;
            moving = true;
        }

        private static double lerp(double a, double b, double t) {
        if(t <= 0) return a;
        if(t >= 1) return b;
        return a + (b - a) * t;
        }

    // Getter for rendering
    public double getTileX() {return tileX;}
    public double getTileY() {return tileY;}

    public int getGridX() {return gridX;}
    public int getGridY() {return gridY;}

    public int getFrameCol() {return frameCol;}
    public int getFrameRow() {
        return switch(direction) {
            case UP -> ROW_UP;
            case DOWN -> ROW_DOWN;
            case LEFT -> ROW_LEFT;
            case RIGHT -> ROW_RIGHT;
        };
    }

    public boolean isMoving() { return moving; }

    // Tuning
    // tiles per second (1 step = 1 tile)
    public void setSpeedTilesPerSecond(double s) {
        this.speedTilesPerSecond = Math.max(0.001, s);
    }
    // Animation duration
    public void setFrameDurationNs(long ms) {
        this.frameDurationNs = Math.max(1, ms) * 1_000_000L;
    }

    // Rest of  player position for NEW GAME via menu
    public void resetTo(int startGridX, int startGridY) {
        // logical position
        this.gridX = startGridX;
        this.gridY = startGridY;

        // render position
        this.tileX = startGridX;
        this.tileY = startGridY;

        // stop any current step
        this.moving = false;
        this.progress = 0.0;

        this.startX = startGridX;
        this.startY = startGridY;
        this.targetX = startGridX;
        this.targetY = startGridY;

        // Clear queued input
        this.queuedDir = null;

        // Optional: look direction back to default
        this.direction = Direction.DOWN;

        // Reset animation to idle
        this.frameCol = COL_IDLE;
        this.moveSeqIndex = 1;
        this.lastFrameTimeNs = 0;
    }
}
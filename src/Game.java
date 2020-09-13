public class Game {
    private final static int BOMB = 0b1;
    private final static int OPEN = 0b10;
    private final static int FLAG = 0b100;
    private final static int VALUE_SHIFT = 3;

    private final static int STATE_PREGAME = -1;
    private final static int STATE_INGAME = 0;
    private final static int STATE_POSTGAME = 1;
    private final static int STATE_PAUSED = 2;

    private int currentState = STATE_PREGAME;

    private long startTime = 0;
    private long timer = 0;


    private int[][][][] map;
    private final int cBombs;
    private final int dimX, dimY, fWidth, fHeight;


    public Game(int dimX, int dimY, int fWidth, int fHeight, int cBombs) throws IllegalArgumentException {
        if (dimX <= 0 || dimY <= 0 || fWidth <= 0 || fHeight <= 0 || cBombs < 0 || cBombs >= (dimX * dimY * fWidth * fHeight))
            throw new IllegalArgumentException("Invalid arguments for a new game");

        this.dimX = dimX;
        this.dimY = dimY;
        this.fWidth = fWidth;
        this.fHeight = fHeight;
        this.cBombs = cBombs;
        map = new int[dimX][dimY][fWidth][fHeight];


        for (int i = 0; i < cBombs; i++) {
            createBomb();
        }
        calcValues();
    }


    public String getFieldText(int dx, int dy, int x, int y) {
        if (isFlag(dx, dy, x, y)) {
            return ("F");
        } else if (isBomb(dx, dy, x, y))
            return ("[B]");
        else {
            if (getValue(dx, dy, x, y) == 0)
                return "";
            return (getValue(dx, dy, x, y) + "");
        }
    }

    public void calcValues() {
        for (int dx = 0; dx < dimX; dx++) {
            for (int dy = 0; dy < dimY; dy++) {
                for (int x = 0; x < fWidth; x++) {
                    for (int y = 0; y < fHeight; y++) {
                        if (isBomb(dx, dy, x, y)) {
                            setValue(dx, dy, x, y, 0);
                            setBomb(dx, dy, x, y);
                            continue;
                        }
                        int n = 0;
                        for (int dxn = dx - 1; dxn <= dx + 1; dxn++) {
                            for (int dyn = dy - 1; dyn <= dy + 1; dyn++) {
                                for (int xn = x - 1; xn <= x + 1; xn++) {
                                    for (int yn = y - 1; yn <= y + 1; yn++) {
                                        if (dx == dxn && dy == dyn && x == xn && y == yn)
                                            continue;

                                        try {
                                            n += isBomb(dxn, dyn, xn, yn) ? 1 : 0;
                                        } catch (Exception ignored) {
                                        }

                                    }
                                }
                            }
                        }
                        setValue(dx, dy, x, y, n);
                    }
                }
            }
        }
    }

    public int getValue(int dx, int dy, int x, int y) {
        return map[dx][dy][x][y] >> VALUE_SHIFT;
    }

    public void setValue(int dx, int dy, int x, int y, int value) {
        map[dx][dy][x][y] = (map[dx][dy][x][y] & (BOMB | OPEN | FLAG) | (value << VALUE_SHIFT));
    }

    public boolean isBomb(int dx, int dy, int x, int y) {
        return (map[dx][dy][x][y] & BOMB) == BOMB;
    }

    public void setBomb(int dx, int dy, int x, int y) {
        map[dx][dy][x][y] |= BOMB;
    }

    public void createBomb() {
        int x, y;
        do {
            x = (int) (dimX * fWidth * Math.random());
            y = (int) (dimY * fHeight * Math.random());
        } while (isBomb(x / fWidth, y / fHeight, x % fWidth, y % fHeight));
        setBomb(x / fWidth, y / fHeight, x % fWidth, y % fHeight);
    }

    public boolean isOpen(int dx, int dy, int x, int y) {
        return ((map[dx][dy][x][y] & OPEN) == OPEN);
    }


    public void spread(int dx, int dy, int x, int y) {
        if (currentState == STATE_PREGAME) {
            if (isBomb(dx, dy, x, y)) {
                createBomb();
                map[dx][dy][x][y] ^= BOMB;
                calcValues();
            }
            startTime = System.currentTimeMillis();
            currentState = STATE_INGAME;
        } else {
            if (isBomb(dx, dy, x, y)) {
                gameOver();
            }
        }
        map[dx][dy][x][y] |= OPEN;
        map[dx][dy][x][y] |= FLAG;
        map[dx][dy][x][y] ^= FLAG;

        if (getValue(dx, dy, x, y) != 0)
            return;

        for (int i = -1; i <= 1; i += 2) {
            try {
                if (!isOpen(dx + i, dy, x, y))
                    spread(dx + i, dy, x, y);
            } catch (Exception ignore) {
            }

            try {
                if (!isOpen(dx, dy, x + i, y))
                    spread(dx, dy, x + i, y);
            } catch (Exception ignore) {
            }

            try {
                if (!isOpen(dx, dy + i, x, y))
                    spread(dx, dy + i, x, y);
            } catch (Exception ignore) {
            }

            try {
                if (!isOpen(dx, dy, x, y + i))
                    spread(dx, dy, x, y + i);
            } catch (Exception ignore) {
            }
        }
    }

    public boolean hasWon() {
        for (int dx = 0; dx < dimX; dx++) {
            for (int dy = 0; dy < dimY; dy++) {
                for (int x = 0; x < fWidth; x++) {
                    for (int y = 0; y < fHeight; y++) {
                        if (isBomb(dx, dy, x, y) ^ isFlag(dx, dy, x, y))
                            return false;
                    }
                }
            }
        }
        return true;
    }

    private void gameOver() {
        timer += System.currentTimeMillis() - startTime;
        currentState = STATE_POSTGAME;
    }

    public boolean isFlag(int dx, int dy, int x, int y) {
        return (map[dx][dy][x][y] & FLAG) == FLAG;
    }

    public void setFlag(int dx, int dy, int x, int y) {
        if (!isOpen(dx, dy, x, y))
            map[dx][dy][x][y] ^= FLAG;
    }

    public int[][][][] getMap() {
        return map;
    }

    public boolean isOver() {
        return currentState == STATE_POSTGAME;
    }

    public boolean isPaused() {
        return currentState == STATE_PAUSED;
    }

    public boolean isRunning() {
        return currentState == STATE_INGAME;
    }

    public void setPaused(boolean paused) {
        if (paused) {
            currentState = STATE_PAUSED;
            timer += System.currentTimeMillis() - startTime;
        } else {
            currentState = STATE_INGAME;
            startTime = System.currentTimeMillis();
        }
    }

    public void setMap(int[][][][] map) {
        this.map = map;
    }

    public int getDimX() {
        return dimX;
    }


    public int getDimY() {
        return dimY;
    }


    public int getfWidth() {
        return fWidth;
    }


    public int getfHeight() {
        return fHeight;
    }

    public int getBombs() {
        return cBombs;
    }

    public long getMilliTime() {
        return timer;
    }

    public void print() {
        for (int dx = 0; dx < dimX; dx++) {
            for (int dy = 0; dy < dimY; dy++) {
                for (int x = 0; x < fWidth; x++) {
                    for (int y = 0; y < fHeight; y++) {
                        if (isBomb(dx, dy, x, y))
                            System.out.print("  [X]");
                        else if (isFlag(dx, dy, x, y)) {
                            System.out.print("  [F]");
                        } else {
                            if (isOpen(dx, dy, x, y))
                                System.out.print(" ");
                            else
                                System.out.print(".");
                            System.out.print("[" + getValue(dx, dy, x, y) + "]  ");
                        }

                    }
                    System.out.print("    ");
                }
                System.out.println();
            }
            System.out.println();
        }
    }

}

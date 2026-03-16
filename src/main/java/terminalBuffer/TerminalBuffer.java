package terminalBuffer;

import java.util.ArrayDeque;

public class TerminalBuffer {

    private int screenHeight;
    private int screenWidth;

    private int scrollbackSize;

    private int cursorPositionX;
    private int cursorPositionY;

    private int currentStyles;
    private int currentBackgroundColor;
    private int currentForegroundColor;

    private Cell[][] screen;
    private ArrayDeque<Cell[]> scrollback;

    public TerminalBuffer(int scrollbackSize, int screenWidth, int screenHeight) {
        this.scrollbackSize = scrollbackSize;
        this.screenWidth = screenWidth;
        this.screenHeight = screenHeight;
        this.currentBackgroundColor = Cell.BLACK;
        this.currentForegroundColor = Cell.WHITE;
        this.currentStyles = 0;
        this.cursorPositionX = 0;
        this.cursorPositionY = 0;
        this.screen = new Cell[this.screenHeight][this.screenWidth];
        for (int row = 0; row < screenHeight; row++) {
            for (int col = 0; col < screenWidth; col++) {
                screen[row][col] = new Cell();
            }
        }
        this.scrollback = new ArrayDeque<>();
    }

    public void setCurrentForegroundColor(int currentForegroundColor) {
        this.currentForegroundColor = currentForegroundColor;
    }

    public void setCurrentBackgroundColor(int currentBackgroundColor) {
        this.currentBackgroundColor = currentBackgroundColor;
    }

    public void setCurrentStyles(int currentStyles) {
        this.currentStyles = currentStyles;
    }

    public void addStyle(Cell.Style style) {
        switch (style) {
            case BOLD:
                currentStyles |= 1;
                break;
            case ITALIC:
                currentStyles |= 1<<1;
                break;
            case UNDERLINE:
                currentStyles |= 1<<2;
                break;
        }
    }

    public void removeStyle(Cell.Style style) {
        switch (style) {
            case BOLD:
                currentStyles &= ~1;
                break;
            case ITALIC:
                currentStyles &= ~(1 << 1);
                break;
            case UNDERLINE:
                currentStyles &= ~(1 << 2);
                break;
        }
    }

    public int getCursorPositionY() {
        return cursorPositionY;
    }

    public int getCursorPositionX() {
        return cursorPositionX;
    }

}

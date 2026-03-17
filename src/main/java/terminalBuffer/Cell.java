package terminalBuffer;

public class Cell {

    private char value;

    private int backgroundColor;
    private int foregroundColor;

    private int styles;

    private boolean cursorAt;

    public Cell(int styles, int foregroundColor, int backgroundColor, char value) {
        this.styles = styles;
        this.foregroundColor = foregroundColor;
        this.backgroundColor = backgroundColor;
        this.value = value;
        cursorAt = false;
    }

    public Cell() {
        this.styles = 0;
        this.foregroundColor = WHITE;
        this.backgroundColor = BLACK;
        this.value = '\u0000';
    }

    public enum Style {
        BOLD,
        ITALIC,
        UNDERLINE,
    }

    public int getForegroundColor() {
        return foregroundColor;
    }

    public int getBackgroundColor() {
        return backgroundColor;
    }

    public char getValue() {
        return value;
    }

    public void setValue(char value) {
        this.value = value;
    }

    public int getStyles() {
        return styles;
    }
    public void setStyles(int styles) {
        this.styles = styles;
    }

    public void setBackgroundColor(int backgroundColor) {
        this.backgroundColor = backgroundColor;
    }

    public void setForegroundColor(int foregroundColor) {
        this.foregroundColor = foregroundColor;
    }

    public void removeStyle(Style s) {
        switch (s) {
            case BOLD:
                styles &= ~1;
                break;
            case ITALIC:
                styles &= ~(1 << 1);
                break;
            case UNDERLINE:
                styles &= ~(1 << 2);
                break;
        }
    }

    public void addStyle(Style s) {
        switch (s) {
            case BOLD:
                styles |= 1;
                break;
            case ITALIC:
                styles |= 1<<1;
                break;
            case UNDERLINE:
                styles |= 1<<2;
                break;
        }
    }

    public static final int BLACK = 0x000000;
    public static final int BRIGHT_WHITE = 0xFFFFFF;
    public static final int RED = 0x800000;
    public static final int GREEN = 0x008000;
    public static final int YELLOW = 0x808000;
    public static final int BLUE = 0x000080;
    public static final int MAGENTA = 0x800080;
    public static final int CYAN = 0x008080;
    public static final int WHITE = 0xc0c0c0;
    public static final int GRAY = 0x808080;
    public static final int BRIGHT_RED = 0xff0000;
    public static final int BRIGHT_GREEN = 0X00ff00;
    public static final int BRIGHT_YELLOW = 0xffff00;
    public static final int BRIGHT_BLUE = 0x0000ff;
    public static final int BRIGHT_MAGENTA = 0xff00ff;
    public static final int BRIGHT_CYAN = 0x00ffff;

    public void resetCell() {
        this.styles = 0;
        this.foregroundColor = 0;
        this.backgroundColor = 0;
        this.value = '\u0000';
    }

    @Override
    public String toString() { return "" + value;}

    public boolean isEmpty() {
        return value == '\u0000';
    }

    @Override
    public Cell clone() {
        return new Cell(styles, foregroundColor, backgroundColor, value);
    }

    public boolean isCursorAt() {
        return cursorAt;
    }

    public void setCursorAt(boolean cursorAt) {
        this.cursorAt = cursorAt;
    }
}

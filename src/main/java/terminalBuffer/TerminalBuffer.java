package terminalBuffer;

import java.util.ArrayDeque;
import java.util.Deque;

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

    private int firstRow;

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
        firstRow = -1;
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

    public void setCursorPosition(int cursorPositionX, int cursorPositionY) {
        if(cursorPositionX < 0 || cursorPositionY < 0 || cursorPositionX >= screenWidth || cursorPositionY >= screenHeight) {
            throw new CursorOutOfBoundaryException("Cursor position out of boundary");
        }
        this.cursorPositionX = cursorPositionX;
        this.cursorPositionY = cursorPositionY;
    }

    public void moveCursorLeft(int n) throws CursorOutOfBoundaryException {
        cursorPositionX -= n;
        if(cursorPositionX < 0) {
            cursorPositionX += n;
            throw new CursorOutOfBoundaryException("X cursor position out of boundary");
        }
    }
    public void moveCursorRight(int n) throws CursorOutOfBoundaryException {
        cursorPositionX += n;
        if(cursorPositionX >= screenWidth) {
            cursorPositionX -= n;
            throw new CursorOutOfBoundaryException("X cursor position out of boundary");
        }
    }

    public void moveCursorUp(int n) throws CursorOutOfBoundaryException {
        cursorPositionY -= n;
        if(cursorPositionY < 0) {
            cursorPositionY += n;
            throw new CursorOutOfBoundaryException("Y cursor position out of boundary");
        }
    }

    public void moveCursorDown(int n) throws CursorOutOfBoundaryException {
        cursorPositionY += n;
        if(cursorPositionY >= screenHeight) {
            cursorPositionY -= n;
            throw new CursorOutOfBoundaryException("Y cursor position out of boundary");
        }
    }

    private void scrollUp() {
        scrollback.addLast(screen[firstRow]);
        if(scrollback.size() > scrollbackSize) {
            scrollback.removeFirst();
        }
        screen[firstRow] = new Cell[screenWidth];
        for(int col = 0; col < screenWidth; col++) {
            screen[firstRow][col] = new Cell();
        }
        firstRow = (firstRow + 1) % screenHeight;
    }

    private Cell getCellAtCurrentCursorPosition() {
        return screen[cursorPositionY][cursorPositionX];
    }


    private void addChar(char c) {
        if(c == '\n') {
            cursorPositionX = 0;
            cursorPositionY = (cursorPositionY + 1) %  screenHeight;

        } else if (c == '\r') {
            cursorPositionX = 0;

        } else {
            screen[cursorPositionY][cursorPositionX].setValue(c);
            screen[cursorPositionY][cursorPositionX].setStyles(currentStyles);
            screen[cursorPositionY][cursorPositionX].setBackgroundColor(currentBackgroundColor);
            screen[cursorPositionY][cursorPositionX].setForegroundColor(currentForegroundColor);

            cursorPositionX++;
            if(cursorPositionX == screenWidth) {
                cursorPositionX = 0;
                cursorPositionY = (cursorPositionY + 1) %  screenHeight;
            }
        }

        if(cursorPositionY > 0 && firstRow == -1) {
            //initaly
            firstRow = 0;
        }
        else if(cursorPositionY == firstRow) {
            scrollUp();
        }
    }

    /// same as addChar, but it changes attributes
    private void addCell(Cell cell) {
        currentBackgroundColor = cell.getBackgroundColor();
        currentForegroundColor = cell.getForegroundColor();
        currentStyles = cell.getStyles();

        addChar(cell.getValue());
    }

    public void writeTextOnLine(String text) {
        int initialRow = cursorPositionY;
        for(int i = 0; i < text.length(); i++) {
            addChar(text.charAt(i));
            if (cursorPositionY != initialRow) break;
        }
    }

    public void insertTextOnLine(String text) {
        Deque<Cell> cells = new ArrayDeque<>();
        Cell currentPositionCell;

        for(int i = 0; i < text.length(); i++) {
            currentPositionCell = getCellAtCurrentCursorPosition();
            if(!currentPositionCell.isEmpty()) {
                cells.addLast(currentPositionCell.clone());
            }
            addChar(text.charAt(i));
        }

        int previousCursorPositionY = cursorPositionY;
        int previousCursorPositionX = cursorPositionX;
        int previousBackgroundColor = currentBackgroundColor;
        int previousForegroundColor = currentForegroundColor;
        int previousStyles = currentStyles;

        while(!cells.isEmpty()) {
            currentPositionCell = getCellAtCurrentCursorPosition();
            if(!currentPositionCell.isEmpty()) {
                cells.addLast(currentPositionCell.clone());
            }
            addCell(cells.pop());
        }

        cursorPositionY = previousCursorPositionY;
        cursorPositionX = previousCursorPositionX;
        currentBackgroundColor =  previousBackgroundColor;
        currentForegroundColor = previousForegroundColor;
        currentStyles = previousStyles;
    }

    public void fillLineWithCharacter(char c) {
        for(int i = 0; i < screenWidth; i++) {
            screen[cursorPositionY][i].setValue(c);
        }
    }

    public void clearLine() {
        fillLineWithCharacter('\u0000');
    }

    public void insertEmptyLineAtBottom() {
        scrollUp();
    }

    public void clearEntireScreen() {
        for (int row = 0; row < screenHeight; row++) {
            for (int col = 0; col < screenWidth; col++) {
                screen[row][col].resetCell();
            }
        }
    }

    public void clearScreenAndScrollback() {
        clearEntireScreen();
        scrollback.clear();
    }

    //GETTERS

    private Cell getCellAt(int row, int col) throws ArgumentsOutOfRangeException{
        if(row < 0 || row >= screenHeight + scrollback.size() || col < 0 || col >= screenWidth) {
            throw new ArgumentsOutOfRangeException("Row or column out of boundary");
        }
        if(row < scrollback.size()) {
            Cell[] line = (Cell[]) scrollback.toArray()[row];
            return line[col];
        } else {
            return this.screen[(firstRow + row) % screenHeight][col];
        }
    }

    public char getCharAt(int row, int col) throws ArgumentsOutOfRangeException {
        return getCellAt(row, col).getValue();
    }

    public int getBackgroundColorAt(int row, int col) throws ArgumentsOutOfRangeException {
        return getCellAt(row, col).getBackgroundColor();
    }

    public int getForegroundColorAt(int row, int col) throws ArgumentsOutOfRangeException {
        return getCellAt(row, col).getForegroundColor();
    }

    public int getStylesAt(int row, int col) throws ArgumentsOutOfRangeException {
        return getCellAt(row, col).getStyles();
    }

    public String getLine(int row) throws ArgumentsOutOfRangeException {
        if(row < 0 || row >= screenHeight + scrollback.size()) {
            throw new ArgumentsOutOfRangeException("Row or column out of boundary");
        }
        StringBuilder sb = new StringBuilder();
        if(row < scrollback.size()) {
            Cell[] line = (Cell[]) scrollback.toArray()[row];
            for(int i = 0 ; i < line.length; i++) {
                sb.append(line[i].getValue());
            }
        } else {
            for(int i = 0 ; i < screenWidth; i++) {
                sb.append(this.screen[(firstRow + row) % screenHeight][i]);
            }
        }
        return sb.toString();
    }

    public String buildStringFromScreen(StringBuilder sb) {
        for(int i = 0; i < this.screenHeight; i++) {
            for(int j = 0; j < this.screenWidth; j++) {
                sb.append(this.screen[(firstRow + i) % screenHeight][j].toString());
            }
            sb.append('\n');
        }
        return sb.toString();
    }

    public String getEntireScreen() {
        StringBuilder sb = new StringBuilder();
        return buildStringFromScreen(sb);
    }

    public String getEntireScreenAndScrollback() {
        StringBuilder sb = new StringBuilder();
        for(Cell[] row: scrollback) {
            for(int col = 0; col < screenWidth; col++) {
                sb.append(row[col].toString());
            }
            sb.append('\n');
        }
        sb.append('\n');
        return buildStringFromScreen(sb);
    }
}

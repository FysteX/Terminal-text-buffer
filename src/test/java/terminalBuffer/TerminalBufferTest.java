package terminalBuffer;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class TerminalBufferTest {

    private TerminalBuffer tb;

    @BeforeEach
    void setUp() {
        tb = new TerminalBuffer(10, 5, 3);
    }

    @Nested
    class Initialization {

        @Test
        void cursorStartsAtOrigin() {
            assertEquals(0, tb.getCursorPositionX());
            assertEquals(0, tb.getCursorPositionY());
        }

        @Test
        void screenIsInitiallyEmpty() {
            for (int row = 0; row < 3; row++) {
                for (int col = 0; col < 5; col++) {
                    assertEquals('\u0000', tb.getCharAt(row, col));
                }
            }
        }

        @Test
        void defaultForegroundIsWhite() {
            tb.writeTextOnLine("A");
            assertEquals(Cell.WHITE, tb.getForegroundColorAt(0, 0));
        }

        @Test
        void defaultBackgroundIsBlack() {
            tb.writeTextOnLine("A");
            assertEquals(Cell.BLACK, tb.getBackgroundColorAt(0, 0));
        }

        @Test
        void defaultStylesAreZero() {
            tb.writeTextOnLine("A");
            assertEquals(0, tb.getStylesAt(0, 0));
        }
    }

    // Cursor movement

    @Nested
    class CursorMovement {

        @Test
        void setCursorPositionUpdatesCoordinates() {
            tb.setCursorPosition(2, 4);
            assertEquals(2, tb.getCursorPositionY());
            assertEquals(4, tb.getCursorPositionX());
        }

        @Test
        void setCursorPositionOutOfBoundsThrows() {
            assertThrows(CursorOutOfBoundaryException.class, () -> tb.setCursorPosition(3, 0));
            assertThrows(CursorOutOfBoundaryException.class, () -> tb.setCursorPosition(0, 5));
            assertThrows(CursorOutOfBoundaryException.class, () -> tb.setCursorPosition(-1, 0));
            assertThrows(CursorOutOfBoundaryException.class, () -> tb.setCursorPosition(0, -1));
        }

        @Test
        void moveCursorRightByOne() {
            tb.moveCursorRight(1);
            assertEquals(1, tb.getCursorPositionX());
        }

        @Test
        void moveCursorRightToLastColumn() {
            tb.moveCursorRight(4);
            assertEquals(4, tb.getCursorPositionX());
        }

        @Test
        void moveCursorRightBeyondBoundaryThrows() {
            assertThrows(CursorOutOfBoundaryException.class, () -> tb.moveCursorRight(5));
        }

        @Test
        void moveCursorLeftByOne() {
            tb.setCursorPosition(0, 3);
            tb.moveCursorLeft(1);
            assertEquals(2, tb.getCursorPositionX());
        }

        @Test
        void moveCursorLeftBeyondBoundaryThrows() {
            assertThrows(CursorOutOfBoundaryException.class, () -> tb.moveCursorLeft(1));
        }

        @Test
        void moveCursorDownByOne() {
            tb.moveCursorDown(1);
            assertEquals(1, tb.getCursorPositionY());
        }

        @Test
        void moveCursorDownToLastRow() {
            tb.moveCursorDown(2);
            assertEquals(2, tb.getCursorPositionY());
        }

        @Test
        void moveCursorDownBeyondBoundaryThrows() {
            assertThrows(CursorOutOfBoundaryException.class, () -> tb.moveCursorDown(3));
        }

        @Test
        void moveCursorUpByOne() {
            tb.setCursorPosition(2, 0);
            tb.moveCursorUp(1);
            assertEquals(1, tb.getCursorPositionY());
        }

        @Test
        void moveCursorUpAtTopRowThrows() {
            assertThrows(CursorOutOfBoundaryException.class, () -> tb.moveCursorUp(1));
        }

    }

    // Attributes

    @Nested
    class Attributes {

        @Test
        void setForegroundColorIsAppliedToNextWrite() {
            tb.setCurrentForegroundColor(Cell.RED);
            tb.writeTextOnLine("A");
            assertEquals(Cell.RED, tb.getForegroundColorAt(0, 0));
        }

        @Test
        void setBackgroundColorIsAppliedToNextWrite() {
            tb.setCurrentBackgroundColor(Cell.BLUE);
            tb.writeTextOnLine("A");
            assertEquals(Cell.BLUE, tb.getBackgroundColorAt(0, 0));
        }

        @Test
        void setAttributesAppliesAllThreeAtOnce() {
            tb.setAttributes(Cell.RED, Cell.BLUE, 1);
            tb.writeTextOnLine("A");
            assertEquals(Cell.RED, tb.getForegroundColorAt(0, 0));
            assertEquals(Cell.BLUE, tb.getBackgroundColorAt(0, 0));
            assertEquals(1, tb.getStylesAt(0, 0));
        }

        @Test
        void addBoldStyle() {
            tb.addStyle(Cell.Style.BOLD);
            tb.writeTextOnLine("A");
            assertTrue((tb.getStylesAt(0, 0) & 1) != 0);
        }

        @Test
        void addItalicStyle() {
            tb.addStyle(Cell.Style.ITALIC);
            tb.writeTextOnLine("A");
            assertTrue((tb.getStylesAt(0, 0) & (1 << 1)) != 0);
        }

        @Test
        void addUnderlineStyle() {
            tb.addStyle(Cell.Style.UNDERLINE);
            tb.writeTextOnLine("A");
            assertTrue((tb.getStylesAt(0, 0) & (1 << 2)) != 0);
        }

        @Test
        void addMultipleStyles() {
            tb.addStyle(Cell.Style.BOLD);
            tb.addStyle(Cell.Style.ITALIC);
            tb.writeTextOnLine("A");
            int styles = tb.getStylesAt(0, 0);
            assertTrue((styles & 1) != 0);
            assertTrue((styles & (1 << 1)) != 0);
        }

        @Test
        void removeStyle() {
            tb.addStyle(Cell.Style.BOLD);
            tb.addStyle(Cell.Style.ITALIC);
            tb.removeStyle(Cell.Style.BOLD);
            tb.writeTextOnLine("A");
            int styles = tb.getStylesAt(0, 0);
            assertEquals(0, styles & 1);           // bold removed
            assertTrue((styles & (1 << 1)) != 0);  // italic still set
        }

        @Test
        void attributesDoNotAffectCellsWrittenBefore() {
            tb.writeTextOnLine("A");
            tb.setCurrentForegroundColor(Cell.RED);
            tb.writeTextOnLine("B");
            assertEquals(Cell.WHITE, tb.getForegroundColorAt(0, 0)); // A written with WHITE
            assertEquals(Cell.RED, tb.getForegroundColorAt(0, 1));   // B written with RED
        }
    }

    // writeTextOnLine

    @Nested
    class WriteTextOnLine {

        @Test
        void writeSingleCharacter() {
            tb.writeTextOnLine("A");
            assertEquals('A', tb.getCharAt(0, 0));
        }

        @Test
        void writeAdvancesCursorByTextLength() {
            tb.writeTextOnLine("ABC");
            assertEquals(3, tb.getCursorPositionX());
            assertEquals(0, tb.getCursorPositionY());
        }

        @Test
        void writeDoesNotWrapToNextLine() {
            tb.writeTextOnLine("ABCDEFGH");
            assertEquals(1, tb.getCursorPositionY());
        }

        @Test
        void writeOverridesExistingContent() {
            tb.writeTextOnLine("AAAAA");
            tb.setCursorPosition(0, 0);
            tb.writeTextOnLine("BB");
            assertEquals('B', tb.getCharAt(0, 0));
            assertEquals('B', tb.getCharAt(0, 1));
            assertEquals('A', tb.getCharAt(0, 2));
        }

        @Test
        void writeFromMidLine() {
            tb.writeTextOnLine("AAAAA");
            tb.setCursorPosition(0, 2);
            tb.writeTextOnLine("BC");
            assertEquals('A', tb.getCharAt(0, 0));
            assertEquals('A', tb.getCharAt(0, 1));
            assertEquals('B', tb.getCharAt(0, 2));
            assertEquals('C', tb.getCharAt(0, 3));
        }

        @Test
        void writeEmptyStringDoesNotMoveCursor() {
            tb.writeTextOnLine("");
            assertEquals(0, tb.getCursorPositionX());
            assertEquals(0, tb.getCursorPositionY());
        }
    }

    // insertTextOnLine

    @Nested
    class InsertTextOnLine {

        @Test
        void insertShiftsExistingCharsRight() {
            tb.writeTextOnLine("ABCDE");
            tb.setCursorPosition(0, 1);
            tb.insertTextOnLine("XY");
            assertEquals('A', tb.getCharAt(0, 0));
            assertEquals('X', tb.getCharAt(0, 1));
            assertEquals('Y', tb.getCharAt(0, 2));
            assertEquals('B', tb.getCharAt(0, 3));
            assertEquals('C', tb.getCharAt(0, 4));
        }

        @Test
        void insertWrapsDisplacedCharsToNextLine() {
            tb.writeTextOnLine("ABCDE");
            tb.setCursorPosition(0, 0);
            tb.insertTextOnLine("XY");
            assertEquals('D', tb.getCharAt(1, 0));
            assertEquals('E', tb.getCharAt(1, 1));
        }

    }

    // fillLineWithCharacter

    @Nested
    class FillLine {

        @Test
        void fillsEntireCurrentRowWithCharacter() {
            tb.fillLineWithCharacter('*');
            for (int col = 0; col < 5; col++) {
                assertEquals('*', tb.getCharAt(0, col));
            }
        }

        @Test
        void fillDoesNotAffectOtherRows() {
            tb.fillLineWithCharacter('*');
            for (int col = 0; col < 5; col++) {
                assertEquals('\u0000', tb.getCharAt(1, col));
                assertEquals('\u0000', tb.getCharAt(2, col));
            }
        }

        @Test
        void fillOnNonZeroRow() {
            tb.setCursorPosition(1, 0);
            tb.fillLineWithCharacter('Z');
            for (int col = 0; col < 5; col++) {
                assertEquals('Z', tb.getCharAt(1, col));
            }
        }
    }

    // Scrollback

    @Nested
    class Scrollback {

        @Test
        void lineScrollsIntoScrollbackWhenScreenFills() {
            tb.writeTextOnLine("AAAAA");
            tb.writeTextOnLine("\nBBBBB");
            tb.writeTextOnLine("\nCCCCC");
            assertEquals('A', tb.getCharAt(0, 0));
        }

        @Test
        void scrollbackRespectsMaxSize() {
            TerminalBuffer small = new TerminalBuffer(2, 5, 2);
            for (int i = 0; i < 5; i++) {
                small.writeTextOnLine("LINE" + i);
                small.insertEmptyLineAtBottom();
            }
            assertTrue(small.getEntireScreenAndScrollback().split("\n").length <= 5); // 2 scrollback + separator + 2 screen
        }

        @Test
        void scrollbackLinesAreImmutable() {
            tb.writeTextOnLine("AAAAA");
            tb.insertEmptyLineAtBottom();
            tb.writeTextOnLine("BBBBB");
            assertEquals('A', tb.getCharAt(0, 0));
        }

        @Test
        void insertEmptyLineAtBottomAddsBlankRow() {
            tb.writeTextOnLine("AAAAA");
            tb.insertEmptyLineAtBottom();
            String screen = tb.getEntireScreen();
            assertNotNull(screen);
        }
    }

    // Clear operations

    @Nested
    class ClearOperations {

        @Test
        void clearEntireScreenErasesAllCells() {
            tb.writeTextOnLine("AAAAA");
            tb.clearEntireScreen();
            for (int col = 0; col < 5; col++) {
                assertEquals('\u0000', tb.getCharAt(0, col));
            }
        }

        @Test
        void clearEntireScreenPreservesScrollback() {
            tb.writeTextOnLine("AAAAA");
            tb.insertEmptyLineAtBottom();
            tb.clearEntireScreen();
            assertEquals('A', tb.getCharAt(0, 0));
        }

        @Test
        void clearScreenAndScrollbackErasesEverything() {
            tb.writeTextOnLine("AAAAA");
            tb.insertEmptyLineAtBottom();
            tb.clearScreenAndScrollback();
            for (int col = 0; col < 5; col++) {
                assertEquals('\u0000', tb.getCharAt(0, col));
            }
            String full = tb.getEntireScreenAndScrollback();
            assertFalse(full.contains("A"));
        }
    }

    // Content access

    @Nested
    class ContentAccess {

        @Test
        void getCharAtReturnsCorrectCharacter() {
            tb.writeTextOnLine("ABC");
            assertEquals('A', tb.getCharAt(0, 0));
            assertEquals('B', tb.getCharAt(0, 1));
            assertEquals('C', tb.getCharAt(0, 2));
        }

        @Test
        void getCharAtOutOfBoundsThrows() {
            assertThrows(ArgumentsOutOfRangeException.class, () -> tb.getCharAt(-1, 0));
            assertThrows(ArgumentsOutOfRangeException.class, () -> tb.getCharAt(0, -1));
            assertThrows(ArgumentsOutOfRangeException.class, () -> tb.getCharAt(0, 5));
        }

        @Test
        void getLineReturnsCorrectString() {
            tb.writeTextOnLine("ABC");
            String line = tb.getLine(0);
            assertTrue(line.startsWith("ABC"));
        }

        @Test
        void getLineOutOfBoundsThrows() {
            assertThrows(ArgumentsOutOfRangeException.class, () -> tb.getLine(-1));
            assertThrows(ArgumentsOutOfRangeException.class, () -> tb.getLine(100));
        }

        @Test
        void getEntireScreenContainsWrittenText() {
            tb.writeTextOnLine("HELLO");
            String screen = tb.getEntireScreen();
            assertTrue(screen.contains("HELLO"));
        }

        @Test
        void getEntireScreenAndScrollbackContainsBothParts() {
            tb.writeTextOnLine("AAAAA");
            tb.insertEmptyLineAtBottom();
            tb.writeTextOnLine("BBBBB");
            String full = tb.getEntireScreenAndScrollback();
            assertTrue(full.contains("AAAAA"));
            assertTrue(full.contains("BBBBB"));
        }

        @Test
        void getAttributesAtScrollbackRow() {
            tb.setCurrentForegroundColor(Cell.RED);
            tb.writeTextOnLine("AAAAA");
            tb.insertEmptyLineAtBottom();
            assertEquals(Cell.RED, tb.getForegroundColorAt(0, 0));
        }
    }

    // Special characters

    @Nested
    class SpecialCharacters {

        @Test
        void newlineMovesToNextRow() {
            tb.writeTextOnLine("A\nB");
            tb.insertTextOnLine("\nB");
            assertEquals(1, tb.getCursorPositionY());
        }

        @Test
        void carriageReturnMovesToColumnZero() {
            tb.writeTextOnLine("ABC");
            tb.insertTextOnLine("\r");
            assertEquals(0, tb.getCursorPositionX());
            assertEquals(0, tb.getCursorPositionY()); // row unchanged
        }
    }

    // Resize

    @Nested
    class Resize {

        @Test
        void resizeToLargerPreservesContent() {
            tb.writeTextOnLine("HELLO");
            TerminalBuffer resized = tb.changeScreenSize(10, 5);
            assertEquals('H', resized.getCharAt(0, 0));
            assertEquals('E', resized.getCharAt(0, 1));
        }

        @Test
        void resizeToSmallerPreservesContent() {
            tb.writeTextOnLine("AB");
            TerminalBuffer resized = tb.changeScreenSize(3, 2);
            assertEquals('A', resized.getCharAt(0, 0));
            assertEquals('B', resized.getCharAt(0, 1));
        }

        @Test
        void resizeWithZeroWidthReturnsOriginal() {
            TerminalBuffer result = tb.changeScreenSize(0, 3);
            assertSame(tb, result);
        }

        @Test
        void resizeWithZeroHeightReturnsOriginal() {
            TerminalBuffer result = tb.changeScreenSize(5, 0);
            assertSame(tb, result);
        }

        @Test
        void resizePreservesScrollback() {
            tb.writeTextOnLine("AAAAA");
            tb.insertEmptyLineAtBottom();
            TerminalBuffer resized = tb.changeScreenSize(10, 5);
            assertEquals('A', resized.getCharAt(0, 0));
        }

        @Test
        void resizeWarpsText() {
            tb.writeTextOnLine("AAAAA");
            TerminalBuffer resized = tb.changeScreenSize(3, 3);
            assertEquals('A', resized.getCharAt(1, 0));
            assertEquals('A', resized.getCharAt(1, 1));
        }

        @Test
        void resizeKeepsCursorAtCharacterItWasOn() {
            tb.writeTextOnLine("ABCDE");
            tb.setCursorPosition(0, 2);
            TerminalBuffer resized = tb.changeScreenSize(3, 3);
            assertEquals('C', resized.getCharAt(resized.getCursorPositionY(), resized.getCursorPositionX()));
        }

    }

    // Edge cases and boundary conditions

    @Nested
    class EdgeCases {

        @Test
        void writeExactlyScreenWidth() {
            tb.writeTextOnLine("ABCDE"); // width = 5
            assertEquals('E', tb.getCharAt(0, 4));
            assertEquals(1, tb.getCursorPositionY());
        }

        @Test
        void singleCellBuffer() {
            TerminalBuffer tiny = new TerminalBuffer(5, 1, 1);
            tiny.writeTextOnLine("A");
            assertEquals('A', tiny.getCharAt(0, 0));
        }

        @Test
        void zeroScrollbackEvictsImmediately() {
            TerminalBuffer noScrollback = new TerminalBuffer(0, 5, 2);
            noScrollback.writeTextOnLine("AAAAA");
            noScrollback.insertEmptyLineAtBottom();
            noScrollback.writeTextOnLine("BBBBB");
            String full = noScrollback.getEntireScreenAndScrollback();
            assertFalse(full.contains("AAAAA"));
        }

        @Test
        void moveCursorByZeroDoesNothing() {
            tb.moveCursorRight(0);
            tb.moveCursorDown(0);
            assertEquals(0, tb.getCursorPositionX());
            assertEquals(0, tb.getCursorPositionY());
        }

        @Test
        void overwritingWithSameCharPreservesAttributes() {
            tb.setCurrentForegroundColor(Cell.RED);
            tb.writeTextOnLine("A");
            tb.setCursorPosition(0, 0);
            tb.setCurrentForegroundColor(Cell.BLUE);
            tb.writeTextOnLine("A");
            assertEquals(Cell.BLUE, tb.getForegroundColorAt(0, 0));
        }

        @Test
        void multipleScrollsPreserveScrollbackOrder() {
            tb.writeTextOnLine("11111");
            tb.insertEmptyLineAtBottom();
            tb.writeTextOnLine("22222");
            tb.insertEmptyLineAtBottom();
            assertEquals('1', tb.getCharAt(0, 0));
            assertEquals('2', tb.getCharAt(1, 0));
        }
    }
}
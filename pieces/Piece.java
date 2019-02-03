package chess.pieces;
import chess.*;
import mytools.Coordinates;
import java.util.Set;
import java.io.*;

public abstract class Piece {

    public final boolean m_isWhite;
    public Type m_type;
    public Coordinates m_rc;
    protected final Board m_board;
    protected Coordinates m_tempRC;
    protected int[][] m_valueByPosition;
    protected int m_power;
    protected final int m_factor;

    /** Constructor */
    public Piece(Board board, Coordinates rc, boolean isWhite) {
        m_board = board;
        m_isWhite = isWhite;
        m_factor = isWhite ? 1 : -1;
        m_rc = rc;
        m_valueByPosition = new int[Board.ROWS][Board.COLS];
    }

    /** Copy constructor for piece on another board */
    public Piece(Board board, Piece otherPiece) {
        this(board, otherPiece.m_rc, otherPiece.m_isWhite);
        m_type = otherPiece.m_type;
        m_power = otherPiece.m_power;
        for (byte row = 0; row < Board.ROWS; row++) {
            for (byte col = 0; col < Board.COLS; col++) {
                m_valueByPosition[row][col] = otherPiece.m_valueByPosition[row][col];
            }
        }
    }

    /** Reads the file given filename to inputs into 2D array m_valueByPosition */
    protected void ReadFileToPositionValues(String fileName) {
        BufferedReader br = null;
        String dir = "/Users/Tim/OneDrive/ProgrammingProjects/Java/chess/pieces/";
        try {
            br = new BufferedReader(new FileReader(dir + fileName));
            for (byte row = 0; row < Board.ROWS; row++) {
                String line = br.readLine();
                String[] values = line.split(",");
                for (byte col = 0; col < Board.COLS; col++) {
                    m_valueByPosition[row][col] = Integer.parseInt(values[col].trim());
                }
            }
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        } finally {
            try {
                if (br != null) br.close();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

    /** Returns true if the given coordinate is outside the board or the same as original position */
    protected boolean outOfBounds(Coordinates rc) {
        return (rc.equals(m_tempRC) || rc.m_row < 0 || rc.m_col < 0 
            ||  rc.m_row >= Board.ROWS || rc.m_col >= Board.COLS);
    }

    /** Checks whether the given coordinate is already occupied by your own piece */
    protected boolean occupiedBySelf(Coordinates rc) {
        Piece otherPiece = m_board.getPieceAtCoordinate(rc);
        return (otherPiece != null && otherPiece.m_isWhite == m_isWhite);
    }

    /** Checks whether the given coordinate is already occupied by an enemy piece */
    protected boolean occupiedByEnemy(Coordinates rc) {
        Piece otherPiece = m_board.getPieceAtCoordinate(rc);
        return (otherPiece != null && otherPiece.m_isWhite != m_isWhite);
    }

    /** If out of bounds or occupied by self returns false, otherwise adds 
     *  the coordinate rc to validMoves if king is not checked at the new move
     *  A version of some piece's getValidMoves will always be in the call stack */
    protected boolean addMoveSucceeded(Set<Coordinates> validMoves, Coordinates rc, boolean toMovePiece) {
        if (outOfBounds(rc) || occupiedBySelf(rc)) return false;
        if (toMovePiece) {
            Piece pieceReplaced = m_board.moveSelectedPiece(m_tempRC, rc, false);
            if (!m_board.isKingChecked(m_isWhite)) validMoves.add(rc);
            m_board.resetBoardToStartOfTurn(pieceReplaced, m_tempRC, rc);
        } else {
            validMoves.add(rc);
        }
        return true;
    }

    /** Takes in an incrementer by row and col (eg -1, 0, or 1) and adds 
     *  coordinates in that direction until the edge of the board or another piece is encountered */
    protected void addValidPath(Set<Coordinates> validMoves, boolean toMovePiece, int rowIncr, int colIncr) {
        int row = m_tempRC.m_row + rowIncr;
        int col = m_tempRC.m_col + colIncr;
        while (true) {
            Coordinates rc = new Coordinates(row, col);
            if (!addMoveSucceeded(validMoves, rc, toMovePiece) || occupiedByEnemy(rc)) break;
            row += rowIncr;
            col += colIncr;
        }
    }

    /** Adds all valid moves by this piece into set of validMoves */
    public abstract void getValidMoves(Set<Coordinates> validMoves, boolean toMovePiece);

    /** Updates variables when this piece is moved (does nothing by default) */
    public void updateWhenMoved() {}

    /** Gets the value of the position of this piece */
    private int getPositionValue(Coordinates rc) {
        int row = (m_isWhite) ? rc.m_row : Board.ROWS - 1 - rc.m_row;
        return m_factor*m_valueByPosition[row][rc.m_col];
    }

    /** Gets the value of the power of this piece */
    public int getPowerValue() {
        return m_power*m_factor + getPositionValue(m_rc);
    }


    public boolean equals(Object obj) {
        if (obj == null) return false;
        if (!(obj instanceof Piece)) return false;
        Piece piece = (Piece) obj;
        return m_isWhite == piece.m_isWhite && m_type == piece.m_type;
    }

    /** Paints whether piece is white or black on the board */
    public void paint() {
        /*char playerSymbol = m_isWhite ?  'w' : 'b';
        System.out.print(playerSymbol);*/
    }
}
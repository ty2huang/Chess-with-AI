package chess.pieces;
import chess.*;
import mytools.Coordinates;
import java.util.Set;

public class King extends Piece {

    public boolean m_canLeftCastle;
    public boolean m_canRightCastle;

    /** Constructor */
    public King(Board board, Coordinates rc, boolean isWhite) {
        super(board, rc, isWhite);
        m_type = Type.KING;
        m_power = 20000;
        ReadFileToPositionValues("King.txt");
        m_canLeftCastle = true;
        m_canRightCastle = true;
    }

    /** Copy constructor */
    public King(Board board, Piece otherPiece) {
        super(board, otherPiece);
        King otherKing = (King) otherPiece;
        m_canLeftCastle = otherKing.m_canLeftCastle;
        m_canRightCastle = otherKing.m_canRightCastle;
    }

    /** Adds castling coordinates (two spaces from king) if all conditions are met
     *  Only used by King's getValidMoves(Set<Coordinates>, boolean) method*/
    private void addCastlingMoves(Set<Coordinates> validMoves, boolean toTheRight) {
        int direction = toTheRight ? 1 : -1;
        Coordinates rc = new Coordinates(m_rc.m_row, m_rc.m_col+direction);
        if (validMoves.contains(rc) && !occupiedByEnemy(rc)) {
            rc = new Coordinates(m_rc.m_row, m_rc.m_col+2*direction);
            if (!occupiedBySelf(rc) && !occupiedByEnemy(rc)) {
                addMoveSucceeded(validMoves, rc, true);
            }
        }
    }

    /** Adds all valid moves by this piece into set of validMoves */
    public void getValidMoves(Set<Coordinates> validMoves, boolean toMovePiece) {
        m_tempRC = m_rc;
        for (byte i = - 1; i <= 1; i++) {
            int row = m_tempRC.m_row + i;
            for (byte j = -1; j <= 1; j++) {
                int col = m_tempRC.m_col + j;
                addMoveSucceeded(validMoves, new Coordinates(row, col), toMovePiece);
            }
        }
        if (toMovePiece && !m_board.isKingChecked(m_isWhite)) {
            if (m_canLeftCastle) addCastlingMoves(validMoves, false);
            if (m_canRightCastle) addCastlingMoves(validMoves, true);
        }
        m_rc = m_tempRC;
    }

    /** Updates variables when this piece is moved */
    public void updateWhenMoved() {
        m_canLeftCastle = false;
        m_canRightCastle = false;
    }

    /** Paints this piece on the board */
    public void paint() {
        super.paint();
        String utf8Sym = (m_isWhite) ? "\u265A" : "\u2654";
        System.out.print(utf8Sym);
    }
}
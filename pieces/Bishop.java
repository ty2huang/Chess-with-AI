package chess.pieces;
import chess.*;
import mytools.Coordinates;
import java.util.Set;

public class Bishop extends Piece {

    /** Constructor */
    public Bishop(Board board, Coordinates rc, boolean isWhite) {
        super(board, rc, isWhite);
        m_type = Type.BISHOP;
    }

    /** Copy constructor */
    public Bishop(Board board, Piece otherPiece) {
        super(board, otherPiece);
        m_type = Type.BISHOP;
    }

    /** Adds all valid moves by this piece into set of validMoves */
    public void getValidMoves(Set<Coordinates> validMoves, boolean toMovePiece) {
        m_tempRC = m_rc;
        for (byte i = -1; i <= 1; i += 2) {
            for (byte j = -1; j <= 1; j += 2) {
                addValidPath(validMoves, toMovePiece, i, j);
            }
        }
        m_rc = m_tempRC;
    }

    /** Gets the value of this piece */
    public int getPowerValue() {
        int factor = m_isWhite ? 1 : -1;
        return 30*factor;
    }

    /** Paints this piece on the board */
    public void paint() {
        super.paint();
        System.out.print("B");
    }
}
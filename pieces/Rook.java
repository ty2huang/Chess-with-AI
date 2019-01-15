package chess.pieces;
import chess.*;
import mytools.Coordinates;
import java.util.Set;

public class Rook extends Piece {

    private final boolean m_isRight;
    private boolean firstMove;

    /** Constructor */
    public Rook(Board board, Coordinates rc, boolean isWhite) {
        super(board, rc, isWhite);
        m_type = Type.ROOK;
        m_isRight = (rc.m_col == 0) ? false : true;
        firstMove = true;
    }

    /** Adds all valid moves by this piece into set of validMoves */
    public void getValidMoves(Set<Coordinates> validMoves, boolean toMovePiece) {
        m_tempRC = m_rc;
        for (byte i = -1; i <= 1; i += 2) {
            addValidPath(validMoves, toMovePiece, i, 0);
            addValidPath(validMoves, toMovePiece, 0, i);
        }
        m_rc = m_tempRC;
    }

    /** Updates variables when this piece is moved */
    public void updateWhenMoved() {
        if (firstMove) {
            m_board.restrictCastling(m_isWhite, m_isRight);
            firstMove = false;
        }
    }

    /** Paints this piece on the board */
    public void paint() {
        super.paint();
        System.out.print("R");
    }
}
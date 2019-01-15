package chess.pieces;
import chess.*;
import mytools.Coordinates;
import java.util.Set;

public class Pawn extends Piece {

    private boolean m_firstMove;
    private final int m_dir;

    /** Constructor */
    public Pawn(Board board, Coordinates rc, boolean isWhite) {
        super(board, rc, isWhite);
        m_type = Type.PAWN;
        m_firstMove = true;
        m_dir = m_isWhite ? -1 : 1;
    }

    /** Adds all valid moves by this piece into set of validMoves */
    public void getValidMoves(Set<Coordinates> validMoves, boolean toMovePiece) {
        m_tempRC = m_rc;
        Coordinates rc = new Coordinates(m_tempRC.m_row + m_dir, m_tempRC.m_col);
        if (toMovePiece) {
            if (m_board.getPieceAtCoordinate(rc) == null) {
                addMoveSucceeded(validMoves, rc, toMovePiece);
                if (m_firstMove) {
                    Coordinates rc2 = new Coordinates(m_tempRC.m_row + 2*m_dir, m_tempRC.m_col);
                    if (m_board.getPieceAtCoordinate(rc2) == null) {
                        addMoveSucceeded(validMoves, rc2, toMovePiece);
                    }
                }
            }
        }
        for (byte j = 0; j < 2; j++) {
            rc = new Coordinates(m_tempRC.m_row + m_dir, m_tempRC.m_col - 1 + 2*j);
            if (!outOfBounds(rc)) {
                if (occupiedByEnemy(rc) || rc.equals(m_board.m_enPassant)) {
                    addMoveSucceeded(validMoves, rc, toMovePiece);
                }
            }
        }
        m_rc = m_tempRC;
    }

    /** Updates variables when this piece is moved */
    public void updateWhenMoved() {
        if (m_firstMove && (m_rc.m_row == 3 || m_rc.m_row == 4)) {
            m_board.m_enPassant = new Coordinates(m_rc.m_row - m_dir, m_rc.m_col);
        }
        m_firstMove = false;
        m_board.turnsSinceLastCapture = 0;
    }

    /** Paints this piece on the board */
    public void paint() {
        super.paint();
        System.out.print("p");
    }
}
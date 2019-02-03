package chess.pieces;
import chess.*;
import mytools.Coordinates;
import java.util.Set;

public class Pawn extends Piece {

    private boolean m_firstMove;

    /** Constructor */
    public Pawn(Board board, Coordinates rc, boolean isWhite) {
        super(board, rc, isWhite);
        m_type = Type.PAWN;
        m_power = 100;
        ReadFileToPositionValues("Pawn.txt");
        m_firstMove = true;
    }

    /** Copy constructor */
    public Pawn(Board board, Piece otherPiece) {
        super(board, otherPiece);
        Pawn otherPawn = (Pawn) otherPiece;
        m_firstMove = otherPawn.m_firstMove;
    }

    /** Adds all valid moves by this piece into set of validMoves */
    public void getValidMoves(Set<Coordinates> validMoves, boolean toMovePiece) {
        m_tempRC = m_rc;
        Coordinates rc = new Coordinates(m_tempRC.m_row - m_factor, m_tempRC.m_col);
        if (toMovePiece) {
            if (m_board.getPieceAtCoordinate(rc) == null) {
                addMoveSucceeded(validMoves, rc, toMovePiece);
                if (m_firstMove) {
                    Coordinates rc2 = new Coordinates(m_tempRC.m_row - 2*m_factor, m_tempRC.m_col);
                    if (m_board.getPieceAtCoordinate(rc2) == null) {
                        addMoveSucceeded(validMoves, rc2, toMovePiece);
                    }
                }
            }
        }
        for (byte j = 0; j < 2; j++) {
            rc = new Coordinates(m_tempRC.m_row - m_factor, m_tempRC.m_col - 1 + 2*j);
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
            m_board.m_enPassant = new Coordinates(m_rc.m_row + m_factor, m_rc.m_col);
            m_board.m_oneTurnSinceEnPassant = true;
        }
        m_firstMove = false;
        m_board.m_turnsSinceLastCapture = -1;
    }

    /** Paints this piece on the board */
    public void paint() {
        super.paint();
        String utf8Sym = (m_isWhite) ? "\u265F" : "\u2659";
        System.out.print(utf8Sym);
    }
}
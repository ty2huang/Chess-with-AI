package chess.pieces;
import chess.*;
import mytools.Coordinates;
import java.util.Set;

public class Knight extends Piece {

    /** Constructor */
    public Knight(Board board, Coordinates rc, boolean isWhite) {
        super(board, rc, isWhite);
        m_type = Type.KNIGHT;
    }

    /** Adds all valid moves by this piece into set of validMoves */
    public void getValidMoves(Set<Coordinates> validMoves, boolean toMovePiece) {
        m_tempRC = m_rc;
        for (byte i = 0; i < 2; i++) {
            int row = m_tempRC.m_row - 2 + 4*i;
            for (byte j = 0; j < 2; j++) {
                int col = m_tempRC.m_col - 1 + 2*j;
                addMoveSucceeded(validMoves, new Coordinates(row, col), toMovePiece);
            }
            row = m_tempRC.m_row - 1 + 2*i;
            for (byte j = 0; j < 2; j++) {
                int col = m_tempRC.m_col - 2 + 4*j;
                addMoveSucceeded(validMoves, new Coordinates(row, col), toMovePiece);
            }
        }
        m_rc = m_tempRC;
    }

    /** Paints this piece on the board */
    public void paint() {
        super.paint();
        System.out.print("N");
    }
}
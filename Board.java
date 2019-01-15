package chess;

import chess.pieces.*;

import java.util.*;
import mytools.Coordinates;

public class Board {

    public static final byte ROWS = 8;
    public static final byte COLS = 8;
    public Coordinates m_enPassant;
    public boolean m_whiteTurn;
    public int turnsSinceLastCapture;

    private Piece[][] m_grid;
    private Map<Boolean, Map<Coordinates, Piece>> m_allPieces; 
    private Map<Boolean, King> m_kings;
    private Set<Coordinates> m_validMoves;
    private Coordinates m_currRC;

    /** Constructor for Board */
    public Board() {
        m_grid = new Piece[ROWS][COLS];
        m_allPieces = new HashMap<Boolean, Map<Coordinates, Piece>>();
        m_allPieces.put(true, new HashMap<Coordinates, Piece>());
        m_allPieces.put(false, new HashMap<Coordinates, Piece>());
        m_kings = new HashMap<Boolean, King>();
        m_validMoves = new HashSet<Coordinates>();
    }

    /** A factory for making a new chess piece dependent on coordinate
     *  Used for init() */
    private Piece makeNewPiece(Coordinates rc) {
        Piece piece = null;
        boolean isWhite = (rc.m_row < 2) ? false : true;
        if (rc.m_row == 1 || rc.m_row == ROWS - 2) {
            piece = new Pawn(this, rc, isWhite);
        } else if (rc.m_row == 0 || rc.m_row == ROWS - 1) {
            switch (rc.m_col) {
                case 0:
                case COLS - 1:
                    piece = new Rook(this, rc, isWhite);
                    break;
                case 1:
                case COLS - 2:
                    piece = new Knight(this, rc, isWhite);
                    break;
                case 2:
                case COLS - 3:
                    piece = new Bishop(this, rc, isWhite);
                    break;
                case 3:
                    piece = new Queen(this, rc, isWhite);
                    break;
                case 4:
                    m_kings.put(isWhite, new King(this, rc, isWhite));
                    piece = m_kings.get(isWhite);
                    break;
            }
        }
        return piece;
    }

    /** Sets all the pieces back to starting position */
    public void init() {
        m_allPieces.get(true).clear();
        m_allPieces.get(false).clear();
        m_kings.clear();
        m_currRC = null;
        m_enPassant = null;
        m_validMoves.clear();
        m_whiteTurn = true;
        turnsSinceLastCapture = 0;
        for (byte row = 0; row < ROWS; row++) {
            for (byte col = 0; col < COLS; col++) {
                Coordinates rc = new Coordinates(row, col);
                setPieceAtCoordinate(makeNewPiece(rc), rc);
            }
        }
    }
    
    /** Get the chess piece at given coordinate */
    public Piece getPieceAtCoordinate(Coordinates rc) {
        return m_grid[rc.m_row][rc.m_col];
    }

    /** Sets the chess piece at given coordinate (and removing previous piece there)
     *  Pass in null to new piece to remove any piece at given coordinate */
    public void setPieceAtCoordinate(Piece newPiece, Coordinates rc) {
        Piece piece = getPieceAtCoordinate(rc);
        if (piece != null) m_allPieces.get(piece.m_isWhite).remove(rc);
        m_grid[rc.m_row][rc.m_col] = newPiece;
        if (newPiece != null) {
            m_allPieces.get(newPiece.m_isWhite).put(rc, newPiece);
            newPiece.m_rc = rc;
        }
    }

    /** Returns set of all coordinates that the selected piece can move to
     *  Takes into account player will be checked or if pieces are in the way */
    public Set<Coordinates> getAllValidMoves(Coordinates rc) {
        m_currRC = rc;
        m_validMoves.clear();
        getPieceAtCoordinate(rc).getValidMoves(m_validMoves, true);
        return m_validMoves;
    }

    /** Checks if the given coloured king is under check */
    public boolean isKingChecked(boolean isWhite) {
        King currKing = m_kings.get(isWhite);
        List<Piece> pieces = new ArrayList<Piece>(m_allPieces.get(!isWhite).values());
        for (Piece piece : pieces) {
            Set<Coordinates> validMoves = new HashSet<Coordinates>();
            piece.getValidMoves(validMoves, false);
            if (validMoves.contains(currKing.m_rc)) return true;
        }
        return false;
    }

    /** Moves the rook on the other side of king when castling
     *  Only used in moveSelectedPiece(Coordinates) */
    private void castlingRook(int row, boolean onTheRight) {
        int factor = onTheRight ? 1 : 0;
        Coordinates oldRC = new Coordinates(row, (COLS-1)*factor);
        Piece rook = getPieceAtCoordinate(oldRC);
        Coordinates newRC = new Coordinates(row, 2*factor + 3);
        setPieceAtCoordinate(rook, newRC);
        setPieceAtCoordinate(null, oldRC);
    }

    /** Prevents castling with given rook after rook is moved
     *  Only used in updateWhenMoved */
    public void restrictCastling(boolean isWhite, boolean isRight) {
        if (isRight) {
            m_kings.get(isWhite).m_canRightCastle = false;
        } else {
            m_kings.get(isWhite).m_canLeftCastle = false;
        }
    }

    /** Moves the piece used for both actual move and validity check move
     *  Sets member variables for actual move */
    public Piece moveSelectedPiece(Coordinates startRC, Coordinates finalRC, boolean actualMove) {
        Piece pieceToRemove = getPieceAtCoordinate(finalRC);
        if (actualMove) {
            turnsSinceLastCapture++;
            if (pieceToRemove != null) turnsSinceLastCapture = 0;
        }

        Piece piece = getPieceAtCoordinate(startRC);
        setPieceAtCoordinate(null, startRC);
        setPieceAtCoordinate(piece, finalRC);
        if (actualMove) piece.updateWhenMoved();
        
        // if castling occurs
        if (piece.m_type == Type.KING) {
            if (startRC.m_col - finalRC.m_col == 2) {
                castlingRook(finalRC.m_row, false);
            } else if (finalRC.m_col - startRC.m_col == 2) {
                castlingRook(finalRC.m_row, true);
            }
        }

        // if en passant occurs
        if (m_enPassant != null) {
            int row = (m_enPassant.m_row == 2) ? 3 : ROWS - 4;
            Coordinates rc = new Coordinates(row, m_enPassant.m_col);
            if (m_whiteTurn != getPieceAtCoordinate(rc).m_isWhite) {
                if (piece.m_type == Type.PAWN && finalRC.equals(m_enPassant)) {
                    pieceToRemove = getPieceAtCoordinate(rc);
                    setPieceAtCoordinate(null, rc);
                }
                if (actualMove) m_enPassant = null;
            }
        } 
        
        if (actualMove) {
            m_currRC = finalRC;
        }
        return pieceToRemove;
    }


    /** Moves the rook on the other side of king when castling
     *  Only used in moveSelectedPiece(Coordinates) */
    private void uncastlingRook(int row, boolean onTheRight) {
        int factor = onTheRight ? 1 : 0;
        Coordinates oldRC = new Coordinates(row, 2*factor + 3);
        Piece rook = getPieceAtCoordinate(oldRC);
        Coordinates newRC = new Coordinates(row, (COLS-1)*factor);
        setPieceAtCoordinate(rook, newRC);
        setPieceAtCoordinate(null, oldRC);
    }

    /** Resets the board back to start of turn during validity checks */
    public void resetBoardToStartOfTurn(Piece pieceRemoved, Coordinates startRC, Coordinates finalRC) {
        Piece piece = getPieceAtCoordinate(finalRC);
        setPieceAtCoordinate(null, finalRC);
        setPieceAtCoordinate(piece, startRC);
        if (pieceRemoved != null) setPieceAtCoordinate(pieceRemoved, pieceRemoved.m_rc);

        // undo castling rook
        if (piece.m_type == Type.KING) {
            if (startRC.m_col - finalRC.m_col == 2) {
                uncastlingRook(finalRC.m_row, false);
            } else if (finalRC.m_col - startRC.m_col == 2) {
                uncastlingRook(finalRC.m_row, true);
            }
        }
    }


    public boolean pawnReachedEnd() {
        Piece piece = getPieceAtCoordinate(m_currRC);
        return (piece.m_type == Type.PAWN) 
            && (m_currRC.m_row == 0 || m_currRC.m_row == ROWS - 1);
    }


    public void evolvePawn(char newPiece) {
        Piece piece = null;
        switch (newPiece) {
            case 'R':
                piece = new Rook(this, m_currRC, m_whiteTurn);
                break;
            case 'N':
                piece = new Knight(this, m_currRC, m_whiteTurn);
                break;
            case 'B':
                piece = new Bishop(this, m_currRC, m_whiteTurn);
                break;
            case 'Q':
                piece = new Queen(this, m_currRC, m_whiteTurn);
                break;
        }
        setPieceAtCoordinate(piece, m_currRC);
    }


    private boolean noValidMovesLeft(boolean isWhite) {
        List<Piece> pieces = new ArrayList<Piece>(m_allPieces.get(isWhite).values());
        for (Piece piece : pieces) {
            Set<Coordinates> validMoves = new HashSet<Coordinates>();
            piece.getValidMoves(validMoves, true);
            if (!validMoves.isEmpty()) return false;
        }
        return true;
    }


    public boolean hasWon() {
        return (isKingChecked(!m_whiteTurn) && noValidMovesLeft(!m_whiteTurn));
    }


    public boolean isDraw() {
        return (turnsSinceLastCapture >= 50 || noValidMovesLeft(!m_whiteTurn));
    }

    /** Paints the board (see comment for GameMain class for design) */
    public void paint(boolean markValidMoves) {
        String dashes = "---------------------------------------";
        System.out.println();
        System.out.println("   " + dashes);
        for (byte row = 0; row < ROWS; row++) {
            System.out.print(ROWS - row + " |");
            for (byte col = 0; col < COLS; col++) {
                Coordinates rc = new Coordinates(row, col);
                char cellPadding = ' ';
                if (markValidMoves && m_validMoves.contains(rc)) {
                    cellPadding = '-';
                }
                System.out.print(cellPadding);
                if (getPieceAtCoordinate(rc) != null) {
                    getPieceAtCoordinate(rc).paint();
                } else {
                    System.out.print("  ");
                }
                System.out.print(cellPadding);
                System.out.print("|");
            }
            System.out.println();
            if (row < ROWS - 1) System.out.println("  |" + dashes + "|");
            else System.out.println("   " + dashes);
        }
        System.out.print("  ");
        for (byte col = 0; col < COLS; col++) {
            char currLetter = (char) ('A' + col);
            System.out.print("  " + currLetter + "  ");
        }
        System.out.println(); System.out.println();
    }
}
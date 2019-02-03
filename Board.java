package chess;

import chess.pieces.*;

import java.util.*;
import mytools.Coordinates;

public class Board {

    public static final byte ROWS = 8;
    public static final byte COLS = 8;
    public Coordinates m_enPassant;
    public boolean m_oneTurnSinceEnPassant;
    public boolean m_whiteTurn;
    public int m_turnsSinceLastCapture;

    private Piece[][] m_grid;
    private Map<Boolean, Map<Coordinates, Piece>> m_allPieces; 
    private Map<Boolean, King> m_kings;
    private Set<Coordinates> m_validMoves;
    private Coordinates m_lastSelection;
    private Coordinates m_lastPlacement;
    //private int m_advantagePoints;

    /** Constructor for Board */
    public Board() {
        m_grid = new Piece[ROWS][COLS];
        m_allPieces = new HashMap<Boolean, Map<Coordinates, Piece>>();
        m_allPieces.put(true, new HashMap<Coordinates, Piece>());
        m_allPieces.put(false, new HashMap<Coordinates, Piece>());
        m_kings = new HashMap<Boolean, King>();
        m_validMoves = new HashSet<Coordinates>();
    }

    /** Copies the other piece, only used in copy constructor */
    private Piece copyPiece(Piece otherPiece) {
        Piece piece = null;
        if (otherPiece == null) return null;
        switch(otherPiece.m_type) {
            case PAWN:
                piece = new Pawn(this, otherPiece);
                break;
            case ROOK:
                piece = new Rook(this, otherPiece);
                break;
            case KNIGHT:
                piece = new Knight(this, otherPiece);
                break;
            case BISHOP:
                piece = new Bishop(this, otherPiece);
                break;
            case QUEEN:
                piece = new Queen(this, otherPiece);
                break;
            case KING:
                m_kings.put(otherPiece.m_isWhite, new King(this, otherPiece));
                piece = m_kings.get(otherPiece.m_isWhite);
                break;
        }
        return piece;
    }

    /** Copy constructor for board */
    public Board(Board otherBoard) {
        this();
        m_enPassant = otherBoard.m_enPassant;
        m_oneTurnSinceEnPassant = otherBoard.m_oneTurnSinceEnPassant;
        m_whiteTurn = otherBoard.m_whiteTurn;
        m_turnsSinceLastCapture = otherBoard.m_turnsSinceLastCapture;
        //m_advantagePoints = otherBoard.m_advantagePoints;
        m_lastSelection = otherBoard.m_lastSelection;
        m_lastPlacement = otherBoard.m_lastPlacement;
        for (byte row = 0; row < ROWS; row++) {
            for (byte col = 0; col < COLS; col++) {
                Coordinates rc = new Coordinates(row, col);
                Piece otherPiece = otherBoard.getPieceAtCoordinate(rc);
                setPieceAtCoordinate(copyPiece(otherPiece), rc);
            }
        }
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
        m_lastSelection = null;
        m_lastPlacement = null;
        m_enPassant = null;
        m_oneTurnSinceEnPassant = false;
        m_validMoves.clear();
        m_whiteTurn = true;
        m_turnsSinceLastCapture = 0;
        //m_advantagePoints = 0;
        for (byte row = 0; row < ROWS; row++) {
            for (byte col = 0; col < COLS; col++) {
                Coordinates rc = new Coordinates(row, col);
                setPieceAtCoordinate(makeNewPiece(rc), rc);
            }
        }
    }

    /** Get the points for the state of the board (positive means white is winning) */
    public int getAdvantagePoints() {
        //return m_advantagePoints;
        int advantagePoints = 0;
        List<Piece> allPieces = new ArrayList<Piece>();
        allPieces.addAll(m_allPieces.get(false).values());
        allPieces.addAll(m_allPieces.get(true).values());
        for (Piece piece : allPieces) {
            advantagePoints += piece.getPowerValue();
        }
        return advantagePoints;
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
            if (m_oneTurnSinceEnPassant) {
                m_oneTurnSinceEnPassant = false;
            } else {
                int row = (m_enPassant.m_row == 2) ? 3 : ROWS - 4;
                Coordinates rc = new Coordinates(row, m_enPassant.m_col);
                if (piece.m_type == Type.PAWN && finalRC.equals(m_enPassant)) {
                    pieceToRemove = getPieceAtCoordinate(rc);
                    setPieceAtCoordinate(null, rc);
                }
                if (actualMove) m_enPassant = null;
            }
        } 
        
        if (actualMove) {
            m_lastSelection = startRC;
            m_lastPlacement = finalRC;
            m_whiteTurn = !m_whiteTurn;
            m_turnsSinceLastCapture++;
            //m_advantagePoints += (piece.getPositionValue(finalRC) - piece.getPositionValue(startRC));
            if (pieceToRemove != null) {
                m_turnsSinceLastCapture = 0;
                //m_advantagePoints -= pieceToRemove.getPowerValue();
            }
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

    /** Returns true if the last moved piece is a pawn that reached the end of the board */
    public boolean pawnReachedEnd() {
        Piece piece = getPieceAtCoordinate(m_lastPlacement);
        return (piece.m_type == Type.PAWN) 
            && (m_lastPlacement.m_row == 0 || m_lastPlacement.m_row == ROWS - 1);
    }

    /** Given a character input, evolves the pawn into the selected piece */
    public void evolvePawn(char newPiece) {
        Piece piece = null;
        switch (newPiece) {
            case 'R':
                piece = new Rook(this, m_lastPlacement, !m_whiteTurn);
                break;
            case 'N':
                piece = new Knight(this, m_lastPlacement, !m_whiteTurn);
                break;
            case 'B':
                piece = new Bishop(this, m_lastPlacement, !m_whiteTurn);
                break;
            case 'Q':
                piece = new Queen(this, m_lastPlacement, !m_whiteTurn);
                break;
        }
        setPieceAtCoordinate(piece, m_lastPlacement);
    }

    /** Checks if the opponent has no valid moves left */
    private boolean noValidMovesLeft(boolean isWhite) {
        List<Piece> pieces = new ArrayList<Piece>(m_allPieces.get(isWhite).values());
        for (Piece piece : pieces) {
            Set<Coordinates> validMoves = new HashSet<Coordinates>();
            piece.getValidMoves(validMoves, true);
            if (!validMoves.isEmpty()) return false;
        }
        return true;
    }

    /** Checks if the current player has won */
    public boolean hasLost() {
        return (isKingChecked(m_whiteTurn) && noValidMovesLeft(m_whiteTurn));
    }

    /** Checks if the game is in a state of a draw */
    public boolean isDraw() {
        return (m_turnsSinceLastCapture >= 50 || noValidMovesLeft(m_whiteTurn));
    }

    /** Gets all possible chess moves for every piece on the board */
    public void getAllPossibleChessMoves(List<Coordinates> selection, List<Coordinates> destination) {
        List<Piece> pieces = new ArrayList<Piece>(m_allPieces.get(m_whiteTurn).values());
        for (Piece piece : pieces) {
            Set<Coordinates> validMoves = new HashSet<Coordinates>();
            piece.getValidMoves(validMoves, true);
            for (Coordinates rc : validMoves) {
                selection.add(piece.m_rc);
                destination.add(rc);
            }
        }
    }


    public boolean equals(Object obj) {
        if (obj == null) return false;
        if (!(obj instanceof Board)) return false;
        Board board = (Board) obj;
        for (int row = 0; row < ROWS; row++) {
            for (int col = 0; col < COLS; col++) {
                Coordinates rc = new Coordinates(row, col);
                Piece piece1 = getPieceAtCoordinate(rc);
                Piece piece2 = board.getPieceAtCoordinate(rc);
                if (piece1 == null && piece2 != null) return false;
                if (piece1 != null && !piece1.equals(piece2)) return false;
            }
        }
        return true;
    }

    /** Paints the board (see comment for GameMain class for design) */
    public void paint(boolean markValidMoves) {
        String dashes = "---------------------------------------";
        String colLabel = "  ";
        for (byte col = 0; col < COLS; col++) {
            char currLetter = (char) ('A' + col);
            colLabel += "  " + currLetter + "  ";
        }
        System.out.println(colLabel);
        System.out.println("   " + dashes);
        for (byte row = 0; row < ROWS; row++) {
            System.out.print((ROWS - row) + " |");
            for (byte col = 0; col < COLS; col++) {
                Coordinates rc = new Coordinates(row, col);
                char cellPadding = ' ';
                if (markValidMoves && m_validMoves.contains(rc)) {
                    cellPadding = '-';
                } else if (!markValidMoves && 
                          (rc.equals(m_lastPlacement) || rc.equals(m_lastSelection))) {
                    cellPadding = '=';
                }
                System.out.print(cellPadding);
                if (getPieceAtCoordinate(rc) != null) {
                    getPieceAtCoordinate(rc).paint();
                } else {
                    System.out.print(" ");
                }
                System.out.print(" ");
                System.out.print(cellPadding);
                System.out.print("|");
            }
            System.out.println(" " + (ROWS - row));
            if (row < ROWS - 1) System.out.println("  |" + dashes + "|");
            else System.out.println("   " + dashes);
        }
        System.out.println(colLabel); 
        System.out.println();
    }
}
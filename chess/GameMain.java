package chess;
import mytools.*;
import chess.pieces.*;

import java.util.*;

/**
 * Main game file for chess game (first enter "chcp 65001" in terminal)
 *      A    B    C    D    E    F    G    H
       ---------------------------------------
    8 | bR | bN | bB | bQ | bK | bB | bN | bR | 8
      |---------------------------------------|
    7 | bp | bp | bp | bp | bp | bp | bp | bp | 7
      |---------------------------------------|
    6 |    |    |    |    |    |    |    |    | 6
      |---------------------------------------|
    5 |    |    |    |    |    |    |    |    | 5
      |---------------------------------------|
    4 |    |    |    |    |    |    |    |    | 4
      |---------------------------------------|
    3 |    |    |    |    |    |    |    |    | 5
      |---------------------------------------|
    2 | wp | wp | wp | wp | wp | wp | wp | wp | 6
      |---------------------------------------|
    1 | wR | wN | wB | wQ | wK | wB | wN | wR | 7
       ---------------------------------------
        A    B    C    D    E    F    G    H  
 */
public class GameMain {

    private Board m_board;
    private BoardHistory m_history;
    private ChessAI m_AI;

    /** Receives input from user whether the given player is an AI */
    private void getAIStatusOfPlayer(Map<Boolean, Boolean> isPlayerAI, boolean isWhite, String[] validArgsYN) {
        String colour = isWhite ? "white" : "black";
        System.out.print("Is player " + colour + " an AI (Y/N)? ");
        String prompt = Reader.receiveValidInput(1, validArgsYN);
        boolean statusAI = (prompt.equals("Y")) ? true : false;
        isPlayerAI.put(isWhite, statusAI);
    }

    /** This converts chess coordinates such as A1 into 2d array index (ex E5 -> <3,4>) */
    private Coordinates convertChessCoordinates(String prompt) {
        int col = prompt.charAt(0) - 'A';
        int row = Board.ROWS - (prompt.charAt(1) - '0');
        return new Coordinates(row, col);
    }

    /** Constructor for GameMain class */
    public GameMain() {
        m_board = new Board();
        m_history = new BoardHistory();
        m_AI = new ChessAI(m_board, m_history);
        
        //Loops per game of chess, break out of loop when input to keep playing is N
        boolean sessionNotDone = true;
        while (sessionNotDone) {
            m_board.init();
            System.out.println("Welcome to the Huang Console Chess Game!");
            m_board.paint(false);
            
            //Key is true for white player, and value is true if player is AI
            Map<Boolean, Boolean> isPlayerAI = new HashMap<Boolean, Boolean>();
            String[] validArgsYN = {"YN"};
            getAIStatusOfPlayer(isPlayerAI, true, validArgsYN);
            getAIStatusOfPlayer(isPlayerAI, false, validArgsYN);

            String[] validArgs = {"ABCDEFGH","12345678"};
            String prompt = "";
            
            //Loops per turn of chess
            boolean gameNotDone = true;
            while (gameNotDone) {
                String currPlayer = m_board.m_whiteTurn ? "white" : "black";
                System.out.println("It is " + currPlayer + "'s turn.");
                
                boolean turnNotDone = true;
                if (isPlayerAI.get(m_board.m_whiteTurn)) {
                    System.out.println("AI is thinking...");
                    Coordinates[] chessMove = new Coordinates[2];
                    char[] pawnEvolution = {'.'};
                    m_AI.generateNextMove(chessMove, pawnEvolution);
                    m_history.addBoard(new Board(m_board));
                    m_board.moveSelectedPiece(chessMove[0], chessMove[1], true);
                    if (m_board.pawnReachedEnd()) {
                        m_board.evolvePawn(pawnEvolution[0]);
                    }
                    turnNotDone = false;
                }

                //Loops until player makes a valid move
                while (turnNotDone) {
                    System.out.print("Please select your piece to move (eg. A3): ");
                    Set<Coordinates> validMoves = null;
                    Coordinates rcStart = null;
                    //Loops until player selects his own chess piece
                    boolean pieceNotSelected = true;
                    while (pieceNotSelected) {
                        prompt = Reader.receiveValidInput(2, validArgs);
                        rcStart = convertChessCoordinates(prompt);
                        Piece piece = m_board.getPieceAtCoordinate(rcStart);
                        if (piece != null && piece.m_isWhite == m_board.m_whiteTurn) {
                            validMoves = m_board.getAllValidMoves(rcStart);
                            if (validMoves.isEmpty()) {
                                System.out.print("No possible moves can be made with this chess piece." +
                                                 " Please select another piece: ");
                            } else {
                                pieceNotSelected = false;
                            }
                        } else {
                            System.out.print("This is not a valid selection. Please select your piece: ");
                        }
                    }
                    m_board.paint(true);
                    System.out.print("Select where to move this piece (or enter X1 to move another piece): ");
                    String[] validArgs2 = {"ABCDEFGHX","12345678"};
                    //Loops until player selects a valid final location for the chess piece (or cancels to select another piece)
                    boolean placeToMoveNotSelected = true;
                    while (placeToMoveNotSelected) {
                        prompt = Reader.receiveValidInput(2, validArgs2);
                        if (prompt.equals("X1")) {
                            m_board.paint(false);
                            placeToMoveNotSelected = false;
                        } else if (prompt.charAt(0) == 'X') {
                            System.out.print("The input you provided is not valid. Please try again: ");
                        } else {
                            Coordinates rcFinal = convertChessCoordinates(prompt);
                            if (validMoves.contains(rcFinal)) {
                                m_history.addBoard(new Board(m_board));
                                m_board.moveSelectedPiece(rcStart, rcFinal, true);
                                if (m_board.pawnReachedEnd()) {
                                    System.out.println("Your pawn has reached the end! Select what it becomes...");
                                    System.out.print(  "(R = rook, N = knight, B = bishop, Q = queen): ");
                                    String[] validChessSymbols = {"RNBQ"};
                                    prompt = Reader.receiveValidInput(1, validChessSymbols);
                                    m_board.evolvePawn(prompt.charAt(0));
                                }
                                turnNotDone = false;
                                placeToMoveNotSelected = false;
                            } else {
                                System.out.print("That is not a valid move for the selected chess piece. Try again: ");
                            }
                        }
                    }
                }
                
                m_board.paint(false);
                if (m_board.hasLost()) {
                    System.out.println("The winning player is " + currPlayer + "!"); 
                    gameNotDone = false;
                } else if (m_board.isDraw()) {
                    System.out.println("It's a draw."); 
                    gameNotDone = false;
                }
            }

            System.out.print("Do you want to play again (Y/N)? ");
            prompt = Reader.receiveValidInput(1, validArgsYN);
            if (prompt.equals("N")) sessionNotDone = false;
        }
    }

    /** Main function, start of program */
    public static void main(String[] args) {
        new GameMain();
        System.out.println("End of Session");
    }
}
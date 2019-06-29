package chess;

import mytools.Coordinates;
import java.util.*;

public class ChessAI {
    
    private static final byte ITERATIONS = 4;
    
    private Board m_board;
    private BoardHistory m_history;
    
    /** Constructor */
    public ChessAI(Board board, BoardHistory history) {
        m_board = board;
        m_history = history;
    }

    /** Looks ahead some depth number of moves to find the first move of optimal result
     *  given that the opponent will look ahead depth - 1 moves */
    private int minMaxPruning(int depth, Board board, List<Coordinates> selections, List<Coordinates> destinations, char[] pawnEvolution, boolean avoidRepeats) {
        int factor = board.m_whiteTurn ? 1 : -1;
        if (board.hasLost()) return -10000*factor*(depth+1);
        if (board.isDraw()) return 0;
        if (depth == 0) return board.getAdvantagePoints();

        List<Coordinates> nextSelects = new ArrayList<Coordinates>();
        List<Coordinates> nextPlacements = new ArrayList<Coordinates>();
        board.getAllPossibleChessMoves(nextSelects, nextPlacements);
        int max = 0;
        boolean firstIteration = true;
        for (int i = 0; i < nextPlacements.size(); i++) {
            Board newBoard = new Board(board);
            newBoard.moveSelectedPiece(nextSelects.get(i), nextPlacements.get(i), true);
            if (newBoard.getPieceAtCoordinate(nextPlacements.get(i)).m_type != Type.PAWN &&
                avoidRepeats && destinations != null && m_history.foundRecentlyNTimes(newBoard, 2)) 
                continue;
            int prediction = -20000;
            if (newBoard.pawnReachedEnd()) {
                String allPossibleEvolves = "QNRB";
                for (int j = 0; j < allPossibleEvolves.length(); j++) {
                    Board newBoard2 = new Board(newBoard);
                    char toEvolve = allPossibleEvolves.charAt(j);
                    newBoard2.evolvePawn(toEvolve);
                    int adv = factor*minMaxPruning(depth-1, newBoard2, null, null, null, avoidRepeats);
                    if (adv > prediction) {
                        prediction = adv;
                        if (destinations != null) pawnEvolution[0] = toEvolve;
                    }
                }
            } else {
                prediction = factor*minMaxPruning(depth-1, newBoard, null, null, null, avoidRepeats);
            }
            if (firstIteration) {
                max = prediction;
                firstIteration = false;
            }
            if (destinations != null) {
                if (prediction > max) {
                    selections.clear();
                    destinations.clear();
                }
                if (prediction >= max) {
                    selections.add(nextSelects.get(i));
                    destinations.add(nextPlacements.get(i));
                }
            }
            if (prediction > max) max = prediction;
        }
        return max*factor;
    }

    /** Determines the next move to be made */
    public void generateNextMove(Coordinates[] chessMove, char[] pawnEvolution) {
        List<Coordinates> selection = new ArrayList<Coordinates>();
        List<Coordinates> destination = new ArrayList<Coordinates>();
        minMaxPruning(ITERATIONS, m_board, selection, destination, pawnEvolution, true);
        if (selection.isEmpty()) minMaxPruning(ITERATIONS, m_board, selection, destination, pawnEvolution, false);
        Random random = new Random();
        int index = random.nextInt(destination.size());
        chessMove[0] = selection.get(index);
        chessMove[1] = destination.get(index);
    }
}
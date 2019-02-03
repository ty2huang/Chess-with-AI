package chess;

import java.util.*;

public class BoardHistory {
    
    private static final byte CAPACITY = 40;
    
    private List<Board> m_history;

    public BoardHistory() {
        m_history = new ArrayList<Board>();
    }

    public void addBoard(Board board) {
        m_history.add(board);
        while (m_history.size() > CAPACITY) {
            m_history.remove(0);
        }
    }

    public boolean foundRecentlyNTimes(Board board, int N) {
        int count = 0;
        for (int i = 0; i < m_history.size() - 2; i++) {
            if (m_history.get(i).equals(board)) {
                count++;
                if (count == N) {
                    return true;
                }
            }
        }
        return false;
    }
}
package mytools;

public class Coordinates {
    public int m_row, m_col;

    public Coordinates(int row, int col) {
        m_row = row;
        m_col = col;
    }

    @Override
    public boolean equals(Object obj) {
        if (obj == null) return false;
        if (this == obj) return true;
        if (!(obj instanceof Coordinates)) return false;
        Coordinates rc = (Coordinates) obj;
        return m_row == rc.m_row && m_col == rc.m_col;
    }

    @Override
    public int hashCode() {
        int result = 31 * m_row + m_col;
        return result;
    }
}
package com.example.demo.engine;

import com.example.demo.dto.BoardDto;
import com.example.demo.dto.MoveResultDto;
import com.example.demo.dto.SimpleMoveDto;

import java.util.*;



public class GameEngine {

    enum Color { W, B }

    static class Pt {
        int x, y;
        Pt(int x, int y) { this.x = x; this.y = y; }
        @Override public boolean equals(Object o) {
            if (!(o instanceof Pt)) return false;
            Pt p = (Pt)o;
            return x == p.x && y == p.y;
        }
        @Override public int hashCode() { return Objects.hash(x, y); }
    }


    public MoveResultDto computeNextMove(BoardDto boardDto) {
        MoveResultDto result = new MoveResultDto();


        if (boardDto == null) {
            throw new IllegalArgumentException("BoardDto is null");
        }
        int N = boardDto.getSize();
        if (N <= 2) {
            throw new IllegalArgumentException("Size must be > 2");
        }
        String data = boardDto.getData();
        if (data == null || data.length() != N * N) {
            throw new IllegalArgumentException("data must be a string of length size*size and contain '.', 'W', 'B'");
        }
        String nextColor = boardDto.getNextPlayerColor();
        if (nextColor == null || !(nextColor.equalsIgnoreCase("W") || nextColor.equalsIgnoreCase("B"))) {
            throw new IllegalArgumentException("nextPlayerColor must be 'W' or 'B'");
        }
        Color next = nextColor.equalsIgnoreCase("W") ? Color.W : Color.B;
        Color other = (next == Color.W) ? Color.B : Color.W;


        char[][] board = new char[N][N];
        Set<Pt> whites = new HashSet<>();
        Set<Pt> blacks = new HashSet<>();
        int idx = 0;
        int emptyCount = 0;
        for (int y = 0; y < N; ++y) {
            for (int x = 0; x < N; ++x) {
                char ch = data.charAt(idx++);
                if (ch != '.' && ch != 'W' && ch != 'B') {
                    throw new IllegalArgumentException("Invalid character in data: must be '.', 'W' or 'B'");
                }
                board[y][x] = ch;
                if (ch == '.') emptyCount++;
                else if (ch == 'W') whites.add(new Pt(x, y));
                else blacks.add(new Pt(x, y));
            }
        }


        Character winner = checkWinnerFromSets(whites, blacks, N);
        if (winner != null) {
            result.setResult("finished");
            result.setWinner(String.valueOf(winner));
            result.setFinalBoard(data);
            return result;
        }
        if (emptyCount == 0) {
            result.setResult("draw");
            result.setFinalBoard(data);
            return result;
        }


        SimpleMoveDto winning = findWinningMove(board, N, next, whites, blacks);
        if (winning != null) {
            result.setMove(winning);
            return result;
        }


        SimpleMoveDto block = findBlockingMove(board, N, next, other, whites, blacks);
        if (block != null) {
            result.setMove(block);
            return result;
        }


        for (int y = 0; y < N; ++y) {
            for (int x = 0; x < N; ++x) {
                if (board[y][x] == '.') {
                    SimpleMoveDto mv = new SimpleMoveDto(x, y, next.name());
                    result.setMove(mv);
                    return result;
                }
            }
        }


        result.setResult("noMove");
        return result;
    }


    private SimpleMoveDto findWinningMove(char[][] board, int N, Color c,
                                          Set<Pt> whites, Set<Pt> blacks) {
        for (int y = 0; y < N; ++y) {
            for (int x = 0; x < N; ++x) {
                if (board[y][x] == '.') {

                    Pt cand = new Pt(x,y);
                    if (c == Color.W) {
                        if (wouldWinIfAdded(cand, whites, N, true)) return new SimpleMoveDto(x,y, "W");
                    } else {
                        if (wouldWinIfAdded(cand, blacks, N, false)) return new SimpleMoveDto(x,y, "B");
                    }
                }
            }
        }
        return null;
    }


    private SimpleMoveDto findBlockingMove(char[][] board, int N, Color myColor, Color oppColor,
                                           Set<Pt> whites, Set<Pt> blacks) {

        for (int y = 0; y < N; ++y) {
            for (int x = 0; x < N; ++x) {
                if (board[y][x] == '.') {
                    Pt cand = new Pt(x,y);
                    boolean oppWins;
                    if (oppColor == Color.W) {
                        oppWins = wouldWinIfAdded(cand, whites, N, true);
                    } else {
                        oppWins = wouldWinIfAdded(cand, blacks, N, false);
                    }
                    if (oppWins) {
                        // block here by playing this cell
                        return new SimpleMoveDto(x, y, myColor.name());
                    }
                }
            }
        }
        return null;
    }


    private boolean wouldWinIfAdded(Pt p, Set<Pt> originalSet, int N, boolean flagIsWhite) {

        Set<Pt> tmp = new HashSet<>(originalSet);
        tmp.add(p);
        return checkWinFromSet(tmp, N);
    }


    private Character checkWinnerFromSets(Set<Pt> whites, Set<Pt> blacks, int N) {
        if (checkWinFromSet(whites, N)) return 'W';
        if (checkWinFromSet(blacks, N)) return 'B';
        return null;
    }


    private boolean checkWinFromSet(Set<Pt> set, int N) {
        if (set.size() < 4) return false;
        List<Pt> pts = new ArrayList<>(set);
        HashSet<Long> keys = new HashSet<>(pts.size());
        for (Pt p : pts) keys.add(combine(p.x, p.y));
        for (int i = 0; i < pts.size(); ++i) {
            Pt A = pts.get(i);
            for (int j = i + 1; j < pts.size(); ++j) {
                Pt B = pts.get(j);
                int ax = A.x, ay = A.y, bx = B.x, by = B.y;
                int vx = bx - ax, vy = by - ay;


                int px = -vy, py = vx;
                int cx = ax + px, cy = ay + py;
                int dx = bx + px, dy = by + py;
                if (inBounds(cx, cy, N) && inBounds(dx, dy, N)) {
                    if (keys.contains(combine(cx, cy)) && keys.contains(combine(dx, dy))) return true;
                }
                int c2x = ax - px, c2y = ay - py;
                int d2x = bx - px, d2y = by - py;
                if (inBounds(c2x, c2y, N) && inBounds(d2x, d2y, N)) {
                    if (keys.contains(combine(c2x, c2y)) && keys.contains(combine(d2x, d2y))) return true;
                }


                int sumx = ax + bx, sumy = ay + by;
                if ((sumx & 1) == 0 && (sumy & 1) == 0) {
                    int mx2 = sumx / 2, my2 = sumy / 2;
                    int ux = ax - mx2, uy = ay - my2;
                    int upx = -uy, upy = ux;
                    int r1x = mx2 + upx, r1y = my2 + upy;
                    int r2x = mx2 - upx, r2y = my2 - upy;
                    if (inBounds(r1x, r1y, N) && inBounds(r2x, r2y, N)) {
                        if (keys.contains(combine(r1x, r1y)) && keys.contains(combine(r2x, r2y))) return true;
                    }
                }
            }
        }
        return false;
    }

    private static long combine(int x, int y) {
        return (((long)x) << 32) | (y & 0xffffffffL);
    }

    private static boolean inBounds(int x, int y, int N) {
        return x >= 0 && y >= 0 && x < N && y < N;
    }
}

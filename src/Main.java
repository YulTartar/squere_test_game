import java.util.*;

public class Main {
    enum Color { W, B }
    enum Type { USER, COMP }

    static class Player {
        Type type;
        Color color;
        Player(Type t, Color c) { type = t; color = c; }
    }

    static class Pt {
        int x, y;
        Pt(int x, int y) { this.x = x; this.y = y; }
        @Override public boolean equals(Object o) {
            if (!(o instanceof Pt)) return false;
            Pt p = (Pt)o;
            return x == p.x && y == p.y;
        }
        @Override public int hashCode() { return Objects.hash(x, y); }
        @Override public String toString() { return "(" + x + ", " + y + ")"; }
    }

    static class Game {
        int N;
        Player p1, p2;
        boolean running = false;
        Color[][] board;
        Set<Pt> whites = new HashSet<>();
        Set<Pt> blacks = new HashSet<>();
        boolean turnP1;
        Game(int N, Player p1, Player p2) {
            this.N = N; this.p1 = p1; this.p2 = p2;
            board = new Color[N][N];
            running = true;
            turnP1 = true;
        }
        Player currentPlayer() { return turnP1 ? p1 : p2; }
        Player otherPlayer() { return turnP1 ? p2 : p1; }
        boolean inBounds(int x, int y) { return x >=0 && y >=0 && x < N && y < N; }
        boolean isCellEmpty(int x, int y) { return inBounds(x,y) && board[y][x] == null; }
        boolean placeMove(int x, int y, Color c) {
            if (!inBounds(x,y)) return false;
            if (board[y][x] != null) return false;
            board[y][x] = c;
            Pt pt = new Pt(x,y);
            if (c == Color.W) whites.add(pt); else blacks.add(pt);
            return true;
        }

        boolean checkWin(Color c) {
            Set<Pt> set = (c == Color.W) ? whites : blacks;
            if (set.size() < 4) return false;
            List<Pt> pts = new ArrayList<>(set);
            HashSet<Long> keys = new HashSet<>(pts.size());
            for (Pt p : pts) keys.add(((long)p.x << 32) | (p.y & 0xffffffffL));
            for (int i = 0; i < pts.size(); ++i) {
                Pt A = pts.get(i);
                for (int j = i+1; j < pts.size(); ++j) {
                    Pt B = pts.get(j);
                    int ax = A.x, ay = A.y, bx = B.x, by = B.y;
                    int vx = bx - ax, vy = by - ay;
                    // AB как сторона
                    int px = -vy, py = vx;
                    int cx = ax + px, cy = ay + py;
                    int dx = bx + px, dy = by + py;
                    if (inBounds(cx, cy) && inBounds(dx, dy)) {
                        long kc = ((long)cx << 32) | (cy & 0xffffffffL);
                        long kd = ((long)dx << 32) | (dy & 0xffffffffL);
                        if (keys.contains(kc) && keys.contains(kd)) return true;
                    }
                    int c2x = ax - px, c2y = ay - py;
                    int d2x = bx - px, d2y = by - py;
                    if (inBounds(c2x, c2y) && inBounds(d2x, d2y)) {
                        long kc2 = ((long)c2x << 32) | (c2y & 0xffffffffL);
                        long kd2 = ((long)d2x << 32) | (d2y & 0xffffffffL);
                        if (keys.contains(kc2) && keys.contains(kd2)) return true;
                    }
                    int sumx = ax + bx, sumy = ay + by;
                    if ((sumx & 1) == 0 && (sumy & 1) == 0) {
                        int mx2 = sumx / 2, my2 = sumy / 2;
                        int ux = ax - mx2, uy = ay - my2;
                        int upx = -uy, upy = ux;
                        int r1x = mx2 + upx, r1y = my2 + upy;
                        int r2x = mx2 - upx, r2y = my2 - upy;
                        if (inBounds(r1x, r1y) && inBounds(r2x, r2y)) {
                            long kr1 = ((long)r1x << 32) | (r1y & 0xffffffffL);
                            long kr2 = ((long)r2x << 32) | (r2y & 0xffffffffL);
                            if (keys.contains(kr1) && keys.contains(kr2)) return true;
                        }
                    }
                }
            }
            return false;
        }

        boolean isFull() {
            for (int y = 0; y < N; ++y) for (int x = 0; x < N; ++x) if (board[y][x] == null) return false;
            return true;
        }

        Pt firstFree() {
            for (int y = 0; y < N; ++y) for (int x = 0; x < N; ++x) if (board[y][x] == null) return new Pt(x,y);
            return null;
        }
    }

    public static void main(String[] args) {
        Scanner sc = new Scanner(System.in);
        Game game = null;
        System.out.println("Type HELP for commands description.");
        outer:
        while (true) {
            String line;
            if (!sc.hasNextLine()) break;
            line = sc.nextLine().trim();
            if (line.isEmpty()) continue;

            String up = line.trim();
            String cmd = up.split("\\s+")[0].toUpperCase();

            switch (cmd) {
                case "HELP":
                    printHelp();
                    break;
                case "EXIT":
                    break outer;
                case "GAME":
                    try {
                        String rest = line.substring(4).trim();
                        String[] parts = rest.split(",");
                        if (parts.length != 3) { System.out.println("Incorrect command"); break; }
                        String nPart = parts[0].trim();
                        String p1Part = parts[1].trim();
                        String p2Part = parts[2].trim();
                        int N = Integer.parseInt(nPart);
                        if (N <= 2) { System.out.println("Incorrect command"); break; }
                        Player pl1 = parsePlayerParam(p1Part);
                        Player pl2 = parsePlayerParam(p2Part);
                        if (pl1 == null || pl2 == null) { System.out.println("Incorrect command"); break; }
                        if (pl1.color == pl2.color) { System.out.println("Incorrect command"); break; }
                        game = new Game(N, pl1, pl2);
                        System.out.println("New game started");
                        if (game.currentPlayer().type == Type.COMP) {
                            doComputerMove(game);
                            printBoard(game);
                            if (!game.running) {
                                game = null;
                            }
                        } else {
                            printBoard(game);
                        }
                    } catch (Exception ex) {
                        System.out.println("Incorrect command");
                    }
                    break;
                case "MOVE":
                    if (game == null || !game.running) { System.out.println("Incorrect command"); break; }
                    try {
                        String rest = line.substring(4).trim();
                        String[] parts = rest.split(",");
                        if (parts.length != 2) { System.out.println("Incorrect command"); break; }
                        int x = Integer.parseInt(parts[0].trim());
                        int y = Integer.parseInt(parts[1].trim());
                        Player cur = game.currentPlayer();
                        if (cur.type != Type.USER) { System.out.println("Incorrect command"); break; }
                        if (!game.inBounds(x,y) || !game.isCellEmpty(x,y)) { System.out.println("Incorrect command"); break; }
                        boolean ok = game.placeMove(x,y, cur.color);
                        if (!ok) { System.out.println("Incorrect command"); break; }
                        System.out.println(cur.color + " (" + x + ", " + y + ")");
                        printBoard(game);
                        if (game.checkWin(cur.color)) {
                            System.out.println("Game finished. " + cur.color + " wins!");
                            printFinalBoard(game);
                            game.running = false;
                            break;
                        }
                        if (game.isFull()) {
                            System.out.println("Game finished. Draw");
                            printFinalBoard(game);
                            game.running = false;
                            break;
                        }
                        game.turnP1 = !game.turnP1;
                        while (game.running && game.currentPlayer().type == Type.COMP) {
                            doComputerMove(game);
                            printBoard(game);
                        }
                        if (!game.running) {
                            if (game != null) printFinalBoard(game);
                            game = null;
                        }
                    } catch (Exception ex) {
                        System.out.println("Incorrect command");
                    }
                    break;
                default:
                    System.out.println("Incorrect command");
                    break;
            }
        }
        System.out.println("Bye!");
        sc.close();
    }

    static void printHelp() {
        System.out.println("Commands:");
        System.out.println("GAME N, TYPE1 C1, TYPE2 C2   - start new game. N>2. TYPE in {user, comp}. C in {W, B}");
        System.out.println("MOVE X, Y                   - place a piece at X,Y (user move)");
        System.out.println("HELP                        - this help");
        System.out.println("EXIT                        - exit program");
        System.out.println("Notes: coordinates start from 0 in the top-left corner.");
        System.out.println("Board is printed after every move: '.' empty, 'W' white, 'B' black.");
        System.out.println("Output of a computer (or any executed) move: \"C (X, Y)\" where C is W or B.");
    }

    static Player parsePlayerParam(String s) {
        s = s.trim();
        String[] toks = s.split("\\s+");
        if (toks.length != 2) return null;
        String typ = toks[0].toLowerCase();
        String col = toks[1].toUpperCase();
        Type t;
        if (typ.equals("user")) t = Type.USER;
        else if (typ.equals("comp")) t = Type.COMP;
        else return null;
        Color c;
        if (col.equals("W")) c = Color.W;
        else if (col.equals("B")) c = Color.B;
        else return null;
        return new Player(t, c);
    }

    static void doComputerMove(Game game) {
        Pt p = game.firstFree();
        if (p == null) {
            System.out.println("Game finished. Draw");
            printFinalBoard(game);
            game.running = false;
            return;
        }
        Player cur = game.currentPlayer();
        boolean ok = game.placeMove(p.x, p.y, cur.color);
        if (!ok) {
            System.out.println("Incorrect command");
            game.running = false;
            return;
        }
        System.out.println(cur.color + " (" + p.x + ", " + p.y + ")");
        if (game.checkWin(cur.color)) {
            System.out.println("Game finished. " + cur.color + " wins!");
            printFinalBoard(game);
            game.running = false;
            return;
        }
        if (game.isFull()) {
            System.out.println("Game finished. Draw");
            printFinalBoard(game);
            game.running = false;
            return;
        }
        game.turnP1 = !game.turnP1;
    }

    static void printBoard(Game game) {
        if (game == null) return;
        int N = game.N;
        System.out.print("   ");
        for (int x = 0; x < N; ++x) {
            System.out.printf("%2d ", x);
        }
        System.out.println();
        for (int y = 0; y < N; ++y) {
            System.out.printf("%2d ", y);
            System.out.print("| ");
            for (int x = 0; x < N; ++x) {
                Color c = game.board[y][x];
                char ch = '.';
                if (c == Color.W) ch = 'W';
                else if (c == Color.B) ch = 'B';
                System.out.print(ch + "  ");
            }
            System.out.println();
        }
    }

    static void printFinalBoard(Game game) {
        System.out.println("Final board:");
        printBoard(game);
    }
}

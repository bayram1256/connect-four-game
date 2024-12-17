//Bayram Ali 210210135

import javax.swing.*;
import java.awt.*;
import java.awt.event.*;
import java.util.*;

class ConnectFour extends JPanel implements ActionListener, MouseListener, MouseMotionListener {

    private static final int initialWidth = 1500; //FRAME WIDTH
    private static final int initialHeight = 900; //FRAME HEIGHT
    private static final int boardLength = 7;
    private static final int boardHeight = 6;
    private static final int widthUnit = initialWidth / (boardLength + 2); //WIDTH OF BOARD
    private static final int heightUnit = initialHeight / (boardHeight + 2); //HEIGHT OF BOARD
    private static final int WIDTH = widthUnit * (boardLength + 2);
    private static final int HEIGHT = heightUnit * (boardHeight + 2);
    private static Point p1;
    private static Point p2;
    JFrame frame;

    ConnectFour() {
        setBackground(Color.WHITE);

        frame = new JFrame("Connect 4");
        frame.setBounds(50, 50, WIDTH, HEIGHT);
        frame.add(this);
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        frame.setVisible(true);
        frame.addMouseListener(this);
        frame.addMouseMotionListener(this); // to track the position of the mouse

        javax.swing.Timer timer = new javax.swing.Timer(10, this); // UPDATE FRAME IN EACH  10MS
        timer.start();
    }


    public void actionPerformed(ActionEvent e) { //repaint the screen each 10ms
        repaint();
    }

    public void paintComponent(Graphics g) { //draws a board to the screen
        super.paintComponent(g);
        Board.draw(g);
    }

    public void mouseMoved(MouseEvent e) {  //activates hover method
        Board.hover(e.getX());
    }

    public void mousePressed(MouseEvent e) {//drops "ball" when mouse pressed
        Board.drop();
    }
//UNUSED METHODS FROM MOUSE LISTENER
    public void mouseReleased(MouseEvent e) {}
    public void mouseClicked(MouseEvent e) {}
    public void mouseEntered(MouseEvent e) {}
    public void mouseExited(MouseEvent e) {}
    public void mouseDragged(MouseEvent e) {}



    static class Board {
        static Color[][] board = new Color[7][6]; //color of each "cell"
        static Color[] players = new Color[]{Color.RED, Color.BLUE};
        static int turn = 0;
        static int hoverX, hoverY; //coordinates of hovered "ball"
        static boolean gameDone;

        static { //to fill initial columns with white circles
            for (Color[] colors : board) {
                Arrays.fill(colors, Color.WHITE);
            }
        }

        public static void draw(Graphics g) {//DRAWS A BOARD WITH COLUMNS FULL OF "CIRCLES"
            ((Graphics2D)g).setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);//make screen look better
            ((Graphics2D)(g)).setStroke(new BasicStroke(2.0f)); //stroke

            for (int i = widthUnit; i <= WIDTH - widthUnit; i += widthUnit) {  //DRAWS VERTICAL LINES
                g.setColor(Color.BLACK);
                g.drawLine(i, heightUnit, i, HEIGHT - heightUnit);
                if (i == WIDTH - widthUnit)
                    continue;

                for (int j = heightUnit; j < HEIGHT - heightUnit; j += heightUnit) { //DRAWS CIRCLES IN EACH COLUMN
                    g.setColor(board[i/widthUnit - 1][j/heightUnit - 1]);
                    g.fillOval(i + 5, j + 5, widthUnit - 10, heightUnit - 10);
                    g.setColor(Color.BLACK);
                    g.drawOval(i + 5, j + 5, widthUnit - 10, heightUnit - 10);
                }
            }

            if (gameDone) { //if game done, change color of current "ball" to green, otherwise to current players color
                g.setColor(Color.GREEN);
            }
            else {
                g.setColor(players[turn]);
            }

            g.fillOval(hoverX + 5, hoverY + 5, widthUnit - 10, heightUnit - 10); // draws current colored circle
            g.setColor(Color.BLACK);
            g.drawOval(hoverX + 5, hoverY + 5, widthUnit - 10, heightUnit - 10);

            g.setColor(Color.BLACK);
            if (p1 != null && p2 != null) { //if game done, draw a line to connect winning condition "circles"
                g.drawLine(p1.x, p1.y, p2.x, p2.y);
            }

        }

        public static void hover(int x) {// to know where "ball" wil be droped
            x -= x%widthUnit; // aline with vertical line
            if (x < widthUnit)
                x = widthUnit;

            if (x >= WIDTH - widthUnit)
                x = WIDTH - 2*widthUnit;
            hoverX = x;
            hoverY = 0; // does not matter because "ball" is always at the top of the board
        }

        public static void drop() {
            if (board[hoverX/widthUnit - 1][0] != Color.WHITE)// to check if the column is already full
                return;

            Color color = players[turn];
            int x = hoverX;
            int i;
            for (i = 0; i < board[x/widthUnit - 1].length && board[x/widthUnit - 1][i] == Color.WHITE; i++) {
                //if the "ball" in range of board(between columns) and NOT taken
                if (gameDone) { // is this drop  was a winning drop?
                    return;
                }
            }
            if (gameDone) {
                return;
            }
            board[x/widthUnit - 1][i - 1] = color; //update the current situation on the board in memory(list)
            checkConnect(x/widthUnit - 1, i - 1);
            turn = (turn + 1) % players.length; // change the turn
        }

        public static void checkConnect(int x, int y) { //checks connection of four balls on the board
            if (gameDone) {
                return;
            }

            PointPair pair = search(board, x, y);

            if (pair != null) { // if four find
                p1 = new Point((pair.p1.x + 1) * widthUnit + widthUnit / 2, (pair.p1.y + 1) * heightUnit + heightUnit / 2);
                p2 = new Point((pair.p2.x + 1) * widthUnit + widthUnit / 2, (pair.p2.y + 1) * heightUnit + heightUnit / 2);
                gameDone = true;
            }
        }

        public static PointPair search(Color[][] arr, int i, int j) {
            Color color = arr[i][j];
            int left;
            int right;
            int up;
            int down;

            // check horizontally left to right
            left = right = i;
            while (left >= 0 && arr[left][j] == color) left--;
            left++;
            while (right < arr.length && arr[right][j] == color) right++;
            right--;
            if (right - left >= 3) { //four in a row found
                return new PointPair(left, j, right, j);
            }

            // check vertically top to bottom
            down = j;
            while (down < arr[i].length && arr[i][down] == color)
            down++;
            down--;
            if (down - j >= 3) {
                return new PointPair(i, j, i, down);
            }

            // check diagonal top left to bottom right
            left = right = i;
            up = down = j;
            while (left >= 0 && up >= 0 && arr[left][up] == color) {
                left--;
                up--;
            }
            left++;
            up++;
            while (right < arr.length && down < arr[right].length && arr[right][down] == color) {
                right++;
                down++;
            }
            right--;
            down--;
            if (right - left >= 3 && down - up >= 3) {
                return new PointPair(left, up, right, down);
            }

            // check diagonal top right to bottom left
            left = right = i;
            up = down = j;
            while (left >= 0 && down < arr[left].length && arr[left][down] == color) {left--; down++;}
            left++; down--;
            while (right < arr.length && up >= 0 && arr[right][up] == color) {right++; up--;}
            right--; up++;
            if (right - left >= 3 && down - up >= 3) {
                return new PointPair(left, down, right, up);
            }

            return null;
        }
        static class PointPair { //inner class
            public Point p1, p2;

            PointPair(int x1, int y1, int x2, int y2) {
                p1 = new Point(x1, y1);
                p2 = new Point(x2, y2);
            }
        }

    }
    public static void main(String[] args) {
        ConnectFour connectFour = new ConnectFour();
    }
}
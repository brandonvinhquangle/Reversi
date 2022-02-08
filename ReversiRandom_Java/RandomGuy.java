import java.awt.*;
import java.util.*;
import java.awt.event.*;
import java.lang.*;
import java.io.*;
import java.net.*;
import javax.swing.*;
import javax.swing.plaf.BorderUIResource.CompoundBorderUIResource;

import java.math.*;
import java.text.*;

class RandomGuy {

    public Socket s;
    public BufferedReader sin;
    public PrintWriter sout;
    Random generator = new Random();

    double t1, t2;
    int me;
    int boardState;
    int state[][] = new int[8][8]; // state[0][0] is the bottom left corner of the board (on the GUI)
    int turn = -1;
    int round;

    int validMoves[] = new int[64];
    int numValidMoves;

    static int heuristicValues[][] = new int[8][8];
    final int cornerVal = 4;
    final int edgeVal = 2;
    final int normalVal = 1;
    final int ply = 3;
    final int inf = 999999;
    final int negInf = -999999;

    public static int getPlayerOneVal(int[][] state) {
        int total = 0;
        for (int i = 0; i < 8; i++) {
            for (int j = 0; j < 8; j++) {
                if (state[i][j] == 1) {
                    total += heuristicValues[i][j];
                } else if (state[i][j] == 2) {
                    total -= heuristicValues[i][j];
                }
            }
        }
        return total;
    }

    // Determine if the spot on the board is a corner
    private boolean isCorner(int i, int j) {
        if ((i == 0 && j == 0) || (i == 0 && j == 7) || (i == 7 && j == 0) || (i == 7 && j == 7)) {
            return true;
        }
        return false;
    }

    // Determine if the spot on the board is an edge, including corners
    private boolean isEdge(int i, int j) {
        if (i == 0 || j == 0 || i == 7 || j == 7) {
            return true;
        }
        return false;
    }

    // main function that (1) establishes a connection with the server, and then
    // plays whenever it is this player's turn
    public RandomGuy(int _me, String host) {
        // Initialize heuristicValues
        for (int i = 0; i < 8; i++) {
            for (int j = 0; j < 8; j++) {
                if (isCorner(i, j)) {
                    heuristicValues[i][j] = cornerVal;
                } else if (isEdge(i, j)) {
                    heuristicValues[i][j] = edgeVal;
                } else {
                    heuristicValues[i][j] = normalVal;
                }
            }
        }

        me = _me;
        initClient(host);

        int myMove;

        while (true) {
            System.out.println("Read");
            readMessage();

            if (turn == me) {
                System.out.println("Move");
                getValidMoves(round, state);

                myMove = move();
                System.out.println("--------------------------------------");
                System.out.println("MyMove: " + myMove);
                System.out.println("--------------------------------------");
                // myMove = generator.nextInt(numValidMoves); // select a move randomly

                String sel = validMoves[myMove] / 8 + "\n" + validMoves[myMove] % 8;

                System.out.println("Selection: " + validMoves[myMove] / 8 + ", " + validMoves[myMove] % 8);

                sout.println(sel);
            }
        }
        // while (turn == me) {
        // System.out.println("My turn");

        // readMessage();
        // }
    }

    // You should modify this function
    // validMoves is a list of valid locations that you could place your "stone" on
    // this turn
    // Note that "state" is a global variable 2D list that shows the state of the
    // game
    private int move() {
        // just move randomly for now
        // int myMove = generator.nextInt(numValidMoves);

        // TODO: Build Cool Algorithm Here

        if (round <= 4) {
            return 0;
        }

        ReversiNode node = new ReversiNode(null, this.state);
        int bestVal = minimax(node, 0, true, negInf, inf);
        getValidMoves(round, node.getState());
        for (int i = 0; i < 64; i++) {
            try {
                System.out.println("Valid Move: " + validMoves[i]);
            }
            catch (Exception ex) {
                break;
            }
        }
        int myMove = 999; // not valid so we know if it works or not

        ArrayList<ReversiNode> children = node.getChildren();
        for (int i = 0; i < 64; i++) {
            try {
                if (children.get(i).getBestVal() == bestVal) {
                    myMove = children.get(i).getMoveIndex();
                }
            }
            catch (Exception ex) {
                break;
            }
        }
        
        return myMove;
    }

    private int[][] copyArray(int[][] arr) {
        int[][] copied = new int[8][8];
        for (int i = 0; i < 8; i++) {
            for (int j = 0; j < 8; j++) {
                copied[i][j] = arr[i][j];
            }
        }
        return copied;
    }

    // Minimax / Alpha Beta Pruning Algorithm
    // returns value associated with node it was called on
    private int minimax(ReversiNode currentNode, int depth, boolean maximizingPlayer, int alpha, int beta) {
        if (ply == depth) {
            return currentNode.getPlayerOneVal();
        }

        getValidMoves(round + depth, currentNode.getState());
        for (int i = 0; i < 64; i++) {
            if (validMoves[i] == 0) {
                break;
            }
            int move = validMoves[i];
            double moveDouble = move;
            int xVal = (int) Math.floor(moveDouble / 8.0);
            int yVal = move % 8;

            int[][] newState = copyArray(currentNode.getState());
            int playerVal = 0;
            if (maximizingPlayer) {
                playerVal = 1;
            } else {
                playerVal = 2;
            }
            newState[xVal][yVal] = playerVal;

            ReversiNode n = new ReversiNode(currentNode, newState);
            n.setMoveIndex(i);
            currentNode.addChild(n);
            // System.out.println("Move option: " + move);
        }

        if (maximizingPlayer) {
            int bestVal = negInf;
            ArrayList<ReversiNode> children = currentNode.getChildren();
            for (int i = 0; i < children.size(); i++) {
                ReversiNode child = children.get(i);
                int value = minimax(child, depth + 1, false, alpha, beta);
                bestVal = Math.max(bestVal, value);
                alpha = Math.max(alpha, bestVal);
                if (beta <= alpha) {
                    break;
                }
            }
            currentNode.setBestVal(bestVal);
            return bestVal;
        } else {
            int bestVal = inf;
            ArrayList<ReversiNode> children = currentNode.getChildren();
            for (int i = 0; i < children.size(); i++) {
                ReversiNode child = children.get(i);
                int value = minimax(child, depth + 1, true, alpha, beta);
                bestVal = Math.min(bestVal, value);
                beta = Math.min(beta, bestVal);
                if (beta <= alpha) {
                    break;
                }
            }
            currentNode.setBestVal(bestVal);
            return bestVal;
        }
    }

    // generates the set of valid moves for the player; returns a list of valid
    // moves (validMoves)
    private void getValidMoves(int round, int state[][]) {
        int i, j;

        numValidMoves = 0;
        if (round < 4) {
            if (state[3][3] == 0) {
                validMoves[numValidMoves] = 3 * 8 + 3;
                numValidMoves++;
            }
            if (state[3][4] == 0) {
                validMoves[numValidMoves] = 3 * 8 + 4;
                numValidMoves++;
            }
            if (state[4][3] == 0) {
                validMoves[numValidMoves] = 4 * 8 + 3;
                numValidMoves++;
            }
            if (state[4][4] == 0) {
                validMoves[numValidMoves] = 4 * 8 + 4;
                numValidMoves++;
            }
            System.out.println("Valid Moves:");
            for (i = 0; i < numValidMoves; i++) {
                System.out.println(validMoves[i] / 8 + ", " + validMoves[i] % 8);
            }
        } else {
            System.out.println("Valid Moves:");
            for (i = 0; i < 8; i++) {
                for (j = 0; j < 8; j++) {
                    if (state[i][j] == 0) {
                        if (couldBe(state, i, j)) {
                            validMoves[numValidMoves] = i * 8 + j;
                            numValidMoves++;
                            System.out.println(i + ", " + j);
                        }
                    }
                }
            }
        }

        // if (round > 3) {
        // System.out.println("checking out");
        // System.exit(1);
        // }
    }

    private boolean checkDirection(int state[][], int row, int col, int incx, int incy) {
        int sequence[] = new int[7];
        int seqLen;
        int i, r, c;

        seqLen = 0;
        for (i = 1; i < 8; i++) {
            r = row + incy * i;
            c = col + incx * i;

            if ((r < 0) || (r > 7) || (c < 0) || (c > 7))
                break;

            sequence[seqLen] = state[r][c];
            seqLen++;
        }

        int count = 0;
        for (i = 0; i < seqLen; i++) {
            if (me == 1) {
                if (sequence[i] == 2)
                    count++;
                else {
                    if ((sequence[i] == 1) && (count > 0))
                        return true;
                    break;
                }
            } else {
                if (sequence[i] == 1)
                    count++;
                else {
                    if ((sequence[i] == 2) && (count > 0))
                        return true;
                    break;
                }
            }
        }

        return false;
    }

    private boolean couldBe(int state[][], int row, int col) {
        int incx, incy;

        for (incx = -1; incx < 2; incx++) {
            for (incy = -1; incy < 2; incy++) {
                if ((incx == 0) && (incy == 0))
                    continue;

                if (checkDirection(state, row, col, incx, incy))
                    return true;
            }
        }

        return false;
    }

    public void readMessage() {
        int i, j;
        String status;
        try {
            // System.out.println("Ready to read again");
            turn = Integer.parseInt(sin.readLine());

            if (turn == -999) {
                try {
                    Thread.sleep(200);
                } catch (InterruptedException e) {
                    System.out.println(e);
                }

                System.exit(1);
            }

            // System.out.println("Turn: " + turn);
            round = Integer.parseInt(sin.readLine());
            t1 = Double.parseDouble(sin.readLine());
            System.out.println(t1);
            t2 = Double.parseDouble(sin.readLine());
            System.out.println(t2);
            for (i = 0; i < 8; i++) {
                for (j = 0; j < 8; j++) {
                    state[i][j] = Integer.parseInt(sin.readLine());
                }
            }
            sin.readLine();
        } catch (IOException e) {
            System.err.println("Caught IOException: " + e.getMessage());
        }

        System.out.println("Turn: " + turn);
        System.out.println("Round: " + round);
        for (i = 7; i >= 0; i--) {
            for (j = 0; j < 8; j++) {
                System.out.print(state[i][j]);
            }
            System.out.println();
        }
        System.out.println();
    }

    public void initClient(String host) {
        int portNumber = 3333 + me;

        try {
            s = new Socket(host, portNumber);
            sout = new PrintWriter(s.getOutputStream(), true);
            sin = new BufferedReader(new InputStreamReader(s.getInputStream()));

            String info = sin.readLine();
            System.out.println(info);
        } catch (IOException e) {
            System.err.println("Caught IOException: " + e.getMessage());
        }
    }

    // compile on your machine: javac *.java
    // call: java RandomGuy [ipaddress] [player_number]
    // ipaddress is the ipaddress on the computer the server was launched on. Enter
    // "localhost" if it is on the same computer
    // player_number is 1 (for the black player) and 2 (for the white player)
    public static void main(String args[]) {
        new RandomGuy(Integer.parseInt(args[1]), args[0]);
    }

}
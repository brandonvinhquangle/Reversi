import java.util.*;
import java.io.*;
import java.net.*;

/* Stuff to do for next time:
1. Different Scoreboards
2. Fix Heuristic Values -> Look these up and don't be stupid by guessing
3. Add an element of Randomness -> Monte-Carlo Algorithm
4. Forcing opponent to take bad positions
5. 
*/

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

    // final static int cornerVal = 20;
    // final static int edgeVal = 5;
    // final static int normalVal = 1;
    // final static int cornerAdjacentEdge = -5;
    // final static int cornerDiagonal = -10;

    static int heuristicValues[][] = {
        { 99, -8, 8, 6, 6, 8, -8, 99 },
        { -8, -24, -4, -3, -3, -4, -24, -8 },
        { 8, -4, 7, 4, 4, 7, -4, 8 },
        { 6, -3, 4, 0, 0, 4, -3, 6 },
        { 6, -3, 4, 0, 0, 4, -3, 6 },
        { -8, -4, 7, 4, 4, 7, -4, -8 },
        { -8, -24, -4, -3, -3, -4, -24, -8 },
        { 99, -8, 8, 6, 6, 8, -8, 99 },
    };

    final int ply = 6;
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

    // main function that (1) establishes a connection with the server, and then
    // plays whenever it is this player's turn
    public RandomGuy(int _me, String host) {
        me = _me;
        initClient(host);

        int myMove;

        while (true) {
            //System.out.println("Read");
            readMessage();

            if (turn == me) {
                //System.out.println("Move");
                getValidMoves(round, state);

                myMove = move();
                //System.out.println("--------------------------------------");
                //System.out.println("MyMove: " + myMove);
                //System.out.println("--------------------------------------");
                // myMove = generator.nextInt(numValidMoves); // select a move randomly

                String sel = validMoves[myMove] / 8 + "\n" + validMoves[myMove] % 8;

                ////System.out.println("Selection: " + validMoves[myMove] / 8 + ", " + validMoves[myMove] % 8);

                sout.println(sel);
            }
        }
        // while (turn == me) {
        // ////System.out.println("My turn");

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
        if (round < 4 || round > 62) {
            return 0;
        }

        ReversiNode node = new ReversiNode(null, this.state);
        int bestVal = minimax(node, 0, true, negInf, inf);
        Vector<Integer> moves = getValidMoves(round, node.getState());
        for (int i = 0; i < moves.size(); i++) {
            try {
                //System.out.println("Valid Move: " + moves.get(i));
            } catch (Exception ex) {
                break;
            }
        }
        int myMove = 999; // not valid so we know if it works or not

        ArrayList<ReversiNode> children = node.getChildren();
        //System.out.println("Number of children: " + children.size());
        for (int i = 0; i < moves.size(); i++) {
            if (children.get(i).getBestVal() == bestVal) {
                myMove = children.get(i).getMoveIndex();
            }
        }

        if (children.size() == 0) {
            return -1;
        }

        if (myMove == 999) {
            myMove = generator.nextInt(numValidMoves);
        }

        return myMove;
    }

    // Copies the the board array to a new array
    private int[][] copyArray(int[][] arr) {
        int[][] copied = new int[8][8];
        for (int i = 0; i < 8; i++) {
            for (int j = 0; j < 8; j++) {
                copied[i][j] = arr[i][j];
            }
        }
        return copied;
    }

    private int counter = 0;

    // Minimax / Alpha Beta Pruning Algorithm
    // Returns value associated with node it was called on
    private int minimax(ReversiNode currentNode, int depth, boolean maximizingPlayer, int alpha, int beta) {
        //System.out.println("-------------------" + counter++ + "-------------------");
        //System.out.println("Depth: " + depth);

        // TODO: Fix the Base Case, fix getValidMoves, flip nodes during minimax
        // Check to see if you're out of Valid Moves
        Vector<Integer> moves = getValidMoves(round + depth, currentNode.getState());

        if (ply == depth || moves.size() == 0) {
            return currentNode.getPlayerOneVal();
        }

        for (int i = 0; i < moves.size(); i++) {
            int move = moves.get(i);
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
            int playerTurn = 1;
            if (maximizingPlayer) {
                playerTurn = 0;
            }
            changeColors(xVal, yVal, playerTurn, newState);

            ReversiNode n = new ReversiNode(currentNode, newState);
            n.setMoveIndex(i);
            currentNode.addChild(n);
            // //System.out.println("Move option: " + move);
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
    private Vector<Integer> getValidMoves(int round, int state[][]) {
        int i, j;

        Vector<Integer> validMovesReturned = new Vector<Integer>();

        numValidMoves = 0;
        if (round < 4) {
            if (state[3][3] == 0) {
                validMovesReturned.add(3 * 8 + 3);
                validMoves[numValidMoves] = 3 * 8 + 3;
                numValidMoves++;
            }
            if (state[3][4] == 0) {
                validMovesReturned.add(3 * 8 + 4);
                validMoves[numValidMoves] = 3 * 8 + 4;
                numValidMoves++;
            }
            if (state[4][3] == 0) {
                validMovesReturned.add(4 * 8 + 3);
                validMoves[numValidMoves] = 4 * 8 + 3;
                numValidMoves++;
            }
            if (state[4][4] == 0) {
                validMovesReturned.add(4 * 8 + 4);
                validMoves[numValidMoves] = 4 * 8 + 4;
                numValidMoves++;
            }
            //System.out.println("Valid Moves:");
            for (i = 0; i < numValidMoves; i++) {
                //System.out.println(validMovesReturned.get(i) / 8 + ", " + validMovesReturned.get(i) % 8);
            }
        } else {
            //System.out.println("Valid Moves:");
            for (i = 0; i < 8; i++) {
                for (j = 0; j < 8; j++) {
                    if (state[i][j] == 0) {
                        if (couldBe(state, i, j)) {
                            validMovesReturned.add(i * 8 + j);
                            validMoves[numValidMoves] = i * 8 + j;
                            numValidMoves++;
                            //System.out.println(i + ", " + j);
                        }
                    }
                }
            }
        }

        return validMovesReturned;

        // if (round > 3) {
        // //System.out.println("checking out");
        // System.exit(1);
        // }
    }

    public static void checkDirection(int row, int col, int incx, int incy, int turn, int[][] state) {
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
            if (turn == 0) {
                if (sequence[i] == 2)
                    count++;
                else {
                    if ((sequence[i] == 1) && (count > 0))
                        count = 20;
                    break;
                }
            } else {
                if (sequence[i] == 1)
                    count++;
                else {
                    if ((sequence[i] == 2) && (count > 0))
                        count = 20;
                    break;
                }
            }
        }

        if (count > 10) {
            if (turn == 0) {
                i = 1;
                r = row + incy * i;
                c = col + incx * i;
                while (state[r][c] == 2) {
                    state[r][c] = 1;
                    i++;
                    r = row + incy * i;
                    c = col + incx * i;
                }
            } else {
                i = 1;
                r = row + incy * i;
                c = col + incx * i;
                while (state[r][c] == 1) {
                    state[r][c] = 2;
                    i++;
                    r = row + incy * i;
                    c = col + incx * i;
                }
            }
        }
    }

    public static void changeColors(int row, int col, int turn, int[][] state) {
        int incx, incy;

        for (incx = -1; incx < 2; incx++) {
            for (incy = -1; incy < 2; incy++) {
                if ((incx == 0) && (incy == 0))
                    continue;

                checkDirection(row, col, incx, incy, turn, state);
            }
        }
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
            // //System.out.println("Ready to read again");
            turn = Integer.parseInt(sin.readLine());

            if (turn == -999) {
                try {
                    Thread.sleep(200);
                } catch (InterruptedException e) {
                    //System.out.println(e);
                }

                System.exit(1);
            }

            // //System.out.println("Turn: " + turn);
            round = Integer.parseInt(sin.readLine());
            t1 = Double.parseDouble(sin.readLine());
            //System.out.println(t1);
            t2 = Double.parseDouble(sin.readLine());
            //System.out.println(t2);
            for (i = 0; i < 8; i++) {
                for (j = 0; j < 8; j++) {
                    state[i][j] = Integer.parseInt(sin.readLine());
                }
            }
            sin.readLine();
        } catch (IOException e) {
            System.err.println("Caught IOException: " + e.getMessage());
        }

        //System.out.println("Turn: " + turn);
        //System.out.println("Round: " + round);
        for (i = 7; i >= 0; i--) {
            for (j = 0; j < 8; j++) {
                //System.out.print(state[i][j]);
            }
            //System.out.println();
        }
        //System.out.println();
    }

    public void initClient(String host) {
        int portNumber = 3333 + me;

        try {
            s = new Socket(host, portNumber);
            sout = new PrintWriter(s.getOutputStream(), true);
            sin = new BufferedReader(new InputStreamReader(s.getInputStream()));

            String info = sin.readLine();
            //System.out.println(info);
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

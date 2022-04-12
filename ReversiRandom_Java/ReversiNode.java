import java.util.ArrayList;

public class ReversiNode {
  private int superneg = -999999999;
  private int[][] state;
  private int playerOneVal;
  private ArrayList<ReversiNode> children;
  private ReversiNode parent;
  private int bestVal;
  private int moveIndex;
  private boolean maximizingPlayer;
  private int xVal;
  private int yVal;
  final int infh = 999999;
  final int negInfh = -999999;
  private int baseHeuristicValues[][] = {
    { infh, negInfh, 8, 6, 6, 8, negInfh, infh },
    { negInfh, negInfh, -4, -3, -3, -4, negInfh, negInfh },
    { 8, -4, 7, 4, 4, 7, -4, 8 },
    { 6, -3, 4, 0, 0, 4, -3, 6 },
    { 6, -3, 4, 0, 0, 4, -3, 6 },
    { 8, -4, 7, 4, 4, 7, -4, 8 },
    { negInfh, negInfh, -4, -3, -3, -4, negInfh, negInfh },
    { infh, negInfh, 8, 6, 6, 8, negInfh, infh },
  };
  private int[][] specificHeuristics = baseHeuristicValues.clone();

  ReversiNode(ReversiNode parent, int[][] state, boolean maximizingPlayer, int xVal, int yVal) {
    this.state = state;
    this.parent = parent;
    this.maximizingPlayer = maximizingPlayer;
    this.xVal = xVal;
    this.yVal = yVal;
    playerOneVal = RandomGuy.getPlayerOneVal(state, this);
    children = new ArrayList<ReversiNode>();
    bestVal = 0;
    moveIndex = 0;
  }

  private void editHeuristics() {
    int us = 2;
    int them = 1;
    if (maximizingPlayer) {
      us = 1;
      them = 2;
    }
    
    final int subtractor = 8;

    for (int i = 0; i < 8; i++) {
      for (int j = 0; j < 8; j++) {
        // don't take pieces next to first, only take the corner
        if (nextToCorner(i, j) && this.state[i][j] == them) {
          baseHeuristicValues[i][j] = 0;
        }
        else {
          baseHeuristicValues[i][j] = negInfh;
        }
        
        // don't take next to enemy edge piece unless taking corner
        if ((i == 0 || i == 7) && this.state[i][j] == them && !(j == 0 || j == 7)) {
          // horizontal
          if (isCorner(i, j+1)) {
            this.state[i][j+1] += 3 * subtractor;
          }
          else {
            this.state[i][j+1] -= subtractor;
          }

          if (isCorner(i, j-1)) {
            this.state[i][j-1] += 3 * subtractor;
          }
          else {
            this.state[i][j-1] -= subtractor;
          }
        }
        else if ((j == 0 || j == 7) && this.state[i][j] == them && !(i == 0 || i == 7)) {
          // vertical
          if (isCorner(i+1, j)) {
            this.state[i+1][j] += 3 * subtractor;
          }
          else {
            this.state[i+1][j] -= subtractor;
          }

          if (isCorner(i-1, j)) {
            this.state[i-1][j] += 3 * subtractor;
          }
          else {
            this.state[i-1][j] -= subtractor;
          }
        }
      }
    }
  }

  public static boolean nextToCorner(int i, int j) {
    if ((i == 1 && j == 0) || (i == 0 && j == 1) || (i == 1 && j == 1) || 
        (i == 0 && j == 6) || (i == 1 && j == 7) || (i == 6 && j == 6) || 
        (i == 6 && j == 0) || (i == 6 && j == 1) || (i == 7 && j == 1) || 
        (i == 7 && j == 6) || (i == 6 && j == 7) || (i == 6 && j == 6)) {
          return true;
        }
    return false;
  }

  private boolean isEdge(int i, int j) {
    if (isCorner(i, j)){
      return false;
    }
    else if (i == 0 || i == 7 || j == 0 || j == 7) {
      return true;
    }
    return false;
  }

  private boolean isCorner(int i, int j) {
    if ((i == 0 || i == 7) && (j == 0 || j == 7)) {
      return true;
    }
    return false;
  }

  public int[][] getHeuristics() {
    editHeuristics();
    return specificHeuristics;
  }

  public void addChild(ReversiNode node) {
    children.add(node);
  }

  public int[][] getState() {
    return this.state;
  }

  public void setState(int[][] state) {
    this.state = state;
  }

  public int getPlayerOneVal() {
    return this.playerOneVal;
  }

  public void setPlayerOneVal(int playerOneVal) {
    this.playerOneVal = playerOneVal;
  }

  public ArrayList<ReversiNode> getChildren() {
    return this.children;
  }

  public void setChildren(ArrayList<ReversiNode> children) {
    this.children = children;
  }

  public ReversiNode getParent() {
    return this.parent;
  }

  public void setParent(ReversiNode parent) {
    this.parent = parent;
  }
  
  public int getBestVal() {
    if (nextToCorner(xVal, yVal)) {
      return superneg;
    }
    return bestVal;
  }

  public void setBestVal(int bestVal) {
    this.bestVal = bestVal;
  }

  public int getMoveIndex() {
    return this.moveIndex;
  }

  public void setMoveIndex(int moveIndex) {
    this.moveIndex = moveIndex;
  }
}

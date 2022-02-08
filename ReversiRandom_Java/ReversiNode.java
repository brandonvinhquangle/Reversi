import java.util.ArrayList;

public class ReversiNode {
  private int[][] state;
  private int playerOneVal;
  private ArrayList<ReversiNode> children;
  private ReversiNode parent;
  private int bestVal;
  private int moveIndex;

  ReversiNode(ReversiNode parent, int[][] state) {
    this.state = state;
    this.parent = parent;
    playerOneVal = RandomGuy.getPlayerOneVal(state);
    children = new ArrayList<ReversiNode>();
    bestVal = 0;
    moveIndex = 0;
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

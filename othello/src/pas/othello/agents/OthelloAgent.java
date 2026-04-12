package src.pas.othello.agents;

// SYSTEM IMPORTS
import java.util.ArrayList;
import java.util.List;
import java.util.Random;
import java.util.Set;

// JAVA PROJECT IMPORTS
import edu.bu.pas.othello.agents.Agent;
import edu.bu.pas.othello.agents.TimedTreeSearchAgent;
import edu.bu.pas.othello.game.Game;
import edu.bu.pas.othello.game.Game.GameView;
import edu.bu.pas.othello.game.PlayerType;
import edu.bu.pas.othello.traversal.Node;
import edu.bu.pas.othello.utils.Coordinate;

public class OthelloAgent
        extends TimedTreeSearchAgent {

    public static class OthelloNode
            extends Node {
        public OthelloNode(final PlayerType maxPlayerType, // who is MAX (me)
                final GameView gameView, // current state of the game
                final int depth) // the depth of this node
        {
            super(maxPlayerType, gameView, depth);
        }

        @Override
        public double getTerminalUtility() {
            // TODO: complete me!
            // must be terminal node
            PlayerType[][] cells = this.getGameView().getCells();
            PlayerType me = this.getMaxPlayerType();
            PlayerType opp = this.getGameView().getOtherPlayerType();
            int myCount = 0, oppCount = 0;
            for (PlayerType[] row : cells) {
                for (PlayerType p : row) {
                    if (p == null)
                        continue;
                    if (p == me)
                        myCount++;
                    else if (p == opp)
                        oppCount++;
                }
            }
            int diff = myCount - oppCount;
            double maxCells = this.getGameView().getMaxXDimension() * this.getGameView().getMaxYDimension();
            double utility = diff / maxCells;
            return utility;
        }

        @Override
        public List<Node> getChildren() {
            GameView gamView = this.getGameView();
            // uses the teacher's given frontier function to get valid moves as mentioned
            // earlier in the problem
            Set<Coordinate> PosMoves = gamView.getFrontier(getCurrentPlayerType());

            // creates a new List<Node> to return
            List<Node> returnVal = new ArrayList<Node>();
            // variable used to check if we actually have any possible moves. Remains 0 if
            // no possible moves
            int counterValue = 0;
            Node newNode = new OthelloNode(getCurrentPlayerType(), gamView, this.getDepth() + 1);
            for (Coordinate Coord : PosMoves) {
                // create a new OthelloNode. I tried new Node(), but that requries some weird
                // function and comes out as new Node(){}; Idk what this means.
                newNode = new OthelloNode(getOtherPlayerType(), gamView, this.getDepth() + 1);
                // in the new possibility tree, that possible move is the move that is made.
                newNode.setUtilityValue(counterValue);
                newNode.setLastMove(Coord);
                // add the new node to the list
                returnVal.add(newNode);
                counterValue += 1;
            }
            if (counterValue != 0) {

                return returnVal;
            }
            // if there is no possible moves return null? I assume this is right
            return null;
        }
    }

    private final Random random;

    public OthelloAgent(final PlayerType myPlayerType,
            final long maxMoveThinkingTimeInMS) {
        super(myPlayerType,
                maxMoveThinkingTimeInMS);
        this.random = new Random();
    }

    public final Random getRandom() {
        return this.random;
    }

    @Override
    public OthelloNode makeRootNode(final GameView game) {
        // if you change OthelloNode's constructor, you will want to change this!
        // Note: I am starting the initial depth at 0 (because I like to count up)
        // change this if you want to count depth differently
        return new OthelloNode(this.getMyPlayerType(), game, 0);
    }

    @Override
    public Node treeSearch(Node n) {
        // TODO: complete me!
        // return null;
        return alphaBeta(n, Double.NEGATIVE_INFINITY, Double.POSITIVE_INFINITY, true);
    }

    private Node alphaBeta(Node node, double alpha, double beta, boolean maximizingPlayer) {
        if (node.isTerminal()) {
            return node;
        }

        if (node.getDepth() >= 3) {
            return node;
        }

        List<Node> children = node.getChildren();
        if (children == null || children.isEmpty()) {
            return node;
        }

        Node bestNode = null;
        if (maximizingPlayer) {
            double value = Double.NEGATIVE_INFINITY;
            for (Node child : children) {
                Node candidate = alphaBeta(child, alpha, beta, false);
                double childValue = candidate.isTerminal() ? candidate.getTerminalUtility() : 0.0;
                if (childValue > value || bestNode == null) {
                    value = childValue;
                    bestNode = child;
                }
                alpha = Math.max(alpha, value);
                if (alpha >= beta)
                    break;
            }
        } else {
            double value = Double.POSITIVE_INFINITY;
            for (Node child : children) {
                Node candidate = alphaBeta(child, alpha, beta, true);
                double childValue = candidate.isTerminal() ? candidate.getTerminalUtility() : 0.0;
                if (childValue < value || bestNode == null) {
                    value = childValue;
                    bestNode = child;
                }
                beta = Math.min(beta, value);
                if (beta <= alpha)
                    break;
            }
        }
        return bestNode;
    }

    @Override
    public Coordinate chooseCoordinateToPlaceTile(final GameView game) {
        // TODO: this move will be called once per turn
        // you may want to use this method to add to data structures and whatnot
        // that your algorithm finds useful

        // make the root node
        Node node = this.makeRootNode(game);

        // call tree search
        Node moveNode = this.treeSearch(node);

        // return the move inside that node
        return moveNode.getLastMove();
    }

    @Override
    public void afterGameEnds(final GameView game) {
    }
}

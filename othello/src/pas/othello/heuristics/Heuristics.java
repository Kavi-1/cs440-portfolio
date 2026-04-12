package src.pas.othello.heuristics;

// SYSTEM IMPORTS
import edu.bu.pas.othello.traversal.Node;

// JAVA PROJECT IMPORTS

// imports 
import edu.bu.pas.othello.game.Game.GameView;
import edu.bu.pas.othello.game.PlayerType;
import edu.bu.pas.othello.traversal.Node;
import edu.bu.pas.othello.utils.Coordinate;

public class Heuristics
        extends Object {

    public static double calculateHeuristicValue(Node node) {
        // TODO: complete me!
        GameView Gamview = node.getGameView();
        PlayerType CurP = Gamview.getCurrentPlayerType();
        int NumWins = 0;
        int NumLoss = 0;
        for (PlayerType[] Xco : Gamview.getCells()) {
            for (PlayerType Yco : Xco) {
                if (CurP.equals(Yco)) {
                    NumWins += 1;
                } else {
                    NumLoss += 1;
                }
            }
        }

        return NumWins / Math.max(1, NumLoss) - NumLoss;
    }

}

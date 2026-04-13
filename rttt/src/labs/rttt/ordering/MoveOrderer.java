package src.labs.rttt.ordering;

// SYSTEM IMPORTS
import edu.bu.labs.rttt.game.CellType;
import edu.bu.labs.rttt.game.PlayerType;
import edu.bu.labs.rttt.game.RecursiveTicTacToeGame;
import edu.bu.labs.rttt.game.RecursiveTicTacToeGame.RecursiveTicTacToeGameView;
import edu.bu.labs.rttt.traversal.Node;
import edu.bu.labs.rttt.utils.Coordinate;
import edu.bu.labs.rttt.utils.Pair;

import java.util.IdentityHashMap;
import java.util.List;
import java.util.Map;

import src.labs.rttt.heuristics.Heuristics;

// JAVA PROJECT IMPORTS

public class MoveOrderer
        extends Object {

    public static List<Node> orderChildren(List<Node> children) {
        // this default ordering does no ordering at all and just returns the children
        // in whatever order they
        // were generated in
        // return children.subList(0, children.size() / 2);
        if (children == null || children.size() <= 1)
            return children;

        Node child = children.get(0);

        boolean parentIsMax = (child.getCurrentPlayerType() != child.getMyPlayerType());

        Map<Node, Double> score = new IdentityHashMap<>(children.size());
        for (Node c : children) {
            double s;
            if (c.isTerminal())
                s = c.getTerminalUtility();
            else
                s = Heuristics.calculateHeuristicValue(c);
            score.put(c, s);
        }

        // try sort for now
        children.sort((a, b) -> {
            double va = score.get(a), vb = score.get(b);
            if (parentIsMax)
                // descending
                return Double.compare(vb, va);
            else
                return Double.compare(va, vb);
        });

        return children;
    }

}

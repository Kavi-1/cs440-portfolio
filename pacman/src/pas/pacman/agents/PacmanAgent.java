package src.pas.pacman.agents;

// SYSTEM IMPORTS
import java.util.Random;
import java.util.Set;
import java.util.Stack;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.Queue;

// for astar
import java.util.HashMap;
import java.util.PriorityQueue;
import java.util.Comparator;

// JAVA PROJECT IMPORTS
import edu.bu.pas.pacman.agents.Agent;
import edu.bu.pas.pacman.agents.SearchAgent;
import edu.bu.pas.pacman.interfaces.ThriftyPelletEater;
import edu.bu.pas.pacman.game.Action;
import edu.bu.pas.pacman.game.Game.GameView;
import edu.bu.pas.pacman.game.entity.Pacman;
import edu.bu.pas.pacman.graph.Path;
import edu.bu.pas.pacman.graph.PelletGraph;
import edu.bu.pas.pacman.graph.PelletGraph.PelletVertex;
import edu.bu.pas.pacman.utils.Coordinate;
import edu.bu.pas.pacman.utils.Pair;

public class PacmanAgent
        extends SearchAgent
        implements ThriftyPelletEater {

    private final Random random;

    public PacmanAgent(int myUnitId,
            int pacmanId,
            int ghostChaseRadius) {
        super(myUnitId, pacmanId, ghostChaseRadius);
        this.random = new Random();
    }

    public final Random getRandom() {
        return this.random;
    }

    @Override
    public Set<PelletVertex> getOutoingNeighbors(final PelletVertex vertex,
            final GameView game) {
        Set<PelletVertex> vertices = new HashSet<>();
        Set<Coordinate> coords = vertex.getRemainingPelletCoordinates();
        for (Coordinate coord : coords) {
            PelletVertex v = vertex.removePellet(coord);
            vertices.add(v);
        }
        return vertices;
    }

    @Override
    public float getEdgeWeight(final PelletVertex src,
            final PelletVertex dst) {
        Set<Coordinate> srcPellets = src.getRemainingPelletCoordinates();
        Set<Coordinate> dstPellets = dst.getRemainingPelletCoordinates();

        // diff is the pellet that pacman will eat if it goes to dst
        Coordinate eaten = null;
        for (Coordinate p : srcPellets) {
            if (!dstPellets.contains(p)) {
                eaten = p;
                break;
            }
        }
        if (eaten == null)
            return 0f;

        // manhattan dist
        Coordinate pac = src.getPacmanCoordinate();
        int d = Math.abs(eaten.getXCoordinate() - pac.getXCoordinate())
                + Math.abs(eaten.getYCoordinate() - pac.getYCoordinate());

        return (float) d;

    }

    // for true edge egde in a*, uses BFS, cus for manhattan it ignores walls
    public float getTrueEdgeWeight(final PelletVertex src,
            final PelletVertex dst,
            final GameView game) {
        Set<Coordinate> srcPellets = src.getRemainingPelletCoordinates();
        Set<Coordinate> dstPellets = dst.getRemainingPelletCoordinates();

        Coordinate eaten = null;
        for (Coordinate p : srcPellets) {
            if (!dstPellets.contains(p)) {
                eaten = p;
                break;
            }
        }
        if (eaten == null)
            return 0f;

        Coordinate pac = src.getPacmanCoordinate();
        Path<Coordinate> path = graphSearch(pac, eaten, game);
        if (path == null)
            return Float.POSITIVE_INFINITY;

        return path.getTrueCost();
    }

    @Override
    public float getHeuristic(final PelletVertex src,
            final GameView game) {
        Set<Coordinate> pelletsCoords = src.getRemainingPelletCoordinates();
        if (pelletsCoords.isEmpty())
            return 0f;

        Coordinate pacmanCoord = src.getPacmanCoordinate();
        int pacX = pacmanCoord.getXCoordinate();
        int pacY = pacmanCoord.getYCoordinate();

        int maxDist = Integer.MIN_VALUE;

        // distance of farthest pellet states
        for (Coordinate pellet : pelletsCoords) {
            int x = pellet.getXCoordinate();
            int y = pellet.getYCoordinate();
            int distance = (Math.abs(x - pacX) + Math.abs(y - pacY));
            maxDist = Math.max(distance, maxDist);
        }
        return (float) maxDist;
    }

    @Override
    public Path<PelletVertex> findPathToEatAllPelletsTheFastest(final GameView game) {
        PelletVertex start = new PelletVertex(game);

        if (start.getRemainingPelletCoordinates().isEmpty()) {
            return new Path<>(start);
        }

        PriorityQueue<Path<PelletVertex>> pq = new PriorityQueue<>(
                Comparator.comparingDouble(
                        p -> p.getTrueCost() + getHeuristic(p.getDestination(), game)));
        HashMap<PelletVertex, Float> visited = new HashMap<>();

        Path<PelletVertex> startPath = new Path<>(start);
        pq.offer(startPath);
        visited.put(start, 0f);

        while (!pq.isEmpty()) {
            Path<PelletVertex> curPath = pq.poll();
            PelletVertex cur = curPath.getDestination();

            if (cur.getRemainingPelletCoordinates().isEmpty()) {
                return curPath;
            }

            for (PelletVertex nxt : getOutoingNeighbors(cur, game)) {
                float w = getTrueEdgeWeight(cur, nxt, game);
                float g = curPath.getTrueCost() + w;

                // seen is best cost for nxt so far
                Float seen = visited.get(nxt);
                if (seen == null || g < seen) {
                    visited.put(nxt, g);
                    pq.offer(new Path<>(nxt, w, curPath));
                }
            }
        }
        return new Path<>(start);
    }

    @Override
    public Set<Coordinate> getOutgoingNeighbors(final Coordinate src,
            final GameView game) {

        Set<Coordinate> coords = new HashSet<>();

        for (Action a : Action.values()) {
            if (game.isLegalPacmanMove(src, a)) {
                Coordinate coord = a.apply(src);
                if (!coord.equals(src))
                    coords.add(coord);
            }
        }

        return coords;
    }

    @Override // BFS
    public Path<Coordinate> graphSearch(final Coordinate src,
            final Coordinate tgt,
            final GameView game) {
        Queue<Path<Coordinate>> q = new LinkedList<>();
        Set<Coordinate> visited = new HashSet<>();
        q.offer(new Path<Coordinate>(src));
        visited.add(src);

        while (!q.isEmpty()) {
            Path<Coordinate> p = q.poll();
            Coordinate c = p.getDestination();

            if (c.equals(tgt))
                return p;

            for (Coordinate neighbor : getOutgoingNeighbors(c, game)) {
                if (!visited.contains(neighbor)) {
                    q.offer(new Path<Coordinate>(neighbor, 1f, p));
                    visited.add(neighbor);
                }
            }
        }

        return null;
    }

    @Override
    public void makePlan(final GameView game) {
        Pacman p = (Pacman) game.getEntity(getPacmanId());
        Stack<Coordinate> s = new Stack<>();

        Coordinate src = p.getCurrentCoordinate();
        Coordinate tgt = getTargetCoordinate();

        if (tgt == null) {
            PelletVertex currentVertex = new PelletVertex(game);
            Set<Coordinate> pellets = currentVertex.getRemainingPelletCoordinates();

            if (!pellets.isEmpty()) {
                Coordinate closestPellet = null;
                int minDistance = Integer.MAX_VALUE;

                for (Coordinate pellet : pellets) {
                    int distance = Math.abs(pellet.getXCoordinate() - src.getXCoordinate()) +
                            Math.abs(pellet.getYCoordinate() - src.getYCoordinate());
                    if (distance < minDistance) {
                        minDistance = distance;
                        closestPellet = pellet;
                    }
                }
                tgt = closestPellet;
            }
        }

        if (tgt != null) {
            Path<Coordinate> path = graphSearch(src, tgt, game);

            while (path != null) {
                Coordinate coord = path.getDestination();
                s.push(coord);
                path = path.getParentPath();
            }
        }

        setPlanToGetToTarget(s);
    }

    @Override
    public Action makeMove(final GameView game) {
        Stack<Coordinate> plan = getPlanToGetToTarget();
        Pacman pac = (Pacman) game.getEntity(game.getPacmanId());
        Coordinate pacmanCoord = pac.getCurrentCoordinate();

        if (plan == null || plan.isEmpty()) {
            makePlan(game);
            plan = getPlanToGetToTarget();
        }

        while (!plan.isEmpty() && plan.peek().equals(pacmanCoord)) {
            plan.pop();
        }

        if (plan.isEmpty())
            return Action.values()[this.getRandom().nextInt(Action.values().length)];

        Coordinate nextCoord = plan.peek();
        for (Action a : Action.values()) {
            if (game.isLegalPacmanMove(pacmanCoord, a) && a.apply(pacmanCoord).equals(nextCoord)) {
                plan.pop();
                return a;
            }
        }

        return Action.values()[this.getRandom().nextInt(Action.values().length)];
    }

    @Override
    public void afterGameEnds(final GameView game) {

    }
}

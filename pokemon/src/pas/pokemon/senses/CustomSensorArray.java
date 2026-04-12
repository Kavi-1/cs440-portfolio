package src.pas.pokemon.senses;

import java.util.HashMap;
import java.util.List;

// SYSTEM IMPORTS

// JAVA PROJECT IMPORTS
import edu.bu.pas.pokemon.agents.senses.SensorArray;
import edu.bu.pas.pokemon.core.Agent;
import edu.bu.pas.pokemon.core.Battle.BattleView;
import edu.bu.pas.pokemon.core.Move.MoveView;
import edu.bu.pas.pokemon.core.Pokemon.PokemonView;
import edu.bu.pas.pokemon.core.Team.TeamView;
import edu.bu.pas.pokemon.core.callbacks.Callback;
import edu.bu.pas.pokemon.core.enums.NonVolatileStatus;
import edu.bu.pas.pokemon.core.enums.Stat;
import edu.bu.pas.pokemon.core.enums.Type;
import edu.bu.pas.pokemon.linalg.Matrix;
import edu.bu.pas.pokemon.utils.Pair;

public class CustomSensorArray
        extends SensorArray {

    // TODO: make fields if you want!

    private int myId;
    private boolean HasCheckedAllPk;

    public Matrix calcBattle(MoveView b1, MoveView b2, BattleView state, int b1i, int b2i, double p1, double p2) {
        int i = -1;
        Matrix r = Matrix.zeros(1, 16);
        int prob1 = -1;
        int prob2 = -1;
        // calculates what happens when player 1 uses a move. Doesn't take into account
        // freeze and sleep.
        List<Pair<Double, BattleView>> c = b1.getPotentialEffects(state, b1i, b2i);
        for (Pair<Double, BattleView> l : c) {
            BattleView afterState = l.getSecond();
            PokemonView P1 = afterState.getTeamView(b2i).getPokemonView(b2i);
            int d1 = P1.getLastDamageDealt() / P1.getCurrentStat(Stat.HP) * 100;
            int d2 = 0;
            // checks if the opposing pokemon can use a move. (ie: not fainted and not 0
            // percent chance of hitting)
            if (!afterState.getTeamView(b2i).getActivePokemonView().hasFainted() || p2 != 0) {
                // if the opponent can use a move, itterate over the possibilities that happen
                // when they use that move
                List<Pair<Double, BattleView>> c2 = b2.getPotentialEffects(afterState, b2i, b1i);
                for (Pair<Double, BattleView> l2 : c2) {
                    BattleView afterState2 = l2.getSecond();
                    PokemonView P2 = afterState2.getTeamView(b2i).getPokemonView(b2i);
                    d2 = P2.getLastDamageDealt() / P2.getCurrentStat(Stat.HP) * 100;
                    // probability that this happened
                    l2.getFirst().intValue();
                    prob2 = (int) (l2.getFirst() * p2 * 100);

                }
            } else {

                // probability that his happened. Notably these probabilities can be 0 to
                // represent a pokemon being asleep.
                prob1 = (int) (l.getFirst() * p1 * 100);
            }

            r.set(0, i, d2);
            i++;
            r.set(0, i, d1);
            i++;
            r.set(0, i, prob1);
            i++;
            r.set(0, i, prob2);
            i++;
            r.set(0, i, prob1 * d1);
            i++;
            r.set(0, i, prob2 * d2);
            i++;
        }

        // some code to convert the calculated values into a matrix is needed.
        return r;
    }

    // replay buffer.
    public double checkStatusPercent(NonVolatileStatus l) {
        // uses one number to represent:
        // probability of using a move.
        // percent of the user's speed stat.
        if (NonVolatileStatus.SLEEP == l || NonVolatileStatus.FREEZE == l) {
            return 0;
        }
        if (NonVolatileStatus.PARALYSIS == l) {
            // when paralyzed you have only a 75% chance to use a selected move. And your
            // speed is reduced to 75%.
            return 0.75;
        }
        // default value of 100%
        return 1;
    }

    public CustomSensorArray(int i) {
        this.myId = i;
        this.HasCheckedAllPk = false;
        // i should be equal to this.getMyTeamIdx();
        // this.getMyTeamIdx() can only be called in the PollicyAgent.java, so we need
        // to pass it in and then use deduction to determine the enemy agent id.
        // the ids in getSensorValues are placeholders since we can either be Team1 or
        // Team2.
    }

    HashMap<Type, Integer> typeMap = new HashMap<>();
    HashMap<NonVolatileStatus, Integer> statusMap = new HashMap<>();

    public Matrix getSensorValues(final BattleView state, final MoveView action) {
        // TODO: Convert a BattleView and a MoveView into a row-vector containing
        // measurements for every sense
        // you want your neural network to have. This method should be called if your
        // model is a q-based model
        Matrix l = Matrix.zeros(1, 181);
        TeamView mi = state.getTeamView(0);
        TeamView ei = null;

        if (state.getTeam1View().getBattleIdx() == this.myId) {
            ei = state.getTeam2View();
        } else {
            ei = state.getTeam1View();
        }
        int index = 0;
        int i = 0;
        while (i < 5) {
            try {
                PokemonView mp = mi.getPokemonView(i);
                index++;
                l.set(index, 0, mp.getCurrentStat(Stat.SPD) / 255.0);
                for (MoveView Move : mp.getMoveViews()) {
                    List<Pair<Double, BattleView>> c = Move.getPotentialEffects(state, ei.getBattleIdx(), myId);
                    if (!(c.size() > 50)) {
                        double sum = 0;
                        for (Pair<Double, BattleView> ll : c) {
                            Double prob = ll.getFirst();
                            BattleView afterState = ll.getSecond();
                            TeamView afterTeam = afterState.getTeamView(ei.getBattleIdx());
                            int ophp = afterTeam.getActivePokemonView().getCurrentStat(Stat.HP);

                            Double dprob = Double.valueOf((double) ophp);
                            double ad = prob * dprob;
                            sum += ad;
                        }
                        index++;
                        l.set(index, 0, sum / ei.getActivePokemonView().getCurrentStat(Stat.HP));
                    }
                }

                if (!this.HasCheckedAllPk) {
                    index++;
                    l.set(index, 0, typeMap.get(mp.getCurrentType1()));
                    index++;
                    l.set(0, 0, typeMap.get(mp.getCurrentType2()));
                    index++;
                    l.set(index, 0, mp.getCurrentStat(Stat.SPDEF) / 255.0);
                    index++;
                    l.set(index, 0, mp.getCurrentStat(Stat.DEF) / 255.0);

                }
                index++;
                l.set(index, 0, mp.getCurrentStat(Stat.ACC) / 100.0);
                index++;
                l.set(index, 0, mp.getCurrentStat(Stat.ATK) / 255.0);
                index++;
                l.set(index, 0, mp.getCurrentStat(Stat.SPATK) / 255.0);
                index++;
                l.set(index, 0, mp.getCurrentStat(Stat.EVASIVE) / 100.0);
                index++;
                double initialHp = mp.getInitialStat(Stat.HP);
                l.set(index, 0, (initialHp > 0) ? (mp.getSubstitute().getHP() / initialHp) : 0.0);
                index++;
                Integer status = statusMap.get(mp.getNonVolatileStatus());
                l.set(index, 0, (status != null) ? (status / 6.0) : 0.0);
            } catch (Exception e) {
            }
            try {
                PokemonView ep = ei.getPokemonView(i);
                index++;
                l.set(index, 0, ep.getCurrentStat(Stat.SPD));
                for (MoveView Move : ep.getMoveViews()) {
                    List<Pair<Double, BattleView>> c = Move.getPotentialEffects(state, ei.getBattleIdx(), myId);
                    if (!(c.size() > 50)) {
                        double sum = 0;
                        for (Pair<Double, BattleView> ll : c) {
                            Double prob = ll.getFirst();
                            BattleView afterState = ll.getSecond();
                            TeamView afterTeam = afterState.getTeamView(ei.getBattleIdx());
                            int ophp = afterTeam.getActivePokemonView().getCurrentStat(Stat.HP);

                            Double dprob = Double.valueOf((double) ophp);
                            double ad = prob * dprob;
                            sum += ad;
                        }
                        index++;
                        l.set(index, 0, sum / ei.getActivePokemonView().getCurrentStat(Stat.HP));
                    }
                }
                if (!this.HasCheckedAllPk) {
                    index++;
                    l.set(index, 0, typeMap.get(ep.getCurrentType1()));
                    index++;
                    l.set(0, 0, typeMap.get(ep.getCurrentType2()));
                    index++;
                    l.set(index, 0, ep.getCurrentStat(Stat.SPDEF));
                    index++;
                    l.set(index, 0, ep.getCurrentStat(Stat.DEF));
                    index++;
                }
                l.set(index, 0, ep.getCurrentStat(Stat.ACC));
                index++;
                l.set(index, 0, ep.getCurrentStat(Stat.ATK));
                index++;
                l.set(index, 0, ep.getCurrentStat(Stat.SPATK));
                index++;
                l.set(index, 0, ep.getCurrentStat(Stat.EVASIVE));
                index++;
                l.set(index, 0, ep.getCurrentStat(Stat.HP));
                index++;
                l.set(index, 0, ep.getSubstitute().getHP());
                index++;
                l.set(index, 0, statusMap.get(ep.getNonVolatileStatus()));
            } catch (Exception e) {
            }
            i++;
        }
        return l;
    }
}
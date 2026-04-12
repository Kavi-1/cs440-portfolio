package src.pas.pokemon.rewards;

// SYSTEM IMPORTS

// JAVA PROJECT IMPORTS
import edu.bu.pas.pokemon.agents.rewards.RewardFunction;
import edu.bu.pas.pokemon.agents.rewards.RewardFunction.RewardType;
import edu.bu.pas.pokemon.core.Battle.BattleView;
import edu.bu.pas.pokemon.core.Move.MoveView;
import edu.bu.pas.pokemon.core.Pokemon.PokemonView;
import edu.bu.pas.pokemon.core.Team.TeamView;
import edu.bu.pas.pokemon.core.enums.Stat;

public class CustomRewardFunction
        extends RewardFunction {

    public CustomRewardFunction() {
        // currently configured to produce rewards as a function of the state
        // super(RewardType.STATE);
        super(RewardType.STATE_ACTION_STATE); // state, action, and next state
    }

    public double getLowerBound() {
        // TODO: change this. Reward values must be finite!
        return -1000.0;
    }

    public double getUpperBound() {
        // TODO: change this. Reward values must be finite!
        return 1000.0;
    }

    public double getStateReward(final BattleView state) {
        return 0d;
    }

    public double getStateActionReward(final BattleView state,
            final MoveView action) {
        return 0d;
    }

    public double getStateActionStateReward(final BattleView state,
            final MoveView action,
            final BattleView nextState) {
        double reward = 0.0;

        // get my and opponent teams
        TeamView myTeam = state.getTeamView(0);
        TeamView opponentTeam = state.getTeamView(1);
        TeamView myNextTeam = nextState.getTeamView(0);
        TeamView opponentNextTeam = nextState.getTeamView(1);

        // damaging opponent
        int opponentHpBefore = opponentTeam.getActivePokemonView().getCurrentStat(Stat.HP);
        int opponentHpAfter = opponentNextTeam.getActivePokemonView().getCurrentStat(Stat.HP);
        int damageDone = opponentHpBefore - opponentHpAfter;
        reward += damageDone * 2.0;

        // taking taking
        int myHpBefore = myTeam.getActivePokemonView().getCurrentStat(Stat.HP);
        int myHpAfter = myNextTeam.getActivePokemonView().getCurrentStat(Stat.HP);
        int damageTaken = myHpBefore - myHpAfter;
        reward -= damageTaken * 1.5;

        // knockout opponent pokemon
        if (opponentHpBefore > 0 && opponentHpAfter == 0) {
            reward += 200.0;
        }

        // our pokemon faint
        if (myHpBefore > 0 && myHpAfter == 0) {
            reward -= 150.0;
        }

        // winning the battle
        if (nextState.isOver()) {
            // count alive pokemons
            int myAlive = 0;
            int oppAlive = 0;
            for (int i = 0; i < myNextTeam.size(); i++) {
                if (!myNextTeam.getPokemonView(i).hasFainted()) {
                    myAlive++;
                }
            }
            for (int i = 0; i < opponentNextTeam.size(); i++) {
                if (!opponentNextTeam.getPokemonView(i).hasFainted()) {
                    oppAlive++;
                }
            }
            // reward if we win
            if (myAlive > oppAlive) {
                reward += 500.0;
            } else {
                reward -= 500.0;
            }
        }

        return reward;
    }

}

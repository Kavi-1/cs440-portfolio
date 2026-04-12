package src.pas.pokemon.agents;

// SYSTEM IMPORTS
import net.sourceforge.argparse4j.inf.Namespace;

import edu.bu.pas.pokemon.agents.NeuralQAgent;
import edu.bu.pas.pokemon.agents.senses.SensorArray;
import edu.bu.pas.pokemon.core.Battle.BattleView;
import edu.bu.pas.pokemon.core.Move.MoveView;
import edu.bu.pas.pokemon.linalg.Matrix;
import edu.bu.pas.pokemon.nn.Model;
import edu.bu.pas.pokemon.nn.models.Sequential;
import edu.bu.pas.pokemon.nn.layers.Dense; // fully connected layer
import edu.bu.pas.pokemon.nn.layers.ReLU; // some activations (below too)
import edu.bu.pas.pokemon.nn.layers.Tanh;
import edu.bu.pas.pokemon.nn.layers.Sigmoid;
import edu.bu.pas.pokemon.core.enums.Stat;
import java.util.Random;

// JAVA PROJECT IMPORTS
import src.pas.pokemon.senses.CustomSensorArray;

public class PolicyAgent
        extends NeuralQAgent {

    // f0r exploration
    private double epsilon = 0.1;
    private Random random;

    public PolicyAgent() {
        super();
        this.random = new java.util.Random();
    }

    public void initializeSenses(Namespace args) {
        SensorArray modelSenses = new CustomSensorArray(this.getMyTeamIdx());

        this.setSensorArray(modelSenses);
    }

    @Override
    public void initialize(Namespace args) {
        // make sure you call this, this will call your initModel() and set a field
        // AND if the command line argument "inFile" is present will attempt to set
        // your model with the contents of that file.
        super.initialize(args);

        // what senses will your neural network have?
        this.initializeSenses(args);

        // do what you want just don't expect custom command line options to be
        // available
        // when I'm testing your code
    }

    @Override
    public Model initModel() {
        // TODO: create your neural network

        // we have x features? with two hidden layer
        int x = 181;
        Sequential qFunction = new Sequential();
        qFunction.add(new Dense(x, 128));
        qFunction.add(new Tanh());
        qFunction.add(new Dense(128, 64));
        qFunction.add(new Tanh());
        qFunction.add(new Dense(64, 1));

        return qFunction;
    }

    @Override
    public Integer chooseNextPokemon(BattleView view) {
        // TODO: change this to something more intelligent!

        // choose the Pokemon with most HP
        int bestIdx = -1;
        double bestHpRatio = -1.0;
        // find a pokemon that is alive (and most healthy)
        for (int idx = 0; idx < this.getMyTeamView(view).size(); ++idx) {
            if (!this.getMyTeamView(view).getPokemonView(idx).hasFainted()) {
                // calc HP
                int currentHp = this.getMyTeamView(view).getPokemonView(idx)
                        .getCurrentStat(Stat.HP);
                int maxHp = this.getMyTeamView(view).getPokemonView(idx)
                        .getInitialStat(Stat.HP);
                double hpRatio = (double) currentHp / maxHp;
                if (hpRatio > bestHpRatio) {
                    bestHpRatio = hpRatio;
                    bestIdx = idx;
                }
            }
        }
        return (bestIdx >= 0) ? bestIdx : null;
    }

    @Override
    public MoveView getMove(BattleView view) {
        // TODO: change this to include random exploration during training and maybe use
        // the transition model to make
        // good predictions?
        // if you choose to use the transition model you might want to also override the
        // makeGroundTruth(...) method
        // to not use temporal difference learning

        // currently always tries to argmax the learned model
        // this is not a good idea to always do when training. When playing evaluation
        // games you *do* want to always
        // argmax your model, but when training our model may not know anything yet! So,
        // its a good idea to sometime
        // during training choose *not* to argmax the model and instead choose something
        // new at random.

        // HOW that randomness works and how often you do it are up to you, but it
        // *will* affect the quality of your
        // learned model whether you do it or not!

        if (this.random.nextDouble() < epsilon) {
            // explore
            java.util.List<MoveView> availableMoves = this.getMyTeamView(view).getActivePokemonView()
                    .getAvailableMoves();
            if (availableMoves.isEmpty()) {
                return null;
            }
            return availableMoves.get(this.random.nextInt(availableMoves.size()));
        } else {
            // exploit
            return this.argmax(view);
        }
    }

    @Override
    public void afterGameEnds(BattleView view) {

    }

}

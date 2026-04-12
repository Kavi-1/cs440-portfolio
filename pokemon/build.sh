#!/usr/bin/env bash
set -e
cd "$(dirname "$0")"
mkdir -p bin
javac -cp "lib/pokePA-1.1.0.jar:lib/argparse4j-0.9.0.jar" -d bin $(find src -name '*.java')
echo "Built. Entry points available in pokePA jar:"
echo "  edu.bu.pas.pokemon.CustomBattle      (play a battle)"
echo "  edu.bu.pas.pokemon.RandomBattle      (random matchup)"
echo "  edu.bu.pas.pokemon.Train             (single-threaded training)"
echo "  edu.bu.pas.pokemon.ParallelTrain     (parallel training)"
echo
echo "Example:"
echo "  java -cp bin:lib/pokePA-1.1.0.jar:lib/argparse4j-0.9.0.jar edu.bu.pas.pokemon.CustomBattle --help"

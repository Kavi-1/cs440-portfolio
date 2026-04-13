#!/usr/bin/env bash
set -e
cd "$(dirname "$0")"
mkdir -p bin
javac -cp "lib/*" -d bin $(find src -name '*.java')
echo "Built. Run with:"
echo "  java -cp \"bin:lib/*\" edu.bu.labs.rttt.Main -x src.labs.rttt.agents.DepthThresholdedAlphaBetaAgent -s"

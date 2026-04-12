#!/usr/bin/env bash
set -e
cd "$(dirname "$0")"
mkdir -p bin
javac -cp lib/othello-0.0.2.jar -d bin $(find src -name '*.java')
echo "Built. Run with:"
echo "  java -cp bin:lib/othello-0.0.2.jar edu.bu.pas.othello.Main"

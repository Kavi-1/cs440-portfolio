#!/usr/bin/env bash
set -e
cd "$(dirname "$0")"
mkdir -p bin
javac -cp lib/pacman-0.0.1.jar -d bin $(find src -name '*.java')
echo "Built. Run with:"
echo "  java -cp bin:lib/pacman-0.0.1.jar edu.bu.pas.pacman.Main"

package mineopoly_three;

import mineopoly_three.game.GameEngine;
import mineopoly_three.graphics.UserInterface;
import mineopoly_three.replay.Replay;
import mineopoly_three.replay.ReplayIO;
import mineopoly_three.strategy.MinePlayerStrategy;
import mineopoly_three.strategy.MyStrategy;
import mineopoly_three.strategy.RandomStrategy;

import javax.swing.SwingUtilities;

public class MineopolyMain {
    private static final int DEFAULT_BOARD_SIZE = 20;
    // Adjust with screen size
    private static final int PREFERRED_GUI_WIDTH = 750;
    // Change to true to test win percent
    private static final boolean TEST_STRATEGY_WIN_PERCENT = false;

    // Use this if you want to view a past match replay
    private static final String savedReplayFilePath = null;
    // Use this to save a replay of the current match
    private static final String replayOutputFilePath = null;



    public static void main(String[] args) {
        if (TEST_STRATEGY_WIN_PERCENT) {
            MinePlayerStrategy yourStrategy = new MyStrategy();
            int[] assignmentBoardSizes = new int[]{14, 20, 26, 32};

            for (int testBoardSize : assignmentBoardSizes) {
                double strategyWinPercent = getStrategyWinPercent(yourStrategy, testBoardSize);
                System.out.println("(Board size, win percent): (" + testBoardSize + ", " + strategyWinPercent + ")");
            }
        } else {
            // Not testing the win percent, show the game instead
            playGameOrReplay();
        }
    }

    private static void playGameOrReplay() {
        final GameEngine gameEngine;
        if (savedReplayFilePath == null) {
            // Not viewing a replay, play a game with a GUI instead
            MinePlayerStrategy redStrategy = new MyStrategy();
            MinePlayerStrategy blueStrategy = new RandomStrategy();
            long randomSeed = System.currentTimeMillis();
            gameEngine = new GameEngine(DEFAULT_BOARD_SIZE, redStrategy, blueStrategy, randomSeed);
            gameEngine.setGuiEnabled(true);
        } else {
            // Showing a replay
            gameEngine = ReplayIO.setupEngineForReplay(savedReplayFilePath);
            if (gameEngine == null) {
                return;
            }
        }

        if (gameEngine.isGuiEnabled()) {
            assert PREFERRED_GUI_WIDTH >= 500;
            // Run the GUI code on a separate Thread
            SwingUtilities.invokeLater(() -> UserInterface.instantiateGUI(gameEngine, PREFERRED_GUI_WIDTH));
        }
        gameEngine.runGame();

        // Record the replay if the output path isn't null
        if (savedReplayFilePath == null && replayOutputFilePath != null) {
            Replay gameReplay = gameEngine.getReplay();
            ReplayIO.writeReplayToFile(gameReplay, replayOutputFilePath);
        }
    }

    private static double getStrategyWinPercent(MinePlayerStrategy yourStrategy, int boardSize) {
        final int numTotalRounds = 1000;
        int numRoundsWonByMinScore = 0;
        MinePlayerStrategy randomStrategy = new RandomStrategy();

        for(int i = 0; i < numTotalRounds; i++) {
            GameEngine gameEngine = new GameEngine(boardSize, yourStrategy, randomStrategy, System.currentTimeMillis());
            gameEngine.runGame();
            int redScore = gameEngine.getRedPlayerScore();
            int blueScore = gameEngine.getBluePlayerScore();
            int minScoreToWin = gameEngine.getMinScoreToWin();
            if (redScore - blueScore >= minScoreToWin) {
                numRoundsWonByMinScore ++;
            }
        }
        return ((double) numRoundsWonByMinScore) / numTotalRounds;
    }
}
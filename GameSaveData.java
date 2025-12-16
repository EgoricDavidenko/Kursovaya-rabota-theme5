import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import javafx.scene.paint.Color;

import java.util.ArrayList;

class GameSaveData {
    private static final Logger logger = LogManager.getLogger(GameSaveData.class);

    public int[][] cellValues;
    public Color[][] cellColors;
    public boolean redPlayerTurn;
    public int currentDay;
    public int redUnits, cyanUnits;
    public int redHouses, cyanHouses;
    public int redRice, cyanRice;
    public int redWater, cyanWater;
    public boolean redWateredYesterday, cyanWateredYesterday;
    public ArrayList<Integer> redUnitsHistory, cyanUnitsHistory;
    public ArrayList<Integer> redRiceHistory, cyanRiceHistory;
    public ArrayList<Integer> redWaterHistory, cyanWaterHistory;
    public ArrayList<Integer> redHousesHistory, cyanHousesHistory;
    public String movesHistory;

    public GameSaveData() {
        redUnitsHistory = new ArrayList<>();
        cyanUnitsHistory = new ArrayList<>();
        redRiceHistory = new ArrayList<>();
        cyanRiceHistory = new ArrayList<>();
        redWaterHistory = new ArrayList<>();
        cyanWaterHistory = new ArrayList<>();
        redHousesHistory = new ArrayList<>();
        cyanHousesHistory = new ArrayList<>();
        logger.info("Game was loaded");
    }
}

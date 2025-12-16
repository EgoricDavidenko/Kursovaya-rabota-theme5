import javafx.geometry.Insets;
import javafx.scene.layout.GridPane;
import javafx.scene.paint.Color;

import java.io.*;
import java.util.ArrayList;
import java.util.Random;

public class Field extends GridPane {
    private static final int BOARD_SIZE = 5;
    private static final int[][] DIRECTIONS = {{-1, 0}, {1, 0}, {0, -1}, {0, 1}, {-1, -1}, {-1, 1}, {1, 1}, {1, -1}};

    private Cell[][] cells = new Cell[BOARD_SIZE][BOARD_SIZE];
    private Random random = new Random();
    private Main mainApp;
    public boolean redPlayerTurn = true;
    private int redUnits = 5;
    private int cyanUnits = 5;
    private int redHouses = 1;
    private int cyanHouses = 1;
    private int redRice = 10;
    private int cyanRice = 10;
    private int redWater = 3;
    private int cyanWater = 3;
    private boolean redWateredYesterday = false;
    private boolean cyanWateredYesterday = false;
    private Cell selectedCell = null;
    public int currentDay = 0;

    private ArrayList<Integer> redUnitsHistory = new ArrayList<>();
    private ArrayList<Integer> cyanUnitsHistory = new ArrayList<>();
    private ArrayList<Integer> redRiceHistory = new ArrayList<>();
    private ArrayList<Integer> cyanRiceHistory = new ArrayList<>();
    private ArrayList<Integer> redWaterHistory = new ArrayList<>();
    private ArrayList<Integer> cyanWaterHistory = new ArrayList<>();
    private ArrayList<Integer> redHousesHistory = new ArrayList<>();
    private ArrayList<Integer> cyanHousesHistory = new ArrayList<>();

    public Field(Main mainApp) {
        this.mainApp = mainApp;
        initializeField();
        setupField();
        recordDayStats();
    }

    public Field(Main mainApp, GameSaveData saveData) {
        this.mainApp = mainApp;
        initializeField();
        loadFromSaveData(saveData);
    }

    private void initializeField() {
        setHgap(2);
        setVgap(2);
        setPadding(new Insets(20));
    }

    private void setupField() {
        for (int row = 0; row < BOARD_SIZE; row++) {
            for (int col = 0; col < BOARD_SIZE; col++) {
                int cellValue = random.nextInt(20) + 1;
                Cell cell = new Cell(cellValue, row, col, this);
                cells[row][col] = cell;
                add(cell, col, row);
            }
        }
        setStartingCells();
    }

    private void setStartingCells() {
        cells[4][4].setColor(Color.RED);
        cells[4][4].setCellValue(0);
        cells[0][0].setColor(Color.CYAN);
        cells[0][0].setCellValue(0);
    }

    private void loadFromSaveData(GameSaveData saveData) {
        redPlayerTurn = saveData.redPlayerTurn;
        currentDay = saveData.currentDay;

        redUnits = saveData.redUnits;
        cyanUnits = saveData.cyanUnits;
        redHouses = saveData.redHouses;
        cyanHouses = saveData.cyanHouses;
        redRice = saveData.redRice;
        cyanRice = saveData.cyanRice;
        redWater = saveData.redWater;
        cyanWater = saveData.cyanWater;
        redWateredYesterday = saveData.redWateredYesterday;
        cyanWateredYesterday = saveData.cyanWateredYesterday;

        redUnitsHistory = saveData.redUnitsHistory;
        cyanUnitsHistory = saveData.cyanUnitsHistory;
        redRiceHistory = saveData.redRiceHistory;
        cyanRiceHistory = saveData.cyanRiceHistory;
        redWaterHistory = saveData.redWaterHistory;
        cyanWaterHistory = saveData.cyanWaterHistory;
        redHousesHistory = saveData.redHousesHistory;
        cyanHousesHistory = saveData.cyanHousesHistory;

        for (int row = 0; row < BOARD_SIZE; row++) {
            for (int col = 0; col < BOARD_SIZE; col++) {
                Cell cell = new Cell(saveData.cellValues[row][col], row, col, this);
                cell.setColor(saveData.cellColors[row][col]);
                if (!saveData.cellColors[row][col].equals(Color.WHITE)) {
                    cell.setCellValue(0);
                    ;
                }
                cells[row][col] = cell;
                add(cell, col, row);
            }
        }
    }

    public void saveGameStatus(File file, String movesHistory) throws IOException {
        try (PrintWriter writer = new PrintWriter(new FileWriter(file))) {
            saveGameStatistics(writer);
            saveFieldState(writer);
            saveHistory(writer, movesHistory);
        }
    }

    private void saveGameStatistics(PrintWriter writer) {
        writer.println("RED_PLAYER_TURN=" + redPlayerTurn);
        writer.println("CURRENT_DAY=" + currentDay);

        writer.println("RED_UNITS=" + redUnits);
        writer.println("CYAN_UNITS=" + cyanUnits);
        writer.println("RED_HOUSES=" + redHouses);
        writer.println("CYAN_HOUSES=" + cyanHouses);
        writer.println("RED_RICE=" + redRice);
        writer.println("CYAN_RICE=" + cyanRice);
        writer.println("RED_WATER=" + redWater);
        writer.println("CYAN_WATER=" + cyanWater);
        writer.println("RED_WATERED_YESTERDAY=" + redWateredYesterday);
        writer.println("CYAN_WATERED_YESTERDAY=" + cyanWateredYesterday);

        writer.println("RED_UNITS_HISTORY=" + arrayListToString(redUnitsHistory));
        writer.println("CYAN_UNITS_HISTORY=" + arrayListToString(cyanUnitsHistory));
        writer.println("RED_RICE_HISTORY=" + arrayListToString(redRiceHistory));
        writer.println("CYAN_RICE_HISTORY=" + arrayListToString(cyanRiceHistory));
        writer.println("RED_WATER_HISTORY=" + arrayListToString(redWaterHistory));
        writer.println("CYAN_WATER_HISTORY=" + arrayListToString(cyanWaterHistory));
        writer.println("RED_HOUSES_HISTORY=" + arrayListToString(redHousesHistory));
        writer.println("CYAN_HOUSES_HISTORY=" + arrayListToString(cyanHousesHistory));
    }

    private void saveFieldState(PrintWriter writer) {
        writer.println("FIELD_STATUS_START");
        for (int row = 0; row < BOARD_SIZE; row++) {
            for (int col = 0; col < BOARD_SIZE; col++) {
                Cell cell = cells[row][col];
                writer.println(row + "," + col + "," + cell.cellValue + "," + colorToString(cell.cellColor));
            }
        }
        writer.println("FIELD_STATUS_END");
    }

    private void saveHistory(PrintWriter writer, String movesHistory) {
        writer.println("MOVE_HISTORY_START");
        writer.println(movesHistory.replace("\n", "\\n"));
        writer.println("MOVES_HISTORY_END");
    }

    public static GameSaveData loadGameFromSave(File file) throws IOException, InvalidSaveFileException {
        GameSaveData saveData = new GameSaveData();

        try (BufferedReader reader = new BufferedReader(new FileReader(file))) {
            String line;
            boolean readingField = false;
            boolean readingHistory = false;
            StringBuilder movesHistory = new StringBuilder();

            while ((line = reader.readLine()) != null) {
                if (line.equals("FIELD_STATUS_START")) {
                    readingField = true;
                    continue;
                } else if (line.equals("FIELD_STATUS_END")) {
                    readingField = false;
                    continue;
                } else if (line.equals("MOVE_HISTORY_START")) {
                    readingHistory = true;
                    continue;
                } else if (line.equals("MOVES_HISTORY_END")) {
                    readingHistory = false;
                    continue;
                }

                if (readingField) {
                    parseFieldLine(saveData, line);
                } else if (readingHistory) {
                    movesHistory.append(line.replace("\\n", "\n"));
                } else if (line.contains("=")) {
                    parseStatistic(saveData, line);
                }
            }

            saveData.movesHistory = movesHistory.toString();

            if (saveData.cellValues == null || saveData.cellColors == null) {
                throw new InvalidSaveFileException("Отсутствует состояние игрового поля");
            }

            return saveData;
        } catch (NumberFormatException | ArrayIndexOutOfBoundsException e) {
            throw new InvalidSaveFileException("Неверный формат данных: " + e.getMessage());
        }
    }

    private static void parseFieldLine(GameSaveData saveData, String line) throws InvalidSaveFileException {
        String[] parts = line.split(",");
        if (parts.length != 4) {
            throw new InvalidSaveFileException("Неверный формат строки клетки: " + line);
        }

        int row = Integer.parseInt(parts[0]);
        int col = Integer.parseInt(parts[1]);
        int cellValue = Integer.parseInt(parts[2]);
        Color color = stringToColor(parts[3]);

        if (saveData.cellValues == null) {
            saveData.cellValues = new int[5][5];
            saveData.cellColors = new Color[5][5];
        }

        saveData.cellValues[row][col] = cellValue;
        saveData.cellColors[row][col] = color;
    }

    private static void parseStatistic(GameSaveData saveData, String line) throws InvalidSaveFileException {
        String[] parts = line.split("=", 2);
        if (parts.length != 2) {
            throw new InvalidSaveFileException("Неверный формат свойства: " + line);
        }

        String key = parts[0];
        String value = parts[1];

        switch (key) {
            case "RED_PLAYER_TURN":
                saveData.redPlayerTurn = Boolean.parseBoolean(value);
                break;
            case "CURRENT_DAY":
                saveData.currentDay = Integer.parseInt(value);
                break;
            case "RED_UNITS":
                saveData.redUnits = Integer.parseInt(value);
                break;
            case "CYAN_UNITS":
                saveData.cyanUnits = Integer.parseInt(value);
                break;
            case "RED_HOUSES":
                saveData.redHouses = Integer.parseInt(value);
                break;
            case "CYAN_HOUSES":
                saveData.cyanHouses = Integer.parseInt(value);
                break;
            case "RED_RICE":
                saveData.redRice = Integer.parseInt(value);
                break;
            case "CYAN_RICE":
                saveData.cyanRice = Integer.parseInt(value);
                break;
            case "RED_WATER":
                saveData.redWater = Integer.parseInt(value);
                break;
            case "CYAN_WATER":
                saveData.cyanWater = Integer.parseInt(value);
                break;
            case "RED_WATERED_YESTERDAY":
                saveData.redWateredYesterday = Boolean.parseBoolean(value);
                break;
            case "CYAN_WATERED_YESTERDAY":
                saveData.cyanWateredYesterday = Boolean.parseBoolean(value);
                break;
            case "RED_UNITS_HISTORY":
                saveData.redUnitsHistory = stringToArrayList(value);
                break;
            case "CYAN_UNITS_HISTORY":
                saveData.cyanUnitsHistory = stringToArrayList(value);
                break;
            case "RED_RICE_HISTORY":
                saveData.redRiceHistory = stringToArrayList(value);
                break;
            case "CYAN_RICE_HISTORY":
                saveData.cyanRiceHistory = stringToArrayList(value);
                break;
            case "RED_WATER_HISTORY":
                saveData.redWaterHistory = stringToArrayList(value);
                break;
            case "CYAN_WATER_HISTORY":
                saveData.cyanWaterHistory = stringToArrayList(value);
                break;
            case "RED_HOUSES_HISTORY":
                saveData.redHousesHistory = stringToArrayList(value);
                break;
            case "CYAN_HOUSES_HISTORY":
                saveData.cyanHousesHistory = stringToArrayList(value);
                break;
            default:
                throw new InvalidSaveFileException("Неизвестный ключ: " + key);
        }
    }

    private String arrayListToString(ArrayList<Integer> list) {
        StringBuilder sb = new StringBuilder();
        for (int i = 0; i < list.size(); i++) {
            sb.append(list.get(i));
            if (i < list.size() - 1) sb.append(",");
        }
        return sb.toString();
    }

    private static ArrayList<Integer> stringToArrayList(String str) {
        ArrayList<Integer> list = new ArrayList<>();
        if (str != null && !str.isEmpty()) {
            for (String part : str.split(",")) {
                list.add(Integer.parseInt(part.trim()));
            }
        }
        return list;
    }

    private String colorToString(Color color) {
        if (color.equals(Color.RED)) {
            return "RED";
        }
        if (color.equals(Color.CYAN)) {
            return "CYAN";
        }
        return "WHITE";
    }

    private static Color stringToColor(String str) {
        switch (str) {
            case "RED":
                return Color.RED;
            case "CYAN":
                return Color.CYAN;
            default:
                return Color.WHITE;
        }
    }

    private void endTurn() {
        if (redPlayerTurn) {
            processTurn(redUnits, redHouses, redRice, redWateredYesterday, Color.RED);
        } else {
            processTurn(cyanUnits, cyanHouses, cyanRice, cyanWateredYesterday, Color.CYAN);
        }

        mainApp.updateStats();
        mainApp.checkWinner();

        if (!redPlayerTurn) {
            currentDay++;
            mainApp.addToHistory("День " + currentDay, Color.BLACK);
            recordDayStats();
        }
    }

    private void processTurn(int units, int houses, int rice, boolean wateredYesterday, Color color) {
        units += houses;
        if (wateredYesterday) {
            rice += 3;
            mainApp.addToHistory("Пополнение юнитов: +" + houses + ", рис: +3 (полив)", color);
        } else {
            rice += 1;
            mainApp.addToHistory("Пополнение юнитов: +" + houses + ", рис: +1", color);
        }

        if (color.equals(Color.RED)) {
            redUnits = units;
            redRice = rice;
            redWateredYesterday = false;
        } else {
            cyanUnits = units;
            cyanRice = rice;
            cyanWateredYesterday = false;
        }
    }

    private void recordDayStats() {
        redUnitsHistory.add(redUnits);
        cyanUnitsHistory.add(cyanUnits);
        redRiceHistory.add(redRice);
        cyanRiceHistory.add(cyanRice);
        redWaterHistory.add(redWater);
        cyanWaterHistory.add(cyanWater);
        redHousesHistory.add(redHouses);
        cyanHousesHistory.add(cyanHouses);
    }

    public void attemptCapture(Cell targetCell) {
        Color currentColor = null;
        int currentUnits = 0;
        if (redPlayerTurn) {
            currentColor = Color.RED;
            currentUnits = redUnits;
        } else {
            currentColor = Color.CYAN;
            currentUnits = cyanUnits;
        }

        if (!isColoredNear(targetCell, currentColor)) {
            return;
        }
        if (currentUnits < targetCell.cellValue || mainApp.highlightedCell == null) {
            return;
        }

        if (redPlayerTurn) {
            redUnits -= targetCell.cellValue;
        } else {
            cyanUnits -= targetCell.cellValue;
        }

        mainApp.highlightedCell = null;
        mainApp.addToHistory("Освоение территории, потрачено " + targetCell.cellValue + " юнитов", currentColor);

        targetCell.setColor(currentColor);
        targetCell.setCellValue(0);
        endTurn();
        redPlayerTurn = !redPlayerTurn;
    }

    public void buildHouse() {
        if (redPlayerTurn) {
            if (!canBuildHouse(redUnits, redRice, redWater)) {
                return;
            }
            redUnits -= 2;
            redRice -= 10;
            redWater -= 1;
            redHouses++;
            mainApp.addToHistory("Постройка дома (потрачено: 2 юнита, 10 риса, 1 вода)", Color.RED);
        } else {
            if (!canBuildHouse(cyanUnits, cyanRice, cyanWater)) {
                return;
            }
            cyanUnits -= 2;
            cyanRice -= 10;
            cyanWater -= 1;
            cyanHouses++;
            mainApp.addToHistory("Постройка дома (потрачено: 2 юнита, 10 риса, 1 вода)", Color.CYAN);
        }
        endTurn();
        redPlayerTurn = !redPlayerTurn;
    }

    private boolean canBuildHouse(int units, int rice, int water) {
        return units >= 2 && rice >= 10 && water >= 1;
    }

    public void collectWater() {
        if (redPlayerTurn) {
            redWater += 1;
            mainApp.addToHistory("Набор воды (+1 вода)", Color.RED);
        } else {
            cyanWater += 1;
            mainApp.addToHistory("Набор воды (+1 вода)", Color.CYAN);
        }
        endTurn();
        redPlayerTurn = !redPlayerTurn;
    }

    public void waterRice() {
        if (redPlayerTurn) {
            if (redWater < 1) {
                return;
            }
            redWater -= 1;
            mainApp.addToHistory("Полив риса (потрачено: 1 вода, рис: +1 сейчас, +3 в след. ход)", Color.RED);
            endTurn();
            redWateredYesterday = true;
        } else {
            if (cyanWater < 1) {
                return;
            }
            cyanWater -= 1;
            mainApp.addToHistory("Полив риса (потрачено: 1 вода, рис: +1 сейчас, +3 в след. ход)", Color.CYAN);
            endTurn();
            cyanWateredYesterday = true;
        }
        redPlayerTurn = !redPlayerTurn;
    }

    private boolean isColoredNear(Cell target, Color color) {
        for (int[] d : DIRECTIONS) {
            int r = target.fieldRow + d[0];
            int c = target.fieldCol + d[1];
            if (r >= 0 && r < BOARD_SIZE && c >= 0 && c < BOARD_SIZE) {
                if (cells[r][c].cellColor.equals(color)) {
                    return true;
                }
            }
        }
        return false;
    }

    public void cellSelection(Cell clickedCell) {
        if (selectedCell != null) selectedCell.setHighlighted(false);
        selectedCell = clickedCell;
        clickedCell.setHighlighted(true);
        mainApp.setHighlightedCell(clickedCell);
    }

    public void performAITurn() {
        if (redPlayerTurn) {
            return;
        }

        Cell cellToCapture = findCellToCapture();
        if (cellToCapture != null) {
            if (selectedCell != null) selectedCell.setHighlighted(false);
            selectedCell = cellToCapture;
            cellToCapture.setHighlighted(true);
            mainApp.setHighlightedCell(cellToCapture);
            attemptCapture(cellToCapture);
            return;
        }

        if (canBuildHouse(cyanUnits, cyanRice, cyanWater)) {
            buildHouse();
            return;
        }

        if (cyanWater >= 1) {
            waterRice();
            return;
        }

        collectWater();
    }

    private Cell findCellToCapture() {
        for (int row = 0; row < BOARD_SIZE; row++) {
            for (int col = 0; col < BOARD_SIZE; col++) {
                Cell cell = cells[row][col];
                if (cell.cellColor.equals(Color.WHITE) && isColoredNear(cell, Color.CYAN)) {
                    if (cyanUnits >= cell.cellValue) {
                        return cell;
                    }
                }
            }
        }
        return null;
    }

    public void showStatsGraphs() {
        StatsGraphWindow graphWindow = new StatsGraphWindow(mainApp.currentStage);
        graphWindow.addGraph("Рис", redRiceHistory, cyanRiceHistory);
        graphWindow.addGraph("Вода", redWaterHistory, cyanWaterHistory);
        graphWindow.addGraph("Юниты", redUnitsHistory, cyanUnitsHistory);
        graphWindow.addGraph("Дома", redHousesHistory, cyanHousesHistory);
        graphWindow.show();
    }

    public int getRedUnits() {
        return redUnits;
    }

    public int getCyanUnits() {
        return cyanUnits;
    }

    public int getRedHouses() {
        return redHouses;
    }

    public int getCyanHouses() {
        return cyanHouses;
    }

    public int getRedRice() {
        return redRice;
    }

    public int getCyanRice() {
        return cyanRice;
    }

    public int getRedWater() {
        return redWater;
    }

    public int getCyanWater() {
        return cyanWater;
    }

    public boolean isRedWateredYesterday() {
        return redWateredYesterday;
    }

    public boolean isCyanWateredYesterday() {
        return cyanWateredYesterday;
    }

    public int countRedCells() {
        return countCellsByColor(Color.RED);
    }

    public int countCyanCells() {
        return countCellsByColor(Color.CYAN);
    }

    private int countCellsByColor(Color color) {
        int count = 0;
        for (int i = 0; i < BOARD_SIZE; i++) {
            for (int j = 0; j < BOARD_SIZE; j++) {
                if (cells[i][j].cellColor.equals(color)) count++;
            }
        }
        return count;
    }
}

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import javafx.application.Application;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.layout.*;
import javafx.scene.paint.Color;
import javafx.scene.text.Font;
import javafx.stage.FileChooser;
import javafx.stage.Modality;
import javafx.stage.Stage;

import java.io.*;

public class Main extends Application {
    private static final Logger logger = LogManager.getLogger(Main.class);

    public Stage currentStage;
    private Field field;
    public Cell highlightedCell = null;
    private Label redStats;
    private Label cyanStats;
    private TextArea movesHistory;
    private boolean winScreenShowed = false;
    public GameSaveData saveData = null;

    @Override
    public void start(Stage currentStage) {
        this.currentStage = currentStage;
        showMainMenu();
        currentStage.setTitle("Rice war");
        currentStage.show();
    }

    private void showMainMenu() {
        MyButton newGameButton = new MyButton("Начать новую игру", 200, 130);
        newGameButton.setOnAction(e -> {
            winScreenShowed = false;
            saveData = null;
            showGame();
            showRules();
        });

        MyButton loadButton = new MyButton("Загрузить игру", 200, 190);
        loadButton.setOnAction(e -> loadGame());
        MyButton exitButton = new MyButton("Выйти", 200, 250);
        exitButton.setOnAction(e -> {
            currentStage.close();
            logger.info("Exit");
        });

        Scene menuScene = new Scene(new Pane(newGameButton, loadButton, exitButton), 600, 500);
        currentStage.setScene(menuScene);
    }

    private void showGame() {
        winScreenShowed = false;
        if (saveData != null){
            field = new Field(this, saveData);
        } else {
            field = (new Field(this));
        }
        saveData = null;
        highlightedCell = null;

        redStats = createStatsLabel(500, 10);
        cyanStats = createStatsLabel(500, 140);
        movesHistory = createHistoryTextArea(500, 270);

        updateStats();

        movesHistory.setText("Начало игры!\n");
        if (field.redPlayerTurn) {
            field.currentDay++;
            addToHistory("День " + field.currentDay, Color.BLACK);
        }

        Scene gameScene = createGameScene();
        currentStage.setScene(gameScene);
        logger.info("Field was created");
    }

    private Label createStatsLabel(int x, int y) {
        Label label = new Label();
        label.setFont(Font.font(14));
        label.setLayoutX(x);
        label.setLayoutY(y);
        return label;
    }

    private TextArea createHistoryTextArea(int x, int y) {
        TextArea textArea = new TextArea();
        textArea.setLayoutX(x);
        textArea.setLayoutY(y);
        textArea.setPrefSize(200, 250);
        textArea.setEditable(false);
        textArea.setWrapText(true);
        return textArea;
    }

    private Scene createGameScene() {
        MyButton menuButton = new MyButton("Выйти в меню", 750, 10);
        MyButton saveButton = new MyButton("Сохранить игру", 750, 70);
        MyButton captureCellButton = new MyButton("Захватить", 750,150);
        MyButton buildHouseButton = new MyButton("Построить дом", 750,210);
        MyButton collectWaterButton = new MyButton("Набрать воды", 750, 270);
        MyButton waterRiceButton = new MyButton("Полить рис", 750, 330);
        MyButton statsGraphButton = new MyButton("Графики ресурсов", 750, 410);
        menuButton.setOnAction(e -> {
            showMainMenu();
            logger.info("Exit to main menu");
        });
        saveButton.setOnAction(e -> saveGame());
        captureCellButton.setOnAction(e -> {
            if (highlightedCell != null) {
                field.attemptCapture(highlightedCell);
                field.performAITurn();
            }
        });
        buildHouseButton.setOnAction(e -> {
            field.buildHouse();
            field.performAITurn();
        });
        collectWaterButton.setOnAction(e -> {
            field.collectWater();
            field.performAITurn();
        });
        waterRiceButton.setOnAction(e -> {
            field.waterRice();
            field.performAITurn();
        });
        statsGraphButton.setOnAction(e -> field.showStatsGraphs());

        return new Scene(new Pane(
                redStats,cyanStats,movesHistory,field, menuButton, saveButton, captureCellButton,
                buildHouseButton, collectWaterButton, waterRiceButton, statsGraphButton), 1000, 700);
    }

    private void saveGame() {
        try {
            FileChooser fileChooser = createFileChooser("Сохранить игру", "Сохранить");
            File file = fileChooser.showSaveDialog(currentStage);
            if (file != null) {
                field.saveGameStatus(file, movesHistory.getText());
                logger.info("Game is saved");
                showAlert("Сохранение", "Игра успешно сохранена в файл: " + file.getName());
            }
        } catch (Exception e) {
            showAlert("Ошибка сохранения", "Не удалось сохранить игру: " + e.getMessage());
        }
    }

    private void loadGame() {
        try {
            FileChooser fileChooser = createFileChooser("Загрузить игру", "Загрузить");
            File file = fileChooser.showOpenDialog(currentStage);
            if (file != null) {
                saveData = Field.loadGameFromSave(file);
                showGame();
                showAlert("Загрузка", "Игра успешно загружена из файла: " + file.getName());
            }
        } catch (InvalidSaveFileException e) {
            showAlert("Ошибка загрузки", "Файл сохранения поврежден или имеет неверный формат");
        } catch (Exception e) {
            showAlert("Ошибка загрузки", "Не удалось загрузить игру");
        }
    }

    private FileChooser createFileChooser(String title, String buttonText) {
        FileChooser fileChooser = new FileChooser();
        fileChooser.setTitle(title);
        fileChooser.getExtensionFilters().add(
                new FileChooser.ExtensionFilter("Текстовые файлы", "*.txt")
        );
        if (buttonText.equals("Сохранить")) {
            fileChooser.setInitialFileName("ricewar_save.txt");
        }
        return fileChooser;
    }

    private void showAlert(String title, String message) {
        Alert alert = new Alert(Alert.AlertType.INFORMATION);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(message);
        alert.showAndWait();
    }

    public void setHighlightedCell(Cell cell) {
        highlightedCell = cell;
    }

    public void updateStats() {
        redStats.setText(createPlayerStats("Красный игрок", field.getRedUnits(),
                field.getRedHouses(), field.getRedRice(), field.getRedWater(),
                field.isRedWateredYesterday()));
        cyanStats.setText(createPlayerStats("Голубой игрок", field.getCyanUnits(),
                field.getCyanHouses(), field.getCyanRice(), field.getCyanWater(),
                field.isCyanWateredYesterday()));
    }

    private String createPlayerStats(String playerName, int units, int houses, int rice, int water, boolean watered) {
        String playerStats = playerName + "\nЮниты: " + units + "\nДома: " + houses + "\nРис: " + rice + "\nВода: " + water;
        if (watered){
            playerStats += "\n(рис полит)";
        }
        return playerStats;
    }

    public void checkWinner() {
        int redCells = field.countRedCells();
        int cyanCells = field.countCyanCells();
        int totalCells = 25;
        if (redCells > totalCells / 2) {
            if (!winScreenShowed) showWinScreen("Вы победили!");
        } else if (cyanCells > totalCells / 2) {
            if (!winScreenShowed) showWinScreen("Вы проиграли!");
        }
    }

    private void showWinScreen(String message) {
        Stage winStage = new Stage();
        winStage.initModality(Modality.WINDOW_MODAL);
        winStage.initOwner(currentStage);
        winStage.setOnCloseRequest(e -> e.consume());
        winScreenShowed = true;

        Label winLabel = new Label(message);
        winLabel.setFont(Font.font(20));
        winLabel.setLayoutX(80);
        winLabel.setLayoutY(50);

        MyButton menuButton = new MyButton("Выйти в меню", 50, 100);
        menuButton.setOnAction(e -> {
            winStage.close();
            showMainMenu();
        });

        Scene winScene = new Scene(new Pane(winLabel, menuButton), 300, 200);

        winStage.setScene(winScene);
        winStage.showAndWait();
    }

    public void addToHistory(String message, Color color) {
        String prefix = "";
        if (color.equals(Color.RED)){
            prefix = "Красный: ";
        } else if (color.equals(Color.CYAN)) {
            prefix = "Голубой: ";
        }
        movesHistory.appendText(prefix + message + "\n");
    }

    private void showRules() {
        Stage rulesWindow = new Stage();
        rulesWindow.initModality(Modality.WINDOW_MODAL);
        rulesWindow.initOwner(currentStage);

        Label rulesLabel = new Label(
                "Здравствуйте, новый феодал!\nБуду краток. Это поле — Ваша земля. Её дали Вам. Там, на другом конце поля, — земля другого, \n" +
                        "такого же как Вы, но другого. Вы - красный справа снизу. Он - синий слева наверху. \n" +
                        "Задача для каждого из вас состоит в том, чтобы как можно быстрее захватывать поле. \n" +
                        "Кто первый захватит 50% поля, тот и победил.\n" +
                        "\nДля захвата одного квадрата поля нужны крестьяне. Количество крестьян, \n" +
                        "необходимое для захвата квадрата написано внутри самого квадрата. \nВ начале игры у Вас и Вашего соперника по 5 крестьян. \n" +
                        "\nДля пополнения рядов крестьян используются дома крестьян. \nВ конце Вашего хода из каждого имеющегося у Вас дома вылезает по одному крестьянину. \nВ начале игры у Вас и Вашего соперника по 1 дому. \n" +
                        "\nМожно приказать построить дом крестьянина. Для постройки дома необходимо потратить \nнекоторые ресурсы. Помимо уже известных Вам крестьян, для постройки дома необходимы \nрис и вода. Рис растёт ежедневно, и его ежедневно собирают (сначала собирают, потом растёт)." +
                        "\nМожно приказать полить рис, чтобы наследующий день его выросло больше. \nЕсли в этот день Вы приказали полить рис, а в прошлый день не приказывали, \n" +
                        "то в конце этого дня Ваши крестьяне соберут 1 рис, а в конце следующего - 3 риса." +
                        "\n\nЧтобы полить рис нужна вода. Воду могут набрать крестьяне по Вашему приказу, \nно сколько бы у Вас ни было крестьян, лейка у Вас всего одна, \n" +
                        "поэтому и количество воды в Ваших запасах всегда будет увеличиваться на 1" +
                        "\nДля того, чтобы построить дом, необходимо истратить двух крестьян, 10 порций риса и \n1 порцию воды." +
                        "\n\nПомните, что за один день Вы можете отдать лишь один приказ: захватить территорию, \nпостроить дом, набрать воды или полить рис." +
                        "\n\nРаспоряжайтесь своими ресурсами правильно, выстраивайте стратегию, и Вы в два счёта выгоните \nсвоего синего соседа со ВАШЕГО поля. Если конечно удача Вам позволит это сделать, \nведь никто не застрахован от случайностей. Успехов!"
        );
        rulesLabel.setFont(Font.font(15));
        rulesLabel.setLayoutX(20);
        rulesLabel.setLayoutY(20);

        MyButton okButton = new MyButton("Понятно", 250, 800);
        okButton.setOnAction(e -> rulesWindow.close());

        Scene rulesScene = new Scene(new Pane(rulesLabel, okButton), 700, 900);
        rulesWindow.setScene(rulesScene);
        rulesWindow.showAndWait();
    }

    public static void main(String[] args) {
        logger.info("The application 'Rice_war' was started");
        launch(args);
    }
}


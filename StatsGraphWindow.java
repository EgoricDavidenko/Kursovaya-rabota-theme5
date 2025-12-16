import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import javafx.geometry.Insets;
import javafx.scene.Scene;
import javafx.scene.chart.LineChart;
import javafx.scene.chart.NumberAxis;
import javafx.scene.chart.XYChart;
import javafx.scene.control.ScrollPane;
import javafx.scene.layout.VBox;
import javafx.stage.Modality;
import javafx.stage.Stage;

import java.util.ArrayList;

class StatsGraphWindow {
    private static final Logger logger = LogManager.getLogger(StatsGraphWindow.class);

    private Stage graphsStage;
    private VBox graphsContainer;
    private static final int GRAPH_HEIGHT = 300;
    private static final int GRAPH_WIDTH = 1000;
    private static final int WINDOW_WIDTH = 1200;
    private static final int WINDOW_HEIGHT = 1000;

    public StatsGraphWindow(Stage owner) {
        graphsStage = new Stage();
        graphsStage.initModality(Modality.WINDOW_MODAL);
        graphsStage.initOwner(owner);
        graphsStage.setTitle("Графики изменения ресурсов");
        graphsStage.setWidth(WINDOW_WIDTH);
        graphsStage.setHeight(WINDOW_HEIGHT);

        graphsContainer = new VBox(15);
        graphsContainer.setPadding(new Insets(15));
    }

    public void addGraph(String title, ArrayList<Integer> redData, ArrayList<Integer> cyanData) {
        LineChart<Number, Number> graph = createGraph(title, "Дни", "Количество");

        XYChart.Series<Number, Number> redGraphs = createGraphs("Красный игрок (Вы)", redData);
        XYChart.Series<Number, Number> cyanGraphs = createGraphs("Голубой игрок (ИИ)", cyanData);

        graph.getData().addAll(redGraphs, cyanGraphs);
        graphsContainer.getChildren().add(graph);
    }

    private LineChart<Number, Number> createGraph(String title, String xLabel, String yLabel) {
        NumberAxis xAxis = new NumberAxis();
        xAxis.setLabel(xLabel);
        NumberAxis yAxis = new NumberAxis();
        yAxis.setLabel(yLabel);
        yAxis.setAutoRanging(true);
        yAxis.setForceZeroInRange(false);
        LineChart<Number, Number> graph = new LineChart<>(xAxis, yAxis);
        graph.setTitle(title);
        graph.setPrefHeight(GRAPH_HEIGHT);
        graph.setPrefWidth(GRAPH_WIDTH);
        graph.setCreateSymbols(true);
        graph.setLegendVisible(true);
        graph.setAnimated(false);
        return graph;
    }

    private XYChart.Series<Number, Number> createGraphs(String name, ArrayList<Integer> data) {
        XYChart.Series<Number, Number> graph = new XYChart.Series<>();
        graph.setName(name);
        for (int i = 0; i < data.size(); i++) {
            graph.getData().add(new XYChart.Data<>(i, data.get(i)));
        }
        return graph;
    }

    public void show() {
        logger.info("Graphs are displayed");
        Scene scene = new Scene(new ScrollPane(graphsContainer), WINDOW_WIDTH + 20, WINDOW_HEIGHT + 20);
        graphsStage.setScene(scene);
        graphsStage.show();
    }
}

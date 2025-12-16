import javafx.scene.control.Label;
import javafx.scene.layout.StackPane;
import javafx.scene.paint.Color;
import javafx.scene.shape.Rectangle;
import javafx.scene.text.Font;

class Cell extends StackPane {
    public int cellValue;
    public int fieldRow, fieldCol;
    private Rectangle shape;
    private Label cellValueLabel;
    public Color cellColor = Color.WHITE;
    private Field field;

    public Cell(int cellValue, int row, int col, Field field) {
        this.cellValue = cellValue;
        this.fieldRow = row;
        this.fieldCol = col;
        this.field = field;
        shape = new Rectangle(35, 35);
        shape.setStroke(Color.BLACK);
        shape.setFill(cellColor);
        cellValueLabel = new Label(String.valueOf(cellValue));
        cellValueLabel.setFont(Font.font(12));
        getChildren().addAll(shape, cellValueLabel);
        setOnMouseClicked(e -> cellClick());
    }

    public void setCellValue(int cellValue) {
        this.cellValue = cellValue;
        if (cellValue == 0) {
            cellValueLabel.setText("");
        } else {
            cellValueLabel.setText(String.valueOf(cellValue));
        }
    }

    public void setHighlighted(boolean highlighted) {
        if (highlighted) {
            shape.setStroke(Color.RED);
        } else {
            shape.setStroke(Color.BLACK);
        }
    }

    public void setColor(Color newColor) {
        cellColor = newColor;
        shape.setFill(cellColor);
        setHighlighted(false);
    }

    private void cellClick() {
        if (!cellColor.equals(Color.WHITE)) {
            return;
        }
        field.cellSelection(this);
    }
}

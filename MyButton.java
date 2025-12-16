import javafx.scene.control.Button;
import javafx.scene.text.Font;

class MyButton extends Button {
    public MyButton(String text, int posX, int posY) {
        super(text);
        setPrefSize(200, 50);
        setLayoutX(posX);
        setLayoutY(posY);
        setFont(Font.font(16));
    }
}

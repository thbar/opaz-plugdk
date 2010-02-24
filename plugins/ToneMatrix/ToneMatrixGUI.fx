import javafx.stage.*;
import javafx.scene.*;
import javafx.scene.shape.*;
import javafx.scene.paint.*;
import javafx.scene.paint.Color.*;

def n = 12;
def size_by_item = 32;
def padding = 5;
def size = n * size_by_item + padding;

var items = for (i in [0..n*n-1]) {
  Rectangle {
        x: padding + size_by_item * (i mod n);
        y: padding + size_by_item * (i / n);
        height: size_by_item - padding;
        width: size_by_item - padding;
        opacity: 0.7;
        fill: Color.rgb(50, 50, 50);
}}

public class ToneMatrixGUI extends Scene {
  init {
    fill = Color.BLACK;
    content = items;
  }
}

function run(args : String[]) {
    Stage {
        title: "ToneMatrix GUI"
        width: size
        height: size
        scene: new ToneMatrixGUI();
    }
}

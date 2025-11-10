package Tools;
import java.awt.*;

public class RectangleTool extends ShapeTool {
    @Override
    protected String getCommandName() {
        return "RECT";
    }

    @Override
    protected void drawShape(Graphics2D g2d, int x, int y, int w, int h) {
        g2d.drawRect(x, y, w, h);
    }
}
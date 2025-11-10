package Tools;
import java.awt.*;

public class TriangleTool extends ShapeTool {
    @Override
    protected String getCommandName() {
        return "TRIANGLE";
    }

    @Override
    protected void drawShape(Graphics2D g2d, int x, int y, int w, int h) {
        // calculates 3 points for an isosceles triangle fitting the box
        int[] xPoints = {x + w / 2, x, x + w};
        int[] yPoints = {y, y + h, y + h};
        g2d.drawPolygon(xPoints, yPoints, 3);
    }
}
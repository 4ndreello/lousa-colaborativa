package Tools;

import java.awt.*;

public class HexagonTool extends ShapeTool {
    @Override
    protected String getCommandName() {
        return "HEXAGON";
    }

    @Override
    protected void drawShape(Graphics2D g2d, int x, int y, int w, int h) {
        // calculates 6 points for a hexagon fitting the box
        int[] xPoints = {
                x + (int)(w * 0.25), // top-leftish
                x + (int)(w * 0.75), // top-rightish
                x + w,               // right-mid
                x + (int)(w * 0.75), // bottom-rightish
                x + (int)(w * 0.25), // bottom-leftish
                x                    // left-mid
        };
        int[] yPoints = {
                y,
                y,
                y + h / 2,
                y + h,
                y + h,
                y + h / 2
        };
        g2d.drawPolygon(xPoints, yPoints, 6);
    }
}
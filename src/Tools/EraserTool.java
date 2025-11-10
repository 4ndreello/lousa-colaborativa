package Tools;
import Connection.ServerConnection;
import Panels.DrawingPanel;
import javax.swing.*;
import java.awt.*;
import java.awt.event.MouseEvent;

public class EraserTool extends PencilTool {

    private int thickness = 30;

    @Override
    public void onMouseDragged(MouseEvent e, ServerConnection connection) {
        int x = e.getX();
        int y = e.getY();

        // dynamic clamping
        Component c = e.getComponent();
        x = Math.max(0, Math.min(x, c.getWidth() - 1));
        y = Math.max(0, Math.min(y, c.getHeight() - 1));

        // uses parent helper to send command, forcing white color
        sendDrawCommand(lastX, lastY, x, y, "#ffffff", thickness, connection);

        lastX = x;
        lastY = y;
    }

    @Override
    public int getThickness() {
        return thickness;
    }

    @Override
    public JComponent getOptionsPanel(DrawingPanel context) {
        JToolBar options = new JToolBar();
        options.setFloatable(false);
        options.add(new JLabel(" eraser config:  "));

        options.add(new JLabel("size: "));
        JSlider slider = new JSlider(JSlider.HORIZONTAL, 10, 100, thickness);
        slider.setPreferredSize(new Dimension(150, 35));
        slider.addChangeListener(e -> {
            this.thickness = slider.getValue();
            context.repaint();
        });
        options.add(slider);

        return options;
    }
}
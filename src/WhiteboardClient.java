import Tools.*;
import Connection.ServerConnection;
import Panels.DrawingPanel;
import javax.swing.*;
import java.awt.*;
import java.io.IOException;
import java.awt.GridLayout;

public class WhiteboardClient extends JFrame {

    private ServerConnection connection;
    private DrawingPanel drawingPanel;
    private JPanel configPanelContainer;

    public WhiteboardClient(String host, String portStr, int timeoutMillis) {
        super("java interactive whiteboard v8 - fixed history");
        setSize(1280, 800);
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setLocationRelativeTo(null);

        try {
            int port = Integer.parseInt(portStr);

            connection = new ServerConnection(host, port, timeoutMillis, msg -> {
                drawingPanel.processCommand(msg);
            });

        } catch (NumberFormatException e) {
            JOptionPane.showMessageDialog(this, "invalid port: " + portStr, "connection error", JOptionPane.ERROR_MESSAGE);
            System.exit(1);
        } catch (IOException e) {
            JOptionPane.showMessageDialog(this, "connection error (" + host + ":" + portStr + "): " + e.getMessage(), "connection error", JOptionPane.ERROR_MESSAGE);
            System.exit(1);
        }

        drawingPanel = new DrawingPanel(connection);
        add(drawingPanel, BorderLayout.CENTER);

        JPanel topContainer = new JPanel(new BorderLayout());
        JToolBar mainToolbar = new JToolBar();
        mainToolbar.setFloatable(false);
        mainToolbar.setBackground(new Color(230, 230, 230));

        mainToolbar.add(createToolButton("✏️", "pencil", new PencilTool()));
        mainToolbar.add(createToolButton("🧽", "eraser", new EraserTool()));
        mainToolbar.addSeparator();
        mainToolbar.add(createToolButton("⬜", "rectangle", new RectangleTool()));
        mainToolbar.add(createToolButton("🔳", "square", new SquareTool()));
        mainToolbar.add(createToolButton("🔺", "triangle", new TriangleTool()));
        mainToolbar.add(createToolButton("⬡", "hexagon", new HexagonTool()));

        mainToolbar.addSeparator();
        JButton btnClear = new JButton("🗑️ clear");
        btnClear.addActionListener(e -> connection.sendMessage("ACTION;CLEAR"));
        mainToolbar.add(btnClear);

        configPanelContainer = new JPanel(new BorderLayout());
        configPanelContainer.setBackground(new Color(245, 245, 245));

        topContainer.add(mainToolbar, BorderLayout.NORTH);
        topContainer.add(configPanelContainer, BorderLayout.SOUTH);
        add(topContainer, BorderLayout.NORTH);

        selectTool(new PencilTool());

        connection.startListening();
    }

    private JButton createToolButton(String icon, String tooltip, Tool tool) {
        JButton btn = new JButton(icon);
        btn.setToolTipText(tooltip);
        btn.addActionListener(e -> selectTool(tool));
        return btn;
    }

    private void selectTool(Tool tool) {
        drawingPanel.setCurrentTool(tool);
        configPanelContainer.removeAll();
        configPanelContainer.add(tool.getOptionsPanel(drawingPanel), BorderLayout.CENTER);
        configPanelContainer.revalidate();
        configPanelContainer.repaint();
    }

    public static void main(String[] args) {
        try { UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName()); } catch (Exception ignored) {}

        final int CONNECTION_TIMEOUT_MS = 3000;

        JTextField hostField = new JTextField("34.39.233.209");
        JTextField portField = new JTextField("8080");

        JPanel panel = new JPanel(new GridLayout(0, 2, 5, 5)); // 2 colunas, espaçamento 5
        panel.add(new JLabel("host:")); // lowercase english
        panel.add(hostField);
        panel.add(new JLabel("port:")); // lowercase english
        panel.add(portField);

        // 3. Exibe o JOptionPane com o painel customizado (lowercase english)
        int result = JOptionPane.showConfirmDialog(null, panel, "connect to server",
                JOptionPane.OK_CANCEL_OPTION, JOptionPane.PLAIN_MESSAGE);

        if (result == JOptionPane.OK_OPTION) {
            String host = hostField.getText();
            String portStr = portField.getText();

            SwingUtilities.invokeLater(() -> new WhiteboardClient(host, portStr, CONNECTION_TIMEOUT_MS).setVisible(true));
        } else {
            System.exit(0);
        }
    }
}
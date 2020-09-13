import javax.imageio.ImageIO;
import javax.swing.*;
import javax.swing.border.EmptyBorder;
import javax.swing.border.EtchedBorder;
import javax.swing.border.LineBorder;
import java.awt.*;
import java.awt.event.*;
import java.io.File;
import java.io.IOException;

public class GameGUI implements MouseMotionListener, MouseListener, KeyListener {

    private int dimX = 4, dimY = 4, fWidth = 4, fHeight = 4, cBombs = 4;

    private JFrame frame;
    private JPanel panel;
    private Game game;

    private JLabel[][] vMap;
    private JPanel[][] vDim;

    public GameGUI() {
        openCreateDialog();
    }

    public void initGame(int dimX, int dimY, int fWidth, int fHeight, int cBombs) {
        if (frame != null)
            frame.dispose();

        try {
            game = new Game(dimX, dimY, fWidth, fHeight, cBombs);
        } catch (Exception ignored) {
            openCreateDialog();
        }
        this.dimX = dimX;
        this.dimY = dimY;
        this.fWidth = fWidth;
        this.fHeight = fHeight;
        this.cBombs = cBombs;

        panel = new JPanel();
        panel.setBounds(0, 0, 1000, 1000);
        panel.setPreferredSize(panel.getSize());
        panel.setDoubleBuffered(true);
        panel.setLayout(new GridLayout(dimY, dimX));
        panel.addMouseListener(this);
        panel.addMouseMotionListener(this);


        frame = new JFrame("Minesweeper4D");
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        frame.setLayout(new BorderLayout());
        try {
            frame.setIconImage(ImageIO.read(new File("./src/assets/KBLogo.png")));
        } catch (IOException e) {
            e.printStackTrace();
        }
        frame.add(panel);
        frame.pack();
        frame.setMinimumSize(frame.getSize());
        frame.addKeyListener(this);
        frame.setLocationRelativeTo(null);
        frame.setVisible(true);


        vMap = new JLabel[dimX * fWidth][dimY * fHeight];
        vDim = new JPanel[dimX][dimY];

        for (int dy = 0; dy < dimY; dy++) {
            for (int dx = 0; dx < dimX; dx++) {
                JPanel jp = new JPanel();
                jp.setOpaque(false);
                jp.setBackground(Color.BLACK);
                jp.setBorder(new EmptyBorder(1, 1, 1, 1));
                jp.setLayout(new GridLayout(fHeight, fWidth));
                panel.add(jp);
                vDim[dx][dy] = jp;
            }
        }
        for (int y = 0; y < vMap[0].length; y++) {
            for (int x = 0; x < vMap.length; x++) {
                JLabel l = new JLabel();
                l.setHorizontalAlignment(SwingConstants.CENTER);
                l.setOpaque(true);
                l.setPreferredSize(new Dimension(10, 10));
                l.setForeground(Color.BLACK);
                l.setBackground(Color.DARK_GRAY);
                l.setBorder(new EtchedBorder());
                l.setText(x + " " + y);
                l.setVisible(true);
                vDim[x / fWidth][y / fHeight].add(l);
                vMap[x][y] = l;
            }
        }


    }

    public void openCreateDialog() {
        JDialog dl = new JDialog();
        dl.setTitle("Create new Game");
        dl.setModal(true);
        dl.setAlwaysOnTop(true);
        dl.setLayout(new GridLayout(3, 5, 5, 5));

        dl.add(new JLabel("Rows:", SwingConstants.CENTER));
        dl.add(new JLabel("Cols:", SwingConstants.CENTER));
        dl.add(new JLabel("Width:", SwingConstants.CENTER));
        dl.add(new JLabel("Height:", SwingConstants.CENTER));
        dl.add(new JLabel("Bombs:", SwingConstants.CENTER));


        SpinnerNumberModel spNumMod = new SpinnerNumberModel(dimX, 0, 255, 1);
        JSpinner spRows = new JSpinner(spNumMod);
        dl.add(spRows);
        spNumMod = new SpinnerNumberModel(dimY, 1, 255, 1);
        JSpinner spCols = new JSpinner(spNumMod);
        dl.add(spCols);
        spNumMod = new SpinnerNumberModel(fWidth, 1, 255, 1);
        JSpinner spWidth = new JSpinner(spNumMod);
        dl.add(spWidth);
        spNumMod = new SpinnerNumberModel(fHeight, 1, 255, 1);
        JSpinner spHeight = new JSpinner(spNumMod);
        dl.add(spHeight);
        spNumMod = new SpinnerNumberModel(cBombs, 1, 255, 1);
        JSpinner spBombs = new JSpinner(spNumMod);
        dl.add(spBombs);

        dl.add(new JPanel());
        dl.add(new JPanel());
        dl.add(new JPanel());

        JButton btnStart = new JButton("Start!");
        btnStart.addActionListener((a) -> {
            dl.dispose();
            initGame((int) spRows.getValue(), (int) spCols.getValue(), (int) spWidth.getValue(), (int) spHeight.getValue(), (int) spBombs.getValue());
        });
        dl.add(btnStart);

        JButton btnCancel = new JButton("Cancel");
        btnCancel.addActionListener((a) -> {
            System.exit(0);
        });
        dl.add(btnCancel);

        dl.pack();
        dl.setLocationRelativeTo(null);
        dl.setResizable(false);
        dl.setVisible(true);
    }

    public void update() {
        int nFlag = 0;
        for (int x = 0; x < vMap.length; x++) {
            for (int y = 0; y < vMap[0].length; y++) {
                vMap[x][y].setBorder(new EtchedBorder());
                if (game.isFlag(x / fWidth, y / fHeight, x % fWidth, y % fHeight)) {
                    nFlag++;
                    vMap[x][y].setBackground(Color.GREEN);
                    vMap[x][y].setText("F");
                } else if (game.isOpen(x / fWidth, y / fHeight, x % fWidth, y % fHeight)) {
                    vMap[x][y].setText(game.getFieldText(x / fWidth, y / fHeight, x % fWidth, y % fHeight));
                    vMap[x][y].setBackground(Color.GRAY);
                } else {
                    vMap[x][y].setText("");
                    vMap[x][y].setBackground(Color.DARK_GRAY);
                }
            }
            frame.setTitle("Minesweeper4D!  [" + nFlag + "/" + game.getBombs() + " bombs marked]");
        }
        if (game.isOver() || game.hasWon()) {
            for (int x = 0; x < vMap.length; x++) {
                for (int y = 0; y < vMap[0].length; y++) {
                    if (game.isBomb(x / fWidth, y / fHeight, x % fWidth, y % fHeight)) {
                        if (game.isFlag(x / fWidth, y / fHeight, x % fWidth, y % fHeight)) {
                            vMap[x][y].setBackground(Color.GREEN);
                        } else {
                            vMap[x][y].setBackground(Color.RED);
                        }
                        vMap[x][y].setText("BOMB");
                    } else if (game.isFlag(x / fWidth, y / fHeight, x % fWidth, y % fHeight)) {
                        vMap[x][y].setBackground(Color.ORANGE);
                    }
                }
            }
            frame.setTitle("Minesweeper4D! [Game over!(" + game.getMilliTime() / 1000 + "s)]");
            openCreateDialog();
        }

    }

    @Override
    public void mouseClicked(MouseEvent e) {

    }

    @Override
    public void mousePressed(MouseEvent e) {
        if (game.isOver() || game.isPaused())
            return;
        int x = (int) (1.0 * e.getX() / (panel.getWidth()) * fWidth * dimX);
        int y = (int) (1.0 * e.getY() / (panel.getHeight()) * fHeight * dimY);
        if (e.getButton() == MouseEvent.BUTTON1)
            game.spread(x / fWidth, y / fHeight, x % fWidth, y % fHeight);
        else if (e.getButton() == MouseEvent.BUTTON3)
            game.setFlag(x / fWidth, y / fHeight, x % fWidth, y % fHeight);
        update();
        mouseMoved(e);
    }

    @Override
    public void mouseReleased(MouseEvent e) {

    }

    @Override
    public void mouseEntered(MouseEvent e) {

    }

    @Override
    public void mouseExited(MouseEvent e) {
        if (game.isOver() || game.isPaused())
            return;
        update();
    }

    @Override
    public void mouseDragged(MouseEvent e) {

    }

    @Override
    public void mouseMoved(MouseEvent e) {
        if (game.isOver() || game.isPaused())
            return;
        update();
        int x = (int) ((1.0 * e.getX() / panel.getWidth()) * fWidth * dimX);
        int y = (int) ((1.0 * e.getY() / panel.getHeight()) * fHeight * dimY);
        for (int i = -1; i <= 1; i++) {
            for (int j = -1; j <= 1; j++) {
                for (int k = -1; k <= 1; k++) {
                    for (int l = -1; l <= 1; l++) {
                        try {
                            if (((x + k) / fWidth != x / fWidth) || (x + k) < 0 || (y + l < 0) || ((y + l) / fHeight != y / fHeight))
                                continue;
                            int xn = x + i * fWidth + k;
                            int yn = y + j * fHeight + l;
                            vMap[xn][yn].setBorder(new LineBorder(Color.BLACK, 3));
                        } catch (Exception ignored) {
                        }
                    }

                }

            }
        }


    }

    @Override
    public void keyTyped(KeyEvent e) {

    }

    @Override
    public void keyPressed(KeyEvent e) {

    }

    @Override
    public void keyReleased(KeyEvent e) {
        if (game.isOver())
            return;
        if (e.getKeyCode() == KeyEvent.VK_ESCAPE) {
            game.setPaused(!game.isPaused());
            if (game.isPaused()) {
                panel.setEnabled(false);
                for (int x = 0; x < vMap.length; x++) {
                    for (int y = 0; y < vMap[0].length; y++) {
                        vMap[x][y].setText("Paused!");
                        vMap[x][y].setBorder(new EtchedBorder());
                        vMap[x][y].setBackground(Color.WHITE);
                    }
                }
            } else {
                panel.setEnabled(true);
                update();
            }
        }
    }
}

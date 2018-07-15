package GUI;

import javax.swing.*;
import java.awt.*;

public class SimpleAnimation {
    //    who calls the event handler? method go() in main()
    int x = 70;
    int y = 70;

    public static void main(String[] args) {
        SimpleAnimation gui = new SimpleAnimation();
        gui.go();
    }

    public void go() {
        JFrame frame = new JFrame();
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);

        MyDrawPanel drawPanel = new MyDrawPanel();

        frame.getContentPane().add(BorderLayout.CENTER, drawPanel);
        frame.setSize(300, 300);
        frame.setVisible(true);

        for (int i = 0; i < 130; i++) {
            x++;
            y++;
            drawPanel.repaint();
            try {
                Thread.sleep(50); // slowdown to see movement
            } catch (Exception ex) {
            }
        }
    }

    class MyDrawPanel extends JPanel {
        /* inner class to access x,y */
        public void paintComponent(Graphics g) {
            //erase last trail by filling entire panel with background color
            g.setColor(Color.white);
            g.fillRect(0, 0, this.getWidth(), this.getHeight());

            g.setColor(Color.green);
            g.fillOval(x, y, 40, 40);
        }
    }
}


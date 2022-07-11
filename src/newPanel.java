import javax.swing.*;
import java.awt.*;

public class newPanel extends JPanel {
    newPanel(){

        this.setPreferredSize(new Dimension(750,750));
        this.setLocation(100,100);
    }

    public void paint (Graphics g){
        super.paintComponent(g);
        Graphics2D g2D = (Graphics2D) g;
        g2D.drawLine(20,20,730,20);
        g2D.drawLine(20,20,20,290);
        g2D.drawLine(20,290,730,290);
        g2D.drawLine(730,20,730,290);

        g2D.drawLine(20,300,730,300);
        g2D.drawLine(20,570,730,570);
        g2D.drawLine(20,300,20,570);
        g2D.drawLine(730,300,730,570);

        g2D.drawLine(25,155,725,155);
        g2D.drawLine(25,435,725,435);


        long[] channel1= readWav.input[0];
        long[] channel2= readWav.input[1];
        System.out.println("length of the channel is: "+channel1.length);
        float scaleRate = (float)650/((float) (readWav.numSample)/2);
        System.out.println("scaleRate is: "+scaleRate);
        for (int i = 0; i< readWav.numSample/2; i++){
            g2D.setColor(Color.BLUE);
            g2D.drawLine((int) (25+i*scaleRate), 155,(int) (25+i*scaleRate), (int) (155-channel1[i]*0.005));
            g2D.setColor(Color.RED);
            g2D.drawLine((int) (25+i*scaleRate), 435,(int) (25+i*scaleRate), (int) (435-channel2[i]*0.005));

        }

    }
}

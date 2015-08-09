import javax.swing.*;
import java.awt.*;
import java.awt.image.BufferedImage;

/**
 * Created by Zenon on 10/01/15.
 */
public class StatPanel extends JPanel {

    private BufferedImage image;
    private static boolean mouseDown = false;
    private int mouseDownCount = 0;
    private static int mouseX = 0;
    private static int mouseY = 0;

    private final int defaultMouseSize = 40;
    private final int defaultEyeSize = 16;
    private final int res = 24;

    private static Point[] heatGazeBuffer = new Point[24];
    private static int heatGazeBufferHead = 0;

    private static Point[] gazeBuffer = new Point[4];
    private static int[] gazeIntensityBuffer = new int[4];
    private static int gazeBufferHead = 0;

    private static boolean HEATMAP = false;

    public StatPanel(BufferedImage image){

        for(int i = 0; i < heatGazeBuffer.length; i++)
        {
            heatGazeBuffer[i] = new Point(-128,-128);
        }
        this.image = image;
    }

    public void setImage(BufferedImage image){
        this.image = image;
    }

    public static void setMousePosition(int x, int y){
        mouseX = x;
        mouseY = y;
    }

    public static void pushGazePosition(int x, int y){

        if (x == 0 && y == 0)
        {
            x = -512;
            y = -512;
        }

        Point p = new Point(x,y);
        //For heatmap
        heatGazeBuffer[heatGazeBufferHead] = p;
        heatGazeBufferHead = (heatGazeBufferHead +1)%(heatGazeBuffer.length);

        //For gaze plot

        //If point is close to previous point...

        if (gazeBuffer[gazeBufferHead] != null)
        {
            if (p.distance(gazeBuffer[gazeBufferHead]) < 64)
            {
                gazeIntensityBuffer[gazeBufferHead]+=1;
            }
            else
            {
                gazeBufferHead = (gazeBufferHead +1)%(gazeBuffer.length);
                gazeBuffer[gazeBufferHead] = p;
                gazeIntensityBuffer[gazeBufferHead]=0;
            }
        }
        else
        {
            gazeBuffer[gazeBufferHead] = p;
        }
    }

    public static void setClicked(){
        mouseDown = true;
    }

    public static void setReleased(){
        mouseDown = false;
    }

    public void paintComponent(Graphics g){
        super.paintComponent(g);
        Graphics2D g2d = (Graphics2D) g;

        int width;
        int height;
        double winWidth = getBounds().getWidth();
        double winHeight = getBounds().getHeight();
        double iWidth = (double)image.getWidth();
        double iHeight = (double)image.getHeight();

        g2d.setRenderingHint(RenderingHints.KEY_INTERPOLATION,RenderingHints.VALUE_INTERPOLATION_BILINEAR);
        g2d.setRenderingHint(RenderingHints.KEY_RENDERING,RenderingHints.VALUE_RENDER_QUALITY);
        g2d.setRenderingHint(RenderingHints.KEY_ANTIALIASING,RenderingHints.VALUE_ANTIALIAS_ON);

        g2d.setFont(new Font("Arial", Font.PLAIN, 12));

        if (Math.abs(iHeight-winHeight) < 200 && Math.abs(iWidth-winWidth) < 200)
        {
            width = (int)iWidth;
            height = (int)iHeight;
        }
        else if (iHeight/iWidth < winHeight/winWidth)
        {
            height = (int)getBounds().getHeight();
            width = (int)((double)image.getWidth()/(double)image.getHeight()*(double)height);
        }
        else
        {
            width = (int)winWidth;
            height = (int)(iHeight/iWidth*(double)width);
        }
        g2d.scale((double)width/iWidth,(double)height/iHeight);
        g2d.drawImage(image, 0, 0, (int)iWidth, (int)iHeight, null);


        //Draw mouse.
        int drawnMouseSize = defaultMouseSize;
        g2d.setColor(new Color(.5f, 0f, 0f, .3f));


        if (mouseDown)
            mouseDownCount = 5;
        else
            mouseDownCount = Math.max(0,mouseDownCount-1);

        if (mouseDownCount>0)
            g2d.setColor(new Color(.5f, 0f, 0f, .5f));

        drawnMouseSize = defaultMouseSize-(int)((double)defaultMouseSize*((double)mouseDownCount/10));

        g2d.fillOval(mouseX-drawnMouseSize/2,mouseY-drawnMouseSize/2,drawnMouseSize,drawnMouseSize);

        int c = 0;

        //g2d.setColor(new Color(0, 0, 128, 50));

        int pixels[][] = new int[(int)(iWidth/res)][(int)(iHeight/res)];

        for(int i = heatGazeBufferHead; c < heatGazeBuffer.length && HEATMAP; i = (i+1)%(heatGazeBuffer.length))
        {

            int mX = heatGazeBuffer[i].x/res;
            int mY = heatGazeBuffer[i].y/res;

            for(int yOffset = -defaultEyeSize/2; yOffset < defaultEyeSize/2; yOffset++)
            {
                if (mY+yOffset < 0 || mY+yOffset >= (int)(iHeight/res))
                    continue;

                for(int xOffset = -defaultEyeSize/2; xOffset < defaultEyeSize/2; xOffset++)
                {
                    double dist = 0;
                    if (mX+xOffset < 0 || mX+xOffset >= (int)(iWidth/res))
                        continue;

                    double pVal = 0;

                    if (xOffset == 0 && yOffset == 0)
                    {
                        dist = 0;
                        pVal = 1;
                    }
                    else
                    {
                        dist = Math.sqrt(Math.pow((mX+xOffset - mX), 2) + Math.pow((mY+yOffset - mY), 2));
                        pVal = 1.0/dist;
                    }

                    pVal = (pVal/ heatGazeBuffer.length)*100;

                    pixels[mX+xOffset][mY+yOffset] = Math.min(100, pixels[mX+xOffset][mY+yOffset]+(int)pVal);
                }
            }

            for(int y = 0; y < (int)(iHeight/res); y++)
            {
                for(int x = 0; x < (int)(iWidth/res); x++)
                {
                    g2d.setComposite(makeComposite((float)pixels[x][y]/800.00f));
                    g2d.setColor(Color.getHSBColor((1-(float)pixels[x][y]/100.00f), 1, 1));

                    if (pixels[x][y] > 5)
                    {
                        g2d.fillRect(x * res, y * res, res, res);
                    }
                }
            }

            g2d.setComposite(makeComposite(1f));

            c++;
        }

        c = 0;



        Point prevPoint = new Point(-512,-512);
        int k = gazeBufferHead;
        for(k = (k+1)%(gazeBuffer.length); c < gazeBuffer.length;  k = (k+1)%(gazeBuffer.length))
        {
            if (gazeBuffer[k] != null)
            {
                System.out.println(k);
                g2d.setColor(new Color(0f, 0f, 1f, .5f));
                int gazeMarkSize = getMarkSize(gazeIntensityBuffer[k]);

                if (prevPoint.x >= 0 && prevPoint.y >= 0 && gazeBuffer[k].x >= 0 && gazeBuffer[k].y >= 0)
                {
                    g2d.drawLine(prevPoint.x,prevPoint.y,gazeBuffer[k].x,gazeBuffer[k].y);
                }

                g2d.fillOval(gazeBuffer[k].x-(gazeMarkSize/2),gazeBuffer[k].y-(gazeMarkSize/2),(gazeMarkSize),(gazeMarkSize));
                g2d.setColor(new Color(1f, 1f, 1f, 1f));
                g2d.drawString(Integer.toString(c+1),gazeBuffer[k].x,gazeBuffer[k].y);
                prevPoint = gazeBuffer[k];
            }

            g2d.setComposite(makeComposite(1f));

            c++;
        }

        System.out.println("------");

    }

    private static int getMarkSize(int intensity){
        return Math.max(24,Math.min((intensity*3), 100));
    }

    private AlphaComposite makeComposite(float alpha) {
        int type = AlphaComposite.SRC_OVER;
        return(AlphaComposite.getInstance(type, alpha));
    }


}
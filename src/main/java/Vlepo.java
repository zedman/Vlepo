/**
 * Created by Zenon on 9/01/15.
 */

import com.theeyetribe.client.IGazeListener;
import com.theeyetribe.client.data.GazeData;
import org.jnativehook.NativeHookException;

import javax.swing.*;
import java.awt.*;
import java.awt.image.*;
import java.awt.Robot;
import java.util.logging.Level;
import java.util.logging.LogManager;
import java.util.logging.Logger;

import com.theeyetribe.client.GazeManager;

import org.jnativehook.GlobalScreen;

public class Vlepo {

    //Temporary store for screen capture data
    private static int[] pixels;

    private static GraphicsDevice gDevice;
    private static Robot robot;
    public static Rectangle screenRect;

    private static ColorModel model = new DirectColorModel(32, 0xff0000, 0xff00, 0xff);

    public static void main (String args[])
    {
        //Get the EyeTribe manager instance
        final GazeManager gm = GazeManager.getInstance();
        boolean success = gm.activate(GazeManager.ApiVersion.VERSION_1_0, GazeManager.ClientMode.PUSH);

        final GazeListener gazeListener = new GazeListener();
        gm.addGazeListener(gazeListener);

        Runtime.getRuntime().addShutdownHook(new Thread()
        {
            @Override
            public void run()
            {
                gm.removeGazeListener(gazeListener);
                gm.deactivate();
            }
        });

        // Clear previous logging configurations.
        LogManager.getLogManager().reset();
        // Get the logger for "org.jnativehook" and set the level to off.
        Logger logger = Logger.getLogger(GlobalScreen.class.getPackage().getName());
        logger.setLevel(Level.OFF);


        try
        {

            gDevice = GraphicsEnvironment.getLocalGraphicsEnvironment().getDefaultScreenDevice();
            robot = new Robot(gDevice);
            screenRect = gDevice.getDefaultConfiguration().getBounds();

            JFrame canvasFrame = new JFrame();
            canvasFrame.setSize(screenRect.width,screenRect.height);
            canvasFrame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
            canvasFrame.setTitle("Analytics Output");

            StatPanel imagePanel = new StatPanel(getFrame());
            canvasFrame.add(imagePanel);
            canvasFrame.setVisible(true);


            try {
                GlobalScreen.registerNativeHook();
            }
            catch (NativeHookException ex) {
                System.err.println("There was a problem registering the native hook.");
                System.err.println(ex.getMessage());

                System.exit(1);
            }

            //Construct the example object.
            InputListener example = new InputListener();

            //Add the appropriate listeners for the example object.
            GlobalScreen.getInstance().addNativeMouseListener(example);
            GlobalScreen.getInstance().addNativeMouseMotionListener(example);

            boolean active = true;

            while(active)
            {
                imagePanel.setImage(getFrame());
                imagePanel.repaint();
            }

        }
        catch(Exception e)
        {
            e.printStackTrace();
        }
    }

    private static class GazeListener implements IGazeListener
    {
        @Override
        public void onGazeUpdate(GazeData gazeData)
        {
            StatPanel.pushGazePosition((int)gazeData.smoothedCoordinates.x,(int)gazeData.smoothedCoordinates.y);
        }
    }

    private static BufferedImage getFrame(){
       return robot.createScreenCapture(new Rectangle(0,0,screenRect.width,screenRect.height));
    };


}
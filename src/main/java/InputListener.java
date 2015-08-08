/**
 * Created by Zenon on 10/01/15.
 */

import org.jnativehook.mouse.NativeMouseEvent;
import org.jnativehook.mouse.NativeMouseInputListener;

public class InputListener implements NativeMouseInputListener {
    public void nativeMouseClicked(NativeMouseEvent e) {
        //System.out.println("Mosue Clicked: " + e.getClickCount());
    }

    public void nativeMousePressed(NativeMouseEvent e) {
        //System.out.println("Mosue Pressed: " + e.getButton());
        StatPanel.setClicked();
    }

    public void nativeMouseReleased(NativeMouseEvent e) {
        //System.out.println("Mosue Released: " + e.getButton());
        StatPanel.setReleased();
    }

    public void nativeMouseMoved(NativeMouseEvent e) {
        //System.out.println("Mosue Moved: " + e.getX() + ", " + e.getY());

        StatPanel.setMousePosition(e.getX(),e.getY());
    }

    public void nativeMouseDragged(NativeMouseEvent e) {
        //System.out.println("Mosue Dragged: " + e.getX() + ", " + e.getY());
        StatPanel.setMousePosition(e.getX(),e.getY());
    }

}
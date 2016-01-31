package de.schmeller.mandelbrot;

import com.mtronicsdev.polygame.core.ClientEngineManager;
import com.mtronicsdev.polygame.display.Input;
import com.mtronicsdev.polygame.display.Monitor;
import com.mtronicsdev.polygame.display.Window;
import com.mtronicsdev.polygame.entities.Entity;
import com.mtronicsdev.polygame.entities.Entity3D;
import com.mtronicsdev.polygame.entities.Module;
import com.mtronicsdev.polygame.entities.modules.Camera;
import com.mtronicsdev.polygame.gui.Dimension2f;
import com.mtronicsdev.polygame.gui.Dimension4f;
import com.mtronicsdev.polygame.gui.GuiEngine;
import com.mtronicsdev.polygame.gui.GuiPanel;
import com.mtronicsdev.polygame.util.math.Vector4f;
import org.lwjgl.BufferUtils;
import org.lwjgl.glfw.GLFW;

import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.nio.ByteBuffer;
import java.util.Random;

import static org.lwjgl.opengl.GL11.*;

/**
 * @author Maximilian Schmeller (mtronics_dev)
 */
public class Main {
    public static void main(String... args) {
        ClientEngineManager.init();

        new Window("Mandelbrot", Monitor.getPrimary());
        //noinspection ResultOfMethodCallIgnored
        new File("img/").mkdirs(); //Create screenshot folder if necessary

        Dimension4f dimA = new Dimension4f(0, 0, 0, 0, true, true, true, true); //Dummy dimension to fill parameters

        //Fullscreen panel for rendering the Mandelbrot set onto it
        GuiPanel panel = new GuiPanel(dimA, dimA, new Dimension2f(1, 1, true, true),
                new Dimension2f(0, 0, true, true), new Vector4f(-.125f, 0, 1, 1), GuiEngine.Alignment.CENTER);

       new Entity3D(new Module() { //Dummy for input processing!
            double lastTime;
            int c = 0;
            int seq = new Random().nextInt(1000);

            {
                Input.registerKeyHandler(GLFW.GLFW_KEY_ESCAPE);

                Input.registerKeyHandler(GLFW.GLFW_KEY_DOWN);
                Input.registerKeyHandler(GLFW.GLFW_KEY_W);
                Input.registerKeyHandler(GLFW.GLFW_KEY_UP);
                Input.registerKeyHandler(GLFW.GLFW_KEY_A);
                Input.registerKeyHandler(GLFW.GLFW_KEY_S);
                Input.registerKeyHandler(GLFW.GLFW_KEY_D);

                lastTime = GLFW.glfwGetTime();
            }

            @Override
            public void update() {
                double time = GLFW.glfwGetTime();
                double delta = time - lastTime;

                if (delta >= .5) {
                    //screenshot(); //Screenshot every half second for creating flipbooks
                    lastTime = time;
                }

                if (Input.keyUp(GLFW.GLFW_KEY_ESCAPE)) System.exit(0);

                Vector4f color = panel.getColor();

                if (Input.keyPressed(GLFW.GLFW_KEY_DOWN)) color.z *= .99f;
                if (Input.keyPressed(GLFW.GLFW_KEY_W)) color.y += .005f / color.z;
                if (Input.keyPressed(GLFW.GLFW_KEY_UP)) color.z /= .99f;
                if (Input.keyPressed(GLFW.GLFW_KEY_A)) color.x -= .005f / color.z;
                if (Input.keyPressed(GLFW.GLFW_KEY_S)) color.y -= .005f / color.z;
                if (Input.keyPressed(GLFW.GLFW_KEY_D)) color.x += .005f / color.z;

                panel.setColor(color);
            }

            private void screenshot() {
                glReadBuffer(GL_FRONT);
                ByteBuffer buffer = BufferUtils.createByteBuffer(1920 * 1080 * 4);
                glReadPixels(0, 0, 1920, 1080, GL_RGBA, GL_UNSIGNED_BYTE, buffer);

                new Thread(() -> {
                    File imgFile = new File("img/seq_" + seq + ".frm_" + c + ".png");
                    c++;
                    BufferedImage img = new BufferedImage(1920, 1080, BufferedImage.TYPE_INT_RGB);

                    for (int x = 0; x < 1920; x++) {
                        for (int y = 0; y < 1080; y++) {
                            int index = (x + (1920 * y)) * 4;
                            int r = buffer.get(index) & 0xFF;
                            int g = buffer.get(index + 1) & 0xFF;
                            int b = buffer.get(index + 2) & 0xFF;
                            img.setRGB(x, 1080 - 1 - y, (0xFF << 24) | (r << 16) | (g << 8) | b);
                        }
                    }

                    try {
                        ImageIO.write(img, "PNG", imgFile);
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                }).start();
            }
        }, new Camera());

        ClientEngineManager.run();
    }
}

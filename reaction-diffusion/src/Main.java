import controlP5.ControlP5;
import processing.core.*;
import processing.opengl.PShader;
import java.util.Random;


public class Main extends PApplet {
    private PFont roboto_regular;

    private PGraphics shaderLayer;
    private PGraphics presentationLayer;

    private PShader turingShader;
    private PShader renderShader;

    private ControlP5 cp5Scott;
    private ControlP5 cp5Turk;

    private static int barHeight = 110;

    private final static int WIDTH = 800;
    private final static int HEIGHT = 800;

    private final static int SAMPLE_WIDTH = 800;
    private final static int SAMPLE_HEIGHT = 800 - barHeight;

    private static boolean drawPresentation = true;
    private static int brushType = 0;
    private static float brushSize = 8.f;
    private static int brushDensity = 60;
    private static float brushWeight = 2.0f;
    private static boolean drawCursor = true;
    private static boolean grayScottOrTruk = true;
    private static boolean mouseControllPanel = false;

    private static float Du = 1.0f;
    private static float Dv = 0.34f;
    private static float k = 0.06f;
    private static float f = 0.041f;
    private static float s = 0.01125f;
    private static float dt = 1.0f;

    public PImage backbuffer;

    public static void main(String[] args) {
        PApplet.main("Main");
    }

    @Override
    public void settings() {
        size( WIDTH, HEIGHT, P2D);
        backbuffer = new PImage(SAMPLE_WIDTH, SAMPLE_HEIGHT);
    }

    @Override
    public void setup() {
        cp5Scott = new ControlP5(this);
        cp5Turk = new ControlP5(this);
        setGUI();
        Random random = new Random();
        int k = backbuffer.pixels.length;
        for (int i = 0; i < k; i++) {
            int j = random.nextInt(k);
            if(random.nextBoolean()) {
                backbuffer.pixels[j] = color(random.nextInt(255), 0, random.nextInt(100));
            } else {
                backbuffer.pixels[j] = color(0, random.nextInt(255), random.nextInt(100));
            }
        }
        backbuffer.updatePixels();
//        backbuffer = loadImage("init.png");
        shaderLayer = createGraphics(SAMPLE_WIDTH, SAMPLE_HEIGHT, P3D);

        shaderLayer.beginDraw();
            shaderLayer.image(backbuffer, 0, 0, WIDTH, HEIGHT-barHeight);
//            shaderLayer.background(255.0f, 0, 0);
        shaderLayer.endDraw();

        presentationLayer = createGraphics(SAMPLE_WIDTH, SAMPLE_HEIGHT, P2D);
        turingShader = loadShader("turingFrag.glsl");
        renderShader = loadShader("renderFrag.glsl");

        renderShader.set("ca", new PVector(115, 98, 110));
        renderShader.set("cb", new PVector(247,228,190));

        roboto_regular = createFont("Roboto-Regular.ttf", 14);
        textFont(roboto_regular);
//        frameRate(30);

//        noCursor();
        noStroke();

    }

    @Override
    public void draw() {
        turingShader.set("r", grayScottOrTruk);
        turingShader.set("Du", Du);
        turingShader.set("Dv", Dv);
        turingShader.set("k", k);
        turingShader.set("f", f);
        turingShader.set("s", s);
        turingShader.set("dt", dt);

        if (mousePressed == true && !mouseControllPanel) mouseEvent();

        shaderLayer.beginDraw();
            shaderLayer.filter(turingShader);
        shaderLayer.endDraw();

        image(shaderLayer, 0, 0, WIDTH, HEIGHT-barHeight);

        if (drawPresentation == false) {
            fill(5);
            rect(0.0f, 20.0f, 90f, 20f);
            fill(245);
            text("Backbuffer", 10.0f, 35.0f);
        } else {
            filter(renderShader);
        }

        fill(255);
        rect(0.0f, HEIGHT - barHeight, WIDTH, barHeight);

        if (drawCursor && !mouseControllPanel) {
            noCursor();
            brush();
        } else {
            cursor();
        }
        if (mouseY > HEIGHT - barHeight) {
            mouseControllPanel = true;
        } else {
            mouseControllPanel = false;
        }
        drawfps();

    }

    public void brush() {
        switch (brushType) {
            case 0:
                fill(255f, 0f, 0f, 25f);
                break;
            case 1:
                fill(0f, 255f, 0f, 25f);
                break;
            case 2:
                fill(0f, 0f, 255f, 25f);
                break;
            case 3:
                fill(255f, 0f, 255f, 25f);
                break;
            case 4:
                fill(255f, 0f, 255f, 25f);
                break;
            default:
                fill(255f, 255f, 255f, 25f);
                break;
        }
        rect(mouseX - brushSize, mouseY - brushSize, 2 * brushSize, 2 * brushSize);
    }

    public void drawfps() {
        fill(5);
        rect(0.0f, 0.0f, 50f, 20f);
        fill(245);
        text((int)frameRate, 10.0f, 15.0f);
    }

    public void mouseEvent() {
        shaderLayer.beginDraw();
        shaderLayer.noStroke();
        switch(brushType) {
            case 0: // red
                for (int i = 0; i < brushDensity; i++) {
                    shaderLayer.fill(200+random(55), 0.0f, random(100), 55 + random(200));
                    shaderLayer.rect(mouseX + random(-brushSize, brushSize),
                            mouseY + random(-brushSize, brushSize), brushWeight, brushWeight);
                }
                break;
            case 1: // green
                for (int i = 0; i < brushDensity; i++) {
                    shaderLayer.rect(mouseX + random(-brushSize, brushSize),
                            mouseY + random(-brushSize, brushSize), brushWeight, brushWeight);
                }
                break;
            case 2: // eraser
                for (int i = 0; i < brushDensity; i++) {
                    shaderLayer.fill(0.0f, 0.0f, random(100), 255);
                    shaderLayer.rect(mouseX + random(-brushSize, brushSize),
                            mouseY + random(-brushSize, brushSize), brushWeight, brushWeight);
                }
                break;
            case 3: //random
                for (int i = 0; i < brushDensity; i++) {
                    shaderLayer.fill(random(255), random(255), random(100), 155 + random(100));
                    shaderLayer.rect(mouseX + random(-brushSize, brushSize),
                            mouseY + random(-brushSize, brushSize), brushWeight, brushWeight);
                }
                break;
            case 4:
                shaderLayer.stroke(0,200,0);
                shaderLayer.strokeWeight(15);
                shaderLayer.line(pmouseX, pmouseY, mouseX, mouseY);
                break;
        }
        shaderLayer.endDraw();
    }

    public void keyPressed() {
        if (key == 'p' || key == 'P') {
            drawPresentation = !drawPresentation;
        }
        if (key == '1' || key == 'q' || key == 'Q') {
            brushType = 0;
        }
        if (key == '2' || key == 'w' || key == 'W') {
            brushType = 1;
        }
        if (key == '3' || key == 'e' || key == 'E') {
            brushType = 2;
        }
        if (key == '4' || key == 'a' || key == 'A') {
            brushType = 3;
        }
        if (key == '5') {
            brushType = 4;
        }
        if (key == '+' || key == '=') {
            brushSize *= 2;
        }
        if (key == '-') {
            if (brushSize > 1.0) {
                brushSize /= 2;
            }
        }
        if (key == ']') {
            brushWeight += 1.0f;
        }
        if (key == '[') {
            if (brushWeight > 0) {
                brushWeight -= 1.0f;
            }
        }
        if (key == 'r' || key == 'R') {
            shaderLayer.beginDraw();
//            shaderLayer.image(backbuffer, SAMPLE_WIDTH, SAMPLE_HEIGHT);
            shaderLayer.fill(255, 0, 0);
            shaderLayer.rect(0.0f, 0.0f, SAMPLE_WIDTH, SAMPLE_HEIGHT);
            shaderLayer.endDraw();

        }
        if (key == 'c' || key == 'C') {
            drawCursor = !drawCursor;
        }
        if (key == 'z' || key == 'Z') {
            grayScottOrTruk = !grayScottOrTruk;

            if(grayScottOrTruk){
                cp5Turk.hide();
                cp5Scott.show();
                cp5Scott.update();
            } else{
                cp5Scott.hide();
                cp5Turk.show();
                cp5Turk.update();
            }
        }
    }



    void setGUI(){

            int length = 200;
            int left = WIDTH - length - 20;
            int height = 10;
            int top = HEIGHT - 10 * height;

            cp5Scott.addSlider("f", 0.0f, 0.2f, left, top, length, height)
                    .setColorValue(color(255)).setColorActive(color(155)).setColorForeground(color(155)).setColorLabel(color(50)).setColorBackground(color(50));
            cp5Scott.addSlider("k", 0.0f, 0.2f, left, top + 2 * height, length, height)
                    .setColorValue(color(255)).setColorActive(color(155)).setColorForeground(color(155)).setColorLabel(color(50)).setColorBackground(color(50));
            cp5Scott.addSlider("Du", 0.0f, 1.0f, left, top + 4 * height, length, height)
                    .setColorValue(color(255)).setColorActive(color(155)).setColorForeground(color(155)).setColorLabel(color(50)).setColorBackground(color(50));
            cp5Scott.addSlider("Dv", 0.00f, 1.0f, left, top + 6 * height, length, height)
                    .setColorValue(color(255)).setColorActive(color(155)).setColorForeground(color(155)).setColorLabel(color(50)).setColorBackground(color(50));
            cp5Scott.addSlider("dt", 0.0f, 3.0f, left, top + 8 * height, length, height)
                    .setColorValue(color(255)).setColorActive(color(155)).setColorForeground(color(155)).setColorLabel(color(50)).setColorBackground(color(50));



            cp5Turk.addSlider("s", 0.0f, 0.1f, left, top , length, height)
                    .setColorValue(color(255)).setColorActive(color(155)).setColorForeground(color(155)).setColorLabel(color(50)).setColorBackground(color(50));
            cp5Turk.addSlider("Du", 0.0f, 1.0f, left, top +  2 * height , length, height)
                    .setColorValue(color(255)).setColorActive(color(155)).setColorForeground(color(155)).setColorLabel(color(50)).setColorBackground(color(50));
            cp5Turk.addSlider("Dv", 0.00f, 1.0f, left, top + 4 * height, length, height)
                    .setColorValue(color(255)).setColorActive(color(155)).setColorForeground(color(155)).setColorLabel(color(50)).setColorBackground(color(50));
            cp5Turk.addSlider("dt", 0.0f, 3.0f, left, top + 6 * height, length, height)
                    .setColorValue(color(255)).setColorActive(color(155)).setColorForeground(color(155)).setColorLabel(color(50)).setColorBackground(color(50));

            cp5Turk.hide();
    }

}

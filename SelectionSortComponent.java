package sorting;

import javax.swing.*;
import java.awt.*;
import java.awt.geom.Line2D;
import java.util.Arrays;
import java.util.Random;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;

public class SelectionSortComponent extends JComponent {
    private SelectionSorter sorter;

    public void startAnimation() {
        int[] values = ArrayUtil.randomIntArray(30, 300);
        sorter = new SelectionSorter(values, this);
        class AnimationRunnable implements Runnable {
            public void run() {
                try {
                    sorter.sort();
                } catch (InterruptedException exception) {
                    System.out.println(Arrays.toString(exception.getStackTrace()));
                }
            }
        }
        Runnable r = new AnimationRunnable();
        Thread t = new Thread(r);
        t.start();
    }

    @Override
    public void paintComponent(Graphics g) {
        if (sorter == null) {
            return;
        }
        Graphics2D g2 = (Graphics2D) g;
        sorter.draw(g2);
    }

    public static void main(String[] args) {
        JFrame jFrame = new JFrame();
        jFrame.setSize(500, 500);
        jFrame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        SelectionSortComponent selectionSortComponent = new SelectionSortComponent();
        jFrame.add(selectionSortComponent, BorderLayout.CENTER);
        jFrame.setVisible(true);
        selectionSortComponent.startAnimation();
    }
}

class ArrayUtil {
    static Random random = new Random();

    private ArrayUtil() {
    }

    public static int[] randomIntArray(int size, int bound) {
        int[] values = new int[size];
        for (int i = 0; i < values.length; i++) {
            values[i] = random.nextInt(bound);
        }
        return values;
    }
}

class SelectionSorter {
    private static final int DELAY = 50;
    private final int[] a;
    private final Lock sortStateLock;
    private final JComponent component;
    private int markedPosition = -1;
    private int alreadySorted = -1;

    public SelectionSorter(int[] anArray, JComponent aComponent) {
        a = anArray;
        sortStateLock = new ReentrantLock();
        component = aComponent;
    }

    public void draw(Graphics2D g2) {
        sortStateLock.lock();
        try {
            int deltaX = component.getWidth() / a.length;
            for (int i = 0; i < a.length; i++) {
                if (i == markedPosition) {
                    g2.setColor(Color.RED);
                } else if (i <= alreadySorted) {
                    g2.setColor(Color.BLUE);
                } else {
                    g2.setColor(Color.BLACK);
                }
                g2.draw(new Line2D.Double((double) i * deltaX, 0, (double) i * deltaX, a[i]));
            }
        } finally {
            sortStateLock.unlock();
        }
    }

    public void sort() throws InterruptedException {
        for (int i = 0; i < a.length - 1; i++) {
            int minPos = minimumPosition(i);
            sortStateLock.lock();
            try {
                swap(a, minPos, i);
                alreadySorted = i;
            } finally {
                sortStateLock.unlock();
            }
            pause(2);
        }
    }

    private int minimumPosition(int from) throws InterruptedException {
        int minPos = from;
        for (int i = from + 1; i < a.length; i++) {
            sortStateLock.lock();
            try {
                if (a[i] < a[minPos]) {
                    minPos = i;
                }
                markedPosition = i;
            } finally {
                sortStateLock.unlock();
            }
            pause(2);
        }
        return minPos;
    }

    public void swap(int[] a, int i, int j) {
        int temp = a[i];
        a[i] = a[j];
        a[j] = temp;
    }

    public void pause(int steps) throws InterruptedException {
        component.repaint();
        Thread.sleep((long) steps * DELAY);
    }
}

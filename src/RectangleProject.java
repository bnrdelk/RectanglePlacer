import javax.swing.*;
import java.awt.*;
import java.io.*;
import java.util.*;

public class RectangleProject extends JPanel {
    private static ArrayList<Rectangle> rectangles;
    private static int gridWidth;
    private static int gridHeight;
    private static final Color[] colors = {
            Color.RED, Color.BLUE, Color.GREEN, Color.YELLOW, Color.ORANGE,
            Color.CYAN, Color.MAGENTA, Color.PINK, Color.LIGHT_GRAY, Color.DARK_GRAY
    };

    public RectangleProject(ArrayList<Rectangle> rectangles, int gridWidth, int gridHeight) {
        this.rectangles = rectangles;
        this.gridWidth = gridWidth;
        this.gridHeight = gridHeight;
    }
    
    @Override
    protected void paintComponent(Graphics g) {
        super.paintComponent(g);
        Rectangle boundingRect = getWrappingFrame(rectangles);
        // Drawing the bounding rectangle
        g.setColor(Color.BLACK);
        g.drawRect(boundingRect.getX(), boundingRect.getY(), boundingRect.getWidth(), boundingRect.getHeight());

        // Setting colors for rectangles to draw
        for (int i = 0; i < rectangles.size(); i++) {
            if (i < colors.length) {
                g.setColor(colors[i]);
            } else {
                g.setColor(Color.BLACK); // Fallback color
            }

            Rectangle rect = rectangles.get(i);
            g.fillRect(rect.getX(), rect.getY(), rect.getWidth(), rect.getHeight());
        }
    }

    Rectangle getWrappingFrame(ArrayList<Rectangle> rectangles) {
        int minX = 99999; // int max
        int minY = 99999;
        int maxX = 0;// int min
        int maxY = 0;

        // find max and mins with looping each rectangle
        for (Rectangle rect : rectangles) {
            minX = Math.min(minX, rect.getX());
            minY = Math.min(minY, rect.getY());
            // to find most right and top
            maxX = Math.max(maxX, rect.getX() + rect.getWidth());
            maxY = Math.max(maxY, rect.getY() + rect.getHeight());
        }

        int width = maxX - minX;
        int height = maxY - minY;

        // frame is also a rectangle, create & locate with found x&y's
        Rectangle boundingRect = new Rectangle(width, height);
        boundingRect.setLocation(minX, minY);

        return boundingRect;
    }

    static boolean canPositionWithoutIntersect(ArrayList<Rectangle> rectangles, Rectangle newRect, int gridWidth, int gridHeight) {
        Random rand = new Random();
        int maxAttempts = 333;
        int attempts = 0;

        do {
                // Randomly choose to translate or rotate
                if (rand.nextBoolean()) {
                    // Translate
                    int x = rand.nextInt(gridWidth - newRect.getWidth());
                    int y = rand.nextInt(gridHeight - newRect.getHeight());
                    newRect.setLocation(x, y);
                } else {
                    // Rotate (swap width and height)
                    newRect.setSize(newRect.getWidth(), newRect.getHeight());
                    int x = rand.nextInt(gridWidth - newRect.getWidth());
                    int y = rand.nextInt(gridHeight - newRect.getHeight());
                    newRect.setLocation(x, y);
                }

                boolean placed = true;
                for (Rectangle rectangle : rectangles) {
                    if (newRect.intersects(rectangle)) {
                        placed = false;
                        break;
                    }
                }

                if (placed) {
                    return true; // successfully placed
                }

            attempts++;
        } while (attempts < maxAttempts);

        return false; // failed to place
    }


    public static void main(String[] args) {
        String filePath = "input.txt";
        int populationSize = 100000;

        // Read and place rectangles
        ArrayList<Rectangle> rectangles = readAndPlaceRectangles(filePath);
        if (rectangles.isEmpty()) {
            System.out.println("error.");
            return;
        }

        System.out.println("Rectangles placed successfully.");

        // Run genetic algorithm
        runGeneticAlgorithm(rectangles, gridWidth, gridHeight, populationSize);
    }

    public static ArrayList<Rectangle> readAndPlaceRectangles(String filePath) {
        ArrayList<Rectangle> rectangles = new ArrayList<>();
        int numRectangles;

        try {
            Scanner scanner = new Scanner(new File(filePath));
            numRectangles = scanner.nextInt();
            gridWidth = scanner.nextInt();
            gridHeight = scanner.nextInt();

            for (int i = 0; i < numRectangles; i++) {
                int width = scanner.nextInt();
                int height = scanner.nextInt();

                Rectangle rect = new Rectangle(width, height);

                boolean canPlaced = false;
                while (!canPlaced) {
                    canPlaced = canPositionWithoutIntersect(rectangles, rect, gridWidth, gridHeight);
                    if (canPlaced) {
                        rectangles.add(rect);
                    }
                }
            }

            scanner.close();
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        }

        return rectangles;
    }

    public static void runGeneticAlgorithm(ArrayList<Rectangle> rectangles, int gridWidth, int gridHeight, int populationSize) {
        ArrayList<Population> initialPopulation = Population.createInitialPopulation(rectangles, populationSize, gridWidth, gridHeight);

            ArrayList<Population> newPopulations = new ArrayList<>();

            // calculate fitness and select the best populations
            ArrayList<Population> selectedForCrossover = selectForCrossover(initialPopulation, 2);

            // apply crossover and mutation
            Population crossedOver = applyCrossover(selectedForCrossover.get(0), selectedForCrossover.get(1), gridWidth, gridHeight);
            crossedOver.applyMutation(gridWidth, gridHeight);
            newPopulations.add(crossedOver);

            // rearrenge population after crossover and mutation
            rearrangePopulation(initialPopulation, newPopulations, populationSize);


        // find the best Population (population with the lowest fitness value)
        Population bestPopulation = Collections.min(initialPopulation, Comparator.comparingInt(Population::calculateFitness));

        calculateResults(bestPopulation);

        // Create and display the RectanglePlacer
        RectangleProject placer = new RectangleProject(bestPopulation.getRectangles(), gridWidth, gridHeight);
        setGUI(placer, gridWidth, gridHeight);
    }

    private static void calculateResults(Population bestPopulation) {
        int bestArea = 0;
        for (Rectangle rect : bestPopulation.getRectangles()) {
            bestArea += (rect.getWidth() * rect.getHeight());
        }

        Rectangle boundingRect = bestPopulation.getWrappingFrame(bestPopulation.getRectangles());
        int instantArea = boundingRect.getWidth() * boundingRect.getHeight();
        int fitness = instantArea - bestArea;

        // Print the results
        System.out.println("Fitness: " + fitness);
        System.out.println("Instant Area: " + instantArea);
        System.out.println("Best Area: " + bestArea);
    }

    public static void setGUI(RectangleProject placer, int gridWidth, int gridHeight) {
        JFrame frame = new JFrame("Rectangle Placement");
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        frame.setContentPane(placer);
        frame.setSize(gridWidth, gridHeight);
        frame.setVisible(true);
    }


    public static ArrayList<Population> selectForCrossover(ArrayList<Population> population, int numberOfBestFits) {
        population.sort(Comparator.comparingInt(Population::calculateFitness));
        ArrayList<Population> crossoverList = new ArrayList<>();

        // Select the best-fit Populations for crossover
        for (Population Population : population) {
            crossoverList.add(Population);
            if (crossoverList.size() == numberOfBestFits) {
                break; // Stop once we have enough best-fit Populations
            }
        }

        return crossoverList;
    }

    public static void rearrangePopulation(ArrayList<Population> population, ArrayList<Population> newPopulations, int maxPopulationSize) {
        population.sort(Comparator.comparingInt(Population::calculateFitness));

        // Replace the worst Populations with children from crossover
        while (population.size() + newPopulations.size() > maxPopulationSize) {
            population.remove(population.size() - 1);
        }

        // Add new Populations
        population.addAll(newPopulations);
    }

    // Method for applying crossover
    public static Population applyCrossover(Population parent1, Population parent2, int gridWidth, int gridHeight) {
        ArrayList<Rectangle> childRectangles = new ArrayList<>();

        for (int i = 0; i < parent1.rectangles.size(); i++) {
            Rectangle randomRect = selectRectangleRandomly(parent1.rectangles.get(i), parent2.rectangles.get(i));

            if(canPositionWithoutIntersect(childRectangles, randomRect, gridWidth, gridHeight)){
                childRectangles.add(randomRect);
            }
        }

        return new Population(childRectangles);
    }

    // Method for selecting a rectangle randomly
    private static Rectangle selectRectangleRandomly(Rectangle rect1, Rectangle rect2){
        Rectangle randomRect;
        Random rand = new Random();

        // Select a rectangle with 50% chance
        if(rand.nextBoolean()){
            randomRect = new Rectangle(rect1.getWidth(), rect1.getHeight());
            randomRect.setLocation(rect1.getX(), rect1.getY());
        }
        else{
            randomRect = new Rectangle(rect2.getWidth(), rect2.getHeight());
            randomRect.setLocation(rect2.getX(), rect2.getY());
        }
        return randomRect;
    }
}

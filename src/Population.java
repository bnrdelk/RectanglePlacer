import java.util.ArrayList;
import java.util.Random;

public class Population {
    ArrayList<Rectangle> rectangles;

    public Population(ArrayList<Rectangle> rectangles) {
        this.rectangles = new ArrayList<>(rectangles);
    }

    // method to start population
    static ArrayList<Population> createInitialPopulation(ArrayList<Rectangle> baseRectangles, int populationSize, int gridWidth, int gridHeight) {
        ArrayList<Population> population = new ArrayList<>();
        Random rand = new Random();

        for (int i = 0; i < populationSize; i++) {
            ArrayList<Rectangle> rectangles = new ArrayList<>();
            int remainingRectangles = baseRectangles.size();

            while (remainingRectangles > 0) {
                Rectangle baseRect = baseRectangles.get(baseRectangles.size() - remainingRectangles);
                Rectangle rectangle;
                boolean isIntersect;

                do {
                    // set coordinates randomly with
                    int x = rand.nextInt(gridWidth-baseRect.getWidth());
                    int y = rand.nextInt(gridHeight-baseRect.getHeight());

                    rectangle = new Rectangle(baseRect.getWidth(), baseRect.getHeight());
                    rectangle.setLocation(x, y);

                    // foreach loop until rectangle is instersects with another rect
                    isIntersect = false;
                    for (Rectangle rect : rectangles) {
                        if (rectangle.intersects(rect)) {
                            isIntersect = true;
                            break;
                        }
                    }
                    // finish the loop, valid rectangle is found
                } while (isIntersect);

                // add to the rectangles & decrease the remaining numbers
                rectangles.add(rectangle);
                remainingRectangles--;
            }

            // population has created
            population.add(new Population(rectangles));
        }


        return population;
    }

    // method to calculate fitness number
    public int calculateFitness() {
        int bestArea = 0;

        for (Rectangle rect : this.rectangles) {
            bestArea += rect.getWidth() * rect.getHeight();
        }

        Rectangle frame = getWrappingFrame(rectangles);

        int instantArea = frame.getWidth() * frame.getHeight();
        int fitness = instantArea - bestArea;

        return fitness;
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

    // mutate method to increase variants
    public void applyMutation(int gridWidth, int gridHeight) {
        Random rand = new Random();

        // mutation has %15 chance
        if (rand.nextDouble() <= 0.15) {
            int randomIndex = rand.nextInt(rectangles.size());

            // get before mutation rect randomly
            Rectangle originalRect = rectangles.get(randomIndex);
            // create the original ones copy
            Rectangle mutatedRect = new Rectangle(originalRect.getWidth(), originalRect.getHeight());

            // deciding to move or rotate randomly
            if (rand.nextBoolean()) {
                // change x-y location (translate)
                mutatedRect.setLocation(rand.nextInt(gridHeight - mutatedRect.getHeight()), rand.nextInt(gridWidth - mutatedRect.getWidth()));
            } else {
                // rotate (swap width and height)
                int temp = mutatedRect.getHeight();
                mutatedRect.setSize(mutatedRect.getWidth(), temp);
                mutatedRect.setLocation(rand.nextInt(gridHeight - mutatedRect.getHeight()), rand.nextInt(gridWidth - mutatedRect.getWidth()));
            }

            // making sure the mutated rectangle does not intersect with others
            if (placeRectangleWithIndex(rectangles, mutatedRect, gridWidth, gridHeight, randomIndex)) {
                // replace original rect with the mutated rect
                rectangles.set(randomIndex, mutatedRect);
            }
        }
    }

    public static boolean placeRectangleWithIndex(ArrayList<Rectangle> rectangles, Rectangle newRect, int gridWidth, int gridHeight, int index) {
        Random rand = new Random();
        int maxAttempts = 500;
        int attempts = 0;

        do {
            int x = rand.nextInt(gridWidth - newRect.getWidth());
            int y = rand.nextInt(gridHeight - newRect.getHeight());
            newRect.setLocation(x, y);

            boolean canPlaced = true;
            for (int i = 0; i < rectangles.size(); i++) {
                if (i != index && newRect.intersects(rectangles.get(i))) {
                    canPlaced = false;
                    break;
                }
            }
            if (canPlaced) {
                return true; // successfully placed
            }
            attempts++;
        } while (attempts < maxAttempts);

        return false; // fail
    }

    public ArrayList<Rectangle> getRectangles() {
        return rectangles;
    }
}
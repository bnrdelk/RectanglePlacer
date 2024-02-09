public class Rectangle {
    private int x;
    private int y;
    private int width;
    private int height;

    public Rectangle(int width, int height) {
        this.width = width;
        this.height = height;
        this.x = 0;
        this.y = 0;
    }

    // method to control instersections
    public boolean intersects(Rectangle other) {
        return (x < other.x + other.width && x + width > other.x && y < other.y + other.height && y + height > other.y);
    }

    // getters
    public int getWidth() {
        return width;
    }

    public int getHeight() {
        return height;
    }

    public void setWidth(int width) {
        this.width = width;
    }

    public void setHeight(int height) {
        this.height = height;
    }

    public int getX() {
        return x;
    }

    public int getY() {
        return y;
    }

    public void setX(int x) {
        this.x = x;
    }

    public void setY(int y) {
        this.y = y;
    }

    @Override
    public String toString() {
        return "Rectangle [x=" + x + ", y=" + y + ", width=" + width + ", height=" + height + "]";
    }

    // set coordinates
    public void setLocation(int x, int y) {
        this.x = x;
        this.y = y;
    }

    // set size
    public void setSize(int width, int height) {
        this.width = width;
        this.height = height;
    }

}

package core;

public class IntVector2D {

	private int x;
	private int y;
	
	public static IntVector2D ORIGIN() {
		return new IntVector2D(0, 0);
	}
	
	public IntVector2D(int x, int y) {
		this.x = x;
		this.y = y;
	}
	
	public IntVector2D(double x, double y) {
		this((int) x, (int) y);
	}
	
	public int getX() {
		return x;
	}
	
	public void setX(int x) {
		this.x = x;
	}
	
	public int getY() {
		return y;
	}
	
	public void setY(int y) {
		this.y = y;
	}
	
	public void set(int x, int y) {
		this.x = x;
		this.y = y;
	}
	
	public void set(IntVector2D o) {
		this.x = o.getX();
		this.y = o.getY();
	}
	
	/**
	 * Adds the x and y values of two vectors.
	 * 
	 * @param other
	 * @return A new vector c such that c.x = a.x + b.x and c.y = a.y + b.y
	 */
	public IntVector2D sum(IntVector2D other) {
		return new IntVector2D(this.x + other.getX(), this.y + other.getY());
	}
	
	/**
	 * Adds a scalar value from both dimensions of this vector.
	 * 
	 * @param scalar
	 * @return A new vector c such that c.x = a.x + scalar and c.y = a.y + scalar
	 */
	public IntVector2D scalarSum(int scalar) {
		return new IntVector2D(x + scalar, y + scalar);
	}
	
	/**
	 * Subtracts the x and y values of two vectors.
	 * 
	 * @param other
	 * @return A new vector c such that c.x = b.x - a.x and c.y = b.y - a.y
	 */
	public IntVector2D sub(IntVector2D other) {
		return new IntVector2D(this.x - other.getX(), this.y - other.getY());
	}
	
	/**
	 * Subtracts a scalar value from both dimensions of this vector.
	 * 
	 * @param scalar
	 * @return A new vector c such that c.x = a.x - scalar and c.y = a.y - scalar
	 */
	public IntVector2D scalarSub(double scalar) {
		return new IntVector2D(x - scalar, y - scalar);
	}
	
	/**
	 * Multiples both dimensions of this vector by a scalar value.
	 * 
	 * @param scalar
	 * @return
	 */
	public IntVector2D scalarMult(double scalar) {
		return new IntVector2D(x * scalar, y * scalar);
	}
	
	/**
	 * Divides both dimensions of this vector by a scalar value.
	 * 
	 * @param scalar
	 * @return
	 */
	public IntVector2D scalarDiv(double scalar) {
		return new IntVector2D((int) (x / scalar), (int) (y / scalar));
	}
	
	public DoubleVector2D asDoubleVector() {
		return new DoubleVector2D(this.getX(), this.getY());
	}
	
	public boolean isAdjacentTo(IntVector2D o) {
		return Math.abs(x - o.x) + Math.abs(y - o.y) == 1;
	}
	
	public double getDistanceFrom(IntVector2D o) {
		return Math.sqrt((o.x - x) * (o.x - x) + (o.y - y) * (o.y - y));
	}
	
	@Override
	public String toString() {
		return "(" + x + ", " + y + ")";
	}
	
	// Required to use as key in hashmap
	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + x;
		result = prime * result + y;
		return result;
	}

	@Override
	public boolean equals(Object obj) {
		if (this == obj)
			return true;
		if (obj == null)
			return false;
		if (getClass() != obj.getClass())
			return false;
		IntVector2D other = (IntVector2D) obj;
		if (x != other.x)
			return false;
		if (y != other.y)
			return false;
		return true;
	}
	
	@Override
	public IntVector2D clone() {
		return new IntVector2D(x, y);
	}
	
}

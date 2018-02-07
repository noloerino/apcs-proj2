package core;

public class DoubleVector2D {

	private double x;
	private double y;
	
	public static DoubleVector2D ORIGIN() {
		return new DoubleVector2D(0, 0);
	}
	
	public DoubleVector2D(int x, int y) {
		this.x = x;
		this.y = y;
	}
	
	public DoubleVector2D(double x, double y) {
		this.x = x;
		this.y = y;
	}
	
	public double getX() {
		return x;
	}
	
	public void setX(double x) {
		this.x = x;
	}
	
	public double getY() {
		return y;
	}
	
	public void setY(double y) {
		this.y = y;
	}
	
	public void set(double x, double y) {
		this.x = x;
		this.y = y;
	}
	
	public void set(DoubleVector2D o) {
		this.x = o.getX();
		this.y = o.getY();
	}
	
	/**
	 * Adds the x and y values of two vectors.
	 * 
	 * @param other
	 * @return A new vector c such that c.x = a.x + b.x and c.y = a.y + b.y
	 */
	public DoubleVector2D sum(DoubleVector2D other) {
		return new DoubleVector2D(this.x + other.getX(), this.y + other.getY());
	}
	
	/**
	 * Adds a scalar value from both dimensions of this vector.
	 * 
	 * @param scalar
	 * @return A new vector c such that c.x = a.x + scalar and c.y = a.y + scalar
	 */
	public DoubleVector2D scalarSum(double scalar) {
		return new DoubleVector2D(x + scalar, y + scalar);
	}
	
	/**
	 * Subtracts the x and y values of two vectors.
	 * 
	 * @param other
	 * @return A new vector c such that c.x = b.x - a.x and c.y = b.y - a.y
	 */
	public DoubleVector2D sub(DoubleVector2D other) {
		return new DoubleVector2D(this.x - other.getX(), this.y - other.getY());
	}
	
	/**
	 * Subtracts a scalar value from both dimensions of this vector.
	 * 
	 * @param scalar
	 * @return A new vector c such that c.x = a.x - scalar and c.y = a.y - scalar
	 */
	public DoubleVector2D scalarSub(double scalar) {
		return new DoubleVector2D(x - scalar, y - scalar);
	}
	
	/**
	 * Multiples both dimensions of this vector by a scalar value.
	 * 
	 * @param scalar
	 * @return
	 */
	public DoubleVector2D scalarMult(double scalar) {
		return new DoubleVector2D(x * scalar, y * scalar);
	}
	
	/**
	 * Divides both dimensions of this vector by a scalar value.
	 * 
	 * @param scalar
	 * @return
	 */
	public DoubleVector2D scalarDiv(double scalar) {
		return new DoubleVector2D(x / scalar, y / scalar);
	}
	
	public IntVector2D asIntVector() {
		return new IntVector2D(getX(), getY());
	}
	
	public boolean isAdjacentTo(DoubleVector2D o) {
		return Math.abs(x - o.x) + Math.abs(y - o.y) == 1;
	}
	
	public double getDistanceFrom(DoubleVector2D o) {
		return Math.sqrt((o.x + x) * (o.x + x) + (o.y + y) * (o.y + y));
	}
	
	@Override
	public String toString() {
		return "(" + x + ", " + y + ")";
	}

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		long temp;
		temp = Double.doubleToLongBits(x);
		result = prime * result + (int) (temp ^ (temp >>> 32));
		temp = Double.doubleToLongBits(y);
		result = prime * result + (int) (temp ^ (temp >>> 32));
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
		DoubleVector2D other = (DoubleVector2D) obj;
		if (Double.doubleToLongBits(x) != Double.doubleToLongBits(other.x))
			return false;
		if (Double.doubleToLongBits(y) != Double.doubleToLongBits(other.y))
			return false;
		return true;
	}
	
	@Override
	public DoubleVector2D clone() {
		return new DoubleVector2D(x, y);
	}
	
}

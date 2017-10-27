package org.genericsystem.reinforcer.tools;

import java.util.Optional;

import org.genericsystem.reinforcer.NormalizedRect;

public class GSRect implements Comparable<GSRect> {
	private double x, y, width, height;

	public GSRect(double x, double y, double width, double height) {
		this.x = x;
		this.y = y;
		this.width = width;
		this.height = height;
	}

	public GSRect() {
		this(0, 0, 0, 0);
	}

	public GSRect(GSPoint p1, GSPoint p2) {
		x = p1.getX() < p2.getX() ? p1.getX() : p2.getX();
		y = p1.getY() < p2.getY() ? p1.getY() : p2.getY();
		width = (p1.getX() > p2.getX() ? p1.getX() : p2.getX()) - x;
		height = (p1.getY() > p2.getY() ? p1.getY() : p2.getY()) - y;
	}

	public GSRect(GSPoint p, GSSize s) {
		this(p.getX(), p.getY(), s.getWidth(), s.getHeight());
	}

	public GSRect(double[] vals) {
		set(vals);
	}

	private void set(double[] vals) {
		if (vals != null) {
			x = vals.length > 0 ? vals[0] : 0;
			y = vals.length > 1 ? vals[1] : 0;
			width = vals.length > 2 ? vals[2] : 0;
			height = vals.length > 3 ? vals[3] : 0;
		} else {
			x = 0;
			y = 0;
			width = 0;
			height = 0;
		}
	}

	@Override
	public GSRect clone() {
		return new GSRect(x, y, width, height);
	}

	public GSPoint tl() {
		return new GSPoint(x, y);
	}

	public GSPoint br() {
		return new GSPoint(x + width, y + height);
	}

	public GSSize size() {
		return new GSSize(width, height);
	}

	public double area() {
		return width * height;
	}

	public boolean empty() {
		return width <= 0 || height <= 0;
	}

	/**
	 * Checks if a {@link GSPoint} is contained in a {@link GSRect} (being inclusive).
	 * 
	 * @param rect - the rectangle
	 * @param p - the point
	 * @return true if <code>p</code> is contained in <code>rect</code>, false otherwise
	 */
	public boolean contains(GSPoint p) {
		return x <= p.getX() && p.getX() <= x + width && y <= p.getY() && p.getY() <= y + height;
	}

	/**
	 * Compute the inclusive area between two rectangles (intersection area / union area)
	 * 
	 * @param rect1 - the first rectangle
	 * @param rect2 - the second rectangle
	 * @return the percentage of overlap between the two rectangles, defined by <code>intersection.area() / union.area()</code>
	 */
	public double inclusiveArea(GSRect rect2) {
		Optional<GSRect> optional = getIntersection(rect2);
		if (!optional.isPresent())
			return 0;
		GSRect intersection = optional.get();
		GSRect union = getUnion(rect2);
		double area = intersection.area() / union.area();
		return area;
	}

	/**
	 * Compute the intersection of two rectangles.
	 * 
	 * @param rect1 - the first rectangle
	 * @param rect2 - the second rectangle
	 * @return an {@link Optional} with the intersecting {@link GSRect}, or an empty Optional if no intersection was found
	 */
	public Optional<GSRect> getIntersection(GSRect rect2) {
		// First, check whether a rectangle is contained in the other
		Optional<GSRect> insider = getInsider(rect2);
		if (insider.isPresent())
			return insider;

		// If not, compute the intersection
		double tlX = Math.max(tl().getX(), rect2.tl().getX());
		double tlY = Math.max(tl().getY(), rect2.tl().getY());
		double brX = Math.min(br().getX(), rect2.br().getX());
		double brY = Math.min(br().getY(), rect2.br().getY());

		if (brX - tlX <= 0 || brY - tlY <= 0) // XXX: swap tl and br if < 0?
			return Optional.empty();
		else
			return Optional.of(new GSRect(new GSPoint(tlX, tlY), new GSPoint(brX, brY)));
	}

	/**
	 * Compute the union of two rectangles.
	 * 
	 * @param rect1 - the first rectangle
	 * @param rect2 - the second rectangle
	 * @return the union {@link GSRect}
	 */
	public GSRect getUnion(GSRect rect2) {
		// First, check whether a rectangle is contained in the other
		Optional<GSRect> inside = getInsider(rect2);
		if (inside.isPresent()) {
			GSRect insider = inside.get();
			return insider.equals(this) ? rect2 : this;
		}

		// If not, compute the union
		double tlX = Math.min(tl().getX(), rect2.tl().getX());
		double tlY = Math.min(tl().getY(), rect2.tl().getY());
		double brX = Math.max(br().getX(), rect2.br().getX());
		double brY = Math.max(br().getY(), rect2.br().getY());

		return new GSRect(new GSPoint(tlX, tlY), new GSPoint(brX, brY));
	}

	/**
	 * Check whether this rectangle overlaps with the given rectangle. This method is inclusive, e.g. it will return true if the rectangles have only a side or a vertex in common.
	 * 
	 * @param other - the second rectangle
	 * @return true is the rectangles overlap, false otherwise
	 * @throws IllegalArgumentException if at least one of the rectangles is <code>null</code>
	 */
	public boolean isOverlapping(GSRect other) throws IllegalArgumentException {
		if (other == null)
			throw new IllegalArgumentException("One of the rectangles is null");
		return x <= other.br().getX() && other.tl().getX() <= br().getX() && y <= other.br().getY() && other.tl().getY() <= br().getY();
	}

	/**
	 * Compare this rectangle with another rectangle, and returns the smaller rectangle if it is inside the other. Returns an empty {@link Optional} if no rectangles is contained in the other.
	 * 
	 * @param rect2 - the second rectangle
	 * @return an {@link Optional} with the rectangle contained in the other, an empty Optional if no rectangles is contained in the other.
	 */
	public Optional<GSRect> getInsider(GSRect rect2) {
		GSPoint[] points1 = decomposeClockwise();
		GSPoint[] points2 = rect2.decomposeClockwise();
		boolean isGSRect2InGSRect1 = true;
		boolean isGSRect1InGSRect2 = true;

		for (GSPoint p : points2) {
			if (!contains(p))
				isGSRect2InGSRect1 = false;
		}

		if (!isGSRect2InGSRect1) {
			for (GSPoint p : points1) {
				if (!rect2.contains(p))
					isGSRect1InGSRect2 = false;
			}
			if (isGSRect1InGSRect2)
				return Optional.of(this);
			else
				return Optional.empty();
		} else {
			return Optional.of(rect2);
		}
	}

	/**
	 * Decompose the {@link GSRect} in four points starting from tl(), clockwise.
	 * 
	 * @return an array of {@link GSPoint} in the order tl, tr, br, bl
	 */
	public GSPoint[] decomposeClockwise() {
		GSPoint[] points = new GSPoint[] { tl(), new GSPoint(br().getX(), tl().getY()), br(), new GSPoint(tl().getX(), br().getY()) };
		return points;
	}

	public NormalizedRect normalize(double mintlx, double mintly, double width, double height) {
		return new NormalizedRect((x - mintlx) / width, (y - mintly) / height, this.width / width, this.height / height);
	}

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		long temp;
		temp = Double.doubleToLongBits(height);
		result = prime * result + (int) (temp ^ (temp >>> 32));
		temp = Double.doubleToLongBits(width);
		result = prime * result + (int) (temp ^ (temp >>> 32));
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
		if (!(obj instanceof GSRect))
			return false;
		GSRect it = (GSRect) obj;
		return x == it.x && y == it.y && width == it.width && height == it.height;
	}

	@Override
	public String toString() {
		return String.format("{GSRect, tlx: %,.1f, tly: %,.1f, width: %,.1f, height: %,.1f}", x, y, width, height);
	}

	public double getX() {
		return x;
	}

	public double getY() {
		return y;
	}

	public double getWidth() {
		return width;
	}

	public double getHeight() {
		return height;
	}

	@Override
	public int compareTo(GSRect o) {
		if (x != o.x)
			return (int) Math.signum(x - o.x);
		if (y != o.y)
			return (int) Math.signum(y - o.y);
		if (width != o.width)
			return (int) Math.signum(width - o.width);
		return (int) Math.signum(height - o.height);
	}
}
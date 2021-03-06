package visitor;

/**
 * A constant value.
 */
public class Value implements Expression {

	private double value_; // the value

	/**
	 * @param value
	 *          the value
	 */
	public Value ( double value ) {
		value_ = value;
	}

	/**
	 * Get the value.
	 * 
	 * @return the value
	 */
	public double getValue () {
		return value_;
	}

//	/**
//	 * Compute and return the value of this expression.
//	 * 
//	 * @return the value of the expression
//	 */
//	public double evaluate () {
//		return value_;
//	}
//
//	/**
//	 * Return a string representation of this expression (infix format).
//	 * 
//	 * @return a string representation of the expression (infix format)
//	 */
//	public String toString () {
//		return "" + value_;
//	}
	
	 public <T> T accept ( ExpressionVisitor<T> visitor ) {
	      return visitor.visit(this);
	  }
}

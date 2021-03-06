package decorator;

import javafx.scene.canvas.GraphicsContext;
import javafx.scene.paint.Color;

/**
 * @author ak5158
 *
 */
public class ResizerBrick extends DecoratedBrick {

	private double resize_;
	private Color color_ ;
	/**
	 * @param brick
	 */
	public ResizerBrick ( Brick brick, double amount ) {
		super(brick);
		resize_ = amount;
	}
	public ResizerBrick (double left, double top, double width, double height,
      Color color, int points, int hits, double amount ) {
		super(new PlainBrick(left,top,width,height,color,points,hits));
		resize_ = amount;
		color_ = color;
	}

	@Override
	public ResizedBall decorate ( Ball ball ) {
		return new ResizedBall(ball,resize_);
	
	}

	@Override
	public void draw ( GraphicsContext g ) {
		super.draw(g);
		g.setFill(color_);
		g.fillRect(brick_.getLeft() + 1,brick_.getTop() + 1,brick_.getWidth() - 2,brick_.getHeight() - 2);
		g.setStroke(Color.BLACK);
		g.strokeRect(brick_.getLeft() + 1,brick_.getTop() + 1,brick_.getWidth() - 2,brick_.getHeight()  - 2);

	}
}

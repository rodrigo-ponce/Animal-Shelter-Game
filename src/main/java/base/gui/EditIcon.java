package base.gui;

import base.Game;
import base.gameobjects.Animal;
import base.graphicsservice.Position;
import base.graphicsservice.Rectangle;
import base.graphicsservice.RenderHandler;
import base.graphicsservice.Sprite;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class EditIcon {

    private final Sprite sprite;
    private Rectangle rectangle;

    protected static final Logger logger = LoggerFactory.getLogger(EditIcon.class);

    public EditIcon(Sprite sprite) {
        this.sprite = sprite;
        rectangle = new Rectangle();
    }

    public void changePosition(Position position) {
        rectangle = new Rectangle(position.getXPosition() - 3, position.getYPosition() - 3, 20, 20);
    }

    public void render(RenderHandler renderer, int xZoom, int yZoom) {
        renderer.renderSprite(sprite, rectangle.getX() + 2, rectangle.getY() + 2, xZoom, yZoom, true);
    }

    public boolean handleMouseClick(Rectangle mouseRectangle, Game game, Animal animal) {
        if (mouseRectangle.intersects(rectangle)) {
            logger.info("edit icon clicked");
            game.editAnimalName(animal);
            return true;
        }
        return false;
    }
}
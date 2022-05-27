package base.gameobjects;

import base.Game;
import base.graphicsservice.ImageLoader;
import base.graphicsservice.Rectangle;
import base.graphicsservice.RenderHandler;
import base.map.GameMap;
import base.map.MapTile;
import base.navigationservice.Direction;
import base.navigationservice.NavigationService;
import base.navigationservice.Route;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import static base.constants.ColorConstant.GREEN;
import static base.constants.Constants.TILE_SIZE;
import static base.constants.Constants.ZOOM;
import static base.constants.FilePath.NPC_SHEET_PATH_LADY;
import static base.constants.MapConstants.MAIN_MAP;
import static base.navigationservice.Direction.DOWN;
import static base.navigationservice.Direction.STAY;

public class Npc implements GameObject, Walking {

    private static final Logger logger = LoggerFactory.getLogger(Npc.class);

    private final AnimatedSprite animatedSprite;
    private final Rectangle rectangle;

    private Direction direction;
    private int movingTicks = 0;
    private transient Route route;
    private String currentMap;
    private final int speed;

    public Npc(int startX, int startY) {
        animatedSprite = ImageLoader.getAnimatedSprite(NPC_SHEET_PATH_LADY, 64);
        logger.info("Loaded sprite");
        speed = 2;
        route = new Route();
        direction = DOWN;

        rectangle = new Rectangle(startX, startY, 32, 32);
        rectangle.generateBorder(1, GREEN);
    }

    @Override
    public void render(RenderHandler renderer, int xZoom, int yZoom) {
        int xForSprite = rectangle.getX() - 32;
        int yForSprite = rectangle.getY() - 48;
        if (animatedSprite != null) {
            renderer.renderSprite(animatedSprite, xForSprite, yForSprite, xZoom, yZoom, false);
//        } else {
            renderer.renderRectangle(rectangle, xZoom, yZoom, false);
        }
    }

    @Override
    public void update(Game game) {
        boolean isMoving = false;
        Direction nextDirection = direction;

        if (route.isEmpty() && !getCurrentMap().equals(MAIN_MAP)) {
            route = game.calculateRouteToMap(this, NavigationService.getNextPortalToGetToCenter(getCurrentMap()));
            movingTicks = 16;
        }

        if (movingTicks < 1 && !route.isEmpty()) {
            nextDirection = route.getNextStep();
            movingTicks = 16;
            logger.info(String.format("Direction: %s, moving ticks: %d", direction.name(), movingTicks));
        }

        if (route.isEmpty() && getCurrentMap().equals(MAIN_MAP)) {
            nextDirection = STAY;
        }

        handleMoving(game.getGameMap(currentMap), nextDirection);
        if (nextDirection != STAY) {
            isMoving = true;
        }

        if (nextDirection != direction) {
            direction = nextDirection;
            updateDirection();
        }
        if (animatedSprite != null) {
            if (isMoving) {
                animatedSprite.update(game);
            } else {
                animatedSprite.reset();
            }
        }

        checkPortal(game);

        movingTicks--;
    }

    @Override
    public int getLayer() {
        return 1;
    }

    @Override
    public boolean handleMouseClick(Rectangle mouseRectangle, Rectangle camera, int xZoom, int yZoom, Game game) {
        return false;
    }

    private void handleMoving(GameMap gameMap, Direction direction) {
        if (unwalkableInThisDirection(gameMap, direction, rectangle, speed, getLayer())) {
            route = new Route();
            movingTicks = 0;
            handleUnwalkable(rectangle, direction, speed);
            return;
        }

        switch (direction) {
            case LEFT:
                if (rectangle.getX() > 0 || nearPortal(gameMap.getPortals(), rectangle)) {
                    rectangle.setX(rectangle.getX() - speed);
                }
                break;
            case RIGHT:
                if (rectangle.getX() < (gameMap.getMapWidth() * TILE_SIZE - rectangle.getWidth()) * ZOOM || nearPortal(gameMap.getPortals(), rectangle)) {
                    rectangle.setX(rectangle.getX() + speed);
                }
                break;
            case UP:
                if (rectangle.getY() > 0 || nearPortal(gameMap.getPortals(), rectangle)) {
                    rectangle.setY(rectangle.getY() - speed);
                }
                break;
            case DOWN:
                if (rectangle.getY() < (gameMap.getMapHeight() * TILE_SIZE - rectangle.getHeight()) * ZOOM || nearPortal(gameMap.getPortals(), rectangle)) {
                    rectangle.setY(rectangle.getY() + speed);
                }
                break;
        }
    }

    private void updateDirection() {
        if (direction == STAY) {
            animatedSprite.setAnimationRange(0, 1);
            return;
        }
        if (animatedSprite != null && direction != null) {
            animatedSprite.setAnimationRange((direction.directionNumber * 8), (direction.directionNumber * 8 + 7)); //if horizontal increase
        }
    }

    private void checkPortal(Game game) {
        MapTile tile = getPortalTile(game, currentMap, rectangle);
        if (tile != null) {
            game.moveNpcToAnotherMap(this, tile);
        }
    }

    public String getCurrentMap() {
        return currentMap;
    }

    public void setCurrentMap(String currentMap) {
        this.currentMap = currentMap;
    }

    public Rectangle getRectangle() {
        return rectangle;
    }
}
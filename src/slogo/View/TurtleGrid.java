package slogo.View;

import java.io.ObjectInputFilter;
import java.util.ArrayList;
import java.util.List;
import java.util.Properties;

import javafx.beans.property.BooleanProperty;
import javafx.beans.property.SimpleBooleanProperty;
import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;
import javafx.collections.ListChangeListener;
import javafx.collections.ObservableList;
import javafx.geometry.Insets;
import javafx.scene.Node;
import javafx.scene.canvas.Canvas;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.*;
import javafx.scene.paint.Color;
import javafx.scene.paint.Paint;
import javafx.scene.shape.Line;
import slogo.Main;
import slogo.model.Turtle;

/**
 * This class holds the grid where the commands are executed on; for example, if the turtle moves
 * forward with its pen down, we would see the line on the canvas. The class also has a drawer class
 * to draw when the pen is down and knows the turtle's information (like location) through binding,
 * and updates the turtle's properties through these bindings
 *
 * @author Michelle Tai, Sanna Symer
 */
public class TurtleGrid {
 //trying to push
  private static final int TURTLE_IMAGE_HEIGHT = 40;
  private static final int TURTLE_IMAGE_WIDTH = 40;
  private static final Color DEFAULT_PEN_COLOR = Color.RED;
  private static final Color DEFAULT_BACKGROUND = Color.LINEN;
  private static final double DEFAULT_PEN_WIDTH = 1;
  private static final int constantOne = 1;
  private int myCanvasWidth, myCanvasHeight;
  private ObservableList<Turtle> viewTurtles;
  private ObservableList<Turtle> activeTurtles;
  private Configuration properties;
  private ArrayList<ImageView> turtleImageViews = new ArrayList<>();
  private Pane myPane; //to change background of grid, change the background of the pane
  private Canvas myCanvas;
  private static final int DEFAULT_CANVAS_WIDTH = 600;
  private static final int DEFAULT_CANVAS_HEIGHT = 600;
  private static final String TURTLE_IMAGE = "TurtleImage";
  private static final Double middleOfTheScreen = 2.0;
  private static final int zeroIndex=0;
  private static final double Opacity=0.7;
  private StackPane retGrid;
  private double centerX, centerY;
  private double turtleCenterX, turtleCenterY;
  private Boolean isPenDown = true;
  private ArrayList<Line> linesDrawn;
  private Paint penColor;
  private static final String commaString=",";
  private double penWidth;
  private Configuration PropertiesView;
  private BooleanProperty clearScreen = new SimpleBooleanProperty();
  private static final int PADDING_INSET = 10;
  

  /**
   * Constructor for the TurtleGrid class, which initializes everything
   *
   * @param canvasWidth  is the width of the canvas where the turtle is located, and where all the
   *                     shapes are drawn
   * @param canvasHeight is the height of the canvas
   */
  public TurtleGrid(int canvasWidth, int canvasHeight, ObservableList<Turtle> viewTurtles, Configuration config) {
    PropertiesView= config;
    myCanvasWidth = canvasWidth;
    myCanvasHeight = canvasHeight;
    centerX = canvasWidth / middleOfTheScreen;
    centerY = canvasHeight / middleOfTheScreen;
    setUpPane();
    setBackground(DEFAULT_BACKGROUND);
    myCanvas = new Canvas(myCanvasWidth, myCanvasHeight);
    setUpGrid();
    penColor = DEFAULT_PEN_COLOR;
    penWidth = DEFAULT_PEN_WIDTH;
    linesDrawn = new ArrayList<>();
    this.viewTurtles = viewTurtles;
    for (Turtle viewTurtle : this.viewTurtles) {
      setUpTurtle(viewTurtle);
    }
    addSizeListener();
  }

public Configuration getConfig(){
      return PropertiesView;
}
  public TurtleGrid(ObservableList<Turtle> turtles, Configuration config,ObservableList<Turtle> activatedTurtles) {
    this(DEFAULT_CANVAS_WIDTH, DEFAULT_CANVAS_HEIGHT, turtles, config);
    activeTurtles = activatedTurtles;
  }

  private void setUpGrid() {
    retGrid = new StackPane();
    retGrid.setPadding(new Insets(PADDING_INSET, PADDING_INSET, PADDING_INSET, zeroIndex));
    retGrid.getChildren().addAll(myCanvas, myPane);
  }

  private void setUpPane() {
    myPane = new Pane();
    myPane.setMaxWidth(myCanvasWidth);
    myPane.setMaxHeight(myCanvasHeight);
  }

  private BooleanProperty clearScreenProperty() {
    return clearScreen;
  }

  private void setUpTurtle(Turtle turtle) {
    Image turtleImage = new Image(Main.myResources.getString(TURTLE_IMAGE));
    ImageView turtleImageView = new ImageView(turtleImage);
    turtleImageView.setOpacity(Opacity);
    int idIndex = turtle.getId() - constantOne;
    turtleImageViews.add(turtle.getId()-constantOne, turtleImageView);
    ImageView imageView = turtleImageViews.get(turtle.getId() - constantOne);
    imageView.setX(centerX);
    imageView.setY(centerY);
    imageView.setFitHeight(TURTLE_IMAGE_HEIGHT);
    imageView.setFitWidth(TURTLE_IMAGE_WIDTH);
    imageView.rotateProperty();
    imageView.requestFocus();
    addListeners(turtle);
    imageView.setOnMouseClicked(e-> {
      turtle.setActivated(!turtle.isActivatedProperty().getValue());
      changeOpacity(turtle);
    });
    myPane.getChildren().add(turtleImageViews.get(turtle.getId()-constantOne));

    turtleCenterX = turtleImageViews.get(turtle.getId()-constantOne).getFitWidth() / 2;
    turtleCenterY = turtleImageViews.get(turtle.getId()-constantOne).getFitHeight() / 2;
  }

  private void changeOpacity(Turtle turtle) {
    ImageView opaquePics = turtleImageViews.get(turtle.getId()-constantOne);
    if(!turtle.isActivatedProperty().getValue()){
      opaquePics.setOpacity(0.2);
    } else{
      opaquePics.setOpacity(Opacity);
    }
    turtleImageViews.set(turtle.getId()-constantOne, opaquePics);
  }

  private void addListeners(Turtle viewTurtle) {
    addCoordinatesListener(viewTurtle);
    addAnglePropertyListener(viewTurtle);
    addPenDownListener(viewTurtle);
    addClearScreenListener(viewTurtle);
    addShowingListener(viewTurtle);
    addActiveListener(viewTurtle);
  }


  private double keepInBoundsX(double coordinate, int bound) {
    if (coordinate > bound - TURTLE_IMAGE_HEIGHT) {
      return bound - TURTLE_IMAGE_HEIGHT;
    } else if (coordinate < zeroIndex) {
      return zeroIndex;
    }
    return coordinate;
  }

  //TODO: need to make each turtle have a center x
  private void addCoordinatesListener(Turtle viewTurtle) {
    viewTurtle.coordinatesProperty().addListener(new ChangeListener() {
      @Override
      public void changed(ObservableValue o, Object oldVal, Object newVal) {
        int id = viewTurtle.getId()-constantOne;
        ImageView thisView = turtleImageViews.get(id);

        double newX = viewTurtle.getX() + centerX;
        double newY = -(viewTurtle.getY()) + centerY;
        newX = keepInBoundsX(newX, myCanvasWidth);
        newY = keepInBoundsX(newY, myCanvasHeight);
        thisView.setX(newX);
        thisView.setY(newY);

        double pastX = keepInBoundsX(viewTurtle.getPastX() + centerX, myCanvasWidth);
        double pastY = keepInBoundsX( - viewTurtle.getPastY() + centerY, myCanvasHeight);

        double oldX = pastX + turtleCenterX;
        double oldY = pastY + turtleCenterY;
        double currentX = newX + turtleCenterX;
        double currentY = newY + turtleCenterY;

        if (isPenDown) {
          makeLine(oldX, oldY, currentX, currentY);
        }
        PropertiesView.makeCoord(viewTurtle);
        drawAllLines();
      }
    });
  }

  private void addActiveListener(Turtle viewTurtle) {
    viewTurtle.isActivatedProperty().addListener(new ChangeListener() {
      @Override
      public void changed(ObservableValue o, Object oldVal, Object newVal) {
        System.out.println(viewTurtle.getId());
          changeOpacity(viewTurtle);
          PropertiesView.changeActive(viewTurtle);
          if (!viewTurtle.isActivatedProperty().getValue()) {
              activeTurtles.remove(viewTurtle);
          }else
          {
              activeTurtles.add(viewTurtle);
          }
      }
    });
  }


  private void addAnglePropertyListener(Turtle viewTurtle) {
    viewTurtle.angleProperty().addListener(new ChangeListener() {
      @Override
      public void changed(ObservableValue o, Object oldVal, Object newVal) {
        turtleImageViews.get(viewTurtle.getId()-constantOne).setRotate(viewTurtle.getDegree());
      }
    });
  }

  private void addPenDownListener(Turtle viewTurtle) {
    viewTurtle.isPenDownProperty().addListener(new ChangeListener() {
      @Override
      public void changed(ObservableValue o, Object oldVal, Object newVal) {
        isPenDown = viewTurtle.isPenDown();
        PropertiesView.changePenDown(isPenDown);
      }
    });
  }

  private void addClearScreenListener(Turtle viewTurtle) {
    viewTurtle.clearScreenProperty().addListener(new ChangeListener<Boolean>() {
      @Override
      public void changed(ObservableValue<? extends Boolean> observable, Boolean oldValue,
          Boolean newValue) {
        if (viewTurtle.clearScreenProperty().get()) {
          removeLines();
        }
      }
    });
  }

  private void addShowingListener(Turtle viewTurtle) {
    viewTurtle.isShowingProperty().addListener(new ChangeListener<Boolean>() {
      @Override
      public void changed(ObservableValue<? extends Boolean> observable, Boolean oldValue,
          Boolean newValue) {
        ImageView thisView = turtleImageViews.get(viewTurtle.getId() - constantOne);
        if (viewTurtle.isShowingProperty().get()) { //make turtle visible
          thisView.setVisible(true);
        } else { //make turtle invisible
          thisView.setVisible(false);
        }
      }
    });
  }

  private void addSizeListener() {
    viewTurtles.addListener(new ListChangeListener<Turtle>() {
      @Override
      public void onChanged(Change<? extends Turtle> c) {
        c.next();
        List<Turtle> newTurtles = (List<Turtle>) c.getAddedSubList();
        for (Turtle changedTurtle : newTurtles) {
          setUpTurtle(changedTurtle);
        }
        for (Turtle changedTurtle : viewTurtles) {
          System.out.println(changedTurtle.getId());
        }
        PropertiesView.addRowListener(viewTurtles);
      }
    });
  }

  protected void setPenColor(Paint color) {
    penColor = color;
      PropertiesView.sendPenColor(color);
  }

  protected void setPenWidth(double width) {
    penWidth = width;
  }

  private void makeLine(double x1, double y1, double x2, double y2) {
    Line line = new Line(x1, y1, x2, y2);
    line.setStroke(penColor);
    line.setStrokeWidth(penWidth);
    linesDrawn.add(line);
  }

  private void drawAllLines() {
    for (Line line : linesDrawn) {
      if (!myPane.getChildren().contains(line)) {
        myPane.getChildren().add(line);
      }
    }
  }

  private void removeLines() {
    for (Line line : linesDrawn) {
      if (myPane.getChildren().contains(line)) {
        myPane.getChildren().remove(line);
      }
    }
    linesDrawn = new ArrayList<>();
  }

  protected Node getTurtleGrid() {
    return retGrid;
  }

  protected void setBackground(Color color) {
      PropertiesView.changeBackground(color);
    myPane.setBackground(new Background(new BackgroundFill(color, null, null)));
  }

  protected void updateTurtlesImage(String string, ObservableList<Turtle> updateTurtles) {
    for (Turtle viewTurtle : updateTurtles) {
      String imageName = string.split(commaString)[zeroIndex];
      turtleImageViews.get(viewTurtle.getId()-constantOne)
          .setImage(new Image(Main.myResources.getString(imageName)));
    }
  }

  public List<ImageView> getTurtleImage(){
    return turtleImageViews;
  }
}


//  protected void updateTurtlesImage(int index, ObservableList<Turtle> updateTurtles) {
//    for (Turtle viewTurtle : updateTurtles) {
//
//      turtleImageView.get(viewTurtle.getId()-1)
//          .setImage(new Image(Main.myResources.getString(imageName)));
//    }
//  }


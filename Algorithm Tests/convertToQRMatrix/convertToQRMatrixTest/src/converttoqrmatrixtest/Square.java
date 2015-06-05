/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package converttoqrmatrixtest;

//import java.util.ArrayList;
import java.util.List;
import static java.lang.Math.pow;
import static java.lang.Math.sqrt;

/**
 *
 * @author Tom
 */
public class Square
{

  private double length_ = 0;
  private double height_ = 0;
  private Coordinate center_;
  private List<Coordinate> edges_;// = new ArrayList<Coordinate>();

  public Square(List<Coordinate> edges)
  {
    edges_ = edges;

    center_ = new Coordinate(
            (edges.get(0).getXcrd() + edges.get(3).getXcrd()) / 2,
            (edges.get(0).getYcrd() + edges.get(3).getYcrd()) / 2);
    //(edges.get(1).getXcrd() - edges.get(3).getXcrd())/2
    //(edges.get(1).getXcrd() - edges.get(3).getXcrd())/2

    //length_ = edges.get(0).crdNorm(edges.get(1));
    //height_ = edges.get(2).crdNorm(edges.get(0));
    
    Coordinate edge_A = new Coordinate(edges.get(0).getXcrd() - 0.5, edges.get(0).getYcrd() - 0.5);
    Coordinate edge_B = new Coordinate(edges.get(1).getXcrd() + 0.5, edges.get(1).getYcrd() - 0.5);
    Coordinate edge_C = new Coordinate(edges.get(3).getXcrd() + 0.5, edges.get(3).getYcrd() + 0.5);
    //Coordinate edge_D = new Coordinate(edges.get(2).getXcrd() - 0.5, edges.get(2).getYcrd() + 0.5);
  
    length_ = edge_A.crdNorm(edge_B);
    height_ = edge_B.crdNorm(edge_C);
  }
  
  public double getAreaSize()
  {
    return length_*height_;
  }

  public Coordinate getEdge(int select)
  {
    return edges_.get(select);
  }
  
  public Coordinate getCenter()
  {
    return center_;
  }
  
  public double getLength()
  {
    return length_;
  }
  
  public double getHeight()
  {
    return height_;
  }
}

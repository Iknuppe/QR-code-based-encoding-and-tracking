
package com.example.tom.qrtrace;

import java.util.List;

public class Square
{

  private double length_ = 0;
  private double height_ = 0;
  private Coordinate center_;
  private List<Coordinate> edges_;

  public Square(List<Coordinate> edges)
  {
    edges_ = edges;

    center_ = new Coordinate(
            (edges.get(0).getXcrd() + edges.get(3).getXcrd()) / 2,
            (edges.get(0).getYcrd() + edges.get(3).getYcrd()) / 2);
    
    Coordinate edge_A = new Coordinate(edges.get(0).getXcrd() - 0.5, edges.get(0).getYcrd() - 0.5);
    Coordinate edge_B = new Coordinate(edges.get(1).getXcrd() + 0.5, edges.get(1).getYcrd() - 0.5);
    Coordinate edge_C = new Coordinate(edges.get(3).getXcrd() + 0.5, edges.get(3).getYcrd() + 0.5);
  
    length_ = edge_A.crdNorm(edge_B);
    height_ = edge_B.crdNorm(edge_C);
  }
  
  public double getAreaSize()
  {
    return ((edges_.get(0).crdNorm(edges_.get(3))) * (edges_.get(1).crdNorm(edges_.get(2)))) / 2;
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

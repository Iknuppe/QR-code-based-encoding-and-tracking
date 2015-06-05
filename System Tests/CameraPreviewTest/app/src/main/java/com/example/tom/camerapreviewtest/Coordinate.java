/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.example.tom.camerapreviewtest;

import static java.lang.Math.pow;
import static java.lang.Math.sqrt;

public class Coordinate
{//virtual mit vector??
  private double x_crd_ = 0;
  private double y_crd_ = 0;
  
  private static final double CRD_TOLERANCE = 0.5;//0.5

  public Coordinate(double x_crd, double y_crd)
  {
    x_crd_ = x_crd;
    y_crd_ = y_crd;
  }

  public Coordinate crdAdd(Coordinate crd)
  {
    return new Coordinate(x_crd_ + crd.getXcrd(), y_crd_ + crd.getYcrd());
  }

  public Coordinate crdSubtract(Coordinate crd)
  {
    return new Coordinate(x_crd_ - crd.getXcrd(), y_crd_ - crd.getYcrd());
  }

  public Coordinate crdMultiply(double value)
  {
    return new Coordinate((x_crd_*value), (y_crd_*value));
  }
  
  public Coordinate crdDivide(double value)
  {
    return new Coordinate((x_crd_/value), (y_crd_/value));
  }
  
  public double crdNorm(Coordinate crd)
  {
    return sqrt(pow(this.crdSubtract(crd).getXcrd(), 2) + pow(this.crdSubtract(crd).getYcrd(), 2));
  }
  
  /* rotate -90° (veränderlich machen? */
  public Coordinate crdRotateNeg90()
  {
    return new Coordinate(y_crd_, -x_crd_);
  }
  
  /* rotate +90° (veränderlich machen? */
  public Coordinate crdRotatePos90()
  {
    return new Coordinate(-y_crd_, x_crd_);
  }
  
  public int crdCheckEqual(Coordinate crd)
  {
    if(((x_crd_ - CRD_TOLERANCE) < (crd.getXcrd() + CRD_TOLERANCE)) &&
       ((x_crd_ + CRD_TOLERANCE) > (crd.getXcrd() - CRD_TOLERANCE)) &&
       ((y_crd_ - CRD_TOLERANCE) < (crd.getYcrd() + CRD_TOLERANCE)) &&
       ((y_crd_ + CRD_TOLERANCE) > (crd.getYcrd() - CRD_TOLERANCE)))
    {
      return 1;
    }
    else
    {
      return 0;
    }
  }


  public double crdCheckNormal(Coordinate crd)
  {
    return ((this.getXcrd() * crd.getXcrd()) + (this.getYcrd() * crd.getYcrd()));
  }

  public double getXcrd()
  {
    return x_crd_;
  }

  public double getYcrd()
  {
    return y_crd_;
  }
}

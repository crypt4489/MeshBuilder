import java.lang.Math;

class Vector
{
   public double x, y, z, w;

   public Vector()
   {
      this.x = 0;
      this.y = 0;
      this.z = 0;
      this.w = 0;
   }
   
   public Vector(double in_x, double in_y, double in_z, double in_w)
   {
      this.x = in_x;
      this.y = in_y;
      this.z = in_z;
      this.w = in_w;
   }
   
   public void DumpVector()
   {
      System.out.printf("%f %f %f %f\n", x, y, z, w);
   }
   
   public Vector CrossProduct(Vector b)
   {
      double x = (this.y*b.z - this.z * b.y);
      double  y = -(this.x * b.z - this.z * b.x);
      double z = (this.x * b.y - this.y * b.x);
   
      return new Vector(x, y, z, 1.0);
   }
   
   @Override
   public boolean equals(Object o) {
      if (o == this) {
         return true;
      }
      
      if (!(o instanceof Vector)) 
      {
         return false;
      }
      
      Vector v1 = (Vector)o;
      
      return ((this.x == v1.x) && 
      (this.y == v1.y) && 
      (this.z == v1.z) && 
      (this.w == v1.w));
   }
   
   public Vector subtract(Vector v1)
   {
      return new Vector(this.x - v1.x, this.y - v1.y, this.z - v1.z, 1.0);
   }
   
   public void normalize()
   {
      double sum = 0.0;
      double mag = 0.0;
      sum += (this.x * this.x);
      sum += (this.y * this.y);
      sum += (this.z * this.z);
      
      mag = Math.sqrt(sum);
      
      this.x = this.x/mag;
      this.y = this.y/mag;
      this.z = this.z/mag;
   }
   
   
}
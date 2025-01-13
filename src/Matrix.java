import java.lang.Math;
import org.lwjgl.assimp.AIMatrix4x4;

class Matrix
{
   public Vector row1, row2, row3, row4;

   public Matrix()
   {

   }

   public Matrix(Vector row1, Vector row2, Vector row3, Vector row4)
   {
     this.row1 = row1;
     this.row2 = row2;
     this.row3 = row3;
     this.row4 = row4;
   }

   public static Matrix Identity()
   {
      return new Matrix(new Vector(1.0f, 0.0f, 0.0f, 0.0f),
                        new Vector(0.0f, 1.0f, 0.0f, 0.0f),
                        new Vector(0.0f, 0.0f, 1.0f, 0.0f),
                        new Vector(0.0f, 0.0f, 0.0f, 1.0f));
   }


   public static Matrix Zero()
   {
      return new Matrix(new Vector(0.0f, 0.0f, 0.0f, 0.0f),
                        new Vector(0.0f, 0.0f, 0.0f, 0.0f),
                        new Vector(0.0f, 0.0f, 0.0f, 0.0f),
                        new Vector(0.0f, 0.0f, 0.0f, 0.0f));
   }

   public static Matrix CreateFromAIMatrix(AIMatrix4x4 input)
   {
      return new Matrix(new Vector(input.a1(), input.b1(), input.c1(), input.d1()),
      new Vector(input.a2(), input.b2(), input.c2(), input.d2()),
      new Vector(input.a3(), input.b3(), input.c3(), input.d3()),
      new Vector(input.a4(), input.b4(), input.c4(), input.d4()));
   }

   public void CreateRotationMatrix(Vector axis, double angle)
   {
      double x = axis.x;
      double y = axis.y;
      double z = axis.z;

      double s = Math.sin(angle);
      double c = Math.cos(angle);

      Vector row1 = new Vector((c + (x * x) * (1.0 - c)), (x * y *(1.0 - c) - (z * s)), (x * y *(1.0 - c) - (z * s)), 0.0);
      Vector row2 = new Vector((y * x * (1.0-c) + (z * s)),  (c + (y * y) *(1.0-c)), (y * x * (1.0-c) - (x * s)), 0.0);
      Vector row3 = new Vector((z * x * (1.0 - c) - (y * s)),  (x * y * (1.0 - c) + (x * s)), (c + (z * z) * (1.0 - c)), 0.0);
      Vector row4 = new Vector(0.0, 0.0, 0.0, 1.0);

      this.row1 = row1;
      this.row2 = row2;
      this.row3 = row3;
      this.row4 = row4;
    }

    public Vector MatrixVectorMultiply(Vector v)
    {
        double x = this.row1.x * v.x + this.row1.y * v.y
        + this.row1.z * v.z + this.row1.w * v.w;
        double y = this.row2.x * v.x + this.row2.y * v.y
        + this.row2.z * v.z + this.row2.w * v.w;
        double z = this.row3.x * v.x + this.row3.y * v.y
        + this.row3.z * v.z + this.row3.w * v.w;
        double w = this.row4.x * v.x + this.row4.y * v.y
        + this.row4.z * v.z + this.row4.w * v.w;


        return new Vector(x, y, z, w);
    }

    public void DumpMatrix()
    {
         this.row1.DumpVector();
         this.row2.DumpVector();
         this.row3.DumpVector();
         this.row4.DumpVector();
    }

    @Override
    public boolean equals(Object o) {
       if (o == this) {
          return true;
       }
       
       if (!(o instanceof Matrix)) 
       {
          return false;
       }
       
       Matrix m = (Matrix)o;
       
       return ((this.row1.equals(m.row1)) && 
       (this.row2.equals(m.row2)) && 
       (this.row3.equals(m.row3)) && 
       (this.row4.equals(m.row4)));
    }









}
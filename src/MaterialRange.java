public class MaterialRange
{
   public DMaterial mat;
   public int start, end;

   public MaterialRange(int _s, int _e, DMaterial _m)
   {
      this.start = _s;
      this.end = _e;
      this.mat = _m;
   }

   public void DumpRange()
   {
      System.out.printf("start %d \nend : %d \n", this.start, this.end);  
   }
}
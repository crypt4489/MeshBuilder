import java.util.Comparator;

class VectorCompare implements Comparator<Vector>
{
   public int compare(Vector a, Vector b)
   {
      if (a.x < b.x)
      {
         return -1;
      } else if (a.x == b.x) {
         if (a.y < b.y) 
         {
            return -1;
         } 
         else if (a.y == b.y)
         {
            if (a.z < b.z)
            {
               return -1;
            } else if (a.z == b.z) {
               return 0;
            }
         }
      }
      
      return 1;
   }
}
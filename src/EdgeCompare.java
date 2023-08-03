import java.util.Comparator;

class EdgeCompare implements Comparator<Edge>
{
   public int compare(Edge e1, Edge e2)
   {
      if (e1.v1 < e2.v1)
      {
         return -1;
      } else if (e1.v1 == e2.v1) {
        if (e1.v2 < e2.v2)
        {
            return -1;
        } else if (e1.v2 == e2.v2) {
            return 0;
        } else {
            return 1;
        }
      }
      else 
      {
         return 1;
      }
   }
}
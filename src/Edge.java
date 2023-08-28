class Edge
{
   public int v1, v2;

   public Edge(int index1, int index2)
   {
      if (index1 < index2)
      {
          this.v1 = index1;
          this.v2 = index2;
      }
      else
      {
          this.v1 = index2;
          this.v2 = index1;
      }
   }
   
   public void PrintEdge()
   {
      System.out.println(this.v1 + " " + this.v2);
   }
   
   @Override
   public boolean equals(Object o) {
      if (o == this) {
         return true;
      }
      
      if (!(o instanceof Edge)) 
      {
         return false;
      }
      
      Edge e1 = (Edge)o;
      
      return ((Integer.compare(this.v1, e1.v1) == 0 && Integer.compare(this.v2, e1.v2) == 0) ||
               (Integer.compare(this.v2, e1.v1) == 0 && Integer.compare(this.v1, e1.v2) == 0)) ;
   }

   
   
}
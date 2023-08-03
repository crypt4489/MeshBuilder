class Neighbors
{
   public int n1, n2;
   
   public Neighbors()
   {
      this.n1 = -1;
      this.n2 = -1;
   }
   
   public void AddNeighbor(int n)
   {
      if (this.n1 == -1)
      {
         n1 = n;
      } 
      else if (this.n2 == -1)
      {
         n2 = n;
      }
      
      
   }
   
   public int GetOther(int me)
   {
      if (n1 == me)
      {
         return n2;
      } 
      else if (n2 == me)
      {
         return n1;
      }
      
      return -1;
   }
   
   public void PrintNeighbor()
   {
      System.out.println(this.n1 + " " + this.n2);
   }
}
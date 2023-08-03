class Face
{
   //public int i1, i2, i3;
   public int[] indices = new int[3];
   
   public Face()
   {
      this.indices[0] = -1;
      this.indices[1] = -1;
      this.indices[2] = -1;
   }
      
   public Face(int index1, int index2, int index3)
   {
      this.indices[0] = index1;
      this.indices[1] = index2;
      this.indices[2] = index3;
   }
   
   public int GetOppositeIndex(Edge e)
   {
      for (int i = 0; i<3; i++)
      {
         int Index = this.indices[i];
         
         if (Index != e.v1 && Index != e.v2)
         {
            return Index;
         } 
      }
      
      System.out.println("Something is wrong with opposite face");
      System.exit(0);
      
      return 0;
   }
   
   public void DumpFace()
   {
      System.out.printf("%d %d %d\n", this.indices[0], this.indices[1],this.indices[2]);
   }
   
   
}
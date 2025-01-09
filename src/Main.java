import java.util.Map;
import java.util.TreeMap;
import java.util.SortedMap;
import java.util.ArrayList;
import java.io.BufferedWriter;
import java.io.FileWriter;
import java.io.FileNotFoundException;
import java.io.IOException;

import org.apache.commons.lang3.SystemUtils;
import org.apache.commons.lang3.ObjectUtils.Null;




class CreateAdjacencyTris
{

   public SortedMap<Vector, Integer> m_posMap = new TreeMap<Vector, Integer>(new VectorCompare());

   public SortedMap<Edge, Neighbors> m_indexMap = new TreeMap<Edge, Neighbors>(new EdgeCompare());

   public ArrayList<Face> unique_faces = new ArrayList<Face>();

   public ArrayList<Integer> indices = new ArrayList<Integer>();

   public CreateAdjacencyTris()
   {

   }

   public void CreateTris(Mesh box, String filename)
   {
        int totalFaces = (int)(box.indices.size() / 3.0);

        int globalOffset = 0; //index for vertex array

        for(int i = 0; i<totalFaces; i++)
        {

            Face unique = new Face();

            for(int j = 0; j<3; j++)
            {
               int index = box.indices.get(globalOffset + j);

               Vector v = box.vertices.get(index);

               if (!(m_posMap.containsKey(v)))
               {
                  m_posMap.put(v, index);
               } else {
                  index = m_posMap.get(v);
               }

               unique.indices[j] = index;
            }

            globalOffset += 3;

            unique_faces.add(unique);

            Edge e1 = new Edge(unique.indices[0], unique.indices[1]);

            Edge e2 = new Edge(unique.indices[1], unique.indices[2]);

            Edge e3 = new Edge(unique.indices[2], unique.indices[0]);

            if (m_indexMap.containsKey(e1))
            {
               Neighbors n = m_indexMap.get(e1);
               n.AddNeighbor(i);
            } else {
               Neighbors n = new Neighbors();
               n.AddNeighbor(i);
               m_indexMap.put(e1, n);
            }

            if (m_indexMap.containsKey(e2))
            {
               Neighbors n = m_indexMap.get(e2);
               n.AddNeighbor(i);
            } else {
               Neighbors n = new Neighbors();
               n.AddNeighbor(i);
               m_indexMap.put(e2, n);
            }


            if (m_indexMap.containsKey(e3))
            {
               Neighbors n = m_indexMap.get(e3);
               n.AddNeighbor(i);
            } else {
               Neighbors n = new Neighbors();
               n.AddNeighbor(i);
               m_indexMap.put(e3, n);
            }


        }

        for (int i = 0; i<totalFaces; i++)
        {
            Face face = unique_faces.get(i);

            for (int j = 0; j<3; j++)
            {
               Edge e = new Edge(face.indices[j], face.indices[(j+1) % 3]);

               if (!(m_indexMap.containsKey(e)))
               {
                  System.out.println("Something is WRONG!");
                  System.exit(0);
               }

               Neighbors n = m_indexMap.get(e);

               int OtherTri = n.GetOther(i);

               if (OtherTri == -1)
               {
                  System.out.println("Something is DOUBLY WRONG!");
                  System.exit(0);
               }

               Face otherFace = unique_faces.get(OtherTri);

               int OppositeIndex = otherFace.GetOppositeIndex(e);

               indices.add(face.indices[j]);

               indices.add(OppositeIndex);
            }
        }

        for (int i = 0; i<indices.size(); i++)
        {
           box.adjArrayList.add(box.vertices.get(indices.get(i)));
        }

       /*
        try {
            BufferedWriter writer = new BufferedWriter(new FileWriter(filename, true));
            String send = null;

            int size = indices.size();

            send = String.format("\n%d\n", size);

            writer.write(send);

            if (size % 3 != 0)
            {
               System.out.println("Let's go!");
               System.exit(0);
            }


            for (int i = 0; i<size; i+=6)
            {
               send = String.format("%d, %d, %d, %d, %d, %d,\n", indices.get(i), indices.get(i+1), indices.get(i+2), indices.get(i+3), indices.get(i+4), indices.get(i+5));
               writer.write(send);
            }

            writer.close();

        } catch (FileNotFoundException e) {
			e.printStackTrace();
		  } catch (IOException e) {
         e.printStackTrace();
        }
        */
   }
}

class FindVertsOnSamePlane
{

   TreeMap<Vector, ArrayList<Face>> m_indexMap;

   public void CreateMapping(Mesh obj)
   {
      m_indexMap = new TreeMap<Vector, ArrayList<Face>>(new VectorCompare());
      ArrayList<Face> faces = new ArrayList<Face>();
      int totalFaces = (int)(obj.indices.size() / 3.0);


      for (int i = 0,  j = 0; i<totalFaces; j+=3, i++)
      {
         Face new_face = new Face();
         new_face.indices[0] = obj.indices.get(j);
         new_face.indices[1] = obj.indices.get(j+1);
         new_face.indices[2] = obj.indices.get(j+2);
         faces.add(new_face);
      }

      for (Face face : faces)
      {
         Vector v1, v2, v3, u, t, normal;

         v1 = obj.vertices.get(face.indices[0]);
         v2 = obj.vertices.get(face.indices[1]);
         v3 = obj.vertices.get(face.indices[2]);

         u = v2.subtract(v1);
         t = v3.subtract(v1);

         normal = u.CrossProduct(t);

        // normal.normalize();

         //normal.DumpVector();

          if (!(m_indexMap.containsKey(normal)))
          {
            ArrayList<Face> normal_faces = new ArrayList<Face>();
            normal_faces.add(face);
            m_indexMap.put(normal, normal_faces);
          } else {
             ArrayList<Face> normal_face = m_indexMap.get(normal);
             normal_face.add(face);
          }



      }

      /*System.out.println(m_indexMap.size());

      for (Map.Entry<Vector, ArrayList<Face>>
                 entry : m_indexMap.entrySet())
      {
         Vector n = entry.getKey();
         n.DumpVector();
         ArrayList<Face> normal_faces = entry.getValue();
         System.out.println(normal_faces.size());
         for(Face face : normal_faces)
         {
            face.DumpFace();
         }

         System.out.println("------------------------");
      } */
   }

   public void DumpIndexMapToFile(String filename)
   {
       try {
            BufferedWriter writer = new BufferedWriter(new FileWriter(filename, true));
            String send = null;

            send = String.format("\nORDERED BY FACES\n");

            writer.write(send);


            for (Map.Entry<Vector, ArrayList<Face>>
                 entry : m_indexMap.entrySet())
            {
               Vector n = entry.getKey();
               n.DumpVector();
               ArrayList<Face> normal_faces = entry.getValue();
               System.out.println(normal_faces.size());
               for(Face face : normal_faces)
               {
                  //face.DumpFace();
                  send = String.format("%d %d %d\n", face.indices[0], face.indices[1], face.indices[2]);
                  writer.write(send);
               }

               System.out.println("------------------------");
             }


            writer.close();

        } catch (FileNotFoundException e) {
			e.printStackTrace();
		  } catch (IOException e) {
         e.printStackTrace();
        }


   }
}

class Main
{
    public static void main(String[] args)   {



      Matrix fbxrot = new Matrix();

      double angle = +90.0f;
//
      double rads = Math.toRadians(angle);

      Vector axis = new Vector(1.0f, 0.0f, 0.0f, 1.0f);

      fbxrot.CreateRotationMatrix(axis, rads);

      Matrix objrot = new Matrix();

      angle = 180.0f;
//
      rads = Math.toRadians(angle);

      objrot.CreateRotationMatrix(axis, rads);

      MatrixTransform objTrans = new MatrixTransform(objrot, true);

      MatrixTransform fbxTrans = new MatrixTransform(fbxrot, true);

    //  MatrixTransform [] transs = {objTrans, fbxTrans};



    //  String [] exts = { ".obj", ".fbx" };

      String cwd = System.getProperty("user.dir");

      String fileSlash = (SystemUtils.IS_OS_WINDOWS == true ? "\\" : "/");

      String jsonFile = cwd + fileSlash + "input.json";

      System.out.println(jsonFile);

      MeshBuilderProgram program = new MeshBuilderProgram(cwd, jsonFile);

      program.RunProgram();

    }
}
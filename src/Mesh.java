import java.util.ArrayList;
import java.util.Map;
import java.util.Scanner;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.BufferedWriter;
import java.io.FileWriter;
import java.io.IOException;
import java.io.FileOutputStream;
import java.io.DataOutputStream;



class Mesh {

   public ArrayList<Integer> indices = new ArrayList<Integer>();
   public ArrayList<Vector> vertices = new ArrayList<Vector>();
   public ArrayList<Vector> texCoords = new ArrayList<Vector>();
   public ArrayList<Vector> normals = new ArrayList<Vector>();
   public ArrayList<VectorInt> bones; //= new ArrayList<VectorInt>();
   public ArrayList<Vector> weights;// = new ArrayList<Vector>();

   public ArrayList<Vector> vertices_i = new ArrayList<Vector>();
   public ArrayList<Vector> texCoords_i = new ArrayList<Vector>();
   public ArrayList<Vector> normals_i = new ArrayList<Vector>();
   public ArrayList<VectorInt> bones_i;// = new ArrayList<VectorInt>();
   public ArrayList<Vector> weights_i; // = new ArrayList<Vector>();

   public ArrayList<Vector> adjArrayList = new ArrayList<Vector>();

   public ArrayList<MaterialRange> materialList = new ArrayList<MaterialRange>();

   public ArrayList<Joint> joints; // = new ArrayList<Joint>();

   public ArrayList<AnimationData> animData;

   public String meshName;

   private boolean createdAnim = false;

   public Mesh() {
      this.meshName= null;
   }

   public Mesh(String _name) {
      this.meshName = _name;
   }

   public AnimationData findAnimDataByName(String name)
   {

      for (AnimationData data : this.animData)
      {
         if (data.CompareAnimName(name))
            return data;
      }

      return null;
   }

   public void CreateAnimationStructures()
   {
      if (!createdAnim)
      {
         this.bones_i = new ArrayList<VectorInt>();
         this.weights_i = new ArrayList<Vector>();

         this.bones = new ArrayList<VectorInt>();
         this.weights = new ArrayList<Vector>();

         this.joints = new ArrayList<Joint>();
         this.animData = new ArrayList<AnimationData>();
         createdAnim = true;
      }
   }


   public void byIndices() {

         for (int i = 0; i < indices.size(); i++) {
            int index = indices.get(i);
            if (vertices.size() != 0) {
               vertices_i.add(vertices.get(index));
            }
            if (texCoords.size() != 0) {
               texCoords_i.add(texCoords.get(index));
            }
            if (normals.size() != 0) {
               normals_i.add(normals.get(index));
            }
            if (bones != null && bones.size() != 0)
            {
               bones_i.add(bones.get(index));
            }

            if (weights != null && weights.size() != 0)
            {
               weights_i.add(weights.get(index));
            }
           // System.out.print((index) + " ");
         }

         //System.out.println();

    //  System.out.println(indices.size());
     // System.out.println(normals_i.size());
    //  System.out.println(texCoords_i.size());
    //  System.out.println(vertices_i.size());
      for (MaterialRange range : materialList)
      {
        // range.DumpRange();
      }
   }

   public void loadData(String filename) {
      try {

         Scanner scanner = new Scanner(new File(filename));
         int totalVerts = scanner.nextInt();
         scanner.nextLine();
         int i;

         for (i = 0; i < totalVerts; i++) {
            double x = scanner.nextDouble();
            double y = scanner.nextDouble();
            double z = scanner.nextDouble();
            double w = scanner.nextDouble();
            Vector v = new Vector(x, y, z, w);
            vertices.add(v);
            scanner.nextLine();
         }

         int totalIndices = scanner.nextInt();

         for (i = 0; i < totalIndices / 3.0; i++) {
            scanner.nextLine();
            int index = scanner.nextInt();
            int index2 = scanner.nextInt();
            int index3 = scanner.nextInt();
            indices.add(index);
            indices.add(index2);
            indices.add(index3);
         }

         scanner.nextInt();

         for (i = 0; i < totalVerts; i++) {
            double x = scanner.nextDouble();
            double y = scanner.nextDouble();
            double z = scanner.nextDouble();
            double w = scanner.nextDouble();
            Vector v = new Vector(x, y, z, w);
            texCoords.add(v);
            scanner.nextLine();
         }

         scanner.close();
      } catch (FileNotFoundException e) {
         e.printStackTrace();
      }
   }

   private final int badbeef = 0xabadbeef;
   private final int fin = 0xFFFF4114;


   private boolean[] GetLastElements(short code)
   {
      boolean [] ret = new boolean[5];
      // just verts
      ret[0] = code == 0x01;
      // last tex
      ret[1] = ((code & 0x02) == 0x02) && code <= 0x03;
      // last normals
      ret[2] = ((code & 0x04) == 0x04) && code <= 0x07;
      // last indices
      ret[3] = ((code & 0x08) == 0x08) && code <= 0x0f;
      // last animation
      ret[4] = ((code & 0x20) == 0x20) && ((code <= 0x2f) || (code <= 0x3f));

      return ret;
   }

   private short GenerateMeshCode()
   {
      short code = 0;
      if (vertices_i.size() != 0)
      {
           code |= 0x01;
      }


      if (normals_i.size() != 0)
      {
         code |= 0x04;
      }

      if (texCoords_i.size() != 0)
      {
         code |= 0x02;
      }


      if (indices.size() != 0)
      {
         code |= 0x08;
      }

      if (materialList.size() != 0)
      {
         code |= 0x10;
      }
      if (bones != null && weights != null)
      {
         if (bones.size() != 0 && weights.size() != 0)
         {
            code |= 0x20;
         }
      }

      return code;
   }

   public void WriteMeshToBinFile(String filename) {
      try {
         FileOutputStream outputStream = new FileOutputStream(filename);
         DataOutputStream dos = new DataOutputStream(outputStream);
         dos.writeShort(0xDF01); //
         short code = GenerateMeshCode();
         boolean [] ret = GetLastElements(code);
         dos.writeShort(code);
         dos.writeInt(this.indices.size());
         dos.writeInt(badbeef);


         int vertsNormTexSize = this.indices.size();
         int endingIndex = this.indices.size()-1;
         int chunks = this.materialList.size();
         int index = 0;

         if (chunks == 0)
            chunks = 1;

         for (int j = 0; j<chunks; j++)
         {
            if (materialList.size() > 0)
            {
               MaterialRange rang = materialList.get(j);
               dos.writeByte(0x08);
               dos.writeInt(rang.start);
               dos.writeInt(rang.end);
               dos.writeByte(rang.mat.fileName.length());
               for (byte b : rang.mat.fileName.getBytes())
               {
                  dos.writeByte(b);
               }

               dos.writeInt(badbeef);
               vertsNormTexSize = (rang.end-rang.start)+1;
               index = rang.start;
               endingIndex = rang.end;
            }

            dos.writeByte(0x02);
            dos.writeInt(vertsNormTexSize);

            for (int i = index; i <= endingIndex; i++) {
               Vector temp = vertices_i.get(i);
               dos.writeFloat((float) temp.x);
               dos.writeFloat((float) temp.y);
               dos.writeFloat((float) temp.z);
               //dos.writeFloat((float) temp.w);
               // temp.DumpVector();
            }

            if (j+1 != chunks && !ret[0])
               dos.writeInt(badbeef);

            if (normals.size() != 0) {

               dos.writeByte(0x05);
               dos.writeInt(vertsNormTexSize);

               for (int i = index; i <= endingIndex; i++) {
                  Vector temp = normals_i.get(i);
                  dos.writeFloat((float) temp.x);
                  dos.writeFloat((float) temp.y);
                  dos.writeFloat((float) temp.z);
                  //dos.writeFloat((float) temp.w);
               }


               if (j+1 != chunks && !ret[2])
                  dos.writeInt(badbeef);

            }

            if (texCoords.size() != 0) {
               dos.writeByte(0x04);
               dos.writeInt(vertsNormTexSize);

               for (int i = index; i <= endingIndex; i++) {
                  Vector temp = texCoords_i.get(i);
                  dos.writeFloat((float) temp.x);
                  dos.writeFloat((float) temp.y);
                 // dos.writeFloat((float) temp.z);
                  //dos.writeFloat((float) temp.w);
               }


                  //System.out.println("here");
               if (j+1 != chunks && !ret[1])
                  dos.writeInt(badbeef);

            }

            if (bones != null && bones.size() != 0)
            {
               dos.writeByte(0x06);
               dos.writeInt(vertsNormTexSize);
               for (int i = index; i <= endingIndex; i++) {
                  VectorInt temp = bones_i.get(i);
                  dos.writeByte(temp.x);
                  dos.writeByte(temp.y);
                  dos.writeByte(temp.z);
                  dos.writeByte(temp.w);
               }

               dos.writeInt(badbeef);

            }

            if (weights != null &&weights.size() != 0)
            {
               dos.writeByte(0x07);
               dos.writeInt(vertsNormTexSize);
               for (int i = index; i <= endingIndex; i++) {
                  Vector temp = weights_i.get(i);
                  dos.writeFloat((float) temp.x);
                  dos.writeFloat((float) temp.y);
                  dos.writeFloat((float) temp.z);
                  dos.writeFloat((float) temp.w);
               }

               dos.writeInt(badbeef);

            }

            if (adjArrayList.size() != 0) {
               dos.writeByte(0x07);
               dos.writeInt(adjArrayList.size());

               for (int i = 0; i < adjArrayList.size(); i++) {
                  Vector temp = adjArrayList.get(i);
                  dos.writeFloat((float) temp.x);
                  dos.writeFloat((float) temp.y);
                  dos.writeFloat((float) temp.z);
                  dos.writeFloat((float) temp.w);
               }

               dos.writeInt(badbeef);
            }
         }

         if (this.createdAnim)
         {
            //write animation data
            System.out.println("HERE!");

            //joints
            dos.writeByte(0x09);
            dos.writeInt(this.joints.size());
            for (Joint joint : this.joints)
            {
               dos.writeByte(joint.id);
               System.out.println(joint.id);
               dos.writeByte(joint.name.length());
               WriteCharsAsBytes(dos, joint.name);
               WriteMatrixToStream(dos, joint.offset);
            }

            //animation data

            for (AnimationData data : this.animData)
            {
               dos.writeInt(badbeef);
               dos.writeByte(0x0A);
               dos.writeInt(data.name.length());
               WriteCharsAsBytes(dos, data.name);
               dos.writeFloat((float)data.m_Duration);
               dos.writeFloat((float)data.m_TicksPerSecond);
               WriteAnimNodesToStream(dos, data.m_RootNode);
               // SRTs
               dos.writeInt(badbeef);
               dos.writeByte(0x0B);
               WriteSRTToStream(dos, data);
            }
         }

         dos.writeInt(fin);

         dos.flush();

         dos.close();

         outputStream.close();

         System.out.println("The Mesh " + this.meshName + " has been written to file : " + filename);

      } catch (FileNotFoundException e) {
         e.printStackTrace();
      } catch (IOException e) {
         e.printStackTrace();
      }
   }

   private void WriteCharsAsBytes(DataOutputStream dos, String text)
   {
      try
      {
         int len = text.length();
         for (int i = 0; i<len; i++)
         {
            dos.writeByte(text.charAt(i));
         }
      } catch (IOException e) {
         e.printStackTrace();
      }
   }

   private void WriteSRTToStream(DataOutputStream dos, AnimationData data)
   {
      try
      {
         dos.writeInt(data.positions.size());
         for (Map.Entry<Integer,ArrayList<AnimationData.KeyPosition>> entry : data.positions.entrySet())
         {
            dos.writeInt(entry.getKey());
            ArrayList<AnimationData.KeyPosition> poses = entry.getValue();
            dos.writeInt(poses.size());
            for (AnimationData.KeyPosition pos : poses)
            {
               dos.writeFloat(pos.timeStamp);
               dos.writeFloat((float)pos.pos.x);
               dos.writeFloat((float)pos.pos.y);
               dos.writeFloat((float)pos.pos.z);
               dos.writeFloat((float)pos.pos.w);
            }
         }

         dos.writeInt(data.rotations.size());
         for (Map.Entry<Integer,ArrayList<AnimationData.KeyRotation>> entry : data.rotations.entrySet())
         {
            dos.writeInt(entry.getKey());
            ArrayList<AnimationData.KeyRotation> rots = entry.getValue();
            dos.writeInt(rots.size());
            for (AnimationData.KeyRotation rot : rots)
            {
               dos.writeFloat(rot.timeStamp);
               dos.writeFloat((float)rot.quat.x);
               dos.writeFloat((float)rot.quat.y);
               dos.writeFloat((float)rot.quat.z);
               dos.writeFloat((float)rot.quat.w);
            }
         }

         dos.writeInt(data.scalings.size());
         for (Map.Entry<Integer,ArrayList<AnimationData.KeyScaling>> entry : data.scalings.entrySet())
         {
            dos.writeInt(entry.getKey());
            ArrayList<AnimationData.KeyScaling> scales = entry.getValue();
            dos.writeInt(scales.size());
            for (AnimationData.KeyScaling scale : scales)
            {
               dos.writeFloat(scale.timeStamp);
               dos.writeFloat((float)scale.scales.x);
               dos.writeFloat((float)scale.scales.y);
               dos.writeFloat((float)scale.scales.z);
               dos.writeFloat((float)scale.scales.w);
            }
         }
      } catch (IOException e) {
         e.printStackTrace();
      }
   }

   private void WriteAnimNodesToStream(DataOutputStream dos, AnimationData.AssimpNodeData node)
   {
      try
      {
         dos.writeInt(node.name.length());
         WriteCharsAsBytes(dos, node.name);
         dos.writeByte(node.childrenCount);
         System.out.println(node.name);
         node.transformation.DumpMatrix();
         WriteMatrixToStream(dos, node.transformation);
         for (AnimationData.AssimpNodeData child : node.children)
         {
            WriteAnimNodesToStream(dos, child);
         }
      } catch (IOException e) {
         e.printStackTrace();
      }
   }

   private void WriteMatrixToStream(DataOutputStream dos, Matrix m)
   {
      try
      {
         dos.writeFloat((float)m.row1.x);
         dos.writeFloat((float)m.row1.y);
         dos.writeFloat((float)m.row1.z);
         dos.writeFloat((float)m.row1.w);

         dos.writeFloat((float)m.row2.x);
         dos.writeFloat((float)m.row2.y);
         dos.writeFloat((float)m.row2.z);
         dos.writeFloat((float)m.row2.w);

         dos.writeFloat((float)m.row3.x);
         dos.writeFloat((float)m.row3.y);
         dos.writeFloat((float)m.row3.z);
         dos.writeFloat((float)m.row3.w);

         dos.writeFloat((float)m.row4.x);
         dos.writeFloat((float)m.row4.y);
         dos.writeFloat((float)m.row4.z);
         dos.writeFloat((float)m.row4.w);

      } catch (IOException e) {
         e.printStackTrace();
      }

   }

   public void WriteMeshToTextFile(String filename) {
      try {
         BufferedWriter writer = new BufferedWriter(new FileWriter(filename, false));
         String send = null;

         int vertsNormTexSize = this.vertices.size();

         send = String.format("%d\n\n -------------------------VERTICES------------------------------- \n\n",
               vertsNormTexSize);
         writer.write(send);

         for (int i = 0; i < vertsNormTexSize; i++) {
            Vector temp = vertices.get(i);
            send = String.format("{%f, %f, %f, %f}, \n", temp.x, temp.y, temp.z, temp.w);
            writer.write(send);
         }

         if (normals.size() != 0) {
            send = String.format("%d\n\n--------------------- NORMALS ------------------------------\n\n",
                  vertsNormTexSize);
            writer.write(send);

            for (int i = 0; i < vertsNormTexSize; i++) {
               Vector temp = normals.get(i);
               send = String.format("{%f, %f, %f, %f}, \n", temp.x, temp.y, temp.z, temp.w);
               writer.write(send);
            }
         }

         if (texCoords.size() != 0) {
            send = String.format("%d\n\n ---------------- COORDS  ----------------------------- \n\n",
                  vertsNormTexSize);
            writer.write(send);

            for (int i = 0; i < vertsNormTexSize; i++) {
               Vector temp = texCoords.get(i);
               send = String.format("{%f, %f, %f, %f}, \n", temp.x, temp.y, temp.z, temp.w);
               writer.write(send);
            }
         }

         send = String.format("%d\n\n --------------------INDICES -------------------------\n\n", indices.size());
         writer.write(send);

         for (int i = 0; i < indices.size(); i += 6) {
            send = String.format("%d, %d, %d, %d, %d, %d,\n", indices.get(i), indices.get(i + 1), indices.get(i + 2),
                  indices.get(i + 3), indices.get(i + 4), indices.get(i + 5));
            writer.write(send);
         }

         writer.close();
      } catch (FileNotFoundException e) {
         e.printStackTrace();
      } catch (IOException e) {
         e.printStackTrace();
      }
   }

   public void DumpIndicesAndVertices() {
      for (Vector v : vertices) {
         v.DumpVector();
      }
      System.out.println(this.indices);
   }

   public void WriteIndicesToTextFileForTRISTRIP(String filename) {
      try {
         BufferedWriter writer = new BufferedWriter(new FileWriter(filename, false));
         String send = null;

         send = String.format("%d\n", indices.size());
         writer.write(send);

         for (int i = 0; i < indices.size(); i += 1) {
            send = String.format("%d\n", indices.get(i));
            writer.write(send);
         }

         writer.close();
      } catch (FileNotFoundException e) {
         e.printStackTrace();
      } catch (IOException e) {
         e.printStackTrace();
      }
   }
}
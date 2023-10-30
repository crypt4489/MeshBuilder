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
   public ArrayList<VectorInt> bones; // = new ArrayList<VectorInt>();
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

   private boolean useIndices = false;

   public Mesh() {
      this.meshName = null;
   }

   public Mesh(String _name) {
      this.meshName = _name;
   }

   public AnimationData findAnimDataByName(String name) {

      for (AnimationData data : this.animData) {
         if (data.CompareAnimName(name))
            return data;
      }

      return null;
   }

   public void CreateAnimationStructures() {
      if (!createdAnim) {
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
      this.useIndices = true;
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
         if (bones != null && bones.size() != 0) {
            bones_i.add(bones.get(index));
         }

         if (weights != null && weights.size() != 0) {
            weights_i.add(weights.get(index));
         }
         // System.out.print((index) + " ");
      }

      // System.out.println();

      // System.out.println(indices.size());
      // System.out.println(normals_i.size());
      // System.out.println(texCoords_i.size());
      // System.out.println(vertices_i.size());
      for (MaterialRange range : materialList) {
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

   private boolean[] GetLastElements(short code) {
      boolean[] ret = new boolean[5];
      // System.out.printf("%x\n", code);
      // just verts
      ret[0] = code == 0x01;
      // last tex
      ret[1] = ((code & 0x02) == 0x02) && (code & 0x2f) <= 0x03;
      // last normals
      ret[2] = ((code & 0x04) == 0x04) && (code & 0x2f) <= 0x07;
      // last indices
      ret[3] = ((code & 0x08) == 0x08) && (code & 0x2f) <= 0x0f;
      // last animation
      ret[4] = ((code & 0x20) == 0x20);

      return ret;
   }

   private short GenerateMeshCode() {
      short code = 0;
      if (vertices_i.size() != 0) {
         code |= 0x01;
      }

      if (normals_i.size() != 0) {
         code |= 0x04;
      }

      if (texCoords_i.size() != 0) {
         code |= 0x02;
      }

      if (indices.size() != 0) {
         code |= 0x08;
      }

      if (materialList.size() != 0) {
         code |= 0x10;
      }
      if (bones != null && weights != null) {
         if (bones.size() != 0 && weights.size() != 0) {
            code |= 0x20;
         }
      }

      return code;
   }

   private int LittleEndianFloatConv(float f)
   {
      return Integer.reverseBytes(Float.floatToIntBits(f));
   }

   public void WriteMeshToBinFile(String filename) {
      try {
         FileOutputStream outputStream = new FileOutputStream(filename);
         DataOutputStream dos = new DataOutputStream(outputStream);
         dos.writeShort(0xDF01); //
         short code = GenerateMeshCode();
         boolean[] ret = GetLastElements(code);
         dos.writeShort(code);
         dos.writeInt(Integer.reverseBytes(this.indices.size()));
         dos.writeInt(Integer.reverseBytes(vertices.size()));
         dos.writeInt(badbeef);

         int vertsNormTexSize = this.indices.size();
         int endingIndex = this.indices.size() - 1;
         int chunks = this.materialList.size();
         int index = 0;
         System.out.println(chunks);
         if (chunks == 0)
            chunks = 1;

         for (int j = 0; j < chunks; j++) {
            if (materialList.size() > 0) {
               MaterialRange rang = materialList.get(j);
               dos.writeByte(0x08);
               dos.writeInt(Integer.reverseBytes(rang.start));
               dos.writeInt(Integer.reverseBytes(rang.end));
               dos.writeByte(rang.mat.fileName.length());
               for (byte b : rang.mat.fileName.getBytes()) {
                  dos.writeByte(b);
               }
               System.out.println(rang.mat.fileName);
               dos.writeInt(badbeef);
               vertsNormTexSize = (rang.end - rang.start) + 1;
               index = rang.start;
               endingIndex = rang.end;
            }

            dos.writeByte(0x02);
            dos.writeInt(Integer.reverseBytes(vertsNormTexSize));

            for (int i = index; i <= endingIndex; i++) {
               Vector temp = vertices_i.get(i);
               dos.writeInt(LittleEndianFloatConv((float) temp.x));
               dos.writeInt(LittleEndianFloatConv((float) temp.y));
               dos.writeInt(LittleEndianFloatConv((float) temp.z));
               // dos.writeFloat((float) temp.w);
               // temp.DumpVector();
            }

            if (!ret[0] || j + 1 < chunks) {
               dos.writeInt(badbeef);
            }

            if (texCoords.size() != 0) {
               dos.writeByte(0x04);
               dos.writeInt(Integer.reverseBytes(vertsNormTexSize));

               for (int i = index; i <= endingIndex; i++) {
                  Vector temp = texCoords_i.get(i);
                  dos.writeInt(LittleEndianFloatConv((float) temp.x));
                  dos.writeInt(LittleEndianFloatConv((float) temp.y));
                  // dos.writeFloat((float) temp.z);
                  // dos.writeFloat((float) temp.w);
               }

               // System.out.println("here");
               if (!ret[1]|| j + 1 < chunks) {
                  dos.writeInt(badbeef);
               }
            }


            if (normals.size() != 0) {

               dos.writeByte(0x05);
               dos.writeInt(Integer.reverseBytes(vertsNormTexSize));

               for (int i = index; i <= endingIndex; i++) {
                  Vector temp = normals_i.get(i);
                  dos.writeInt(LittleEndianFloatConv((float) temp.x));
                  dos.writeInt(LittleEndianFloatConv((float) temp.y));
                  dos.writeInt(LittleEndianFloatConv((float) temp.z));
                  // dos.writeFloat((float) temp.w);
               }

               if (!ret[2] || j + 1 < chunks) {
                  dos.writeInt(badbeef);
               }
            }

            if (bones != null && bones.size() != 0) {
               dos.writeByte(0x06);
               dos.writeInt(Integer.reverseBytes(vertsNormTexSize));
               for (int i = index; i <= endingIndex; i++) {
                  VectorInt temp = bones_i.get(i);
                  dos.writeByte(temp.x);
                  dos.writeByte(temp.y);
                  dos.writeByte(temp.z);
                  dos.writeByte(temp.w);
               }

               dos.writeInt(badbeef);

            }

            if (weights != null && weights.size() != 0) {
               dos.writeByte(0x07);
               dos.writeInt(Integer.reverseBytes(vertsNormTexSize));
               for (int i = index; i <= endingIndex; i++) {
                  Vector temp = weights_i.get(i);
                  dos.writeInt(LittleEndianFloatConv((float) temp.x));
                  dos.writeInt(LittleEndianFloatConv((float) temp.y));
                  dos.writeInt(LittleEndianFloatConv((float) temp.z));
                  dos.writeInt(LittleEndianFloatConv((float) temp.w));
               }
               dos.writeInt(badbeef);

            }

            dos.writeByte(0x03);
            dos.writeInt(Integer.reverseBytes(vertsNormTexSize));

            for (int i = index; i <= endingIndex; i++) {
               dos.writeInt(Integer.reverseBytes(indices.get(i)));
            }

            if (!ret[3] || j + 1 < chunks) {
                  dos.writeInt(badbeef);
            }

            if (adjArrayList.size() != 0) {
               dos.writeByte(0x07);
               dos.writeInt(Integer.reverseBytes(adjArrayList.size()));

               for (int i = 0; i < adjArrayList.size(); i++) {
                  Vector temp = adjArrayList.get(i);
                  dos.writeInt(LittleEndianFloatConv((float) temp.x));
                  dos.writeInt(LittleEndianFloatConv((float) temp.y));
                  dos.writeInt(LittleEndianFloatConv((float) temp.z));
                  dos.writeInt(LittleEndianFloatConv((float) temp.w));
               }
               System.out.println("adjs");
               dos.writeInt(badbeef);
            }
         }

         if (this.createdAnim) {
            // write animation data

            // joints
            dos.writeByte(0x09);
            dos.writeInt(Integer.reverseBytes(this.joints.size()));
            for (Joint joint : this.joints) {
               dos.writeByte(joint.id);
               System.out.println(joint.id);
               dos.writeByte(joint.name.length());
               WriteCharsAsBytes(dos, joint.name);
               WriteMatrixToStream(dos, joint.offset);
            }

            // animation data

            for (AnimationData data : this.animData) {
               dos.writeInt(badbeef);
               dos.writeByte(0x0A);
               dos.writeInt(Integer.reverseBytes(data.name.length()));
               WriteCharsAsBytes(dos, data.name);
               dos.writeInt(LittleEndianFloatConv((float) data.m_Duration));
               dos.writeInt(LittleEndianFloatConv((float) data.m_TicksPerSecond));
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

   private void WriteCharsAsBytes(DataOutputStream dos, String text) {
      try {
         int len = text.length();
         for (int i = 0; i < len; i++) {
            dos.writeByte(text.charAt(i));
         }
      } catch (IOException e) {
         e.printStackTrace();
      }
   }

   private void WriteSRTToStream(DataOutputStream dos, AnimationData data) {
      try {
         dos.writeInt(Integer.reverseBytes(data.positions.size()));
         for (Map.Entry<Integer, ArrayList<AnimationData.KeyPosition>> entry : data.positions.entrySet()) {
            dos.writeInt(Integer.reverseBytes(entry.getKey()));
            ArrayList<AnimationData.KeyPosition> poses = entry.getValue();
            dos.writeInt(Integer.reverseBytes(poses.size()));
            for (AnimationData.KeyPosition pos : poses) {
               dos.writeInt(LittleEndianFloatConv(pos.timeStamp));
               dos.writeInt(LittleEndianFloatConv((float) pos.pos.x));
               dos.writeInt(LittleEndianFloatConv((float) pos.pos.y));
               dos.writeInt(LittleEndianFloatConv((float) pos.pos.z));
               dos.writeInt(LittleEndianFloatConv((float) pos.pos.w));
            }
         }

         dos.writeInt(Integer.reverseBytes(data.rotations.size()));
         for (Map.Entry<Integer, ArrayList<AnimationData.KeyRotation>> entry : data.rotations.entrySet()) {
            dos.writeInt(Integer.reverseBytes(entry.getKey()));
            ArrayList<AnimationData.KeyRotation> rots = entry.getValue();
            dos.writeInt(Integer.reverseBytes(rots.size()));
            for (AnimationData.KeyRotation rot : rots) {
               dos.writeInt(LittleEndianFloatConv(rot.timeStamp));
               dos.writeInt(LittleEndianFloatConv((float) rot.quat.x));
               dos.writeInt(LittleEndianFloatConv((float) rot.quat.y));
               dos.writeInt(LittleEndianFloatConv((float) rot.quat.z));
               dos.writeInt(LittleEndianFloatConv((float) rot.quat.w));
            }
         }

         dos.writeInt(Integer.reverseBytes(data.scalings.size()));
         for (Map.Entry<Integer, ArrayList<AnimationData.KeyScaling>> entry : data.scalings.entrySet()) {
            dos.writeInt(Integer.reverseBytes(entry.getKey()));
            ArrayList<AnimationData.KeyScaling> scales = entry.getValue();
            dos.writeInt(Integer.reverseBytes(scales.size()));
            for (AnimationData.KeyScaling scale : scales) {
               dos.writeInt(LittleEndianFloatConv(scale.timeStamp));
               dos.writeInt(LittleEndianFloatConv((float) scale.scales.x));
               dos.writeInt(LittleEndianFloatConv((float) scale.scales.y));
               dos.writeInt(LittleEndianFloatConv((float) scale.scales.z));
               dos.writeInt(LittleEndianFloatConv((float) scale.scales.w));
            }
         }
      } catch (IOException e) {
         e.printStackTrace();
      }
   }

   private void WriteAnimNodesToStream(DataOutputStream dos, AnimationData.AssimpNodeData node) {
      try {
         dos.writeInt(Integer.reverseBytes(node.name.length()));
         WriteCharsAsBytes(dos, node.name);
         dos.writeByte(node.childrenCount);
         System.out.println(node.name);
         node.transformation.DumpMatrix();
         WriteMatrixToStream(dos, node.transformation);
         for (AnimationData.AssimpNodeData child : node.children) {
            WriteAnimNodesToStream(dos, child);
         }
      } catch (IOException e) {
         e.printStackTrace();
      }
   }

   private void WriteMatrixToStream(DataOutputStream dos, Matrix m) {
      try {
         dos.writeInt(LittleEndianFloatConv((float) m.row1.x));
         dos.writeInt(LittleEndianFloatConv((float) m.row1.y));
         dos.writeInt(LittleEndianFloatConv((float) m.row1.z));
         dos.writeInt(LittleEndianFloatConv((float) m.row1.w));

         dos.writeInt(LittleEndianFloatConv((float) m.row2.x));
         dos.writeInt(LittleEndianFloatConv((float) m.row2.y));
         dos.writeInt(LittleEndianFloatConv((float) m.row2.z));
         dos.writeInt(LittleEndianFloatConv((float) m.row2.w));

         dos.writeInt(LittleEndianFloatConv((float) m.row3.x));
         dos.writeInt(LittleEndianFloatConv((float) m.row3.y));
         dos.writeInt(LittleEndianFloatConv((float) m.row3.z));
         dos.writeInt(LittleEndianFloatConv((float) m.row3.w));

         dos.writeInt(LittleEndianFloatConv((float) m.row4.x));
         dos.writeInt(LittleEndianFloatConv((float) m.row4.y));
         dos.writeInt(LittleEndianFloatConv((float) m.row4.z));
         dos.writeInt(LittleEndianFloatConv((float) m.row4.w));

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

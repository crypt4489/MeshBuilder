
import java.util.ArrayList;

class TriStrip
{
    class AdjacentTriangle
    {
        Face face;
        Face[] adjFaces = new Face[3];

        public AdjacentTriangle(Face _f)
        {
            this.face = _f;
            for (Face face : adjFaces) {
                face = null;
            }
        }
    }

    public ArrayList<Integer> CreateAdjacencyTris(ArrayList<Face> faces)
    {

        return null;
    }

    public static void main(String[] args)   {
        int[] box = {
            0, 1, 2,
            3, 2, 1,
            6, 5, 4,
            5, 6, 7,
            10, 9, 8,
            9, 10, 11,
            12, 13, 14,
            15, 14, 13,
            16, 17, 18,
            19, 18, 17,
            22, 21, 20,
            21, 22, 23
        };

        if (box.length % 3 != 0)
        {
            System.err.println("Expected Triangles got a number not a multiple of three!!");
            return;
        }

        ArrayList<Face> triangles = new ArrayList<Face>();

        for (int i = 0; i<box.length; i+=3)
        {
            Face tri = new Face(box[i], box[i+1], box[i+2]);
            triangles.add(tri);
        }


        System.out.println("TriStrips");
    }
}

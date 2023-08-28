
import java.util.ArrayList;
import java.util.Comparator;
import java.util.PriorityQueue;
import org.apache.commons.lang3.tuple.Pair;


class TriStrip
{
    static class Strip
    {
        public ArrayList<Integer> indices;
        public Strip()
        {
            indices = new ArrayList<Integer>();
        }
        public void AddFace(Face f)
        {
            indices.add(f.indices[0]);
            indices.add(f.indices[1]);
            indices.add(f.indices[2]);
        }
        public void AddIndex(int index)
        {
            indices.add(index);
        }
        public void PrintNode()
        {
            System.out.println(indices);
        }
    }

    static class AdjacentTriangle
    {
        public Face face;
        public AdjacentTriangle[] adjFaces = new AdjacentTriangle[3];
        public boolean visited;
        public int connections;
        public AdjacentTriangle(Face _f)
        {
            this.face = _f;
            this.connections = 0;
            this.visited = false;
        }

        public boolean AddAdjFace(int index, AdjacentTriangle adjFace)
        {
            if (connections == 3)
            {
                System.err.println("Connection is already three");
                return false;
            }
            this.adjFaces[index] = adjFace;
            connections++;
            return true;
        }

        public int GetOppositeIndex(int v1, int v2)
        {
            int ret = -1;
            for (int index : this.face.indices)
            {
                if (index != v1 && index != v2)
                {
                    ret = index;
                    break;
                }
            }
            return ret;
        }

        public Pair<Boolean, Integer> AllVisited()
        {
            for (int i = 0; i<3; i++)
            {
                if (adjFaces[i] != null && !adjFaces[i].visited)
                {
                    return Pair.of(false, i);
                }
            }

            return Pair.of(true, -1);
        }
    }

    static class AdjacentTriangleComparator implements Comparator<AdjacentTriangle> {
        public int compare(AdjacentTriangle tri1, AdjacentTriangle tri2)
        {
            if (tri1.connections > tri2.connections)
            {
                return 1;
            }
            
            if (tri1.connections < tri2.connections)
            {
                return -1;
            }

            return 0;
        }
    } 

    public static void CreateAdjacencyTris(ArrayList<AdjacentTriangle> faces)
    {
        for (AdjacentTriangle testTri : faces)
        {
            for (AdjacentTriangle compareTri : faces)
            {
                if (compareTri == testTri)
                    continue;
                for (int i = 0; i<3; i++)
                {
                    Edge testE = testTri.face.edges[i];
                    for (Edge compE : compareTri.face.edges)
                    {
                        if (testE.equals(compE))
                        {
                            testTri.AddAdjFace(i, compareTri);
                            break;
                        }
                    }
                }
            }
        }
    }

    public static ArrayList<Strip> BuildStrips(PriorityQueue<AdjacentTriangle> queue)
    {
        ArrayList<Strip> strips = new ArrayList<Strip>();
        while(!queue.isEmpty())
        {
            AdjacentTriangle tri = queue.remove();
            if (tri.visited)
                continue;
            Strip strip = new Strip();
            strip.AddFace(tri.face);
            while(true)
            {
                tri.visited = true;
                Pair<Boolean, Integer> ret = tri.AllVisited();
                if (ret.getLeft())
                    break;
                int neighborIndex = ret.getRight();
                AdjacentTriangle neighbor = tri.adjFaces[neighborIndex];
                
                int edgeV1 = tri.face.edges[neighborIndex].v1;
                int edgeV2 = tri.face.edges[neighborIndex].v2;

                int stripIndex = neighbor.GetOppositeIndex(edgeV1, edgeV2);
                strip.AddIndex(stripIndex);
                tri = neighbor;
            }

            strips.add(strip);
        }

        return strips;
    }

    public static void main(String[] args)   {
        /*int[] box = {
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
        }; */
        /*int[] box = {
            0,1,2,
            1,2,3,
            2,3,4,
            3,4,5,
            4,5,6,
            5,6,7,
            6,7,8,
            7,8,9
        }; */
        int[] box = {
            18, 19, 20,
            21, 22, 23,
            76, 77, 78,
            21, 23, 79,
            80, 81, 82,
            76, 78, 83,
            84, 85, 86,
            87, 88, 89,
            121, 20, 82,
            80, 82, 19,
            19, 82, 20,
            122, 23, 89,
            87, 89, 22,
            22, 89, 23,
            122, 78, 135,
            121, 135, 77,
            77, 135, 78,
            80, 79, 81,
            122, 81, 23,
            23, 81, 79,
            121, 82, 135,
            122, 135, 81,
            81, 135, 82,
            136, 83, 137,
            122, 137, 78,
            78, 137, 83,
            87, 86, 88,
            136, 88, 85,
            85, 88, 86,
            122, 89, 137,
            136, 137, 88,
            88, 137, 89,
            29, 30, 31,
            21, 32, 33,
            103, 104, 105,
            21, 33, 106,
            107, 108, 109,
            103, 105, 110,
            56, 111, 112,
            73, 113, 114,
            124, 31, 109,
            107, 109, 30,
            30, 109, 31,
            125, 33, 114,
            73, 114, 32,
            32, 114, 33,
            125, 105, 141,
            124, 141, 104,
            104, 141, 105,
            107, 106, 108,
            125, 108, 33,
            33, 108, 106,
            124, 109, 141,
            125, 141, 108,
            108, 141, 109,
            142, 110, 143,
            125, 143, 105,
            105, 143, 110,
            73, 112, 113,
            142, 113, 111,
            111, 113, 112,
            125, 114, 143,
            142, 143, 113,
            113, 143, 114,
            12, 13, 14,
            15, 16, 17,
            62, 63, 64,
            15, 17, 65,
            66, 67, 68,
            62, 64, 69,
            70, 71, 72,
            73, 74, 75,
            119, 14, 68,
            66, 68, 13,
            13, 68, 14,
            120, 17, 75,
            73, 75, 16,
            16, 75, 17,
            120, 64, 132,
            119, 132, 63,
            63, 132, 64,
            66, 65, 67,
            120, 67, 17,
            17, 67, 65,
            119, 68, 132,
            120, 132, 67,
            67, 132, 68,
            133, 69, 134,
            120, 134, 64,
            64, 134, 69,
            73, 72, 74,
            133, 74, 71,
            71, 74, 72,
            120, 75, 134,
            133, 134, 74,
            74, 134, 75,
            0, 1, 2,
            3, 4, 5,
            34, 35, 36,
            3, 5, 37,
            38, 39, 40,
            34, 36, 41,
            42, 43, 44,
            45, 46, 47,
            115, 2, 40,
            38, 40, 1,
            1, 40, 2,
            116, 5, 47,
            45, 47, 4,
            4, 47, 5,
            116, 36, 126,
            115, 126, 35,
            35, 126, 36,
            38, 37, 39,
            116, 39, 5,
            5, 39, 37,
            115, 40, 126,
            116, 126, 39,
            39, 126, 40,
            127, 41, 128,
            116, 128, 36,
            36, 128, 41,
            45, 44, 46,
            127, 46, 43,
            43, 46, 44,
            116, 47, 128,
            127, 128, 46,
            46, 128, 47,
            18, 24, 25,
            26, 27, 28,
            48, 90, 91,
            26, 28, 92,
            93, 94, 95,
            48, 91, 96,
            97, 98, 99,
            100, 101, 102,
            119, 25, 95,
            93, 95, 24,
            24, 95, 25,
            123, 28, 102,
            100, 102, 27,
            27, 102, 28,
            123, 91, 138,
            119, 138, 90,
            90, 138, 91,
            93, 92, 94,
            123, 94, 28,
            28, 94, 92,
            119, 95, 138,
            123, 138, 94,
            94, 138, 95,
            139, 96, 140,
            123, 140, 91,
            91, 140, 96,
            100, 99, 101,
            139, 101, 98,
            98, 101, 99,
            123, 102, 140,
            139, 140, 101,
            101, 140, 102,
            6, 7, 8,
            9, 10, 11,
            48, 49, 50,
            9, 11, 51,
            52, 53, 54,
            48, 50, 55,
            56, 57, 58,
            59, 60, 61,
            117, 8, 54,
            52, 54, 7,
            7, 54, 8,
            118, 11, 61,
            59, 61, 10,
            10, 61, 11,
            118, 50, 129,
            117, 129, 49,
            49, 129, 50,
            52, 51, 53,
            118, 53, 11,
            11, 53, 51,
            117, 54, 129,
            118, 129, 53,
            53, 129, 54,
            130, 55, 131,
            118, 131, 50,
            50, 131, 55,
            59, 58, 60,
            130, 60, 57,
            57, 60, 58,
            118, 61, 131,
            130, 131, 60,
            60, 131, 61
        };

        if (box.length % 3 != 0)
        {
            System.err.println("Expected Triangles got a number not a multiple of three!!");
            return;
        }

        ArrayList<AdjacentTriangle> triangles = new ArrayList<AdjacentTriangle>();

        for (int i = 0; i<box.length; i+=3)
        {
            Face tri = new Face(box[i], box[i+1], box[i+2]);
            AdjacentTriangle aTri = new AdjacentTriangle(tri);
            triangles.add(aTri);
        }

        TriStrip.CreateAdjacencyTris(triangles);

        PriorityQueue<AdjacentTriangle> triangleQueue = new PriorityQueue<>(triangles.size(), 
                                                        new AdjacentTriangleComparator());

        for (int i = 0; i<triangles.size(); i++)
        {
             System.out.printf("Face #%d\n", i+1);
            AdjacentTriangle tri = triangles.get(i);
            tri.face.DumpFace();
            System.out.println("+++++++++++++++");
            for (AdjacentTriangle adj : tri.adjFaces)
            {
                if (adj != null)
                    adj.face.DumpFace();
                else    
                    System.out.println("null face");
            }
            System.out.println("-------------");
            
            triangleQueue.add(triangles.get(i));
        }

        ArrayList<Strip> strips = BuildStrips(triangleQueue);
        for (Strip strip : strips)
            strip.PrintNode();
    }
}

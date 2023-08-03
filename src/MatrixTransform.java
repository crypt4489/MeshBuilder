public class MatrixTransform implements Runnable {
    
    private Matrix matTransform;
    private Mesh toTransform;
    private Boolean adjustNormals = false;

    public MatrixTransform(Matrix _mat, Boolean _set)
    {
        matTransform = _mat;
        adjustNormals = _set;
    }

    public Mesh getMesh()
    {
        return this.toTransform;
    }

    public void setMesh(Mesh mesh)
    {
        this.toTransform = mesh;
    }
    
    @Override
    public void run()
    {
        int size = toTransform.indices.size();
        for (int i = 0; i<size; i++)
        {
            Vector vert = matTransform.MatrixVectorMultiply(toTransform.vertices_i.get(i));
            toTransform.vertices_i.set(i, vert);
            if (adjustNormals)
            {
                Vector norm = matTransform.MatrixVectorMultiply(toTransform.normals_i.get(i));
                /* 
                System.out.println("-------Normie unAdjusted ----------");
                toTransform.normals_i.get(i).DumpVector();

                System.out.println("-------Normie Adjusted ----------");
                norm.DumpVector();*/
                toTransform.normals_i.set(i, norm);
            }
        }
    }
}

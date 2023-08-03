class VectorInt
{
    public int x, y, z, w;

    public VectorInt(int _x, int _y, int _z, int _w)
    {
        this.x = _x;
        this.y = _y;
        this.z = _z;
        this.w = _w;
    }

    public void DumpVector()
    {
        System.out.printf("%d %d %d %d\n", this.x, this.y, this.z, this.w);
    }
}
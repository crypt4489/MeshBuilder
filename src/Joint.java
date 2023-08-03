class Joint
{
    public String name;
    public int id;
    public Matrix offset;  

    public Joint(String _name, int _id, Matrix _offset)
    {
        this.id = _id;
        this.name = _name;
        this.offset = _offset;
    }
}
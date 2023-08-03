import java.util.ArrayList;
import java.util.HashMap;

class AnimationData
{
    static abstract class AnimationKey
    {
        float timeStamp;
    }

    public static class KeyRotation extends AnimationKey
    {
        Vector quat;

        public KeyRotation(Vector _in, float _time)
        {
            this.quat = _in;
            this.timeStamp = _time;
        }
    }

    public static class KeyPosition extends AnimationKey
    {
        Vector pos;

        public KeyPosition(Vector _in, float _time)
        {
            this.pos = _in;
            this.timeStamp = _time;
        }
    }

    public static class KeyScaling extends AnimationKey
    {
        Vector scales;

        public KeyScaling(Vector _in, float _time)
        {
            this.scales = _in;
            this.timeStamp = _time;
        }
    }

    public static class AssimpNodeData
    {
        public Matrix transformation;
        public String name;
        public int childrenCount;
        public ArrayList<AssimpNodeData> children;

        public AssimpNodeData()
        {
            this.name = "";
            this.transformation = Matrix.Zero();
            this.childrenCount = -1;
            this.children = new ArrayList<AssimpNodeData>();
        }

        public AssimpNodeData(String _name, Matrix _trans)
        {
            this.name = _name;
            this.transformation = _trans;
            this.childrenCount = 0;
            this.children = new ArrayList<AssimpNodeData>();
        }

        public void AddChildToNodeData(AssimpNodeData child)
        {
            this.children.add(child);
            //this.childrenCount++;
        }
    }

    public float m_Duration;
    public float m_TicksPerSecond;
    public String name;
    public AssimpNodeData m_RootNode;
    public HashMap<Integer, ArrayList<AnimationData.KeyPosition>> positions = new HashMap<Integer, ArrayList<KeyPosition>>();
    public HashMap<Integer, ArrayList<AnimationData.KeyRotation>> rotations = new HashMap<Integer, ArrayList<KeyRotation>>();
    public HashMap<Integer, ArrayList<AnimationData.KeyScaling>> scalings = new HashMap<Integer, ArrayList<KeyScaling>>();

    public AnimationData(String _name, float _ticks, float _duration)
    {
        this.name = _name;
        this.m_TicksPerSecond = _ticks;
        this.m_Duration = _duration;
        this.m_RootNode = new AssimpNodeData();
    }

    public void DumpAnimation()
    {
        System.out.println("Duration: " + this.m_Duration);
        System.out.println("Ticks: " + this.m_TicksPerSecond);
        System.out.println("Name: " + this.name);
    }

    public void AddPosition(int index, KeyPosition pos)
    {
        ArrayList<KeyPosition> posie = null;

        if ((posie = positions.get(index)) == null)
        {
            positions.put(index, posie = new ArrayList<KeyPosition>());
        }

        posie.add(pos);
    }

    public void AddRotation(int index, KeyRotation rot)
    {
        ArrayList<KeyRotation> rots = null;

        if ((rots = rotations.get(index)) == null)
        {
            rotations.put(index,  rots = new ArrayList<KeyRotation>());
        }

        rots.add(rot);
    }

    public void AddScaling(int index, KeyScaling scale)
    {
        ArrayList<KeyScaling>  scales = null;

        if ((scales = scalings.get(index)) == null)
        {
            scalings.put(index,  scales = new ArrayList<KeyScaling>());
        }

        scales.add(scale);
    }

    public boolean CompareAnimName(String name)
    {
        return name == this.name;
    }

    public void DumpSRT(int index)
    {
        System.out.printf("Scales for %d : %d\n", index, scalings.get(index).size());
        System.out.printf("Rotations %d : %d\n", index, rotations.get(index).size());
        System.out.printf("Poses %d : %d\n", index, positions.get(index).size());
    }
}
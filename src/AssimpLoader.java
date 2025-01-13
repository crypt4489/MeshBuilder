import org.lwjgl.assimp.*;

import AnimationData.AssimpNodeData;

import java.nio.IntBuffer;
import java.util.ArrayList;

import org.lwjgl.PointerBuffer;

class AssimpData {
    public AIMesh inputMesh;
    public AIMaterial inputMaterial;

    public AssimpData(AIMesh _mesh, AIMaterial _mat) {
        this.inputMaterial = _mat;
        this.inputMesh = _mesh;
    }
}

class AssimpLoader
{
    public AIScene ai_scene;
    public AIMesh ai_mesh;
    public Mesh myMesh;

    public void initScene(String fileName)
    {
        int flags = Assimp.aiProcess_Triangulate | Assimp.aiProcess_GenSmoothNormals | Assimp.aiProcess_CalcTangentSpace;
        this.ai_scene = Assimp.aiImportFile(fileName, flags);
    }

    public PointerBuffer getMeshesFromScene()
    {
        return this.ai_scene.mMeshes();
    }

    public PointerBuffer getMaterialsFromScene()
    {
        return this.ai_scene.mMaterials();
    }

    public void loadFile(String fileName, Mesh mesh, String meshName)
    {
        int flags = Assimp.aiProcess_Triangulate | Assimp.aiProcess_GenSmoothNormals | Assimp.aiProcess_CalcTangentSpace;
        ai_scene = Assimp.aiImportFile(fileName, flags);
        PointerBuffer meshes = ai_scene.mMeshes();
        PointerBuffer materials = ai_scene.mMaterials();
        System.out.println(meshes.capacity());
        int offsetIndex = 0;
        int offsetVertex = 0;
        for (int i = 0; i<meshes.capacity(); i++)
        {
            ai_mesh = AIMesh.create(meshes.get(i));
            System.out.println(ai_mesh.mName().dataString());

            if (meshName.equals(ai_mesh.mName().dataString()))
            {
                if (materials.capacity() > 1)
                {
                    AIMaterial mat = AIMaterial.create(materials.get(ai_mesh.mMaterialIndex()));
                    AIString path = AIString.calloc();
                    Assimp.aiGetMaterialTexture(mat, Assimp.aiTextureType_DIFFUSE, 0, path, (IntBuffer)null, null, null, null, null, null);
                    DMaterial myMat = new DMaterial();
                    MaterialRange range = new MaterialRange(offsetIndex, 0, myMat);
                    myMat.FindFileName(path);
                    int ret = createMatMeshFromAiMesh(mesh, myMat, offsetVertex);
                    offsetVertex = offsetVertex + ai_mesh.mNumVertices();
                    offsetIndex = ret + offsetIndex;
                    range.end = offsetIndex-1;
                    mesh.materialList.add(range);
                } else {
                    createMyMeshFromAiMesh(mesh);
                }
                CreateBonesWeightsAnimationDataFromAIMesh(mesh);
            } else {
                break;
            }
        }

    }

    public void CreateBonesWeightsAnimationDataFromAIMesh(Mesh mesh)
    {
        int bonesNum = 0;
        System.out.println("Bones");
        if ((bonesNum = ai_mesh.mNumBones()) != 0)
        {
            mesh.CreateAnimationStructures();
            
            for (int j = 0; j<ai_mesh.mNumVertices(); j++) {
                mesh.weights.add(new Vector(0.0, 0.0, 0.0, 0.0f));
                mesh.bones.add(new VectorInt(-1, -1, -1, -1));
            }

            int boneCount = 0;

            PointerBuffer bones = ai_mesh.mBones();

            for (int i = 0; i<bonesNum; i++)
            {
                AIBone bone = AIBone.create(bones.get(i));

                String name = bone.mName().dataString();
                int boneID = FindJointIndexUsingName(name, mesh.joints);
                if (boneID == -1)
                {
                    AIMatrix4x4 offsetMat = bone.mOffsetMatrix();
                    Joint joint = new Joint(name, boneCount, Matrix.CreateFromAIMatrix(offsetMat));
                    System.out.printf("the bone is %s and index is %d\n", name, joint.id);
                    mesh.joints.add(joint);
                    boneCount++;
                }
                
                AIVertexWeight.Buffer weights = bone.mWeights();

                for (int w = 0; w<bone.mNumWeights(); w++)
                {
                    AIVertexWeight weight = weights.get(w);
                    int vertexID = weight.mVertexId();
                    float weightVal = weight.mWeight();
                    SetVertexBoneData(mesh, vertexID, i, weightVal);
                }
            }

            GetAnimationData(mesh);

            System.out.println("Weights count " + mesh.weights.size());
            System.out.println("Bones count " + mesh.bones.size());

        }
    }

    // create mesh from material (so only add a specific range of vertices)
    public int createMatMeshFromAiMesh(Mesh mesh, DMaterial mat, int offset)
    {
        System.out.println(mesh.meshName);

        int numVertices = ai_mesh.mNumVertices();
        AIVector3D.Buffer aiVertices = ai_mesh.mVertices();
        AIVector3D.Buffer aiNormals = ai_mesh.mNormals();
        AIVector3D.Buffer aiTex = ai_mesh.mTextureCoords(0);

        System.out.println(numVertices);

        // load vertices, normals, and texcoords which are stored by vertex
        for (int i = 0; i<numVertices; i++)
        {
            AIVector3D aiVertex = aiVertices.get();
            AIVector3D aiNormal = aiNormals.get();
            AIVector3D aiTexCoord = aiTex.get();
            mesh.vertices.add(new Vector(aiVertex.x(), aiVertex.y(), aiVertex.z(), 1.0f));
            mesh.normals.add(new Vector(aiNormal.x(), aiNormal.y(), aiNormal.z(), 1.0f));
            mesh.texCoords.add(new Vector(aiTexCoord.x(), aiTexCoord.y(), 1.0f, 1.0f));
        }

        System.out.println(mesh.vertices.size());
        System.out.println(mesh.normals.size());
        System.out.println(mesh.texCoords.size());
        AIFace.Buffer faces = ai_mesh.mFaces();
        for (int i = 0; i < ai_mesh.mNumFaces(); i++)
	    {
		    AIFace face = faces.get();
            IntBuffer indices = face.mIndices();
		    for (int j = 0; j < face.mNumIndices(); j++)
		    {
			    mesh.indices.add(indices.get(j) + offset);
		    }
	    }
        if (offset == 0) CreateBonesWeightsAnimationDataFromAIMesh(mesh);
        
        return mesh.indices.size();
    }

    public void createMyMeshFromAiMesh(Mesh mesh)
    {
        System.out.println(mesh.meshName);
        int numVertices = ai_mesh.mNumVertices();
        AIVector3D.Buffer aiVertices = ai_mesh.mVertices();
        AIVector3D.Buffer aiNormals = ai_mesh.mNormals();
        AIVector3D.Buffer aiTex = ai_mesh.mTextureCoords(0);

        System.out.println(numVertices);

        for (int i = 0; i<numVertices; i++)
        {
            AIVector3D aiVertex = aiVertices.get();
            AIVector3D aiNormal = aiNormals.get();
            AIVector3D aiTexCoord = aiTex.get();
            mesh.vertices.add(new Vector(aiVertex.x(), aiVertex.y(), aiVertex.z(), 1.0f));
            mesh.normals.add(new Vector(aiNormal.x(), aiNormal.y(), aiNormal.z(), 1.0f));
            mesh.texCoords.add(new Vector(aiTexCoord.x(), aiTexCoord.y(), 1.0f, 1.0f));
        }

        System.out.println(mesh.vertices.size());
        System.out.println(mesh.normals.size());
        System.out.println(mesh.texCoords.size());
        AIFace.Buffer faces = ai_mesh.mFaces();
        for (int i = 0; i < ai_mesh.mNumFaces(); i++)
	    {
		    AIFace face = faces.get();
            IntBuffer indices = face.mIndices();
		    for (int j = 0; j < face.mNumIndices(); j++)
		    {
			    mesh.indices.add(indices.get(j));
		    }
	    }

        CreateBonesWeightsAnimationDataFromAIMesh(mesh);
    }

    private void SetVertexBoneData(Mesh mesh, int vertexIndex, int boneID, float weight)
    {
        VectorInt bones_id = mesh.bones.get(vertexIndex);
        Vector weights = mesh.weights.get(vertexIndex);
        if (bones_id.x == -1)
        {
            mesh.weights.set(vertexIndex, new Vector(weight, weights.y, weights.z, weights.w));
            mesh.bones.set(vertexIndex, new VectorInt(boneID, bones_id.y, bones_id.z, bones_id.w));
        } else if (bones_id.y == -1) {
            mesh.weights.set(vertexIndex, new Vector(weights.x, weight, weights.z, weights.w));
            mesh.bones.set(vertexIndex, new VectorInt(bones_id.x, boneID, bones_id.z, bones_id.w));
        } else if (bones_id.z == -1) {
            mesh.weights.set(vertexIndex, new Vector(weights.x, weights.y, weight, weights.w));
            mesh.bones.set(vertexIndex, new VectorInt(bones_id.x, bones_id.y, boneID, bones_id.w));
        } else if (bones_id.w == -1) {
            mesh.weights.set(vertexIndex, new Vector(weights.x, weights.y, weights.z, weight));
            mesh.bones.set(vertexIndex, new VectorInt(bones_id.x, bones_id.y, bones_id.z, boneID));
        }
    }

    private int FindJointIndexUsingName(String name, ArrayList<Joint> joints)
    {
        for (Joint joint : joints)
        {
            if (joint.name.equals(name))
            {
                return joint.id;
            }
        }

        return -1;
    }

    private void GetAnimationData(Mesh mesh)
    {
        int numOfAnims = this.ai_scene.mNumAnimations();
        mesh.nodeCount = ReadHierarchyData(mesh.m_RootNode, this.ai_scene.mRootNode(), mesh.nodeCount);
        System.out.printf("The Node Count is TADA : %d\n", mesh.nodeCount);
        for (int i = 0; i<numOfAnims; i++)
        {
            AIAnimation anim = AIAnimation.create(this.ai_scene.mAnimations().get(i));
            AnimationData data = new AnimationData(anim.mName().dataString(),
                                            (float)anim.mTicksPerSecond(),
                                            (float)anim.mDuration());
            data.DumpAnimation();
            
            
            GetAnimationSRT(anim, data, mesh);
            mesh.animData.add(data);
        }

       //System.out.printf("IT IS A SHAME %b\n", CompareHierarchies(mesh.animData.get(0).m_RootNode, mesh.animData.get(1).m_RootNode));
    }

    private boolean CompareHierarchies(AssimpNodeData root1, AssimpNodeData root2)
    {
        boolean ret = root1.transformation.equals(root2.transformation) && root1.childrenCount == root2.childrenCount;

        if (!ret)
        {
            System.out.printf("Dumping matrices: %s\n", root1.name);
            System.out.println("Node 1");
            root1.transformation.DumpMatrix();
            System.out.println("Node 2");
            root2.transformation.DumpMatrix();
            return false;
        }

        for (int i = 0; i<root1.childrenCount; i++)
        {
            ret = CompareHierarchies(root1.children.get(i), root2.children.get(i));
            if (!ret) break;
        }

        return ret;
    }

    private void GetAnimationSRT(AIAnimation anim, AnimationData data, Mesh mesh)
    {
        int size = anim.mNumChannels();
        for (int j = 0; j<size; j++)
        {
            AINodeAnim channel = AINodeAnim.create(anim.mChannels().get(j));
            String boneName = channel.mNodeName().dataString();
            int index = FindJointIndexUsingName(boneName, mesh.joints);
            if (index == -1)
            {
                index = mesh.joints.size();
                Joint joint = new Joint(boneName, index, Matrix.Zero());
                System.out.printf("Secondary: the bone is %s and index is %d\n", boneName, mesh.joints.size());
                mesh.joints.add(joint);
            }
            ReadAnimationData(data, channel, index);
        }
    }

    private int ReadHierarchyData(AssimpNodeData data, AINode src, int nodeCount)
    {
        data.name = src.mName().dataString();
        data.transformation = Matrix.CreateFromAIMatrix(src.mTransformation());
        int count = src.mNumChildren();
        System.out.println(count);
        System.out.println(data.name);
        data.childrenCount = count;
        for (int i = 0; i<count; i++)
        {
            AssimpNodeData datum = new AssimpNodeData();
            nodeCount = ReadHierarchyData(datum, AINode.create(src.mChildren().get(i)), nodeCount);
            data.AddChildToNodeData(datum);
        }

        return (nodeCount+=1);
    }

    private void ReadAnimationData(AnimationData data, AINodeAnim channel, int index)
    {
        int numPositions = channel.mNumPositionKeys();
        int numRotations = channel.mNumRotationKeys();
        int numScalings = channel.mNumScalingKeys();

        for (int i = 0; i<numPositions; i++)
        {
            AIVectorKey key = channel.mPositionKeys().get(i);
            AIVector3D aiPos = key.mValue();
            float timeStamp = (float)key.mTime();
            Vector vec = new Vector(aiPos.x(), aiPos.y(), aiPos.z(), 1.0f);
            AnimationData.KeyPosition pos = new AnimationData.KeyPosition(vec, timeStamp);
            data.AddPosition(index, pos);
        }

        for (int i = 0; i<numRotations; i++)
        {
            AIQuatKey key = channel.mRotationKeys().get(i);
            AIQuaternion aiRot = key.mValue();
            float timeStamp = (float)key.mTime();
            Vector vec = new Vector(aiRot.x(), aiRot.y(), aiRot.z(), aiRot.w());
            AnimationData.KeyRotation rot = new AnimationData.KeyRotation(vec, timeStamp);
            data.AddRotation(index, rot);
        }

        for (int i = 0; i<numScalings; i++)
        {
            AIVectorKey key = channel.mScalingKeys().get(i);
            AIVector3D aiScale = key.mValue();
            float timeStamp = (float)key.mTime();
            Vector vec = new Vector(aiScale.x(), aiScale.y(), aiScale.z(), 1.0f);
            AnimationData.KeyScaling pos = new AnimationData.KeyScaling(vec, timeStamp);
            data.AddScaling(index, pos);
        }
        data.DumpSRT(index);
    }
}
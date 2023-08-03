import org.lwjgl.assimp.*;

import java.nio.IntBuffer;
import java.util.ArrayList;

import org.lwjgl.PointerBuffer;


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
                //createMyMeshFromAiMesh(mesh, ai_mesh);
                if (materials.capacity() > 1)
                {
                    AIMaterial mat = AIMaterial.create(materials.get(ai_mesh.mMaterialIndex()));
                    AIString path = AIString.calloc();
                    Assimp.aiGetMaterialTexture(mat, Assimp.aiTextureType_DIFFUSE, 0, path, (IntBuffer)null, null, null, null, null, null);
                    DMaterial myMat = new DMaterial();
                    MaterialRange range = new MaterialRange(offsetIndex, 0, myMat);
                    myMat.FindFileName(path);
                    int ret = createMatMeshFromAiMesh(mesh, ai_mesh, myMat, offsetVertex);
                    offsetVertex = offsetVertex + ai_mesh.mNumVertices();
                    offsetIndex = ret + offsetIndex;
                    range.end = offsetIndex-1;
                    mesh.materialList.add(range);
                } else {
                    createMyMeshFromAiMesh(mesh, ai_mesh);
                }
            } else {
                break;
            }
        }

    }

    public int createMatMeshFromAiMesh(Mesh mesh, AIMesh aiMesh, DMaterial mat, int offset)
    {
       /*  int numVertices = aiMesh.mNumVertices();
        AIVector3D.Buffer aiVertices = aiMesh.mVertices();
        AIVector3D.Buffer aiNormals = aiMesh.mNormals();
        AIVector3D.Buffer aiTex = aiMesh.mTextureCoords(0);

        System.out.println(numVertices);

        ArrayList<Integer> indices = new ArrayList<Integer>();
        ArrayList<Vector> vertices = new ArrayList<Vector>();
        ArrayList<Vector> texCoords = new ArrayList<Vector>();
        ArrayList<Vector> normals = new ArrayList<Vector>();

        for (int i = 0; i<numVertices; i++)
        {
            AIVector3D aiVertex = aiVertices.get();
            AIVector3D aiNormal = aiNormals.get();
            AIVector3D aiTexCoord = aiTex.get();
            //Vector pos = new Vector(aiVertex.x(), aiVertex.y(), aiVertex.z(), 1.0f);
            vertices.add(new Vector(aiVertex.x(), aiVertex.y(), aiVertex.z(), 1.0f));
            normals.add(new Vector(aiNormal.x(), aiNormal.y(), aiNormal.z(), 1.0f));
            texCoords.add(new Vector(aiTexCoord.x(), aiTexCoord.y(), 1.0f, 1.0f));


          //  mesh.vertices.get(i).DumpVector();
           // mesh.normals.get(i).DumpVector();
           // mesh.texCoords.get(i).DumpVector();
        }

        System.out.println(vertices.size());
        System.out.println(normals.size());
        System.out.println(texCoords.size());
        AIFace.Buffer faces = aiMesh.mFaces();
        for (int i = 0; i < aiMesh.mNumFaces(); i++)
	    {
		    AIFace face = faces.get();
            IntBuffer ai_indices = face.mIndices();
		    for (int j = 0; j < face.mNumIndices(); j++)
		    {
			    indices.add(ai_indices.get(j));
		    }
	    }



        System.out.println(indices.size());

        mesh.matVectors.put(mat, vertices);
        mesh.matNorms.put(mat, normals);
        mesh.matTex.put(mat, texCoords);
        mesh.matIndices.put(mat, indices); */

        System.out.println(mesh.meshName);

        int numVertices = aiMesh.mNumVertices();
        AIVector3D.Buffer aiVertices = aiMesh.mVertices();
        AIVector3D.Buffer aiNormals = aiMesh.mNormals();
        AIVector3D.Buffer aiTex = aiMesh.mTextureCoords(0);

        System.out.println(numVertices);

        for (int i = 0; i<numVertices; i++)
        {
            AIVector3D aiVertex = aiVertices.get();
            AIVector3D aiNormal = aiNormals.get();
            AIVector3D aiTexCoord = aiTex.get();
            //Vector pos = new Vector(aiVertex.x(), aiVertex.y(), aiVertex.z(), 1.0f);
            mesh.vertices.add(new Vector(aiVertex.x(), aiVertex.y(), aiVertex.z(), 1.0f));
            mesh.normals.add(new Vector(aiNormal.x(), aiNormal.y(), aiNormal.z(), 1.0f));
            mesh.texCoords.add(new Vector(aiTexCoord.x(), aiTexCoord.y(), 1.0f, 1.0f));


          //  mesh.vertices.get(i).DumpVector();
           // mesh.normals.get(i).DumpVector();
           // mesh.texCoords.get(i).DumpVector();
        }

        System.out.println(mesh.vertices.size());
        System.out.println(mesh.normals.size());
        System.out.println(mesh.texCoords.size());
        AIFace.Buffer faces = aiMesh.mFaces();
        for (int i = 0; i < aiMesh.mNumFaces(); i++)
	    {
		    AIFace face = faces.get();
            IntBuffer indices = face.mIndices();
		    for (int j = 0; j < face.mNumIndices(); j++)
		    {
			    mesh.indices.add(indices.get(j) + offset);
		    }
	    }
        int bonesNum = 0;
        System.out.println("Bones");
        if ((bonesNum = aiMesh.mNumBones()) != 0)
        {
            mesh.CreateAnimationStructures();
            for (int j = 0; j<aiMesh.mNumVertices(); j++) {
                mesh.weights.add(new Vector(0.0, 0.0, 0.0, 1.0f));
                mesh.bones.add(new VectorInt(-1, -1, -1, -1));
            }
            int boneCount = 0;

            PointerBuffer bones = aiMesh.mBones();

            for (int i = 0; i<bonesNum; i++)
            {
                AIBone bone = AIBone.create(bones.get(i));

                String name = bone.mName().toString();
                int boneID = FindJointIndexUsingName(name, mesh.joints);
                if (boneID == -1)
                {
                    AIMatrix4x4 offsetMat = bone.mOffsetMatrix();
                    Joint joint = new Joint(name, boneCount, Matrix.CreateFromAIMatrix(offsetMat));
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
            this.GetAnimationData(mesh);
            for (Vector weight : mesh.weights)
            {
                weight.DumpVector();
            }

            for(VectorInt bone : mesh.bones)
            {
                bone.DumpVector();
            }

        }



        System.out.println(mesh.indices.size());

        int ret;
        ret = (aiMesh.mNumFaces() * 3);

        return ret;

    }

    public void createMyMeshFromAiMesh(Mesh mesh, AIMesh aiMesh)
    {
        System.out.println(mesh.meshName);
        int numVertices = aiMesh.mNumVertices();
        AIVector3D.Buffer aiVertices = aiMesh.mVertices();
        AIVector3D.Buffer aiNormals = aiMesh.mNormals();
        AIVector3D.Buffer aiTex = aiMesh.mTextureCoords(0);

        System.out.println(numVertices);

        for (int i = 0; i<numVertices; i++)
        {
            AIVector3D aiVertex = aiVertices.get();
            AIVector3D aiNormal = aiNormals.get();
            AIVector3D aiTexCoord = aiTex.get();
            //Vector pos = new Vector(aiVertex.x(), aiVertex.y(), aiVertex.z(), 1.0f);
            mesh.vertices.add(new Vector(aiVertex.x(), aiVertex.y(), aiVertex.z(), 1.0f));
            mesh.normals.add(new Vector(aiNormal.x(), aiNormal.y(), aiNormal.z(), 1.0f));
            mesh.texCoords.add(new Vector(aiTexCoord.x(), aiTexCoord.y(), 1.0f, 1.0f));


          //  mesh.vertices.get(i).DumpVector();
           // mesh.normals.get(i).DumpVector();
           // mesh.texCoords.get(i).DumpVector();
        }

        System.out.println(mesh.vertices.size());
        System.out.println(mesh.normals.size());
        System.out.println(mesh.texCoords.size());
        AIFace.Buffer faces = aiMesh.mFaces();
        for (int i = 0; i < aiMesh.mNumFaces(); i++)
	    {
		    AIFace face = faces.get();
            IntBuffer indices = face.mIndices();
		    for (int j = 0; j < face.mNumIndices(); j++)
		    {
			    mesh.indices.add(indices.get(j));
		    }
	    }

        int bonesNum = 0;
        System.out.println("Bones");
        if ((bonesNum = aiMesh.mNumBones()) != 0)
        {
            mesh.CreateAnimationStructures();
            for (int j = 0; j<aiMesh.mNumVertices(); j++) {
                mesh.weights.add(new Vector(0.0, 0.0, 0.0, 0.0f));
                mesh.bones.add(new VectorInt(-1, -1, -1, -1));
            }
            int boneCount = 0;

            PointerBuffer bones = aiMesh.mBones();

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
            this.GetAnimationData(mesh);
           // for (Vector weight : mesh.weights)
            {
                //weight.DumpVector();
            }

            System.out.println("Weights count " + mesh.weights.size());

           // for(VectorInt bone : mesh.bones)
            {
               // bone.DumpVector();
            }

            System.out.println("Bones count " + mesh.bones.size());

        }

        System.out.println(mesh.indices.size());
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
        for (int i = 0; i<numOfAnims; i++)
        {
            AIAnimation anim = AIAnimation.create(this.ai_scene.mAnimations().get(i));
            AnimationData data = new AnimationData(anim.mName().dataString(),
                                            (float)anim.mTicksPerSecond(),
                                            (float)anim.mDuration());
            data.DumpAnimation();
            ReadHierarchyData(data.m_RootNode, this.ai_scene.mRootNode());
            GetAnimationSRT(anim, data, mesh);
            mesh.animData.add(data);
        }
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

    private void ReadHierarchyData(AnimationData.AssimpNodeData data, AINode src)
    {
        data.name = src.mName().dataString();
        data.transformation = Matrix.CreateFromAIMatrix(src.mTransformation());
        int count = src.mNumChildren();
        System.out.println(count);
        System.out.println(data.name);
        data.childrenCount = count;
        for (int i = 0; i<count; i++)
        {
            AnimationData.AssimpNodeData datum = new AnimationData.AssimpNodeData();
            ReadHierarchyData(datum, AINode.create(src.mChildren().get(i)));
            data.AddChildToNodeData(datum);
        }
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
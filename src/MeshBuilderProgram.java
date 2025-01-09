import java.util.Map;
import java.util.HashMap;
import java.util.ArrayList;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;

import org.lwjgl.PointerBuffer;
import org.lwjgl.assimp.*;

import org.apache.commons.lang3.SystemUtils;
import org.apache.commons.lang3.ObjectUtils.Null;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;

import java.nio.IntBuffer;
//import org.json.

class MeshJSONData {
    private String fileName;
    private String fileExt;
    private boolean cooked;

    private transient String filePath;

    public void setfileName(String _str) {
        this.fileName = _str;
    }

    public void setfileExt(String _str) {
        this.fileExt = _str;
    }

    public void setCooked(boolean _set) {
        this.cooked = _set;
    }

    public String getfileName() {
        return this.fileName;
    }

    public String getExtName() {
        return this.fileExt;
    }

    public boolean getCooked() {
        return this.cooked;
    }

    public void setfilePath(String _str) {
        this.filePath = _str;
    }

    public String getFilePath() {
        return this.filePath;
    }

}

public class MeshBuilderProgram {

    public String cwd, jsonfile;
    private final String fileSlash;
    private final String outputExt = ".bin";
    private final String inputDir;
    private final String outputDir;
    private MeshJSONData[] data;
   
    private ArrayList<Mesh> meshes = new ArrayList<Mesh>();
    private HashMap<String, MatrixTransform> fileTransforms = new HashMap<String, MatrixTransform>();
    private HashMap<String, MatrixTransform> meshTransforms = new HashMap<String, MatrixTransform>();
    private AssimpLoader loader;
    MeshBuilderProgram() {
        this.cwd = System.getProperty("user.dir");
        this.jsonfile = "";
        if (SystemUtils.IS_OS_WINDOWS)
        {
            fileSlash = "\\";
        } else {
            fileSlash = "/";
        }
        inputDir = fileSlash+"input"+fileSlash;
        outputDir = fileSlash+"generated"+fileSlash;
    }

    MeshBuilderProgram(String _file) {
        this.cwd = System.getProperty("user.dir");
        this.jsonfile = _file;
        //this.transformMethod = _run;
        if (SystemUtils.IS_OS_WINDOWS)
        {
            fileSlash = "\\";
        } else {
            fileSlash = "/";
        }
        inputDir = fileSlash+"input"+fileSlash;
        outputDir = fileSlash+"generated"+fileSlash;
    }

    MeshBuilderProgram(String _cwd, String _file) {
        this.cwd = _cwd;
        this.jsonfile = _file;
        if (SystemUtils.IS_OS_WINDOWS)
        {
            fileSlash = "\\";
        } else {
            fileSlash = "/";
        }
        inputDir = fileSlash+"input"+fileSlash;
        outputDir = fileSlash+"generated"+fileSlash;
    }

    MeshBuilderProgram(String _cwd, String _file, MatrixTransform transforms[], String exts[]) {
        this.cwd = _cwd;
        this.jsonfile = _file;
        setFileTransform(transforms, exts);
        if (SystemUtils.IS_OS_WINDOWS)
        {
            fileSlash = "\\";
        } else {
            fileSlash = "/";
        }
        inputDir = fileSlash+"input"+fileSlash;
        outputDir = fileSlash+"generated"+fileSlash;
    }

    public void setFileTransform(MatrixTransform transforms[], String exts[])
    {
        for (int i = 0; i<transforms.length; i++)
        {
            fileTransforms.putIfAbsent(exts[i], transforms[i]);
        }
    }

    public boolean IsTxtFile(String file) {
        return file.endsWith(".txt");
    }

    public String GetMeshName(String file)
    {
        String ret = "";
        int lastSlash = file.lastIndexOf(fileSlash);
        int dot = file.lastIndexOf(".");
        ret = file.substring(lastSlash+1, dot);
        System.out.println(ret);
        return ret;
    }

    public void createAssimpDataFromJSON()
    {
        loader = new AssimpLoader();
        for (MeshJSONData datum : data) {
            if (!(datum.getCooked())) {
                String modelPath = datum.getFilePath();
                System.out.println(modelPath);
                if (!(IsTxtFile(modelPath)))
                {
                    loader.initScene(modelPath);
                    PointerBuffer meshes = loader.getMeshesFromScene();
                    PointerBuffer materials = loader.getMaterialsFromScene();
                    HashMap<String, ArrayList<AssimpData>> assimpMeshes = new HashMap<String, ArrayList<AssimpData>>();
                
                    for (int i = 0; i < meshes.capacity(); i++) {

                        AIMesh impMesh = AIMesh.create(meshes.get(i));
                        
                        String name = impMesh.mName().dataString();

                        System.out.println(name);

                        meshTransforms.putIfAbsent(name, fileTransforms.get(datum.getExtName()));

                        AIMaterial material = null;

                        int index = -1;

                        if ((index = impMesh.mMaterialIndex()) >= 0) {
                            material = AIMaterial.create(materials.get(index));
                        }

                        AssimpData assData = new AssimpData(impMesh, material);

                        assimpMeshes.putIfAbsent(name, new ArrayList<AssimpData>()); 

                        assimpMeshes.get(name).add(assData);
                    }
                    createMeshObjectFromAssimp(assimpMeshes);
                } else {
                    String name = GetMeshName(modelPath);
                    Mesh mesh = new Mesh(name);
                    mesh.loadData(modelPath);
                    meshes.add(mesh);
                }
            }
        }
    }


    public void createMeshObjectFromAssimp(HashMap<String, ArrayList<AssimpData>> assimpMeshes)
    { 
        for (Map.Entry<String,ArrayList<AssimpData>> element : assimpMeshes.entrySet())
        {
            String name = element.getKey();
            ArrayList<AssimpData> data = element.getValue();
            Mesh mesh = new Mesh(name);

            if (data.size() > 1)
            {
                int offsetIndex = 0;
                int offsetVertex = 0;
                for (int i = 0; i<data.size(); i++)
                {
                    AssimpData datum = data.get(i);
                    loader.ai_mesh = datum.inputMesh;
                    AIMaterial mat = datum.inputMaterial;
                    AIString path = AIString.calloc();
                    Assimp.aiGetMaterialTexture(mat, Assimp.aiTextureType_DIFFUSE, 0, path, (IntBuffer)null, null, null, null, null, null);
                    DMaterial myMat = new DMaterial();
                    myMat.FindFileName(path);
                    MaterialRange range = new MaterialRange(offsetIndex, 0, myMat);
                    int ret = loader.createMatMeshFromAiMesh(mesh, myMat, offsetVertex);
                    offsetVertex = offsetVertex + loader.ai_mesh.mNumVertices();
                    offsetIndex = ret + offsetIndex;
                    range.end = offsetIndex-1;
                    mesh.materialList.add(range);
                }
            } else {
                loader.ai_mesh = data.get(0).inputMesh;
                loader.createMyMeshFromAiMesh(mesh);
            }
            meshes.add(mesh);
        }
    }

    public void writeMeshesToFile()
    {
        HashMap<String, Integer> sameMeshNames = new HashMap<String, Integer>();
        for (Mesh mesh : meshes)
        {
            mesh.byIndices();
            
            MatrixTransform trans = meshTransforms.get(mesh.meshName);

            if (trans != null)
            {
                try {
                    trans.setMesh(mesh);
                    trans.run();
                    trans.wait();
                } 
                catch (InterruptedException e)
                {
                    System.err.println(e.getMessage());
                    e.printStackTrace();
                }
            }

            String name = mesh.meshName;

            sameMeshNames.putIfAbsent(name, 1);

            int size = 1;

            if ((size = sameMeshNames.get(name))> 1)
                name += "_00" + Integer.toString(size);

            String output = cwd + outputDir + name + outputExt;

            mesh.WriteMeshToBinFile(output);
        }
    }

    public void cookedAndWriteJSON()
    {
        for (MeshJSONData datum : data)
        {
            if (!(datum.getCooked()))
            {
                datum.setCooked(true);
            }
        }

        Gson gson = new GsonBuilder().setPrettyPrinting().create();

        try
        {
            FileWriter writer = new FileWriter(this.jsonfile);
            gson.toJson(data, writer);
            writer.close();
        } catch (IOException e) {
            System.err.println(e.getMessage());
            e.printStackTrace();
        }

    }

    public void loadJson() {

        Gson gson = new Gson();
        try {
            data = gson.fromJson(new FileReader(this.jsonfile), MeshJSONData[].class);
            for (MeshJSONData datum : data) {
                if (!(datum.getCooked())) {
                    String modelPath = cwd + inputDir + datum.getfileName() + datum.getExtName();
                    datum.setfilePath(modelPath);
                    System.out.println(datum.getFilePath());
                }
            }
        } catch (FileNotFoundException e) {
            System.err.println(e.getMessage());
            e.printStackTrace();
        }
    }

    public void RunProgram()
    {
        loadJson();

        createAssimpDataFromJSON();

        writeMeshesToFile();

        cookedAndWriteJSON();
    }
}

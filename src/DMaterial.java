
import org.lwjgl.assimp.*;
import org.apache.commons.lang3.SystemUtils;;
public class DMaterial {
    String fileName;
    String fileSlash = "/";
    DMaterial()
    {
        this.fileName = "";
        if (SystemUtils.IS_OS_WINDOWS)
        {
            fileSlash = "\\";
        }
    }

    public void FindFileName(AIString path)
    {
        
        String analysis = path.dataString();
        int index = analysis.lastIndexOf(fileSlash);
        if (index < analysis.length() && index >= 0)
        {
            this.fileName = CreatePS2Path(analysis.substring(index+1, analysis.length()));
            System.out.println(this.fileName);
        } else {
            System.out.println("No File name for material" + analysis);
            return;
        }
       return;
    }

    public String CreatePS2Path(String file)
    {
        return file.toUpperCase();
    }
}

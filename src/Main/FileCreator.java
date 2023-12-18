package Main;

import java.io.File;
import java.io.IOException;
import java.nio.charset.StandardCharsets;

import java.io.FileOutputStream;
import java.io.OutputStreamWriter;
import java.io.Writer;


public class FileCreator{
    /**
     * This method creates a new file located in the specified path
     *
     * @param path the path to the created file
     * @throws IOException the exception thrown when a problem occurs during the file creation
     */
    public void createFile(String path) throws IOException{
        File f = new File(path);
        f.createNewFile();
    }


    /**
     * This method writes the content specified as second parameter
     * in the file located in the path specified as first parameter
     *
     * @param path the path of the file to write in
     * @param content the content to write in the file
     * @throws IOException the exception thrown when a problem occurs during the writing
     */

    public void setContent(String path, String content) throws IOException {
    try (Writer writer = new OutputStreamWriter(new FileOutputStream(path), StandardCharsets.UTF_8)) {
        writer.write(content);
    }
}


}

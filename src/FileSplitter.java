/*
 * Class name	:	FileSplitter.java
 * Description	:	This class defines the file processing for the application. The class is going to be responsible
 *                  for forming the split parts for the file to be downloaded.
 * Institution	:	University of Florida
 * Reference    :   This code is taken from :http://www.admios.com/blog/how-to-split-a-file-using-java
 * Credits      :   ARISTIDES MELENDEZ
 */

import java.io.File;
import java.io.IOException;
import java.io.RandomAccessFile;
import java.nio.channels.FileChannel;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.stream.Stream;
import java.util.ArrayList;
import java.util.List;

public class FileSplitter {

    private static String dir = System.getProperty("user.dir");
    private static final String suffix = ".splitPart";
    public static Stream<String> convertFileToStream(String location) throws IOException {
        return Files.lines(Paths.get(location));
    }

    public static void convertStreamToFile(Stream<String> data, Path path) throws IOException {
        Files.write(path, (Iterable<String>) data::iterator);
    }

    /**
     * Split a file into multiples files.
     *
     * @param fileName   Name of file to be split.
     * @param mBperSplit maximum number of MB per file.
     * @throws IOException
     */
    public static List<Path> splitFile(final String fileName, final int mBperSplit, int peerID) throws IOException {

        dir = dir + File.separator + "peer_" + peerID;
        if (mBperSplit <= 0) {
            throw new IllegalArgumentException("mBperSplit must be more than zero");
        }

        List<Path> partFiles = new ArrayList<>();
        final long sourceSize = Files.size(Paths.get(fileName));

        final long bytesPerSplit = mBperSplit;

        final long numSplits = sourceSize / bytesPerSplit;

        final long remainingBytes = sourceSize % bytesPerSplit;
        
        
        int position = 0;

        try (RandomAccessFile sourceFile = new RandomAccessFile(fileName, "r");
             FileChannel sourceChannel = sourceFile.getChannel()) {

            for (; position < numSplits; position++) 
            {
                writePartToFile(bytesPerSplit, position * bytesPerSplit, sourceChannel, partFiles, position);
            }

            if (remainingBytes > 0) 
            {
                writePartToFile(remainingBytes, position * bytesPerSplit, sourceChannel, partFiles, position);
            }
        }
       
        return partFiles;
    }


    /**
     * write the split into  file.
     *
     * @param byteSize   Name of file to be split.
     * @param position maximum number of MB per file.
     * @param sourceChannel 
     * @param partFiles
     * @param count
     * @throws IOException
     */
    private static void writePartToFile(long byteSize, long position, FileChannel sourceChannel, List<Path> partFiles, int count) throws IOException {
        Path fileName = Paths.get(dir + File.separator + count + suffix);
        try (RandomAccessFile toFile = new RandomAccessFile(fileName.toFile(), "rw");
             FileChannel toChannel = toFile.getChannel()) {
            sourceChannel.position(position);
            toChannel.transferFrom(sourceChannel, 0, byteSize);
        }
        partFiles.add(fileName);
    }

}



package software.amazon.event.ruler;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.HdrHistogram.Histogram;

import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.nio.charset.StandardCharsets;
import java.nio.file.FileSystems;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Arrays;
import java.util.Base64;
import java.util.HashMap;
import java.util.Map;

public class HistogramUtils {

    private static String displayTemplate;
    private static ObjectMapper mapper;

    public static String getHistogramDisplayTemplate() throws IOException {
        if (displayTemplate == null) {
            displayTemplate = new String(Files.readAllBytes(Paths.get("src/test/data/histogram_template.html")), StandardCharsets.UTF_8);
            mapper = new ObjectMapper();
        }
        return displayTemplate;
    }

    public static void saveHistogramDisplay(String data, Path path) throws IOException {

        Map<String, Object> displayInput = new HashMap<>();

        displayInput.put("sample", data);

        String rendered = getHistogramDisplayTemplate().replace("<<PLACEHOLDER>>", mapper.writeValueAsString(displayInput));

        Files.write(path, rendered.getBytes());

    }

    public static void saveHistogramComparison(String baseLine, String data, Path path) throws IOException {

        Map<String, Object> displayInput = new HashMap<>();

        displayInput.put("sample", data);
        displayInput.put("baseLine", baseLine);

        String rendered = getHistogramDisplayTemplate().replace("<<PLACEHOLDER>>", mapper.writeValueAsString(displayInput));

        Files.write(path, rendered.getBytes());

    }

    public static void saveHistogram(Histogram h, String name) throws IOException {

        if (h == null) {
            return;
        }

        String folder = "output/histograms";

        String baseLineFolder = "./../baseline/output/histograms";

        Files.createDirectories(Paths.get(folder));

        ByteBuffer targetBuffer = ByteBuffer.allocate(h.getNeededByteBufferCapacity()).order(ByteOrder.BIG_ENDIAN);

        int compressedLength = h.encodeIntoCompressedByteBuffer(targetBuffer, 9);

        byte[] compressedArray = Arrays.copyOf(targetBuffer.array(), compressedLength);

        String encodedString = Base64.getEncoder().encodeToString(compressedArray);

        Files.write(Paths.get(folder, name + "_data.hdr"), encodedString.getBytes());

        saveHistogramDisplay(encodedString, Paths.get(folder, name + ".html"));

        if (Files.isDirectory(Paths.get(baseLineFolder))) {
            Path baseLineDataPath = Paths.get(baseLineFolder, name + "_data.hdr");
            if (Files.exists(baseLineDataPath)) {
                String baselineBase64 = new String(Files.readAllBytes(baseLineDataPath), StandardCharsets.UTF_8);
                saveHistogramComparison(baselineBase64, encodedString, Paths.get(folder, name + "_vs_baseline.html"));
            }
        }

    }

}

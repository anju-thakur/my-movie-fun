package org.superbiz.moviefun.albums;

import org.apache.tika.Tika;
import org.apache.tika.io.IOUtils;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import org.superbiz.moviefun.blob.Blob;
import org.superbiz.moviefun.blob.BlobStore;

import java.io.*;
import java.net.URISyntaxException;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Map;
import java.util.Optional;

import static java.lang.String.format;
import static java.nio.file.Files.readAllBytes;
import static org.springframework.http.MediaType.IMAGE_JPEG_VALUE;

@Controller
@RequestMapping("/albums")
public class AlbumsController {

    private final AlbumsBean albumsBean;
    private final BlobStore blobStore;

    public AlbumsController(AlbumsBean albumsBean, BlobStore blobstore) {
        this.albumsBean = albumsBean;
        this.blobStore = blobstore;
    }


    @GetMapping
    public String index(Map<String, Object> model) {
        model.put("albums", albumsBean.getAlbums());
        return "albums";
    }

    @GetMapping("/{albumId}")
    public String details(@PathVariable long albumId, Map<String, Object> model) {
        model.put("album", albumsBean.find(albumId));
        return "albumDetails";
    }

    @PostMapping("/{albumId}/cover")
    public String uploadCover(@PathVariable long albumId, @RequestParam("file") MultipartFile uploadedFile) throws IOException {
        //saveUploadToFile(uploadedFile, getCoverFile(albumId));

        //String contentType = new Tika().detect(uploadedFile.getInputStream());

        Blob file = new Blob(format("covers/%d",albumId) ,uploadedFile.getInputStream(), uploadedFile.getContentType());
        blobStore.put(file);


        return format("redirect:/albums/%d", albumId);
    }

    @GetMapping("/{albumId}/cover")
    public HttpEntity<byte[]> getCover(@PathVariable long albumId) throws IOException, URISyntaxException {

/*Path coverFilePath = getExistingCoverPath(albumId);
        byte[] imageBytes = readAllBytes(coverFilePath);
        HttpHeaders headers = createImageHttpHeaders(coverFilePath, imageBytes);*/


        Optional<Blob> img = ((BlobStore) blobStore).get(format("covers/%d",albumId));
        //Blob image = img.get();
        Blob image = img.orElse(buildDefaultCoverBlob());
        byte[] imageBytes = IOUtils.toByteArray(image.getInputStream());

        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.parseMediaType(image.getContentType()));
        headers.setContentLength(imageBytes.length);


        return new HttpEntity<>(imageBytes, headers);
    }


    private void saveUploadToFile(@RequestParam("file") MultipartFile uploadedFile, File targetFile) throws IOException {
        targetFile.delete();
        targetFile.getParentFile().mkdirs();
        targetFile.createNewFile();

        try (FileOutputStream outputStream = new FileOutputStream(targetFile)) {
            outputStream.write(uploadedFile.getBytes());
        }
    }

    private HttpHeaders createImageHttpHeaders(Path coverFilePath, byte[] imageBytes) throws IOException {
        String contentType = new Tika().detect(coverFilePath);

        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.parseMediaType(contentType));
        headers.setContentLength(imageBytes.length);
        return headers;
    }

    private File getCoverFile(@PathVariable long albumId) {
        String coverFileName = format("covers/%d", albumId);
        return new File(coverFileName);
    }

    private Path getExistingCoverPath(@PathVariable long albumId) throws URISyntaxException {
        File coverFile = getCoverFile(albumId);
        Path coverFilePath;

        if (coverFile.exists()) {
            coverFilePath = coverFile.toPath();
        } else {
            coverFilePath = Paths.get(this.getClass().getClassLoader().getResource("default-cover.jpg").toURI());
        }

        return coverFilePath;
    }

    private Blob buildDefaultCoverBlob() {
        ClassLoader classLoader = getClass().getClassLoader();
        //InputStream input = classLoader.getResourceAsStream("default-cover.jpg");
        InputStream input = this.getClass().getClassLoader().getResourceAsStream("default-cover.jpg");

        return new Blob("default-cover", input, IMAGE_JPEG_VALUE);
    }

    @GetMapping("/deleteall")
    public String deleteall()
    {
        blobStore.deleteAll();
        return "albums";
    }

}

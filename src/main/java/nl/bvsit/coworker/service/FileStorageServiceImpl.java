package nl.bvsit.coworker.service;

import nl.bvsit.coworker.controller.FileStorageController;
import nl.bvsit.coworker.exceptions.BadRequestException;
import nl.bvsit.coworker.payload.FileInfo;
import nl.bvsit.coworker.payload.response.MessageResponse;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.io.Resource;
import org.springframework.core.io.UrlResource;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Service;
import org.springframework.util.FileSystemUtils;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.servlet.mvc.method.annotation.MvcUriComponentsBuilder;

import java.io.File;
import java.io.IOException;
import java.net.MalformedURLException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.Stream;

//By courtesy of https://bezkoder.com/spring-boot-file-upload
@Service
public class FileStorageServiceImpl implements FileStorageService {

  @Value("${app.upload.dir:uploads}")
  private String storageDir;
  private Path root;

  public String init() {
    //Initialize upload folder
    Path currentPath = Paths.get(".").toAbsolutePath().normalize();
    root = currentPath.resolve(storageDir);
    if (!Files.exists(root)) {
      try {
        root = Files.createDirectory(root);
      } catch (IOException e) {
        throw new RuntimeException("Could not initialize folder for upload!");
      }
    }
    System.out.println("Upload folder path = " + root);
    return root.toString();
  }

  public String getStorageLocation() {
    if (root == null) return init();
    return root.toString();
  }

  @Override
    public ResponseEntity<MessageResponse> save(MultipartFile multipartFile){
        try{
            multipartFile.transferTo(new File( getStorageLocation(), multipartFile.getOriginalFilename())) ;
        }
        catch(IOException exc){
            throw new BadRequestException("Error: could not upload file");
        }
        return ResponseEntity.ok(new MessageResponse("File uploaded successfully!"));
    }

  @PreAuthorize("hasRole('ROLE_MANAGER') or hasRole('ROLE_ADMIN')")
  public ResponseEntity<MessageResponse> saveAuthenticated(MultipartFile multipartFile) {
    return save(multipartFile);
  }

  @Override
  public Resource load(String filename) {
    try {
      Path file = root.resolve(filename);
      Resource resource = new UrlResource(file.toUri());

      if (resource.exists() || resource.isReadable()) {
        return resource;
      } else {
        throw new RuntimeException("Could not read the file!");
      }
    } catch (MalformedURLException e) {
      throw new RuntimeException("Error: " + e.getMessage());
    }
  }

  @Override
  public void deleteAll() {
    FileSystemUtils.deleteRecursively(root.toFile());
  }

  @Override
  public Stream<Path> loadAll() {
    try {
      return Files.walk(this.root, 1).filter(path -> !path.equals(this.root)).map(this.root::relativize);
    } catch (IOException e) {
      throw new RuntimeException("Could not load the files!");
    }
  }

  public List<FileInfo> getListFiles(){
    List<FileInfo> fileInfos = loadAll().map(path -> {
      String filename = path.getFileName().toString();
      String url = MvcUriComponentsBuilder
              .fromMethodName(FileStorageController.class, "getFile", path.getFileName().toString()).build().toString();
      return new FileInfo(filename, url);
    }).collect(Collectors.toList());
    return fileInfos;
  }

}

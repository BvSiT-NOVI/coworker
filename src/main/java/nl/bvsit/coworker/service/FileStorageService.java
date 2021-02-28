package nl.bvsit.coworker.service;

import nl.bvsit.coworker.payload.FileInfo;
import nl.bvsit.coworker.payload.response.MessageResponse;
import org.springframework.core.io.Resource;
import org.springframework.http.ResponseEntity;
import org.springframework.web.multipart.MultipartFile;

import java.nio.file.Path;
import java.util.List;
import java.util.stream.Stream;

public interface FileStorageService {
  public String init();

  public String getStorageLocation();

  public ResponseEntity<MessageResponse> save(MultipartFile file);

  public ResponseEntity<MessageResponse> saveAuthenticated(MultipartFile multipartFile);

  public Resource load(String filename);

  public void deleteAll();

  public Stream<Path> loadAll();

  public List<FileInfo> getListFiles();

}

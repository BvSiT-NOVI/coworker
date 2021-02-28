package nl.bvsit.coworker.controller;

import nl.bvsit.coworker.payload.FileInfo;
import nl.bvsit.coworker.payload.response.MessageResponse;
import nl.bvsit.coworker.service.FileStorageService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.io.Resource;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;

@RequestMapping("/api")
@RestController
public class FileStorageController {

    @Autowired
    FileStorageService fileStorageService;

    @PostMapping("/upload")
    @ResponseBody
    public ResponseEntity<MessageResponse> uploadFile(@RequestParam("file") MultipartFile multipartFile) {
        return fileStorageService.saveAuthenticated(multipartFile);
    }

    @GetMapping("/uploads")
    public ResponseEntity<List<FileInfo>> getListFiles() {
        return ResponseEntity.status(HttpStatus.OK).body(fileStorageService.getListFiles());
    }

    @GetMapping("/uploads/{filename:.+}")
    public ResponseEntity<Resource> getFile(@PathVariable String filename) {
        Resource file = fileStorageService.load(filename);
        return ResponseEntity.ok()
                .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=\"" + file.getFilename() + "\"").body(file);
    }

}

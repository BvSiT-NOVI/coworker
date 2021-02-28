package nl.bvsit.coworker.controller;

import nl.bvsit.coworker.domain.CpMenuItem;
import nl.bvsit.coworker.exceptions.TestException;
import nl.bvsit.coworker.payload.CpMenuItemDTO;
import nl.bvsit.coworker.service.CpMenuItemService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.support.ServletUriComponentsBuilder;

import java.net.URI;
import java.util.List;

@RequestMapping("/api/menuitems")
@RestController
public class CpMenuItemController {

    @Autowired
    CpMenuItemService cpMenuItemService;

    @PostMapping
    public ResponseEntity<CpMenuItemDTO> create(@RequestBody CpMenuItemDTO cpMenuItemDTO) {
        CpMenuItemDTO responseCpMenuItemDTO = cpMenuItemService.createCpMenuItem(cpMenuItemDTO);
        URI location = ServletUriComponentsBuilder.fromCurrentRequest().path("/{id}")
                .buildAndExpand(responseCpMenuItemDTO.getId()).toUri();
        return ResponseEntity.created(location).body(responseCpMenuItemDTO);
    }

    @PutMapping("/{id}")
    public ResponseEntity<CpMenuItemDTO> update(@PathVariable Long id, @RequestBody CpMenuItemDTO cpMenuItemDTO) {
        CpMenuItemDTO responseDTO = cpMenuItemService.updateCpMenuItem(id,cpMenuItemDTO);
        return ResponseEntity.ok(responseDTO);
    }    

    @DeleteMapping("/{id}")
    public ResponseEntity<CpMenuItem> delete(@PathVariable Long id) {
        cpMenuItemService.deleteCpMenuItem(id);
        return ResponseEntity.noContent().build();
    }

    @GetMapping("/{id}")
    public ResponseEntity<CpMenuItemDTO> getById(@PathVariable Long id) {
        return ResponseEntity.ok(cpMenuItemService.getCpMenuItemDTO(id));
    }

    @GetMapping
    public ResponseEntity<Page<CpMenuItemDTO>> getAll(Pageable pageable) {
        return ResponseEntity.ok(cpMenuItemService.getAllAsDTO(pageable));
    }

    @GetMapping("/test")
    public List<CpMenuItem> getAll() {
        return cpMenuItemService.getAll();
    }

    @GetMapping("/test2")
    public ResponseEntity<?> test() {
        if (true) throw new TestException(3L);
        return  ResponseEntity.ok("test");
    }

}

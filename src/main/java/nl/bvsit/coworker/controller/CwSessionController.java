package nl.bvsit.coworker.controller;

import nl.bvsit.coworker.domain.CwSession;
import nl.bvsit.coworker.payload.CwSessionDTO;
import nl.bvsit.coworker.service.CwSessionService;
import nl.bvsit.coworker.service.UserDetailsImpl;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.support.ServletUriComponentsBuilder;

import java.net.URI;

@RequestMapping("/api/sessions")
@RestController
public class CwSessionController {

    @Autowired
    CwSessionService cwSessionService;

    @PostMapping
    public ResponseEntity<CwSessionDTO> create(@RequestBody CwSessionDTO cwSessionDTO,@AuthenticationPrincipal UserDetailsImpl authUser) {
        CwSessionDTO responseCwSessionDTO = cwSessionService.createCwSessionAuthenticated(cwSessionDTO, authUser);
        URI location = ServletUriComponentsBuilder.fromCurrentRequest()
                .buildAndExpand(responseCwSessionDTO.getId()).toUri();
        return ResponseEntity.created(location).body(responseCwSessionDTO);
    }

    @PutMapping("/{id}/end")
    public ResponseEntity<CwSessionDTO> endCwSession(@PathVariable Long id, @AuthenticationPrincipal UserDetailsImpl authUser){
        URI location = ServletUriComponentsBuilder.fromCurrentRequest().path("/{id}")
                .buildAndExpand(id).toUri();
        return ResponseEntity.created(location).body(cwSessionService.endCwSessionAuthenticated(id,authUser));
    }

    @PutMapping("/{id}/paid")
    public ResponseEntity<CwSessionDTO> isPaidCwSession(@PathVariable Long id,@AuthenticationPrincipal UserDetailsImpl authUser){
        URI location = ServletUriComponentsBuilder.fromCurrentRequest().path("/{id}/paid")
                .buildAndExpand(id).toUri();
        return ResponseEntity.created(location).body(cwSessionService.setAsPaidCwSessionAuthenticated(id,authUser));
    }

    @PutMapping("/{id}/close")
    public ResponseEntity<CwSessionDTO> closeCwSession(@PathVariable Long id){
        URI location = ServletUriComponentsBuilder.fromCurrentRequest().path("/{id}")
                .buildAndExpand(id).toUri();
        return ResponseEntity.created(location).body(cwSessionService.closeCwSession(id));
    }

    @GetMapping("/{id}")
    public ResponseEntity<CwSessionDTO> getById(@PathVariable Long id,@AuthenticationPrincipal UserDetailsImpl authUser) {
        return ResponseEntity.ok(cwSessionService.getCwSessionAuthenticated(id,authUser));
    }

    @GetMapping
    public ResponseEntity<Page<CwSessionDTO>> getAll(Pageable pageable,@AuthenticationPrincipal UserDetailsImpl authUser) {
        return ResponseEntity.ok(cwSessionService.getAllAuthenticated(pageable,authUser));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<CwSession> delete(@PathVariable Long id) {
        cwSessionService.deleteCwSession(id);
        return ResponseEntity.noContent().build();
    }
}


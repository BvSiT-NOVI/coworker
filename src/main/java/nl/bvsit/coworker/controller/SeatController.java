package nl.bvsit.coworker.controller;

import nl.bvsit.coworker.domain.Seat;
import nl.bvsit.coworker.payload.SeatDTO;
import nl.bvsit.coworker.service.SeatService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.support.ServletUriComponentsBuilder;

import java.net.URI;
import java.util.List;

@RequestMapping("/api/seats")
@RestController
public class SeatController {

    @Autowired
    SeatService seatService;

    @PostMapping
    public ResponseEntity<SeatDTO> create(@RequestBody SeatDTO seatDTO) {
        SeatDTO responseSeatDTO = seatService.createSeat(seatDTO);
        URI location = ServletUriComponentsBuilder.fromCurrentRequest().path("/{id}")
                .buildAndExpand(responseSeatDTO.getId()).toUri();
        return ResponseEntity.created(location).body(responseSeatDTO);
    }

    @PutMapping("/{id}")
    public ResponseEntity<SeatDTO> update(@PathVariable Long id,@RequestBody SeatDTO seatDTO) {
        SeatDTO responseDTO = seatService.updateSeat(id,seatDTO);
        return ResponseEntity.ok(responseDTO);
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Seat> delete(@PathVariable Long id) {
        seatService.deleteSeat(id);
        return ResponseEntity.noContent().build();
    }

    @GetMapping("/{id}")
    public ResponseEntity<SeatDTO> getById(@PathVariable Long id) {
        return ResponseEntity.ok(seatService.getSeatDTO(id));
    }

    @GetMapping
    public ResponseEntity<List<SeatDTO>> getAll() {
        return ResponseEntity.ok(seatService.getAll());
    }

}

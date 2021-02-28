package nl.bvsit.coworker.service;

import nl.bvsit.coworker.domain.Seat;
import nl.bvsit.coworker.exceptions.*;
import nl.bvsit.coworker.repository.SeatRepository;
import nl.bvsit.coworker.payload.SeatDTO;
import org.modelmapper.ModelMapper;
import org.modelmapper.convention.MatchingStrategies;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Service;
import org.springframework.validation.annotation.Validated;

import javax.validation.Valid;
import java.util.List;
import java.util.stream.Collectors;

@Validated
@Service
public class SeatService {
    @Autowired
    private SeatRepository seatRepository;

    private ModelMapper modelMapper;
    @Autowired
    public void setModelMapper(ModelMapper modelMapper) { //NB See @Bean in DemoApplication
        this.modelMapper = modelMapper;
        this.modelMapper.getConfiguration().setMatchingStrategy(MatchingStrategies.LOOSE);
    }

    @PreAuthorize("hasAnyRole('ROLE_ADMIN','ROLE_MANAGER')")
    public SeatDTO createSeat(@Valid SeatDTO seatDTO){
        if (seatDTO.getId()!=null) throw new BadRequestException();
        if (Boolean.TRUE.equals(seatRepository.existsByCode(seatDTO.getCode()))) {
            throw new NotUniqueException("Code is not unique");
        }
        return toDto ( seatRepository.save(modelMapper.map(seatDTO,Seat.class)) );
    }

    @PreAuthorize("hasAnyRole('ROLE_ADMIN','ROLE_MANAGER')")
    public void deleteSeat(Long id){
        Seat seat = seatRepository.findById(id).orElseThrow(RecordNotFoundException::new);
        if (seat.getCwSessions().size()>0) throw new DeleteRecordException("Seat is in use and can not be deleted.");
        seatRepository.delete( seat );
    }

    @PreAuthorize("hasAnyRole('ROLE_ADMIN','ROLE_MANAGER')")
    public SeatDTO updateSeat(Long id,SeatDTO seatDTO){
        if (seatDTO.getId()!=null) throw new BadRequestException();
        Seat existingSeat = seatRepository.findById(id).orElseThrow(RecordNotFoundException::new);
        if (existingSeat.getCwSessions().size()>0) throw new UpdateException("Seat is in use and can not be updated.");
        Seat other = seatRepository.findByCode(seatDTO.getCode()).orElse(null);
        if (other!=null && other.getId()!= existingSeat.getId()){
            throw new NotUniqueException("Code exists already");
        }
        seatDTO.setId(existingSeat.getId());//!
        return toDto ( seatRepository.save(modelMapper.map(seatDTO,Seat.class)) );
    }

    public SeatDTO getSeatDTO(long id) {
        return toDto(seatRepository.findById(id).orElseThrow(RecordNotFoundException::new));
    }

    public List<SeatDTO> getAll() {
        List<Seat> seatList =  seatRepository.findAll();
        if (seatList.size()==0) throw new RecordNotFoundException();
        return  seatList.stream().map(this::toDto).collect(Collectors.toList());
    }

    public SeatDTO toDto(Seat seat){
        return modelMapper.map(seat, SeatDTO.class);
    }

}

package nl.bvsit.coworker.service;

import nl.bvsit.coworker.domain.CpMenuItem;
import nl.bvsit.coworker.exceptions.*;
import nl.bvsit.coworker.payload.CpMenuItemDTO;
import nl.bvsit.coworker.repository.CpMenuItemRepository;
import org.modelmapper.ModelMapper;
import org.modelmapper.convention.MatchingStrategies;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.validation.annotation.Validated;

import javax.validation.Valid;
import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;

@Validated
@Service
public class CpMenuItemService {
    @Autowired
    private CpMenuItemRepository cpMenuItemRepository;

    private ModelMapper modelMapper;
    @Autowired
    public void setModelMapper(ModelMapper modelMapper) { //NB See @Bean in DemoApplication
        this.modelMapper = modelMapper;
        this.modelMapper.getConfiguration().setMatchingStrategy(MatchingStrategies.LOOSE);
    }

    public CpMenuItemDTO createCpMenuItem(@Valid CpMenuItemDTO cpMenuItemDTO){
        if (cpMenuItemDTO.getId()!=null) throw new BadRequestException();
        if (Boolean.TRUE.equals(cpMenuItemRepository.existsByName(cpMenuItemDTO.getName()))) {
            throw new NotUniqueException("Name is not unique");
        }
        return toDto ( cpMenuItemRepository.save(modelMapper.map(cpMenuItemDTO,CpMenuItem.class)) );
    }

    public void deleteCpMenuItem(Long id){
        CpMenuItem cpMenuItem = cpMenuItemRepository.findById(id).orElseThrow(RecordNotFoundException::new);
        if (cpMenuItem.getOrderItems().size()>0) throw new DeleteRecordException("Menu item is in use and can not be deleted.");
        cpMenuItemRepository.delete( cpMenuItem );
    }

    public CpMenuItemDTO updateCpMenuItem(Long id, CpMenuItemDTO cpMenuItemDTO){
        if (cpMenuItemDTO.getId()!=null) throw new BadRequestException();
        CpMenuItem existingCpMenuItem = cpMenuItemRepository.findById(id).orElseThrow(RecordNotFoundException::new);
        if (existingCpMenuItem.getOrderItems().size()>0) throw new UpdateException("Menu item is in use and can not be updated.");
        CpMenuItem other = cpMenuItemRepository.findByName(cpMenuItemDTO.getName()).orElse(null);
        if (other == null || Objects.equals(other.getId(), existingCpMenuItem.getId())) {
            cpMenuItemDTO.setId(existingCpMenuItem.getId());//!
            return toDto(cpMenuItemRepository.save(modelMapper.map(cpMenuItemDTO, CpMenuItem.class)));
        } else {
            throw new NotUniqueException("Name exists already");
        }
    }

    public CpMenuItemDTO getCpMenuItemDTO(long id) {
        return toDto(cpMenuItemRepository.findById(id).orElseThrow(RecordNotFoundException::new));
    }

    public Page<CpMenuItemDTO> getAllAsDTO(Pageable pageable) {
        List<CpMenuItem> cpMenuItemList =  cpMenuItemRepository.findAll();
        if (cpMenuItemList.size()==0) throw new RecordNotFoundException();
        List<CpMenuItemDTO> cpMenutemDTOList=  cpMenuItemList.stream().map(this::toDto).collect(Collectors.toList());
        return new PageImpl<>(cpMenutemDTOList, pageable, cpMenutemDTOList.size());
    }

    public  List<CpMenuItem> getAll() {
        return  cpMenuItemRepository.findAll();
    }

    public CpMenuItemDTO toDto(CpMenuItem cpMenuItem){
        return modelMapper.map(cpMenuItem, CpMenuItemDTO.class);
    }
}

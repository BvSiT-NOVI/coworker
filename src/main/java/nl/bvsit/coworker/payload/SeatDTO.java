package nl.bvsit.coworker.payload;

import javax.validation.constraints.Min;
import javax.validation.constraints.Size;

public class SeatDTO {
    @Min(value = 1)
    private Long id;
    @Size(min=1,max=10)
    private String code;
    private String description;

    //constructors
    public SeatDTO() {
    }

    public SeatDTO(String code, String description) {
        this.code = code;
        this.description = description;
    }

    //getters and setters
    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getCode() {
        return code;
    }

    public void setCode(String code) {
        this.code = code;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }
}

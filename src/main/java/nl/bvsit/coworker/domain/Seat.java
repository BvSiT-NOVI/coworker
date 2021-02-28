package nl.bvsit.coworker.domain;
import com.fasterxml.jackson.annotation.JsonIgnore;

import javax.persistence.*;
import java.util.HashSet;
import java.util.Set;

@Entity
@Table(name = "cp_seat",uniqueConstraints =
            @UniqueConstraint(name="unique_code", columnNames={"code"}) )
public class Seat {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    @Column(length = 10)
    private String code;
    private String description;

    @JsonIgnore
    @OneToMany( mappedBy = "seat",cascade = CascadeType.ALL, orphanRemoval = true,fetch=FetchType.LAZY )
    Set<CwSession> cwSessions = new HashSet<>();

    //constructors
    public Seat() {
    }

    public Seat(String code, String description) {
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

    public Set<CwSession> getCwSessions() {
        return cwSessions;
    }

    public void setCwSessions(Set<CwSession> cwSessions) {
        this.cwSessions = cwSessions;
    }
}


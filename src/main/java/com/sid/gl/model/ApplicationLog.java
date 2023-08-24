package com.sid.gl.model;

import lombok.*;

import javax.persistence.*;
import java.io.Serializable;
import java.time.LocalDateTime;

@Entity
@Table(name="tb_audit")
@AllArgsConstructor
@NoArgsConstructor
@Getter
@Setter
@ToString
public class ApplicationLog implements Serializable {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    private String username;
    private String Operation;
    private String endPoint;

    private String method;
    private String params;
    private LocalDateTime requestTime;
}

package com.permission.entity;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.Id;
import javax.persistence.Table;

@Data
@Entity
@Table(name = "roles")
@NoArgsConstructor
@AllArgsConstructor
public class Role {
    
    @Id
    @Column(name = "role_id")
    private Integer roleId;
    
    @Column(name = "role_code", unique = true)
    private String roleCode;
}

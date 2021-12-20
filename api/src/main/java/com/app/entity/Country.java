package com.app.entity;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Getter;
import lombok.Setter;
import org.hibernate.validator.constraints.Length;

import javax.persistence.*;
import javax.validation.constraints.Min;
import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotNull;
import java.io.Serializable;
import java.util.List;

@Entity
@Getter
@Setter
public class Country implements Serializable {
    @Id
    @JsonProperty("code")
    @Column(length = 5)
    @Length(min = 2, max = 5,message = "Короткий код")
    @NotNull(message = "Код не может быть пустым")
    String code;

    @JsonProperty("name")
    @NotBlank(message = "Название не может быть пустым")
    @Length(min = 2, max = 150)
    @Column(length = 150,nullable = false)
    String name;

    @JsonProperty("nameEng")
    @NotBlank(message = "Название не может быть пустым")
    @Column(length = 150,nullable = false)
    String nameEng;


    @JsonIgnore
    @OneToMany(mappedBy = "country",fetch = FetchType.LAZY)
    List<Company> companies;

    boolean visible;


    public Country() {
    }

    public Country(String code, String name, String nameEng) {
        this.code = code;
        this.name = name;
        this.nameEng = nameEng;
        this.visible=true;
    }
}

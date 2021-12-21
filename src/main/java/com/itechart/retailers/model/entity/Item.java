package com.itechart.retailers.model.entity;

import lombok.*;

import javax.persistence.*;
import java.util.Set;


@Builder
@NoArgsConstructor
@AllArgsConstructor
@Getter
@Setter
@ToString
@Entity
@Table(name = "item")
public class Item extends Identity {

    @Column(name = "upc", length = 20)
    private String upc;

    @Column(name = "label", length = 45)
    private String label;

    @Column(name = "units")
    private Integer units;

    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "category_id", nullable = false)
    @ToString.Exclude
    private Category category;

    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "customer_id", nullable = false)
    private Customer customer;

    @OneToMany(mappedBy = "item", fetch = FetchType.LAZY)
    @ToString.Exclude
    private Set<ApplicationItem> applicationAssoc;

    public Item(Long id){
        this.setId(id);
    }

}

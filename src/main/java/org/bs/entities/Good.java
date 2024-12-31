package org.bs.entities;

import lombok.*;


@Getter
@Setter
@ToString
@NoArgsConstructor
@Data
public class Good {
    int id;
    String title;
    String image;
    String price;
    String shopName;
    String source;
}

package org.bs.entities;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.checkerframework.checker.builder.qual.CalledMethods;

@Setter
@Getter
@AllArgsConstructor
@NoArgsConstructor

public class History {
    String date;
    String price;
}

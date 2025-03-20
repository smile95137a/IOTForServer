package com.frontend.entity.iot;

import com.frontend.entity.equipment.Router;
import jakarta.persistence.Entity;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;

@EqualsAndHashCode(callSuper = true)
@Data
@NoArgsConstructor
@Entity
public class DeskLamp extends Router {
}

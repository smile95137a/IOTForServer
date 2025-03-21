package com.frontend.factory;

import com.frontend.entity.router.Router;
import com.frontend.entity.iot.AirConditioner;
import com.frontend.entity.iot.Camera;
import com.frontend.entity.iot.DeskLamp;
import com.frontend.entity.iot.Light;
import com.frontend.enums.RouterType;
import org.springframework.stereotype.Component;

@Component
public class RouterFactory {

    public Router createRouter(String routerType) {
        RouterType type = RouterType.fromString(routerType);

        return switch (type) {
            case LIGHT -> new Light();
            case DESKLAMP -> new DeskLamp();
            case AIRCONDITIONER -> new AirConditioner();
            case CAMERA -> new Camera();
        };
    }
}

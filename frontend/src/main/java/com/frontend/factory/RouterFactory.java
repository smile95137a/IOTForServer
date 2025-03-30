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

    public Router createRouter(RouterType routerType) {
        RouterType type = RouterType.fromString(String.valueOf(routerType));

        return switch (type) {
            case LIGHT -> new Light();
            case DESKLAMP -> new DeskLamp();
            case AIRCONDITIONER -> new AirConditioner();
            case CAMERA -> new Camera();
        };
    }
}

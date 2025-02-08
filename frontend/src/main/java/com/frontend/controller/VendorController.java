package com.frontend.controller;

import com.frontend.config.message.ApiResponse;
import com.frontend.utils.ResponseUtils;
import org.springframework.beans.factory.annotation.Autowired;
import com.frontend.entity.store.Store;
import com.frontend.entity.vendor.Vendor;
import com.frontend.res.vendor.VendorRes;
import com.frontend.service.VendorService;
import com.frontend.utils.RandomUtils;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;
import java.util.*;

@RestController
@RequestMapping("/vendors")
public class VendorController {
}
